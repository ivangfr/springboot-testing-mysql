package com.mycompany.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {

    @ApiModelProperty(example = "ivan2.franchin")
    private String username;

    @ApiModelProperty(position = 1, example = "ivan2.franchin@test.com")
    @Email
    private String email;

    @ApiModelProperty(position = 2, example = "2002-02-02")
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

}
