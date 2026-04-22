package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionTest {

    @Test
    void testConstructorThrowsUnsupportedOperationException() throws Exception {
        Constructor<DatabaseConnection> constructor = DatabaseConnection.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void testCloseConnectionWithNullDoesNotThrow() {
        assertDoesNotThrow(() -> DatabaseConnection.closeConnection(null));
    }

    @Test
    void testCloseConnectionHandlesSQLException() {
        Connection failingConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        throw new SQLException("close failed");
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        assertDoesNotThrow(() -> DatabaseConnection.closeConnection(failingConnection));
    }

    @Test
    void testCloseConnectionSuccessPath() {
        AtomicBoolean closeCalled = new AtomicBoolean(false);
        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        closeCalled.set(true);
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        DatabaseConnection.closeConnection(connection);
        assertTrue(closeCalled.get());
    }

    @Test
    void testChooseValueWhenEnvironmentValueExists() {
        assertEquals("db-url", DatabaseConnection.chooseValue("db-url", "default-url"));
        assertEquals("db-user", DatabaseConnection.chooseValue("db-user", "root"));
        assertEquals("db-pass", DatabaseConnection.chooseValue("db-pass", "root"));
    }

    @Test
    void testChooseValueWhenEnvironmentValueMissing() {
        assertEquals("default-url", DatabaseConnection.chooseValue(null, "default-url"));
        assertEquals("root", DatabaseConnection.chooseValue(null, "root"));
    }

    @Test
    void testGetConnectionThrowsWhenDriverMissing() {
        SQLException exception = assertThrows(SQLException.class,
                () -> DatabaseConnection.getConnection("no.such.DriverClass", "jdbc:mariadb://localhost:3306/x", "u", "p"));
        assertTrue(exception.getMessage().contains("MariaDB JDBC Driver not found"));
    }

    @Test
    void testGetConnectionThrowsWhenUrlInvalid() {
        assertThrows(SQLException.class,
                () -> DatabaseConnection.getConnection("org.mariadb.jdbc.Driver", "jdbc:mariadb://127.0.0.1:1/not_exist", "u", "p"));
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
