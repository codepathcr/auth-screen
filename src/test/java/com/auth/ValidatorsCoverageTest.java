package com.auth;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidatorsCoverageTest {

    @Test
    public void emailValidator_nullAndBadAndGood() {
        assertFalse(EmailValidator.isValid(null));
        assertFalse(EmailValidator.isValid("no-at-sign.com"));
        assertFalse(EmailValidator.isValid("no-dot@com"));
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    public void passwordValidator_edgeCasesAndValid() {
        assertFalse(PasswordValidator.isValid(null));
        assertFalse(PasswordValidator.isValid("S!")); // too short
        assertFalse(PasswordValidator.isValid("VeryLongPWD!A")); // too long

        // Missing uppercase
        assertFalse(PasswordValidator.isValid("abc!de"));

        // Missing special
        assertFalse(PasswordValidator.isValid("ABCdef"));

        // Valid: length 6-10, contains uppercase and special
        assertTrue(PasswordValidator.isValid("Abc!12"));
    }
}
