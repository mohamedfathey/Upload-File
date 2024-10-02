package com.UploadFile.controller;

import com.UploadFile.Entity.Card;
import com.UploadFile.Repo.CardRepository;
import com.UploadFile.Service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class UploadFileController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CardRepository cardRepository;

    @PostMapping("/uploads")
    public ResponseEntity<Object> uploadFile(@RequestParam Long id,
                                             @RequestParam String pathFile,
                                             @RequestParam MultipartFile multipartFile,
                                             @RequestParam String cardName,
                                             @RequestParam String cardDetails) {

        Card card = cardRepository.findById(id).orElse(new Card());
        card.setCardName(cardName);
        card.setCardDetails(cardDetails);
        cardRepository.save(card);

        // رفع الملف
        String fileName = fileUploadService.storeFile(fileUploadService.convertMultiPartFile(multipartFile), id, pathFile);

        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }
}
