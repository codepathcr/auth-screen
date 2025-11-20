package com.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public String login(String email, String password) {
        try (Connection conn = DbConnection.getConnection()) {
            return loginWithConnection(conn, email, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error de BD: " + ex.getMessage();
        }
    }

    // Package-private for testing with an injected Connection
    String loginWithConnection(Connection conn, String email, String password) {
        if (!EmailValidator.isValid(email)) {
            return "Email no válido";
        }

        if (!PasswordValidator.isValid(password)) {
            return "Clave inválida: 5-10 chars, 1 mayúscula, 1 carácter especial";
        }

        try {
            String selectSql = "SELECT id, clave_hash, intentos_fallidos, bloqueado FROM usuarios WHERE email = ?";
            PreparedStatement ps = null;
            ResultSet rs = null;
            SQLException primaryEx = null;
            try {
                ps = conn.prepareStatement(selectSql);
                ps.setString(1, email);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    return "Usuario no encontrado";
                }

                int id = rs.getInt("id");
                String claveBD = rs.getString("clave_hash");
                int intentosFallidos = rs.getInt("intentos_fallidos");
                boolean bloqueado = rs.getBoolean("bloqueado");

                if (bloqueado) {
                    return "Cuenta bloqueada por intentos fallidos";
                }

                if (claveBD.equals(password)) {
                    resetIntentos(conn, id);
                    return "Login exitoso 🎉";
                } else {
                    intentosFallidos++;
                    boolean bloquear = intentosFallidos >= 5;
                    actualizarIntentos(conn, id, intentosFallidos, bloquear);
                    if (bloquear) {
                        return "Cuenta bloqueada. Excedió los 5 intentos.";
                    } else {
                        return "Clave incorrecta. Intentos: " + intentosFallidos + "/5";
                    }
                }
            } catch (SQLException e) {
                primaryEx = e;
                throw e;
            } finally {
                SQLException closeEx = null;
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        closeEx = e;
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        if (closeEx == null) {
                            closeEx = e;
                        } else {
                            // Intentionally do not chain suppressed exceptions here; keep the
                            // first close exception (closeEx) as the primary close error.
                        }
                    }
                }

                if (primaryEx != null) {
                    if (closeEx != null && primaryEx != closeEx) {
                        primaryEx.addSuppressed(closeEx);
                    }
                } else if (closeEx != null) {
                    throw closeEx;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error de BD: " + ex.getMessage();
        }
    }

    public String recoverPassword(String email) {
        try (Connection conn = DbConnection.getConnection()) {
            return recoverPasswordWithConnection(conn, email);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error de BD: " + ex.getMessage();
        }
    }

    // Package-private for testing with an injected Connection
    String recoverPasswordWithConnection(Connection conn, String email) {
        if (!EmailValidator.isValid(email)) {
            return "Ingrese un email válido para recuperar clave";
        }

        try {
            String sql = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement ps = null;
            ResultSet rs = null;
            SQLException primaryEx = null;
            try {
                ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    return "No existe un usuario con ese email";
                }
            } catch (SQLException e) {
                primaryEx = e;
                throw e;
            } finally {
                SQLException closeEx = null;
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        closeEx = e;
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        if (closeEx == null) {
                            closeEx = e;
                        } else {
                            // Intentionally do not chain suppressed exceptions here; keep the
                            // first close exception (closeEx) as the primary close error.
                        }
                    }
                }

                if (primaryEx != null) {
                    if (closeEx != null) {
                        primaryEx.addSuppressed(closeEx);
                    }
                } else if (closeEx != null) {
                    throw closeEx;
                }
            }

            enviarEmailRecuperacion(email);
            return "Se ha enviado un email de recuperación (simulado).";
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error de BD: " + ex.getMessage();
        }
    }

    private void enviarEmailRecuperacion(String email) {
        System.out.println("Simulando envío de email de recuperación a: " + email);
    }

    private void resetIntentos(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void actualizarIntentos(Connection conn, int userId, int intentos, boolean bloquear) throws SQLException {
        String sql = "UPDATE usuarios SET intentos_fallidos = ?, bloqueado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, intentos);
            ps.setBoolean(2, bloquear);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }
}
