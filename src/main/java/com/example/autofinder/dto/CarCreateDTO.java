package com.example.autofinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarCreateDTO {
    private String carType;     // 차량 종류 (국산차/수입차)
    private String model;       // 차량 모델명 (필수)
    private String year;        // 연식 (필수)
    private Long mileage;       // 주행거리
    private Long price;         // 가격 (필수)
    private String fuel;        // 연료 종류 (필수)
    private String region;      // 지역 (필수)

    // 추가 필드
    private String description;             // 차량 상세 설명
    private String carNumber;               // 차량 번호
    private LocalDate registrationDate;     // 등록일
    private String carClass;                // 차종 (경차, 소형, 준중형 등)
    private String color;                   // 색상
    private String transmission;            // 변속기 (자동, 수동 등)

    // 이미지 업로드 관련 필드
    private List<String> imageUrls = new ArrayList<>(); // 업로드된 이미지 URL 목록
    private Integer mainImageIndex;                    // 대표 이미지 인덱스 (0부터 시작)
}