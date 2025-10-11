package com.example.photoshare.repository;

import com.example.photoshare.domain.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private PhotoRepository photoRepository;

    @GetMapping
    public List<Photo> getRanking() {
        return photoRepository.findAllByOrderByLikesDesc();
    }
}