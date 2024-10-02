package com.UploadFile.controller;

import com.UploadFile.Entity.Event;
import com.UploadFile.Entity.Story;
import com.UploadFile.Repo.EventRepo;
import com.UploadFile.Repo.StoryRepo;
import com.UploadFile.Service.EventService;
import com.UploadFile.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepo eventRepo;

    @PostMapping("/uploads")
    public ResponseEntity<Object> uploadStory(@RequestParam Long id,
                                              @RequestParam String pathFile,
                                              @RequestParam MultipartFile file,
                                              @RequestParam String eventName ,
                                              @RequestParam String eventDetails ,
                                              @RequestParam String eventEnd

    ) {
        Event event = eventRepo.findById(id).orElse(new Event());
        event.setEventName(eventName);
        event.setEventDetails(eventDetails);
        event.setEventEnd(eventEnd);
        eventRepo.save(event);

        String message = eventService.UploadEvent(eventService.convertMultiPartFile(file), id, pathFile);
        return ResponseEntity.ok(message);
    }
}
