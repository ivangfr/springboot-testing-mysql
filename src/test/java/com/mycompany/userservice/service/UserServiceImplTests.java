package com.mycompany.userservice.service;

import com.google.common.collect.Lists;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class UserServiceImplTests {

    private UserService userService;
    private UserRepository userRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void given_oneUser_when_saveUser_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.save(user)).willReturn(user);

        User userSaved = userService.saveUser(user);

        assertThat(userSaved).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_noUser_when_getAllUsers_then_returnEmptyList() {
        given(userRepository.findAll()).willReturn(new ArrayList<>());

        List<User> usersFound = userService.getAllUsers();

        assertThat(usersFound).hasSize(0);
    }

    @Test
    public void given_oneUser_when_getAllUsers_then_returnListWithOneUser() {
        User user = getDefaultUser();
        List<User> users = Lists.newArrayList(user);

        given(userRepository.findAll()).willReturn(users);

        List<User> usersFound = userService.getAllUsers();

        assertThat(usersFound).hasSize(1);
        assertThat(usersFound.get(0)).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_oneUser_when_getUserById_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserById(user.getId())).willReturn(user);

        User userFound = userService.getUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_oneUser_when_getUserByUsername_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        User userFound = userService.getUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_oneUser_when_getUserByEmail_then_returnUser() {
        User user = getDefaultUser();
        given(userRepository.findUserByEmail(user.getEmail())).willReturn(user);

        User userFound = userService.getUserByEmail(user.getEmail());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_oneUser_when_validateAndGetUserById_then_returnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserById(user.getId())).willReturn(user);

        User userFound = userService.validateAndGetUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_nonExistingId_when_validateAndGetUserById_then_throwException() throws UserNotFoundException {
        given(userRepository.findUserById(anyString())).willReturn(null);

        expectedException.expect(UserNotFoundException.class);
        expectedException.expectMessage("User with id 'xyz' doesn't exist");

        userService.validateAndGetUserById("xyz");
    }

    @Test
    public void given_oneUser_when_validateAndGetUserByUsername_then_returnUser() throws UserNotFoundException {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        User userFound = userService.validateAndGetUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_nonExistingUsername_when_validateAndGetUserByUsername_then_throwException() throws UserNotFoundException {
        given(userRepository.findUserByUsername(anyString())).willReturn(null);

        expectedException.expect(UserNotFoundException.class);
        expectedException.expectMessage("User with username 'ivan' doesn't exist");

        userService.validateAndGetUserByUsername("ivan");
    }

    @Test
    public void given_existingUsername_when_validateUserExistsByUsername_then_throwException() throws UserUsernameDuplicatedException {
        User user = getDefaultUser();
        given(userRepository.findUserByUsername(user.getUsername())).willReturn(user);

        expectedException.expect(UserUsernameDuplicatedException.class);
        expectedException.expectMessage("User with username 'ivan' already exists.");

        userService.validateUserExistsByUsername(user.getUsername());
    }

    @Test
    public void given_nonExistingUsername_when_validateUserExistsByUsername_then_doNothing() throws UserUsernameDuplicatedException {
        String username = "ivan2";
        given(userRepository.findUserByUsername(username)).willReturn(null);

        userService.validateUserExistsByUsername(username);
    }

    @Test
    public void given_existingEmail_when_validateUserExistsByEmail_then_throwException() throws UserEmailDuplicatedException {
        User user = getDefaultUser();
        given(userRepository.findUserByEmail(user.getEmail())).willReturn(user);

        expectedException.expect(UserEmailDuplicatedException.class);
        expectedException.expectMessage("User with email 'ivan@test' already exists.");

        userService.validateUserExistsByEmail(user.getEmail());
    }

    @Test
    public void given_nonExistingEmail_when_validateUserExistsByEmail_then_doNothing() throws UserEmailDuplicatedException {
        String email = "ivan2@test";
        given(userRepository.findUserByEmail(email)).willReturn(null);

        userService.validateUserExistsByEmail(email);
    }

}
