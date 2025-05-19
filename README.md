# DiscountRuleEngine

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

