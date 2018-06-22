package com.mycompany.userservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.UUID;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUserDto;
import static com.mycompany.userservice.util.MyLocalDateHandler.fromStringToDate;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
public class UserDtoTests {

    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        String id = UUID.randomUUID().toString();
        UserDto userDto = getAnUserDto(id, "ivan", "ivan@test", "01-01-2018");

        JsonContent<UserDto> jsonContent = jacksonTester.write(userDto);

        assertThat(jsonContent).hasJsonPathStringValue("@.id");
        assertThat(jsonContent).extractingJsonPathStringValue("@.id").isEqualTo(userDto.getId());
        assertThat(jsonContent).hasJsonPathStringValue("@.username");
        assertThat(jsonContent).extractingJsonPathStringValue("@.username").isEqualTo("ivan");
        assertThat(jsonContent).hasJsonPathStringValue("@.email");
        assertThat(jsonContent).extractingJsonPathStringValue("@.email").isEqualTo("ivan@test");
        assertThat(jsonContent).hasJsonPathStringValue("@.birthday");
        assertThat(jsonContent).extractingJsonPathStringValue("@.birthday").isEqualTo("01-01-2018");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":\"5aa5fad4-03ed-43e0-9e5f-8cfaf1ef616c\",\"username\":\"ivan\",\"email\":\"ivan@test\",\"birthday\":\"01-01-2018\"}";

        UserDto userDto = jacksonTester.parseObject(content);

        assertThat(userDto.getId()).isEqualTo("5aa5fad4-03ed-43e0-9e5f-8cfaf1ef616c");
        assertThat(userDto.getUsername()).isEqualTo("ivan");
        assertThat(userDto.getEmail()).isEqualTo("ivan@test");
        assertThat(userDto.getBirthday()).isEqualTo(fromStringToDate("01-01-2018"));
    }

}