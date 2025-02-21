package com.example.autofinder.controller;

import com.example.autofinder.model.User;
import com.example.autofinder.service.UserService;
import com.example.autofinder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String role = request.getOrDefault("role", "USER");  // 기본값: USER

        User newUser = userService.registerUser(username, password, role);
        return ResponseEntity.ok(newUser);
    }

    // 로그인 API (JWT 토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.authenticateUser(username, password);
        if (user != null) {
            // `role`을 문자열로 변환하여 전달
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            return ResponseEntity.ok(token);  // JWT 반환
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 사용자명 또는 비밀번호가 잘못되었습니다.");
        }
    }
}