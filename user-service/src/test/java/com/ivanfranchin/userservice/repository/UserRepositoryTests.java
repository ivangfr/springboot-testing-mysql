package com.ivanfranchin.userservice.repository;

import com.ivanfranchin.userservice.MySQLTestcontainers;
import com.ivanfranchin.userservice.user.UserRepository;
import com.ivanfranchin.userservice.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ImportTestcontainers(MySQLTestcontainers.class)
class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindUserByUsernameWhenExistent() {
        User user = entityManager.persist(getDefaultUser());

        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByUsernameWhenNonExistent() {
        Optional<User> userOptional = userRepository.findByUsername("ivan2");
        assertThat(userOptional).isNotPresent();
    }

    @Test
    void testFindUserByEmailWhenExistent() {
        User user = entityManager.persist(getDefaultUser());

        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());

        assertThat(userOptional).isPresent();
        assertThat(userOptional.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByEmailWhenNonExistent() {
        Optional<User> userOptional = userRepository.findByEmail("ivan2@test");
        assertThat(userOptional).isNotPresent();
    }

    @Test
    void testSaveUserWithDuplicateUsername() {
        entityManager.persist(getDefaultUser());
        entityManager.flush();

        User duplicate = new User("ivan", "other@test", LocalDate.parse("2018-01-01"));

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testSaveUserWithDuplicateEmail() {
        entityManager.persist(getDefaultUser());
        entityManager.flush();

        User duplicate = new User("ivan2", "ivan@test", LocalDate.parse("2018-01-01"));

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User getDefaultUser() {
        return new User("ivan", "ivan@test", LocalDate.parse("2018-01-01"));
    }
}
