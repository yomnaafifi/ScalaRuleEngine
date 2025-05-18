CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        transaction_date DATE,
                        product_name TEXT,
                        expiry_date DATE,
                        quantity INT,
                        unit_price DOUBLE PRECISION,
                        channel TEXT,
                        payment_method TEXT,
                        discount DOUBLE PRECISION,
                        final_price DOUBLE PRECISION
);
