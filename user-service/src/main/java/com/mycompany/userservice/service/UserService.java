package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserDataDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;

import java.util.List;

public interface UserService {

    User saveUser(User user) throws UserDataDuplicatedException;

    void deleteUser(User user);

    List<User> getAllUsers();

    User validateAndGetUserById(String id) throws UserNotFoundException;

    User validateAndGetUserByUsername(String username) throws UserNotFoundException;

}
