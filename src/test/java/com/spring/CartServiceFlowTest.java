package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceFlowTest {

    @Test
    void testSaveCartSuccessPath() {
        Scenario scenario = new Scenario(false, false, false);
        TestCartService service = new TestCartService(scenario.createConnection());

        int id = service.saveCart(3, 25.0, "en", List.of(new CartItem(10, 2, 20), new CartItem(5, 1, 5)));

        assertEquals(42, id);
        assertTrue(scenario.commitCalled.get());
        assertFalse(scenario.rollbackCalled.get());
        assertTrue(service.closeCalled.get());
    }

    @Test
    void testSaveCartSQLExceptionRollsBack() {
        Scenario scenario = new Scenario(true, false, false);
        TestCartService service = new TestCartService(scenario.createConnection());

        int id = service.saveCart(1, 10.0, "en", List.of(new CartItem(10, 1, 10)));

        assertEquals(-1, id);
        assertTrue(scenario.rollbackCalled.get());
        assertFalse(scenario.commitCalled.get());
        assertTrue(service.closeCalled.get());
    }

    @Test
    void testSaveCartRollbackFailureStillReturnsMinusOne() {
        Scenario scenario = new Scenario(true, true, false);
        TestCartService service = new TestCartService(scenario.createConnection());

        int id = service.saveCart(1, 10.0, "en", List.of(new CartItem(10, 1, 10)));

        assertEquals(-1, id);
        assertTrue(scenario.rollbackAttempted.get());
    }

    @Test
    void testSaveCartFinallyHandlesAutoCommitResetFailure() {
        Scenario scenario = new Scenario(false, false, true);
        TestCartService service = new TestCartService(scenario.createConnection());

        int id = service.saveCart(1, 10.0, "en", List.of(new CartItem(10, 1, 10)));

        assertEquals(42, id);
        assertTrue(scenario.autoCommitTrueAttempted.get());
        assertFalse(service.closeCalled.get());
    }

    private static class TestCartService extends CartService {
        private final Connection connection;
        private final AtomicBoolean closeCalled = new AtomicBoolean(false);

        private TestCartService(Connection connection) {
            this.connection = connection;
        }

        @Override
        protected Connection getConnection() {
            return connection;
        }

        @Override
        protected void closeConnection(Connection conn) {
            closeCalled.set(true);
        }
    }

    private static class Scenario {
        private final boolean failOnItemInsert;
        private final boolean rollbackThrows;
        private final boolean setAutoCommitTrueThrows;

        private final AtomicBoolean commitCalled = new AtomicBoolean(false);
        private final AtomicBoolean rollbackCalled = new AtomicBoolean(false);
        private final AtomicBoolean rollbackAttempted = new AtomicBoolean(false);
        private final AtomicBoolean autoCommitTrueAttempted = new AtomicBoolean(false);
        private final AtomicInteger prepareCallCount = new AtomicInteger(0);

        private Scenario(boolean failOnItemInsert, boolean rollbackThrows, boolean setAutoCommitTrueThrows) {
            this.failOnItemInsert = failOnItemInsert;
            this.rollbackThrows = rollbackThrows;
            this.setAutoCommitTrueThrows = setAutoCommitTrueThrows;
        }

        private Connection createConnection() {
            PreparedStatement recordStmt = createRecordStatement();
            PreparedStatement itemStmt = createItemStatement();

            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setAutoCommit".equals(name)) {
                            boolean value = (boolean) args[0];
                            if (value) {
                                autoCommitTrueAttempted.set(true);
                                if (setAutoCommitTrueThrows) {
                                    throw new SQLException("setAutoCommit(true) failed");
                                }
                            }
                            return null;
                        }
                        if ("prepareStatement".equals(name)) {
                            int idx = prepareCallCount.incrementAndGet();
                            if (idx == 1 && args.length == 2) {
                                return recordStmt;
                            }
                            if (idx == 2 && args.length == 1) {
                                if (failOnItemInsert) {
                                    throw new SQLException("item insert failed");
                                }
                                return itemStmt;
                            }
                            throw new SQLException("Unexpected prepareStatement call");
                        }
                        if ("commit".equals(name)) {
                            commitCalled.set(true);
                            return null;
                        }
                        if ("rollback".equals(name)) {
                            rollbackAttempted.set(true);
                            if (rollbackThrows) {
                                throw new SQLException("rollback failed");
                            }
                            rollbackCalled.set(true);
                            return null;
                        }
                        return defaultValue(method);
                    }
            );
        }

        private PreparedStatement createRecordStatement() {
            ResultSet generatedKeys = (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class[]{ResultSet.class},
                    new java.lang.reflect.InvocationHandler() {
                        private boolean first = true;

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("next".equals(method.getName())) {
                                if (first) {
                                    first = false;
                                    return true;
                                }
                                return false;
                            }
                            if ("getInt".equals(method.getName())) {
                                return 42;
                            }
                            return defaultValue(method);
                        }
                    }
            );

            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("executeUpdate".equals(name)) {
                            return 1;
                        }
                        if ("getGeneratedKeys".equals(name)) {
                            return generatedKeys;
                        }
                        if ("setInt".equals(name)
                                || "setDouble".equals(name)
                                || "setString".equals(name)
                                || "setTimestamp".equals(name)
                                || "close".equals(name)) {
                            return null;
                        }
                        return defaultValue(method);
                    }
            );
        }

        private PreparedStatement createItemStatement() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setInt".equals(name)
                                || "setDouble".equals(name)
                                || "addBatch".equals(name)
                                || "close".equals(name)) {
                            return null;
                        }
                        if ("executeBatch".equals(name)) {
                            return new int[]{1};
                        }
                        return defaultValue(method);
                    }
            );
        }
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
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
