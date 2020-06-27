package com.mycompany.userservice.repository;

import com.mycompany.userservice.AbstractTestcontainers;
import com.mycompany.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTests extends AbstractTestcontainers {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenExistingUserUsernameWhenFindUserByUsernameThenReturnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        Optional<User> userOptional = userRepository.findUserByUsername(user.getUsername());

        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get()).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNonExistingUserUsernameWhenFindUserByUsernameUsingNonExistingUsernameThenReturnOptionalEmpty() {
        Optional<User> userOptional = userRepository.findUserByUsername("ivan2");
        assertThat(userOptional.isPresent()).isFalse();
    }

    @Test
    void givenExistingUserIdWhenFindUserByIdThenReturnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        Optional<User> userOptional = userRepository.findUserById(user.getId());

        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get()).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNonExistingUserIdWhenFindUserByIdUsingNonExistingIdThenReturnOptionalEmpty() {
        Optional<User> userOptional = userRepository.findUserById("xyz");
        assertThat(userOptional.isPresent()).isFalse();
    }

    @Test
    void givenExistingUserEmailWhenFindUserByEmailThenReturnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());

        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get()).isEqualToComparingFieldByField(user);
    }

    @Test
    void givenNonExistingUserEmailWhenFindUserByEmailUsingNonExistingEmailThenReturnOptionalEmpty() {
        Optional<User> userOptional = userRepository.findUserByEmail("ivan2@test");
        assertThat(userOptional.isPresent()).isFalse();
    }

}
