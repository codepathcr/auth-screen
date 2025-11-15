package com.auth;

public class EmailValidator {

    public static boolean isValid(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
