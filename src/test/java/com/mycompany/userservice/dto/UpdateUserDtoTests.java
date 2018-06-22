package com.mycompany.userservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUpdateUserDto;
import static com.mycompany.userservice.util.MyLocalDateHandler.fromStringToDate;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
public class UpdateUserDtoTests {

    @Autowired
    private JacksonTester<UpdateUserDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan", "ivan@test", "01-01-2018");

        JsonContent<UpdateUserDto> jsonContent = jacksonTester.write(updateUserDto);

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

        UpdateUserDto updateUserDto = jacksonTester.parseObject(content);

        assertThat(updateUserDto.getUsername()).isEqualTo("ivan");
        assertThat(updateUserDto.getEmail()).isEqualTo("ivan@test");
        assertThat(updateUserDto.getBirthday()).isEqualTo(fromStringToDate("01-01-2018"));
    }

}