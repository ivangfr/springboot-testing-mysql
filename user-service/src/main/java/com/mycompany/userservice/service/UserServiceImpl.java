package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserDataDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) throws UserDataDuplicatedException {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserDataDuplicatedException("The username and/or email informed already exists.");
        }
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
    public User validateAndGetUserById(String id) throws UserNotFoundException {
        return userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id '%s' doesn't exist.", id)));
    }

    @Override
    public User validateAndGetUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with username '%s' doesn't exist.", username)));
    }

}
