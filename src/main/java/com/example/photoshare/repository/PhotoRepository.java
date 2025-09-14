package com.example.photoshare.repository;

import com.example.photoshare.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    List<Photo> findAllByOrderByUploadTimeDesc();
}