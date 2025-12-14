package com.dbt.chatease.Controller;

import com.dbt.chatease.Entity.AppVersion;
import com.dbt.chatease.Repository.AppVersionRepository;
import com.dbt.chatease.Utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app-version")
@Tag(name = "Client App Version Controller", description = "App Update Check API for Client")
@RequiredArgsConstructor
@Slf4j
public class ClientAppVersionController {

    private final AppVersionRepository appVersionRepository;

    /**
     * Check for latest version
     */
    @GetMapping("/latest")
    @Operation(summary = "Get Latest Version", description = "Get the latest published app version info")
    public Result getLatestVersion() {
        //Find the latest version with status = 1 (Published)
        AppVersion latest = appVersionRepository.findTopByStatusOrderByCreateTimeDesc(1);
        if (latest == null) {
            //No version published yet
            return Result.ok();
        }
        return Result.ok(latest);
    }

}