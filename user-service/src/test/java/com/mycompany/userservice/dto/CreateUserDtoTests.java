package com.mycompany.userservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnCreateUserDto;
import static com.mycompany.userservice.util.MyLocalDateHandler.fromStringToDate;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
public class CreateUserDtoTests {

    @Autowired
    private JacksonTester<CreateUserDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        CreateUserDto createUserDto = getAnCreateUserDto("ivan", "ivan@test", "01-01-2018");

        JsonContent<CreateUserDto> jsonContent = jacksonTester.write(createUserDto);

        assertThat(jsonContent).hasJsonPathStringValue("@.username");
        assertThat(jsonContent).extractingJsonPathStringValue("@.username").isEqualTo("ivan");
        assertThat(jsonContent).hasJsonPathStringValue("@.email");
        assertThat(jsonContent).extractingJsonPathStringValue("@.email").isEqualTo("ivan@test");
        assertThat(jsonContent).hasJsonPathStringValue("@.birthday");
        assertThat(jsonContent).extractingJsonPathStringValue("@.birthday").isEqualTo("01-01-2018");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"username\":\"ivan\",\"email\":\"ivan@test\",\"birthday\":\"01-01-2018\"}";

        CreateUserDto createUserDto = jacksonTester.parseObject(content);

        assertThat(createUserDto.getUsername()).isEqualTo("ivan");
        assertThat(createUserDto.getEmail()).isEqualTo("ivan@test");
        assertThat(createUserDto.getBirthday()).isEqualTo(fromStringToDate("01-01-2018"));
    }

}