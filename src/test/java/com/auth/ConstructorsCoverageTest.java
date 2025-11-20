package com.auth;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConstructorsCoverageTest {

    @Test
    public void instantiateValidators_toCoverDefaultConstructors() {
        EmailValidator ev = new EmailValidator();
        PasswordValidator pv = new PasswordValidator();
        assertNotNull(ev);
        assertNotNull(pv);
    }
}
