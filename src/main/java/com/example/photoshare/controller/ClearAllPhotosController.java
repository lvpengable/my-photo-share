package com.example.photoshare.controller;

import com.example.photoshare.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class ClearAllPhotosController {

    @Autowired
    private PhotoRepository photoRepository;

    @PostMapping("/clear-all-photos")
    public ResponseEntity<?> clearAllPhotos() {
        try {
            // 可选：先查询当前有多少条记录（调试用）
            long countBefore = photoRepository.count();
            System.out.println("当前数据库中的照片数量（删除前）: " + countBefore);

            // 🔥 核心：直接删除所有照片记录
            photoRepository.deleteAll();

            // 可选：返回删除的记录数量（如果你想显示）
            long countAfter = photoRepository.count();
            System.out.println("当前数据库中的照片数量（删除后）: " + countAfter);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("✅ 成功清空数据库中的所有照片记录，共删除 %d 条", countBefore));
            response.put("deletedCount", countBefore);

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "清空数据库照片记录时发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    // ✅ 你可以根据实际情况修改这个路径！
    // 比如：项目外的目录 /uploads/ 或 /data/uploads/photos/
    // 请确保这个路径是照片实际存储的目录！！
    private static final String UPLOADS_DIR_PATH = "uploads"; // 或者 "/uploads/"、"/data/uploads/photos/"

    @PostMapping("/clear-all-photos-real")
    public ResponseEntity<?> clearAllPhotosReal() {
        try {
            // 构建 File 对象（如果是相对路径如 "uploads"，则是项目工作目录下的 uploads 文件夹）
            File uploadsDir = new File(UPLOADS_DIR_PATH);

            // 如果你使用的是绝对路径，比如 Linux 服务器上的 /uploads/，可以这样：
            // File uploadsDir = new File("/uploads/");

            if (!uploadsDir.exists()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "照片目录不存在，可能没有照片需要删除");
                return ResponseEntity.ok().body(response);
            }

            if (!uploadsDir.isDirectory()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "指定的路径不是一个目录: " + UPLOADS_DIR_PATH);
                return ResponseEntity.badRequest().body(response);
            }

            // 获取目录下所有文件
            File[] files = uploadsDir.listFiles();
            if (files == null || files.length == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "照片目录为空，没有照片需要删除");
                return ResponseEntity.ok().body(response);
            }

            int deletedCount = 0;
            for (File file : files) {
                if (file.isFile()) {
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        deletedCount++;
                    } else {
                        System.err.println("无法删除文件: " + file.getAbsolutePath());
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("✅ 成功删除 %d 张照片", deletedCount));
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "删除照片时发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}