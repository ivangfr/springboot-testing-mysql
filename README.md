# springboot-testing-mysql

## Goal

The goals of this project are:

1. Create a simple REST API to manage users, `user-service`;
2. Explore the utilities and annotations that Spring Boot provides when testing applications.

## Running the application

1. Inside `/springboot-testing-mysql/dev` folder run
```
docker-compose up
```

2. Open a new terminal and, inside `/springboot-testing-mysql/dev` folder, run the following script to initialize the MySQL database
```
./init-db.sh
```

3. Go to `/springboot-testing-mysql` folder and run the command to start the application
```
gradle bootRun
```

4. Access Swagger website: http://localhost:8080/swagger-ui.html

## Running unit and integration testing

1. In order to run unit and integration testing type
```
gradle test integrationTest
```

2. From `springboot-testing-mysql` root folder, unit testing report can be found in
```
/build/reports/tests/test/index.html
```

3. From `springboot-testing-mysql` root folder, integration testing report can be found in
```
/build/reports/tests/integrationTest/index.html
```

## More about testing Spring Boot Applications

Spring Boot provides a number of utilities and annotations to help when testing your application.

### Unit Testing

The idea of the unit testing is to test each layer of the application (repository, service and controller) individually.
The repository classes usually don't depends on any other classes, so we can write test cases without any mocking.
On the other hand, the services classes depend on repositories. So, as we have already test cases to cover the repositories, while writing test cases for the services we don't need to care about the quality of the repositories classes. So, every calls to repositories classes should be mocked.
The same happens to controller classes that depends on the services classes. While writing tests for the controllers, service calls on the controller classes should be mocked.

#### Repository Testing

The `@DataJpaTest` annotation can be used to test the repositories of the application.
By default, it configures an in-memory embedded database, scans for `@Entity` classes, and configures Spring Data JPA repositories.
Data JPA tests may also inject a `TestEntityManager` bean, which provides an alternative to the standard JPA EntityManager that is specifically designed for tests.
For example:

```
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTests {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository repository;

	// Tests ..
}
```

#### Service Testing

In order to test the application services, we can use a something similar as shown bellow, as we create an instance of `UserServiceImpl` and mock the `userRepository` 

```
@RunWith(SpringRunner.class)
public class UserServiceImplTests {

    private UserService userService;
    private UserRepository userRepository;

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }
    
    // Tests
}
```

#### Controller Testing

`@WebMvcTest` annotation can be used to test whether Spring MVC controllers are working as expected.
`@WebMvcTest` is limited to a single controller and is used in combination with `@MockBean` to provide mock implementations for required dependencies.
`@WebMvcTest` also auto-configures `MockMvc`. Mock MVC offers a powerful way to quickly test MVC controllers without needing to start a full HTTP server.
In the example bellow, you can see that we mocking the services (in this case `userService`) used by `UserController`.

```
@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    
    // Tests ... 
}
```

#### DTO Testing

`@JsonTest` annotation can be used to test whether object JSON serialization and deserialization is working as expected.
In the example bellow, it is used `JacksonTester`. However, `GsonTester`, `JsonbTester` and `BasicJsonTester` could also be used instead.
Btw, I've tried to use all of them, but just `JacksonTester` worked easily and as expected.  

```
@RunWith(SpringRunner.class)
@JsonTest
public class MyJsonTests {

	@Autowired
	private JacksonTester<VehicleDetails> json;

	// Tests ...
}
```

### Integration Testing

The main goal of the integration tests is, as its name suggests, to integrate the different layers of the application. Here, no mocking is involved and a full running HTTP server is needed. 
So, in order to have it, we can use the `@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)` annotation. What this annotation does is to start a full running server running in a random ports. Spring Boot also provides a `TestRestTemplate` facility, for example:

```
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RandomPortTestRestTemplateExampleTests {

	@Autowired
	private TestRestTemplate restTemplate;

	// Tests ...

}
```

Integration tests should run separated from the unit tests and, mainly, it should runs after unit tests. In this project, we created a new integrationTest Gradle task to handle exclusively integration tests.

### Sources:

- https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
- http://www.baeldung.com/spring-boot-testing
