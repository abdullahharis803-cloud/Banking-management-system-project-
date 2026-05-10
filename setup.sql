-- Run this in MySQL first
CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

-- Table 1: accounts (stores login info separately from balance info - normalization)
CREATE TABLE IF NOT EXISTS accounts (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(10)  NOT NULL DEFAULT 'user'
);

-- Table 2: balances (separated from accounts - normalization)
CREATE TABLE IF NOT EXISTS balances (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL UNIQUE,
    amount     DOUBLE NOT NULL DEFAULT 0.0,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Table 3: transactions (all money moves)
CREATE TABLE IF NOT EXISTS transactions (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    type       VARCHAR(20) NOT NULL,
    amount     DOUBLE NOT NULL,
    note       VARCHAR(200),
    done_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Table 4: complaints (user complaints to admin)
CREATE TABLE IF NOT EXISTS complaints (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    message    VARCHAR(500) NOT NULL,
    sent_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Default admin account
INSERT IGNORE INTO accounts (username, password, role) VALUES ('admin', 'admin123', 'admin');
INSERT IGNORE INTO balances (account_id, amount)
    SELECT id, 0.0 FROM accounts WHERE username = 'admin';
