package com.mycompany.springboottestingmysql.service;

import com.mycompany.springboottestingmysql.exception.UserEmailDuplicatedException;
import com.mycompany.springboottestingmysql.exception.UserNotFoundException;
import com.mycompany.springboottestingmysql.exception.UserUsernameDuplicatedException;
import com.mycompany.springboottestingmysql.model.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    List<User> getAllUsers();

    User getUserById(String id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User validateAndGetUserById(String id) throws UserNotFoundException;

    User validateAndGetUserByUsername(String username) throws UserNotFoundException;

    void validateUserExistsByUsername(String username) throws UserUsernameDuplicatedException;

    void validateUserExistsByEmail(String email) throws UserEmailDuplicatedException;

}
