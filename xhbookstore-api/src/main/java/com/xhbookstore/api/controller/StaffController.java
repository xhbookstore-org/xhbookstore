package com.xhbookstore.api.controller;

import java.util.*;
import java.math.BigDecimal;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.domain.entity.SysUser;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.book.*;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.mapper.SysMenuMapper;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.SysUserMapper;
import com.xhbookstore.system.mapper.member.MemberCardLogMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.book.IBookBorrowService;
import com.xhbookstore.system.service.member.ICardTypeService;
import com.xhbookstore.system.service.member.IMemberCardService;
import com.xhbookstore.system.service.member.IMemberCodeTokenService;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;

@Tag(name = "Staff API", description = "Staff mini-program APIs")
@RestController
@RequestMapping("/api/mp/v1/staff")
public class StaffController {
    @Autowired private IMemberService memberService;
    @Autowired private MemberMapper memberMapper;
    @Autowired private IPointsService pointsService;
    @Autowired private IBookBorrowService bookBorrowService;
    @Autowired private MemberCardLogMapper memberCardLogMapper;
    @Autowired private SysMenuMapper sysMenuMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysDeptMapper sysDeptMapper;
    @Autowired private ICardTypeService cardTypeService;
    @Autowired private IMemberCardService memberCardService;
    @Autowired private IMemberCodeTokenService memberCodeTokenService;

    @Operation(summary = "Staff home")
    @GetMapping("/home")
    public ApiResponse<Map<String,Object>> home(HttpServletRequest request){
        SysUser staff=currentStaff(request);
        Map<String,Object> data=new HashMap<>();
        SysDept dept=staff.getDeptId()!=null?sysDeptMapper.selectDeptById(staff.getDeptId()):null;
        data.put("storeName",dept!=null?dept.getDeptName():"Xinhua Bookstore");
        data.put("storeQrCodeImageUrl",dept!=null?dept.getQrCodeImageUrl():null);
        data.put("todayStoreBorrowCount",staff.getDeptId()!=null?bookBorrowService.countTodayByDeptId(staff.getDeptId()):0);
        data.put("todayStaffBorrowCount",bookBorrowService.countTodayByStaffId(String.valueOf(staff.getUserId())));
        return ApiResponse.success(data);
    }

    @Operation(summary = "Card type list")
    @GetMapping("/card-types")
    public ApiResponse<Map<String,Object>> cardTypes(){
        List<CardType> list=cardTypeService.selectAll();
        List<Map<String,Object>> items=new ArrayList<>();
        for(CardType ct:list){
            Map<String,Object> item=new HashMap<>();
            item.put("id",ct.getId()); item.put("typeName",ct.getTypeName()); item.put("price",ct.getPrice());
            item.put("validDays",ct.getValidDays()); item.put("borrowLimit",ct.getBorrowLimit()); item.put("discount",ct.getDiscount());
            item.put("isRenewal",ct.getIsRenewal()!=null&&ct.getIsRenewal()==1); item.put("description",ct.getDescription());
            item.put("sort",ct.getSort()); item.put("status",ct.getStatus()); items.add(item);
        }
        Map<String,Object> data=new HashMap<>(); data.put("list",items); return ApiResponse.success(data);
    }

    @Operation(summary = "Activate member card")
    @SuppressWarnings("unchecked")
    @PostMapping("/members/{memberId}/activate-card")
    public ApiResponse<Map<String,Object>> activateCard(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){
        String memberCodeToken = stringValue(body.get("memberCodeToken"));
        if(memberCodeToken==null||memberCodeToken.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"memberCodeToken is required");
        Member tokenMember;
        try { tokenMember = memberCodeTokenService.verifyToken(memberCodeToken,"BUY_CARD"); }
        catch (IllegalArgumentException e) { throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage()); }
        if(!String.valueOf(tokenMember.getId()).equals(memberId)) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Member code does not match path memberId");
        Integer cardTypeId=body.get("cardTypeId")!=null?Integer.valueOf(body.get("cardTypeId").toString()):null;
        if(cardTypeId==null) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Card type is required");
        String requestMemberName=requestMemberName(body);
        if(requestMemberName!=null){
            Member update=new Member();
            update.setId(tokenMember.getId());
            update.setName(requestMemberName);
            update.setLastOperator(getStaffName(request));
            memberMapper.updateMember(update);
        }
        AjaxResult result=memberCardService.buyCard(tokenMember.getId(),cardTypeId,toBigDecimal(body.get("paidAmount")),
                stringValue(body.get("paymentType")),getStaffId(request),getStaffName(request),null,requestRemark(body));
        if(result.isError()) throw new ApiException(ApiErrorCode.PARAM_INVALID,String.valueOf(result.get("msg")));
        return ApiResponse.success((Map<String,Object>)result.get("data"));
    }

    @Operation(summary = "Scan member code")
    @PostMapping("/member-code/scan")
    public ApiResponse<Map<String,Object>> scanMemberCode(@RequestBody Map<String,String> body, HttpServletRequest request){
        String token=body.get("memberCodeToken"); if(token==null||token.isEmpty()) token=body.get("scanResult");
        if(token==null||token.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"memberCodeToken is required");
        Member member;
        try { member=memberCodeTokenService.verifyToken(token,"BUY_CARD"); }
        catch (IllegalArgumentException e) { throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage()); }
        memberCardService.refreshMemberCardStatus(member.getId(),getStaffId(request),getStaffName(request),"STAFF_MP_SCAN");
        Map<String,Object> data=new HashMap<>();
        data.put("memberId",String.valueOf(member.getId()));
        data.put("memberNo",member.getCardNo());
        data.put("memberName",member.getName());
        data.put("phoneDisplay",maskPhone(member.getPhone()));
        data.put("cards",memberCardService.getMemberCardView(member.getId()).get("cards"));
        return ApiResponse.success(data);
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Buy member card by member code token")
    @PostMapping("/member-cards/buy")
    public ApiResponse<Map<String,Object>> buyMemberCard(@RequestBody Map<String,Object> body,HttpServletRequest request){
        String memberCodeToken=stringValue(body.get("memberCodeToken"));
        if(memberCodeToken==null||memberCodeToken.isEmpty()) memberCodeToken=stringValue(body.get("scanResult"));
        Integer cardTypeId=body.get("cardTypeId")!=null?Integer.valueOf(body.get("cardTypeId").toString()):null;
        BigDecimal paidAmount=toBigDecimal(body.get("paidAmount"));
        AjaxResult result;
        try {
            result=memberCardService.buyCardByToken(memberCodeToken,cardTypeId,paidAmount,
                    stringValue(body.get("paymentType")),getStaffId(request),getStaffName(request),null,requestRemark(body));
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage());
        }
        if(result.isError()) throw new ApiException(ApiErrorCode.PARAM_INVALID,String.valueOf(result.get("msg")));
        return ApiResponse.success((Map<String,Object>)result.get("data"));
    }

    @Operation(summary = "Member overview")
    @GetMapping("/members/{memberId}/overview")
    public ApiResponse<Map<String,Object>> memberOverview(@PathVariable String memberId,HttpServletRequest request){
        Member member=memberService.selectMemberById(Integer.parseInt(memberId)); if(member==null) throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        List<BookBorrowOrder> orders=bookBorrowService.selectByMemberId(member.getId()); int currentBorrowingCount=0;
        if(orders!=null) for(BookBorrowOrder o:orders){ List<BookBorrowDetail> ds=bookBorrowService.selectDetailsByOrderId(o.getId()); if(ds!=null) for(BookBorrowDetail d:ds) currentBorrowingCount+=Math.max(0,remainingQty(d)); }
        Map<String,Object> memberMap=new HashMap<>(); memberMap.put("memberId",String.valueOf(member.getId())); memberMap.put("memberNo",member.getCardNo()); memberMap.put("memberName",member.getName()); memberMap.put("phoneDisplay",maskPhone(member.getPhone())); memberMap.put("currentPoints",member.getCurrentPoints()); memberMap.put("currentBorrowingCount",currentBorrowingCount); memberMap.put("yearBorrowCount",orders!=null?orders.size():0); memberMap.put("card",buildMemberCard(member));
        Map<String,Object> cardView=memberCardService.getMemberCardView(member.getId());
        MemberCard activeCard=cardView.get("activeCard") instanceof MemberCard?(MemberCard)cardView.get("activeCard"):null;
        memberMap.put("remark",member.getRemark()!=null&&!member.getRemark().trim().isEmpty()?member.getRemark():activeCard!=null?activeCard.getRemark():null);
        CardType activeCardType=activeCard!=null&&activeCard.getCardTypeId()!=null?cardTypeService.selectById(activeCard.getCardTypeId()):null;
        boolean memberActive=member.getStatus()==null||member.getStatus()!=1;
        boolean hasBorrowCard=activeCard!=null&&activeCardType!=null&&activeCardType.getBorrowLimit()!=null&&activeCardType.getBorrowLimit()>0;
        boolean hasBorrowQuota=hasBorrowCard&&currentBorrowingCount<activeCardType.getBorrowLimit();
        boolean canBorrow=hasStaffPerm(request,"book:borrow:add","book:borrow:create","staff:borrow:add","staff:borrow:create")&&memberActive&&hasBorrowCard&&hasBorrowQuota;
        boolean canReturn=hasStaffPerm(request,"book:return:add","book:return:create","staff:return:add","staff:return:create")&&currentBorrowingCount>0;
        Map<String,Object> availability=new HashMap<>(); availability.put("canBorrow",canBorrow); availability.put("borrowDisabledReason",borrowDisabledReason(memberActive,hasBorrowCard,hasBorrowQuota)); availability.put("canReturn",canReturn); availability.put("returnDisabledReason",currentBorrowingCount>0?null:"No borrowing books"); availability.put("canAdjustPoints",true); availability.put("canOpenBorrowCard",member.getCardTypeId()==null||member.getCardTypeId()==1); availability.put("canRenewBorrowCard",member.getCardTypeId()!=null&&member.getCardTypeId()!=1); availability.put("maxAddPoints",99999); availability.put("maxDeductPoints",member.getCurrentPoints()!=null?member.getCurrentPoints():0);
        Map<String,Object> data=new HashMap<>(); data.put("member",memberMap); data.put("availability",availability); return ApiResponse.success(data);
    }

    /**
     * 员工扫码后查询会员卡；返回结构与用户端 GET /user/member-cards 保持一致。
     */
    @Operation(summary = "Query scanned member cards")
    @GetMapping("/members/{memberId}/member-cards")
    public ApiResponse<Map<String,Object>> memberCards(@PathVariable String memberId, HttpServletRequest request) {
        currentStaff(request);
        Integer memberIdValue;
        try {
            memberIdValue = Integer.valueOf(memberId);
        } catch (NumberFormatException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "memberId must be a number");
        }
        Member member = memberService.selectMemberById(memberIdValue);
        if (member == null) throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        return ApiResponse.success(visibleMemberCardView(memberCardService.getMemberCardView(memberIdValue)));
    }

    @Operation(summary = "Borrow list")
    @GetMapping("/borrows")
    public ApiResponse<Map<String,Object>> borrowsList(@RequestParam(required=false) String phone,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize,HttpServletRequest request){ validatePage(pageNo,pageSize); Integer statusValue=status!=null&&!status.isEmpty()?Integer.parseInt(status):null; int offset=(pageNo-1)*pageSize; List<Map<String,Object>> list=bookBorrowService.selectBorrowDetailPage(phone,statusValue,null,false,offset,pageSize); long total=bookBorrowService.countBorrowDetailPage(phone,statusValue,null,false); Map<String,Object> data=new HashMap<>(); data.put("page",new PageResult<>(list,pageNo,pageSize,total)); return ApiResponse.success(data); }

    @Operation(summary = "Borrow detail")
    @GetMapping("/borrows/{detailId}")
    public ApiResponse<Map<String,Object>> borrowDetail(@PathVariable String detailId){
        BookBorrowDetail detail=bookBorrowService.selectDetailById(parseLong(detailId,"detailId"));
        if(detail==null) throw new ApiException(ApiErrorCode.NOT_FOUND,"Borrow detail not found");
        BookBorrowOrder order=bookBorrowService.selectOrderByNo(detail.getBorrowOrderNo());
        List<BookReturnDetail> returns=order!=null?bookBorrowService.selectReturnsByOrderId(order.getId()):Collections.emptyList();
        Map<String,Object> data=new HashMap<>();
        data.put("item",buildFlatItem(order,detail));
        data.put("order",order);
        data.put("returns",filterReturns(returns,detail.getId()));
        data.put("images",bookBorrowService.selectImagesByDetailId(detail.getId()));
        data.put("orderNo",detail.getBorrowOrderNo());
        data.put("borrowTime",detail.getBorrowTime()!=null?detail.getBorrowTime().getTime():null);
        return ApiResponse.success(data);
    }

    @Operation(summary = "Return one borrowed book")
    @PostMapping("/borrow-returns")
    public ApiResponse<Map<String,Object>> returnBooks(@RequestBody Map<String,Object> body,HttpServletRequest request){
        Long borrowDetailId=parseLong(stringValue(body.get("borrowDetailId")),"borrowDetailId");
        String returnCondition=body.get("returnCondition")==null?null:String.valueOf(body.get("returnCondition")).trim();
        Integer points=parseOptionalNonNegativeInt(body.get("points"),"points");
        AjaxResult result=bookBorrowService.returnBook(borrowDetailId,returnCondition,points,
                body.get("remark")==null?null:String.valueOf(body.get("remark")),
                getStaffId(request),getStaffName(request),null);
        if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED,(String)result.get("msg"));
        return ApiResponse.success((Map<String,Object>)result.get("data"));
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Borrow to purchase")
    @PostMapping("/borrow-purchases")
    public ApiResponse<Map<String,Object>> borrowPurchases(@RequestBody Map<String,Object> body,HttpServletRequest request){ List<Map<String,Object>> items=(List<Map<String,Object>>)body.get("items"); if(items==null||items.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Purchase items are required"); AjaxResult result=bookBorrowService.borrowToPurchase(items,getStaffId(request),getStaffName(request),null); if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED,(String)result.get("msg")); return ApiResponse.success((Map<String,Object>)result.get("data")); }

    @Operation(summary = "Member borrow records")
    @GetMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String,Object>> memberBorrows(@PathVariable String memberId,@RequestParam(required=false) String mode,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize){ validatePage(pageNo,pageSize); Integer memberIdValue=Integer.parseInt(memberId); Member member=memberService.selectMemberById(memberIdValue); if(member==null) throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND); Integer statusValue=status!=null&&!status.isEmpty()?Integer.parseInt(status):null; boolean borrowingOnly="current".equals(mode); int offset=(pageNo-1)*pageSize; List<Map<String,Object>> list=bookBorrowService.selectBorrowDetailPage(null,statusValue,memberIdValue,borrowingOnly,offset,pageSize); long total=bookBorrowService.countBorrowDetailPage(null,statusValue,memberIdValue,borrowingOnly); Map<String,Object> data=new HashMap<>(); Map<String,Object> m=new HashMap<>(); m.put("memberId",String.valueOf(member.getId())); m.put("memberNo",member.getCardNo()); m.put("memberName",member.getName()); m.put("phoneDisplay",maskPhone(member.getPhone())); m.put("currentPoints",member.getCurrentPoints()); data.put("member",m); data.put("page",new PageResult<>(list,pageNo,pageSize,total)); return ApiResponse.success(data); }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Create borrow order")
    @PostMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String,Object>> borrow(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ List<Map<String,Object>> books=(List<Map<String,Object>>)body.get("books"); if(books==null||books.isEmpty()) throw new ApiException(ApiErrorCode.BORROW_BOOK_REQUIRED); SysUser staff=currentStaff(request); AjaxResult result=bookBorrowService.createBorrowOrder(Integer.parseInt(memberId),books,(String)body.get("remark"),String.valueOf(staff.getUserId()),getStaffName(request),staff.getDeptId()); if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_DENIED,(String)result.get("msg")); return ApiResponse.success((Map<String,Object>)result.get("data")); }

    @Operation(summary = "Point reasons")
    @GetMapping("/points-reasons")
    public ApiResponse<Map<String,Object>> pointsReasons(@RequestParam String direction,@RequestParam(required=false) String memberId){ List<Map<String,Object>> list=new ArrayList<>(); Map<String,Object> r1=new HashMap<>(); r1.put("reasonId","1"); r1.put("reasonName","activity_bonus"); r1.put("enabled",true); r1.put("defaultPoints",50); list.add(r1); Map<String,Object> r2=new HashMap<>(); r2.put("reasonId","2"); r2.put("reasonName","borrow_reward"); r2.put("enabled",true); r2.put("defaultPoints",10); list.add(r2); Map<String,Object> data=new HashMap<>(); data.put("list",list); data.put("maxPoints",99999); return ApiResponse.success(data); }

    @Operation(summary = "Add points")
    @PostMapping("/members/{memberId}/points/add")
    public ApiResponse<Map<String,Object>> addPoints(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ int points=parseInt(body.get("points"),0); if(points<=0) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Points must be positive"); AjaxResult result=pointsService.addPoints(Integer.parseInt(memberId),points,(String)body.get("remark"),getStaffName(request),"mini-program"); if(result.isError()) throw new ApiException(ApiErrorCode.POINTS_OPERATION_DENIED,(String)result.get("msg")); Map<String,Object> data=new HashMap<>(); data.put("success",true); data.put("pointsDelta",points); data.put("operatedAt",System.currentTimeMillis()); return ApiResponse.success(data); }

    @Operation(summary = "Deduct points")
    @PostMapping("/members/{memberId}/points/deduct")
    public ApiResponse<Map<String,Object>> deductPoints(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ int points=parseInt(body.get("points"),0); if(points<=0) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Points must be positive"); AjaxResult result=pointsService.deductPoints(Integer.parseInt(memberId),points,(String)body.get("remark"),getStaffName(request),"mini-program"); if(result.isError()) throw new ApiException(ApiErrorCode.POINTS_OPERATION_DENIED,(String)result.get("msg")); Map<String,Object> data=new HashMap<>(); data.put("success",true); data.put("pointsDelta",-points); data.put("operatedAt",System.currentTimeMillis()); return ApiResponse.success(data); }

    @Operation(summary = "Point records")
    @GetMapping("/points-records")
    public ApiResponse<Map<String,Object>> pointsRecordsList(@RequestParam(required=false) String phone,@RequestParam(required=false) String memberId,@RequestParam(required=false) String direction,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize){ validatePage(pageNo,pageSize); if(direction!=null&&!direction.isEmpty()&&!"add".equals(direction)&&!"deduct".equals(direction)) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Invalid direction"); Integer memberIdValue=memberId!=null&&!memberId.isEmpty()?Integer.parseInt(memberId):null; int offset=(pageNo-1)*pageSize; List<PointsOrder> orders=pointsService.selectPage(phone,memberIdValue,direction,offset,pageSize); long total=pointsService.countPage(phone,memberIdValue,direction); List<Map<String,Object>> records=new ArrayList<>(); for(PointsOrder o:orders){ Map<String,Object> r=new HashMap<>(); r.put("pointsRecordId",o.getOrderNumber()); r.put("reasonName",o.getDescription()); r.put("direction",o.getAmount()!=null&&o.getAmount()>0?"add":"deduct"); r.put("pointsDelta",o.getAmount()); r.put("beforePoints",o.getOrginPoints()); r.put("afterPoints",o.getAfterPoints()); r.put("operatedAt",o.getCreatedAt()!=null?o.getCreatedAt().getTime():null); r.put("staffName",o.getOperationDevice()); records.add(r); } Map<String,Object> data=new HashMap<>(); data.put("page",new PageResult<>(records,pageNo,pageSize,total)); return ApiResponse.success(data); }

    @Operation(summary = "Point detail")
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String,Object>> pointsDetail(@PathVariable String pointsRecordId){ PointsOrder order=pointsService.selectByOrderNumber(pointsRecordId); if(order==null||order.getIsDel()!=null&&order.getIsDel()!=0) throw new ApiException(ApiErrorCode.NOT_FOUND,"Points record not found"); Map<String,Object> data=new HashMap<>(); data.put("pointsRecordId",order.getOrderNumber()); data.put("memberId",order.getMemberId()); data.put("reasonName",order.getDescription()); data.put("direction",order.getOrderNumber()!=null&&order.getOrderNumber().startsWith("IN")?"add":"deduct"); data.put("pointsDelta",order.getAmount()); data.put("beforePoints",order.getOrginPoints()); data.put("afterPoints",order.getAfterPoints()); data.put("operatedAt",order.getCreatedAt()!=null?order.getCreatedAt().getTime():null); data.put("operationDevice",order.getOperationDevice()); return ApiResponse.success(data); }

    private Map<String,Object> buildMemberCard(Member m){ Map<String,Object> card=new HashMap<>(); card.put("cardTypeId",m.getCardTypeId()); card.put("cardTypeName",m.getCardTypeName()); card.put("memberNo",m.getCardNo()); card.put("cardStatus",m.getStatus()!=null&&m.getStatus()==0?"active":"inactive"); card.put("level",m.getLevelId()); card.put("remainingDays",m.getValidDate()!=null?Math.max(0,(m.getValidDate().getTime()-System.currentTimeMillis())/86400000L):0); card.put("effectiveAt",m.getCreatedAt()!=null?m.getCreatedAt().getTime():null); card.put("expiredAt",m.getValidDate()!=null?m.getValidDate().getTime():null); return card; }
    private Map<String,Object> visibleMemberCardView(Map<String,Object> cardView) {
        if (cardView == null) return Collections.emptyMap();
        Object cardsValue = cardView.get("cards");
        if (!(cardsValue instanceof List<?> cards)) return cardView;
        List<MemberCard> visibleCards = new ArrayList<>();
        List<MemberCard> pendingCards = new ArrayList<>();
        MemberCard activeCard = null;
        for (Object item : cards) {
            if (!(item instanceof MemberCard card)) continue;
            Integer status = card.getStatus();
            if (status == null || (status != 0 && status != 1)) continue;
            visibleCards.add(card);
            if (status == 1 && activeCard == null) activeCard = card;
            if (status == 0) pendingCards.add(card);
        }
        Map<String,Object> data = new HashMap<>(cardView);
        data.put("activeCard", activeCard);
        data.put("pendingCards", pendingCards);
        data.put("cards", visibleCards);
        data.put("hasActiveCard", activeCard != null);
        return data;
    }
    private Map<String,Object> buildFlatItem(BookBorrowOrder o,BookBorrowDetail d){ Map<String,Object> item=new HashMap<>(); item.put("detailId",d.getId()); item.put("borrowDetailId",d.getId()); item.put("orderNo",d.getBorrowOrderNo()); item.put("memberId",d.getMemberId()); item.put("bookCode",d.getBookCode()); item.put("bookName",d.getBookName()); item.put("borrowStatus",d.getBorrowStatus()!=null?d.getBorrowStatus():0); item.put("borrowQty",d.getBorrowQty()!=null?d.getBorrowQty():0); item.put("returnedQty",d.getReturnedQty()!=null?d.getReturnedQty():0); item.put("purchaseQty",d.getPurchaseQty()!=null?d.getPurchaseQty():0); item.put("remainingQty",remainingQty(d)); item.put("purchaseOrderNo",d.getPurchaseOrderNo()); item.put("borrowTime",timeMillis(d.getBorrowTime())); item.put("returnAllTime",d.getReturnAllTime()!=null?timeMillis(d.getReturnAllTime()):(o!=null?timeMillis(o.getReturnAllTime()):null)); item.put("expectedReturnTime",o!=null?timeMillis(o.getExpectedReturnTime()):null); item.put("remark",d.getRemark()!=null?d.getRemark():(o!=null?o.getRemark():null)); return item; }
    private List<BookReturnDetail> filterReturns(List<BookReturnDetail> returns,Long detailId){ List<BookReturnDetail> list=new ArrayList<>(); if(returns==null) return list; for(BookReturnDetail r:returns){ if(r.getBorrowDetailId()!=null&&r.getBorrowDetailId().equals(detailId)) list.add(r); } return list; }
    private void validatePage(int pageNo,int pageSize){ if(pageNo<1) throw new ApiException(ApiErrorCode.PARAM_INVALID,"pageNo must be greater than 0"); if(pageSize<1||pageSize>100) throw new ApiException(ApiErrorCode.PARAM_INVALID,"pageSize must be between 1 and 100"); }
    private SysUser currentStaff(HttpServletRequest request){ Object attr=request.getAttribute("staffUserId"); if(attr==null) throw new ApiException(ApiErrorCode.FORBIDDEN,"No staff permission"); SysUser staff=sysUserMapper.selectUserById(Long.valueOf(attr.toString())); if(staff==null) throw new ApiException(ApiErrorCode.FORBIDDEN,"Staff not found"); return staff; }
    private List<Long> visibleDeptIds(HttpServletRequest request){ SysUser staff=currentStaff(request); if(staff.isAdmin()) return null; List<Long> ids=new ArrayList<>(); if(staff.getDeptId()!=null){ ids.add(staff.getDeptId()); List<SysDept> children=sysDeptMapper.selectChildrenDeptById(staff.getDeptId()); if(children!=null) for(SysDept d:children){ if("0".equals(d.getStatus())) ids.add(d.getDeptId()); } } return ids; }
    private String getStaffId(HttpServletRequest request){ Object attr=request.getAttribute("staffUserId"); return attr!=null?String.valueOf(attr):"system"; }
    private String getStaffName(HttpServletRequest request){ SysUser staff=currentStaff(request); return staff.getNickName()!=null&&!staff.getNickName().trim().isEmpty()?staff.getNickName():staff.getUserName(); }
    private boolean hasStaffPerm(HttpServletRequest request,String... perms){ SysUser staff=currentStaff(request); if(staff.isAdmin()) return true; Set<String> candidates=new HashSet<>(Arrays.asList(perms)); List<String> allPerms=sysMenuMapper.selectMenuPerms(); boolean configured=false; if(allPerms!=null) for(String p:allPerms){ if(candidates.contains(p)){ configured=true; break; } } if(!configured) return true; List<String> userPerms=sysMenuMapper.selectMenuPermsByUserId(staff.getUserId()); if(userPerms==null||userPerms.isEmpty()) return false; Set<String> set=new HashSet<>(); for(String p:userPerms){ if(p!=null&&!p.isEmpty()) set.add(p); } if(set.contains("*:*:*")) return true; for(String p:perms){ if(set.contains(p)) return true; } return false; }
    private String borrowDisabledReason(boolean memberActive,boolean hasBorrowCard,boolean hasBorrowQuota){ if(!memberActive) return "Member inactive"; if(!hasBorrowCard) return "No active borrow card"; if(!hasBorrowQuota) return "Borrow limit reached"; return null; }
    private int remainingQty(BookBorrowDetail d){ int b=d.getBorrowQty()!=null?d.getBorrowQty():0; int r=d.getReturnedQty()!=null?d.getReturnedQty():0; int p=d.getPurchaseQty()!=null?d.getPurchaseQty():0; return b-r-p; }
    private Long parseLong(String v,String fieldName){ try{return Long.valueOf(v);}catch(Exception e){ throw new ApiException(ApiErrorCode.PARAM_INVALID,fieldName+" must be a number"); } }
    private Integer parseOptionalNonNegativeInt(Object value,String fieldName){ if(value==null||String.valueOf(value).trim().isEmpty()) return null; try{ int parsed=Integer.parseInt(String.valueOf(value).trim()); if(parsed<0) throw new NumberFormatException(); return parsed; }catch(Exception e){ throw new ApiException(ApiErrorCode.PARAM_INVALID,fieldName+" must be a non-negative integer"); } }
    private int parseInt(Object v,int def){ if(v==null) return def; return Integer.parseInt(v.toString()); }
    private String stringValue(Object v){ return v!=null?String.valueOf(v):null; }
    private String requestRemark(Map<String,Object> body){ String v=stringValue(body.get("remark")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("remarks")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("memo")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("note")); return v!=null?v.trim():null; }
    private String requestMemberName(Map<String,Object> body){ String v=stringValue(body.get("memberName")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("name")); return v!=null&&!v.trim().isEmpty()?v.trim():null; }
    private BigDecimal toBigDecimal(Object v){ return v!=null&&String.valueOf(v).trim().length()>0?new BigDecimal(String.valueOf(v)):null; }
    private Long timeMillis(Date date){ return date!=null?Long.valueOf(date.getTime()):null; }
    private String maskPhone(String phone){ if(phone==null||phone.length()<7) return phone; return phone.substring(0,3)+"****"+phone.substring(phone.length()-4); }
}
