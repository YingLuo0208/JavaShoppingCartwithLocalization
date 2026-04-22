package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceInternalTest {

    private final CartService cartService = new CartService();

    @Test
    void testInsertCartRecordSuccess() throws Exception {
        Connection connection = createConnectionForInsertCartRecord(1, true, 101);
        int id = invokeInsertCartRecord(connection, 2, 30.0, "en");
        assertEquals(101, id);
    }

    @Test
    void testInsertCartRecordThrowsWhenNoRowsAffected() {
        Connection connection = createConnectionForInsertCartRecord(0, true, 101);
        SQLException exception = assertThrows(SQLException.class, () -> invokeInsertCartRecord(connection, 2, 30.0, "en"));
        assertTrue(exception.getMessage().contains("no rows affected"));
    }

    @Test
    void testInsertCartRecordThrowsWhenNoGeneratedId() {
        Connection connection = createConnectionForInsertCartRecord(1, false, 0);
        SQLException exception = assertThrows(SQLException.class, () -> invokeInsertCartRecord(connection, 2, 30.0, "en"));
        assertTrue(exception.getMessage().contains("no ID obtained"));
    }

    @Test
    void testInsertCartItemsSuccess() throws Exception {
        AtomicInteger addBatchCalls = new AtomicInteger(0);
        Connection connection = createConnectionForInsertItems(addBatchCalls);

        List<CartItem> items = List.of(
                new CartItem(10.0, 1, 10.0),
                new CartItem(5.0, 2, 10.0)
        );

        invokeInsertCartItems(connection, 1, items);
        assertEquals(2, addBatchCalls.get());
    }

    @Test
    void testInsertCartItemsThrowsForNullItem() {
        AtomicInteger addBatchCalls = new AtomicInteger(0);
        Connection connection = createConnectionForInsertItems(addBatchCalls);

        List<CartItem> items = Arrays.asList(new CartItem(10.0, 1, 10.0), null);

        SQLException exception = assertThrows(SQLException.class, () -> invokeInsertCartItems(connection, 1, items));
        assertTrue(exception.getMessage().contains("cannot be null"));
        assertEquals(1, addBatchCalls.get());
    }

    private int invokeInsertCartRecord(Connection connection, int totalItems, double totalCost, String language) throws Exception {
        Method method = CartService.class.getDeclaredMethod("insertCartRecord", Connection.class, int.class, double.class, String.class);
        method.setAccessible(true);
        try {
            return (int) method.invoke(cartService, connection, totalItems, totalCost, language);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }
            throw e;
        }
    }

    private void invokeInsertCartItems(Connection connection, int cartRecordId, List<CartItem> items) throws Exception {
        Method method = CartService.class.getDeclaredMethod("insertCartItems", Connection.class, int.class, List.class);
        method.setAccessible(true);
        try {
            method.invoke(cartService, connection, cartRecordId, items);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            }
            throw e;
        }
    }

    private Connection createConnectionForInsertCartRecord(int affectedRows, boolean hasGeneratedKey, int generatedKey) {
        ResultSet resultSet = createGeneratedKeysResultSet(hasGeneratedKey, generatedKey);
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("executeUpdate".equals(methodName)) {
                        return affectedRows;
                    }
                    if ("getGeneratedKeys".equals(methodName)) {
                        return resultSet;
                    }
                    if ("setInt".equals(methodName) || "setDouble".equals(methodName)
                            || "setString".equals(methodName) || "setTimestamp".equals(methodName)
                            || "close".equals(methodName)) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return statement;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private Connection createConnectionForInsertItems(AtomicInteger addBatchCalls) {
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("addBatch".equals(methodName)) {
                        addBatchCalls.incrementAndGet();
                        return null;
                    }
                    if ("executeBatch".equals(methodName)) {
                        return new int[]{addBatchCalls.get()};
                    }
                    if ("setInt".equals(methodName) || "setDouble".equals(methodName) || "close".equals(methodName)) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return statement;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private ResultSet createGeneratedKeysResultSet(boolean hasGeneratedKey, int generatedKey) {
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                new java.lang.reflect.InvocationHandler() {
                    private boolean nextCalled;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String methodName = method.getName();
                        if ("next".equals(methodName)) {
                            if (!nextCalled) {
                                nextCalled = true;
                                return hasGeneratedKey;
                            }
                            return false;
                        }
                        if ("getInt".equals(methodName)) {
                            return generatedKey;
                        }
                        if ("close".equals(methodName)) {
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
                }
        );
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
