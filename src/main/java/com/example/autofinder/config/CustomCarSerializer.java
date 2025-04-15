package com.example.autofinder.config;

import com.example.autofinder.model.Car;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class CustomCarSerializer extends JsonSerializer<Car> {

    @Override
    public void serialize(Car car, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        // ID 및 기본 필드
        gen.writeNumberField("id", car.getId());
        gen.writeStringField("carType", car.getCarType() != null ? car.getCarType() : "");
        gen.writeStringField("model", car.getModel() != null ? car.getModel() : "");
        gen.writeStringField("year", car.getYear() != null ? car.getYear() : "");

        // 숫자 필드 - null 처리
        if (car.getMileage() != null) {
            gen.writeNumberField("mileage", car.getMileage());
        } else {
            gen.writeNullField("mileage");
        }

        if (car.getPrice() != null) {
            gen.writeNumberField("price", car.getPrice());
        } else {
            gen.writeNullField("price");
        }

        gen.writeStringField("fuel", car.getFuel() != null ? car.getFuel() : "");
        gen.writeStringField("region", car.getRegion() != null ? car.getRegion() : "");
        gen.writeStringField("url", car.getUrl() != null ? car.getUrl() : "");
        gen.writeStringField("imageUrl", car.getImageUrl() != null ? car.getImageUrl() : "");

        // 추가 필드
        gen.writeStringField("description", car.getDescription() != null ? car.getDescription() : "");
        gen.writeStringField("carNumber", car.getCarNumber() != null ? car.getCarNumber() : "");

        // 날짜 필드
        if (car.getRegistrationDate() != null) {
            gen.writeStringField("registrationDate", car.getRegistrationDate().toString());
        } else {
            gen.writeNullField("registrationDate");
        }

        gen.writeStringField("carClass", car.getCarClass() != null ? car.getCarClass() : "");
        gen.writeStringField("color", car.getColor() != null ? car.getColor() : "");
        gen.writeStringField("transmission", car.getTransmission() != null ? car.getTransmission() : "");

        // 이미지 갤러리 관련
        gen.writeNumberField("mainImageIndex", car.getMainImageIndex() != null ? car.getMainImageIndex() : 0);

        // imageGallery 필드로 리스트 출력 (프론트엔드 호환성)
        gen.writeFieldName("imageGallery");
        gen.writeStartArray();
        if (car.getImageGallery() != null && !car.getImageGallery().isEmpty()) {
            for (String imageUrl : car.getImageGallery()) {
                gen.writeString(imageUrl);
            }
        }
        gen.writeEndArray();

        // 생성 시간
        if (car.getCreatedAt() != null) {
            gen.writeStringField("createdAt", car.getCreatedAt().toString());
        } else {
            gen.writeNullField("createdAt");
        }

        gen.writeEndObject();
    }
}