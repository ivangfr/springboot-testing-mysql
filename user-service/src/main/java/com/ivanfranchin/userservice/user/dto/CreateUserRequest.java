package com.ivanfranchin.userservice.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ivanfranchin.userservice.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUserRequest(
        @Schema(example = "ivan.franchin") @NotBlank @Size(max = 100) String username,
        @Schema(example = "ivan.franchin@test.com") @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "2001-01-01") @NotNull @Past @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {

    public User toDomain() {
        return new User(username, email, birthday);
    }
}
