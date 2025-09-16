package com.example.photoshare.service;

import com.example.photoshare.domain.Photo;
import com.example.photoshare.repository.PhotoRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

        // 3. 将 MultipartFile 转为临时 File（供 Thumbnailator 使用）
        File tempInputFile = File.createTempFile("upload_", "_" + originalFilename);
        file.transferTo(tempInputFile);

        // 4. 使用 Thumbnailator 压缩图片（生成压缩后的 JPG）
        String compressedJpgFilename = "compressed_" + filename.replace(extension, ".jpg");
        Path compressedJpgPath = Paths.get(uploadDir, compressedJpgFilename);
        File compressedJpgFile = compressedJpgPath.toFile();
        Thumbnails.of(tempInputFile) // ← 重点：这里的 input 是 tempInputFile（File 类型）
                .size(1024, 1024)  // 限制最大宽高
                .outputQuality(0.7)  // 压缩质量 70%
                .outputFormat("jpg") // 输出 JPG
                .toFile(compressedJpgFile);

        // 5. 调用 cwebp，将压缩后的 JPG 转为 WebP
        String webpFilename = "compressed_" + filename.replace(extension, ".webp");
        Path webpPath = Paths.get(uploadDir, webpFilename);
        File webpFile = webpPath.toFile();

        // 调用你的工具方法，传入压缩后的 JPG 路径 和 WebP 输出路径
        // 读取 PNG
        BufferedImage image = ImageIO.read(compressedJpgFile);
        // 写入 WebP
        ImageIO.write(image, "webp", webpFile);

        // 6. （可选）删除临时文件
        tempInputFile.delete();

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