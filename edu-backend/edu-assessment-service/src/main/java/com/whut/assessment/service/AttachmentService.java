package com.whut.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.AssignmentAttachment;
import com.whut.assessment.entity.Submission;
import com.whut.assessment.entity.SubmissionAttachment;
import com.whut.assessment.mapper.AssignmentAttachmentMapper;
import com.whut.assessment.mapper.SubmissionAttachmentMapper;
import com.whut.assessment.vo.AttachmentResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final AssignmentAttachmentMapper assignmentAttachmentMapper;
    private final SubmissionAttachmentMapper submissionAttachmentMapper;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final Path storageRoot;

    public AttachmentService(AssignmentAttachmentMapper assignmentAttachmentMapper,
                             SubmissionAttachmentMapper submissionAttachmentMapper,
                             AssignmentService assignmentService,
                             SubmissionService submissionService,
                             @Value("${edu.assessment.storage-dir:./data/assessment-files}") String storageDir) {
        this.assignmentAttachmentMapper = assignmentAttachmentMapper;
        this.submissionAttachmentMapper = submissionAttachmentMapper;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    public List<AttachmentResponse> listAssignmentAttachments(Long assignmentId) {
        Assignment assignment = assignmentService.requireAssignment(assignmentId);
        assignmentService.assertAssignmentAccess(assignment, currentUser());
        return assignmentAttachmentMapper.selectList(new LambdaQueryWrapper<AssignmentAttachment>()
                        .eq(AssignmentAttachment::getAssignmentId, assignmentId)
                        .orderByAsc(AssignmentAttachment::getId))
                .stream().map(this::toResponse).toList();
    }

    public List<AttachmentResponse> listSubmissionAttachments(Long submissionId) {
        submissionService.assertSubmissionAccess(submissionId, currentUser());
        return submissionAttachmentMapper.selectList(new LambdaQueryWrapper<SubmissionAttachment>()
                        .eq(SubmissionAttachment::getSubmissionId, submissionId)
                        .orderByAsc(SubmissionAttachment::getId))
                .stream().map(this::toResponse).toList();
    }

    public AttachmentResponse uploadAssignmentAttachment(Long assignmentId, MultipartFile file) {
        AuthUser currentUser = currentUser();
        Assignment assignment = assignmentService.requireAssignment(assignmentId);
        if (!assignmentService.canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权上传该作业附件");
        }
        StoredFile stored = store(file, "assignments");
        AssignmentAttachment attachment = new AssignmentAttachment();
        attachment.setAssignmentId(assignmentId);
        attachment.setOriginalName(stored.originalName());
        attachment.setStoredName(stored.storedName());
        attachment.setContentType(stored.contentType());
        attachment.setFileSize(stored.fileSize());
        attachment.setUploadedBy(currentUser.getId());
        assignmentAttachmentMapper.insert(attachment);
        return toResponse(attachment);
    }

    public AttachmentResponse uploadSubmissionAttachment(Long submissionId, MultipartFile file) {
        AuthUser currentUser = currentUser();
        Submission submission = submissionService.requireOwnedSubmission(submissionId, currentUser);
        StoredFile stored = store(file, "submissions");
        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setSubmissionId(submission.getId());
        attachment.setOriginalName(stored.originalName());
        attachment.setStoredName(stored.storedName());
        attachment.setContentType(stored.contentType());
        attachment.setFileSize(stored.fileSize());
        submissionAttachmentMapper.insert(attachment);
        return toResponse(attachment);
    }

    public DownloadFile assignmentDownload(Long attachmentId) {
        AssignmentAttachment attachment = assignmentAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw BusinessException.notFound("作业附件不存在");
        }
        Assignment assignment = assignmentService.requireAssignment(attachment.getAssignmentId());
        assignmentService.assertAssignmentAccess(assignment, currentUser());
        return load("assignments", attachment.getStoredName(), attachment.getOriginalName(), attachment.getContentType());
    }

    public DownloadFile submissionDownload(Long attachmentId) {
        SubmissionAttachment attachment = submissionAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw BusinessException.notFound("提交附件不存在");
        }
        submissionService.assertSubmissionAccess(attachment.getSubmissionId(), currentUser());
        return load("submissions", attachment.getStoredName(), attachment.getOriginalName(), attachment.getContentType());
    }

    private StoredFile store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("单个文件不能超过20MB");
        }
        String originalName = Path.of(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename())
                .getFileName().toString();
        String extension = originalName.lastIndexOf('.') >= 0 ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String storedName = UUID.randomUUID() + extension;
        Path targetDirectory = storageRoot.resolve(directory).normalize();
        Path target = targetDirectory.resolve(storedName).normalize();
        if (!target.startsWith(targetDirectory)) {
            throw BusinessException.badRequest("文件名不合法");
        }
        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException(500, "文件保存失败");
        }
        return new StoredFile(originalName, storedName, file.getContentType(), file.getSize());
    }

    private DownloadFile load(String directory, String storedName, String originalName, String contentType) {
        try {
            Path file = storageRoot.resolve(directory).resolve(storedName).normalize();
            if (file.startsWith(storageRoot) && Files.exists(file)) {
                Resource resource = new UrlResource(file.toUri());
                return new DownloadFile(resource, originalName, contentType);
            }
            ClassPathResource seedResource = new ClassPathResource("seed-files/" + storedName);
            if (seedResource.exists()) {
                return new DownloadFile(seedResource, originalName, contentType);
            }
            throw BusinessException.notFound("附件文件不存在");
        } catch (IOException exception) {
            throw BusinessException.notFound("附件文件不存在");
        }
    }

    private AttachmentResponse toResponse(AssignmentAttachment attachment) {
        AttachmentResponse response = baseResponse(attachment.getId(), attachment.getOriginalName(),
                attachment.getContentType(), attachment.getFileSize(), attachment.getCreatedAt());
        response.setDownloadUrl("/assessments/assignment-attachments/" + attachment.getId() + "/download");
        return response;
    }

    private AttachmentResponse toResponse(SubmissionAttachment attachment) {
        AttachmentResponse response = baseResponse(attachment.getId(), attachment.getOriginalName(),
                attachment.getContentType(), attachment.getFileSize(), attachment.getCreatedAt());
        response.setDownloadUrl("/assessments/submission-attachments/" + attachment.getId() + "/download");
        return response;
    }

    private AttachmentResponse baseResponse(Long id, String name, String type, Long size, java.time.LocalDateTime createdAt) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(id);
        response.setOriginalName(name);
        response.setContentType(type);
        response.setFileSize(size);
        response.setCreatedAt(createdAt);
        return response;
    }

    private AuthUser currentUser() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }

    private record StoredFile(String originalName, String storedName, String contentType, long fileSize) {
    }

    public record DownloadFile(Resource resource, String originalName, String contentType) {
    }
}
