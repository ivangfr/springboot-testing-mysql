package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserById(String id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

}
