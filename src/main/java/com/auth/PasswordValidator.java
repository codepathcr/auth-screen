package com.auth;

public class PasswordValidator {

    private static final String ESPECIALES = "!@#$%^&*()_+-={}[]|:;\"'<>,.?/";

    public static boolean isValid(String password) {
        if (password == null) return false;
        if (password.length() < 5 || password.length() > 10) return false;

        boolean tieneMayuscula = false;
        boolean tieneEspecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            }
            if (ESPECIALES.indexOf(c) >= 0) {
                tieneEspecial = true;
            }
        }

        return tieneMayuscula && tieneEspecial;
    }
}
