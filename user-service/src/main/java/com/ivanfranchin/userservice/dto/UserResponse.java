package com.ivanfranchin.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import java.time.LocalDate;

public record UserResponse(Long id, String username, String email,
                           @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {
}