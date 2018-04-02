package com.mycompany.springboottestingmysql.exception;

public class UserEmailDuplicatedException extends Exception {

    public UserEmailDuplicatedException(String message) {
        super(message);
    }
}
