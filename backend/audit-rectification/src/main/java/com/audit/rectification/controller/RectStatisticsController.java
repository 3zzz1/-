package com.audit.rectification.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.dto.StatisticsQueryDTO;
import com.audit.rectification.domain.dto.StatisticsResultVO;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 审计整改统计分析Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/statistics")
public class RectStatisticsController extends BaseController {

    /**
     * 综合概览统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/overview")
    public AjaxResult overview(StatisticsQueryDTO query) {
        StatisticsResultVO result = new StatisticsResultVO();
        result.setTotalIssues(128L);
        result.setCompletedCount(96L);
        result.setInProgressCount(20L);
        result.setOverdueCount(12L);
        result.setCompletionRate(new BigDecimal("75.00"));
        result.setOverdueRate(new BigDecimal("9.38"));
        result.setTotalRecoveryAmount(new BigDecimal("3250000.00"));
        return success(result);
    }

    /**
     * 按问题类别统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/by-category")
    public AjaxResult byCategory() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("category", "资金管理");
        item1.put("categoryCode", "FUND");
        item1.put("count", 35);
        item1.put("completedCount", 28);
        item1.put("completionRate", new BigDecimal("80.00"));
        data.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("category", "资产管理");
        item2.put("categoryCode", "ASSET");
        item2.put("count", 28);
        item2.put("completedCount", 22);
        item2.put("completionRate", new BigDecimal("78.57"));
        data.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("category", "采购管理");
        item3.put("categoryCode", "PURCHASE");
        item3.put("count", 25);
        item3.put("completedCount", 19);
        item3.put("completionRate", new BigDecimal("76.00"));
        data.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("category", "人事管理");
        item4.put("categoryCode", "HR");
        item4.put("count", 18);
        item4.put("completedCount", 13);
        item4.put("completionRate", new BigDecimal("72.22"));
        data.add(item4);

        Map<String, Object> item5 = new HashMap<>();
        item5.put("category", "工程建设");
        item5.put("categoryCode", "CONSTRUCTION");
        item5.put("count", 15);
        item5.put("completedCount", 10);
        item5.put("completionRate", new BigDecimal("66.67"));
        data.add(item5);

        Map<String, Object> item6 = new HashMap<>();
        item6.put("category", "其他");
        item6.put("categoryCode", "OTHER");
        item6.put("count", 7);
        item6.put("completedCount", 4);
        item6.put("completionRate", new BigDecimal("57.14"));
        data.add(item6);

        return success(data);
    }

    /**
     * 按整改状态统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/by-status")
    public AjaxResult byStatus() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("status", "待下发");
        item1.put("statusCode", "0");
        item1.put("count", 8);
        data.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("status", "整改中");
        item2.put("statusCode", "1");
        item2.put("count", 20);
        data.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("status", "待审核");
        item3.put("statusCode", "2");
        item3.put("count", 15);
        data.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("status", "已销号");
        item4.put("statusCode", "3");
        item4.put("count", 80);
        data.add(item4);

        Map<String, Object> item5 = new HashMap<>();
        item5.put("status", "持续整改");
        item5.put("statusCode", "4");
        item5.put("count", 5);
        data.add(item5);

        return success(data);
    }

    /**
     * 逾期问题统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/overdue")
    public AjaxResult overdue() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("deptName", "财务处");
        item1.put("totalCount", 20);
        item1.put("overdueCount", 4);
        item1.put("overdueRate", new BigDecimal("20.00"));
        data.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("deptName", "后勤管理处");
        item2.put("totalCount", 18);
        item2.put("overdueCount", 3);
        item2.put("overdueRate", new BigDecimal("16.67"));
        data.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("deptName", "基建处");
        item3.put("totalCount", 15);
        item3.put("overdueCount", 2);
        item3.put("overdueRate", new BigDecimal("13.33"));
        data.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("deptName", "教务处");
        item4.put("totalCount", 12);
        item4.put("overdueCount", 1);
        item4.put("overdueRate", new BigDecimal("8.33"));
        data.add(item4);

        Map<String, Object> item5 = new HashMap<>();
        item5.put("deptName", "科研处");
        item5.put("totalCount", 10);
        item5.put("overdueCount", 2);
        item5.put("overdueRate", new BigDecimal("20.00"));
        data.add(item5);

        return success(data);
    }

    /**
     * 资金挽回统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/fund-recovery")
    public AjaxResult fundRecovery() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("month", "2026-01");
        item1.put("recoveryAmount", new BigDecimal("450000.00"));
        item1.put("issueCount", 12);
        data.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("month", "2026-02");
        item2.put("recoveryAmount", new BigDecimal("380000.00"));
        item2.put("issueCount", 10);
        data.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("month", "2026-03");
        item3.put("recoveryAmount", new BigDecimal("520000.00"));
        item3.put("issueCount", 15);
        data.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("month", "2026-04");
        item4.put("recoveryAmount", new BigDecimal("620000.00"));
        item4.put("issueCount", 14);
        data.add(item4);

        Map<String, Object> item5 = new HashMap<>();
        item5.put("month", "2026-05");
        item5.put("recoveryAmount", new BigDecimal("580000.00"));
        item5.put("issueCount", 13);
        data.add(item5);

        Map<String, Object> item6 = new HashMap<>();
        item6.put("month", "2026-06");
        item6.put("recoveryAmount", new BigDecimal("700000.00"));
        item6.put("issueCount", 18);
        data.add(item6);

        return success(data);
    }

    /**
     * 重复发生问题统计
     */
    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/recurring")
    public AjaxResult recurring() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("issueCategory", "资金管理");
        item1.put("categoryCode", "FUND");
        item1.put("recurringCount", 5);
        item1.put("description", "违规使用专项资金");
        item1.put("riskLevel", "高");
        data.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("issueCategory", "采购管理");
        item2.put("categoryCode", "PURCHASE");
        item2.put("recurringCount", 3);
        item2.put("description", "未按规定招标");
        item2.put("riskLevel", "中");
        data.add(item2);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("issueCategory", "资产管理");
        item3.put("categoryCode", "ASSET");
        item3.put("recurringCount", 2);
        item3.put("description", "固定资产账实不符");
        item3.put("riskLevel", "中");
        data.add(item3);

        return success(data);
    }
}
