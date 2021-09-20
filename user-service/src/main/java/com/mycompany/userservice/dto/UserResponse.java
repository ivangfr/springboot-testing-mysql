package com.mycompany.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Value;

import java.time.LocalDate;

@Value
public class UserResponse {

    Long id;
    String username;
    String email;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate birthday;
}