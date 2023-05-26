package com.ivanfranchin.userservice;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface MyContainers {

    @Container
    @ServiceConnection
    MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.32")
            .withUrlParam("characterEncoding", "UTF-8")
            .withUrlParam("serverTimezone", "UTC");
}
