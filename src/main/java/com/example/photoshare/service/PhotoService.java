package com.example.photoshare.service;

import com.example.photoshare.constant.ExceptionMsg;
import com.example.photoshare.constant.PhotoCompressConstant;
import com.example.photoshare.domain.Photo;
import com.example.photoshare.domain.PhotoComment;
import com.example.photoshare.domain.PhotoLikers;
import com.example.photoshare.dto.CommentDto;
import com.example.photoshare.repository.PhotoCommentRepository;
import com.example.photoshare.repository.PhotoLikersRepository;
import com.example.photoshare.repository.PhotoRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoCommentRepository photoCommentRepository;

    @Autowired
    private PhotoLikersRepository photoLikersRepository;

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

        // 3. 将 MultipartFile 转为临时 File（供 Thumbnailator 使用）
        File tempInputFile = File.createTempFile("upload_", "_" + originalFilename);
        file.transferTo(tempInputFile);

        // 4. 使用 Thumbnailator 压缩图片（生成压缩后的 JPG）
        String compressedJpgFilename = PhotoCompressConstant.COMPRESSED + filename.replace(extension, PhotoCompressConstant.JPG);
        Path compressedJpgPath = Paths.get(uploadDir, compressedJpgFilename);
        File compressedJpgFile = compressedJpgPath.toFile();
        Thumbnails.of(tempInputFile) // ← 重点：这里的 input 是 tempInputFile（File 类型）
                .size(1024, 1024)  // 限制最大宽高
                .outputQuality(0.3)  // 压缩质量 70%
                .outputFormat("jpg") // 输出 JPG
                .toFile(compressedJpgFile);

//        // 5. 调用 cwebp，将压缩后的 JPG 转为 WebP
//        String webpFilename = "compressed_" + filename.replace(extension, ".webp");
//        Path webpPath = Paths.get(uploadDir, webpFilename);
//        File webpFile = webpPath.toFile();

        // 调用你的工具方法，传入压缩后的 JPG 路径 和 WebP 输出路径
        // 读取 PNG
//        BufferedImage image = ImageIO.read(compressedJpgFile);
        // 写入 WebP
//        ImageIO.write(image, "webp", webpFile);

        // 6. （可选）删除临时文件
        tempInputFile.delete();

        // 创建并保存照片记录
        Photo photo = new Photo(filename, originalFilename, description);
        return photoRepository.save(photo);
    }

    @Transactional
    public boolean likePhoto(String photoId, String ipAddress) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            PhotoLikers byLikerDeviceIdAndPhotoId = photoLikersRepository.findByLikerDeviceIdAndPhotoId(ipAddress, photoId);
            if (byLikerDeviceIdAndPhotoId == null) {
                PhotoLikers photoLikers = new PhotoLikers();
                photoLikers.setPhotoId(photoId);
                photoLikers.setLikedAt(LocalDateTime.now());
                photoLikers.setLikerDeviceId(ipAddress);
                photoLikersRepository.save(photoLikers);
                int likes = photo.getLikes();
                photo.setLikes(likes+1);
                photoRepository.save(photo);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean cancelLikePhoto(String photoId, String ipAddress) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            PhotoLikers byLikerDeviceIdAndPhotoId = photoLikersRepository.findByLikerDeviceIdAndPhotoId(ipAddress, photoId);
            if (byLikerDeviceIdAndPhotoId != null) {
                photoLikersRepository.deleteByLikerDeviceIdAndPhotoId(ipAddress, photoId);
                int likes = photo.getLikes();
                photo.setLikes(likes == 0 ? 0 : likes-1);
                photoRepository.save(photo);
                return true;
            }
        }
        return false;
    }

    public boolean isPhotoLikedByIp(String photoId, String ipAddress) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (photoOptional.isPresent()) {
            Photo photo = photoOptional.get();
            PhotoLikers byLikerDeviceIdAndPhotoId = photoLikersRepository.findByLikerDeviceIdAndPhotoId(ipAddress, photoId);
            return byLikerDeviceIdAndPhotoId != null;
        }
        return false;
    }

    public Photo getPhotoById(String photoId) {
        return photoRepository.findById(photoId).orElse(null);
    }

    public List<CommentDto> getCommentsByPhotoId(String photoId) {
        List<PhotoComment> photoComments = photoCommentRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
        return photoComments.stream()
                .map(comment -> {
                    // 格式化时间，比如 "yyyy-MM-dd HH:mm:ss"
                    String formattedTime = comment.getCreatedAt() != null
                            ? comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            : "未知时间";

                    return new CommentDto(
                            comment.getCommenterName(),  // 评论者昵称
                            comment.getCommentText(),    // 评论内容
                            formattedTime                // 格式化后的时间字符串
                    );
                })
                .collect(Collectors.toList());
    }

    public boolean toggleLike(String photoId, String likerDeviceId) {
        Optional<Photo> photoOptional = photoRepository.findById(photoId);
        if (! photoOptional.isPresent()) {
            return false;
        }
        Photo photo = photoOptional.get();
        List<PhotoLikers> photoLikers = photoLikersRepository.findByPhotoId(photoId);
        List<String> likedBy = photoLikers == null ? new ArrayList<>() : photoLikers.stream().map(PhotoLikers::getLikerDeviceId).collect(Collectors.toList());

        if (likedBy.contains(likerDeviceId)) {
            System.out.println("已经点赞，走取消");
            return this.cancelLikePhoto(photoId, likerDeviceId);
        } else {
            long likeCount = photoLikersRepository.countByLikerDeviceId(likerDeviceId);
            System.out.println("用户点赞过的照片总数: " + likeCount);
            if (likeCount >= 3) {
                throw new RuntimeException(ExceptionMsg.MAX_LIKE_COUNT_MSG);
            }

            return this.likePhoto(photoId, likerDeviceId);
        }
    }
}