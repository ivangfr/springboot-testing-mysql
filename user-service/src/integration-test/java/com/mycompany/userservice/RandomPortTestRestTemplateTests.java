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

import java.util.Optional;
import java.util.UUID;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnCreateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUpdateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUser;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultCreateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUpdateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(ContainersExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RandomPortTestRestTemplateTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /* GET /api/users */

    @Test
    void givenNoUsersWhenGetAllUsersThenReturnStatusOkAndEmptyArray() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    void givenOneUserWhenGetAllUsersThenReturnStatusOkAndArrayWithOneUser() {
        User user = getDefaultUser();
        userRepository.save(user);

        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()[0].getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody()[0].getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody()[0].getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody()[0].getBirthday()).isEqualTo(user.getBirthday());
    }

    /* GET /api/users/username/{username} */

    @Test
    void givenNonExistingUserUsernameWhenGetUserByUsernameThenReturnStatusNotFound() {
        String url = String.format(API_USERS_USERNAME_USERNAME_URL, "ivan");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity(url, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with username 'ivan' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenExistingUserUsernameWhenGetUserByUsernameThenReturnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        String url = String.format(API_USERS_USERNAME_USERNAME_URL, user.getUsername());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity(url, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());
    }

    /* POST /api/users */

    @Test
    void givenValidUserWhenCreateUserThenReturnStatusCreatedAndUserJson() {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEmpty();
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(createUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(createUserDto.getBirthday());
    }

    @Test
    void givenUserWithAnExistingUsernameWhenCreateUserThenReturnStatusBadRequest() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto(user.getUsername(), "ivan2@test", "2018-02-02");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenUserWithAnExistingEmailWhenCreateUserThenReturnStatusBadRequest() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto("ivan2", user.getEmail(), "2018-02-02");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenUserWithUniqueUsernameAndEmailWhenCreateUserThenReturnStatusCreatedAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = getAnCreateUserDto("ivan2", "ivan2@test", "2018-02-02");
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotEmpty();
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(createUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(createUserDto.getBirthday());
    }

    @Test
    void givenUserWithInvalidEmailFormatWhenCreateUserThenReturnStatusBadRequest() {
        CreateUserDto createUserDto = getAnCreateUserDto("ivan", "ivan", "2018-01-01");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
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
    void givenUserWithoutUsernameWhenCreateUserThenReturnStatusBadRequest() {
        CreateUserDto createUserDto = getAnCreateUserDto(null, "ivan@test", "2018-01-01");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
        assertThat(responseEntity.getBody().getErrors()).hasSize(1);
    }

    /* PUT /api/users */

    @Test
    void givenNonExistingUserIdWhenUpdateUserThenReturnStatusNotFound() {
        String id = "5dcb867b-01e5-4741-8da8-c8c97e17842c";
        UpdateUserDto updateUserDto = getDefaultUpdateUserDto();
        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);

        String url = String.format(API_USERS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenTwoUsersWhenUpdateUser1WithUser2UsernameThenReturnStatusBadRequest() {
        User user1 = getDefaultUser();
        userRepository.save(user1);

        User user2 = getAnUser(UUID.randomUUID().toString(), "ivan2", "ivan2@test", "2018-02-02");
        userRepository.save(user2);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername(user2.getUsername());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenTwoUsersWhenUpdateUser1WithUser2EmailThenReturnStatusBadRequest() {
        User user1 = getDefaultUser();
        userRepository.save(user1);

        User user2 = getAnUser(UUID.randomUUID().toString(), "ivan2", "ivan2@test", "2018-02-02");
        userRepository.save(user2);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail(user2.getEmail());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenExistingUserIdWhenUpdateUserWithUniqueUsernameAndEmailThenReturnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan2");
        updateUserDto.setEmail("ivan2@test");

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(updateUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(updateUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());
    }

    @Test
    void givenExistingUserIdWhenUpdateUserInformingSameUsernameAndEmailButDifferentBirthdayThenReturnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan", "ivan@test", "2018-02-02");

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(updateUserDto.getBirthday());
    }

    /* DELETE /api/users */

    @Test
    void givenNonExistingUserIdWhenDeleteUserThenReturnStatusNotFound() {
        UUID id = UUID.randomUUID();
        String url = String.format(API_USERS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE, null, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenExistingUserIdWhenDeleteUserThenReturnStatusOkAndUserJson() {
        User user = getDefaultUser();
        userRepository.save(user);

        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());

        Optional<User> userOptional = userRepository.findUserById(user.getId());

        assertThat(userOptional.isPresent()).isFalse();
    }

    private static final String API_USERS_URL = "/api/users";
    private static final String API_USERS_USERNAME_USERNAME_URL = "/api/users/username/%s";
    private static final String API_USERS_ID_URL = "/api/users/%s";

    private static final String ERROR_NOT_FOUND = "Not Found";
    private static final String ERROR_CODE_NOT_FOUND = "UserNotFound";
    private static final String ERROR_BAD_REQUEST = "Bad Request";
    private static final String ERROR_CODE_BAD_REQUEST = "BadRequest";
    private static final String ERROR_CODE_USER_DATA_DUPLICATED = "UserDataDuplicated";

    private static final String MSG_USERNAME_EMAIL_ALREADY_EXISTS = "The username and/or email informed already exists.";


}
