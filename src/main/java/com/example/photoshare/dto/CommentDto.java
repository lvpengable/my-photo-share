package com.example.photoshare.dto;

public class CommentDto {
    private String commenterName;   // 评论者昵称/名号
    private String commentText;     // 评论内容
    private String createdAt;       // 评论时间（可格式化为字符串）

    // 构造方法
    public CommentDto(String commenterName, String commentText, String createdAt) {
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public String getCommenterName() { return commenterName; }
    public void setCommenterName(String commenterName) { this.commenterName = commenterName; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}