package com.mycompany.userservice.controller;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.exception.UserDataDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.mapper.UserMapper;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/username/{username}")
    public UserDto getUserByUsername(@PathVariable String username) throws UserNotFoundException {
        User user = userService.validateAndGetUserByUsername(username);
        return userMapper.toUserDto(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto createUser(@Valid @RequestBody CreateUserDto createUserDto) throws UserDataDuplicatedException {
        User user = userMapper.toUser(createUserDto);
        user.setId(UUID.randomUUID().toString());
        user = userService.saveUser(user);
        return userMapper.toUserDto(user);
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDto updateUserDto) throws UserNotFoundException, UserDataDuplicatedException {
        User user = userService.validateAndGetUserById(id.toString());
        userMapper.updateUserFromDto(updateUserDto, user);
        user = userService.saveUser(user);
        return userMapper.toUserDto(user);
    }

    @DeleteMapping("/users/{id}")
    public UserDto deleteUser(@PathVariable UUID id) throws UserNotFoundException {
        User user = userService.validateAndGetUserById(id.toString());
        userService.deleteUser(user);
        return userMapper.toUserDto(user);
    }

}
