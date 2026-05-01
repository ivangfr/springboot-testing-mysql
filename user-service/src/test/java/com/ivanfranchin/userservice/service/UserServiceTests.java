package com.ivanfranchin.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ivanfranchin.userservice.user.UserRepository;
import com.ivanfranchin.userservice.user.UserService;
import com.ivanfranchin.userservice.user.exception.UserDataDuplicatedException;
import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.model.User;

@ExtendWith(SpringExtension.class)
@Import(UserService.class)
class UserServiceTests {

  @Autowired private UserService userService;

  @MockitoBean private UserRepository userRepository;

  @Test
  void testSaveUser() {
    User user = getDefaultUser();
    given(userRepository.save(any(User.class))).willReturn(user);

    User userSaved = userService.saveUser(user);
    assertThat(userSaved).isEqualTo(user);
  }

  @Test
  void testSaveUserWhenDataIntegrityViolationOccurs() {
    User user = getDefaultUser();
    given(userRepository.save(any(User.class))).willThrow(DataIntegrityViolationException.class);

    assertThatThrownBy(() -> userService.saveUser(user))
        .isInstanceOf(UserDataDuplicatedException.class)
        .hasMessage("The username and/or email informed already exists.");
  }

  @Test
  void testDeleteUser() {
    User user = getDefaultUser();
    willDoNothing().given(userRepository).delete(any(User.class));

    userService.deleteUser(user);

    verify(userRepository).delete(user);
  }

  @Test
  void testGetUsersWhenThereIsNone() {
    given(userRepository.findAll()).willReturn(List.of());

    List<User> usersFound = userService.getUsers();
    assertThat(usersFound).isEmpty();
  }

  @Test
  void testGetUsersWhenThereIsOne() {
    User user = getDefaultUser();
    List<User> users = List.of(user);

    given(userRepository.findAll()).willReturn(users);

    List<User> usersFound = userService.getUsers();
    assertThat(usersFound).hasSize(1);
    assertThat(usersFound.getFirst()).isEqualTo(user);
  }

  @Test
  void testValidateAndGetUserByIdWhenExisting() {
    User user = getDefaultUser();
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

    User userFound = userService.validateAndGetUserById(user.getId());
    assertThat(userFound).isEqualTo(user);
  }

  @Test
  void testValidateAndGetUserByIdWhenNonExisting() {
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.validateAndGetUserById(1L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User with id '1' doesn't exist.");
  }

  @Test
  void testValidateAndGetUserByUsernameWhenExisting() {
    User user = getDefaultUser();
    given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));

    User userFound = userService.validateAndGetUserByUsername(user.getUsername());
    assertThat(userFound).isEqualTo(user);
  }

  @Test
  void testValidateAndGetUserByUsernameWhenNonExisting() {
    given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.validateAndGetUserByUsername("ivan"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User with username 'ivan' doesn't exist.");
  }

  private User getDefaultUser() {
    User user = new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    user.setId(1L);
    return user;
  }
}
