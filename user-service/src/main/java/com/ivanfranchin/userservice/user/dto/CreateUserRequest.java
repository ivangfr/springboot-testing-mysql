package com.ivanfranchin.userservice.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CreateUserRequest(
        @Schema(example = "ivan.franchin") @NotBlank String username,
        @Schema(example = "ivan.franchin@test.com") @NotBlank @Email String email,
        @Schema(example = "2001-01-01") @Past @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {
}
