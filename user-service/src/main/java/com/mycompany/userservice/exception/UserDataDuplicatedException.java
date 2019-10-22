package com.mycompany.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserDataDuplicatedException extends Exception {

    public UserDataDuplicatedException(String message) {
        super(message);
    }
}
