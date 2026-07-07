package com.audit.rectification.controller;

import java.util.List;

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
import com.audit.rectification.service.IRectMaterialService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.file.FileUtils;

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

    /**
     * 查询整改材料列表
     */
    @PreAuthorize("@ss.hasPermi('rectification:material:list')")
    @GetMapping("/list/{issueId}")
    public TableDataInfo list(@PathVariable Long issueId) {
        startPage();
        List<RectMaterial> list = rectMaterialService.selectRectMaterialByIssueId(issueId);
        return getDataTable(list);
    }

    /**
     * 上传整改材料
     */
    @PreAuthorize("@ss.hasPermi('rectification:material:upload')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, RectMaterial material) {
        return toAjax(rectMaterialService.insertRectMaterial(material));
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
    @PreAuthorize("@ss.hasPermi('rectification:material:download')")
    @GetMapping("/info/{materialId}")
    public AjaxResult getInfo(@PathVariable Long materialId) {
        List<RectMaterial> list = rectMaterialService.selectRectMaterialByIssueId(materialId);
        return success(list.isEmpty() ? null : list.get(0));
    }
}
