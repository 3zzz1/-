package com.audit.rectification.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审计整改材料对象 rect_material
 *
 * @author audit
 * @date 2026-07-06
 */
public class RectMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 材料ID */
    private Long materialId;

    /** 关联任务ID */
    private Long taskId;

    /** 关联问题ID */
    private Long issueId;

    /** 材料类型（CONTRACT/VOUCHER/POLICY/PHOTO/OTHER） */
    private String materialType;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件扩展名 */
    private String fileExt;

    /** 上传用户ID */
    private Long uploadUserId;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    /** 删除标志（0=存在 2=删除） */
    private String delFlag;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RectMaterial{" +
                "materialId=" + materialId +
                ", taskId=" + taskId +
                ", issueId=" + issueId +
                ", materialType='" + materialType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileExt='" + fileExt + '\'' +
                ", uploadUserId=" + uploadUserId +
                ", uploadTime=" + uploadTime +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
