package com.dbt.chatease.Controller;


import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
@Tag(name = "UploadController", description = "APIs for uploading and deleting images")
public class UploadController {
    //Base storage directory (Should ideally be in application.yml)
    private static final String BASE_STORE_DIR = "C:\\imgStore";

    //Sub-directories
    private static final String IMG_DIR = "/chateaseimg";
    private static final String FILE_DIR = "/chateasefile";

    /**
     * Upload Image
     */
    @PostMapping("blog")
    @Operation(summary = "Upload Image", description = "Upload an image file (returns URL)")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        //Reuse handleUpload logic, targeting the image directory
        return handleUpload(image, IMG_DIR);
    }

    /**
     * Upload Generic File
     */
    @PostMapping("/file")
    @Operation(summary = "Upload Generic File", description = "Upload video, audio or other files")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        return handleUpload(file, FILE_DIR);
    }

    /**
     * Delete File
     */
    @GetMapping("/blog/delete")
    @Operation(summary = "Delete File", description = "Delete a previously uploaded file by filename/path")
    public Result deleteFile(@RequestParam("name") String filename) {
        //Remove URL prefix "/files"
        String relativePath = filename.replace("/files", "").replace("/", "\\");

        //Construct path
        File file = new File(BASE_STORE_DIR + relativePath);

        if (file.isDirectory() || !file.exists()) {
            return Result.fail("File does not exist or path is invalid");
        }
        try {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("File deleted successfully: {}", file.getAbsolutePath());
                return Result.ok("File deleted successfully");
            } else {
                return Result.fail("Failed to delete file");
            }
        } catch (Exception e) {
            log.error("Exception during file deletion", e);
            return Result.fail("Deletion error: " + e.getMessage());
        }
    }


    private Result handleUpload(MultipartFile file, String subDir) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            // 1. Generate unique filename
            String fileName = UUID.randomUUID().toString() + suffix;

            // 2. Generate date directory (e.g., /2023/11/28/)
            String datePath = new SimpleDateFormat("/yyyy/MM/dd/").format(new Date());

            //3. Construct full disk path: C:\imgStore\chateaseimg\2023\11\28\ uuid.jpg
            String savePathStr = BASE_STORE_DIR + subDir + datePath;
            File saveDir = new File(savePathStr);

            if (!saveDir.exists()) {
                saveDir.mkdirs(); // Create directories recursively
            }

            //4. Save file
            File destFile = new File(saveDir, fileName);
            file.transferTo(destFile);

            //5. Return Web Access URL (Matches WebConfig mapping)
            //Format: /files/chateaseimg/2023/11/28/uuid.jpg
            String urlPath = "/files" + subDir + datePath + fileName;

            log.info("File upload success: {} -> {}", originalFilename, destFile.getAbsolutePath());
            return Result.ok(urlPath);

        } catch (IOException e) {
            log.error("Upload failed", e);
            throw new RuntimeException("File upload failed", e);
        }
    }
}
