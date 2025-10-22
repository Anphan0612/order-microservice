-- Create idempotency_keys table
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    idem_key VARCHAR(36) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    order_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_idem_user_key UNIQUE (user_id, idem_key)
);

CREATE INDEX IF NOT EXISTS idx_idem_user_id ON idempotency_keys (user_id);
CREATE INDEX IF NOT EXISTS idx_idem_order_id ON idempotency_keys (order_id);

-- Normalize money columns to DECIMAL(19,4)
ALTER TABLE orders
    MODIFY subtotal DECIMAL(19,4) NOT NULL,
    MODIFY discount DECIMAL(19,4) NOT NULL,
    MODIFY shipping_fee DECIMAL(19,4) NOT NULL,
    MODIFY grand_total DECIMAL(19,4) NOT NULL;

ALTER TABLE order_items
    MODIFY unit_price DECIMAL(19,4) NOT NULL,
    MODIFY line_total DECIMAL(19,4) NOT NULL;


