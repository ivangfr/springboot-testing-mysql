package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.exception.UserUsernameDuplicatedException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User validateAndGetUserById(String id) throws UserNotFoundException {
        User user = getUserById(id);
        if (user == null) {
            String message = String.format("User with id '%s' doesn't exist.", id);
            throw new UserNotFoundException(message);
        }
        return user;
    }

    @Override
    public User validateAndGetUserByUsername(String username) throws UserNotFoundException {
        User user = getUserByUsername(username);
        if (user == null) {
            String message = String.format("User with username '%s' doesn't exist.", username);
            throw new UserNotFoundException(message);
        }
        return user;
    }

    @Override
    public void validateUserExistsByUsername(String username) throws UserUsernameDuplicatedException {
        User user = getUserByUsername(username);
        if (user != null) {
            String message = String.format("User with username '%s' already exists.", username);
            throw new UserUsernameDuplicatedException(message);
        }
    }

    @Override
    public void validateUserExistsByEmail(String email) throws UserEmailDuplicatedException {
        User user = getUserByEmail(email);
        if (user != null) {
            String message = String.format("User with email '%s' already exists.", email);
            throw new UserEmailDuplicatedException(message);
        }
    }
}
