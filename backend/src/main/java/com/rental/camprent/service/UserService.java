package com.rental.camprent.service;

import com.rental.camprent.domain.user.User;
import com.rental.camprent.domain.user.UserRepository;
import com.rental.camprent.domain.user.UserRole;
import com.rental.camprent.dto.request.LoginRequest;
import com.rental.camprent.dto.request.UserSignupRequest;
import com.rental.camprent.dto.response.LoginResponse;
import com.rental.camprent.dto.response.UserResponse;
import com.rental.camprent.exception.ItemNotFoundException;
import com.rental.camprent.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        // username 중복 체크
        if(userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 아이디 입니다.");
        }

        // email 중복 체크
        if(userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // User 엔티티 생성 (DTO -> Entity)
        User user = User.builder()
                .username(request.username())
                .password(encodedPassword)      // 암호화된 비밀번호
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .role(UserRole.USER)            // 회원가입 시 항상 USER 권한
                .build();

        // DB 저장
        User savedUser = userRepository.save(user);

        // Entity -> DTO 변환하여 반환
        return UserResponse.from(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // username으로 사용자 찾기
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(()-> new ItemNotFoundException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증
        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        // 사용자 정보 DTO 변환
        UserResponse userResponse = UserResponse.from(user);

        // 사용자 정보 반환
        return LoginResponse.of(token, userResponse);
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param username 사용자 아이디 (JWT 토큰에서 추출됨)
     * @return 사용자 정보 DTO
     * */
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ItemNotFoundException("존재하지 않는 사용자입니다."));

        return UserResponse.from(user);
    }
}
