package com.example.autofinder.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 이미지 업로드 API - 다중 파일 지원
     * @param files 업로드할 이미지 파일 목록
     * @return 업로드된 이미지 URL 목록
     */
    @PostMapping("/images")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> fileUrls = new ArrayList<>();

            // 업로드 디렉토리 생성 (없는 경우)
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 각 파일 저장 및 URL 생성
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                System.out.println("업로드된 파일: " + file.getOriginalFilename() + ", 크기: " + file.getSize() + " bytes");

                // 파일 이름 생성 (중복 방지를 위한 UUID 추가)
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                String fileExtension = getFileExtension(originalFilename);
                String fileName = UUID.randomUUID().toString() + "." + fileExtension;

                // 파일 저장 경로
                Path targetLocation = Paths.get(uploadDir).resolve(fileName);

                // 파일 저장
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // URL 생성
                String fileUrl = baseUrl + "/uploads/images/" + fileName;
                fileUrls.add(fileUrl);

                System.out.println("파일 저장 완료: " + targetLocation + ", URL: " + fileUrl);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("urls", fileUrls);

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            System.err.println("파일 업로드 오류: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            return "png"; // 기본 확장자
        }
    }
}