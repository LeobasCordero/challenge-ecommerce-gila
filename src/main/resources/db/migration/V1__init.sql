CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(12, 4) NOT NULL,
    stock INT NOT NULL,
    initial_stock INT NOT NULL,
    category VARCHAR(100) NOT NULL
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    total_price DECIMAL(12, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(12, 4) NOT NULL
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMPTZ NOT NULL,
    username VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    details JSONB
);

-- Seed Roles
INSERT INTO roles (id, name) VALUES 
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'ROLE_ADMIN'),
('c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f', 'ROLE_CUSTOMER');

-- Seed Users (password is "password")
INSERT INTO users (id, username, password, enabled) VALUES 
('9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d', 'admin', '$2a$10$Dpwk8.EOvDvWnLEdOYEn8eXCacNMj3TZ6A2LkNFGSC9fmCTCxi7dG', true),
('8a7b6c5d-4e3f-2a1b-0c9d-8e7f6a5b4c3d', 'customer', '$2a$10$Dpwk8.EOvDvWnLEdOYEn8eXCacNMj3TZ6A2LkNFGSC9fmCTCxi7dG', true);

-- Assign Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
('9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'),
('8a7b6c5d-4e3f-2a1b-0c9d-8e7f6a5b4c3d', 'c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f');
