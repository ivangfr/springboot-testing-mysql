package com.mycompany.userservice.service;

import com.mycompany.userservice.model.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    void deleteUser(User user);

    List<User> getAllUsers();

    User validateAndGetUserById(String id);

    User validateAndGetUserByUsername(String username);

}
