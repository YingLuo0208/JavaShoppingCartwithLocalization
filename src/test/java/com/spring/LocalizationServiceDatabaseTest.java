package com.spring;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceDatabaseTest {

    @Test
    void testLoadMessagesFromDatabaseReturnsRows() throws Exception {
        TestLocalizationService service = new TestLocalizationService(createConnectionForMessages(true), null);
        Map<String, String> messages = invokeLoadMessagesFromDatabase(service, "fi");
        assertEquals("Kokonaishinta:", messages.get("total.cost"));
        assertEquals("Hinta", messages.get("enter.price"));
    }

    @Test
    void testLoadMessagesFromDatabaseFallsBackOnSQLException() throws Exception {
        TestLocalizationService service = new TestLocalizationService(null, new SQLException("db down"));
        Map<String, String> messages = invokeLoadMessagesFromDatabase(service, "fi");
        assertEquals("Total cost: ", messages.get("total.cost"));
        assertTrue(messages.containsKey("enter.price"));
    }

    @Test
    void testLoadMessagesFallsBackToDatabaseWhenBundleMissing() {
        TestLocalizationService service = new TestLocalizationService(createConnectionForMessages(true), null);
        service.throwMissingBundle = true;
        Map<String, String> messages = service.loadMessages("fi");
        assertEquals("Kokonaishinta:", messages.get("total.cost"));
        assertEquals("Hinta", messages.get("enter.price"));
    }

    @Test
    void testLoadMessagesUsesBundleWhenPresent() {
        TestLocalizationService service = new TestLocalizationService(createConnectionForMessages(true), null);
        service.customBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                        {"total.cost", "Bundle Total"},
                        {"enter.price", "Bundle Price"}
                };
            }
        };

        Map<String, String> messages = service.loadMessages("fi");
        assertEquals("Bundle Total", messages.get("total.cost"));
        assertEquals("Bundle Price", messages.get("enter.price"));
    }

    @Test
    void testGetMessageReturnsDbValue() {
        TestLocalizationService service = new TestLocalizationService(createConnectionForSingleMessage(true), null);
        assertEquals("Price", service.getMessage("enter.price", "en"));
    }

    @Test
    void testGetMessageReturnsKeyWhenMissing() {
        TestLocalizationService service = new TestLocalizationService(createConnectionForSingleMessage(false), null);
        assertEquals("enter.unknown", service.getMessage("enter.unknown", "en"));
    }

    @Test
    void testGetMessageReturnsKeyOnSQLException() {
        TestLocalizationService service = new TestLocalizationService(null, new SQLException("query failed"));
        assertEquals("total.cost", service.getMessage("total.cost", "en"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> invokeLoadMessagesFromDatabase(LocalizationService service, String languageCode) throws Exception {
        Method method = LocalizationService.class.getDeclaredMethod("loadMessagesFromDatabase", String.class);
        method.setAccessible(true);
        try {
            return (Map<String, String>) method.invoke(service, languageCode);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private Connection createConnectionForMessages(boolean withRows) {
        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                new java.lang.reflect.InvocationHandler() {
                    private int idx = 0;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("next".equals(method.getName())) {
                            if (!withRows) {
                                return false;
                            }
                            idx++;
                            return idx <= 2;
                        }
                        if ("getString".equals(method.getName())) {
                            String col = (String) args[0];
                            if ("key".equals(col)) {
                                return idx == 1 ? "total.cost" : "enter.price";
                            }
                            return idx == 1 ? "Kokonaishinta:" : "Hinta";
                        }
                        return defaultValue(method.getReturnType());
                    }
                }
        );

        return createConnectionReturningResultSet(resultSet);
    }

    private Connection createConnectionForSingleMessage(boolean hasRow) {
        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                new java.lang.reflect.InvocationHandler() {
                    private boolean consumed = false;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("next".equals(method.getName())) {
                            if (consumed) {
                                return false;
                            }
                            consumed = true;
                            return hasRow;
                        }
                        if ("getString".equals(method.getName())) {
                            return "Price";
                        }
                        return defaultValue(method.getReturnType());
                    }
                }
        );

        return createConnectionReturningResultSet(resultSet);
    }

    private Connection createConnectionReturningResultSet(ResultSet resultSet) {
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("executeQuery".equals(method.getName())) {
                        return resultSet;
                    }
                    if ("setString".equals(method.getName()) || "close".equals(method.getName())) {
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

    private static class TestLocalizationService extends LocalizationService {
        private final Connection connection;
        private final SQLException connectionException;
        private boolean throwMissingBundle;
        private ResourceBundle customBundle;

        private TestLocalizationService(Connection connection, SQLException connectionException) {
            this.connection = connection;
            this.connectionException = connectionException;
        }

        @Override
        protected Connection getConnection() throws SQLException {
            if (connectionException != null) {
                throw connectionException;
            }
            return connection;
        }

        @Override
        protected ResourceBundle getBundle(java.util.Locale locale) {
            if (throwMissingBundle) {
                throw new MissingResourceException("missing", "MessagesBundle", "MessagesBundle");
            }
            if (customBundle != null) {
                return customBundle;
            }
            return super.getBundle(locale);
        }
    }
}
