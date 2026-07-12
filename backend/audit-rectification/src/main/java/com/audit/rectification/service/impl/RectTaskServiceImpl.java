package com.audit.rectification.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.audit.rectification.domain.RectIssue;
import com.audit.rectification.domain.RectNotification;
import com.audit.rectification.domain.RectProgress;
import com.audit.rectification.domain.RectTask;
import com.audit.rectification.domain.dto.TaskDispatchDTO;
import com.audit.rectification.mapper.*;
import com.audit.rectification.service.IRectTaskService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.*;

@Service
public class RectTaskServiceImpl implements IRectTaskService {

    @Autowired private RectTaskMapper rectTaskMapper;
    @Autowired private RectIssueMapper rectIssueMapper;
    @Autowired private RectProgressMapper rectProgressMapper;
    @Autowired private RectNotificationMapper rectNotificationMapper;
    @Autowired private SysDeptMapper sysDeptMapper;

    @Override
    public List<RectTask> selectRectTaskList(RectTask task) {
        return rectTaskMapper.selectRectTaskList(task);
    }

    @Override
    public RectTask selectRectTaskById(Long taskId) {
        return rectTaskMapper.selectRectTaskById(taskId);
    }

    @Override @Transactional
    public int insertRectTask(TaskDispatchDTO dto) {
        String taskNo = "RW" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        RectTask task = new RectTask();
        task.setTaskNo(taskNo);
        if (dto.getIssueIds() != null && !dto.getIssueIds().isEmpty())
            task.setIssueIds(dto.getIssueIds().toString());
        task.setRectDeptId(dto.getRectDeptId());
        task.setContactPerson(dto.getContactPerson());
        task.setContactPhone(dto.getContactPhone());
        task.setTaskRequirement(dto.getTaskRequirement());
        if (dto.getDeadline() != null) {
            try { task.setDeadline(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getDeadline())); } catch (Exception e) {}
        }
        task.setStatus("0"); task.setDispatchUserId(SecurityUtils.getUserId());
        task.setDispatchTime(new Date());
        task.setCreateBy(SecurityUtils.getUsername()); task.setCreateTime(new Date());
        int rows = rectTaskMapper.insertRectTask(task);
        addProgress(task.getTaskId(), null, "DISPATCH", "整改任务已下发，任务编号：" + taskNo);
        addNotification(task.getTaskId(), "整改任务", "新的整改任务：" + taskNo);
        // 更新关联问题状态: 待下发(0) -> 整改中(1)
        if (dto.getIssueIds() != null) {
            for (Long issueId : dto.getIssueIds()) {
                RectIssue issue = rectIssueMapper.selectRectIssueById(issueId);
                if (issue != null && "0".equals(issue.getStatus())) {
                    issue.setStatus("1");
                    rectIssueMapper.updateRectIssue(issue);
                }
            }
        }
        return rows;
    }

    @Override @Transactional
    public int batchDispatch(List<TaskDispatchDTO> dtoList) {
        int total = 0;
        for (TaskDispatchDTO dto : dtoList) total += insertRectTask(dto);
        return total;
    }

    @Override @Transactional
    public int confirmTask(Long taskId) {
        RectTask t = new RectTask(); t.setTaskId(taskId); t.setStatus("1");
        t.setConfirmTime(new Date()); t.setUpdateBy(SecurityUtils.getUsername()); t.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(t);
        addProgress(taskId, null, "CONFIRM", "整改任务已确认接收");
        return rows;
    }

    @Override @Transactional
    public int assignTask(Long taskId, Long assignUserId) {
        RectTask t = new RectTask(); t.setTaskId(taskId);
        t.setUpdateBy(SecurityUtils.getUsername()); t.setUpdateTime(new Date());
        int rows = rectTaskMapper.updateRectTask(t);
        addProgress(taskId, null, "ASSIGN", "整改任务已分办给责任人，用户ID：" + assignUserId);
        return rows;
    }

    @Override
    public void generateNotice(Long taskId, HttpServletResponse response) {
        try {
            RectTask task = selectRectTaskById(taskId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy年MM月dd日");

            // Get dept name
            String deptName = "";
            if (task != null && task.getRectDeptId() != null) {
                var dept = sysDeptMapper.selectDeptById(task.getRectDeptId());
                if (dept != null) deptName = dept.getDeptName();
            }

            // Get linked issues
            List<RectIssue> issues = new ArrayList<>();
            if (task != null && task.getIssueIds() != null) {
                String ids = task.getIssueIds().replace("[","").replace("]","");
                for (String id : ids.split(",")) {
                    try {
                        RectIssue issue = rectIssueMapper.selectRectIssueById(Long.parseLong(id.trim()));
                        if (issue != null) issues.add(issue);
                    } catch (Exception e) {}
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=notice_" + taskId + ".docx");

            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph p; XWPFRun r;

            // ====== Title ======
            p = doc.createParagraph(); p.setAlignment(ParagraphAlignment.CENTER);
            r = p.createRun(); r.setBold(true); r.setFontSize(22); r.setFontFamily("SimHei");
            r.setText("审计整改通知书");

            // ====== Part 1: Header ======
            p = doc.createParagraph(); p.setAlignment(ParagraphAlignment.LEFT);
            r = p.createRun(); r.setBold(true); r.setFontSize(14); r.setFontFamily("SimHei");
            r.setText("一、基本信息");

            addField(doc, "整改责任单位", deptName);
            addField(doc, "整改联络人", task != null ? task.getContactPerson() : "");
            addField(doc, "联系电话", task != null ? task.getContactPhone() : "");
            addField(doc, "通知书编号", task != null ? task.getTaskNo() : "");
            addField(doc, "下发日期", task != null && task.getDispatchTime() != null ? sdf.format(task.getDispatchTime()) : "");

            // ====== Part 2: Timeline & Warning ======
            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(14); r.setFontFamily("SimHei");
            r.setText("二、整改时限与预警说明");

            addField(doc, "整改截止时限", task != null && task.getDeadline() != null ? sdf.format(task.getDeadline()) : "");
            p = doc.createParagraph(); p.setIndentationLeft(400);
            r = p.createRun(); r.setFontSize(12);
            r.setText("逾期风险提示：若临近截止日期或超期未完成整改，系统将通过站内信、短信、企业微信等渠道循环预警，并将逾期情况通报审计处负责人。请务必在截止日期前完成整改并提交销号申请。");

            // ====== Part 3: Issue Details ======
            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(14); r.setFontFamily("SimHei");
            r.setText("三、审计发现问题明细清单");
            p = doc.createParagraph();
            r = p.createRun(); r.setFontSize(12);
            r.setText("贵单位需对以下审计发现的问题逐项进行整改：");

            if (issues.isEmpty()) {
                p = doc.createParagraph(); p.setIndentationLeft(400);
                r = p.createRun(); r.setFontSize(12); r.setText("（暂无明细问题数据，请参考整改要求执行）");
            } else {
                int idx = 1;
                for (RectIssue issue : issues) {
                    p = doc.createParagraph();
                    r = p.createRun(); r.setBold(true); r.setFontSize(12);
                    r.setText(idx + ". " + (issue.getIssueTitle() != null ? issue.getIssueTitle() : "无标题"));
                    if (issue.getIssueNo() != null) {
                        p = doc.createParagraph(); p.setIndentationLeft(400);
                        r = p.createRun(); r.setFontSize(11); r.setText("问题编号：" + issue.getIssueNo());
                    }
                    if (issue.getIssueDesc() != null) {
                        p = doc.createParagraph(); p.setIndentationLeft(400);
                        r = p.createRun(); r.setFontSize(11); r.setText("问题描述：" + issue.getIssueDesc());
                    }
                    if (issue.getIssueAmount() != null && issue.getIssueAmount().compareTo(BigDecimal.ZERO) > 0) {
                        p = doc.createParagraph(); p.setIndentationLeft(400);
                        r = p.createRun(); r.setFontSize(11); r.setText("涉及金额：¥" + issue.getIssueAmount());
                    }
                    if (issue.getLegalBasis() != null && !issue.getLegalBasis().isEmpty()) {
                        p = doc.createParagraph(); p.setIndentationLeft(400);
                        r = p.createRun(); r.setFontSize(11); r.setText("定性法规依据：" + issue.getLegalBasis());
                    }
                    if (issue.getResponsiblePerson() != null && !issue.getResponsiblePerson().isEmpty()) {
                        p = doc.createParagraph(); p.setIndentationLeft(400);
                        r = p.createRun(); r.setFontSize(11); r.setText("责任干部/责任人：" + issue.getResponsiblePerson());
                    }
                    idx++;
                }
            }

            // ====== Part 4: Requirements ======
            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(14); r.setFontFamily("SimHei");
            r.setText("四、整改填报与操作规范要求");
            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(12); r.setText("1. 整改方案编制：");
            r = p.createRun(); r.setFontSize(12);
            r.setText("接收任务后，请及时登录系统（PC端或企业微信移动端），在规定时间内线上编制并提交整改计划与整改方案。如需延期或转为长期持续整改，请在系统内提交申请并等待审批。");

            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(12); r.setText("2. 佐证材料规范：");
            r = p.createRun(); r.setFontSize(12);
            r.setText("整改完成后，必须上传合同、凭证、新出台的制度文件等全套电子版佐证材料，作为销号依据。材料类型包括但不限于：合同、财务凭证、红头文件、现场照片等。");

            p = doc.createParagraph();
            r = p.createRun(); r.setBold(true); r.setFontSize(12); r.setText("3. 审批上报流程：");
            r = p.createRun(); r.setFontSize(12);
            r.setText("整改填报完成后，系统将自动生成标准化《整改报告》。该报告须经贵单位负责人线上审批通过后，方可正式提交至审计处进行销号审核。");

            // ====== Footer ======
            doc.createParagraph();
            p = doc.createParagraph(); p.setAlignment(ParagraphAlignment.RIGHT);
            r = p.createRun(); r.setFontSize(12);
            r.setText("审计处");
            p = doc.createParagraph(); p.setAlignment(ParagraphAlignment.RIGHT);
            r = p.createRun(); r.setFontSize(12);
            r.setText(task != null && task.getDispatchTime() != null ? sdfFull.format(task.getDispatchTime()) : "");

            doc.write(response.getOutputStream());
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate notice", e);
        }
    }

    private void addField(XWPFDocument doc, String label, String value) {
        XWPFParagraph p = doc.createParagraph(); p.setIndentationLeft(400);
        XWPFRun r = p.createRun(); r.setBold(true); r.setFontSize(12); r.setText(label + "：");
        r = p.createRun(); r.setFontSize(12); r.setText(value != null ? value : "");
    }

    private void addProgress(Long taskId, Long issueId, String type, String content) {
        RectProgress pg = new RectProgress();
        pg.setTaskId(taskId); pg.setIssueId(issueId);
        pg.setProgressType(type); pg.setContent(content);
        pg.setOperatorId(SecurityUtils.getUserId());
        pg.setOperatorName(SecurityUtils.getUsername());
        pg.setOperateTime(new Date());
        rectProgressMapper.insertRectProgress(pg);
    }

    private void addNotification(Long taskId, String title, String content) {
        RectNotification n = new RectNotification();
        n.setTaskId(taskId); n.setNotifyType("SYSTEM_MSG");
        n.setTitle(title); n.setContent(content);
        n.setSendStatus("1"); n.setSendTime(new Date()); n.setReadStatus("0");
        rectNotificationMapper.insertRectNotification(n);
    }

    @Override public List<RectTask> selectMyTaskList() {
        return rectTaskMapper.selectRectTaskListByDeptId(SecurityUtils.getLoginUser().getDeptId());
    }

    @Override public List<RectTask> selectOverdueAlertList() {
        List<RectTask> all = rectTaskMapper.selectRectTaskListByDeptId(SecurityUtils.getLoginUser().getDeptId());
        List<RectTask> overdue = new ArrayList<>();
        Date now = new Date();
        for (RectTask t : all)
            if (t.getDeadline() != null && t.getDeadline().before(now) && !"4".equals(t.getStatus()) && !"3".equals(t.getStatus()))
                overdue.add(t);
        return overdue;
    }

    @Override @Transactional
    public int updateRectTask(RectTask task) {
        task.setUpdateBy(SecurityUtils.getUsername()); task.setUpdateTime(new Date());
        return rectTaskMapper.updateRectTask(task);
    }

    @Override @Transactional
    public int deleteRectTaskByIds(Long[] taskIds) {
        return rectTaskMapper.deleteRectTaskByIds(taskIds);
    }
}
