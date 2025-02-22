package com.example.autofinder.controller;

import com.example.autofinder.model.User;
import com.example.autofinder.repository.UserRepository;
import com.example.autofinder.service.UserService;
import com.example.autofinder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            User newUser = userService.registerUser(username, password); // role 제거
            return ResponseEntity.ok(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("로그아웃되었습니다. 클라이언트 측에서 토큰을 삭제하세요.");
    }

    // 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        // DB에서 사용자 정보 조회
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("사용자 정보를 찾을 수 없습니다.");
        }

        // 사용자 정보 반환
        return ResponseEntity.ok(user);
    }

    // 이메일(아이디) 중복 확인 API
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        return ResponseEntity.ok(exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.");
    }
}