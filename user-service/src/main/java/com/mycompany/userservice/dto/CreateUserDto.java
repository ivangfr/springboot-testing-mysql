package com.mycompany.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    @ApiModelProperty(example = "ivan.franchin")
    @NotBlank
    private String username;

    @ApiModelProperty(position = 2, example = "ivan.franchin@test.com")
    @NotBlank
    @Email
    private String email;

    @ApiModelProperty(position = 3, example = "01-01-2001")
    @Past
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthday;

}
