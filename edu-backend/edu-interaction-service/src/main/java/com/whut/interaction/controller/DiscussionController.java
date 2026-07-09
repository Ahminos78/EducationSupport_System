package com.whut.interaction.controller;

import com.whut.common.result.Result;
import com.whut.interaction.dto.DiscussionStatusUpdateRequest;
import com.whut.interaction.dto.DiscussionUpdateRequest;
import com.whut.interaction.dto.ReplyCreateRequest;
import com.whut.interaction.dto.TopicCreateRequest;
import com.whut.interaction.service.DiscussionService;
import com.whut.interaction.vo.DiscussionResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @GetMapping("/courses/{courseId}/topics")
    public Result<List<DiscussionResponse>> topics(@PathVariable Long courseId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(discussionService.topics(courseId, page, size));
    }

    @GetMapping("/topics/{topicId}")
    public Result<DiscussionResponse> detail(@PathVariable Long topicId) {
        return Result.success(discussionService.detail(topicId));
    }

    @GetMapping("/topics/{topicId}/replies")
    public Result<List<DiscussionResponse>> replies(@PathVariable Long topicId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.success(discussionService.replies(topicId, page, size));
    }

    @PostMapping("/topics")
    public Result<DiscussionResponse> createTopic(@RequestBody TopicCreateRequest request) {
        return Result.success(discussionService.createTopic(request));
    }

    @PostMapping("/topics/{topicId}/replies")
    public Result<DiscussionResponse> createReply(@PathVariable Long topicId,
                                                  @RequestBody ReplyCreateRequest request) {
        return Result.success(discussionService.createReply(topicId, request));
    }

    @PutMapping("/{id}")
    public Result<DiscussionResponse> update(@PathVariable Long id,
                                             @RequestBody DiscussionUpdateRequest request) {
        return Result.success(discussionService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public Result<DiscussionResponse> updateStatus(@PathVariable Long id,
                                                   @RequestBody DiscussionStatusUpdateRequest request) {
        return Result.success(discussionService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        discussionService.delete(id);
        return Result.success();
    }
}
