package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    void deleteUser(User user);

    List<User> getAllUsers();

    User getUserById(String id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User validateAndGetUserById(String id) throws UserNotFoundException;

    User validateAndGetUserByUsername(String username) throws UserNotFoundException;

    void validateUserExistsByUsername(String username) throws UserUsernameDuplicatedException;

    void validateUserExistsByEmail(String email) throws UserEmailDuplicatedException;

}
