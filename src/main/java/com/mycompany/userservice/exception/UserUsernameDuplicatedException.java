package com.mycompany.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserUsernameDuplicatedException extends Exception {

    public UserUsernameDuplicatedException(String message) {
        super(message);
    }
}
