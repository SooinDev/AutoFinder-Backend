package com.example.autofinder.service;

import com.example.autofinder.dto.CarDTO;
import com.example.autofinder.model.Car;
import com.example.autofinder.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {
    private final CarRepository carRepository;

    // 차량 추가 (CREATE) - DTO 버전
    @Transactional
    public Car addCar(CarDTO carDTO) {
        log.info("차량 추가 서비스 호출: {}", carDTO);

        // DTO → Entity 변환
        Car car = convertDtoToCar(carDTO);

        log.info("DB 저장 전 차량 정보: {}", car);

        // 이미지 갤러리 확인
        if (car.getImageGallery() != null) {
            log.info("이미지 갤러리 크기: {}", car.getImageGallery().size());
            for (int i = 0; i < car.getImageGallery().size(); i++) {
                log.info("이미지 [{}]: {}", i, car.getImageGallery().get(i));
            }
        }

        // 저장 및 반환
        Car savedCar = carRepository.save(car);
        log.info("차량 저장 완료, ID: {}", savedCar.getId());

        return savedCar;
    }

    // 특정 ID로 차량 조회 (READ)
    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    // 차량 목록 조회 (페이징 포함)
    public Page<Car> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable);
    }

    // 차량 목록 검색 (페이징 및 필터링 포함)
    public Page<Car> searchCars(String model, Integer minPrice, Integer maxPrice,
                                Integer minMileage, Integer maxMileage, String fuel,
                                String region, String year, Pageable pageable) {
        try {
            if (fuel != null) {
                fuel = URLDecoder.decode(fuel, StandardCharsets.UTF_8);
            }
            if (model != null) {
                model = URLDecoder.decode(model, StandardCharsets.UTF_8);
            }
            if (region != null) {
                region = URLDecoder.decode(region, StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 URL 인코딩 요청", e);
        }

        return carRepository.findByFilters(model, minPrice, maxPrice, minMileage, maxMileage, fuel, region, year, pageable);
    }

    // 차량 정보 수정 (UPDATE) - DTO 버전
    @Transactional
    public Car updateCar(Long id, CarDTO carDTO) {
        log.info("차량 업데이트 서비스 호출 - ID: {}, DTO: {}", id, carDTO);

        return carRepository.findById(id).map(car -> {
            // 기본 필드 업데이트
            car.setCarType(carDTO.getCarType());
            car.setModel(carDTO.getModel());
            car.setYear(carDTO.getYear());
            car.setMileage(carDTO.getMileage());
            car.setPrice(carDTO.getPrice());
            car.setFuel(carDTO.getFuel());
            car.setRegion(carDTO.getRegion());

            // URL 설정 (null 방지)
            car.setUrl(carDTO.getUrl() != null ? carDTO.getUrl() : "");

            // 추가 필드 업데이트
            car.setDescription(carDTO.getDescription());
            car.setCarNumber(carDTO.getCarNumber());
            car.setRegistrationDate(carDTO.getRegistrationDate());
            car.setCarClass(carDTO.getCarClass());
            car.setColor(carDTO.getColor());
            car.setTransmission(carDTO.getTransmission());

            // 이미지 갤러리 업데이트
            updateCarImages(car, carDTO);

            log.info("차량 업데이트 전 정보: {}", car);
            Car updatedCar = carRepository.save(car);
            log.info("차량 업데이트 완료, ID: {}", updatedCar.getId());

            return updatedCar;
        }).orElseThrow(() -> new IllegalArgumentException("해당 ID의 차량을 찾을 수 없습니다."));
    }

    // 차량 삭제 (DELETE)
    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 차량을 찾을 수 없습니다.");
        }
        carRepository.deleteById(id);
    }

    // DTO -> 엔티티 변환 메소드
    private Car convertDtoToCar(CarDTO dto) {
        Car car = new Car();
        car.setCarType(dto.getCarType() != null ? dto.getCarType() : "국산차");
        car.setModel(dto.getModel());
        car.setYear(dto.getYear());
        car.setMileage(dto.getMileage());
        car.setPrice(dto.getPrice());
        car.setFuel(dto.getFuel());
        car.setRegion(dto.getRegion());

        // URL 필드 설정 (null 방지)
        car.setUrl(dto.getUrl() != null ? dto.getUrl() : "");

        // 추가 필드 설정
        car.setDescription(dto.getDescription());
        car.setCarNumber(dto.getCarNumber());
        car.setRegistrationDate(dto.getRegistrationDate());
        car.setCarClass(dto.getCarClass());
        car.setColor(dto.getColor());
        car.setTransmission(dto.getTransmission());

        // 이미지 갤러리 설정
        updateCarImages(car, dto);

        return car;
    }

    // 차량 이미지 업데이트 메소드
    private void updateCarImages(Car car, CarDTO dto) {
        log.info("이미지 업데이트 시작 - DTO 정보: imageGallery={}, imageUrls={}, imageUrl={}",
                dto.getImageGallery() != null ? dto.getImageGallery().size() : 0,
                dto.getImageUrls() != null ? dto.getImageUrls().size() : 0,
                dto.getImageUrl());

        // 이미지 갤러리 처리 - 프론트엔드 형식(imageGallery) 먼저 확인
        if (dto.getImageGallery() != null && !dto.getImageGallery().isEmpty()) {
            // 기존 이미지 갤러리 정보 초기화
            car.getImageGallery().clear();
            car.getImageGallery().addAll(dto.getImageGallery());
            log.info("imageGallery에서 이미지 {}개 추가됨", dto.getImageGallery().size());
        }
        // 백엔드 형식(imageUrls)으로 전달된 경우
        else if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            // 기존 이미지 갤러리 정보 초기화
            car.getImageGallery().clear();
            car.getImageGallery().addAll(dto.getImageUrls());
            log.info("imageUrls에서 이미지 {}개 추가됨", dto.getImageUrls().size());
        }
        // 단일 이미지 URL만 있는 경우 (이전 버전 호환성)
        else if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty()) {
            car.getImageGallery().clear();
            car.getImageGallery().add(dto.getImageUrl());
            log.info("단일 imageUrl 추가됨: {}", dto.getImageUrl());
        }

        // 메인 이미지 인덱스 설정
        car.setMainImageIndex(dto.getMainImageIndex() != null ? dto.getMainImageIndex() : 0);

        // 메인 이미지 URL을 imageUrl 필드에도 설정 (이전 버전 호환성)
        if (!car.getImageGallery().isEmpty() && car.getMainImageIndex() < car.getImageGallery().size()) {
            car.setImageUrl(car.getImageGallery().get(car.getMainImageIndex()));
            log.info("대표 이미지 설정 (인덱스 {}): {}", car.getMainImageIndex(), car.getImageUrl());
        } else if (!car.getImageGallery().isEmpty()) {
            car.setImageUrl(car.getImageGallery().get(0));
            log.info("첫 번째 이미지를 대표 이미지로 설정: {}", car.getImageUrl());
        }

        // imageUrl이 null인 경우 처리
        if (car.getImageUrl() == null && !car.getImageGallery().isEmpty()) {
            car.setImageUrl(car.getImageGallery().get(0));
            log.info("imageUrl이 null이어서 첫 번째 이미지로 설정: {}", car.getImageUrl());
        } else if (car.getImageUrl() == null) {
            car.setImageUrl(""); // 기본 빈 값 설정
            log.info("이미지가 없어 imageUrl을 빈 문자열로 설정");
        }

        log.info("이미지 업데이트 완료 - 갤러리 크기: {}, 대표 이미지: {}",
                car.getImageGallery().size(), car.getImageUrl());
    }
}