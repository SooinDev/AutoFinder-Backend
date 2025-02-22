package com.example.autofinder.util;

import io.jsonwebtoken.*;  // JWT 관련 라이브러리
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    // JWT 서명(Signing)용 비밀 키 (32바이트 이상이어야 함)
    private static final String SECRET_KEY = "37g4cnd3hdKHD/0eNbBMhdSUnQQpzqVx0YGTQ++6znQ=\n";

    // JWT 유효 기간 (밀리초 단위) → 24시간 설정
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // 비밀 키를 이용해 HMAC SHA 알고리즘을 위한 Key 객체 생성
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * JWT 토큰 생성 메서드
     * 사용자의 `username`과 `role`을 포함하여 JWT를 생성
     * @param username 사용자 아이디
     * @param role 사용자 역할 (USER or ADMIN)
     * @return JWT 토큰 (String)
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) // 사용자 아이디 저장 (토큰의 Subject)
                .claim("role", role) // 역할(role) 추가 (ROLE_USER 또는 ROLE_ADMIN)
                .setIssuedAt(new Date()) // 토큰 생성 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256) // HMAC SHA256 알고리즘으로 서명
                .compact(); // 최종적으로 JWT 문자열 반환
    }

    /**
     * JWT 검증 후 사용자 아이디 반환
     * @param token 클라이언트가 보낸 JWT
     * @return 사용자 아이디 (정상 토큰인 경우), 유효하지 않으면 `null`
     */
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // 서명 검증
                    .build()
                    .parseClaimsJws(token) // 토큰을 파싱하여 내용 추출
                    .getBody();
            return claims.getSubject(); // 토큰에서 사용자 아이디 반환
        } catch (JwtException e) { // 유효하지 않은 토큰일 경우 예외 처리
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
                    .setSigningKey(key) // 서명 검증
                    .build()
                    .parseClaimsJws(token) // 토큰을 파싱하여 내용 추출
                    .getBody();
            return claims.get("role", String.class); // 역할(role) 값 반환
        } catch (JwtException e) { // 유효하지 않은 토큰일 경우 예외 처리
            return null;
        }
    }
}