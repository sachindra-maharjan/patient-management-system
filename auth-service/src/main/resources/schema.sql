CREATE TABLE IF NOT EXISTS USERS (
    ID VARCHAR(36) PRIMARY KEY,
    EMAIL VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    ROLES VARCHAR(25)[] NOT NULL
);

