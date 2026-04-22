package com.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Service class for fetching localized UI strings from the database.
 * 从数据库获取本地化UI字符串的服务类。
 */
public class LocalizationService {
    private static final Logger LOGGER = Logger.getLogger(LocalizationService.class.getName());

    /**
     * Load all localized messages for a given language.
     * 加载指定语言的所有本地化消息。
     *
     * @param languageCode language code (en, fi, sv, ja, ar)
     * @return Map of message keys to values
     */
    public Map<String, String> loadMessages(String languageCode) {
        Map<String, String> messages = new HashMap<>();
        
        try {
            // First try to load from ResourceBundle (.properties files)
            Locale locale = getLocaleFromCode(languageCode);
            ResourceBundle bundle = getBundle(locale);
            
            // Convert ResourceBundle to Map
            for (String key : bundle.keySet()) {
                messages.put(key, bundle.getString(key));
            }
            
            LOGGER.info("Loaded messages from ResourceBundle for language: " + languageCode);
            return messages;
            
        } catch (MissingResourceException e) {
            LOGGER.severe("Error loading from ResourceBundle: " + e.getMessage());
            // Fall back to database
            return loadMessagesFromDatabase(languageCode);
        }
    }
    
    /**
     * Load messages from database as fallback.
     * 从数据库加载消息作为后备。
     */
    private Map<String, String> loadMessagesFromDatabase(String languageCode) {
        Map<String, String> messages = new HashMap<>();
        String sql = "SELECT `key`, `value` FROM localization_strings WHERE language = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, languageCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.put(rs.getString("key"), rs.getString("value"));
                }
            }

        } catch (SQLException e) {
            LOGGER.severe("Error loading localization strings from database: " + e.getMessage());
            // Return default English messages as fallback
            return getFallbackMessages();
        }

        return messages;
    }
    
    /**
     * Convert language code to Locale.
     * 将语言代码转换为Locale。
     */
    private Locale getLocaleFromCode(String languageCode) {
        switch (languageCode) {
            case "en":
                return Locale.forLanguageTag("en-US");
            case "fi":
                return Locale.forLanguageTag("fi-FI");
            case "sv":
                return Locale.forLanguageTag("sv-SE");
            case "ja":
                return Locale.forLanguageTag("ja-JP");
            case "ar":
                return Locale.forLanguageTag("ar-AR");
            default:
                return Locale.US;
        }
    }

    /**
     * Get a specific message for a given language.
     * 获取指定语言的特定消息。
     *
     * @param key message key
     * @param languageCode language code
     * @return message value, or the key if not found
     */
    public String getMessage(String key, String languageCode) {
        String sql = "SELECT `value` FROM localization_strings WHERE `key` = ? AND language = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, key);
            pstmt.setString(2, languageCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }

        } catch (SQLException e) {
            LOGGER.severe("Error getting message: " + e.getMessage());
        }

        // Return the key itself if not found
        return key;
    }

    /**
     * Fallback English messages in case database query fails.
     * 数据库查询失败时的备用英文消息。
     */
    private Map<String, String> getFallbackMessages() {
        Map<String, String> messages = new HashMap<>();
        messages.put("enter.num.items", "Enter the number of items to purchase: ");
        messages.put("enter.price", "Enter the price for item ");
        messages.put("enter.quantity", "Enter the quantity for item ");
        messages.put("total.cost", "Total cost: ");
        return messages;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("MessagesBundle", locale);
    }
}
