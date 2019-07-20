package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findUserById(String id);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

}
