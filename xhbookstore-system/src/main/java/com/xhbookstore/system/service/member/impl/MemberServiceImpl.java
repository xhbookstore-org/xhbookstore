package com.xhbookstore.system.service.member.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.alibaba.fastjson2.JSON;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.utils.SecurityUtils;
import com.xhbookstore.common.utils.StringUtils;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.MemberCardOrder;
import com.xhbookstore.system.domain.member.MemberExt;
import com.xhbookstore.system.domain.member.MemberExport;
import com.xhbookstore.system.domain.member.MemberImportDetail;
import com.xhbookstore.system.domain.member.MemberImportLog;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.mapper.member.MemberCardMapper;
import com.xhbookstore.system.mapper.member.MemberCardOrderMapper;
import com.xhbookstore.system.mapper.member.MemberExtMapper;
import com.xhbookstore.system.mapper.member.MemberImportDetailMapper;
import com.xhbookstore.system.mapper.member.MemberImportLogMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.common.core.domain.entity.SysDept;

@Service
public class MemberServiceImpl implements IMemberService {

    @Autowired private MemberMapper memberMapper;
    @Autowired private MemberExtMapper memberExtMapper;
    @Autowired private CardTypeMapper cardTypeMapper;
    @Autowired private SysDeptMapper deptMapper;
    @Autowired private MemberImportLogMapper importLogMapper;
    @Autowired private MemberImportDetailMapper importDetailMapper;
    @Autowired private MemberCardMapper memberCardMapper;
    @Autowired private MemberCardOrderMapper memberCardOrderMapper;

    @Override
    public List<Member> selectMemberList(Member member) {
        return memberMapper.selectMemberList(member);
    }

    @Override
    public Member selectMemberById(Integer id) {
        return memberMapper.selectMemberById(id);
    }

    @Override
    public MemberExt selectMemberExt(Integer memberId) {
        return memberExtMapper.selectByMemberId(memberId);
    }

    @Override
    public Member getByPhone(String phone) {
        return memberMapper.selectMemberByPhone(phone);
    }

    @Override
    public String generateCardNo(Long deptId) {
        // Get dept ERP ID as card prefix
        String prefix = String.valueOf(deptId);
        SysDept dept = deptMapper.selectDeptById(deptId);
        if (dept != null && dept.getErpDeptId() != null) {
            prefix = String.valueOf(dept.getErpDeptId());
        }

        String maxCardNo = memberMapper.selectMaxCardNoByDept(prefix);
        long seq = 1;
        if (maxCardNo != null && maxCardNo.length() >= 11) {
            String seqStr = maxCardNo.substring(prefix.length());
            try { seq = Long.parseLong(seqStr) + 1; } catch (NumberFormatException e) { /* start from 1 */ }
        }
        String seqPart = String.format("%0" + (11 - prefix.length()) + "d", seq);
        String cardNo = prefix + seqPart;
        while (memberMapper.selectMemberByCardNo(cardNo) != null) {
            seq++;
            seqPart = String.format("%0" + (11 - prefix.length()) + "d", seq);
            cardNo = prefix + seqPart;
        }
        return cardNo;
    }

    @Override
    @Transactional
    public AjaxResult insertMember(Member member, MemberExt ext) {
        // Check phone uniqueness
        if (member.getPhone() != null && memberMapper.selectMemberByPhoneAnyStatus(member.getPhone()) != null) {
            return AjaxResult.error("该手机号已注册");
        }
        // Generate card_no
        String cardNo = generateCardNo(member.getDeptId());
        member.setCardNo(cardNo);
        member.setStatus(0);
        member.setBorrowCountValid(0);
        member.setCurrentPoints(0);
        member.setSource("manual");
        member.setSyncErp(0);
        member.setLastOperator(SecurityUtils.getUsername());

        // Calculate valid_date based on card type
        CardType cardType = cardTypeMapper.selectById(member.getCardTypeId());
        if (cardType != null && cardType.getIsRenewal() == 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, cardType.getValidDays());
            member.setValidDate(cal.getTime());
        }

        memberMapper.insertMember(member);

        if (ext != null) {
            ext.setMemberId(member.getId());
            ext.setJoinDate(new Date());
            ext.setTotalPoints(0);
            ext.setLevelPoints(0);
            ext.setBusinessStaffName(SecurityUtils.getUsername());
            memberExtMapper.insertMemberExt(ext);
        }

        return AjaxResult.success("新增成功");
    }

    @Override
    @Transactional
    public AjaxResult updateMember(Member member, MemberExt ext) {
        Member existing = memberMapper.selectMemberById(member.getId());
        if (existing == null) return AjaxResult.error("会员不存在");

        // Check phone uniqueness
        if (member.getPhone() != null) {
            Member phoneCheck = memberMapper.selectMemberByPhoneAnyStatus(member.getPhone());
            if (phoneCheck != null && !phoneCheck.getId().equals(member.getId())) {
                return AjaxResult.error("该手机号已被其他会员使用");
            }
        }

        // Calculate valid_date for renewal
        CardType cardType = cardTypeMapper.selectById(member.getCardTypeId());
        if (cardType != null) {
            Calendar cal = Calendar.getInstance();
            if (cardType.getIsRenewal() == 1) {
                // Renewal: extend from original valid_date
                if (existing.getValidDate() != null) {
                    cal.setTime(existing.getValidDate());
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, cardType.getValidDays());
            member.setValidDate(cal.getTime());
        }

        member.setLastOperator(SecurityUtils.getUsername());
        memberMapper.updateMember(member);

        if (ext != null) {
            ext.setMemberId(member.getId());
            MemberExt existingExt = memberExtMapper.selectByMemberId(member.getId());
            if (existingExt != null) {
                memberExtMapper.updateMemberExt(ext);
            } else {
                ext.setJoinDate(existing.getCreatedAt());
                memberExtMapper.insertMemberExt(ext);
            }
        }

        return AjaxResult.success("编辑成功");
    }

    @Override
    public AjaxResult deleteMember(Integer id) {
        memberMapper.deleteMemberById(id);
        return AjaxResult.success("删除成功");
    }

    @Override
    public List<Member> selectMemberListForExport(Member member) {
        return memberMapper.selectMemberListForExport(member);
    }

    @Override
    public List<MemberExport> selectMemberExportList(Member member) {
        return memberMapper.selectMemberExportList(member);
    }

    @Override
    @Transactional
    public AjaxResult importMembers(MultipartFile file, Long deptId, String operator) throws Exception {
        if (file == null || file.isEmpty()) return AjaxResult.error("请选择要导入的Excel文件");
        if (deptId == null) return AjaxResult.error("请选择导入门店");

        MemberImportLog log = new MemberImportLog();
        log.setOperator(operator);
        log.setFileName(file.getOriginalFilename());
        log.setTotalRecords(0);
        log.setSuccessRecords(0);
        log.setFailRecords(0);
        log.setRemark("ERP会员Excel导入");
        importLogMapper.insert(log);

        int total = 0;
        int success = 0;
        int fail = 0;
        int skipped = 0;
        StringBuilder errors = new StringBuilder();
        List<Map<String, Object>> failureRows = new java.util.ArrayList<>();
        List<Map<String, Object>> skippedRows = new java.util.ArrayList<>();
        List<Map<String, Object>> warningRows = new java.util.ArrayList<>();
        Set<String> importedCardNos = new HashSet<>();
        Map<String, CardType> cardTypeMap = buildCardTypeMap();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) return AjaxResult.error("Excel没有可导入的数据");
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            Map<String, Integer> headerMap = readHeader(sheet.getRow(0), formatter);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) continue;

                ImportRow importRow = parseImportRow(row, headerMap, formatter);
                if (isNonBusinessRow(importRow)) continue;
                total++;

                MemberImportDetail detail = buildImportDetail(log.getId(), i + 1, importRow);
                importDetailMapper.insert(detail);

                try {
                    String normalizedCardNo = normalizeMemberNo(importRow.cardNo);
                    if (StringUtils.isNotBlank(normalizedCardNo) && !importedCardNos.add(normalizedCardNo)) {
                        throw new ImportSkipException("未导入：会员卡号已存在，不再重新导入");
                    }
                    ImportResult importResult = importOneMember(importRow, deptId, operator, cardTypeMap);
                    detail.setImportStatus(1);
                    detail.setMemberId(importResult.memberId);
                    detail.setErrorMsg(importResult.cardWarning);
                    if (StringUtils.isNotBlank(importResult.cardWarning)) {
                        warningRows.add(buildImportMessage(i + 1, importRow, importResult.cardWarning));
                    }
                    success++;
                } catch (ImportSkipException e) {
                    String skipMsg = e.getMessage();
                    detail.setImportStatus(3);
                    detail.setErrorMsg(skipMsg);
                    skippedRows.add(buildImportMessage(i + 1, importRow, skipMsg));
                    skipped++;
                } catch (Exception e) {
                    String errorMsg = simplifyImportError(e);
                    detail.setImportStatus(2);
                    detail.setErrorMsg(errorMsg);
                    errors.append("第").append(i + 1).append("行：").append(errorMsg).append("\n");
                    failureRows.add(buildImportMessage(i + 1, importRow, errorMsg));
                    fail++;
                }
                importDetailMapper.updateResult(detail);
            }
        }

        log.setTotalRecords(total);
        log.setSuccessRecords(success);
        log.setFailRecords(fail);
        log.setErrorLog(errors.length() > 0 ? errors.toString() : null);
        importLogMapper.updateResult(log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("logId", log.getId());
        data.put("totalRecords", total);
        data.put("successRecords", success);
        data.put("failRecords", fail);
        data.put("skippedRecords", skipped);
        data.put("errorLog", log.getErrorLog());
        data.put("failureRows", failureRows);
        data.put("skippedRows", skippedRows);
        data.put("warningRows", warningRows);
        return AjaxResult.success("导入完成", data);
    }

    private ImportResult importOneMember(ImportRow row, Long deptId, String operator, Map<String, CardType> cardTypeMap) {
        if (StringUtils.isBlank(row.phone)) throw new IllegalArgumentException("有用户无手机号，请确认数据");
        if (StringUtils.isBlank(row.cardNo)) throw new IllegalArgumentException("会员卡号不能为空");
        if (StringUtils.isBlank(row.name)) throw new IllegalArgumentException("姓名不能为空");

        row.cardNo = normalizeMemberNo(row.cardNo);
        Member exists = memberMapper.selectMemberByCardNo(row.cardNo);
        if (exists != null) {
            throw new ImportSkipException("未导入：会员卡号已存在，不再重新导入");
        }
        if (StringUtils.isNotBlank(row.phone)) {
            Member phoneMember = memberMapper.selectMemberByPhoneAnyStatus(row.phone);
            if (phoneMember != null && !row.cardNo.equals(phoneMember.getCardNo())) {
                throw new IllegalArgumentException("手机号已被其他会员使用");
            }
        }

        CardType cardType = findCardType(row.cardTypeName, cardTypeMap);
        if (cardType == null) {
            String cardTypeName = StringUtils.isBlank(row.cardTypeName) ? "空" : row.cardTypeName;
            throw new IllegalArgumentException("会员卡类型[" + cardTypeName + "]本地数据库不存在");
        }
        Integer levelId = StringUtils.isNotBlank(row.levelName) ? memberMapper.selectLevelIdByName(row.levelName) : null;
        boolean isNew = true;
        Member member = new Member();
        member.setCardNo(row.cardNo);
        member.setName(row.name);
        member.setPhone(row.phone);
        member.setCardTypeId(cardType.getId());
        member.setLevelId(levelId);
        member.setValidDate(row.validDate);
        member.setStatus(isCancelled(row.cancelled) ? 1 : 0);
        member.setRemark(row.remark);
        member.setLastOperator(operator);
        member.setSyncErp(1);

        member.setDeptId(deptId);
        member.setBorrowCountValid(0);
        member.setCurrentPoints(row.currentPoints != null ? row.currentPoints : 0);
        member.setSource("import");
        memberMapper.insertMember(member);

        upsertMemberExt(member.getId(), row, operator, isNew);
        String cardWarning = createImportedMemberCardIfNeeded(member, row, cardType, member.getDeptId(), operator);
        return new ImportResult(member.getId(), cardWarning);
    }

    private void upsertMemberExt(Integer memberId, ImportRow row, String operator, boolean isNew) {
        MemberExt ext = new MemberExt();
        ext.setMemberId(memberId);
        ext.setGender(row.gender);
        ext.setAge(row.age);
        ext.setUnitPhone(row.unitPhone);
        ext.setWechat(row.wechat);
        ext.setWeibo(row.weibo);
        ext.setJoinDate(row.joinDate);
        if (isNew) {
            ext.setTotalPoints(row.totalPoints);
            ext.setLevelPoints(row.levelPoints);
        }
        ext.setDiscount(row.discount);
        ext.setTotalPurchaseAmount(row.totalPurchaseAmount);
        ext.setTotalPurchaseCount(row.totalPurchaseCount);
        ext.setTotalPurchaseTimes(row.totalPurchaseTimes);
        ext.setSuperiorName(row.superiorName);
        ext.setSuperiorPointsRatio(row.superiorPointsRatio);
        ext.setBusinessStaffName(StringUtils.isNotBlank(row.businessStaffName) ? row.businessStaffName : operator);
        ext.setExcelRawData(JSON.toJSONString(row.rawData));
        if (memberExtMapper.selectByMemberId(memberId) == null) {
            memberExtMapper.insertMemberExt(ext);
        } else {
            memberExtMapper.updateMemberExt(ext);
        }
    }

    private String createImportedMemberCardIfNeeded(Member member, ImportRow row, CardType cardType, Long deptId, String operator) {
        if (cardType == null) return "未生成会员卡：卡类型[" + (row.cardTypeName == null ? "" : row.cardTypeName) + "]未匹配系统卡类型";
        if (row.validDate == null) return "未生成会员卡：有效日期为空";
        List<MemberCard> existingCards = memberCardMapper.selectByMemberId(member.getId());
        for (MemberCard card : existingCards) {
            if (card.getSaleOrderNo() != null && card.getSaleOrderNo().startsWith("ERPIMPORT")) {
                return null;
            }
        }

        Date effectiveAt = row.joinDate != null ? row.joinDate : new Date();
        Date expiredAt = row.validDate;
        int status = expiredAt.before(new Date()) ? 2 : 1;
        String orderNo = "ERPIMPORT" + member.getCardNo();
        MemberCard existingCard = memberCardMapper.selectBySaleOrderNo(orderNo);
        if (existingCard != null) {
            if (Objects.equals(existingCard.getMemberId(), member.getId())) {
                return null;
            }
            return "未生成会员卡：售卡单号[" + orderNo + "]已存在于历史会员卡[id=" + existingCard.getId()
                    + ", memberId=" + existingCard.getMemberId() + ", memberNo=" + existingCard.getMemberNo()
                    + "]，当前会员[id=" + member.getId() + ", memberNo=" + member.getCardNo()
                    + "]，请核对重复或孤立数据";
        }
        MemberCardOrder existingOrder = memberCardOrderMapper.selectByOrderNo(orderNo);
        if (existingOrder != null) {
            return existingOrder.getMemberCardId() == null
                    ? "未生成会员卡：已存在ERP导入售卡订单[" + orderNo + "]，但订单未绑定会员卡，请核对历史数据"
                    : null;
        }

        MemberCardOrder order = new MemberCardOrder();
        order.setOrderNo(orderNo);
        order.setMemberId(member.getId());
        order.setMemberNo(member.getCardNo());
        order.setMemberName(member.getName());
        order.setMemberPhone(member.getPhone());
        order.setCardTypeId(cardType.getId());
        order.setCardTypeName(cardType.getTypeName());
        order.setValidDays(cardType.getValidDays() != null ? cardType.getValidDays() : 0);
        order.setReceivableAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentType("ERP_IMPORT");
        order.setOrderStatus(1);
        order.setPayTime(effectiveAt);
        order.setDeptId(deptId);
        order.setCreateStaffId(operator);
        order.setCreateStaffName(operator);
        order.setRemark("ERP历史会员导入");

        MemberCard card = new MemberCard();
        card.setMemberId(member.getId());
        card.setMemberNo(member.getCardNo());
        card.setCardTypeId(cardType.getId());
        card.setCardTypeName(cardType.getTypeName());
        card.setValidDays(cardType.getValidDays() != null ? cardType.getValidDays() : 0);
        card.setSaleAmount(BigDecimal.ZERO);
        card.setSaleOrderNo(orderNo);
        card.setStatus(status);
        card.setPaidAt(effectiveAt);
        card.setEffectiveAt(effectiveAt);
        card.setExpiredAt(expiredAt);
        card.setDeptId(deptId);
        card.setCreateStaffId(operator);
        card.setCreateStaffName(operator);
        card.setRemark("ERP历史会员导入");

        memberCardOrderMapper.insert(order);
        memberCardMapper.insert(card);
        order.setMemberCardId(card.getId());
        memberCardOrderMapper.bindMemberCard(order);
        return null;
    }

    private Map<String, Object> buildImportMessage(int rowIndex, ImportRow row, String reason) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("rowIndex", rowIndex);
        message.put("cardNo", row.cardNo);
        message.put("name", row.name);
        message.put("phone", row.phone);
        message.put("cardTypeName", row.cardTypeName);
        message.put("reason", reason);
        return message;
    }

    private String simplifyImportError(Exception e) {
        String message = e.getMessage();
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (StringUtils.isNotBlank(cause.getMessage())) {
                message = cause.getMessage();
            }
        }
        if (StringUtils.isBlank(message)) {
            message = e.getClass().getSimpleName();
        }
        message = message.replaceAll("\\s+", " ").trim();
        if (message.length() > 2000) {
            message = message.substring(0, 2000) + "...";
        }
        return message;
    }

    private Map<String, Integer> readHeader(Row header, DataFormatter formatter) {
        if (header == null) throw new IllegalArgumentException("Excel表头为空");
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.getLastCellNum(); i++) {
            String value = formatter.formatCellValue(header.getCell(i));
            if (StringUtils.isNotBlank(value)) map.put(value.trim(), i);
        }
        return map;
    }

    private ImportRow parseImportRow(Row row, Map<String, Integer> headerMap, DataFormatter formatter) {
        ImportRow data = new ImportRow();
        data.cardNo = normalizeMemberNo(textFirst(row, headerMap, formatter, "会员编号", "会员码", "会员卡号", "会员号", "卡号"));
        data.name = text(row, headerMap, "姓名", formatter);
        data.cardTypeName = text(row, headerMap, "卡类型", formatter);
        data.levelName = text(row, headerMap, "会员级别", formatter);
        data.discount = decimal(row, headerMap, "折扣", formatter);
        data.totalPoints = integer(row, headerMap, "总积分", formatter);
        data.currentPoints = integer(row, headerMap, "当前积分", formatter);
        data.levelPoints = integer(row, headerMap, "级别积分", formatter);
        data.cancelled = text(row, headerMap, "注销", formatter);
        data.lostFlag = text(row, headerMap, "挂失 标记", formatter);
        data.phone = text(row, headerMap, "手机", formatter);
        data.totalPurchaseAmount = decimal(row, headerMap, "累计购书额", formatter);
        data.totalPurchaseCount = integer(row, headerMap, "累计购书册数", formatter);
        data.totalPurchaseTimes = integer(row, headerMap, "累计购书次数", formatter);
        data.joinDate = date(row, headerMap, "入会日期", formatter);
        data.gender = text(row, headerMap, "性别", formatter);
        data.age = integer(row, headerMap, "年龄", formatter);
        data.remark = text(row, headerMap, "备注", formatter);
        data.unitPhone = text(row, headerMap, "单位电话", formatter);
        data.validDate = date(row, headerMap, "有效日期", formatter);
        data.wechat = text(row, headerMap, "微信", formatter);
        data.weibo = text(row, headerMap, "微博", formatter);
        data.superiorPointsRatio = decimal(row, headerMap, "上级积分比例", formatter);
        data.superiorName = text(row, headerMap, "上级名称", formatter);
        data.businessStaffName = text(row, headerMap, "业务员名称", formatter);
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            data.rawData.put(entry.getKey(), text(row, entry.getValue(), formatter));
        }
        return data;
    }

    private MemberImportDetail buildImportDetail(Integer logId, int rowIndex, ImportRow row) {
        MemberImportDetail detail = new MemberImportDetail();
        detail.setLogId(logId);
        detail.setRowIndex(rowIndex);
        detail.setCardNo(row.cardNo);
        detail.setName(row.name);
        detail.setPhone(row.phone);
        detail.setCardTypeName(row.cardTypeName);
        detail.setLevelName(row.levelName);
        detail.setValidDate(row.validDate);
        detail.setUpdateTime(new Date());
        detail.setImportStatus(0);
        return detail;
    }

    private Map<String, CardType> buildCardTypeMap() {
        Map<String, CardType> map = new HashMap<>();
        for (CardType type : cardTypeMapper.selectAll()) {
            map.put(normalize(type.getTypeName()), type);
        }
        return map;
    }

    private CardType findCardType(String name, Map<String, CardType> cardTypeMap) {
        if (StringUtils.isBlank(name)) return null;
        return cardTypeMap.get(normalize(name));
    }

    private boolean isCancelled(String value) {
        return "T".equalsIgnoreCase(value) || "1".equals(value) || "是".equals(value);
    }

    private boolean isNonBusinessRow(ImportRow row) {
        if (row == null) return true;
        if (StringUtils.isNotBlank(row.cardNo) && row.cardNo.startsWith("记录数")) return true;
        return StringUtils.isBlank(row.cardNo)
                && StringUtils.isBlank(row.name)
                && StringUtils.isBlank(row.phone)
                && StringUtils.isBlank(row.cardTypeName);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace(" ", "").replace("　", "").trim().toLowerCase(Locale.ROOT);
    }

    private String text(Row row, Map<String, Integer> headerMap, String name, DataFormatter formatter) {
        Integer index = headerMap.get(name);
        return index == null ? null : text(row, index, formatter);
    }

    private String textFirst(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String... names) {
        for (String name : names) {
            String value = text(row, headerMap, name, formatter);
            if (StringUtils.isNotBlank(value)) return value;
        }
        return null;
    }

    private String text(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0) return null;
        String value = formatter.formatCellValue(row.getCell(index));
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String normalizeMemberNo(String value) {
        if (StringUtils.isBlank(value)) return null;
        String normalized = value.trim()
                .replace("'", "")
                .replace(",", "")
                .replace("，", "")
                .replaceAll("\\s+", "");
        if (normalized.endsWith(".0")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        return normalized;
    }

    private Integer integer(Row row, Map<String, Integer> headerMap, String name, DataFormatter formatter) {
        String value = text(row, headerMap, name, formatter);
        if (StringUtils.isBlank(value)) return null;
        try { return new BigDecimal(value.replace(",", "")).intValue(); } catch (Exception e) { return null; }
    }

    private BigDecimal decimal(Row row, Map<String, Integer> headerMap, String name, DataFormatter formatter) {
        String value = text(row, headerMap, name, formatter);
        if (StringUtils.isBlank(value)) return null;
        try { return new BigDecimal(value.replace(",", "")); } catch (Exception e) { return null; }
    }

    private Date date(Row row, Map<String, Integer> headerMap, String name, DataFormatter formatter) {
        Integer index = headerMap.get(name);
        if (index == null || row == null) return null;
        Cell cell = row.getCell(index);
        if (cell != null && (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA)
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }
        String value = text(row, index, formatter);
        if (StringUtils.isBlank(value)) return null;
        return com.xhbookstore.common.utils.DateUtils.parseDate(value);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && StringUtils.isNotBlank(new DataFormatter().formatCellValue(cell))) return false;
        }
        return true;
    }

    private static class ImportResult {
        Integer memberId;
        String cardWarning;

        ImportResult(Integer memberId, String cardWarning) {
            this.memberId = memberId;
            this.cardWarning = cardWarning;
        }
    }

    private static class ImportSkipException extends RuntimeException {
        ImportSkipException(String message) {
            super(message);
        }
    }

    private static class ImportRow {
        String cardNo;
        String name;
        String phone;
        String cardTypeName;
        String levelName;
        BigDecimal discount;
        Integer totalPoints;
        Integer currentPoints;
        Integer levelPoints;
        String cancelled;
        String lostFlag;
        BigDecimal totalPurchaseAmount;
        Integer totalPurchaseCount;
        Integer totalPurchaseTimes;
        Date joinDate;
        String gender;
        Integer age;
        String remark;
        String unitPhone;
        Date validDate;
        String wechat;
        String weibo;
        BigDecimal superiorPointsRatio;
        String superiorName;
        String businessStaffName;
        Map<String, Object> rawData = new LinkedHashMap<>();
    }
}
