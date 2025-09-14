package com.example.photoshare.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Photo {
    @Id
    private String id;
    
    private String filename;
    private String originalName;
    private String description;
    private String uploadTime;
    private int likes;

    // 在 Photo 类中添加以下字段
    private boolean likedByUser;

    // 添加 getter 和 setter 方法
    public boolean isLikedByUser() {
        return likedByUser;
    }

    public void setLikedByUser(boolean likedByUser) {
        this.likedByUser = likedByUser;
    }
    
    @ElementCollection
    @CollectionTable(name = "photo_likers", joinColumns = @JoinColumn(name = "photo_id"))
    @Column(name = "liker_ip")
    private List<String> likedBy = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.uploadTime == null) {
            this.uploadTime = LocalDateTime.now().toString();
        }
    }

    // 构造函数
    public Photo() {}

    public Photo(String filename, String originalName, String description) {
        this.filename = filename;
        this.originalName = originalName;
        this.description = description;
        this.likes = 0;
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public void addLike(String ip) {
        if (!likedBy.contains(ip)) {
            likedBy.add(ip);
            likes++;
        }
    }

    public void removeLike(String ip) {
        if (likedBy.contains(ip)) {
            likedBy.remove(ip);
            likes--;
        }
    }
}