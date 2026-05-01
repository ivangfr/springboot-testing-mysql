package com.ivanfranchin.userservice.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ivanfranchin.userservice.user.dto.CreateUserRequest;
import com.ivanfranchin.userservice.user.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.user.dto.UserResponse;
import com.ivanfranchin.userservice.user.model.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<UserResponse> getUsers(@RequestParam(required = false) String username) {
    if (username != null) {
      User user = userService.validateAndGetUserByUsername(username);
      return List.of(UserResponse.from(user));
    }
    return userService.getUsers().stream().map(UserResponse::from).toList();
  }

  @GetMapping("/{id}")
  public UserResponse getUserById(@PathVariable Long id) {
    User user = userService.validateAndGetUserById(id);
    return UserResponse.from(user);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
    User user = createUserRequest.toDomain();
    user = userService.saveUser(user);
    return UserResponse.from(user);
  }

  @PatchMapping("/{id}")
  public UserResponse updateUser(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    User user = userService.validateAndGetUserById(id);
    boolean changed = updateUserRequest.hasChanges(user);
    updateUserRequest.applyTo(user);
    if (changed) {
      user = userService.saveUser(user);
    }
    return UserResponse.from(user);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable Long id) {
    User user = userService.validateAndGetUserById(id);
    userService.deleteUser(user);
  }
}
