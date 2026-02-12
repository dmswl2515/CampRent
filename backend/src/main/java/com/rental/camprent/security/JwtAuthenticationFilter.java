package com.rental.camprent.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청을 가로채서 JWT 토큰을 검증하고
 * 인증 정보를 SecurityContext에 저장
 * */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 JWT 토큰 추출
        String authHeader = request.getHeader("Authorization");

        // 2. 해더가 없거나 "Bearer "로 시작하지 않으면 다음 필터로 이동
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 3. "Bearer " 제거하고 실제 토큰만 추출
        String token = authHeader.substring(7);

        // 4. 토큰 유효성 검증
        if(jwtUtil.validateToken(token)) {
            // 5. 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            // 6. Spring Security 권한 형식으로 변환 (ROLE_접두사 필요)
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

            // 7. 인증 객체 생성 (principal, credentials, authorities)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,              // principal (주체 : 사용자 식별자)
                    null,                  // credentials (자격증명 : 비밀전호, JWT에서는 불필요)
                    List.of(authority)     // authorities (권한 : ROLE_USER, ROLE_ADMIN)
            );

            // 8. 요청 세부 정보 설정 (IP, 세션 ID 등)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 9. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 10. 다음 필터로 이동
        filterChain.doFilter(request, response);

    }
}
