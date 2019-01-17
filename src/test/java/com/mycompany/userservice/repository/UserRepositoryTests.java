package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.mycompany.userservice.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void given_existingUserUsername_when_findUserByUsername_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_nonExistingUserUsername_when_findUserByUsernameUsingNonExistingUsername_then_returnNull() {
        User userFound = userRepository.findUserByUsername("ivan2");

        assertThat(userFound).isNull();
    }

    @Test
    void given_existingUserId_when_findUserById_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_nonExistingUserId_when_findUserByIdUsingNonExistingId_then_returnNull() {
        User userFound = userRepository.findUserById("xyz");
        assertThat(userFound).isNull();
    }

    @Test
    void given_existingUserEmail_when_findUserByEmail_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserByEmail(user.getEmail());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    void given_nonExistingUserEmail_when_findUserByEmailUsingNonExistingEmail_then_returnNull() {
        User userFound = userRepository.findUserByEmail("ivan2@test");
        assertThat(userFound).isNull();
    }

}
