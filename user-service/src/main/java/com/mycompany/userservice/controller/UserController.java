package com.mycompany.userservice.controller;

import com.mycompany.userservice.dto.CreateUserRequest;
import com.mycompany.userservice.dto.UpdateUserRequest;
import com.mycompany.userservice.dto.UserResponse;
import com.mycompany.userservice.mapper.UserMapper;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/username/{username}")
    public UserResponse getUserByUsername(@PathVariable String username) {
        User user = userService.validateAndGetUserByUsername(username);
        return userMapper.toUserResponse(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = userMapper.toUser(createUserRequest);
        user = userService.saveUser(user);
        return userMapper.toUserResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User user = userService.validateAndGetUserById(id);
        userMapper.updateUserFromRequest(updateUserRequest, user);
        user = userService.saveUser(user);
        return userMapper.toUserResponse(user);
    }

    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);
        userService.deleteUser(user);
        return userMapper.toUserResponse(user);
    }
}
