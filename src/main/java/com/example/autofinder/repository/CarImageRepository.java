package com.example.autofinder.repository;

import com.example.autofinder.model.CarImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    List<CarImage> findByCarId(Long carId);
    Optional<CarImage> findByCarIdAndIsMainTrue(Long carId);

    // 특정 차량의 모든 이미지 삭제
    @Modifying
    @Transactional
    void deleteByCarId(Long carId);
}
