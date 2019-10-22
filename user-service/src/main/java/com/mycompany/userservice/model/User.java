package com.mycompany.userservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private LocalDate birthday;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdOn;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", updatable = false)
    private ZonedDateTime updatedOn;

    public User(String id, String username, String email, LocalDate birthday) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

}