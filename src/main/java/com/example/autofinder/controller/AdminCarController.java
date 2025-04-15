package com.example.autofinder.controller;

import com.example.autofinder.dto.CarDTO;
import com.example.autofinder.model.Car;
import com.example.autofinder.service.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 관리자용 차량 관리 API
 * 이미지 업로드와 차량 정보 저장을 한번에 처리할 수 있는 편의 기능 제공
 */
@RestController
@RequestMapping("/api/admin/cars")
@RequiredArgsConstructor
@Slf4j
public class AdminCarController {

    private final CarService carService;
    private final FileUploadController fileUploadController;

    /**
     * 차량 추가 (이미지 업로드 포함)
     * 이미지 파일과 차량 정보를 함께 받아서 처리
     */
    @PostMapping
    public ResponseEntity<?> addCarWithImages(
            @RequestPart(value = "carData", required = true) String carDataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {

        try {
            log.info("차량 추가 요청 (이미지 포함) - 차량 정보 JSON: {}", carDataJson);

            // JSON 문자열을 CarDTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // LocalDate 처리를 위한 모듈 등록
            CarDTO carDTO = objectMapper.readValue(carDataJson, CarDTO.class);

            log.info("변환된 차량 DTO: {}", carDTO);

            // 1. 이미지 파일 업로드 (있는 경우)
            if (files != null && files.length > 0) {
                log.info("이미지 파일 {} 개 업로드 시작", files.length);

                // 이미지 업로드 API 호출
                ResponseEntity<?> uploadResponse = fileUploadController.uploadImages(files);

                if (uploadResponse.getStatusCode().is2xxSuccessful() && uploadResponse.getBody() instanceof Map) {
                    // 업로드된 이미지 URL 추출
                    @SuppressWarnings("unchecked")
                    List<String> imageUrls = (List<String>) ((Map<String, Object>) uploadResponse.getBody()).get("urls");

                    log.info("이미지 업로드 성공: {} 개 URL 반환됨", imageUrls.size());

                    // DTO에 이미지 URL 설정
                    carDTO.setImageUrls(imageUrls);

                    // 이미지 갤러리로도 설정 (프론트엔드 호환성)
                    carDTO.setImageGallery(imageUrls);

                    // 대표 이미지 설정 (인덱스가 지정되지 않은 경우 첫 번째 이미지 사용)
                    if (carDTO.getMainImageIndex() == null || carDTO.getMainImageIndex() >= imageUrls.size()) {
                        carDTO.setMainImageIndex(0);
                    }

                    // 대표 이미지 URL 설정
                    carDTO.setImageUrl(imageUrls.get(carDTO.getMainImageIndex()));
                } else {
                    log.error("이미지 업로드 실패: {}", uploadResponse.getBody());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("이미지 업로드 중 오류가 발생했습니다");
                }
            } else {
                log.info("이미지 파일 없음");
            }

            // 2. 차량 정보 저장
            Car savedCar = carService.addCar(carDTO);
            log.info("차량 저장 완료, ID: {}", savedCar.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
        } catch (Exception e) {
            log.error("차량 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("차량 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 차량 정보 수정 (이미지 업로드 포함)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCarWithImages(
            @PathVariable Long id,
            @RequestPart(value = "carData", required = true) String carDataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {

        try {
            log.info("차량 수정 요청 (이미지 포함) - ID: {}, 차량 정보 JSON: {}", id, carDataJson);

            // JSON 문자열을 CarDTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // LocalDate 처리를 위한 모듈 등록
            CarDTO carDTO = objectMapper.readValue(carDataJson, CarDTO.class);

            log.info("변환된 차량 DTO: {}", carDTO);

            // 1. 이미지 파일 업로드 (있는 경우)
            if (files != null && files.length > 0) {
                log.info("이미지 파일 {} 개 업로드 시작", files.length);

                // 이미지 업로드 API 호출
                ResponseEntity<?> uploadResponse = fileUploadController.uploadImages(files);

                if (uploadResponse.getStatusCode().is2xxSuccessful() && uploadResponse.getBody() instanceof Map) {
                    // 업로드된 이미지 URL 추출
                    @SuppressWarnings("unchecked")
                    List<String> imageUrls = (List<String>) ((Map<String, Object>) uploadResponse.getBody()).get("urls");

                    log.info("이미지 업로드 성공: {} 개 URL 반환됨", imageUrls.size());

                    // 기존 이미지 URL이 있으면 유지
                    if (carDTO.getImageUrls() != null && !carDTO.getImageUrls().isEmpty()) {
                        carDTO.getImageUrls().addAll(imageUrls);
                    } else {
                        carDTO.setImageUrls(imageUrls);
                    }

                    // 이미지 갤러리로도 설정 (프론트엔드 호환성)
                    if (carDTO.getImageGallery() != null && !carDTO.getImageGallery().isEmpty()) {
                        carDTO.getImageGallery().addAll(imageUrls);
                    } else {
                        carDTO.setImageGallery(imageUrls);
                    }

                    // 대표 이미지 설정 (인덱스가 유효한지 확인)
                    if (carDTO.getMainImageIndex() == null ||
                            carDTO.getMainImageIndex() >= carDTO.getImageUrls().size()) {
                        carDTO.setMainImageIndex(0);
                    }

                    // 대표 이미지 URL 설정
                    carDTO.setImageUrl(carDTO.getImageUrls().get(carDTO.getMainImageIndex()));
                } else {
                    log.error("이미지 업로드 실패: {}", uploadResponse.getBody());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("이미지 업로드 중 오류가 발생했습니다");
                }
            } else {
                log.info("업로드할 새 이미지 파일 없음");
            }

            // 2. 차량 정보 수정
            Car updatedCar = carService.updateCar(id, carDTO);
            log.info("차량 정보 수정 완료, ID: {}", updatedCar.getId());

            return ResponseEntity.ok(updatedCar);
        } catch (Exception e) {
            log.error("차량 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("차량 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}