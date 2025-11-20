package com.auth;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class FinalCoverageTest {

    @After
    public void tearDown() {
        System.clearProperty("DB_USER");
        System.clearProperty("DB_URL");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_DRIVER");
    }

    @Test
    public void envHelper_sysPropertyWins_and_emptySysUsesFallback() {
        System.setProperty("DB_USER", "sysuser");
        String v = DbConnection.getEnvOrDefaultUsingValue("DB_USER", "fallback", "envuser");
        assertEquals("sysuser", v);

        System.setProperty("DB_USER", "   ");
        String v2 = DbConnection.getEnvOrDefaultUsingValue("DB_USER", "fallback", "envuser");
        // blank sys property -> use envValue (not fallback)
        assertEquals("envuser", v2);
    }

    @Test
    public void envHelper_envValueAndNullAndEmptyHandled() {
        System.clearProperty("DB_USER");
        // envValue present
        String v = DbConnection.getEnvOrDefaultUsingValue("DB_USER", "fallback", "envuser");
        assertEquals("envuser", v);

        // envValue null -> fallback
        String v2 = DbConnection.getEnvOrDefaultUsingValue("DB_USER", "fallback", null);
        assertEquals("fallback", v2);

        // envValue empty -> fallback
        String v3 = DbConnection.getEnvOrDefaultUsingValue("DB_USER", "fallback", "   ");
        assertEquals("fallback", v3);
    }

    @Test
    public void emailValidator_edgeCases() {
        assertFalse(EmailValidator.isValid(null));
        assertFalse(EmailValidator.isValid("plainaddress"));
        assertFalse(EmailValidator.isValid("missingdot@domain"));
        assertTrue(EmailValidator.isValid("a@b.com"));
    }

    @Test
    public void passwordValidator_variousLengthsAndChars() {
        assertFalse(PasswordValidator.isValid(null));
        assertFalse(PasswordValidator.isValid("Ab!")); // too short
        assertFalse(PasswordValidator.isValid("ABCDEFGHIJK!")); // too long

        // length 5 with uppercase and special -> valid
        assertTrue(PasswordValidator.isValid("Aab!5"));

        // length 10 with uppercase and special -> valid
        assertTrue(PasswordValidator.isValid("Abcdef!GHJ"));

        // missing uppercase
        assertFalse(PasswordValidator.isValid("abcdef!"));

        // missing special
        assertFalse(PasswordValidator.isValid("Abcdefgh"));
    }
}
