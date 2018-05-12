package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

    User findUserById(String id);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

}
