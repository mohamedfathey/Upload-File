package com.UploadFile.controller;

import com.UploadFile.Entity.Story;
import com.UploadFile.Repo.StoryRepo;
import com.UploadFile.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;@RestController
@RequestMapping("/story")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private StoryRepo storyRepo;

    @PostMapping("/uploads")
    public ResponseEntity<Object> uploadStory(@RequestParam Long id,
                                              @RequestParam String pathFile,
                                              @RequestParam MultipartFile file) {
        Story story = storyRepo.findById(id).orElse(new Story());
        storyRepo.save(story);

        String message = storyService.uploadStory(storyService.convertMultiPartFile(file), id, pathFile);
        return ResponseEntity.ok(message);
    }
}
