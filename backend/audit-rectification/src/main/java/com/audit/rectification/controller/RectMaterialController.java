package com.audit.rectification.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.audit.rectification.domain.RectMaterial;
import com.audit.rectification.mapper.RectMaterialMapper;
import com.audit.rectification.service.IRectMaterialService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 审计整改材料Controller
 *
 * @author audit
 * @date 2026-07-06
 */
@RestController
@RequestMapping("/rectification/material")
public class RectMaterialController extends BaseController {

    @Autowired
    private IRectMaterialService rectMaterialService;
    @Autowired
    private RectMaterialMapper rectMaterialMapper;

    /**
     * 查询整改材料列表
     */
    @PreAuthorize("@ss.hasAnyPermi('rectification:material:list,rectification:material:download,rectification:closure:audit,rectification:report:approve') or @ss.hasAnyRoles('audited_unit_leader,audit_lead,audit_director,rect_responsible')")
    @GetMapping("/list/{issueId}")
    public TableDataInfo list(@PathVariable Long issueId) {
        startPage();
        List<RectMaterial> list = rectMaterialService.selectRectMaterialByIssueId(issueId);
        // 角色过滤：整改责任人/被审单位负责人只看自己的，处长/组长看全部
        boolean isAdmin = com.ruoyi.common.utils.SecurityUtils.isAdmin();
        boolean isLead = com.ruoyi.common.utils.SecurityUtils.hasPermi("rectification:closure:audit");
        boolean isUnitLeader = com.ruoyi.common.utils.SecurityUtils.hasRole("audited_unit_leader");
        if (!isAdmin && !isLead && !isUnitLeader) {
            String username = getUsername();
            list.removeIf(m -> !username.equals(m.getCreateBy()));
        }
        return getDataTable(list);
    }

    /**
     * 上传整改材料
     */
    @PreAuthorize("@ss.hasPermi('rectification:material:upload')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, RectMaterial material) {
        try {
            String dir = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "material";
            File dirFile = new File(dir);
            if (!dirFile.exists()) dirFile.mkdirs();
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, savedName);
            file.transferTo(dest);
            material.setFileName(originalName);
            material.setFilePath(dir + File.separator + savedName);
            material.setFileSize(file.getSize());
            material.setFileExt(ext);
            return toAjax(rectMaterialService.insertRectMaterial(material));
        } catch (Exception e) {
            return error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除整改材料
     */
    @PreAuthorize("@ss.hasPermi('rectification:material:remove')")
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds) {
        return toAjax(rectMaterialService.deleteRectMaterialByIds(materialIds));
    }

    /**
     * 获取材料详情（含下载路径）
     */
    @org.springframework.web.bind.annotation.RequestMapping(
        value = "/download/{materialId}",
        method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST})
    @PreAuthorize("@ss.hasAnyPermi('rectification:material:download,rectification:material:list,rectification:closure:audit,rectification:report:approve') or @ss.hasAnyRoles('audited_unit_leader,audit_lead,audit_director,rect_responsible')")
    public void download(@PathVariable Long materialId, HttpServletResponse response) throws Exception {
        RectMaterial m = rectMaterialMapper.selectRectMaterialById(materialId);
        if (m == null) throw new RuntimeException("Attachment record not found");
        java.io.File f = new java.io.File(m.getFilePath());
        if (!f.exists()) throw new RuntimeException("Physical file not found: " + m.getFileName());
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(m.getFileName(), "UTF-8"));
        java.io.FileInputStream fis = new java.io.FileInputStream(f);
        org.apache.commons.io.IOUtils.copy(fis, response.getOutputStream());
        fis.close();
    }
}
