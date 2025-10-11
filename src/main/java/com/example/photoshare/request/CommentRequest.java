package com.example.photoshare.request;

public class CommentRequest {
    private String photoId;
    private String commenterId;
    private String commenterName;
    private String commentText;

    // 必须有无参构造
    public CommentRequest() {}

    // Getter 和 Setter
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getCommenterId() { return commenterId; }
    public void setCommenterId(String commenterId) { this.commenterId = commenterId; }

    public String getCommenterName() { return commenterName; }
    public void setCommenterName(String commenterName) { this.commenterName = commenterName; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}