package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindUserByUsernameWhenExistent() {
        User user = entityManager.persist(getDefaultUser());

        Optional<User> userOptional = userRepository.findUserByUsername(user.getUsername());

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByUsernameWhenNonExistent() {
        Optional<User> userOptional = userRepository.findUserByUsername("ivan2");
        assertThat(userOptional).isNotPresent();
    }

    @Test
    void testFindUserByEmailWhenExistent() {
        User user = entityManager.persist(getDefaultUser());

        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByEmailWhenNonExistent() {
        Optional<User> userOptional = userRepository.findUserByEmail("ivan2@test");
        assertThat(userOptional).isNotPresent();
    }

    private User getDefaultUser() {
        return new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    }
}
