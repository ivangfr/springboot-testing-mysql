# springboot-testing-mysql

The goals of this project are:
- Create a simple [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) REST API to manage users called `user-service`. The database used is [`MySQL`](https://www.mysql.com);
- Explore the utilities and annotations that `Spring Boot` provides for testing applications;
- Test using [`Testcontainers`](https://www.testcontainers.org/);
- Test using [`Postman`](https://www.getpostman.com) and [`Newman`](https://github.com/postmanlabs/newman).

## Prerequisites

- `Java 11+`
- `Docker`
- `Docker-Compose`

## Start environment

- Open a terminal and inside `springboot-testing-mysql` root folder run
  ```
  docker-compose up -d
  ```

- Wait a little bit until `MySQL` is `Up (healthy)`. You can check it by running
  ```
  docker-compose ps
  ```

## Start application

- In a terminal, make sure you are in `springboot-testing-mysql` root folder

- Initialize `MySQL` database
  ```
  ./init-db.sh
  ```

- Run application
  ```
  ./gradlew user-service:clean user-service:bootRun
  ```

- Swagger website is http://localhost:8080/swagger-ui.html

## Testing with Postman and Newman

- Before start, make sure `user-service` application is running

- Open a terminal and navigate to `springboot-testing-mysql` root folder

- For this test, `users` table must be empty. Run the script below to re-start it
  ```
  ./init-db.sh
  ```

- Export to `HOST_IP_ADDR` environment variable the ip address of your machine
  > **Note:** The ip address can be obtained by executing `ifconfig` command on Mac/Linux terminal or `ipconfig` on Windows
  ```
  export HOST_IP_ADDR=...
  ```

- \[Optional\] In `springboot-testing-mysql/postman` folder there is a pre-defined `Postman` testing collection for `user-service`. You can import and edit it in `Postman`

- Run `Newman` docker container
  ```
  docker run -t --rm --name newman \
    -v $PWD/postman:/etc/newman \
    postman/newman:5.1.0-alpine \
    run UserService.postman_collection.json --global-var "USER_SERVICE_ADDR=$HOST_IP_ADDR"
  ```

## Shutdown

- Go to the terminal where `user-service` is running and press `Ctrl+C`

- In a terminal and inside `springboot-testing-mysql` root folder, run to command below to stop and remove containers, networks and volumes
  ```
  docker-compose down -v
  ```

## Running Unit and Integration Testing

- In a terminal, navigate to `springboot-testing-mysql` root folder

- Run unit and integration tests
  ```
  ./gradlew user-service:clean user-service:assemble \
    user-service:cleanTest \
    user-service:test \
    user-service:integrationTest
  ```
  > During the tests, `Testcontainers` starts automatically `MySQL` docker container before the tests begin and shuts it down when the tests finish.

- **Unit Testing Report** can be found at
  ```
  user-service/build/reports/tests/test/index.html
  ```

- **Integration Testing Report** can be found at
  ```
  user-service/build/reports/tests/integrationTest/index.html
  ```
  
## Util Commands

- **MySQL**
  ```
  docker exec -it mysql mysql -uroot -psecret --database userdb
  select * from users;
  ```

## References

- https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
- http://www.baeldung.com/spring-boot-testing
