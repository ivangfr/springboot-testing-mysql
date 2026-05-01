package com.ivanfranchin.userservice.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ivanfranchin.userservice.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);
}
