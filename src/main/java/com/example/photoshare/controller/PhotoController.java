package com.example.photoshare.controller;

import com.example.photoshare.domain.Photo;
import com.example.photoshare.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        List<Photo> photos = photoService.getAllPhotos();
        
        // 检查客户端IP
        String ipAddress = getClientIp(request);
        
        // 为每个照片添加是否已点赞的信息
        photos.forEach(photo -> {
            photo.setLikedByUser(photoService.isPhotoLikedByIp(photo.getId(), ipAddress));
        });
        
        model.addAttribute("photos", photos);
        return "index";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("photo") MultipartFile file,
            @RequestParam("description") String description,
            HttpServletRequest request) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"请选择要上传的文件\"}");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("{\"error\": \"请上传图片文件\"}");
        }

        try {
            // 保存照片
            Photo savedPhoto = photoService.savePhoto(file, description);
            
            return ResponseEntity.ok("{\"success\": true, \"message\": \"照片上传成功！\", \"photoId\": \"" + savedPhoto.getId() + "\"}");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\": \"文件保存失败: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/like/{photoId}")
    @ResponseBody
    public ResponseEntity<?> likePhoto(@PathVariable String photoId, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        
        boolean success = photoService.likePhoto(photoId, ipAddress);
        
        if (success) {
            return ResponseEntity.ok("{\"success\": true, \"message\": \"点赞成功！\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"error\": \"点赞失败\"}");
        }
    }

    @GetMapping("/photos/{photoId}/image")
    public ResponseEntity<Resource> getImage(@PathVariable String photoId) {
        Photo photo = photoService.getPhotoById(photoId);
        if (photo != null) {
            try {
                Path filePath = Paths.get("uploads/" + photo.getFilename());
                Resource resource = new UrlResource(filePath.toUri());
                
                if (resource.exists() && resource.isReadable()) {
                    String contentType = "image/jpeg"; // 默认类型
                    String filename = photo.getOriginalName();
                    
                    // 尝试根据文件扩展名确定内容类型
                    if (filename != null) {
                        if (filename.toLowerCase().endsWith(".png")) {
                            contentType = "image/png";
                        } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                            contentType = "image/jpeg";
                        } else if (filename.toLowerCase().endsWith(".gif")) {
                            contentType = "image/gif";
                        }
                    }
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getOriginalName() + "\"")
                            .body(resource);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    @GetMapping("/photos-data")
    @ResponseBody
    public ResponseEntity<List<Photo>> getPhotosData(HttpServletRequest request) {
        List<Photo> photos = photoService.getAllPhotos();

        // 为每个照片添加是否已点赞的信息
        String ipAddress = getClientIp(request);
        photos.forEach(photo -> {
            photo.setLikedByUser(photoService.isPhotoLikedByIp(photo.getId(), ipAddress));
        });

        return ResponseEntity.ok(photos);
    }

}