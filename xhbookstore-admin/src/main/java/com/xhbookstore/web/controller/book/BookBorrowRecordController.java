package com.xhbookstore.web.controller.book;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.common.utils.poi.ExcelUtil;
import com.xhbookstore.system.domain.book.BookBorrowDetailExport;
import com.xhbookstore.system.domain.book.BookBorrowRecord;
import com.xhbookstore.system.service.ISysDeptService;
import com.xhbookstore.system.service.book.IBookBorrowAdminService;

@RestController
@RequestMapping("/borrow/record")
public class BookBorrowRecordController extends BaseController {
    @Autowired private IBookBorrowAdminService borrowAdminService;
    @Autowired private ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('borrow:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(BookBorrowRecord query) {
        startPage();
        return getDataTable(borrowAdminService.selectList(query));
    }

    @PreAuthorize("@ss.hasPermi('borrow:record:query')")
    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable Long orderId) {
        BookBorrowRecord query = new BookBorrowRecord();
        query.setId(orderId);
        Map<String, Object> detail = borrowAdminService.selectDetail(query);
        return detail == null ? AjaxResult.error("借阅记录不存在或无权查看") : AjaxResult.success(detail);
    }

    @PreAuthorize("@ss.hasPermi('borrow:record:list')")
    @GetMapping("/depts/options")
    public AjaxResult depts() {
        SysDept query = new SysDept();
        query.setStatus("0");
        return AjaxResult.success(deptService.selectDeptList(query));
    }

    @PreAuthorize("@ss.hasPermi('borrow:record:export')")
    @Log(title = "借阅记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BookBorrowRecord query) {
        List<BookBorrowDetailExport> list = borrowAdminService.selectExportList(query);
        ExcelUtil<BookBorrowDetailExport> util = new ExcelUtil<>(BookBorrowDetailExport.class);
        util.exportExcel(response, list, "借阅记录明细");
    }
}
