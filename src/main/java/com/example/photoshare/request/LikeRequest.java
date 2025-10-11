package com.example.photoshare.request;

// 可以放在一个包如：com.example.photoshare.dto 或直接放在 controller 同包
public class LikeRequest {
    private String likerDeviceId;

    // 必须要有一个无参构造函数（Jackson 反序列化需要）
    public LikeRequest() {}

    public String getLikerDeviceId() {
        return likerDeviceId;
    }

    public void setLikerDeviceId(String likerDeviceId) {
        this.likerDeviceId = likerDeviceId;
    }
}