package tn.pedialink.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.pedialink.messaging.dto.ApiResponse;
import tn.pedialink.messaging.dto.FileUploadResponse;
import tn.pedialink.messaging.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading file: {}", file.getOriginalFilename());
        
        try {
            FileUploadResponse response = fileStorageService.storeFile(file);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "File uploaded successfully",
                    response
            ));
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Failed to upload file: " + e.getMessage(),
                    null
            ));
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            return fileStorageService.loadFile(fileName);
        } catch (Exception e) {
            log.error("Error downloading file", e);
            return ResponseEntity.notFound().build();
        }
    }
}
