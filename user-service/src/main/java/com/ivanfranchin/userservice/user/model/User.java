package com.ivanfranchin.userservice.user.model;

import com.ivanfranchin.userservice.user.dto.CreateUserRequest;
import com.ivanfranchin.userservice.user.dto.UpdateUserRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "UK_email", columnNames = "email"),
        @UniqueConstraint(name = "UK_username", columnNames = "username")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private LocalDate birthday;

    @Column(nullable = false)
    private Instant createdOn;

    @Column(nullable = false)
    private Instant updatedOn;

    public User(String username, String email, LocalDate birthday) {
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

    @PrePersist
    public void onPrePersist() {
        createdOn = updatedOn = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedOn = Instant.now();
    }

    public static User from(CreateUserRequest createUserRequest) {
        return new User(
                createUserRequest.username(),
                createUserRequest.email(),
                createUserRequest.birthday()
        );
    }

    public static void updateFromRequest(UpdateUserRequest updateUserRequest, User user) {
        if (updateUserRequest.username() != null) {
            user.setUsername(updateUserRequest.username());
        }
        if (updateUserRequest.email() != null) {
            user.setEmail(updateUserRequest.email());
        }
        if (updateUserRequest.birthday() != null) {
            user.setBirthday(updateUserRequest.birthday());
        }
    }
}