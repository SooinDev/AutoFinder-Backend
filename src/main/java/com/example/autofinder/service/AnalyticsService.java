package com.example.autofinder.service;

import com.example.autofinder.model.Car;
import com.example.autofinder.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final CarRepository carRepository;

    /**
     * 특정 모델의 연식별 가격 통계 조회
     * @param model 차량 모델명
     * @return 연식별 최저가, 평균가, 최고가 통계
     */
    public List<Map<String, Object>> getPriceStatsByYear(String model) {
        // 해당 모델의 모든 차량 조회
        List<Car> cars = carRepository.findByModelContaining(model);

        // 연식별로 그룹화하여 통계 계산
        return cars.stream()
                .collect(Collectors.groupingBy(Car::getYear))
                .entrySet().stream()
                .map(entry -> {
                    String year = entry.getKey();
                    List<Car> carsInYear = entry.getValue();

                    // 최저가, 평균가, 최고가 계산
                    long minPrice = carsInYear.stream()
                            .mapToLong(Car::getPrice)
                            .min()
                            .orElse(0);

                    double avgPrice = carsInYear.stream()
                            .mapToLong(Car::getPrice)
                            .average()
                            .orElse(0);

                    long maxPrice = carsInYear.stream()
                            .mapToLong(Car::getPrice)
                            .max()
                            .orElse(0);

                    long count = carsInYear.size();

                    // 결과 맵 생성
                    Map<String, Object> yearStats = Map.of(
                            "year", year,
                            "minPrice", minPrice,
                            "avgPrice", Math.round(avgPrice),
                            "maxPrice", maxPrice,
                            "count", count
                    );

                    return yearStats;
                })
                .sorted((a, b) -> {
                    // 연식 기준으로 정렬 (최신순)
                    String yearA = a.get("year").toString().replaceAll("[^0-9]", "");
                    String yearB = b.get("year").toString().replaceAll("[^0-9]", "");
                    return yearB.compareTo(yearA);
                })
                .collect(Collectors.toList());
    }
}