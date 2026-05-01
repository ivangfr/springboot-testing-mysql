package com.ivanfranchin.userservice.controller;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.ivanfranchin.userservice.user.UserController;
import com.ivanfranchin.userservice.user.UserService;
import com.ivanfranchin.userservice.user.dto.CreateUserRequest;
import com.ivanfranchin.userservice.user.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.user.exception.UserDataDuplicatedException;
import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.model.User;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  @Test
  void testGetUsersWhenThereIsNone() throws Exception {
    given(userService.getUsers()).willReturn(List.of());

    ResultActions resultActions = mockMvc.perform(get(API_USERS_URL)).andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$, hasSize(0)));
  }

  @Test
  void testGetUsersWhenThereIsOne() throws Exception {
    User user = getDefaultUser();
    List<User> users = List.of(user);

    given(userService.getUsers()).willReturn(users);

    ResultActions resultActions = mockMvc.perform(get(API_USERS_URL)).andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$, hasSize(1)))
        .andExpect(jsonPath(JSON_$_0_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_0_USERNAME, is(user.getUsername())))
        .andExpect(jsonPath(JSON_$_0_EMAIL, is(user.getEmail())))
        .andExpect(jsonPath(JSON_$_0_BIRTHDAY, is(user.getBirthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testGetUserByIdWhenExistent() throws Exception {
    User user = getDefaultUser();

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);

    ResultActions resultActions =
        mockMvc.perform(get(API_USERS_ID_URL, user.getId())).andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(user.getUsername())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
        .andExpect(jsonPath(JSON_$_BIRTHDAY, is(user.getBirthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testGetUserByIdWhenNonExistent() throws Exception {
    given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

    ResultActions resultActions = mockMvc.perform(get(API_USERS_ID_URL, 1L)).andDo(print());

    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void testCreateUserInformingValidInfo() throws Exception {
    User user = getDefaultUser();
    given(userService.saveUser(any(User.class))).willReturn(user);

    CreateUserRequest createUserRequest = getDefaultCreateUserRequest();
    ResultActions resultActions =
        mockMvc
            .perform(
                post(API_USERS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andDo(print());

    resultActions
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(user.getUsername())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
        .andExpect(jsonPath(JSON_$_BIRTHDAY, is(user.getBirthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testCreateUserWhenInformingExistentUsername() throws Exception {
    CreateUserRequest createUserRequest = getDefaultCreateUserRequest();

    willThrow(UserDataDuplicatedException.class).given(userService).saveUser(any(User.class));

    ResultActions resultActions =
        mockMvc
            .perform(
                post(API_USERS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isConflict());
  }

  @Test
  void testCreateUserInformingInvalidEmailFormat() throws Exception {
    CreateUserRequest createUserRequest =
        new CreateUserRequest("ivan", "not-an-email", LocalDate.parse("2018-01-01"));

    ResultActions resultActions =
        mockMvc
            .perform(
                post(API_USERS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserNotInformingUsername() throws Exception {
    CreateUserRequest createUserRequest =
        new CreateUserRequest(null, "ivan@test", LocalDate.parse("2018-01-01"));

    ResultActions resultActions =
        mockMvc
            .perform(
                post(API_USERS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUserNotInformingBirthday() throws Exception {
    CreateUserRequest createUserRequest = new CreateUserRequest("ivan", "ivan@test", null);

    ResultActions resultActions =
        mockMvc
            .perform(
                post(API_USERS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateUserWhenNoFieldChanges() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest = new UpdateUserRequest(user.getUsername(), null, null);

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(user.getUsername())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
        .andExpect(jsonPath(JSON_$_BIRTHDAY, is(user.getBirthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testUpdateUserWhenNonExisting() throws Exception {
    given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

    UpdateUserRequest updateUserRequest = new UpdateUserRequest("ivan2", null, null);
    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void testUpdateUserWhenInformingExistingUsername() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest = new UpdateUserRequest("ivan2", null, null);

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    willThrow(UserDataDuplicatedException.class).given(userService).saveUser(any(User.class));

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isConflict());
  }

  @Test
  void testUpdateUserWhenInformingExistingEmail() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest = new UpdateUserRequest("ivan2", "ivan2@test", null);

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    willThrow(UserDataDuplicatedException.class).given(userService).saveUser(any(User.class));

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions.andExpect(status().isConflict());
  }

  @Test
  void testUpdateUserWhenChangingAllFields() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest =
        new UpdateUserRequest("ivan2", "ivan2@test", LocalDate.parse("2018-02-02"));

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    given(userService.saveUser(any(User.class))).willReturn(user);

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(updateUserRequest.username())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(updateUserRequest.email())))
        .andExpect(
            jsonPath(JSON_$_BIRTHDAY, is(updateUserRequest.birthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testUpdateUserWhenChangingJustUsernameField() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest = new UpdateUserRequest("ivan2", null, null);

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    given(userService.saveUser(any(User.class))).willReturn(user);

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(updateUserRequest.username())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
        .andExpect(jsonPath(JSON_$_BIRTHDAY, is(user.getBirthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testUpdateUserWhenChangingJustBirthdayField() throws Exception {
    User user = getDefaultUser();
    UpdateUserRequest updateUserRequest =
        new UpdateUserRequest("ivan2", null, LocalDate.parse("2018-02-02"));

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    given(userService.saveUser(any(User.class))).willReturn(user);

    ResultActions resultActions =
        mockMvc
            .perform(
                patch(API_USERS_ID_URL, user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andDo(print());

    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
        .andExpect(jsonPath(JSON_$_USERNAME, is(user.getUsername())))
        .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
        .andExpect(
            jsonPath(JSON_$_BIRTHDAY, is(updateUserRequest.birthday().format(ISO_LOCAL_DATE))));
  }

  @Test
  void testDeleteUserWhenExistent() throws Exception {
    User user = getDefaultUser();

    given(userService.validateAndGetUserById(anyLong())).willReturn(user);
    willDoNothing().given(userService).deleteUser(any(User.class));

    ResultActions resultActions =
        mockMvc.perform(delete(API_USERS_ID_URL, user.getId())).andDo(print());

    resultActions.andExpect(status().isNoContent());
  }

  @Test
  void testDeleteUserWhenNonExistent() throws Exception {
    given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

    ResultActions resultActions = mockMvc.perform(delete(API_USERS_ID_URL, 1L)).andDo(print());

    resultActions.andExpect(status().isNotFound());
  }

  private User getDefaultUser() {
    User user = new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    user.setId(1L);
    return user;
  }

  private CreateUserRequest getDefaultCreateUserRequest() {
    return new CreateUserRequest("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
  }

  private static final String API_USERS_URL = "/api/users";
  private static final String API_USERS_ID_URL = "/api/users/{id}";

  private static final String JSON_$ = "$";

  private static final String JSON_$_ID = "$.id";
  private static final String JSON_$_USERNAME = "$.username";
  private static final String JSON_$_EMAIL = "$.email";
  private static final String JSON_$_BIRTHDAY = "$.birthday";

  private static final String JSON_$_0_ID = "$[0].id";
  private static final String JSON_$_0_USERNAME = "$[0].username";
  private static final String JSON_$_0_EMAIL = "$[0].email";
  private static final String JSON_$_0_BIRTHDAY = "$[0].birthday";
}
