# `springboot-testing-mysql`

The goals of this project are:

- Create a simple [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) REST API to
manage users called `user-service`. The database used is [`MySQL`](https://www.mysql.com);
- Explore the utilities and annotations that `Spring Boot` provides when testing applications.
- Testing with [`Postman`](https://www.getpostman.com) and [`Newman`](https://github.com/postmanlabs/newman)

## Start environment

Open a terminal and inside `springboot-testing-mysql` root folder run
```
docker-compose up -d
```

Wait a little bit until `MySQL` is `Up (healthy)`. You can check it by running
```
docker-compose ps
```

## Start application

First of all, we need to initialize `MySQL` database. For it, inside `springboot-testing-mysql` root folder, run the
following script 
```
./init-db.sh
```

Still inside `springboot-testing-mysql` root folder, run the command to start the application
```
./gradlew user-service:clean user-service:bootRun
```

The `user-service` endpoints can be access using Swagger website: http://localhost:8080/swagger-ui.html

## Testing with Postman and Newman

**IMPORTANT: `user-service` must be running and the `users` table must be empty**

- In the `springboot-testing-mysql/postman` folder there is a pre-defined `Postman` testing collection for
`user-service`. You can import and edit it in your `Postman`.

- Export to `HOST_IP_ADDR` environment variable the ip address of your machine
> the ip address can be obtained by executing `ifconfig` command on Mac/Linux terminal or `ipconfig` on Windows
```
export HOST_IP_ADDR=...
```

- Inside `springboot-testing-mysql` root folder, execute the following command to run `Newman` docker container
```
docker run -t --rm --name newman \
  -v $PWD/postman:/etc/newman \
  postman/newman_ubuntu1404:4.5.5 \
  run UserService.postman_collection.json --global-var "USER_SERVICE_ADDR=$HOST_IP_ADDR"
```

## Shutdown

Run to command below to stop and remove containers, networks and volumes
```
docker-compose down -v
```

## Running Unit and Integration Testing

In a terminal and inside `springboot-testing-mysql` root folder, run the command below to run unit and integration
tests
```
./gradlew user-service:cleanTest user-service:test user-service:integrationTest
```
> We are using a Gradle plugin that uses docker-compose to start a MySQL container that is required by the tests.

- From `springboot-testing-mysql` root folder, **Unit Testing Report** can be found at
```
user-service/build/reports/tests/test/index.html
```
- From `springboot-testing-mysql` root folder, **Integration Testing Report** can be found at
```
user-service/build/reports/tests/integrationTest/index.html
```

## References

- https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
- http://www.baeldung.com/spring-boot-testing
