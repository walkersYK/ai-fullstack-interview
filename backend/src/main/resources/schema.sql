DROP TABLE IF EXISTS device;

CREATE TABLE device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    disable_reason VARCHAR(200),
    disabled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);
