package com.mycompany.springboottestingmysql.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class User {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private Date birthday;

    public User(String id, String username, String email, Date birthday) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }
}
