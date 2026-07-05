package com.xhbookstore.api.controller;

import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.book.*;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
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
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private ICardTypeService cardTypeService;
    @Autowired private IMemberCardService memberCardService;
    @Autowired private IMemberCodeTokenService memberCodeTokenService;

    @Operation(summary = "Staff home")
    @GetMapping("/home")
    public ApiResponse<Map<String,Object>> home(){
        Map<String,Object> data=new HashMap<>();
        data.put("storeName","Xinhua Bookstore");
        data.put("todayStoreBorrowCount",12);
        data.put("todayStaffBorrowCount",3);
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
        try { tokenMember = memberCodeTokenService.consumeToken(memberCodeToken,"BUY_CARD"); }
        catch (IllegalArgumentException e) { throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage()); }
        if(!String.valueOf(tokenMember.getId()).equals(memberId)) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Member code does not match path memberId");
        Integer cardTypeId=body.get("cardTypeId")!=null?Integer.valueOf(body.get("cardTypeId").toString()):null;
        if(cardTypeId==null) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Card type is required");
        AjaxResult result=memberCardService.buyCard(tokenMember.getId(),cardTypeId,toBigDecimal(body.get("paidAmount")),
                stringValue(body.get("paymentType")),getStaffId(request),"staff",null,requestRemark(body));
        if(result.isError()) throw new ApiException(ApiErrorCode.PARAM_INVALID,String.valueOf(result.get("msg")));
        return ApiResponse.success((Map<String,Object>)result.get("data"));
    }

    @Operation(summary = "Scan member code")
    @PostMapping("/member-code/scan")
    public ApiResponse<Map<String,Object>> scanMemberCode(@RequestBody Map<String,String> body){
        String token=body.get("memberCodeToken"); if(token==null||token.isEmpty()) token=body.get("scanResult");
        if(token==null||token.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"memberCodeToken is required");
        Member member;
        try { member=memberCodeTokenService.verifyToken(token,"BUY_CARD"); }
        catch (IllegalArgumentException e) { throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage()); }
        memberCardService.refreshMemberCardStatus(member.getId(),"system","staff","STAFF_MP_SCAN");
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
                    stringValue(body.get("paymentType")),getStaffId(request),"staff",null,requestRemark(body));
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID,e.getMessage());
        }
        if(result.isError()) throw new ApiException(ApiErrorCode.PARAM_INVALID,String.valueOf(result.get("msg")));
        return ApiResponse.success((Map<String,Object>)result.get("data"));
    }

    @Operation(summary = "Member overview")
    @GetMapping("/members/{memberId}/overview")
    public ApiResponse<Map<String,Object>> memberOverview(@PathVariable String memberId){
        Member member=memberService.selectMemberById(Integer.parseInt(memberId)); if(member==null) throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        List<BookBorrowOrder> orders=bookBorrowService.selectByMemberId(member.getId()); int currentBorrowingCount=0;
        if(orders!=null) for(BookBorrowOrder o:orders){ List<BookBorrowDetail> ds=bookBorrowService.selectDetailsByOrderId(o.getId()); if(ds!=null) for(BookBorrowDetail d:ds) currentBorrowingCount+=Math.max(0,remainingQty(d)); }
        Map<String,Object> memberMap=new HashMap<>(); memberMap.put("memberId",String.valueOf(member.getId())); memberMap.put("memberNo",member.getCardNo()); memberMap.put("memberName",member.getName()); memberMap.put("phoneDisplay",maskPhone(member.getPhone())); memberMap.put("currentPoints",member.getCurrentPoints()); memberMap.put("currentBorrowingCount",currentBorrowingCount); memberMap.put("yearBorrowCount",orders!=null?orders.size():0); memberMap.put("card",buildMemberCard(member));
        Map<String,Object> availability=new HashMap<>(); availability.put("canBorrow",true); availability.put("canReturn",currentBorrowingCount>0); availability.put("returnDisabledReason",currentBorrowingCount>0?null:"No borrowing books"); availability.put("canAdjustPoints",true); availability.put("canOpenBorrowCard",member.getCardTypeId()==null||member.getCardTypeId()==1); availability.put("canRenewBorrowCard",member.getCardTypeId()!=null&&member.getCardTypeId()!=1); availability.put("maxAddPoints",99999); availability.put("maxDeductPoints",member.getCurrentPoints()!=null?member.getCurrentPoints():0);
        Map<String,Object> data=new HashMap<>(); data.put("member",memberMap); data.put("availability",availability); return ApiResponse.success(data);
    }

    @Operation(summary = "Borrow list")
    @GetMapping("/borrows")
    public ApiResponse<Map<String,Object>> borrowsList(@RequestParam(required=false) String phone,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize){ Map<String,Object> data=new HashMap<>(); data.put("page",new PageResult<>(Collections.emptyList(),pageNo,pageSize,0)); return ApiResponse.success(data); }

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
        data.put("orderNo",detail.getBorrowOrderNo());
        data.put("borrowTime",detail.getBorrowTime()!=null?detail.getBorrowTime().getTime():null);
        return ApiResponse.success(data);
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Return books")
    @PostMapping("/borrow-returns")
    public ApiResponse<Map<String,Object>> returnBooks(@RequestBody Map<String,Object> body,HttpServletRequest request){
        List<Map<String,Object>> returnItems=new ArrayList<>(); Object obj=body.get("returnItems");
        if(obj instanceof List<?>){ for(Object row:(List<?>)obj){ if(row instanceof Map<?,?>){ Map<?,?> raw=(Map<?,?>)row; Map<String,Object> item=new HashMap<>(); item.put("borrowDetailId",raw.get("borrowDetailId")); item.put("returnQty",raw.get("returnQty")); item.put("returnType",raw.get("returnType")!=null?raw.get("returnType"):1); item.put("remark",raw.get("remark")); returnItems.add(item); } } }
        else { List<Object> ids=(List<Object>)body.get("borrowDetailIds"); if(ids!=null) for(Object idObj:ids){ Long detailId=Long.valueOf(idObj.toString()); BookBorrowDetail detail=bookBorrowService.selectDetailById(detailId); if(detail==null) throw new ApiException(ApiErrorCode.NOT_FOUND,"Borrow detail not found: "+detailId); Map<String,Object> item=new HashMap<>(); item.put("borrowDetailId",detailId); item.put("returnQty",remainingQty(detail)); item.put("returnType",1); returnItems.add(item); } }
        if(returnItems.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Return items are required");
        Map<String,List<Map<String,Object>>> byOrder=new LinkedHashMap<>();
        for(Map<String,Object> item:returnItems){ Long detailId=Long.valueOf(item.get("borrowDetailId").toString()); BookBorrowDetail detail=bookBorrowService.selectDetailById(detailId); if(detail==null) throw new ApiException(ApiErrorCode.NOT_FOUND,"Borrow detail not found: "+detailId); int qty=parseInt(item.get("returnQty"),0); if(qty<=0||qty>remainingQty(detail)) throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED,"Invalid return quantity"); item.put("borrowDetailId",detailId); item.put("returnQty",qty); item.put("returnType",parseInt(item.get("returnType"),1)); byOrder.computeIfAbsent(detail.getBorrowOrderNo(),k->new ArrayList<>()).add(item); }
        List<String> returnOrderNos=new ArrayList<>(); int totalReturned=0; String traceId=null;
        for(Map.Entry<String,List<Map<String,Object>>> e:byOrder.entrySet()){ AjaxResult result=bookBorrowService.returnBook(e.getKey(),e.getValue(),getStaffId(request),"staff",null); if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED,(String)result.get("msg")); Map<String,Object> rd=(Map<String,Object>)result.get("data"); if(rd!=null){ Object nos=rd.get("returnOrderNos"); if(nos instanceof Collection<?>) for(Object no:(Collection<?>)nos) returnOrderNos.add(String.valueOf(no)); if(rd.get("totalReturned")!=null) totalReturned+=Integer.parseInt(rd.get("totalReturned").toString()); if(rd.get("traceId")!=null) traceId=rd.get("traceId").toString(); } }
        Map<String,Object> data=new HashMap<>(); data.put("returnOrderNos",returnOrderNos); data.put("totalReturned",totalReturned); data.put("traceId",traceId); return ApiResponse.success(data);
    }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Borrow to purchase")
    @PostMapping("/borrow-purchases")
    public ApiResponse<Map<String,Object>> borrowPurchases(@RequestBody Map<String,Object> body,HttpServletRequest request){ List<Map<String,Object>> items=(List<Map<String,Object>>)body.get("items"); if(items==null||items.isEmpty()) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Purchase items are required"); AjaxResult result=bookBorrowService.borrowToPurchase(items,getStaffId(request),"staff",null); if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED,(String)result.get("msg")); return ApiResponse.success((Map<String,Object>)result.get("data")); }

    @Operation(summary = "Member borrow records")
    @GetMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String,Object>> memberBorrows(@PathVariable String memberId,@RequestParam(required=false) String mode,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize){ Member member=memberService.selectMemberById(Integer.parseInt(memberId)); if(member==null) throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND); List<Map<String,Object>> list=flattenBorrowDetails(bookBorrowService.selectByMemberId(Integer.parseInt(memberId)),"current".equals(mode)?"borrowing":"all"); Map<String,Object> data=new HashMap<>(); Map<String,Object> m=new HashMap<>(); m.put("memberId",String.valueOf(member.getId())); m.put("memberNo",member.getCardNo()); m.put("memberName",member.getName()); m.put("phoneDisplay",maskPhone(member.getPhone())); m.put("currentPoints",member.getCurrentPoints()); data.put("member",m); data.put("page",new PageResult<>(list,pageNo,pageSize,list.size())); return ApiResponse.success(data); }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Create borrow order")
    @PostMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String,Object>> borrow(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ List<Map<String,Object>> books=(List<Map<String,Object>>)body.get("books"); if(books==null||books.isEmpty()) throw new ApiException(ApiErrorCode.BORROW_BOOK_REQUIRED); List<String> imageUrls=null; Object imgs=body.get("imageUrls"); if(imgs instanceof List<?>) imageUrls=((List<?>)imgs).stream().map(Object::toString).collect(Collectors.toList()); AjaxResult result=bookBorrowService.createBorrowOrder(Integer.parseInt(memberId),books,(String)body.get("remark"),getStaffId(request),"staff",null,imageUrls); if(result.isError()) throw new ApiException(ApiErrorCode.BORROW_DENIED,(String)result.get("msg")); return ApiResponse.success((Map<String,Object>)result.get("data")); }

    @Operation(summary = "Point reasons")
    @GetMapping("/points-reasons")
    public ApiResponse<Map<String,Object>> pointsReasons(@RequestParam String direction,@RequestParam(required=false) String memberId){ List<Map<String,Object>> list=new ArrayList<>(); Map<String,Object> r1=new HashMap<>(); r1.put("reasonId","1"); r1.put("reasonName","activity_bonus"); r1.put("enabled",true); r1.put("defaultPoints",50); list.add(r1); Map<String,Object> r2=new HashMap<>(); r2.put("reasonId","2"); r2.put("reasonName","borrow_reward"); r2.put("enabled",true); r2.put("defaultPoints",10); list.add(r2); Map<String,Object> data=new HashMap<>(); data.put("list",list); data.put("maxPoints",99999); return ApiResponse.success(data); }

    @Operation(summary = "Add points")
    @PostMapping("/members/{memberId}/points/add")
    public ApiResponse<Map<String,Object>> addPoints(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ int points=parseInt(body.get("points"),0); if(points<=0) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Points must be positive"); AjaxResult result=pointsService.addPoints(Integer.parseInt(memberId),points,(String)body.get("remark"),getStaffId(request),"mini-program"); if(result.isError()) throw new ApiException(ApiErrorCode.POINTS_OPERATION_DENIED,(String)result.get("msg")); Map<String,Object> data=new HashMap<>(); data.put("success",true); data.put("pointsDelta",points); data.put("operatedAt",System.currentTimeMillis()); return ApiResponse.success(data); }

    @Operation(summary = "Deduct points")
    @PostMapping("/members/{memberId}/points/deduct")
    public ApiResponse<Map<String,Object>> deductPoints(@PathVariable String memberId,@RequestBody Map<String,Object> body,HttpServletRequest request){ int points=parseInt(body.get("points"),0); if(points<=0) throw new ApiException(ApiErrorCode.PARAM_INVALID,"Points must be positive"); AjaxResult result=pointsService.deductPoints(Integer.parseInt(memberId),points,(String)body.get("remark"),getStaffId(request),"mini-program"); if(result.isError()) throw new ApiException(ApiErrorCode.POINTS_OPERATION_DENIED,(String)result.get("msg")); Map<String,Object> data=new HashMap<>(); data.put("success",true); data.put("pointsDelta",-points); data.put("operatedAt",System.currentTimeMillis()); return ApiResponse.success(data); }

    @Operation(summary = "Point records")
    @GetMapping("/points-records")
    public ApiResponse<Map<String,Object>> pointsRecordsList(@RequestParam(required=false) String phone,@RequestParam(required=false) String memberId,@RequestParam(required=false) String direction,@RequestParam(defaultValue="1") int pageNo,@RequestParam(defaultValue="20") int pageSize){ List<PointsOrder> orders=memberId!=null&&!memberId.isEmpty()?pointsService.selectByMemberId(Integer.parseInt(memberId)):Collections.emptyList(); List<Map<String,Object>> records=new ArrayList<>(); for(PointsOrder o:orders){ Map<String,Object> r=new HashMap<>(); r.put("pointsRecordId",o.getOrderNumber()); r.put("reasonName",o.getDescription()); r.put("direction",o.getOrderNumber().startsWith("IN")?"add":"deduct"); r.put("pointsDelta",o.getAmount()); r.put("beforePoints",o.getOrginPoints()); r.put("afterPoints",o.getAfterPoints()); r.put("operatedAt",o.getCreatedAt().getTime()); r.put("staffName",o.getOperationDevice()); records.add(r); } Map<String,Object> data=new HashMap<>(); data.put("page",new PageResult<>(records,pageNo,pageSize,records.size())); return ApiResponse.success(data); }

    @Operation(summary = "Point detail")
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String,Object>> pointsDetail(@PathVariable String pointsRecordId){ Map<String,Object> data=new HashMap<>(); data.put("pointsRecordId",pointsRecordId); return ApiResponse.success(data); }

    private Map<String,Object> buildMemberCard(Member m){ Map<String,Object> card=new HashMap<>(); card.put("cardTypeId",m.getCardTypeId()); card.put("cardTypeName",m.getCardTypeName()); card.put("memberNo",m.getCardNo()); card.put("cardStatus",m.getStatus()!=null&&m.getStatus()==0?"active":"inactive"); card.put("level",m.getLevelId()); card.put("remainingDays",m.getValidDate()!=null?Math.max(0,(m.getValidDate().getTime()-System.currentTimeMillis())/86400000L):0); card.put("effectiveAt",m.getCreatedAt()!=null?m.getCreatedAt().getTime():null); card.put("expiredAt",m.getValidDate()!=null?m.getValidDate().getTime():null); return card; }
    private List<Map<String,Object>> flattenBorrowDetails(List<BookBorrowOrder> orders,String mode){ List<Map<String,Object>> list=new ArrayList<>(); if(orders==null) return list; for(BookBorrowOrder o:orders){ List<BookBorrowDetail> ds=bookBorrowService.selectDetailsByOrderId(o.getId()); for(BookBorrowDetail d:ds){ if("borrowing".equals(mode)&&remainingQty(d)<=0) continue; list.add(buildFlatItem(o,d)); } } return list; }
    private Map<String,Object> buildFlatItem(BookBorrowOrder o,BookBorrowDetail d){ Map<String,Object> item=new HashMap<>(); item.put("detailId",d.getId()); item.put("borrowDetailId",d.getId()); item.put("orderNo",d.getBorrowOrderNo()); item.put("memberId",d.getMemberId()); item.put("bookId",d.getBookId()); item.put("bookName",d.getBookName()); item.put("borrowStatus",d.getBorrowStatus()!=null?d.getBorrowStatus():0); item.put("borrowQty",d.getBorrowQty()!=null?d.getBorrowQty():0); item.put("returnedQty",d.getReturnedQty()!=null?d.getReturnedQty():0); item.put("purchaseQty",d.getPurchaseQty()!=null?d.getPurchaseQty():0); item.put("remainingQty",remainingQty(d)); item.put("purchaseOrderNo",d.getPurchaseOrderNo()); item.put("borrowTime",timeMillis(d.getBorrowTime())); item.put("returnAllTime",d.getReturnAllTime()!=null?timeMillis(d.getReturnAllTime()):(o!=null?timeMillis(o.getReturnAllTime()):null)); item.put("expectedReturnTime",o!=null?timeMillis(o.getExpectedReturnTime()):null); item.put("remark",d.getRemark()!=null?d.getRemark():(o!=null?o.getRemark():null)); return item; }
    private List<BookReturnDetail> filterReturns(List<BookReturnDetail> returns,Long detailId){ List<BookReturnDetail> list=new ArrayList<>(); if(returns==null) return list; for(BookReturnDetail r:returns){ if(r.getBorrowDetailId()!=null&&r.getBorrowDetailId().equals(detailId)) list.add(r); } return list; }
    private String getStaffId(HttpServletRequest request){ Object attr=request.getAttribute("staffUserId"); return attr!=null?String.valueOf(attr):"system"; }
    private int remainingQty(BookBorrowDetail d){ int b=d.getBorrowQty()!=null?d.getBorrowQty():0; int r=d.getReturnedQty()!=null?d.getReturnedQty():0; int p=d.getPurchaseQty()!=null?d.getPurchaseQty():0; return b-r-p; }
    private Long parseLong(String v,String fieldName){ try{return Long.valueOf(v);}catch(Exception e){ throw new ApiException(ApiErrorCode.PARAM_INVALID,fieldName+" must be a number"); } }
    private int parseInt(Object v,int def){ if(v==null) return def; return Integer.parseInt(v.toString()); }
    private String stringValue(Object v){ return v!=null?String.valueOf(v):null; }
    private String requestRemark(Map<String,Object> body){ String v=stringValue(body.get("remark")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("remarks")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("memo")); if(v==null||v.trim().isEmpty()) v=stringValue(body.get("note")); return v!=null?v.trim():null; }
    private BigDecimal toBigDecimal(Object v){ return v!=null&&String.valueOf(v).trim().length()>0?new BigDecimal(String.valueOf(v)):null; }
    private Long timeMillis(Date date){ return date!=null?Long.valueOf(date.getTime()):null; }
    private String maskPhone(String phone){ if(phone==null||phone.length()<7) return phone; return phone.substring(0,3)+"****"+phone.substring(phone.length()-4); }
}
