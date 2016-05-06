# CREATE CUSTOMERS TABLE

# --- !Ups

CREATE TABLE customers (
  id           UUID         NOT NULL PRIMARY KEY,

  CREATED_AT   TIMESTAMP    NOT NULL DEFAULT CURRENT_DATE,
  UPDATED_AT   TIMESTAMP    NOT NULL DEFAULT CURRENT_DATE,

  display_name VARCHAR(255) NOT NULL,
  email        VARCHAR(255) NOT NULL,
  token        VARCHAR(255)
);

# --- !Downs

DROP TABLE customers;
