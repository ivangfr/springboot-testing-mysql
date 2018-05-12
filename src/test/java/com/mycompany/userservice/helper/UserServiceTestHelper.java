package com.mycompany.userservice.helper;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;

import java.util.Date;

import static com.mycompany.userservice.util.MyLocalDateHandler.fromStringToDate;

public class UserServiceTestHelper {

    /* Default User Values */

    private static final String ID = "d8bcc132-c704-4d21-b05f-9557d7fc3d91";
    private static final String USERNAME = "ivan";
    private static final String EMAIL = "ivan@test";
    private static final String BIRTHDAY = "01-01-2018";

    /* User */

    public static User getDefaultUser() {
        return getAnUser(ID, USERNAME, EMAIL, BIRTHDAY);
    }

    public static User getAnUser(String id, String username, String email, String birthdayStr) {
        Date birthday = fromStringToDate(birthdayStr);
        return new User(id, username, email, birthday);
    }

    /* UserDto */

    public static UserDto getDefaultUserDto() {
        return getAnUserDto(ID, USERNAME, EMAIL, BIRTHDAY);
    }

    public static UserDto getAnUserDto(String id, String username, String email, String birthdayStr) {
        Date birthday = fromStringToDate(birthdayStr);
        return new UserDto(id, username, email, birthday);
    }

    /* UpdateUserDto */

    public static UpdateUserDto getDefaultUpdateUserDto() {
        return getAnUpdateUserDto(USERNAME, EMAIL, BIRTHDAY);
    }

    public static UpdateUserDto getAnUpdateUserDto(String username, String email, String birthdayStr) {
        Date birthday = fromStringToDate(birthdayStr);
        return new UpdateUserDto(username, email, birthday);
    }

    /* CreateUserDto */

    public static CreateUserDto getDefaultCreateUserDto() {
        return getAnCreateUserDto(USERNAME, EMAIL, BIRTHDAY);
    }

    public static CreateUserDto getAnCreateUserDto(String username, String email, String birthdayStr) {
        Date birthday = fromStringToDate(birthdayStr);
        return new CreateUserDto(username, email, birthday);
    }

}
