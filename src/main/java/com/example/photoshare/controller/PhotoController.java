package com.example.photoshare.controller;

import com.example.photoshare.constant.PhotoCompressConstant;
import com.example.photoshare.domain.Photo;
import com.example.photoshare.dto.CommentDto;
import com.example.photoshare.request.LikeRequest;
import com.example.photoshare.response.PhotoDetailResponse;
import com.example.photoshare.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> likePhoto(@PathVariable String photoId, @RequestBody LikeRequest likeRequest, HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        String likerDeviceId = likeRequest.getLikerDeviceId();
        System.out.println("likePhoto=" + ipAddress + " likerDeviceId=" + likerDeviceId);
//        boolean success = photoService.likePhoto(photoId, likerDeviceId);
//
//        if (success) {
//            return ResponseEntity.ok("{\"success\": true, \"message\": \"点赞成功！\"}");
//        } else {
//            return ResponseEntity.badRequest().body("{\"error\": \"点赞失败\"}");
//        }

        // ✅ 调用 service 方法，执行【点赞/取消点赞 toggle】逻辑，并返回当前是否已点赞
        boolean isNowLiked = photoService.toggleLike(photoId, likerDeviceId);

        // 构造返回信息
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("isLiked", isNowLiked);

        // 可选：根据状态设置不同提示信息
        if (isNowLiked) {
            result.put("message", "点赞成功！");
        } else {
            result.put("message", "取消点赞成功");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/static/photos/{photoId}")
    public ResponseEntity<Resource> getImage(@PathVariable String photoId) {
        Photo photo = photoService.getPhotoById(photoId);
        if (photo != null) {
            try {
                String fileNameDB = photo.getFilename();
                String newFilename = fileNameDB.substring(0, fileNameDB.lastIndexOf(PhotoCompressConstant.DOT)) + PhotoCompressConstant.JPG;

                Path filePath = Paths.get("uploads/" + PhotoCompressConstant.COMPRESSED + newFilename);
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

    @GetMapping("/photos/{photoId}")
    @ResponseBody
    public ResponseEntity<?> getPhotoDetail(@PathVariable String photoId, HttpServletRequest request) {
        try {
            // 1. 查询照片
            Photo photo = photoService.getPhotoById(photoId);

            // 2. 查询当前用户（IP 或 设备ID）是否点过赞
            String ipAddress = getClientIp(request);
            // 如果你是用 like_device_id 来防重复点赞，请传入设备唯一标识而不是 IP
            boolean likedByUser = photoService.isPhotoLikedByIp(photoId, ipAddress); // 或改成设备ID

            // 3. 查询该照片的评论列表
            List<CommentDto> comments = photoService.getCommentsByPhotoId(photoId);

            // 4. 组装返回对象
            PhotoDetailResponse response = new PhotoDetailResponse(photo, likedByUser, comments);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}