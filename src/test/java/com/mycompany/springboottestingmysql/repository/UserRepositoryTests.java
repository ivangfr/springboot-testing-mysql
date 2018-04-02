package com.mycompany.springboottestingmysql.repository;

import com.mycompany.springboottestingmysql.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static com.mycompany.springboottestingmysql.helper.UserServiceTestHelper.getDefaultUser;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void given_oneUser_when_findUserByUsername_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserByUsername(user.getUsername());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_noUsers_when_findUserByUsernameUsingNonExistingUsername_then_returnNull() {
        User userFound = userRepository.findUserByUsername("ivan2");

        assertThat(userFound).isNull();
    }

    @Test
    public void given_oneUser_when_findUserById_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserById(user.getId());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_noUsers_when_findUserByIdUsingNonExistingId_then_returnNull() {
        User userFound = userRepository.findUserById("xyz");
        assertThat(userFound).isNull();
    }

    @Test
    public void given_oneUser_when_findUserByEmail_then_returnUser() {
        User user = getDefaultUser();
        entityManager.persist(user);

        User userFound = userRepository.findUserByEmail(user.getEmail());

        assertThat(userFound).isEqualToComparingFieldByField(user);
    }

    @Test
    public void given_noUsers_when_findUserByEmailUsingNonExistingEmail_then_returnNull() {
        User userFound = userRepository.findUserByEmail("ivan2@test");
        assertThat(userFound).isNull();
    }

}
