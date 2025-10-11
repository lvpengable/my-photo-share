package com.example.photoshare.controller;

import com.example.photoshare.domain.PhotoComment;
import com.example.photoshare.repository.PhotoCommentRepository;
import com.example.photoshare.request.CommentRequest;
import com.example.photoshare.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    private PhotoCommentRepository photoCommentRepository;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 提交一条评论
     * POST /api/comments
     */
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request) {

        String photoId = request.getPhotoId();
        String commenterId = request.getCommenterId();
        String commenterName = request.getCommenterName();
        String commentText = request.getCommentText();
        // 如果 commenterName 为空，可以默认为 "匿名用户"
        if (commenterName == null || commenterName.trim().isEmpty()) {
            commenterName = "匿名用户";
        }

        PhotoComment comment = commentService.addComment(photoId, commenterId, commenterName, commentText);

        return ResponseEntity.ok(createSuccessResponse(true, "评论发表成功", comment));

    }

    /**
     * ✅ GET 接口：根据 photoId 查询评论列表，按时间倒序
     */
    @GetMapping
    public Map<String, Object> getCommentsByPhotoId(@RequestParam String photoId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<PhotoComment> comments = photoCommentRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);

            // 构造评论列表（map 列表，
            List<Map<String, Object>> commentsList = new ArrayList<>();
            if (comments != null) {
                for (PhotoComment comment : comments) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("id", comment.getId());
                    c.put("commenterId", comment.getCommenterId());
                    c.put("commenterName", comment.getCommenterName() != null ? comment.getCommenterName() : "匿名用户");
                    c.put("commentText", comment.getCommentText());
                    c.put("createdAt", comment.getCreatedAt()); // 可格式化为字符串，但先传原对象
                    commentsList.add(c);
                }
            }

            response.put("success", true);
            response.put("comments", commentsList);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取评论失败");
        }

        return response;
    }


    private Map<String, Object> createSuccessResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("comment", data);  // 或者你也可以命名为 "data"
        }
        return response;
    }
}