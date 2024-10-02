package com.UploadFile.Service;

import ch.qos.logback.core.util.TimeUtil;
import com.UploadFile.Entity.Event;
import com.UploadFile.Error.FileStorageException;
import com.UploadFile.Repo.EventRepo;
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
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class EventService {
    @Autowired
    private EventRepo eventRepo ;
    Logger logger = LoggerFactory.getLogger(EventService.class);
    private Path fileStorageLocation ;

    @Value("${event.upload.base.path}")
    private String basePath ;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public String UploadEvent(File file ,Long id , String pathType){
        executorService.submit(()->{
            try {
                this.fileStorageLocation = Paths.get(basePath,pathType.replace("\\","/")).toAbsolutePath().normalize();
                Files.createDirectories(this.fileStorageLocation);

                String fileName = StringUtils.cleanPath(id + "-" + file.getName());

                if (fileName.contains("..")){
                    throw new FileStorageException("Invalid path sequence in file name: " + fileName);
                }

                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                try (InputStream targetStream = new FileInputStream(file)){
                    Files.copy(targetStream,targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }

                Event event = eventRepo.findById(id).orElseThrow(()->new FileStorageException("Event Not Found With ID : " + id));

                event.setMediaUrl(targetLocation.toString());
                eventRepo.save(event);
                logger.info("Event Save Successfully !");

                executorService.submit(()->{
                    try {
                        TimeUnit.SECONDS.sleep(60);
                        eventRepo.deleteById(id);
                        logger.info("Event With Id: " + id + " Deleted After 1 Minute ");

                    }catch (InterruptedException e) {
                        logger.error("Error during deletion delay: ", e);
                    }
                }) ;
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

