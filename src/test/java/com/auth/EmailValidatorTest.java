package com.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class EmailValidatorTest {

    @Test
    public void validEmail() {
        assertTrue(EmailValidator.isValid("user@example.com"));
    }

    @Test
    public void nullEmail() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    public void missingAt() {
        assertFalse(EmailValidator.isValid("userexample.com"));
    }

    @Test
    public void missingDot() {
        assertFalse(EmailValidator.isValid("user@examplecom"));
    }

    @Test
    public void emptyEmail() {
        assertFalse(EmailValidator.isValid(""));
    }
}
