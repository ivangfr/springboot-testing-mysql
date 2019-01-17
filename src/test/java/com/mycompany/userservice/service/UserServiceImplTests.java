package com.mycompany.userservice.service;

import com.google.common.collect.Lists;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class UserServiceImplTests {

    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void given_validUser_when_saveUser_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.save(user)).willReturn(user);

        User userSaved = userService.saveUser(user);

        assertThat(userSaved).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_noUser_when_getAllUsers_then_returnEmptyList() {
        given(userRepository.findAll()).willReturn(new ArrayList<>());

        List<User> usersFound = userService.getAllUsers();

        assertThat(usersFound).hasSize(0);
    }

    @Test
    void given_oneUser_when_getAllUsers_then_returnListWithOneUser() {
        User user = getDefaultUser();
        List<User> users = Lists.newArrayList(user);

        given(userRepository.findAll()).willReturn(users);

        List<User> usersFound = userService.getAllUsers();

        assertThat(usersFound).hasSize(1);
        assertThat(usersFound.get(0)).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_existingUserId_when_getUserById_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserById(user.getId())).willReturn(user);

        User userFound = userService.getUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_existingUserUsername_when_getUserByUsername_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        User userFound = userService.getUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_existingUserEmail_when_getUserByEmail_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserByEmail(user.getEmail())).willReturn(user);

        User userFound = userService.getUserByEmail(user.getEmail());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_existingUserId_when_validateAndGetUserById_then_returnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserById(user.getId())).willReturn(user);

        User userFound = userService.validateAndGetUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_nonExistingUserId_when_validateAndGetUserById_then_throwUserNotFoundException() {
        given(userRepository.findUserById(anyString())).willReturn(null);

        Throwable exception = assertThrows(UserNotFoundException.class, () -> userService.validateAndGetUserById("xyz"));
        assertThat("User with id 'xyz' doesn't exist.").isEqualTo(exception.getMessage());
    }

    @Test
    void given_existingUserUsername_when_validateAndGetUserByUsername_then_returnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        User userFound = userService.validateAndGetUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_nonExistingUserUsername_when_validateAndGetUserByUsername_then_throwUserNotFoundException() {
        given(userRepository.findUserByUsername(anyString())).willReturn(null);

        Throwable exception = assertThrows(UserNotFoundException.class, () -> userService.validateAndGetUserByUsername("ivan"));
        assertThat("User with username 'ivan' doesn't exist.").isEqualTo(exception.getMessage());
    }

    @Test
    void given_existingUserUsername_when_validateUserExistsByUsername_then_throwUserUsernameDuplicatedException() {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        Throwable exception = assertThrows(UserUsernameDuplicatedException.class, () -> userService.validateUserExistsByUsername(user.getUsername()));
        assertThat("User with username 'ivan' already exists.").isEqualTo(exception.getMessage());
    }

    @Test
    void given_nonExistingUserUsername_when_validateUserExistsByUsername_then_doNothing() throws UserUsernameDuplicatedException {
        String username = "ivan2";
        given(userRepository.findUserByUsername(username)).willReturn(null);

        userService.validateUserExistsByUsername(username);
    }

    @Test
    void given_existingUserEmail_when_validateUserExistsByEmail_then_throwUserEmailDuplicatedException() {
        User user = getDefaultUser();
        given(userRepository.findUserByEmail(user.getEmail())).willReturn(user);

        Throwable exception = assertThrows(UserEmailDuplicatedException.class, () -> userService.validateUserExistsByEmail(user.getEmail()));
        assertThat("User with email 'ivan@test' already exists.").isEqualTo(exception.getMessage());
    }

    @Test
    void given_nonExistingUserEmail_when_validateUserExistsByEmail_then_doNothing() throws UserEmailDuplicatedException {
        String email = "ivan2@test";
        given(userRepository.findUserByEmail(email)).willReturn(null);

        userService.validateUserExistsByEmail(email);
    }

}
