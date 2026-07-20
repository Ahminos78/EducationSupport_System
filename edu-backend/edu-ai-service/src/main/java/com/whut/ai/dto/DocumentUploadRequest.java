package com.whut.ai.dto;

public class DocumentUploadRequest {

    /** 文档标题 */
    private String title;

    /** 文档类型（pdf, docx, txt 等） */
    private String docType;

    /** 文档内容（Base64 编码或纯文本） */
    private String content;

    /** 文件下载 URL */
    private String fileUrl;

    /** 关联课程 ID */
    private Long courseId;

    /** 文档标签 */
    private String tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
