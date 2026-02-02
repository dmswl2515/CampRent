package com.rental.camprent.dto.response;

import com.rental.camprent.domain.user.User;
import com.rental.camprent.domain.user.UserRole;

public record UserResponse (
        Long id,
        String username,
        String name,
        String email,
        String phone,
        UserRole role
) {
    // Entity -> DTO 변환 (정적 팩토리 메서드)
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}
