package com.mycompany.springboottestingmysql.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idxEmail", columnList = "email", unique = true),
        @Index(name = "idxUsername", columnList = "username", unique = true)})
public class User {

    @Id
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private Date birthday;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

//    H2 database doesn't have the ON UPDATE as MySQL has.
//    For this case, it would be nice to have a MySQL embedded DB!
//    However, I haven't found yet a good one.
//    ---
//    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", updatable = false)
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date updatedOn;

    public User(String id, String username, String email, Date birthday) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
    }

}