package com.mycompany.userservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDate;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUpdateUserDto;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class UpdateUserDtoTests {

    @Autowired
    private JacksonTester<UpdateUserDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan", "ivan@test", "2018-01-01");

        JsonContent<UpdateUserDto> jsonContent = jacksonTester.write(updateUserDto);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.username")
                .extractingJsonPathStringValue("@.username").isEqualTo("ivan");

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.email")
                .extractingJsonPathStringValue("@.email").isEqualTo("ivan@test");

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.birthday")
                .extractingJsonPathStringValue("@.birthday").isEqualTo("2018-01-01");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"username\":\"ivan\",\"email\":\"ivan@test\",\"birthday\":\"2018-01-01\"}";

        UpdateUserDto updateUserDto = jacksonTester.parseObject(content);

        assertThat(updateUserDto.getUsername()).isEqualTo("ivan");
        assertThat(updateUserDto.getEmail()).isEqualTo("ivan@test");
        assertThat(updateUserDto.getBirthday()).isEqualTo(LocalDate.parse("2018-01-01"));
    }

}