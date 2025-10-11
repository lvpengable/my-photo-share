package com.example.photoshare.repository;

import com.example.photoshare.domain.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {
    // 可选：根据 photoId 查询评论列表
    List<PhotoComment> findByPhotoIdOrderByCreatedAtDesc(String photoId);
}