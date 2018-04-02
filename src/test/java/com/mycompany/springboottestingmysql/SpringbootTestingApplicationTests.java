package com.mycompany.springboottestingmysql;

import com.mycompany.springboottestingmysql.controller.UserController;
import com.mycompany.springboottestingmysql.repository.UserRepository;
import com.mycompany.springboottestingmysql.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class SpringbootTestingApplicationTests {

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void contextLoads() {
        assertThat(userController).isNotNull();
        assertThat(userService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

}
