# springboot-testing-mysql

The goals of this project are to:
- Create a simple [`Spring Boot`](https://docs.spring.io/spring-boot/index.html) application to manage users called `user-service`. The database used is [`MySQL`](https://www.mysql.com);
- Explore the utilities and annotations that `Spring Boot` provides for testing applications;
- Test using [`Testcontainers`](https://testcontainers.com).

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in by using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

## Project Diagram

![project-diagram](documentation/project-diagram.jpeg)

## Application

- ### user-service

  `Spring Boot` Web Java application to manage users. The data is stored in `MySQL`.
  
  ![user-service-swagger](documentation/user-service-swagger.jpeg)

## Prerequisites

- [`Java 21`](https://www.oracle.com/java/technologies/downloads/#java21) or higher;
- A containerization tool (e.g., [`Docker`](https://www.docker.com), [`Podman`](https://podman.io), etc.)

## Start Environment

- Open a terminal and inside the `springboot-testing-mysql` root folder run:
  ```
  docker compose up -d
  ```

- Wait for the `MySQL` Docker container to be up and running. To check it, run:
  ```
  docker ps -a
  ```

## Start Application

- In a terminal, make sure you are in the `springboot-testing-mysql` root folder;

- Run application:
  ```
  ./gradlew user-service:clean user-service:bootRun
  ```

- The Swagger website can be accessed at http://localhost:8080/swagger-ui.html

## Shutdown

- Go to the terminal where `user-service` is running and press `Ctrl+C`;

- In a terminal, inside the `springboot-testing-mysql` root folder, run the command below to stop and remove the Docker Compose `mysql` container and network:
  ```
  docker compose down -v
  ```

## Running Unit and Integration Tests

- In a terminal, navigate to the `springboot-testing-mysql` root folder;

- Running Tests

  - Unit Tests only:
    ```
    ./gradlew user-service:clean user-service:cleanTest user-service:test
    ```

  - Unit and Integration Tests:
    ```
    ./gradlew user-service:clean user-service:cleanTest user-service:check user-service:integrationTest
    ```
    > **Note**: During the tests, `Testcontainers` automatically starts the `MySQL` Docker container before the tests begin and shuts it down when the tests finish.

- **Unit Test Report** can be found at:
  ```
  user-service/build/reports/tests/test/index.html
  ```

- **Integration Test Report** can be found at:
  ```
  user-service/build/reports/tests/integrationTest/index.html
  ```

## Useful Commands

- **MySQL**
  ```
  docker exec -it -e MYSQL_PWD=secret mysql mysql -uroot --database userdb
  SELECT * FROM users;
  ```
