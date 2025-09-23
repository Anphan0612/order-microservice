CREATE TABLE orders (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        order_code VARCHAR(32) UNIQUE NOT NULL,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        currency VARCHAR(8) NOT NULL DEFAULT 'VND',
                        subtotal BIGINT NOT NULL,
                        discount BIGINT NOT NULL DEFAULT 0,
                        shipping_fee BIGINT NOT NULL DEFAULT 0,
                        grand_total BIGINT NOT NULL,
                        note VARCHAR(255),
                        receiver_name VARCHAR(100) NOT NULL,
                        receiver_phone VARCHAR(20) NOT NULL,
                        address_line1 VARCHAR(255) NOT NULL,
                        ward VARCHAR(100) NOT NULL,
                        district VARCHAR(100) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             order_id BIGINT NOT NULL,
                             product_id VARCHAR(50) NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             unit_price BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             line_total BIGINT NOT NULL,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE idempotency_keys (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  idem_key CHAR(36) NOT NULL,
                                  request_hash CHAR(64) NOT NULL,
                                  order_id BIGINT NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE (user_id, idem_key)
);

CREATE TABLE outbox_events (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               aggregate_type VARCHAR(50) NOT NULL,
                               aggregate_id VARCHAR(64) NOT NULL,
                               type VARCHAR(100) NOT NULL,
                               payload JSON NOT NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'NEW',
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
