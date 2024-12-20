package com.ivanfranchin.userservice.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreateUserRequestTests {

    @Autowired
    private JacksonTester<CreateUserRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        CreateUserRequest createUserRequest = new CreateUserRequest("ivan", "ivan@test", LocalDate.parse("2018-01-01"));

        JsonContent<CreateUserRequest> jsonContent = jacksonTester.write(createUserRequest);

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

        CreateUserRequest createUserRequest = jacksonTester.parseObject(content);

        assertThat(createUserRequest.username()).isEqualTo("ivan");
        assertThat(createUserRequest.email()).isEqualTo("ivan@test");
        assertThat(createUserRequest.birthday()).isEqualTo(LocalDate.parse("2018-01-01"));
    }
}