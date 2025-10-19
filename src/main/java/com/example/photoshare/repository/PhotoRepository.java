package com.example.photoshare.repository;

import com.example.photoshare.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    List<Photo> findAllByOrderByUploadTimeDesc();

    // 按点赞数量倒序排序
    List<Photo> findAllByOrderByLikesDesc();
    @Transactional
    Photo save(Photo photo);

}