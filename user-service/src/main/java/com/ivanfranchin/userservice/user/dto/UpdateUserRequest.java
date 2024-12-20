package com.ivanfranchin.userservice.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Schema(example = "ivan2.franchin") String username,
        @Schema(example = "ivan2.franchin@test.com") @Email String email,
        @Schema(example = "2002-02-02") @Past @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {
}
