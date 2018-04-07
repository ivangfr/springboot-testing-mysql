package com.mycompany.springboottestingmysql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.springboottestingmysql.config.ModelMapperConfig;
import com.mycompany.springboottestingmysql.dto.CreateUserDto;
import com.mycompany.springboottestingmysql.dto.UpdateUserDto;
import com.mycompany.springboottestingmysql.exception.UserEmailDuplicatedException;
import com.mycompany.springboottestingmysql.exception.UserNotFoundException;
import com.mycompany.springboottestingmysql.exception.UserUsernameDuplicatedException;
import com.mycompany.springboottestingmysql.model.User;
import com.mycompany.springboottestingmysql.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mycompany.springboottestingmysql.helper.UserServiceTestHelper.*;
import static com.mycompany.springboottestingmysql.util.MyLocalDateHandler.PATTERN;
import static com.mycompany.springboottestingmysql.util.MyLocalDateHandler.fromDateToString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@Import(ModelMapperConfig.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper().setDateFormat(new SimpleDateFormat(PATTERN));
    }

    @Test
    public void given_noUsers_when_getAllUsers_then_returnEmptyJsonArray() throws Exception {
        given(userService.getAllUsers()).willReturn(new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(get("/api/users")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void given_oneUser_when_getAllUsers_then_returnJsonArrayWithOneUser() throws Exception {
        User user = getDefaultUser();
        List<User> users = Arrays.asList(user);

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
    public void given_nonExistingUsername_when_getUserByUsername_then_returnNotFound() throws Exception {
        String username = "ivan2";

        given(userService.validateAndGetUserByUsername(username)).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(get("/api/users/username/{username}", username)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void given_oneUser_when_getUserByUsername_then_returnUserJson() throws Exception {
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
    public void given_existingUsername_when_createUserWithTheSameUsername_then_returnBadRequest() throws Exception {
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
    public void given_existingEmail_when_createAnotherUserWithTheSameEmail_then_returnBadRequest() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto();

        willDoNothing().given(userService).validateUserExistsByUsername(any(String.class));
        willThrow(UserEmailDuplicatedException.class).given(userService).validateUserExistsByEmail(createUserDto.getEmail());

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void given_nonExistingUsernameAndEmail_when_createUser_then_returnUserJson() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        User user = getDefaultUser();

        willDoNothing().given(userService).validateUserExistsByUsername(any(String.class));
        willDoNothing().given(userService).validateUserExistsByEmail(any(String.class));
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
    public void given_oneUser_when_updateUserWithAlreadyExistingUsername_then_returnBadRequest() throws Exception {
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
    public void given_oneUser_when_updateUserWithAlreadyExistingEmail_then_returnBadRequest() throws Exception {
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
    public void given_oneUser_when_updateUserChangingAllFields_then_returnUserJsonWithAllFieldsChanged() throws Exception {
        User user = getDefaultUser();
        UpdateUserDto updateUserDto = getAnUpdateUserDto("ivan2", "ivan2@test", "02-02-2018");

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willDoNothing().given(userService).validateUserExistsByUsername(any(String.class));
        willDoNothing().given(userService).validateUserExistsByEmail(any(String.class));
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
    public void given_oneUser_when_updateUserChangingJustUsernameField_then_returnUserJsonWithJustUsernameChanged() throws Exception {
        User user = getDefaultUser();
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("ivan2");

        given(userService.validateAndGetUserById(user.getId())).willReturn(user);
        willDoNothing().given(userService).validateUserExistsByUsername(any(String.class));
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
    public void given_oneUser_when_updateUserWithSameUsernameAndEmailButDifferentBirthday_then_returnUserJsonWithJustBirthdayChanged() throws Exception {
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

}
