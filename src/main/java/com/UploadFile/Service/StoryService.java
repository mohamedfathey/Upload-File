package com.UploadFile.Service;

import com.UploadFile.Entity.Story;
import com.UploadFile.Error.FileStorageException;
import com.UploadFile.Repo.StoryRepo;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class StoryService {

    @Autowired
    private StoryRepo storyRepo;

    Logger logger = LoggerFactory.getLogger(StoryService.class);

    private Path fileStorageLocation;

    @Value("${story.upload.base.path}")
    private String basePath;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public String uploadStory(File file, Long id, String pathType) {
        executor.submit(() -> {
            try {
                this.fileStorageLocation = Paths.get(basePath, pathType.replace("\\", "/")).toAbsolutePath().normalize();
                Files.createDirectories(this.fileStorageLocation);

                String fileName = StringUtils.cleanPath(id + "-" + file.getName());

                if (fileName.contains("..")) {
                    throw new FileStorageException("Invalid path sequence in file name: " + fileName);
                }

                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                try (InputStream targetStream = new FileInputStream(file)) {
                    Files.copy(targetStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }

                Story story = storyRepo.findById(id).orElseThrow(() -> new FileStorageException("Story Not Found With id: " + id));
                story.setStoryUrl(targetLocation.toString());
                storyRepo.save(story);

                logger.info("Story saved successfully.");

                executor.submit(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(60);
                        storyRepo.deleteById(id);
                        logger.info("Story with id " + id + " deleted after 1 minute.");
                    } catch (InterruptedException e) {
                        logger.error("Error during deletion delay: ", e);
                    }
                });

            } catch (IOException e) {
                logger.error("Error in uploading story: ", e);
                throw new FileStorageException("Could not store file " + e.getMessage(), e);
            }
        });

        return "Story is being uploaded and will be deleted after 1 minute...";
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
