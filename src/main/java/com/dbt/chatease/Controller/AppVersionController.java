package com.dbt.chatease.Controller;

import com.dbt.chatease.DTO.AppVersionDTO;
import com.dbt.chatease.Entity.AppVersion;
import com.dbt.chatease.Repository.AppVersionRepository;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/app-version")
@Tag(name = "App Version Controller", description = "APIs for App Version Management")
@RequiredArgsConstructor
@Slf4j
public class AppVersionController {

    private final AppVersionRepository appVersionRepository;
    
    //Directory to store app packages
    private static final String APP_UPLOAD_DIR = "C:\\imgStore\\app_packages\\";

    /**
     * Publish a new version
     */
    @PostMapping(value = "/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publish New Version", description = "Upload installer and save version info")
    public Result publishVersion(AppVersionDTO dto) {
        //Validation
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
            return Result.fail("Please upload the installer file");
        }
        if (dto.getVersionNumber() == null) {
            return Result.fail("Version number is required");
        }

        try {
            //Handle File Upload
            MultipartFile file = dto.getFile();
            String originalFilename = file.getOriginalFilename();
            
            //Get extension (.exe, .dmg, .zip)
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            //Generate filename: App_v1.0.0_timestamp.exe
            String newFileName = "App_v" + dto.getVersionNumber() + "_" + System.currentTimeMillis() + suffix;
            
            File destFile = new File(APP_UPLOAD_DIR + newFileName);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);

            //Construct Download URL
            String downloadUrl = "/files/app_packages/" + newFileName;

            //Save to Database
            AppVersion appVersion = new AppVersion();
            appVersion.setVersionNumber(dto.getVersionNumber());
            appVersion.setUpdateContent(dto.getUpdateContent());
            appVersion.setDownloadUrl(downloadUrl);
            appVersion.setFileSize(file.getSize());
            appVersion.setStatus(1); // Published
            appVersion.setCreateTime(LocalDateTime.now());

            appVersionRepository.save(appVersion);

            return Result.ok("Version published successfully");

        } catch (IOException e) {
            log.error("Failed to upload app package", e);
            return Result.fail("File upload failed: " + e.getMessage());
        }
    }
    
    /**
     * Get version history list
     */
    @GetMapping("/list")
    @Operation(summary = "Get Version List", description = "Get paginated version history")
    public Result getList(@RequestParam(defaultValue = "1") Integer page, 
                          @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return Result.ok(appVersionRepository.findAll(pageable));
    }


}