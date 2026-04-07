# Shopping Cart Localization Project - Ying Luo

## Implemented Features

### JavaFX GUI application
- Multi-item shopping cart flow:
  - Enter number of items
  - Enter item price and quantity
  - Add multiple items
  - Calculate item subtotals and overall total

### Localization support
- English (en_US)
- Finnish (fi_FI)
- Swedish (sv_SE)
- Japanese (ja_JP)
- Arabic (ar_AR) with **RTL UI direction**
- Real-time language switching
- Author name "Ying Luo" stays constant across languages

### Localization source
- Reads from `MessagesBundle_*.properties` files (primary)
- Falls back to database table `localization_strings` if needed

### Database persistence
- Saves shopping cart summary to `cart_records`
- Saves item rows to `cart_items` (foreign key to `cart_records`)

### Unit tests (JUnit 5) and coverage (JaCoCo)

### CI/CD files
- Dockerfile
- Jenkinsfile

## Tech Stack

- Java 17
- Maven
- JavaFX 21
- MariaDB/MySQL
- JUnit 5 + JaCoCo

## Quick Start

### 1) Prerequisites
- JDK 17
- Maven 3.9+
- MySQL or MariaDB

### 2) Clone and open project
```bash
git clone <your-repository-url>
cd JavaShoppingCartwithLocalization
```

### 3) Create database schema
Use one of the following methods:

**Option A (MySQL client):**
```sql
SOURCE src/main/resources/db/schema.sql;
```

**Option B:** Open `src/main/resources/db/schema.sql` and execute manually.

### 4) Configure database connection (optional)
By default, the app uses:
```
DB_URL=jdbc:mariadb://localhost:3306/shopping_cart_localization
DB_USER=root
DB_PASSWORD=root
```

If your local credentials differ, set environment variables before running:

**Windows PowerShell:**
```powershell
$env:DB_URL="jdbc:mariadb://127.0.0.1:3306/shopping_cart_localization"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

**Linux/macOS:**
```bash
export DB_URL="jdbc:mariadb://127.0.0.1:3306/shopping_cart_localization"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

### 5) Run tests
```bash
mvn clean test
```

### 6) Run application (GUI)
```bash
mvn javafx:run
```

## How to Verify Functionality

1. Select a language from dropdown.
2. Enter number of items.
3. Enter price and quantity for each item.
4. Click Calculate total.
5. Click Save to Database.
6. Confirm results appear in UI and data is saved to DB.

Check database:
```sql
USE shopping_cart_localization;
SELECT * FROM cart_records ORDER BY id DESC;
SELECT * FROM cart_items ORDER BY id DESC;
```

## Reset Data
```sql
USE shopping_cart_localization;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE cart_records;
SET FOREIGN_KEY_CHECKS = 1;
```

## Docker

Pull image from Docker Hub:
```bash
docker pull <your-dockerhub-username>/shopping-cart-localization:latest
```

Or build locally:
```bash
docker build -t shopping-cart-localization:latest .
```

Run image (GUI mode with X11 forwarding):

**Start Xming first** (Windows, recommended: disable access control).

**Windows PowerShell:**
```powershell
docker run -it --rm `
  -e DISPLAY=host.docker.internal:0.0 `
  -e DB_URL="jdbc:mariadb://host.docker.internal:3306/shopping_cart_localization" `
  -e DB_USER=root `
  -e DB_PASSWORD=your_password `
  shopping-cart-localization:latest
```

## Jenkins Pipeline

`Jenkinsfile` stages:
1. Checkout
2. Test (`mvn --batch-mode clean test`)
3. Package (`mvn --batch-mode clean package -DskipTests`)
4. Build Docker image
5. Push Docker image to Docker Hub

---

**Author:** Ying Luo
