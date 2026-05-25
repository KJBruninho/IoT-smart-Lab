-- init_iot_room.sql

CREATE DATABASE IF NOT EXISTS iot_room
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE iot_room;

DROP VIEW IF EXISTS vw_ultimas_leituras;
DROP VIEW IF EXISTS vw_historico_leituras;
DROP VIEW IF EXISTS vw_sensores_estacoes;

DROP PROCEDURE IF EXISTS sp_registar_leitura;
DROP PROCEDURE IF EXISTS sp_ultimas_leituras_sensor;

DROP TRIGGER IF EXISTS trg_leitura_validar_valor;
DROP TRIGGER IF EXISTS trg_sensor_validar_tipo;

DROP TABLE IF EXISTS leituras_sensor;
DROP TABLE IF EXISTS sensores;
DROP TABLE IF EXISTS estacoes;

CREATE TABLE estacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    localizacao VARCHAR(150),
    device_id VARCHAR(100) NOT NULL UNIQUE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE sensores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    estacao_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sensores_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uk_sensor_tipo_estacao
        UNIQUE (estacao_id, tipo)
) ENGINE=InnoDB;

CREATE TABLE leituras_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id BIGINT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    registado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leituras_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- INDEXES

CREATE INDEX idx_estacoes_device_id
ON estacoes(device_id);

CREATE INDEX idx_sensores_estacao
ON sensores(estacao_id);

CREATE INDEX idx_sensores_tipo
ON sensores(tipo);

CREATE INDEX idx_sensores_ativo
ON sensores(ativo);

CREATE INDEX idx_leituras_sensor
ON leituras_sensor(sensor_id);

CREATE INDEX idx_leituras_sensor_data
ON leituras_sensor(sensor_id, registado_em);

CREATE INDEX idx_leituras_data
ON leituras_sensor(registado_em);

-- TRIGGERS

DELIMITER //

CREATE TRIGGER trg_sensor_validar_tipo
BEFORE INSERT ON sensores
FOR EACH ROW
BEGIN
    SET NEW.tipo = UPPER(NEW.tipo);

    IF NEW.tipo NOT IN ('TEMPERATURA', 'TDS') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de sensor inválido. Usa TEMPERATURA ou TDS.';
    END IF;
END//

CREATE TRIGGER trg_leitura_validar_valor
BEFORE INSERT ON leituras_sensor
FOR EACH ROW
BEGIN
    IF NEW.valor < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'O valor da leitura não pode ser negativo.';
    END IF;
END//

DELIMITER ;

-- PROCEDURES

DELIMITER //

CREATE PROCEDURE sp_registar_leitura(
    IN p_device_id VARCHAR(100),
    IN p_tipo_sensor VARCHAR(50),
    IN p_valor DECIMAL(10,2)
)
BEGIN
    DECLARE v_sensor_id BIGINT;

    SELECT s.id
    INTO v_sensor_id
    FROM sensores s
    INNER JOIN estacoes e ON e.id = s.estacao_id
    WHERE e.device_id = p_device_id
      AND s.tipo = UPPER(p_tipo_sensor)
      AND e.ativa = TRUE
      AND s.ativo = TRUE
    LIMIT 1;

    IF v_sensor_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Sensor não encontrado ou inativo.';
    ELSE
        INSERT INTO leituras_sensor (sensor_id, valor)
        VALUES (v_sensor_id, p_valor);
    END IF;
END//

CREATE PROCEDURE sp_ultimas_leituras_sensor(
    IN p_sensor_id BIGINT,
    IN p_limite INT
)
BEGIN
    SELECT
        l.id,
        l.sensor_id,
        s.nome AS sensor,
        s.tipo,
        s.unidade,
        l.valor,
        l.registado_em
    FROM leituras_sensor l
    INNER JOIN sensores s ON s.id = l.sensor_id
    WHERE l.sensor_id = p_sensor_id
    ORDER BY l.registado_em DESC
    LIMIT p_limite;
END//

DELIMITER ;

-- VIEWS

CREATE VIEW vw_sensores_estacoes AS
SELECT
    e.id AS estacao_id,
    e.nome AS estacao,
    e.localizacao,
    e.device_id,
    e.ativa AS estacao_ativa,
    s.id AS sensor_id,
    s.nome AS sensor,
    s.tipo,
    s.unidade,
    s.ativo AS sensor_ativo
FROM estacoes e
INNER JOIN sensores s ON s.estacao_id = e.id;

CREATE VIEW vw_historico_leituras AS
SELECT
    l.id AS leitura_id,
    e.id AS estacao_id,
    e.nome AS estacao,
    e.device_id,
    s.id AS sensor_id,
    s.nome AS sensor,
    s.tipo,
    s.unidade,
    l.valor,
    l.registado_em
FROM leituras_sensor l
INNER JOIN sensores s ON s.id = l.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id;

CREATE VIEW vw_ultimas_leituras AS
SELECT
    l.id AS leitura_id,
    e.nome AS estacao,
    e.device_id,
    s.nome AS sensor,
    s.tipo,
    s.unidade,
    l.valor,
    l.registado_em
FROM leituras_sensor l
INNER JOIN sensores s ON s.id = l.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id
INNER JOIN (
    SELECT
        sensor_id,
        MAX(registado_em) AS ultima_data
    FROM leituras_sensor
    GROUP BY sensor_id
) ult ON ult.sensor_id = l.sensor_id
     AND ult.ultima_data = l.registado_em;

-- DADOS INICIAIS

INSERT INTO estacoes (nome, localizacao, device_id)
VALUES ('Sala IoT', 'Laboratório', 'esp32_sala_01');

INSERT INTO sensores (nome, tipo, unidade, estacao_id)
VALUES
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 1),
('Sensor TDS', 'TDS', 'ppm', 1);

CALL sp_registar_leitura('esp32_sala_01', 'TEMPERATURA', 23.50);
CALL sp_registar_leitura('esp32_sala_01', 'TDS', 450.00);

-- TESTES

SELECT * FROM estacoes;
SELECT * FROM sensores;
SELECT * FROM leituras_sensor;

SELECT * FROM vw_sensores_estacoes;
SELECT * FROM vw_historico_leituras;
SELECT * FROM vw_ultimas_leituras;
