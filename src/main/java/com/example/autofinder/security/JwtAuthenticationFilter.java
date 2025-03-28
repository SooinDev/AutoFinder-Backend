package com.example.autofinder.security;

import com.example.autofinder.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component // Spring이 관리하는 Bean으로 등록
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil; // JWT 유틸리티 클래스

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 요청 헤더에서 "Authorization" 값 가져오기
        String token = request.getHeader("Authorization");

        // 토큰이 존재하고 "Bearer "으로 시작하는지 확인
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거 후 실제 JWT 토큰 값 추출
            String username = jwtUtil.validateToken(token); // JWT 검증 후 사용자 이름 가져오기
            String role = jwtUtil.extractRole(token); // JWT에서 역할(role) 정보 추출

            // 디버깅용 로그
            System.out.println("JWT 필터 - 사용자: " + username + ", 역할: " + role);

            // 토큰이 유효하면 SecurityContext에 인증 정보 저장
            if (username != null && role != null) {
                // Spring Security는 "ROLE_"을 자동으로 추가하므로, ROLE_ 붙여서 저장
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // Spring Security의 UserDetails 객체 생성 (비밀번호는 필요 없으므로 빈 값 전달)
                UserDetails userDetails = new User(username, "", authorities);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                // SecurityContextHolder에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }
}