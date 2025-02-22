package com.example.autofinder.repository;

import com.example.autofinder.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
//    // 차량명 검색
//    Page<Car> findByModelContaining(String model, Pageable pageable);
//
//    // 연료 타입 필터링
//    Page<Car> findByFuel(String fuel, Pageable pageable);
//
//    // 지역 필터링
//    Page<Car> findByRegion(String region, Pageable pageable);
//
//    // 가격 범위 검색 (min ≤ price ≤ max)
//    Page<Car> findByPriceBetween(Long minPrice, Long maxPrice, Pageable pageable);

    @Query("SELECT c FROM Car c WHERE " +
            "(:model IS NULL OR c.model LIKE %:model%) AND " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
            "(:minMileage IS NULL OR c.mileage >= :minMileage) AND " +
            "(:maxMileage IS NULL OR c.mileage <= :maxMileage) AND " +
            "(:fuel IS NULL OR LOWER(c.fuel) = LOWER(:fuel)) AND " +
            "(:region IS NULL OR LOWER(c.region) = LOWER(:region)) AND " +
            "(:year IS NULL OR CAST(c.year AS string) LIKE %:year%)")
    Page<Car> findByFilters(
            @Param("model") String model,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minMileage") Integer minMileage,
            @Param("maxMileage") Integer maxMileage,
            @Param("fuel") String fuel,
            @Param("region") String region,
            @Param("year") String year,
            Pageable pageable
    );
}
