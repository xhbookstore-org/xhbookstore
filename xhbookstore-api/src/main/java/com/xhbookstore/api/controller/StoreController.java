package com.xhbookstore.api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.system.mapper.SysDeptMapper;

@Tag(name = "Store API", description = "Store list for mini-program binding")
@RestController
@RequestMapping("/api/mp/v1")
public class StoreController {

    @Autowired private SysDeptMapper sysDeptMapper;

    @Operation(summary = "Store list")
    @GetMapping("/stores")
    public ApiResponse<Map<String, Object>> stores() {
        SysDept query = new SysDept();
        query.setStatus("0");
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        List<Map<String, Object>> list = new ArrayList<>();
        if (depts != null) {
            for (SysDept dept : depts) {
                if (dept == null || dept.getDeptId() == null) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("deptId", dept.getDeptId());
                item.put("deptName", dept.getDeptName());
                list.add(item);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return ApiResponse.success(data);
    }
}
