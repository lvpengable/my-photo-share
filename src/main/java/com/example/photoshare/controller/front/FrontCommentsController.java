package com.example.photoshare.controller.front;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontCommentsController {

    @GetMapping("/photo-comments")
    public String ranking() {
        return "photo-comments"; // 对应 src/main/resources/templates/clear-all.html（Thymeleaf）
        // 或者，如果你把页面放在 static/ 下，直接返回静态资源路径：
        // return "forward:/clear-all.html"; 或者直接让用户访问 /clear-all.html
    }
}