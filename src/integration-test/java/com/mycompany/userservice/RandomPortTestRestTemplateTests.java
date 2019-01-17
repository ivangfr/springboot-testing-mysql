package com.mycompany.userservice;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static com.mycompany.userservice.helper.UserServiceTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RandomPortTestRestTemplateTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /*
     * GET /api/users
     * ============== */

    @Test
    void given_noUsers_when_getAllUsers_then_returnStatusOkAndEmptyArray() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity("/api/users", UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    void given_oneUser_when_getAllUsers_then_returnStatusOkAndArrayWithOneUser() {
        User user = getDefaultUser();
        userRepository.save(user);

        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity("/api/users", UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()[0].getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody()[0].getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody()[0].getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody()[0].getBirthday()).isEqualTo(user.getBirthday());
    }

    /*
     * GET /api/users/username/{username}
     * ================================== */

    @Test
    void given_nonExistingUserUsername_when_getUserByUsername_then_returnStatusNotFound() {
        String username = "ivan";
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity("/api/users/username/" + username, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with username '" + username + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/username/ivan");
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserNotFound");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_existingUserUsername_when_getUserByUsername_then_returnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity("/api/users/username/" + user.getUsername(), UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());
    }

    /*
     * POST /api/users
     * =============== */

    @Test
    void given_validUser_when_createUser_then_returnStatusCreatedAndUserJson() {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEmpty();
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(createUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(createUserDto.getBirthday());
    }

    @Test
    void given_userWithAnExistingUsername_when_createUser_then_returnStatusBadRequest() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto(user.getUsername(), "ivan2@test", "02-02-2018");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with username '" + user.getUsername() + "' already exists.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users");
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserUsernameDuplicated");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_userWithAnExistingEmail_when_createUser_then_returnStatusBadRequest() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto("ivan2", user.getEmail(), "02-02-2018");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with email '" + user.getEmail() + "' already exists.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users");
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserEmailDuplicated");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_userWithUniqueUsernameAndEmail_when_createUser_then_returnStatusCreatedAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto("ivan2", "ivan2@test", "02-02-2018");
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEmpty();
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(createUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(createUserDto.getBirthday());
    }

    @Test
    void given_userWithInvalidEmailFormat_when_createUser_then_returnStatusBadRequest() {
        CreateUserDto createUserDto = getAnCreateUserDto("ivan", "ivan", "01-01-2018");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users");
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("BadRequest");
        assertThat(responseEntity.getBody().getErrors()).hasSize(1);
        assertThat(responseEntity.getBody().getErrors().get(0).getCodes()).contains("Email.createUserDto.email", "Email.email", "Email.java.lang.String", "Email");
        assertThat(responseEntity.getBody().getErrors().get(0).getDefaultMessage()).isEqualTo("must be a well-formed email address");
        assertThat(responseEntity.getBody().getErrors().get(0).getObjectName()).isEqualTo("createUserDto");
        assertThat(responseEntity.getBody().getErrors().get(0).getField()).isEqualTo("email");
        assertThat(responseEntity.getBody().getErrors().get(0).getRejectedValue()).isEqualTo("ivan");
        assertThat(responseEntity.getBody().getErrors().get(0).isBindingFailure()).isFalse();
        assertThat(responseEntity.getBody().getErrors().get(0).getCode()).isEqualTo("Email");
    }

    @Test
    void given_userWithoutUsername_when_createUser_then_returnStatusBadRequest() {
        CreateUserDto createUserDto = getAnCreateUserDto(null, "ivan@test", "01-01-2018");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users");
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("BadRequest");
        assertThat(responseEntity.getBody().getErrors()).hasSize(1);
    }

    /*
     * PUT /api/users
     * ============== */

    @Test
    void given_nonExistingUserId_when_updateUser_then_returnStatusNotFound() {
        String id = "5dcb867b-01e5-4741-8da8-c8c97e17842c";
        UpdateUserDto updateUserDto = getDefaultUpdateUserDto();
        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange("/api/users/" + id, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/" + id);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserNotFound");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_twoUsers_when_updateUser1WithUser2Username_then_returnStatusBadRequest() {
        User user1 = getDefaultUser();
        userRepository.save(user1);

        User user2 = getAnUser(UUID.randomUUID().toString(), "ivan2", "ivan2@test", "02-02-2018");
        userRepository.save(user2);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername(user2.getUsername());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange("/api/users/" + user1.getId(), HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with username '" + user2.getUsername() + "' already exists.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/" + user1.getId());
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserUsernameDuplicated");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_twoUsers_when_updateUser1WithUser2Email_then_returnStatusBadRequest() {
        User user1 = getDefaultUser();
        userRepository.save(user1);

        User user2 = getAnUser(UUID.randomUUID().toString(), "ivan2", "ivan2@test", "02-02-2018");
        userRepository.save(user2);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail(user2.getEmail());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange("/api/users/" + user1.getId(), HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with email '" + user2.getEmail() + "' already exists.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/" + user1.getId());
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserEmailDuplicated");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_existingUserId_when_updateUserWithUniqueUsernameAndEmail_then_returnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan2");
        updateUserDto.setEmail("ivan2@test");

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + user.getId(), HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(updateUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(updateUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());
    }

    @Test
    void given_existingUserId_when_updateUserInformingSameUsernameAndEmailButDifferentBirthday_then_returnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan", "ivan@test", "02-02-2018");

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + user.getId(), HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(updateUserDto.getBirthday());
    }

    /*
     * DELETE /api/users
     * ================= */

    @Test
    void given_nonExistingUserId_when_deleteUser_then_returnStatusNotFound() {
        UUID id = UUID.randomUUID();
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange("/api/users/" + id, HttpMethod.DELETE, null, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/" + id);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo("UserNotFound");
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_existingUserId_when_deleteUser_then_returnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + user.getId(), HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());

        User userFound = userRepository.findUserById(user.getId());

        assertThat(userFound).isNull();
    }

}
