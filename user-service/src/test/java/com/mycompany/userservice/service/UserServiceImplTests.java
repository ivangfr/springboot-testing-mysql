package com.mycompany.userservice.service;

import com.google.common.collect.Lists;
import com.mycompany.userservice.exception.UserDataDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    void givenValidUserWhenSaveUserThenReturnUser() throws UserDataDuplicatedException {
        User user = getDefaultUser();
        given(userRepository.save(user)).willReturn(user);

        User userSaved = userService.saveUser(user);
        assertThat(userSaved).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNoUserWhenGetAllUsersThenReturnEmptyList() {
        given(userRepository.findAll()).willReturn(Collections.emptyList());

        List<User> usersFound = userService.getAllUsers();
        assertThat(usersFound).hasSize(0);
    }

    @Test
    void givenOneUserWhenGetAllUsersThenReturnListWithOneUser() {
        User user = getDefaultUser();
        List<User> users = Lists.newArrayList(user);

        given(userRepository.findAll()).willReturn(users);

        List<User> usersFound = userService.getAllUsers();
        assertThat(usersFound).hasSize(1);
        assertThat(usersFound.get(0)).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenExistingUserIdWhenValidateAndGetUserByIdThenReturnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));

        User userFound = userService.validateAndGetUserById(user.getId());
        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNonExistingUserIdWhenValidateAndGetUserByIdThenThrowUserNotFoundException() {
        given(userRepository.findUserById(anyString())).willReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class, () -> userService.validateAndGetUserById("xyz"));
        assertThat(exception.getMessage()).isEqualTo("User with id 'xyz' doesn't exist.");
    }

    @Test
    void givenExistingUserUsernameWhenValidateAndGetUserByUsernameThenReturnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(Optional.of(user));

        User userFound = userService.validateAndGetUserByUsername(user.getUsername());
        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNonExistingUserUsernameWhenValidateAndGetUserByUsernameThenThrowUserNotFoundException() {
        given(userRepository.findUserByUsername(anyString())).willReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class, () -> userService.validateAndGetUserByUsername("ivan"));
        assertThat(exception.getMessage()).isEqualTo("User with username 'ivan' doesn't exist.");
    }

}
