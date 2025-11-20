package com.auth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AppConstructorTest {

    @Test
    public void instantiateAppConstructor() {
        App a = new App();
        assertNotNull(a);
    }
}
