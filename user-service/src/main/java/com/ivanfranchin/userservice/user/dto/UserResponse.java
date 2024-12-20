package com.ivanfranchin.userservice.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.ivanfranchin.userservice.user.model.User;

import java.time.LocalDate;

public record UserResponse(Long id, String username, String email,
                           @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthday()
        );
    }
}