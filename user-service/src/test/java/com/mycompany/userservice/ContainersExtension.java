package com.mycompany.userservice;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

@Testcontainers
public class ContainersExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private MySQLContainer mySQLContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        mySQLContainer = new MySQLContainer("mysql:8.0.20")
                .withDatabaseName("userdb-test")
                .withUsername("root-test")
                .withPassword("secret-test");
        mySQLContainer.setPortBindings(Collections.singletonList("33066:3306"));
        mySQLContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        mySQLContainer.stop();
    }
}
