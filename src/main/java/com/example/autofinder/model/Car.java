package com.example.autofinder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String carType;     // 차량 종류 (국산차/수입차)
    private String model;       // 차량 모델명
    private String year;        // 연식
    private Long mileage;       // 주행거리
    private Long price;         // 가격
    private String fuel;        // 연료 종류
    private String region;      // 지역

    // 추가된 필드
    @Column(columnDefinition = "TEXT")
    private String description;             // 차량 상세 설명

    private String carNumber;               // 차량 번호

    private LocalDate registrationDate;     // 등록일

    private String carClass;                // 차종 (경차, 소형, 준중형 등)

    private String color;                   // 색상

    private String transmission;            // 변속기 (자동, 수동 등)

    // 데이터 생성 시간 (자동 설정)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // 생성 시 자동으로 현재 시간 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<CarImage> images = new ArrayList<>();

    // 이미지 갤러리 접근 유틸리티 메서드
    public List<String> getImageGallery() {
        return images.stream()
                .sorted(Comparator.comparing(CarImage::getDisplayOrder))
                .map(CarImage::getImageUrl)
                .collect(Collectors.toList());
    }

    // 이미지 추가 메서드
    public void addImage(String imageUrl, boolean isMain, int displayOrder) {
        CarImage image = new CarImage();
        image.setCar(this);
        image.setImageUrl(imageUrl);
        image.setMain(isMain);
        image.setDisplayOrder(displayOrder);
        this.images.add(image);
    }
}