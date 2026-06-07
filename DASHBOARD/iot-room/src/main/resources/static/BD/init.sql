
CREATE DATABASE IF NOT EXISTS iot_room
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE iot_room;

-- =========================================================
-- LIMPEZA
-- =========================================================

DROP VIEW IF EXISTS vw_ultimas_leituras;
DROP VIEW IF EXISTS vw_historico_leituras;
DROP VIEW IF EXISTS vw_sensores_estacoes;
DROP VIEW IF EXISTS vw_utilizadores_grupos;
DROP VIEW IF EXISTS vw_acessos_estacoes;

DROP PROCEDURE IF EXISTS sp_registar_leitura;
DROP PROCEDURE IF EXISTS sp_ultimas_leituras_sensor;

DROP TRIGGER IF EXISTS trg_leitura_validar_valor;
DROP TRIGGER IF EXISTS trg_sensor_validar_tipo;
DROP TRIGGER IF EXISTS trg_utilizador_validar_tipo;

DROP TABLE IF EXISTS permissoes_utilizador_estacao;
DROP TABLE IF EXISTS permissoes_grupo_estacao;
DROP TABLE IF EXISTS utilizador_grupos;
DROP TABLE IF EXISTS roles_grupo;
DROP TABLE IF EXISTS grupos;
DROP TABLE IF EXISTS utilizadores;
DROP TABLE IF EXISTS leituras_sensor;
DROP TABLE IF EXISTS sensores;
DROP TABLE IF EXISTS estacoes;

-- =========================================================
-- TABELAS PRINCIPAIS DO SISTEMA IOT
-- =========================================================

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
    data_registo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sensor_id BIGINT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_leituras_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- TABELAS DE UTILIZADORES, GRUPOS, ROLES E PERMISSÕES
-- =========================================================

CREATE TABLE utilizadores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    tipo_utilizador ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE grupos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE roles_grupo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE utilizador_grupos (
    utilizador_id BIGINT NOT NULL,
    grupo_id BIGINT NOT NULL,
    role_grupo_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (utilizador_id, grupo_id),

    CONSTRAINT fk_utilizador_grupos_utilizador
        FOREIGN KEY (utilizador_id)
        REFERENCES utilizadores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_utilizador_grupos_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_utilizador_grupos_role
        FOREIGN KEY (role_grupo_id)
        REFERENCES roles_grupo(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE permissoes_grupo_estacao (
    grupo_id BIGINT NOT NULL,
    estacao_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (grupo_id, estacao_id),

    CONSTRAINT fk_permissoes_grupo_estacao_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_permissoes_grupo_estacao_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE permissoes_utilizador_estacao (
    utilizador_id BIGINT NOT NULL,
    estacao_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (utilizador_id, estacao_id),

    CONSTRAINT fk_permissoes_utilizador_estacao_utilizador
        FOREIGN KEY (utilizador_id)
        REFERENCES utilizadores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_permissoes_utilizador_estacao_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- ÍNDICES
-- =========================================================

CREATE INDEX idx_estacoes_device_id ON estacoes(device_id);

CREATE INDEX idx_sensores_estacao ON sensores(estacao_id);
CREATE INDEX idx_sensores_tipo ON sensores(tipo);
CREATE INDEX idx_sensores_ativo ON sensores(ativo);

CREATE INDEX idx_leituras_sensor ON leituras_sensor(sensor_id);
CREATE INDEX idx_leituras_sensor_data ON leituras_sensor(sensor_id, data_registo);
CREATE INDEX idx_leituras_data ON leituras_sensor(data_registo);

CREATE INDEX idx_utilizadores_email ON utilizadores(email);
CREATE INDEX idx_utilizadores_tipo ON utilizadores(tipo_utilizador);

CREATE INDEX idx_utilizador_grupos_grupo ON utilizador_grupos(grupo_id);
CREATE INDEX idx_utilizador_grupos_role ON utilizador_grupos(role_grupo_id);

CREATE INDEX idx_permissoes_grupo_estacao_estacao ON permissoes_grupo_estacao(estacao_id);
CREATE INDEX idx_permissoes_utilizador_estacao_estacao ON permissoes_utilizador_estacao(estacao_id);

-- =========================================================
-- TRIGGERS
-- =========================================================

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

CREATE TRIGGER trg_utilizador_validar_tipo
BEFORE INSERT ON utilizadores
FOR EACH ROW
BEGIN
    SET NEW.tipo_utilizador = UPPER(NEW.tipo_utilizador);

    IF NEW.tipo_utilizador NOT IN ('ADMIN', 'USER') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de utilizador inválido. Usa ADMIN ou USER.';
    END IF;
END//

DELIMITER ;

-- =========================================================
-- PROCEDURES
-- =========================================================

DELIMITER //

CREATE PROCEDURE sp_registar_leitura(
    IN p_device_id VARCHAR(100),
    IN p_tipo_sensor VARCHAR(50),
    IN p_valor DECIMAL(10,2)
)
BEGIN
    DECLARE v_sensor_id BIGINT;
    DECLARE v_unidade VARCHAR(20);

    SELECT s.id, s.unidade
    INTO v_sensor_id, v_unidade
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
        INSERT INTO leituras_sensor (data_registo, sensor_id, unidade, valor)
        VALUES (CURRENT_TIMESTAMP, v_sensor_id, v_unidade, p_valor);
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
        l.unidade,
        l.valor,
        l.data_registo
    FROM leituras_sensor l
    INNER JOIN sensores s ON s.id = l.sensor_id
    WHERE l.sensor_id = p_sensor_id
    ORDER BY l.data_registo DESC
    LIMIT p_limite;
END//

DELIMITER ;

-- =========================================================
-- VIEWS
-- =========================================================

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
    l.unidade,
    l.valor,
    l.data_registo
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
    l.unidade,
    l.valor,
    l.data_registo
FROM leituras_sensor l
INNER JOIN sensores s ON s.id = l.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id
INNER JOIN (
    SELECT
        sensor_id,
        MAX(data_registo) AS ultima_data
    FROM leituras_sensor
    GROUP BY sensor_id
) ult ON ult.sensor_id = l.sensor_id
     AND ult.ultima_data = l.data_registo;

CREATE VIEW vw_utilizadores_grupos AS
SELECT
    u.id AS utilizador_id,
    u.nome AS utilizador,
    u.email,
    u.tipo_utilizador,
    u.ativo AS utilizador_ativo,
    g.id AS grupo_id,
    g.nome AS grupo,
    g.ativo AS grupo_ativo,
    rg.nome AS role_grupo
FROM utilizadores u
INNER JOIN utilizador_grupos ug ON ug.utilizador_id = u.id
INNER JOIN grupos g ON g.id = ug.grupo_id
INNER JOIN roles_grupo rg ON rg.id = ug.role_grupo_id;

CREATE VIEW vw_acessos_estacoes AS
SELECT
    'GRUPO' AS tipo_acesso,
    g.id AS entidade_id,
    g.nome AS entidade,
    e.id AS estacao_id,
    e.nome AS estacao,
    e.device_id,
    e.localizacao
FROM permissoes_grupo_estacao pge
INNER JOIN grupos g ON g.id = pge.grupo_id
INNER JOIN estacoes e ON e.id = pge.estacao_id

UNION ALL

SELECT
    'UTILIZADOR' AS tipo_acesso,
    u.id AS entidade_id,
    u.email AS entidade,
    e.id AS estacao_id,
    e.nome AS estacao,
    e.device_id,
    e.localizacao
FROM permissoes_utilizador_estacao pue
INNER JOIN utilizadores u ON u.id = pue.utilizador_id
INNER JOIN estacoes e ON e.id = pue.estacao_id;

-- =========================================================
-- DADOS INICIAIS
-- =========================================================

INSERT INTO estacoes (nome, localizacao, device_id)
VALUES ('Sala IoT', 'Laboratório', 'esp32_sala_01');

INSERT INTO sensores (nome, tipo, unidade, estacao_id)
VALUES
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 1),
('Sensor TDS', 'TDS', 'ppm', 1);

INSERT INTO roles_grupo (nome, descricao)
VALUES
('OWNER', 'Responsável principal pelo grupo'),
('MANAGER', 'Pode gerir utilizadores e permissões do grupo'),
('OPERATOR', 'Pode consultar dados e operar estações'),
('VIEWER', 'Pode apenas consultar dados');

INSERT INTO grupos (nome, descricao)
VALUES
('Grupo Ambiente', 'Grupo responsável pela monitorização ambiental'),
('Grupo Investigacao', 'Grupo de investigação ligado à qualidade da água');

INSERT INTO utilizadores (nome, email, password_hash, tipo_utilizador)
VALUES
('Administrador', 'admin@sala-iot.local', '$2y$10$exemplo_hash_admin', 'ADMIN'),
('Bruno Marinho', 'bruno@sala-iot.local', '$2y$10$exemplo_hash_user', 'USER'),
('Investigador', 'investigador@sala-iot.local', '$2y$10$exemplo_hash_user2', 'USER');

INSERT INTO utilizador_grupos (utilizador_id, grupo_id, role_grupo_id)
VALUES
(1, 1, 1),
(2, 1, 3),
(3, 2, 4);

INSERT INTO permissoes_grupo_estacao (grupo_id, estacao_id)
VALUES
(1, 1),
(2, 1);

INSERT INTO permissoes_utilizador_estacao (utilizador_id, estacao_id)
VALUES
(2, 1);

CALL sp_registar_leitura('esp32_sala_01', 'TEMPERATURA', 0.0);
CALL sp_registar_leitura('esp32_sala_01', 'TDS', 0.00);
