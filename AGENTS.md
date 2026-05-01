# AGENTS.md — springboot-testing-mysql

Guidance for agentic coding agents working in this repository.

---

## Project Overview

A Spring Boot 4.0.5 / Java 25 REST service (`user-service`) backed by MySQL.
Single Maven module under the `user-service/` directory. The parent `pom.xml`
at the root only handles module aggregation.

---

## Build & Run Commands

All commands use the Maven Wrapper from the repository root.

```bash
# Compile
./mvnw clean compile --projects user-service

# Run the application (requires MySQL; see docker-compose.yml)
./mvnw clean spring-boot:run --projects user-service

# Start MySQL via Docker Compose
docker compose up -d mysql
```

---

## Test Commands

Tests are split into two phases by naming convention:

| Suffix   | Maven phase | Plugin      | Description                  |
|----------|-------------|-------------|------------------------------|
| `*Tests` | `test`      | surefire    | Unit / slice tests           |
| `*IT`    | `verify`    | failsafe    | Full integration tests       |

```bash
# Run all unit/slice tests
./mvnw clean test --projects user-service

# Run all tests including integration tests
./mvnw clean verify --projects user-service

# Run a single test class
./mvnw test --projects user-service -Dtest=UserControllerTests

# Run a single test method
./mvnw test --projects user-service -Dtest=UserControllerTests#testGetUsersWhenThereIsNone

# Run a single integration test class
./mvnw verify --projects user-service -Dit.test=UserServiceApplicationIT

# Run a single integration test method
./mvnw verify --projects user-service -Dit.test=UserServiceApplicationIT#testCreateUser
```

Integration tests spin up a real MySQL instance via Testcontainers automatically —
no manual Docker setup is needed to run them.

---

## Module & Package Structure

```
user-service/src/main/java/com/ivanfranchin/userservice/
├── UserServiceApplication.java          # @SpringBootApplication entry point
├── config/
│   ├── MyErrorAttributes.java           # Custom error response body
│   └── SwaggerConfig.java               # OpenAPI / Swagger UI config
└── user/                                # Feature package (domain-first layout)
    ├── UserController.java
    ├── UserRepository.java
    ├── UserService.java
    ├── dto/
    │   ├── CreateUserRequest.java        # Java record, input DTO
    │   ├── UpdateUserRequest.java        # Java record, input DTO
    │   └── UserResponse.java            # Java record, output DTO
    ├── exception/
    │   ├── UserNotFoundException.java
    │   └── UserDataDuplicatedException.java
    └── model/
        └── User.java                    # JPA entity
```

**Packaging convention**: domain/feature-first, not layer-first.
New features get their own sub-package (e.g., `com.ivanfranchin.userservice.order/`).

---

## Code Style Guidelines

### General Formatting
- **Indentation**: 2 spaces (Google Java Format default, enforced by Spotless)
- **Braces**: K&R style — opening brace on the same line
- **Blank lines**: one blank line between methods
- **Line length**: 100 characters (enforced by .editorconfig)
- **No wildcard imports**: always use fully-qualified single-type imports (enforced by Spotless)
- **Import order**: `java.*` → `jakarta.*` → `org.*` → `com.*` (enforced by Spotless)
- **Automated formatting**: Run `./mvnw spotless:apply` to auto-format code

### Naming Conventions
- Classes: `PascalCase` (e.g., `UserController`, `CreateUserRequest`)
- Methods and variables: `camelCase` (e.g., `validateAndGetUserById`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `API_USERS_URL`)
- Input DTOs: `*Request` suffix (e.g., `CreateUserRequest`)
- Output DTOs: `*Response` suffix (e.g., `UserResponse`)
- Exceptions: `*Exception` suffix (e.g., `UserNotFoundException`)
- No `Impl` suffix — services are concrete classes with no interface

### DTOs
Use **Java records** for all request and response DTOs.

- **Create request DTOs** carry a `toDomain()` instance method that constructs and returns the domain entity:
  ```java
  public record CreateUserRequest(String username, String email, LocalDate birthday) {
      public User toDomain() {
          return new User(username(), email(), birthday());
      }
  }
  ```
- **Update request DTOs** carry a `hasChanges(Entity)` method that checks whether any field differs from the entity, and an `applyTo(Entity)` instance method that applies non-null fields to an existing entity:
  ```java
  public record UpdateUserRequest(String username, String email, LocalDate birthday) {
      public boolean hasChanges(User user) {
          return (username() != null && !username().equals(user.getUsername()))
                  || (email() != null && !email().equals(user.getEmail()))
                  || (birthday() != null && !birthday().equals(user.getBirthday()));
      }

      public void applyTo(User user) {
          if (username() != null) user.setUsername(username());
          if (email() != null)    user.setEmail(email());
          if (birthday() != null) user.setBirthday(birthday());
      }
  }
  ```
- **Response DTOs** carry a `from(Entity)` static factory:
  ```java
  public record UserResponse(Long id, String username, String email) {
      public static UserResponse from(User user) {
          return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
      }
  }
  ```

### Entities
- Annotate with `@Entity` and `@Table(name = "...")`.
- Unique constraints belong on `@Table(uniqueConstraints = {...})`, not only as
  `@Column(unique = true)`, so they can be named explicitly.
- Manage `createdOn` / `updatedOn` via `@PrePersist` / `@PreUpdate` lifecycle
  callbacks on the entity itself (not in the service).
- Do **not** put DTO-mapping methods on the entity. Instead:
  - Request DTOs carry a `toDomain()` instance method (e.g., `createUserRequest.toDomain()` returns a new `User`).
  - Response DTOs carry a `from(Entity)` static factory (e.g., `UserResponse.from(user)`).

### Dependency Injection
- Use **constructor injection** via Lombok `@RequiredArgsConstructor`.
- Field injection (`@Autowired`) is acceptable **only in test classes**.

### Lombok
Lombok is on the compile classpath. Prefer:
- `@Data` for simple mutable classes including entities (generates getters, setters, `equals`, `hashCode`, `toString`)
- `@RequiredArgsConstructor` for constructor injection
- `@NoArgsConstructor` / `@AllArgsConstructor` where needed by JPA or tests

---

## Error Handling

### Custom Exceptions
Extend `RuntimeException`. Apply `@ResponseStatus` with the appropriate HTTP
status directly on the exception class. No `@ControllerAdvice` needed.

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}
```

### Service-layer Exception Translation
Catch JPA/Spring exceptions (`DataIntegrityViolationException`) in the service
and re-throw as a domain exception:

```java
public User saveUser(User user) {
    try {
        return userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
        throw new UserDataDuplicatedException();
    }
}
```

### Validate-and-get Pattern
Service lookup methods must throw if the entity is not found:

```java
public User validateAndGetUserById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(
                    "User with id '" + id + "' doesn't exist."));
}
```

---

## Testing Guidelines

### Test Naming
Test methods follow `testVerbNounWhenCondition()`:
```
testGetUsersWhenThereIsNone()
testCreateUserInformingValidInfo()
testValidateAndGetUserByIdWhenNonExisting()
```

### Test Slices — Use the Right Annotation
| What to test             | Annotation                        | Notes                          |
|--------------------------|-----------------------------------|--------------------------------|
| Web/controller layer     | `@WebMvcTest(XController.class)`  | Uses `MockMvc`                 |
| JPA / repository layer   | `@DataJpaTest`                    | Uses Testcontainers MySQL      |
| JSON serialization       | `@JsonTest`                       | Uses `JacksonTester`           |
| Service (unit)           | `@ExtendWith(SpringExtension.class)` + `@Import(XService.class)` | Mocks repository |
| Full application         | `@SpringBootTest(webEnvironment = RANDOM_PORT)` | Uses `TestRestTemplate` |

### Testcontainers
A shared `MySQLTestcontainers` interface (top-level test package) holds the
`@Container @ServiceConnection MySQLContainer`. Reuse it via:

```java
@DataJpaTest
@ImportTestcontainers(MySQLTestcontainers.class)
class UserRepositoryTests { ... }
```

Do not create new `MySQLContainer` instances in individual test classes.

### Mocking
Use BDD-style Mockito from Spring Boot (`BDDMockito`):
```java
given(userService.validateAndGetUserById(userId)).willReturn(user);
given(userService.saveUser(any(User.class))).willThrow(new UserDataDuplicatedException());
willDoNothing().given(userService).deleteUser(user);
```
Use `@MockitoBean` (Spring Boot 4 replacement for `@MockBean`) to register mocks
in the Spring context.

### Assertions
Use **AssertJ** (`assertThat(...)`) for all assertions. Do not use JUnit 5's
`Assertions` class directly.

### Test Constants
Declare URL strings and JSON path constants as `private static final String` at
the end of the test class body:
```java
private static final String API_USERS_URL = "/api/users";
private static final String JSON_$_ID = "$.id";
```

### Database Isolation
In integration tests, call `userRepository.deleteAll()` in a `@BeforeEach`
method to guarantee a clean state before each test.

---

## Persistence Notes

- Schema management: `spring.jpa.hibernate.ddl-auto=update` (no Flyway/Liquibase)
- No explicit `@Transactional` in the service layer — rely on Spring Data JPA
  repository defaults
- Derived query method names only in repositories; avoid `@Query` unless
  strictly necessary

---

## Code Formatting Enforcement

This project uses **Spotless Maven Plugin** with **Google Java Format** for automated code formatting.

- **Auto-format code**: `./mvnw spotless:apply`
- **Check formatting**: `./mvnw spotless:check` (runs automatically during `./mvnw verify`)
- **`.editorconfig`** is present at the repository root for consistent editor behavior

The formatting rules are enforced automatically — no manual formatting is needed.
