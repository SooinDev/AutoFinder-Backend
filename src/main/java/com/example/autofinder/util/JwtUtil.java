package com.example.autofinder.util;

import com.example.autofinder.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    // Base64 인코딩된 시크릿 키 (환경변수로 저장 권장)
    private static final String SECRET_KEY = "37g4cnd3hdKHD/0eNbBMhdSUnQQpzqVx0YGTQ++6znQ=";

    // 로그인 토큰 만료 시간 조정
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일 (자동 로그인)
    private static final long SHORT_EXPIRATION_TIME = 1000 * 60 * 60; // 1시간 (일반 로그인)

    // Base64 디코딩하여 키 변환
    private final Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

    /**
     * `Authentication` 객체를 받아서 JWT 생성 (만료 시간 선택 가능)
     * @param authentication Spring Security 인증 객체
     * @param rememberMe 로그인 유지 여부 (true → 7일, false → 1시간)
     * @return JWT 토큰
     */
    public String generateToken(Authentication authentication, boolean rememberMe) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        Long userId = userDetails.getId(); // 사용자 ID 추가

        long expirationTime = rememberMe ? EXPIRATION_TIME : SHORT_EXPIRATION_TIME;

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)  // 사용자 ID 추가
                .claim("role", userDetails.getAuthorities().toString()) // 역할 추가
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256) // 서명
                .compact();
    }

    /**
     * JWT 검증 후 사용자 아이디 반환
     * @param token 클라이언트가 보낸 JWT
     * @return 사용자 아이디 (유효한 토큰인 경우), 유효하지 않으면 `null`
     */
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject(); // 사용자 아이디 반환
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다.");
            return null;
        } catch (JwtException e) {
            System.out.println("토큰이 유효하지 않습니다.");
            return null;
        }
    }

    /**
     * JWT에서 사용자 ID 추출
     * @param token 클라이언트가 보낸 JWT
     * @return 사용자 ID (유효한 토큰인 경우), 유효하지 않으면 `null`
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다.");
            return null;
        } catch (JwtException e) {
            System.out.println("토큰이 유효하지 않습니다.");
            return null;
        }
    }

    /**
     * JWT에서 역할(Role) 정보 추출
     * @param token 클라이언트가 보낸 JWT
     * @return 사용자의 역할 (USER or ADMIN), 유효하지 않으면 `null`
     */
    public String extractRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다.");
            return null;
        } catch (JwtException e) {
            System.out.println("토큰이 유효하지 않습니다.");
            return null;
        }
    }
}