package com.mycompany.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserDataDuplicatedException extends RuntimeException {

    public UserDataDuplicatedException() {
        super("The username and/or email informed already exists.");
    }
}
