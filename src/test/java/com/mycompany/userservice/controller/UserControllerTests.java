package com.mycompany.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mycompany.userservice.config.ModelMapperConfig;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getAnUpdateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultCreateUserDto;
import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static com.mycompany.userservice.util.MyLocalDateHandler.PATTERN;
import static com.mycompany.userservice.util.MyLocalDateHandler.fromDateToString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(ModelMapperConfig.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper().setDateFormat(new SimpleDateFormat(PATTERN));
    }

    @Test
    void given_noUsers_when_getAllUsers_then_returnStatusOkAndEmptyJsonArray() throws Exception {
        given(userService.getAllUsers()).willReturn(new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void given_oneUser_when_getAllUsers_then_returnStatusOkAndJsonArrayWithOneUser() throws Exception {
        User user = getDefaultUser();
        List<User> users = Lists.newArrayList(user);

        given(userService.getAllUsers()).willReturn(users);

        ResultActions resultActions = mockMvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user.getId())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())))
                .andExpect(jsonPath("$[0].birthday", is(fromDateToString(user.getBirthday()))));
    }

    @Test
    void given_nonExistingUserUsername_when_getUserByUsername_then_returnStatusNotFound() throws Exception {
        String username = "ivan2";

        given(userService.validateAndGetUserByUsername(username)).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(get("/api/users/username/{username}", username)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void given_existingUserUsername_when_getUserByUsername_then_returnStatusOkAndUserJson() throws Exception {
        User user = getDefaultUser();

        given(userService.validateAndGetUserByUsername(user.getUsername())).willReturn(user);

        ResultActions resultActions = mockMvc.perform(get("/api/users/username/{username}", user.getUsername())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(user.getBirthday()))));
    }

    @Test
    void given_existingUserUsername_when_createUserInformingTheSameUsername_then_returnStatusBadRequest() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto();

        willThrow(UserUsernameDuplicatedException.class).given(userService).validateUserExistsByUsername(createUserDto.getUsername());

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void given_existingUserEmail_when_createUserInformingTheSameEmail_then_returnStatusBadRequest() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto();

        willDoNothing().given(userService).validateUserExistsByUsername(anyString());
        willThrow(UserEmailDuplicatedException.class).given(userService).validateUserExistsByEmail(createUserDto.getEmail());

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void given_nonExistingUserUsernameAndEmail_when_createUser_then_returnStatusCreatedAndUserJson() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        User user = getDefaultUser();

        willDoNothing().given(userService).validateUserExistsByUsername(anyString());
        willDoNothing().given(userService).validateUserExistsByEmail(anyString());
        given(userService.saveUser(any(User.class))).willReturn(user);

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(user.getBirthday()))));
    }

    @Test
    void given_oneUser_when_updateUserWithAlreadyExistingUsername_then_returnStatusBadRequest() throws Exception {
        User user = getDefaultUser();
        String username = "ivan2";
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername(username);

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willThrow(UserUsernameDuplicatedException.class).given(userService).validateUserExistsByUsername(username);

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void given_oneUser_when_updateUserWithAlreadyExistingEmail_then_returnStatusBadRequest() throws Exception {
        User user = getDefaultUser();
        String email = "ivan2@test";
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail(email);

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willThrow(UserEmailDuplicatedException.class).given(userService).validateUserExistsByEmail(email);

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void given_oneUser_when_updateUserChangingAllFields_then_returnStatusOkAndUserJsonWithAllFieldsChanged() throws Exception {
        User user = getDefaultUser();
        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan2", "ivan2@test", "02-02-2018");

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willDoNothing().given(userService).validateUserExistsByUsername(anyString());
        willDoNothing().given(userService).validateUserExistsByEmail(anyString());
        given(userService.saveUser(any(User.class))).willReturn(user);

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(updateUserDto.getUsername())))
                .andExpect(jsonPath("$.email", is(updateUserDto.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(updateUserDto.getBirthday()))));
    }

    @Test
    void given_oneUser_when_updateUserChangingJustUsernameField_then_returnStatusOkAndUserJsonWithJustUsernameChanged() throws Exception {
        User user = getDefaultUser();
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan2");

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willDoNothing().given(userService).validateUserExistsByUsername(anyString());
        given(userService.saveUser(any(User.class))).willReturn(user);

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(updateUserDto.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(user.getBirthday()))));
    }

    @Test
    void given_oneUser_when_updateUserWithSameUsernameAndEmailButDifferentBirthday_then_returnStatusOkAndUserJsonWithJustBirthdayChanged()
            throws Exception {
        User user = getDefaultUser();
        UpdateUserDto updateUserDto = getAnUpdateUserDto(user.getUsername(), user.getEmail(), "02-02-2018");

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        given(userService.saveUser(any(User.class))).willReturn(user);

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(updateUserDto.getBirthday()))));
    }

    @Test
    void given_existingUserId_when_deleteUser_then_returnStatusOk() throws Exception {
        User user = getDefaultUser();

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willDoNothing().given(userService).deleteUser(any(User.class));

        ResultActions resultActions = mockMvc.perform(delete("/api/users/{id}", user.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(user.getId())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.birthday", is(fromDateToString(user.getBirthday()))));
    }

    @Test
    void given_nonExistingUserId_when_deleteUser_then_returnStatusNotFound() throws Exception {
        given(userService.validateAndGetUserById(anyString())).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

}
