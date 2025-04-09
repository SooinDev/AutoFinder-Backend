package com.example.autofinder.service;

import com.example.autofinder.model.Car;
import com.example.autofinder.model.CarImage;
import com.example.autofinder.repository.CarImageRepository;
import com.example.autofinder.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarService {
    private final CarRepository carRepository;
    private final CarImageRepository carImageRepository;

    // 차량 추가 (CREATE)
    public Car addCar(Car car) {
        return carRepository.save(car);
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

    // 차량 정보 수정 (UPDATE)
    public Car updateCar(Long id, Car updatedCar) {
        return carRepository.findById(id).map(car -> {
            car.setCarType(updatedCar.getCarType());
            car.setModel(updatedCar.getModel());
            car.setYear(updatedCar.getYear());
            car.setMileage(updatedCar.getMileage());
            car.setFuel(updatedCar.getFuel());
            car.setRegion(updatedCar.getRegion());
            return carRepository.save(car);
        }).orElseThrow(() -> new IllegalArgumentException("해당 ID의 차량을 찾을 수 없습니다."));
    }

    // 차량 삭제 (DELETE)
    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 차량을 찾을 수 없습니다.");
        }
        carRepository.deleteById(id);
    }

    public Car getCarWithImages(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("차량을 찾을 수 없습니다: " + id));

        // 이미지 로드는 지연 로딩을 사용할 경우 필요
        List<CarImage> images = carImageRepository.findByCarId(id);
        return car;
    }

    public Car saveCar(Car car, List<String> imageUrls, Integer mainImageIndex) {
        // 기존 이미지 삭제 (수정 시)
        if (car.getId() != null) {
            carImageRepository.deleteByCarId(car.getId());
            // 기존 이미지 목록도 초기화
            car.getImages().clear();
        }

        // 새 이미지 저장
        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                car.addImage(imageUrls.get(i), isMain, i);
            }
        }

        // 이미지가 포함된 Car 객체를 한 번에 저장
        return carRepository.save(car);
    }
}
