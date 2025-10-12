package com.example.photoshare.response;

import com.example.photoshare.domain.Photo;
import com.example.photoshare.dto.CommentDto;

import java.util.List;

public class PhotoDetailResponse {
    private Photo photo;                    // 照片基本信息
    private boolean likedByUser;            // 当前用户（IP）是否点过赞
    private List<CommentDto> comments;      // 该照片的评论列表

    // 构造方法
    public PhotoDetailResponse(Photo photo, boolean likedByUser, List<CommentDto> comments) {
        this.photo = photo;
        this.likedByUser = likedByUser;
        this.comments = comments;
    }

    // Getter & Setter
    public Photo getPhoto() { return photo; }
    public void setPhoto(Photo photo) { this.photo = photo; }

    public boolean isLikedByUser() { return likedByUser; }
    public void setLikedByUser(boolean likedByUser) { this.likedByUser = likedByUser; }

    public List<CommentDto> getComments() { return comments; }
    public void setComments(List<CommentDto> comments) { this.comments = comments; }
}