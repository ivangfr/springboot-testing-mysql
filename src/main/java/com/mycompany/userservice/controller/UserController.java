package com.mycompany.userservice.controller;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Get all users");

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            userDtos.add(modelMapper.map(user, UserDto.class));
        }

        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    @GetMapping(value = "/users/username/{username}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) throws UserNotFoundException {
        logger.info("Get user with username '{}'", username);

        User user = userService.validateAndGetUserByUsername(username);

        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.OK);
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto)
            throws UserUsernameDuplicatedException, UserEmailDuplicatedException {
        logger.info("Post request to create user {}", createUserDto);

        userService.validateUserExistsByUsername(createUserDto.getUsername());
        userService.validateUserExistsByEmail(createUserDto.getEmail());

        UUID id = UUID.randomUUID();
        User user = modelMapper.map(createUserDto, User.class);
        user.setId(id.toString());
        user = userService.saveUser(user);

        logger.info("CREATED {}", user);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.CREATED);
    }

    @PutMapping(value = "/users/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDto updateUserDto)
            throws UserNotFoundException, UserUsernameDuplicatedException, UserEmailDuplicatedException {
        logger.info("Put request to update user with id {}. New values: {}", id, updateUserDto);

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

        logger.info("UPDATED {}", user);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.OK);
    }

    @DeleteMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDto> deleteUser(@PathVariable UUID id) throws UserNotFoundException {
        logger.info("Delete request to remove user with id {}", id);

        User user = userService.validateAndGetUserById(id.toString());
        userService.deleteUser(user);

        logger.info("DELETED {}", user);
        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.OK);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public void handleNotFoundException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler({UserUsernameDuplicatedException.class, UserEmailDuplicatedException.class})
    public void handleBadRequestException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
