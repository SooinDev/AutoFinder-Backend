package com.example.autofinder.controller;

import com.example.autofinder.model.Car;
import com.example.autofinder.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{carId}")
    public ResponseEntity<String> addFavorite(@RequestParam Long userId, @PathVariable Long carId) {
        favoriteService.addFavorite(userId, carId);
        return ResponseEntity.ok("Favorite added successfully");
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<String> removeFavorite(@RequestParam Long userId, @PathVariable Long carId) {
        favoriteService.removeFavorite(userId, carId);
        return ResponseEntity.ok("Favorite removed successfully");
    }

    @GetMapping
    public ResponseEntity<List<Car>> getUserFavorites(@RequestParam Long userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
}