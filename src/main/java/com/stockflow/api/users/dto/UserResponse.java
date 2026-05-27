package com.stockflow.api.users.dto;

import com.stockflow.api.users.User;

public record UserResponse(Long id, String email, String fullName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName());
    }
}

