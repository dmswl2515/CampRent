package com.rental.camprent.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // IP별 버킷 저장소 (signup / login 각각 분리)
    private final Map<String, Bucket> signupBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    // signup: IP당 5분에 최대 3번
    private Bucket createSignupBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillIntervally(3, Duration.ofMinutes(5))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // login: IP당 1분에 최대 10번
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        Bucket bucket = null;

        if ("api/auth/signup".equals(path)) {
            bucket = signupBuckets.computeIfAbsent(clientIp, k -> createSignupBucket());
        } else if ("api/auth/login".equals(path)) {
            bucket = loginBuckets.computeIfAbsent(clientIp, k -> createLoginBucket());
        }

        // rate limit 대상 엔드포인트, 토큰 소진시 429(속도제한) 반환
        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":429,\"code\":\"TOO_MANY_REQUESTS\"," +
                        "\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 프록시 환경 (nginx 등)을 고려한 실제 IP 추출
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if(xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if(xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

}
