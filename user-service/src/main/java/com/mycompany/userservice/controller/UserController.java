package com.mycompany.userservice.controller;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        log.info("Get all users");

        return userService.getAllUsers()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/users/username/{username}")
    public UserDto getUserByUsername(@PathVariable String username) throws UserNotFoundException {
        log.info("Get user with username '{}'", username);

        User user = userService.validateAndGetUserByUsername(username);

        return modelMapper.map(user, UserDto.class);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto createUser(@Valid @RequestBody CreateUserDto createUserDto)
            throws UserUsernameDuplicatedException, UserEmailDuplicatedException {
        log.info("Post request to create user {}", createUserDto);

        userService.validateUserExistsByUsername(createUserDto.getUsername());
        userService.validateUserExistsByEmail(createUserDto.getEmail());

        User user = modelMapper.map(createUserDto, User.class);
        user.setId(UUID.randomUUID().toString());
        user = userService.saveUser(user);

        log.info("CREATED {}", user);
        return modelMapper.map(user, UserDto.class);
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDto updateUserDto)
            throws UserNotFoundException, UserUsernameDuplicatedException, UserEmailDuplicatedException {
        log.info("Put request to update user with id {}. New values: {}", id, updateUserDto);

        User user = userService.validateAndGetUserById(id.toString());

        String userUsername = user.getUsername();
        String updateUserDtoUsername = updateUserDto.getUsername();
        if (!StringUtils.isEmpty(updateUserDtoUsername) && !updateUserDtoUsername.equals(userUsername)) {
            userService.validateUserExistsByUsername(updateUserDtoUsername);
        }

        String userEmail = user.getEmail();
        String updateUserDtoEmail = updateUserDto.getEmail();
        if (!StringUtils.isEmpty(updateUserDtoEmail) && !updateUserDtoEmail.equals(userEmail)) {
            userService.validateUserExistsByEmail(updateUserDtoEmail);
        }

        modelMapper.map(updateUserDto, user);
        user = userService.saveUser(user);

        log.info("UPDATED {}", user);
        return modelMapper.map(user, UserDto.class);
    }

    @DeleteMapping("/users/{id}")
    public UserDto deleteUser(@PathVariable UUID id) throws UserNotFoundException {
        log.info("Delete request to remove user with id {}", id);

        User user = userService.validateAndGetUserById(id.toString());
        userService.deleteUser(user);

        log.info("DELETED {}", user);
        return modelMapper.map(user, UserDto.class);
    }

}
