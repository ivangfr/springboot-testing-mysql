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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class UserDtoTests {

    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UserDto userDto = new UserDto(1L, "ivan", "ivan@test", LocalDate.parse("2018-01-01"));

        JsonContent<UserDto> jsonContent = jacksonTester.write(userDto);

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.id")
                .extractingJsonPathNumberValue("@.id").isEqualTo(userDto.getId().intValue());

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
        String content = "{\"id\":1,\"username\":\"ivan\",\"email\":\"ivan@test\",\"birthday\":\"2018-01-01\"}";

        UserDto userDto = jacksonTester.parseObject(content);

        assertThat(userDto.getId()).isEqualTo(1);
        assertThat(userDto.getUsername()).isEqualTo("ivan");
        assertThat(userDto.getEmail()).isEqualTo("ivan@test");
        assertThat(userDto.getBirthday()).isEqualTo(LocalDate.parse("2018-01-01"));
    }
}