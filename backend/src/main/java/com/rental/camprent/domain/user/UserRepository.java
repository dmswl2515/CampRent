package com.rental.camprent.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // username으로 사용자 찾기(로그인 시 사용)
    Optional<User> findByUsername(String username);

    // email로 사용자 찾기 (중복 체크, 이메일 찾기 등)
    Optional<User> findByEmail(String email);

    // username 중복 체크
    boolean existsByUsername(String username);

    // email 중복 체크
    boolean existsByEmail(String email);



}
