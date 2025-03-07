package com.ivanfranchin.userservice.user;

import com.ivanfranchin.userservice.user.exception.UserDataDuplicatedException;
import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserDataDuplicatedException();
        }
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User validateAndGetUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id '%s' doesn't exist.", id)));
    }

    public User validateAndGetUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with username '%s' doesn't exist.", username)));
    }
}
