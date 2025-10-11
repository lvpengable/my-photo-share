package com.example.photoshare.service;

import com.example.photoshare.domain.PhotoComment;
import com.example.photoshare.repository.PhotoCommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final PhotoCommentRepository photoCommentRepository;

    public CommentService(PhotoCommentRepository photoCommentRepository) {
        this.photoCommentRepository = photoCommentRepository;
    }

    /**
     * 给指定 photoId 添加一条评论
     */
    public PhotoComment addComment(String photoId, String commenterId, String commenterName, String commentText) {
        PhotoComment comment = new PhotoComment(photoId, commenterId, commenterName, commentText);
        return photoCommentRepository.save(comment);  // 保存到数据库
    }
}