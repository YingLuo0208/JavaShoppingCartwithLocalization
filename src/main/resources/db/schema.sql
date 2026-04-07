-- Shopping Cart Localization Database Schema
-- Author: Ying Luo
-- This file should be placed at: src/main/resources/db/schema.sql

CREATE DATABASE IF NOT EXISTS shopping_cart_localization
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shopping_cart_localization;

-- Table: cart_records
-- Stores shopping cart summary information
CREATE TABLE IF NOT EXISTS cart_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_items INT NOT NULL,
    total_cost DOUBLE NOT NULL,
    language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: cart_items
-- Stores individual items in each cart with foreign key relationship
CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_record_id INT,
    item_number INT NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (cart_record_id) REFERENCES cart_records(id) ON DELETE CASCADE
);

-- Table: localization_strings
-- Stores UI message translations for different languages
CREATE TABLE IF NOT EXISTS localization_strings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL
);

-- Optional: Insert sample localization strings (if properties files are not available)
-- English
INSERT INTO localization_strings (`key`, value, language) VALUES
('enter.num.items', 'Enter the number of items to purchase:', 'en'),
('enter.price', 'Price', 'en'),
('enter.quantity', 'Quantity', 'en'),
('total.cost', 'Total cost:', 'en'),
('language.label', 'Language:', 'en'),
('button.start', 'Start', 'en'),
('button.calculate', 'Calculate Total', 'en'),
('button.save', 'Save to Database', 'en'),
('app.title', 'Shopping Cart', 'en')
ON DUPLICATE KEY UPDATE value=VALUES(value);

-- Finnish
INSERT INTO localization_strings (`key`, value, language) VALUES
('enter.num.items', 'Syötä ostettavien tuotteiden määrä:', 'fi'),
('enter.price', 'Hinta', 'fi'),
('enter.quantity', 'Määrä', 'fi'),
('total.cost', 'Kokonaishinta:', 'fi'),
('language.label', 'Kieli:', 'fi'),
('button.start', 'Aloita', 'fi'),
('button.calculate', 'Laske yhteensä', 'fi'),
('button.save', 'Tallenna tietokantaan', 'fi'),
('app.title', 'Ostoskori', 'fi')
ON DUPLICATE KEY UPDATE value=VALUES(value);

-- Swedish
INSERT INTO localization_strings (`key`, value, language) VALUES
('enter.num.items', 'Ange antalet varor att köpa:', 'sv'),
('enter.price', 'Pris', 'sv'),
('enter.quantity', 'Mängd', 'sv'),
('total.cost', 'Total kostnad:', 'sv'),
('language.label', 'Språk:', 'sv'),
('button.start', 'Börja', 'sv'),
('button.calculate', 'Beräkna totalt', 'sv'),
('button.save', 'Spara i databas', 'sv'),
('app.title', 'Varukorg', 'sv')
ON DUPLICATE KEY UPDATE value=VALUES(value);

-- Japanese
INSERT INTO localization_strings (`key`, value, language) VALUES
('enter.num.items', '購入する商品の数を入力してください:', 'ja'),
('enter.price', '価格', 'ja'),
('enter.quantity', '数量', 'ja'),
('total.cost', '合計金額:', 'ja'),
('language.label', '言語:', 'ja'),
('button.start', '開始', 'ja'),
('button.calculate', '合計を計算', 'ja'),
('button.save', 'データベースに保存', 'ja'),
('app.title', 'ショッピングカート', 'ja')
ON DUPLICATE KEY UPDATE value=VALUES(value);

-- Arabic
INSERT INTO localization_strings (`key`, value, language) VALUES
('enter.num.items', 'أدخل عدد العناصر المراد شراؤها: ', 'ar'),
('enter.price', 'السعر', 'ar'),
('enter.quantity', 'الكمية', 'ar'),
('total.cost', 'التكلفة الإجمالية:', 'ar'),
('language.label', 'اللغة:', 'ar'),
('button.start', 'ابدأ', 'ar'),
('button.calculate', 'احسب الإجمالي', 'ar'),
('button.save', 'حفظ في قاعدة البيانات', 'ar'),
('app.title', 'عربة التسوق', 'ar')
ON DUPLICATE KEY UPDATE value=VALUES(value);
