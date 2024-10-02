package com.UploadFile.Service;

import com.UploadFile.Entity.Card;
import com.UploadFile.Error.FileStorageException;
import com.UploadFile.Repo.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {
    Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private CardRepository cardRepository;

    private Path fileStorageLocation;

    @Value("${file.upload.base.path}")
    private String basePath;

    // استخدام Paths.get() لبناء المسار بطريقة صحيحة
    public String storeFile(File file, Long id, String pathType) {
        try {
            // تجنب استخدام الأحرف غير الصالحة
            this.fileStorageLocation = Paths.get(basePath, pathType.replace("\\", "/")).toAbsolutePath().normalize();

            Files.createDirectories(this.fileStorageLocation); // إنشاء الدليل إذا لم يكن موجودًا

            String fileName = StringUtils.cleanPath(id + "-" + file.getName());

            // تحقق من صحة اسم الملف
            if (fileName.contains("..")) {
                throw new FileStorageException("Invalid path sequence in file name: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            try (InputStream targetStream = new FileInputStream(file)) {
                Files.copy(targetStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // تحديث الـ Card بالمسار الخاص بالصورة
            Card card = cardRepository.findById(id).orElseThrow(() -> new FileStorageException("Card not found with id: " + id));
            card.setCardMediaURL(targetLocation.toString());
            cardRepository.save(card);

            return fileName;

        } catch (IOException e) {
            throw new FileStorageException("Could not store file. Error: " + e.getMessage(), e);
        }
    }

    public File convertMultiPartFile(final MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            logger.error("Error converting multipart file to file: ", e);
        }
        return file;
    }
}
