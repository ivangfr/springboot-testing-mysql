package com.ivanfranchin.userservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.ZonedDateTime;

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
    private ZonedDateTime createdOn;

    @Column(nullable = false)
    private ZonedDateTime updatedOn;

    public User(String username, String email, LocalDate birthday) {
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

    @PrePersist
    public void onPrePersist() {
        createdOn = updatedOn = ZonedDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedOn = ZonedDateTime.now();
    }
}