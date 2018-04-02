package com.mycompany.springboottestingmysql.exception;

public class UserUsernameDuplicatedException extends Exception {

    public UserUsernameDuplicatedException(String message) {
        super(message);
    }
}
