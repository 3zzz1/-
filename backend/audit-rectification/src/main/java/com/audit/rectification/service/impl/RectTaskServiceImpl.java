package com.audit.rectification.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.dto.TaskDispatchDTO;
import com.audit.rectification.mapper.RectIssueMapper;
import com.audit.rectification.mapper.RectProgressMapper;
import com.audit.rectification.mapper.RectTaskMapper;
import com.audit.rectification.service.IRectNotificationService;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;

@Service
public class RectTaskServiceImpl implements IRectTaskService {

    private static final String[] UNIT_LIAISON_ROLES = { "audited_unit_liaison" };
    private static final String[] UNIT_LEADER_ROLES = { "audited_unit_leader" };

    @Autowired
    private RectTaskMapper rectTaskMapper;

    @Autowired
    private RectIssueMapper rectIssueMapper;

    @Autowired
    private RectProgressMapper rectProgressMapper;

    @Autowired
    private IRectNotificationService rectNotificationService;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public List<RectTask> selectRectTaskList(RectTask task) {
        return rectTaskMapper.selectRectTaskList(task);
    }

    @Override
    public RectTask selectRectTaskById(Long taskId) {
        return rectTaskMapper.selectRectTaskById(taskId);
    }

    @Override
    @Transactional
    public int insertRectTask(TaskDispatchDTO dto) {
        String taskNo = "RW" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        RectTask task = new RectTask();
        task.setTaskNo(taskNo);
        if (dto.getIssueIds() != null && !dto.getIssueIds().isEmpty()) {
            task.setIssueIds(dto.getIssueIds().toString());
        }
        task.setRectDeptId(dto.getRectDeptId());
        task.setContactPerson(dto.getContactPerson());
        task.setContactPhone(dto.getContactPhone());
        task.setTaskRequirement(dto.getTaskRequirement());
        if (dto.getDeadline() != null) {
            try {
                task.setDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getDeadline()));
            } catch (Exception ignored) {
            }
        }
        task.setStatus("0");
        task.setDispatchUserId(SecurityUtils.getUserId());
        task.setDispatchTime(new Date());
        task.setCreateBy(SecurityUtils.getUsername());
        task.setCreateTime(new Date());
        int rows = rectTaskMapper.insertRectTask(task);

        addProgress(task.getTaskId(), null, "DISPATCH", "整改任务已下发，任务编号：" + taskNo);
        notifyUnitRoles(task, "新的整改任务", "审计处已下发整改任务：" + taskNo + "，请及时确认并分办。");

        if (dto.getIssueIds() != null) {
            for (Long issueId : dto.getIssueIds()) {
                RectIssue issue = rectIssueMapper.selectRectIssueById(issueId);
                if (issue != null && "0".equals(issue.getStatus())) {
                    RectIssue update = new RectIssue();
                    update.setIssueId(issueId);
                    update.setStatus("1");
                    update.setUpdateBy(SecurityUtils.getUsername());
                    update.setUpdateTime(new Date());
                    rectIssueMapper.updateRectIssue(update);
                }
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int batchDispatch(List<TaskDispatchDTO> dtoList) {
        int total = 0;
        for (TaskDispatchDTO dto : dtoList) {
            total += insertRectTask(dto);
        }
        return total;
    }

    @Override
    @Transactional
    public int confirmTask(Long taskId) {
        RectTask update = new RectTask();
        update.setTaskId(taskId);
        update.setStatus("1");
        update.setConfirmTime(new Date());
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(update);

        RectTask task = rectTaskMapper.selectRectTaskById(taskId);
        addProgress(taskId, null, "CONFIRM", "整改任务已确认接收");
        if (task != null && task.getDispatchUserId() != null) {
            rectNotificationService.notifyUser(task.getDispatchUserId(), taskId, firstIssueId(task),
                    "整改任务已确认", "整改单位已确认接收任务：" + safeTaskNo(task));
        }
        return rows;
    }

    @Override
    @Transactional
    public int assignTask(Long taskId, Long assignUserId) {
        RectTask update = new RectTask();
        update.setTaskId(taskId);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(update);

        RectTask task = rectTaskMapper.selectRectTaskById(taskId);
        addProgress(taskId, firstIssueId(task), "ASSIGN", "整改任务已分办给责任人，用户ID：" + assignUserId);
        rectNotificationService.notifyUser(assignUserId, taskId, firstIssueId(task),
                "整改任务已分办", "联络员已将整改任务分办给你：" + safeTaskNo(task) + "，请及时处理。");
        return rows;
    }

    @Override
    public void generateNotice(Long taskId, HttpServletResponse response) {
        try {
            RectTask task = selectRectTaskById(taskId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String deptName = "";
            if (task != null && task.getRectDeptId() != null) {
                var dept = sysDeptMapper.selectDeptById(task.getRectDeptId());
                if (dept != null) {
                    deptName = dept.getDeptName();
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=notice_" + taskId + ".docx");

            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setBold(true);
            r.setFontSize(22);
            r.setFontFamily("SimHei");
            r.setText("审计整改通知书");

            addSection(doc, "一、基本信息");
            addField(doc, "整改责任单位", deptName);
            addField(doc, "整改联络人", task != null ? task.getContactPerson() : "");
            addField(doc, "联系电话", task != null ? task.getContactPhone() : "");
            addField(doc, "任务编号", task != null ? task.getTaskNo() : "");
            addField(doc, "整改截止日期", task != null && task.getDeadline() != null ? sdf.format(task.getDeadline()) : "");

            addSection(doc, "二、整改要求");
            addText(doc, task != null && task.getTaskRequirement() != null ? task.getTaskRequirement() : "请按审计整改要求完成整改。");

            addSection(doc, "三、问题明细");
            List<RectIssue> issues = loadIssues(task);
            if (issues.isEmpty()) {
                addText(doc, "暂无关联问题明细，请以系统问题台账为准。");
            } else {
                int index = 1;
                for (RectIssue issue : issues) {
                    addText(doc, index + ". " + value(issue.getIssueTitle(), "未命名问题"));
                    addText(doc, "问题描述：" + value(issue.getIssueDesc(), "-"));
                    BigDecimal amount = issue.getIssueAmount();
                    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                        addText(doc, "涉及金额：" + amount);
                    }
                    index++;
                }
            }

            addSection(doc, "四、办理要求");
            addText(doc, "请整改单位在系统中完成任务确认、责任分办、整改方案、佐证材料、整改报告和销号申请。临近截止或逾期未完成时，系统将持续推送提醒。");

            doc.write(response.getOutputStream());
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate notice", e);
        }
    }

    @Override
    public List<RectTask> selectMyTaskList() {
        return rectTaskMapper.selectRectTaskListByDeptId(SecurityUtils.getLoginUser().getDeptId());
    }

    @Override
    public List<RectTask> selectOverdueAlertList() {
        List<RectTask> all = rectTaskMapper.selectRectTaskListByDeptId(SecurityUtils.getLoginUser().getDeptId());
        List<RectTask> overdue = new ArrayList<>();
        Date now = new Date();
        for (RectTask task : all) {
            if (task.getDeadline() != null && task.getDeadline().before(now)
                    && !"4".equals(task.getStatus()) && !"3".equals(task.getStatus())) {
                overdue.add(task);
            }
        }
        return overdue;
    }

    @Override
    @Transactional
    public int updateRectTask(RectTask task) {
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        return rectTaskMapper.updateRectTask(task);
    }

    @Override
    @Transactional
    public int deleteRectTaskByIds(Long[] taskIds) {
        return rectTaskMapper.deleteRectTaskByIds(taskIds);
    }

    private void addProgress(Long taskId, Long issueId, String type, String content) {
        RectProgress progress = new RectProgress();
        progress.setTaskId(taskId);
        progress.setIssueId(issueId);
        progress.setProgressType(type);
        progress.setContent(content);
        progress.setOperatorId(SecurityUtils.getUserId());
        progress.setOperatorName(SecurityUtils.getUsername());
        progress.setOperateTime(new Date());
        progress.setCreateBy(SecurityUtils.getUsername());
        progress.setCreateTime(new Date());
        rectProgressMapper.insertRectProgress(progress);
    }

    private void notifyUnitRoles(RectTask task, String title, String content) {
        if (task == null) {
            return;
        }
        rectNotificationService.notifyRoles(UNIT_LIAISON_ROLES, task.getRectDeptId(), task.getTaskId(), firstIssueId(task), title, content);
        rectNotificationService.notifyRoles(UNIT_LEADER_ROLES, task.getRectDeptId(), task.getTaskId(), firstIssueId(task), title, content);
    }

    private Long firstIssueId(RectTask task) {
        if (task == null || task.getIssueIds() == null) {
            return null;
        }
        String ids = task.getIssueIds().replace("[", "").replace("]", "");
        for (String id : ids.split(",")) {
            try {
                return Long.parseLong(id.trim());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private List<RectIssue> loadIssues(RectTask task) {
        List<RectIssue> issues = new ArrayList<>();
        if (task == null || task.getIssueIds() == null) {
            return issues;
        }
        String ids = task.getIssueIds().replace("[", "").replace("]", "");
        for (String id : ids.split(",")) {
            try {
                RectIssue issue = rectIssueMapper.selectRectIssueById(Long.parseLong(id.trim()));
                if (issue != null) {
                    issues.add(issue);
                }
            } catch (Exception ignored) {
            }
        }
        return issues;
    }

    private String safeTaskNo(RectTask task) {
        return task != null && task.getTaskNo() != null ? task.getTaskNo() : "任务";
    }

    private String value(String value, String fallback) {
        return value != null && !value.isEmpty() ? value : fallback;
    }

    private void addSection(XWPFDocument doc, String title) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(14);
        r.setFontFamily("SimHei");
        r.setText(title);
    }

    private void addField(XWPFDocument doc, String label, String value) {
        addText(doc, label + "：" + value(value, ""));
    }

    private void addText(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(360);
        XWPFRun r = p.createRun();
        r.setFontSize(12);
        r.setText(text != null ? text : "");
    }
}
