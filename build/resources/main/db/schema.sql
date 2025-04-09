-- 차량 테이블
CREATE TABLE cars (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 차량 ID (자동 증가)
                      car_type VARCHAR(50) NOT NULL,         -- 차량 종류 (국산차/수입차)
                      model VARCHAR(100) NOT NULL,           -- 차량 모델명
                      year VARCHAR(20) NOT NULL,             -- 연식 (예: "22/01식")
                      mileage BIGINT,                        -- 주행거리 (없으면 NULL 저장)
                      price BIGINT UNSIGNED NOT NULL DEFAULT 0,  -- 차량 가격 (기본값 0)
                      fuel VARCHAR(20) NOT NULL,             -- 연료 종류 (예: 가솔린, 디젤)
                      region VARCHAR(50) NOT NULL,           -- 지역 (예: 서울, 경기 등)
                      description TEXT,                      -- 차량 상세 설명
                      car_number VARCHAR(20),                -- 차량 번호
                      registration_date DATE,                -- 등록일
                      car_class VARCHAR(50),                 -- 차종 (경차, 소형, 준중형 등)
                      color VARCHAR(50),                     -- 색상
                      transmission VARCHAR(30),              -- 변속기 (자동, 수동 등)
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 데이터 삽입 시간 기록
);

-- 차량 이미지 테이블
CREATE TABLE car_images (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            car_id BIGINT NOT NULL,
                            image_url VARCHAR(255) NOT NULL,
                            is_main BOOLEAN DEFAULT FALSE,         -- 대표 이미지 여부
                            display_order INT DEFAULT 0,           -- 이미지 표시 순서
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- 사용자 테이블
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- 즐겨찾기 테이블
CREATE TABLE favorites (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           car_id BIGINT NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
                           UNIQUE KEY unique_user_car (user_id, car_id)  -- 사용자별 차량 즐겨찾기 중복 방지
);

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_cars_model ON cars(model);
CREATE INDEX idx_cars_price ON cars(price);
CREATE INDEX idx_cars_year ON cars(year);
CREATE INDEX idx_cars_fuel ON cars(fuel);
CREATE INDEX idx_cars_region ON cars(region);
CREATE INDEX idx_cars_created_at ON cars(created_at);
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_car_id ON favorites(car_id);
CREATE INDEX idx_car_images_car_id ON car_images(car_id);
CREATE INDEX idx_car_images_is_main ON car_images(is_main);