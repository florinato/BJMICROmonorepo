-- Esta es una db de testing para los e2e, un arrange, if you wll

CREATE DATABASE IF NOT EXISTS `user_db`;
CREATE DATABASE IF NOT EXISTS `bets_db`;
-- CREATE DATABASE IF NOT EXISTS `game_core_db`;


USE `user_db`;


CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `balance` DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
  `role` VARCHAR(255) NOT NULL DEFAULT 'USER'
);

-- User de prueba
-- IMPORTANTE: La contraseña debe estar HASHEADA con BCrypt, igual que lo haría tu aplicación.

INSERT INTO `users` (username, email, password_hash, role)
VALUES ('testuser', 'testuser@example.com', '$2a$10$3g5v3.gY8.5f.21a1j2o3uL6b8c7d9e0f.1g2h3i4j5k6l7m8n9o', 'USER')
ON DUPLICATE KEY UPDATE username=username; -- No hacer nada si el usuario ya existe.
