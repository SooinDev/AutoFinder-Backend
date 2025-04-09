package com.example.autofinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDetailDTO {
    private Long id;
    private String carType;     // 차량 종류 (국산차/수입차)
    private String model;       // 차량 모델명
    private String year;        // 연식
    private Long mileage;       // 주행거리
    private Long price;         // 가격
    private String fuel;        // 연료 종류
    private String region;      // 지역
    private String imageUrl;    // 대표 이미지 URL

    // 추가 필드
    private String description;             // 차량 상세 설명
    private String carNumber;               // 차량 번호
    private LocalDate registrationDate;     // 등록일
    private String carClass;                // 차종 (경차, 소형, 준중형 등)
    private String color;                   // 색상
    private String transmission;            // 변속기 (자동, 수동 등)
    private LocalDateTime createdAt;        // 생성 시간

    // 이미지 갤러리 추가
    private List<String> imageGallery = new ArrayList<>();
}