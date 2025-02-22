package com.example.autofinder.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "37g4cnd3hdKHD/0eNbBMhdSUnQQpzqVx0YGTQ++6znQ=\n"; // 32바이트 이상 필요
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // JWT 생성 (역할 포함)
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // "ROLE_" 없이 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 검증 후 사용자 이름 반환
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject(); // 사용자 이름 반환
        } catch (JwtException e) {
            return null;
        }
    }

    // JWT에서 역할(Role) 추출
    public String extractRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class); // 역할 값 반환
        } catch (JwtException e) {
            return null;
        }
    }
}