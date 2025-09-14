package com.example.photoshare.service;

import com.example.photoshare.domain.Photo;
import com.example.photoshare.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public PhotoService() {
        // 确保上传目录存在
        try {
            Files.createDirectories(Paths.get("uploads"));
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadDir, e);
        }
    }

    public List<Photo> getAllPhotos() {
        return photoRepository.findAllByOrderByUploadTimeDesc();
    }

    public Photo savePhoto(MultipartFile file, String description) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), filePath);

        // 创建并保存照片记录
        Photo photo = new Photo(filename, originalFilename, description);
        return photoRepository.save(photo);
    }

    public boolean likePhoto(String photoId, String ipAddress) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            photo.addLike(ipAddress);
            photoRepository.save(photo);
            return true;
        }
        return false;
    }

    public boolean isPhotoLikedByIp(String photoId, String ipAddress) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (photoOptional.isPresent()) {
            return photoOptional.get().getLikedBy().contains(ipAddress);
        }
        return false;
    }

    public Photo getPhotoById(String photoId) {
        return photoRepository.findById(photoId).orElse(null);
    }
}