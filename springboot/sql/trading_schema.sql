DROP VIEW IF EXISTS position;
DROP TABLE IF EXISTS security_order;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS trader;
DROP TABLE IF EXISTS quote;

CREATE TABLE trader (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  dob DATE NOT NULL,
  country VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL
);

CREATE TABLE account (
  id INT PRIMARY KEY,
  trader_id INT NOT NULL,
  amount DOUBLE PRECISION NOT NULL,
  CONSTRAINT account_trader_fk FOREIGN KEY (trader_id) REFERENCES trader(id)
);

CREATE TABLE security_order (
  id SERIAL PRIMARY KEY,
  account_id INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  ticker VARCHAR(10) NOT NULL,
  size INT NOT NULL,
  price DOUBLE PRECISION NOT NULL,
  notes VARCHAR(255),
  side VARCHAR(30),
  type VARCHAR(30),
  CONSTRAINT security_order_account_fk FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE quote (
  symbol VARCHAR(10) PRIMARY KEY,
  last_price DOUBLE PRECISION,
  bid_price DOUBLE PRECISION,
  bid_size INTEGER,
  ask_price DOUBLE PRECISION,
  ask_size INTEGER
);

CREATE VIEW position AS
  SELECT account_id,
         ticker,
         SUM(CASE
               WHEN side = 'BUY' THEN size
               WHEN side = 'SELL' THEN -size
               ELSE 0
             END) AS position
  FROM security_order
  GROUP BY account_id, ticker;
