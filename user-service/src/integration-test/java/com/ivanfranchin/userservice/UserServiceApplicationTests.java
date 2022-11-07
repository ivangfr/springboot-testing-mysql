package com.ivanfranchin.userservice;

import com.ivanfranchin.userservice.dto.CreateUserRequest;
import com.ivanfranchin.userservice.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.dto.UserResponse;
import com.ivanfranchin.userservice.model.User;
import com.ivanfranchin.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserServiceApplicationTests extends AbstractTestcontainers {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /* GET /api/users */

    @Test
    void testGetUsersWhenThereIsNone() {
        ResponseEntity<UserResponse[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void testGetUsersWhenThereIsOne() {
        User user = getDefaultUser();
        userRepository.save(user);

        ResponseEntity<UserResponse[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()[0].id()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody()[0].username()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody()[0].email()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody()[0].birthday()).isEqualTo(user.getBirthday());
    }

    /* GET /api/users/username/{username} */

    @Test
    void testGetUserByUsernameWhenNonExistent() {
        String url = String.format(API_USERS_USERNAME_USERNAME_URL, "ivan");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity(url, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().message()).isEqualTo("User with username 'ivan' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo(url);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testGetUserByUsernameWhenExistent() {
        User user = getDefaultUser();
        userRepository.save(user);

        String url = String.format(API_USERS_USERNAME_USERNAME_URL, user.getUsername());
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.getForEntity(url, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().username()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().email()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().birthday()).isEqualTo(user.getBirthday());
    }

    /* POST /api/users */

    @Test
    void testCreateUserInformingValidInfo() {
        CreateUserRequest createUserRequest = new CreateUserRequest("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserRequest, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isNotNull();
        assertThat(responseEntity.getBody().username()).isEqualTo(createUserRequest.getUsername());
        assertThat(responseEntity.getBody().email()).isEqualTo(createUserRequest.getEmail());
        assertThat(responseEntity.getBody().birthday()).isEqualTo(createUserRequest.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().id());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userCreated -> {
            assertThat(userCreated.getUsername()).isEqualTo(createUserRequest.getUsername());
            assertThat(userCreated.getEmail()).isEqualTo(createUserRequest.getEmail());
            assertThat(userCreated.getBirthday()).isEqualTo(createUserRequest.getBirthday());
            assertThat(userCreated.getCreatedOn()).isNotNull();
            assertThat(userCreated.getUpdatedOn()).isNotNull();
        });
    }

    @Test
    void testCreateUserWhenInformingExistentUsername() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserRequest createUserRequest = new CreateUserRequest(user.getUsername(), "ivan2@test", LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserRequest, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(409);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().message()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().path()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testCreateUserWhenInformingExistentEmail() {
        User user = getDefaultUser();
        userRepository.save(user);

        CreateUserRequest createUserRequest = new CreateUserRequest("ivan2", user.getEmail(), LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserRequest, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(409);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().message()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().path()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testCreateUserInformingInvalidEmailFormat() {
        CreateUserRequest createUserRequest = new CreateUserRequest("ivan", "ivan", LocalDate.parse("2018-01-01"));
        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserRequest, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(400);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().message())
                .isEqualTo("Validation failed for object='createUserRequest'. Error count: 1");
        assertThat(responseEntity.getBody().path()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
        assertThat(responseEntity.getBody().errors()).hasSize(1);
        assertThat(responseEntity.getBody().errors().get(0).codes())
                .contains("Email.createUserRequest.email", "Email.email", "Email.java.lang.String", "Email");
        assertThat(responseEntity.getBody().errors().get(0).defaultMessage())
                .isEqualTo("must be a well-formed email address");
        assertThat(responseEntity.getBody().errors().get(0).objectName()).isEqualTo("createUserRequest");
        assertThat(responseEntity.getBody().errors().get(0).field()).isEqualTo("email");
        assertThat(responseEntity.getBody().errors().get(0).rejectedValue()).isEqualTo("ivan");
        assertThat(responseEntity.getBody().errors().get(0).bindingFailure()).isFalse();
        assertThat(responseEntity.getBody().errors().get(0).code()).isEqualTo("Email");
    }

    @Test
    void testCreateUserNotInformingUsername() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("ivan@test");
        createUserRequest.setBirthday(LocalDate.parse("2018-01-01"));

        ResponseEntity<MessageError> responseEntity = testRestTemplate.postForEntity(
                API_USERS_URL, createUserRequest, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(400);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_BAD_REQUEST);
        assertThat(responseEntity.getBody().message())
                .isEqualTo("Validation failed for object='createUserRequest'. Error count: 1");
        assertThat(responseEntity.getBody().path()).isEqualTo(API_USERS_URL);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_BAD_REQUEST);
        assertThat(responseEntity.getBody().errors()).hasSize(1);
    }

    /* PUT /api/users */

    @Test
    void testUpdateUserWhenNonExisting() {
        Long id = 1L;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername("ivan");
        updateUserRequest.setEmail("ivan@test");
        updateUserRequest.setBirthday(LocalDate.parse("2018-01-01"));

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);

        String url = String.format(API_USERS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().message()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo(url);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testUpdateUserWhenUpdatingUsernameWithExistingOne() {
        User user1 = userRepository.save(new User("ivan", "ivan@test", LocalDate.parse("2018-01-01")));
        User user2 = userRepository.save(new User("ivan2", "ivan2@test", LocalDate.parse("2018-02-02")));

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername(user2.getUsername());

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(409);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().message()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().path()).isEqualTo(url);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testUpdateUserWhenUpdatingEmailWithExistingOne() {
        User user1 = userRepository.save(new User("ivan", "ivan@test", LocalDate.parse("2018-01-01")));
        User user2 = userRepository.save(new User("ivan2", "ivan2@test", LocalDate.parse("2018-02-02")));

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setEmail(user2.getEmail());

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String url = String.format(API_USERS_ID_URL, user1.getId());
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(409);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_CONFLICT);
        assertThat(responseEntity.getBody().message()).isEqualTo(MSG_USERNAME_EMAIL_ALREADY_EXISTS);
        assertThat(responseEntity.getBody().path()).isEqualTo(url);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_USER_DATA_DUPLICATED);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testUpdateUserWhenUpdatingUsernameAndEmailWithUniqueValues() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setUsername("ivan2");
        updateUserRequest.setEmail("ivan2@test");

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().username()).isEqualTo(updateUserRequest.getUsername());
        assertThat(responseEntity.getBody().email()).isEqualTo(updateUserRequest.getEmail());
        assertThat(responseEntity.getBody().birthday()).isEqualTo(user.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().id());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userUpdated -> {
            assertThat(userUpdated.getUsername()).isEqualTo(updateUserRequest.getUsername());
            assertThat(userUpdated.getEmail()).isEqualTo(updateUserRequest.getEmail());
            assertThat(userUpdated.getBirthday()).isEqualTo(user.getBirthday());
            assertThat(userUpdated.getCreatedOn()).isNotNull();
            assertThat(userUpdated.getUpdatedOn()).isNotNull();
        });
    }

    @Test
    void testUpdateUserWhenUpdatingBirthday() {
        User user = getDefaultUser();
        userRepository.save(user);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setBirthday(LocalDate.parse("2018-02-02"));

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.PUT, requestUpdate, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().username()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().email()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().birthday()).isEqualTo(updateUserRequest.getBirthday());

        Optional<User> userOptional = userRepository.findById(responseEntity.getBody().id());
        assertThat(userOptional.isPresent()).isTrue();
        userOptional.ifPresent(userUpdated -> {
            assertThat(userUpdated.getUsername()).isEqualTo(user.getUsername());
            assertThat(userUpdated.getEmail()).isEqualTo(user.getEmail());
            assertThat(userUpdated.getBirthday()).isEqualTo(updateUserRequest.getBirthday());
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
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().message()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo(url);
        assertThat(responseEntity.getBody().errorCode()).isEqualTo(ERROR_CODE_NOT_FOUND);
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    @Test
    void testDeleteUserWhenExistent() {
        User user = getDefaultUser();
        userRepository.save(user);

        String url = String.format(API_USERS_ID_URL, user.getId());
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(
                url, HttpMethod.DELETE, null, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().username()).isEqualTo(user.getUsername());
        assertThat(responseEntity.getBody().email()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().birthday()).isEqualTo(user.getBirthday());

        Optional<User> userOptional = userRepository.findById(user.getId());
        assertThat(userOptional).isNotPresent();
    }

    private User getDefaultUser() {
        return new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    }

    private record MessageError(String timestamp, int status, String error, String message, String path,
                                String errorCode, List<ErrorDetail> errors) {

        public record ErrorDetail(List<String> codes, String defaultMessage, String objectName, String field,
                                  String rejectedValue, boolean bindingFailure, String code) {
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
