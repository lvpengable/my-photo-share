package com.example.photoshare.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_comments", schema = "photo_share")  // 指定 schema（你的表在 photo_share 库下）
@Data
public class PhotoComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // MySQL AUTO_INCREMENT
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private String photoId;  // 对应照片ID，比如 UUID

    @Column(name = "commenter_id")
    private String commenterId;  // 可为登录用户ID 或 设备ID

    @Column(name = "commenter_name")
    private String commenterName;  // 昵称或 "匿名用户"

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;  // 评论内容，不能为空

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 评论时间，默认当前时间，也可由后端传入

    // 如果你希望 createdAt 默认是当前时间，也可以通过字段初始化，或者数据库默认值
    public PhotoComment() {
        this.createdAt = LocalDateTime.now();  // 可选：Java 层设置默认时间
    }

    // 可选：带参构造（方便新建评论对象）
    public PhotoComment(String photoId, String commenterId, String commenterName, String commentText) {
        this.photoId = photoId;
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.createdAt = LocalDateTime.now();
    }
}