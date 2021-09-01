package com.mycompany.userservice.service;

import com.mycompany.userservice.model.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    void deleteUser(User user);

    List<User> getUsers();

    User validateAndGetUserById(Long id);

    User validateAndGetUserByUsername(String username);

}
