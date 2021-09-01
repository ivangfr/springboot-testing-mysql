package com.mycompany.userservice;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import lombok.Data;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class RandomPortTestRestTemplateTests extends AbstractTestcontainers {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    /* GET /api/users */

    @Test
    void testGetUsersWhenThereIsNone() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void testGetUsersWhenThereIsOne() {
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
    void testGetUserByUsernameWhenNonExistent() {
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
    void testGetUserByUsernameWhenExistent() {
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
    void testCreateUserInformingValidInfo() {
        CreateUserDto createUserDto = new CreateUserDto("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(createUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(createUserDto.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().getId());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userCreated -> {
            assertThat(userCreated.getUsername()).isEqualTo(createUserDto.getUsername());
            assertThat(userCreated.getEmail()).isEqualTo(createUserDto.getEmail());
            assertThat(userCreated.getBirthday()).isEqualTo(createUserDto.getBirthday());
            assertThat(userCreated.getCreatedOn()).isNotNull();
            assertThat(userCreated.getUpdatedOn()).isNotNull();
        });
    }

    @Test
    void testCreateUserWhenInformingExistentUsername() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = new CreateUserDto(user.getUsername(), "ivan2@test", LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(409);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testCreateUserWhenInformingExistentEmail() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserDto createUserDto = new CreateUserDto("ivan2", user.getEmail(), LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(409);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testCreateUserInformingInvalidEmailFormat() {
        CreateUserDto createUserDto = new CreateUserDto("ivan", "ivan", LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage())
                .isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
        assertThat(responseEntity.getBody().getErrors()).hasSize(1);
        assertThat(responseEntity.getBody().getErrors().get(0).getCodes())
                .contains("Email.createUserDto.email", "Email.email", "Email.java.lang.String", "Email");
        assertThat(responseEntity.getBody().getErrors().get(0).getDefaultMessage())
                .isEqualTo("must be a well-formed email address");
        assertThat(responseEntity.getBody().getErrors().get(0).getObjectName()).isEqualTo("createUserDto");
        assertThat(responseEntity.getBody().getErrors().get(0).getField()).isEqualTo("email");
        assertThat(responseEntity.getBody().getErrors().get(0).getRejectedValue()).isEqualTo("ivan");
        assertThat(responseEntity.getBody().getErrors().get(0).isBindingFailure()).isFalse();
        assertThat(responseEntity.getBody().getErrors().get(0).getCode()).isEqualTo("Email");
    }

    @Test
    void testCreateUserNotInformingUsername() {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("ivan@test");
        createUserDto.setBirthday(LocalDate.parse("2018-01-01"));

        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserDto, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(400);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage())
                .isEqualTo("Validation failed for object='createUserDto'. Error count: 1");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
        assertThat(responseEntity.getBody().getErrors()).hasSize(1);
    }

    /* PUT /api/users */

    @Test
    void testUpdateUserWhenNonExisting() {
        Long id = 1L;
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan");
        updateUserDto.setEmail("ivan@test");
        updateUserDto.setBirthday(LocalDate.parse("2018-01-01"));

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);

        String url = String.format(API_USERS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

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
    void testUpdateUserWhenUpdatingUsernameWithExistingOne() {
        User user1 = userRepository.save(new User("ivan", "ivan@test", LocalDate.parse("2018-01-01")));
        User user2 = userRepository.save(new User("ivan2", "ivan2@test", LocalDate.parse("2018-02-02")));

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername(user2.getUsername());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(409);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testUpdateUserWhenUpdatingEmailWithExistingOne() {
        User user1 = userRepository.save(new User("ivan", "ivan@test", LocalDate.parse("2018-01-01")));
        User user2 = userRepository.save(new User("ivan2", "ivan2@test", LocalDate.parse("2018-02-02")));

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail(user2.getEmail());

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(409);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testUpdateUserWhenUpdatingUsernameAndEmailWithUniqueValues() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan2");
        updateUserDto.setEmail("ivan2@test");

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(updateUserDto.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(updateUserDto.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().getId());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userUpdated -> {
            assertThat(userUpdated.getUsername()).isEqualTo(updateUserDto.getUsername());
            assertThat(userUpdated.getEmail()).isEqualTo(updateUserDto.getEmail());
            assertThat(userUpdated.getBirthday()).isEqualTo(user.getBirthday());
            assertThat(userUpdated.getCreatedOn()).isNotNull();
            assertThat(userUpdated.getUpdatedOn()).isNotNull();
        });
    }

    @Test
    void testUpdateUserWhenUpdatingBirthday() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setBirthday(LocalDate.parse("2018-02-02"));

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(updateUserDto.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().getId());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userUpdated -> {
            assertThat(userUpdated.getUsername()).isEqualTo(user.getUsername());
            assertThat(userUpdated.getEmail()).isEqualTo(user.getEmail());
            assertThat(userUpdated.getBirthday()).isEqualTo(updateUserDto.getBirthday());
        });
    }

    /* DELETE /api/users */

    @Test
    void testDeleteUserWhenNonExistent() {
        Long id = 1L;
        String url = String.format(API_USERS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.DELETE, null, MessageError.class);

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
    void testDeleteUserWhenExistent() {
        User user = getDefaultUser();
        userRepository.save(user);

        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getUsername()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getBirthday()).isEqualTo(user.getBirthday());

        Optional<User> userOptional = userRepository.findById(user.getId());
        assertThat(userOptional).isNotPresent();
    }

    private User getDefaultUser() {
        return new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    }

    @Data
    private static class MessageError {

        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String errorCode;
        private List<ErrorDetail> errors;

        @Data
        public static class ErrorDetail {
            private List<String> codes;
            private String defaultMessage;
            private String objectName;
            private String field;
            private String rejectedValue;
            private boolean bindingFailure;
            private String code;
        }
    }

    private static final String API_USERS_URL = "/api/users";
    private static final String API_USERS_USERNAME_USERNAME_URL = "/api/users/username/%s";
    private static final String API_USERS_ID_URL = "/api/users/%s";

    private static final String ERROR_NOT_FOUND = "Not Found";
    private static final String ERROR_CODE_NOT_FOUND = "UserNotFound";
    private static final String ERROR_BAD_REQUEST = "Bad Request";
    private static final String ERROR_CODE_BAD_REQUEST = "BadRequest";
    private static final String ERROR_CONFLICT = "Conflict";
    private static final String ERROR_CODE_USER_DATA_DUPLICATED = "UserDataDuplicated";

    private static final String MSG_USERNAME_EMAIL_ALREADY_EXISTS = "The username and/or email informed already exists.";

}
