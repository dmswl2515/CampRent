package com.rental.camprent.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * JWT 토큰 생성 및 검증 유틸리티
 * */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationTime;

    /**
     * 생성자 : 환경별 설정 파일에서 JWT 설정 주입
     * */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    /**
     * JWT 토큰 생성
     *
     * @param username  사용자 아아디
     * @param role      사용자 권한 (ADMIN, USER)
     * @return          JWT 토큰 문자열
     * */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰에서 username 추출
     *
     * @param token JWT 토큰
     * @return username
     * */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 role 추출
     *
     * @param token JWT 토큰
     * @return role (ADMIN, USER)
     * */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 만료/변조되면 false
     * */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 토큰 파싱 (내부 메서드)
     *
     * @param token JWT 토큰
     * @return Claims (토큰 페이로드)
     * */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
