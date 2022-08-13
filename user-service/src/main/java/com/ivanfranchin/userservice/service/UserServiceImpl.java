package com.ivanfranchin.userservice.service;

import com.ivanfranchin.userservice.exception.UserNotFoundException;
import com.ivanfranchin.userservice.exception.UserDataDuplicatedException;
import com.ivanfranchin.userservice.model.User;
import com.ivanfranchin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserDataDuplicatedException();
        }
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User validateAndGetUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id '%s' doesn't exist.", id)));
    }

    @Override
    public User validateAndGetUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with username '%s' doesn't exist.", username)));
    }
}
