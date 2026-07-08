package com.audit.rectification.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectClosure;
import com.audit.rectification.domain.dto.StatisticsQueryDTO;
import com.audit.rectification.domain.dto.StatisticsResultVO;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectClosureMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/rectification/statistics")
public class RectStatisticsController extends BaseController {

    @Autowired
    private RectIssueMapper rectIssueMapper;
    @Autowired
    private RectClosureMapper rectClosureMapper;

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/overview")
    public AjaxResult overview(StatisticsQueryDTO query) {
        StatisticsResultVO vo = new StatisticsResultVO();
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        long total = all.size();
        long completed = all.stream().filter(i -> "3".equals(i.getStatus())).count();
        long inProgress = all.stream().filter(i -> "1".equals(i.getStatus())).count();
        long overdue = all.stream().filter(i -> {
            if (i.getDeadline() == null) return false;
            if ("3".equals(i.getStatus()) || "4".equals(i.getStatus())) return false;
            return i.getDeadline().before(new Date());
        }).count();

        vo.setTotalIssues(total);
        vo.setCompletedCount(completed);
        vo.setInProgressCount(inProgress);
        vo.setOverdueCount(overdue);
        vo.setCompletionRate(total > 0 ? new BigDecimal(String.format("%.2f", 100.0 * completed / total)) : BigDecimal.ZERO);
        vo.setOverdueRate(total > 0 ? new BigDecimal(String.format("%.2f", 100.0 * overdue / total)) : BigDecimal.ZERO);
        vo.setTotalRecoveryAmount(all.stream()
            .filter(i -> i.getIssueAmount() != null && "3".equals(i.getStatus()))
            .map(i -> i.getIssueAmount()).reduce(BigDecimal.ZERO, BigDecimal::add));
        return success(vo);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/by-category")
    public AjaxResult byCategory() {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        Map<String, Long> map = new HashMap<>();
        for (RectIssue i : all) {
            String cat = i.getIssueCategory() != null ? i.getIssueCategory() : "OTHER";
            map.put(cat, map.getOrDefault(cat, 0L) + 1);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        String[] cats = {"FUND","ASSET","PURCHASE","HR","CONSTRUCTION","OTHER"};
        String[] labels = {"资金类","资产类","采购类","人事类","基建类","其他"};
        for (int j = 0; j < cats.length; j++) {
            Map<String, Object> m = new HashMap<>();
            m.put("category", labels[j]); m.put("count", map.getOrDefault(cats[j], 0L));
            list.add(m);
        }
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/by-status")
    public AjaxResult byStatus() {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        Map<String, Long> map = new HashMap<>();
        String[] st = {"0","1","2","3","4"};
        String[] lb = {"待下发","整改中","待审核","已销号","持续整改"};
        for (RectIssue i : all) {
            String s = i.getStatus() != null ? i.getStatus() : "0";
            map.put(s, map.getOrDefault(s, 0L) + 1);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int j = 0; j < st.length; j++) {
            Map<String, Object> m = new HashMap<>();
            m.put("status", lb[j]); m.put("count", map.getOrDefault(st[j], 0L));
            list.add(m);
        }
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/overdue")
    public AjaxResult overdue() {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        Date now = new Date();
        long count = all.stream().filter(i -> {
            if (i.getDeadline() == null) return false;
            if ("3".equals(i.getStatus())) return false;
            return i.getDeadline().before(now);
        }).count();
        Map<String, Object> m = new HashMap<>();
        m.put("overdueCount", count);
        m.put("totalCount", all.size());
        return success(m);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/fund-recovery")
    public AjaxResult fundRecovery() {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        BigDecimal total = all.stream()
            .filter(i -> i.getIssueAmount() != null && "3".equals(i.getStatus()))
            .map(i -> i.getIssueAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new HashMap<>();
        m.put("totalRecoveryAmount", total);
        return success(m);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/recurring")
    public AjaxResult recurring() {
        return success(new ArrayList<>());
    }
}
