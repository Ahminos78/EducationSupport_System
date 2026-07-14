package com.whut.assessment.controller;

import com.whut.assessment.service.AttachmentService;
import com.whut.assessment.vo.AttachmentResponse;
import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/assignments/{assignmentId}/attachments")
    public Result<List<AttachmentResponse>> assignmentAttachments(@PathVariable Long assignmentId) {
        return Result.success(attachmentService.listAssignmentAttachments(assignmentId));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping(value = "/assignments/{assignmentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AttachmentResponse> uploadAssignmentAttachment(@PathVariable Long assignmentId,
                                                                  @RequestParam("file") MultipartFile file) {
        return Result.success(attachmentService.uploadAssignmentAttachment(assignmentId, file));
    }

    @GetMapping("/submissions/{submissionId}/attachments")
    public Result<List<AttachmentResponse>> submissionAttachments(@PathVariable Long submissionId) {
        return Result.success(attachmentService.listSubmissionAttachments(submissionId));
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping(value = "/submissions/{submissionId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AttachmentResponse> uploadSubmissionAttachment(@PathVariable Long submissionId,
                                                                  @RequestParam("file") MultipartFile file) {
        return Result.success(attachmentService.uploadSubmissionAttachment(submissionId, file));
    }

    @GetMapping("/assignment-attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAssignmentAttachment(@PathVariable Long attachmentId) {
        return downloadResponse(attachmentService.assignmentDownload(attachmentId));
    }

    @GetMapping("/submission-attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadSubmissionAttachment(@PathVariable Long attachmentId) {
        return downloadResponse(attachmentService.submissionDownload(attachmentId));
    }

    private ResponseEntity<Resource> downloadResponse(AttachmentService.DownloadFile file) {
        MediaType mediaType;
        try {
            mediaType = file.contentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(file.contentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.originalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .body(file.resource());
    }
}
