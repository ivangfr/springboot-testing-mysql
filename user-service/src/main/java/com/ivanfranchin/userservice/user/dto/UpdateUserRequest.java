package com.ivanfranchin.userservice.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.ivanfranchin.userservice.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Schema(example = "ivan2.franchin") @NotBlank @Size(max = 100) String username,
        @Schema(example = "ivan2.franchin@test.com") @Email @Size(max = 150) String email,
        @Schema(example = "2002-02-02") @Past @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd") LocalDate birthday) {

    public void applyTo(User user) {
        if (username() != null) {
            user.setUsername(username());
        }
        if (email() != null) {
            user.setEmail(email());
        }
        if (birthday() != null) {
            user.setBirthday(birthday());
        }
    }
}
