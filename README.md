# DiscountRuleEngine


# RuleEngine

## Overview

The `RuleEngine` is a Scala application designed to process order data from a CSV file, apply a set of business rules to calculate discounts and final prices, and persist the processed orders into a PostgreSQL database. The application demonstrates functional programming concepts, error handling, and integration with external resources such as files and databases.

---

## Features

- **CSV Parsing:** Reads order data from a CSV file (`TRX1000.csv`).
- **Order Mapping:** Converts each CSV line into a strongly-typed `Order` case class.
- **Rule-Based Discount Calculation:** Applies multiple business rules to determine applicable discounts.
- **Final Price Calculation:** Computes the final price after applying the best discounts.
- **Database Integration:** Inserts processed orders into a PostgreSQL database.
- **Logging:** Logs key events and errors for traceability.

---

## How It Works

1. **Read Orders from CSV:**
   - The application reads the `TRX1000.csv` file, skipping the header row.
   - Each line is parsed into an `Order` object.

2. **Define Business Rules:**
   - Each rule is a tuple of:
     ```scala
     (Order => Boolean, Order => Double)
     ```
     where the first function determines if the rule applies (qualifier), and the second computes the discount (calculator).
   - Example rules include:
     - Orders expiring in less than 30 days.
     - Orders for products starting with "Cheese" or "Wine".
     - Orders sold on March 23rd.
     - Orders with more than 5 items.
     - Orders placed through the app.
     - Orders paid by Visa.

3. **Apply Rules and Calculate Discounts:**
   - For each order, all rules are evaluated.
   - The two highest discounts are averaged and applied to the order.
   - The final price is calculated and stored in the order object.

4. **Persist Orders to Database:**
   - Each processed order is inserted into the `orders` table in a PostgreSQL database.
   - Database connection details are managed in the `DBConfig` object.

5. **Logging and Error Handling:**
   - The application logs successful operations and errors using a logger.

---

## Example CSV Input





-----------
## 📌 Project Overview

- **Input**: A CSV file of retail transactions (`TRX1000.csv`)
- **Process**:
  - Read and parse transaction data
  - Evaluate discount eligibility based on business rules
  - Apply the top 2 qualifying discounts and average them
  - Calculate final prices
  - Log the operation for auditing
  - Write the enriched transaction to a PostgreSQL database
- **Output**: Discounted transactions persisted in a relational database

---

## 🧠 Core Logic

1. **Read CSV File**
   - Skip the header and parse each line into a strongly typed `Order` case class.

2. **Business Rules Engine**
   - Each rule is a tuple of:
     ```scala
     (Order => Boolean, Order => Double)
     ```
     where the first part is a qualifier and the second is a discount calculator.
   - Examples:
     - Near-expiry products (less than 30 days): Up to 29% discount
     - Product name starts with "Cheese" or "Wine"
     - Quantity-based tiered discount (e.g., 5% for 1-5, 10% for 6-10, etc.)

3. **Discount Selection**
   - All qualifying discounts are computed
   - The top 2 discounts (if any) are averaged to get the final discount rate

4. **Final Price Calculation**
   - Final price is computed as:
     ```
     final_price = unit_price * (1 - average_discount)
     ```

5. **Database Persistence**
   - A case class with `Option` types for computed fields (`discount`, `final_price`) is written to the `orders` table in PostgreSQL using JDBC.

6. **Logging**
   - Every processed transaction is logged using Java’s built-in logging system to ensure traceability.

---

## 📁 Project Structure

-----

## 🧪 How to Run

### 1. Start the PostgreSQL Database

```bash
cd utils
docker-compose up -d
```

This spins up a PostgreSQL instance accessible at localhost:6432 with a database named orders_db.

Default credentials:

User: scala

Password: scala