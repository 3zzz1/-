package com.audit.rectification.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return success(buildRiskAreaList(all));
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/recurring")
    public AjaxResult recurring() {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        Map<String, List<RectIssue>> grouped = all.stream()
            .filter(i -> i.getIssueTitle() != null && !i.getIssueTitle().trim().isEmpty())
            .collect(Collectors.groupingBy(i -> normalizeTitle(i.getIssueTitle()), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = grouped.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(e -> {
                List<RectIssue> issues = e.getValue();
                RectIssue latest = issues.stream()
                    .max(Comparator.comparing(RectIssue::getCreateTime, Comparator.nullsLast(Date::compareTo)))
                    .orElse(issues.get(0));
                Map<String, Object> m = new HashMap<>();
                m.put("issueType", latest.getIssueTitle());
                m.put("area", categoryLabel(latest.getIssueCategory()));
                m.put("occurrences", issues.size());
                m.put("completed", issues.stream().filter(i -> "3".equals(i.getStatus())).count());
                m.put("overdue", issues.stream().filter(this::isOverdue).count());
                m.put("riskLevel", riskLevelLabel(maxRiskLevel(issues)));
                m.put("lastFound", latest.getCreateTime());
                m.put("suggestion", "纳入下一年度审计重点，复核制度执行和整改长效机制。");
                return m;
            })
            .sorted((a, b) -> Long.compare(toLong(b.get("occurrences")), toLong(a.get("occurrences"))))
            .limit(10)
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            result = buildRiskAreaList(all).stream()
                .filter(m -> toLong(m.get("count")) > 0)
                .map(m -> {
                    Map<String, Object> r = new HashMap<>();
                    r.put("issueType", m.get("area"));
                    r.put("area", m.get("area"));
                    r.put("occurrences", m.get("count"));
                    r.put("completed", m.get("completed"));
                    r.put("overdue", m.get("overdue"));
                    r.put("riskLevel", "按数量识别");
                    r.put("lastFound", null);
                    r.put("suggestion", "作为高频风险领域纳入下一年度审计计划。");
                    return r;
                })
                .limit(10)
                .collect(Collectors.toList());
        }
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('rectification:statistics:view')")
    @GetMapping("/trend")
    public AjaxResult trend(String type) {
        List<RectIssue> all = rectIssueMapper.selectRectIssueList(new RectIssue());
        boolean quarter = "quarter".equals(type);
        int bucketCount = quarter ? 4 : 12;
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < bucketCount; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("period", quarter ? ("Q" + (i + 1)) : ((i + 1) + "月"));
            m.put("discovered", 0L);
            m.put("completed", 0L);
            m.put("overdue", 0L);
            result.add(m);
        }

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        Date now = new Date();
        for (RectIssue issue : all) {
            Date createTime = issue.getCreateTime();
            if (createTime != null) {
                cal.setTime(createTime);
                if (cal.get(Calendar.YEAR) == currentYear) {
                    increment(result.get(bucketIndex(cal, quarter)), "discovered");
                }
            }
            Date closureDate = issue.getClosureDate();
            if (closureDate != null) {
                cal.setTime(closureDate);
                if (cal.get(Calendar.YEAR) == currentYear) {
                    increment(result.get(bucketIndex(cal, quarter)), "completed");
                }
            }
            if (issue.getDeadline() != null && issue.getDeadline().before(now) && !"3".equals(issue.getStatus())) {
                cal.setTime(issue.getDeadline());
                if (cal.get(Calendar.YEAR) == currentYear) {
                    increment(result.get(bucketIndex(cal, quarter)), "overdue");
                }
            }
        }
        return success(result);
    }

    private List<Map<String, Object>> buildRiskAreaList(List<RectIssue> issues) {
        String[] cats = {"FUND","ASSET","PURCHASE","HR","CONSTRUCTION","OTHER"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (String cat : cats) {
            List<RectIssue> same = issues.stream()
                .filter(i -> cat.equals(i.getIssueCategory() != null ? i.getIssueCategory() : "OTHER"))
                .collect(Collectors.toList());
            long count = same.size();
            long completed = same.stream().filter(i -> "3".equals(i.getStatus())).count();
            long overdue = same.stream().filter(this::isOverdue).count();
            BigDecimal recovery = same.stream()
                .filter(i -> i.getIssueAmount() != null && "3".equals(i.getStatus()))
                .map(RectIssue::getIssueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new HashMap<>();
            m.put("area", categoryLabel(cat));
            m.put("count", count);
            m.put("completed", completed);
            m.put("overdue", overdue);
            m.put("completionRate", count > 0 ? new BigDecimal(String.format("%.2f", 100.0 * completed / count)) : BigDecimal.ZERO);
            m.put("recoveryAmount", recovery);
            list.add(m);
        }
        list.sort((a, b) -> Long.compare(toLong(b.get("count")), toLong(a.get("count"))));
        return list;
    }

    private boolean isOverdue(RectIssue issue) {
        return issue.getDeadline() != null && issue.getDeadline().before(new Date()) && !"3".equals(issue.getStatus());
    }

    private int bucketIndex(Calendar cal, boolean quarter) {
        int month = cal.get(Calendar.MONTH);
        return quarter ? month / 3 : month;
    }

    private void increment(Map<String, Object> map, String key) {
        map.put(key, toLong(map.get(key)) + 1L);
    }

    private long toLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value == null) return 0L;
        try { return Long.parseLong(value.toString()); } catch (Exception e) { return 0L; }
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.replaceAll("[\\s，。,.；;：:（）()【】\\[\\]]", "").trim();
    }

    private String categoryLabel(String category) {
        if ("FUND".equals(category)) return "资金类";
        if ("ASSET".equals(category)) return "资产类";
        if ("PURCHASE".equals(category)) return "采购类";
        if ("HR".equals(category)) return "人事类";
        if ("CONSTRUCTION".equals(category)) return "基建类";
        return "其他";
    }

    private String riskLevelLabel(String level) {
        if ("3".equals(level)) return "高";
        if ("2".equals(level)) return "中";
        if ("1".equals(level)) return "低";
        return "-";
    }

    private String maxRiskLevel(List<RectIssue> issues) {
        int max = 0;
        for (RectIssue issue : issues) {
            try {
                max = Math.max(max, Integer.parseInt(issue.getRiskLevel()));
            } catch (Exception ignored) {
            }
        }
        return max == 0 ? null : String.valueOf(max);
    }
}
