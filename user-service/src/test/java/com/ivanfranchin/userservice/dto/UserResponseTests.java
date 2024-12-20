package com.ivanfranchin.userservice.dto;

import com.ivanfranchin.userservice.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserResponseTests {

    @Autowired
    private JacksonTester<UserResponse> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UserResponse userResponse = new UserResponse(1L, "ivan", "ivan@test", LocalDate.parse("2018-01-01"));

        JsonContent<UserResponse> jsonContent = jacksonTester.write(userResponse);

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.id")
                .extractingJsonPathNumberValue("@.id").isEqualTo(userResponse.id().intValue());

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

        UserResponse userResponse = jacksonTester.parseObject(content);

        assertThat(userResponse.id()).isEqualTo(1);
        assertThat(userResponse.username()).isEqualTo("ivan");
        assertThat(userResponse.email()).isEqualTo("ivan@test");
        assertThat(userResponse.birthday()).isEqualTo(LocalDate.parse("2018-01-01"));
    }
}