package com.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class PasswordValidatorTest {

    @Test
    public void nullPassword() {
        assertFalse(PasswordValidator.isValid(null));
    }

    @Test
    public void tooShort() {
        assertFalse(PasswordValidator.isValid("A!a"));
    }

    @Test
    public void tooLong() {
        assertFalse(PasswordValidator.isValid("AbcdefGHIJ!"));
    }

    @Test
    public void noUppercase() {
        assertFalse(PasswordValidator.isValid("abcde!"));
    }

    @Test
    public void noSpecialChar() {
        assertFalse(PasswordValidator.isValid("Abcdef"));
    }

    @Test
    public void validPasswordMinLength() {
        assertTrue(PasswordValidator.isValid("Aab!5"));
    }

    @Test
    public void validPasswordMaxLength() {
        assertTrue(PasswordValidator.isValid("AbcDEfg!9"));
    }
}
