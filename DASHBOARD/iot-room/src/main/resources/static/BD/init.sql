
CREATE DATABASE IF NOT EXISTS iot_room
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE iot_room;

-- =========================================================
-- 00. LIMPEZA
-- =========================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS logs_sistema;
DROP VIEW IF EXISTS pedidos_alteracao_sensor;
DROP VIEW IF EXISTS vw_logs_completos;
DROP VIEW IF EXISTS vw_alertas_sensor_resumo;
DROP VIEW IF EXISTS vw_comandos_sensor;
DROP VIEW IF EXISTS vw_ultimas_leituras;
DROP VIEW IF EXISTS vw_historico_leituras;
DROP VIEW IF EXISTS vw_sensores_estacoes;
DROP VIEW IF EXISTS vw_utilizadores_grupos;
DROP VIEW IF EXISTS vw_acessos_estacoes;
DROP VIEW IF EXISTS vw_experiencias_estacoes;
DROP VIEW IF EXISTS vw_experiencias_resumo;
DROP VIEW IF EXISTS vw_experiencias_ativas_estacoes;

DROP PROCEDURE IF EXISTS sp_registar_leitura;
DROP PROCEDURE IF EXISTS sp_registar_leitura_ativa;
DROP PROCEDURE IF EXISTS sp_obter_experiencia_ativa_por_device;
DROP PROCEDURE IF EXISTS sp_iniciar_experiencia;
DROP PROCEDURE IF EXISTS sp_cancelar_experiencia;
DROP PROCEDURE IF EXISTS sp_registar_log;
DROP PROCEDURE IF EXISTS sp_registar_comando_sensor;
DROP PROCEDURE IF EXISTS sp_atualizar_estado_comando_sensor;
DROP PROCEDURE IF EXISTS sp_ultimas_leituras_sensor;
DROP PROCEDURE IF EXISTS sp_ultimas_leituras_experiencia;
DROP PROCEDURE IF EXISTS sp_criar_experiencia;
DROP PROCEDURE IF EXISTS sp_associar_estacao_experiencia;
DROP PROCEDURE IF EXISTS sp_finalizar_experiencia;

DROP TRIGGER IF EXISTS trg_sensor_validar_tipo_insert;
DROP TRIGGER IF EXISTS trg_sensor_validar_tipo_update;
DROP TRIGGER IF EXISTS trg_leitura_validar_valor_insert;
DROP TRIGGER IF EXISTS trg_leitura_validar_experiencia_sensor_insert;
DROP TRIGGER IF EXISTS trg_utilizador_validar_tipo_insert;
DROP TRIGGER IF EXISTS trg_utilizador_validar_tipo_update;
DROP TRIGGER IF EXISTS trg_experiencia_validar_estado_insert;
DROP TRIGGER IF EXISTS trg_experiencia_validar_estado_update;
DROP TRIGGER IF EXISTS trg_log_validar_nivel_insert;

DROP TABLE IF EXISTS logs_detalhes;
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS configuracoes_sistema;
DROP TABLE IF EXISTS configuracoes_modo_sensor;
DROP TABLE IF EXISTS comandos_sensor;
DROP TABLE IF EXISTS pedidos_configuracao_sensor;
DROP TABLE IF EXISTS alertas_sensor;
DROP TABLE IF EXISTS regras_alerta_sensor;
DROP TABLE IF EXISTS forum_respostas;
DROP TABLE IF EXISTS forum_topicos;
DROP TABLE IF EXISTS avisos;
DROP TABLE IF EXISTS tipos_log;
DROP TABLE IF EXISTS leituras_sensor;
DROP TABLE IF EXISTS experiencia_estacoes;
DROP TABLE IF EXISTS experiencias;
DROP TABLE IF EXISTS permissoes_utilizador_estacao;
DROP TABLE IF EXISTS permissoes_grupo_estacao;
DROP TABLE IF EXISTS utilizador_grupos;
DROP TABLE IF EXISTS roles_grupo;
DROP TABLE IF EXISTS grupos;
DROP TABLE IF EXISTS utilizadores;
DROP TABLE IF EXISTS sensores;
DROP TABLE IF EXISTS estacoes;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 01. ESTAÇÕES E SENSORES
-- =========================================================
CREATE TABLE estacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    localizacao VARCHAR(150),
    device_id VARCHAR(100) NOT NULL UNIQUE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sensores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    estacao_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    remoto_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    fator_calibracao DECIMAL(10,6) NOT NULL DEFAULT 1.000000,
    offset_calibracao DECIMAL(10,6) NOT NULL DEFAULT 0.000000,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_sensores_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uk_sensor_tipo_estacao
        UNIQUE (estacao_id, tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 02. UTILIZADORES, GRUPOS, ROLES E PERMISSÕES
-- =========================================================
CREATE TABLE utilizadores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    role ENUM('ADMIN', 'PROFESSOR', 'ALUNO') NOT NULL DEFAULT 'ALUNO',
    grupo_id BIGINT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE grupos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT NULL,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_grupos_professor
        FOREIGN KEY (professor_id)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE utilizadores
    ADD CONSTRAINT fk_utilizadores_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

CREATE TABLE roles_grupo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 03. EXPERIÊNCIAS E LEITURAS
-- =========================================================
CREATE TABLE experiencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim DATETIME NULL,
    estado ENUM('CRIADA', 'ATIVA', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'CRIADA',
    grupo_id BIGINT NOT NULL,
    criado_por BIGINT NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_experiencias_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_experiencias_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES utilizadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experiencia_estacoes (
    experiencia_id BIGINT NOT NULL,
    estacao_id BIGINT NOT NULL,
    ordem INT NOT NULL DEFAULT 1,
    observacao VARCHAR(255),
    adicionada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (experiencia_id, estacao_id),

    CONSTRAINT fk_experiencia_estacoes_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_experiencia_estacoes_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE leituras_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    experiencia_id BIGINT NOT NULL,
    data_registo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sensor_id BIGINT NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_leituras_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_leituras_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 04. AVISOS MANUAIS
-- =========================================================
CREATE TABLE avisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    criado_por BIGINT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em DATETIME NULL,

    CONSTRAINT fk_avisos_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES utilizadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 05. FÓRUM
-- =========================================================
CREATE TABLE forum_topicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NOT NULL,
    criado_por BIGINT NOT NULL,
    grupo_id BIGINT NULL,
    experiencia_id BIGINT NULL,
    estado ENUM('ABERTO', 'FECHADO') NOT NULL DEFAULT 'ABERTO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL,
    fechado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_forum_topicos_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES utilizadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_forum_topicos_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_forum_topicos_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE forum_respostas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topico_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_forum_respostas_topico
        FOREIGN KEY (topico_id)
        REFERENCES forum_topicos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_forum_respostas_autor
        FOREIGN KEY (autor_id)
        REFERENCES utilizadores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 06. ALERTAS AUTOMÁTICOS DOS SENSORES
-- =========================================================
CREATE TABLE regras_alerta_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT NOT NULL,
    grupo_id BIGINT NULL,
    experiencia_id BIGINT NULL,
    estacao_id BIGINT NULL,
    tipo_sensor ENUM('TEMPERATURA', 'TDS', 'PH') NOT NULL,
    operador ENUM('ACIMA_DE', 'ABAIXO_DE', 'ENTRE', 'FORA_INTERVALO') NOT NULL,
    valor_min DECIMAL(10,2) NULL,
    valor_max DECIMAL(10,2) NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NULL,
    severidade ENUM('INFO', 'AVISO', 'CRITICO') NOT NULL DEFAULT 'AVISO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    cooldown_minutos INT NOT NULL DEFAULT 10,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizada_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_regras_alerta_professor
        FOREIGN KEY (professor_id)
        REFERENCES utilizadores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_regras_alerta_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_regras_alerta_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_regras_alerta_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE alertas_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    regra_id BIGINT NOT NULL,
    leitura_id BIGINT NULL,
    professor_id BIGINT NOT NULL,
    experiencia_id BIGINT NULL,
    grupo_id BIGINT NULL,
    estacao_id BIGINT NULL,
    sensor_id BIGINT NULL,
    tipo_sensor ENUM('TEMPERATURA', 'TDS', 'PH') NOT NULL,
    valor_lido DECIMAL(10,2) NOT NULL,
    valor_min DECIMAL(10,2) NULL,
    valor_max DECIMAL(10,2) NULL,
    titulo VARCHAR(150) NOT NULL,
    mensagem TEXT NULL,
    severidade ENUM('INFO', 'AVISO', 'CRITICO') NOT NULL DEFAULT 'AVISO',
    estado ENUM('NOVO', 'LIDO', 'RESOLVIDO', 'IGNORADO') NOT NULL DEFAULT 'NOVO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lido_em TIMESTAMP NULL DEFAULT NULL,
    resolvido_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_alertas_sensor_regra
        FOREIGN KEY (regra_id)
        REFERENCES regras_alerta_sensor(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_leitura
        FOREIGN KEY (leitura_id)
        REFERENCES leituras_sensor(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_professor
        FOREIGN KEY (professor_id)
        REFERENCES utilizadores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_alertas_sensor_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =========================================================
-- 07. COMANDOS REMOTOS PARA SENSORES / ESP32
-- =========================================================
CREATE TABLE comandos_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    tipo_sensor VARCHAR(50) NOT NULL,
    comando VARCHAR(255) NOT NULL,
    estado ENUM('ENVIADO', 'CONFIRMADO', 'ERRO') NOT NULL DEFAULT 'ENVIADO',
    tentativas_envio INT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publicado_em TIMESTAMP NULL DEFAULT NULL,
    confirmado_em TIMESTAMP NULL DEFAULT NULL,
    resposta TEXT NULL,
    ultimo_erro TEXT NULL,

    CONSTRAINT fk_comandos_sensor_professor
        FOREIGN KEY (professor_id)
        REFERENCES utilizadores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_comandos_sensor_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE configuracoes_modo_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    sensor_id BIGINT NOT NULL,

    intervalo_rapido_ms INT NOT NULL DEFAULT 1000,
    intervalo_estavel_ms INT NOT NULL DEFAULT 30000,
    duracao_modo_rapido_ms INT NOT NULL DEFAULT 120000,

    delta_significativo DECIMAL(10,4) NOT NULL DEFAULT 1.0000,

    atualizado_por BIGINT NULL,

    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizada_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_configuracoes_modo_sensor_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_configuracoes_modo_sensor_atualizado_por
        FOREIGN KEY (atualizado_por)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    UNIQUE KEY uk_configuracoes_modo_sensor_sensor (sensor_id),
    INDEX idx_configuracoes_modo_sensor_atualizado_por (atualizado_por)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE configuracoes_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(255) NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'TEXTO',
    descricao VARCHAR(255) NULL,
    atualizado_por BIGINT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_configuracoes_sistema_atualizado_por
        FOREIGN KEY (atualizado_por)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pedidos_configuracao_sensor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    sensor_id BIGINT NOT NULL,

    solicitado_por BIGINT NULL,
    analisado_por BIGINT NULL,

    origem ENUM('PROFESSOR', 'RESEARCHER', 'ESP32', 'SISTEMA') NOT NULL DEFAULT 'RESEARCHER',

    estado ENUM('PENDENTE', 'APROVADO', 'REJEITADO', 'APLICADO', 'ERRO') NOT NULL DEFAULT 'PENDENTE',

    intervalo_rapido_ms INT NOT NULL,
    intervalo_estavel_ms INT NOT NULL,
    duracao_modo_rapido_ms INT NOT NULL,
    delta_significativo DECIMAL(10,4) NOT NULL,

    motivo TEXT NULL,
    resposta_professor TEXT NULL,

    comando_id BIGINT NULL,

    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    analisado_em TIMESTAMP NULL DEFAULT NULL,
    aplicado_em TIMESTAMP NULL DEFAULT NULL,

    CONSTRAINT fk_pedidos_configuracao_sensor_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensores(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_pedidos_configuracao_sensor_solicitado_por
        FOREIGN KEY (solicitado_por)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_pedidos_configuracao_sensor_analisado_por
        FOREIGN KEY (analisado_por)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_pedidos_configuracao_sensor_comando
        FOREIGN KEY (comando_id)
        REFERENCES comandos_sensor(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_pedidos_configuracao_sensor_sensor (sensor_id),
    INDEX idx_pedidos_configuracao_sensor_estado (estado),
    INDEX idx_pedidos_configuracao_sensor_origem (origem),
    INDEX idx_pedidos_configuracao_sensor_criado_em (criado_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 08. LOGS
-- =========================================================
CREATE TABLE tipos_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_log_id BIGINT NOT NULL,
    utilizador_id BIGINT NULL,
    estacao_id BIGINT NULL,
    experiencia_id BIGINT NULL,
    nivel ENUM('TRACE', 'DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL') NOT NULL DEFAULT 'INFO',
    acao VARCHAR(100) NOT NULL,
    mensagem TEXT,
    ip VARCHAR(45),
    dispositivo VARCHAR(150),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_logs_tipo_log
        FOREIGN KEY (tipo_log_id)
        REFERENCES tipos_log(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_logs_utilizador
        FOREIGN KEY (utilizador_id)
        REFERENCES utilizadores(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_logs_estacao
        FOREIGN KEY (estacao_id)
        REFERENCES estacoes(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_logs_experiencia
        FOREIGN KEY (experiencia_id)
        REFERENCES experiencias(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE logs_detalhes (
    log_id BIGINT PRIMARY KEY,
    dados JSON NOT NULL,

    CONSTRAINT fk_logs_detalhes_log
        FOREIGN KEY (log_id)
        REFERENCES logs(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 09. ÍNDICES
-- =========================================================
CREATE INDEX idx_estacoes_device_id ON estacoes(device_id);
CREATE INDEX idx_estacoes_ativa ON estacoes(ativa);

CREATE INDEX idx_sensores_estacao ON sensores(estacao_id);
CREATE INDEX idx_sensores_tipo ON sensores(tipo);
CREATE INDEX idx_sensores_ativo ON sensores(ativo);
CREATE INDEX idx_sensores_estacao_ativo ON sensores(estacao_id, ativo);
CREATE INDEX idx_sensores_remoto_ativo ON sensores(remoto_ativo);
CREATE INDEX idx_sensores_tipo_ativo ON sensores(tipo, ativo);

CREATE INDEX idx_utilizadores_email ON utilizadores(email);
CREATE INDEX idx_utilizadores_role ON utilizadores(role);
CREATE INDEX idx_utilizadores_ativo ON utilizadores(ativo);
CREATE INDEX idx_utilizadores_grupo ON utilizadores(grupo_id);

CREATE INDEX idx_grupos_professor ON grupos(professor_id);
CREATE INDEX idx_grupos_ativo ON grupos(ativo);

CREATE INDEX idx_utilizador_grupos_grupo ON utilizador_grupos(grupo_id);
CREATE INDEX idx_utilizador_grupos_role ON utilizador_grupos(role_grupo_id);

CREATE INDEX idx_permissoes_grupo_estacao_estacao ON permissoes_grupo_estacao(estacao_id);
CREATE INDEX idx_permissoes_utilizador_estacao_estacao ON permissoes_utilizador_estacao(estacao_id);

CREATE INDEX idx_experiencias_grupo ON experiencias(grupo_id);
CREATE INDEX idx_experiencias_criado_por ON experiencias(criado_por);
CREATE INDEX idx_experiencias_estado ON experiencias(estado);
CREATE INDEX idx_experiencias_data_inicio ON experiencias(data_inicio);
CREATE INDEX idx_experiencias_grupo_estado ON experiencias(grupo_id, estado);
CREATE INDEX idx_experiencias_criado_por_estado ON experiencias(criado_por, estado);

CREATE INDEX idx_experiencia_estacoes_estacao ON experiencia_estacoes(estacao_id);
CREATE INDEX idx_experiencia_estacoes_ordem ON experiencia_estacoes(experiencia_id, ordem);

CREATE INDEX idx_leituras_experiencia ON leituras_sensor(experiencia_id);
CREATE INDEX idx_leituras_sensor ON leituras_sensor(sensor_id);
CREATE INDEX idx_leituras_data ON leituras_sensor(data_registo);
CREATE INDEX idx_leituras_sensor_data ON leituras_sensor(sensor_id, data_registo);
CREATE INDEX idx_leituras_experiencia_data ON leituras_sensor(experiencia_id, data_registo);
CREATE INDEX idx_leituras_experiencia_sensor_data ON leituras_sensor(experiencia_id, sensor_id, data_registo);

CREATE INDEX idx_avisos_criado_por ON avisos(criado_por);
CREATE INDEX idx_avisos_ativo ON avisos(ativo);
CREATE INDEX idx_avisos_criado_em ON avisos(criado_em);

CREATE INDEX idx_forum_topicos_criado_por ON forum_topicos(criado_por);
CREATE INDEX idx_forum_topicos_grupo ON forum_topicos(grupo_id);
CREATE INDEX idx_forum_topicos_experiencia ON forum_topicos(experiencia_id);
CREATE INDEX idx_forum_topicos_estado ON forum_topicos(estado);
CREATE INDEX idx_forum_topicos_criado_em ON forum_topicos(criado_em);
CREATE INDEX idx_forum_respostas_topico ON forum_respostas(topico_id);
CREATE INDEX idx_forum_respostas_autor ON forum_respostas(autor_id);
CREATE INDEX idx_forum_respostas_criado_em ON forum_respostas(criado_em);

CREATE INDEX idx_regras_alerta_professor ON regras_alerta_sensor(professor_id);
CREATE INDEX idx_regras_alerta_grupo ON regras_alerta_sensor(grupo_id);
CREATE INDEX idx_regras_alerta_experiencia ON regras_alerta_sensor(experiencia_id);
CREATE INDEX idx_regras_alerta_estacao ON regras_alerta_sensor(estacao_id);
CREATE INDEX idx_regras_alerta_tipo_sensor ON regras_alerta_sensor(tipo_sensor);
CREATE INDEX idx_regras_alerta_ativo ON regras_alerta_sensor(ativo);
CREATE INDEX idx_regras_alerta_aplicacao ON regras_alerta_sensor(professor_id, tipo_sensor, ativo);

CREATE INDEX idx_alertas_professor_estado ON alertas_sensor(professor_id, estado);
CREATE INDEX idx_alertas_regra_data ON alertas_sensor(regra_id, criado_em);
CREATE INDEX idx_alertas_experiencia ON alertas_sensor(experiencia_id);
CREATE INDEX idx_alertas_estacao ON alertas_sensor(estacao_id);
CREATE INDEX idx_alertas_sensor ON alertas_sensor(sensor_id);
CREATE INDEX idx_alertas_criado_em ON alertas_sensor(criado_em);

CREATE INDEX idx_comandos_sensor_sensor ON comandos_sensor(sensor_id);
CREATE INDEX idx_comandos_sensor_professor ON comandos_sensor(professor_id);
CREATE INDEX idx_comandos_sensor_device ON comandos_sensor(device_id);
CREATE INDEX idx_comandos_sensor_estado ON comandos_sensor(estado);
CREATE INDEX idx_comandos_sensor_criado_em ON comandos_sensor(criado_em);
CREATE INDEX idx_comandos_sensor_estado_envio ON comandos_sensor(estado, confirmado_em, publicado_em, tentativas_envio);
CREATE INDEX idx_comandos_sensor_publicado_em ON comandos_sensor(publicado_em);
CREATE INDEX idx_comandos_sensor_tentativas ON comandos_sensor(tentativas_envio);

CREATE INDEX idx_logs_tipo ON logs(tipo_log_id);
CREATE INDEX idx_logs_utilizador ON logs(utilizador_id);
CREATE INDEX idx_logs_estacao ON logs(estacao_id);
CREATE INDEX idx_logs_experiencia ON logs(experiencia_id);
CREATE INDEX idx_logs_nivel ON logs(nivel);
CREATE INDEX idx_logs_criado_em ON logs(criado_em);
CREATE INDEX idx_logs_tipo_data ON logs(tipo_log_id, criado_em);
CREATE INDEX idx_logs_experiencia_data ON logs(experiencia_id, criado_em);

-- =========================================================
-- 10. TRIGGERS
-- =========================================================
DELIMITER //

CREATE TRIGGER trg_sensor_validar_tipo_insert
BEFORE INSERT ON sensores
FOR EACH ROW
BEGIN
    SET NEW.tipo = UPPER(NEW.tipo);

    IF NEW.tipo NOT IN ('TEMPERATURA', 'TDS', 'PH') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de sensor inválido. Usa TEMPERATURA, TDS ou PH.';
    END IF;

    IF NEW.unidade IS NULL OR NEW.unidade = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A unidade do sensor é obrigatória.';
    END IF;
END//

CREATE TRIGGER trg_sensor_validar_tipo_update
BEFORE UPDATE ON sensores
FOR EACH ROW
BEGIN
    SET NEW.tipo = UPPER(NEW.tipo);

    IF NEW.tipo NOT IN ('TEMPERATURA', 'TDS', 'PH') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de sensor inválido. Usa TEMPERATURA, TDS ou PH.';
    END IF;

    IF NEW.unidade IS NULL OR NEW.unidade = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A unidade do sensor é obrigatória.';
    END IF;

    SET NEW.atualizado_em = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER trg_leitura_validar_valor_insert
BEFORE INSERT ON leituras_sensor
FOR EACH ROW
BEGIN
    IF NEW.valor < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'O valor da leitura não pode ser negativo.';
    END IF;

    IF NEW.unidade IS NULL OR NEW.unidade = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A unidade da leitura é obrigatória.';
    END IF;
END//

CREATE TRIGGER trg_leitura_validar_experiencia_sensor_insert
BEFORE INSERT ON leituras_sensor
FOR EACH ROW
BEGIN
    DECLARE v_count BIGINT;

    SELECT COUNT(*)
    INTO v_count
    FROM sensores s
    INNER JOIN experiencia_estacoes ee ON ee.estacao_id = s.estacao_id
    INNER JOIN experiencias e ON e.id = ee.experiencia_id
    WHERE s.id = NEW.sensor_id
      AND ee.experiencia_id = NEW.experiencia_id
      AND e.estado IN ('CRIADA', 'ATIVA');

    IF v_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'O sensor não pertence a uma estação associada à experiência ativa.';
    END IF;
END//

CREATE TRIGGER trg_utilizador_validar_tipo_insert
BEFORE INSERT ON utilizadores
FOR EACH ROW
BEGIN
    SET NEW.role = UPPER(COALESCE(NEW.role, 'ALUNO'));

    IF NEW.role NOT IN ('ADMIN', 'PROFESSOR', 'ALUNO') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Role de utilizador inválida. Usa ADMIN, PROFESSOR ou ALUNO.';
    END IF;
END//

CREATE TRIGGER trg_utilizador_validar_tipo_update
BEFORE UPDATE ON utilizadores
FOR EACH ROW
BEGIN
    SET NEW.role = UPPER(COALESCE(NEW.role, 'ALUNO'));

    IF NEW.role NOT IN ('ADMIN', 'PROFESSOR', 'ALUNO') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Role de utilizador inválida. Usa ADMIN, PROFESSOR ou ALUNO.';
    END IF;

    SET NEW.atualizado_em = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER trg_experiencia_validar_estado_insert
BEFORE INSERT ON experiencias
FOR EACH ROW
BEGIN
    SET NEW.estado = UPPER(NEW.estado);

    IF NEW.estado NOT IN ('CRIADA', 'ATIVA', 'FINALIZADA', 'CANCELADA') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Estado de experiência inválido.';
    END IF;

    IF NEW.data_fim IS NOT NULL AND NEW.data_fim < NEW.data_inicio THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A data_fim não pode ser anterior à data_inicio.';
    END IF;
END//

CREATE TRIGGER trg_experiencia_validar_estado_update
BEFORE UPDATE ON experiencias
FOR EACH ROW
BEGIN
    SET NEW.estado = UPPER(NEW.estado);

    IF NEW.estado NOT IN ('CRIADA', 'ATIVA', 'FINALIZADA', 'CANCELADA') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Estado de experiência inválido.';
    END IF;

    IF NEW.data_fim IS NOT NULL AND NEW.data_fim < NEW.data_inicio THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A data_fim não pode ser anterior à data_inicio.';
    END IF;
END//

CREATE TRIGGER trg_log_validar_nivel_insert
BEFORE INSERT ON logs
FOR EACH ROW
BEGIN
    SET NEW.nivel = UPPER(NEW.nivel);

    IF NEW.nivel NOT IN ('TRACE', 'DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nível de log inválido.';
    END IF;
END//

DELIMITER ;

-- =========================================================
-- 10. STORED PROCEDURES
-- =========================================================
DELIMITER //

CREATE PROCEDURE sp_criar_experiencia(
    IN p_nome VARCHAR(120),
    IN p_descricao TEXT,
    IN p_grupo_id BIGINT,
    IN p_criado_por BIGINT
)
BEGIN
    INSERT INTO experiencias (nome, descricao, data_inicio, estado, grupo_id, criado_por)
    VALUES (p_nome, p_descricao, CURRENT_TIMESTAMP, 'CRIADA', p_grupo_id, p_criado_por);

    SELECT LAST_INSERT_ID() AS experiencia_id;
END//

CREATE PROCEDURE sp_associar_estacao_experiencia(
    IN p_experiencia_id BIGINT,
    IN p_estacao_id BIGINT,
    IN p_ordem INT,
    IN p_observacao VARCHAR(255)
)
BEGIN
    INSERT INTO experiencia_estacoes (experiencia_id, estacao_id, ordem, observacao)
    VALUES (p_experiencia_id, p_estacao_id, COALESCE(p_ordem, 1), p_observacao)
    ON DUPLICATE KEY UPDATE
        ordem = VALUES(ordem),
        observacao = VALUES(observacao);
END//

CREATE PROCEDURE sp_iniciar_experiencia(IN p_experiencia_id BIGINT)
BEGIN
    UPDATE experiencias
    SET estado = 'ATIVA'
    WHERE id = p_experiencia_id
      AND estado = 'CRIADA';
END//

CREATE PROCEDURE sp_finalizar_experiencia(IN p_experiencia_id BIGINT)
BEGIN
    UPDATE experiencias
    SET estado = 'FINALIZADA', data_fim = CURRENT_TIMESTAMP
    WHERE id = p_experiencia_id
      AND estado IN ('CRIADA', 'ATIVA');
END//

CREATE PROCEDURE sp_cancelar_experiencia(IN p_experiencia_id BIGINT)
BEGIN
    UPDATE experiencias
    SET estado = 'CANCELADA', data_fim = CURRENT_TIMESTAMP
    WHERE id = p_experiencia_id
      AND estado IN ('CRIADA', 'ATIVA');
END//

CREATE PROCEDURE sp_obter_experiencia_ativa_por_device(IN p_device_id VARCHAR(100))
BEGIN
    SELECT
        exp.id AS experiencia_id,
        exp.nome AS experiencia,
        exp.estado,
        e.id AS estacao_id,
        e.nome AS estacao,
        e.device_id
    FROM experiencias exp
    INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
    INNER JOIN estacoes e ON e.id = ee.estacao_id
    WHERE e.device_id = p_device_id
      AND e.ativa = TRUE
      AND exp.estado IN ('CRIADA', 'ATIVA')
    ORDER BY exp.data_inicio DESC
    LIMIT 1;
END//

CREATE PROCEDURE sp_registar_leitura(
    IN p_experiencia_id BIGINT,
    IN p_device_id VARCHAR(100),
    IN p_tipo_sensor VARCHAR(50),
    IN p_valor DECIMAL(10,2)
)
BEGIN
    DECLARE v_sensor_id BIGINT;
    DECLARE v_unidade VARCHAR(20);
    DECLARE v_leitura_id BIGINT;

    SELECT s.id, s.unidade
    INTO v_sensor_id, v_unidade
    FROM sensores s
    INNER JOIN estacoes e ON e.id = s.estacao_id
    INNER JOIN experiencia_estacoes ee ON ee.estacao_id = e.id
    INNER JOIN experiencias exp ON exp.id = ee.experiencia_id
    WHERE exp.id = p_experiencia_id
      AND e.device_id = p_device_id
      AND s.tipo = UPPER(p_tipo_sensor)
      AND e.ativa = TRUE
      AND s.ativo = TRUE
      AND exp.estado IN ('CRIADA', 'ATIVA')
    LIMIT 1;

    IF v_sensor_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Sensor não encontrado, inativo ou não associado à experiência.';
    ELSE
        INSERT INTO leituras_sensor (experiencia_id, data_registo, sensor_id, unidade, valor)
        VALUES (p_experiencia_id, CURRENT_TIMESTAMP, v_sensor_id, v_unidade, p_valor);

        SET v_leitura_id = LAST_INSERT_ID();

        UPDATE experiencias
        SET estado = 'ATIVA'
        WHERE id = p_experiencia_id
          AND estado = 'CRIADA';

        SELECT v_leitura_id AS leitura_id;
    END IF;
END//

CREATE PROCEDURE sp_registar_leitura_ativa(
    IN p_device_id VARCHAR(100),
    IN p_tipo_sensor VARCHAR(50),
    IN p_valor DECIMAL(10,2)
)
BEGIN
    DECLARE v_experiencia_id BIGINT;
    DECLARE v_sensor_id BIGINT;
    DECLARE v_unidade VARCHAR(20);
    DECLARE v_leitura_id BIGINT;

    SELECT exp.id, s.id, s.unidade
    INTO v_experiencia_id, v_sensor_id, v_unidade
    FROM experiencias exp
    INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
    INNER JOIN estacoes e ON e.id = ee.estacao_id
    INNER JOIN sensores s ON s.estacao_id = e.id
    WHERE e.device_id = p_device_id
      AND s.tipo = UPPER(p_tipo_sensor)
      AND e.ativa = TRUE
      AND s.ativo = TRUE
      AND exp.estado IN ('CRIADA', 'ATIVA')
    ORDER BY exp.data_inicio DESC
    LIMIT 1;

    IF v_experiencia_id IS NULL OR v_sensor_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Não existe experiência ativa para esta estação/sensor.';
    ELSE
        INSERT INTO leituras_sensor (experiencia_id, data_registo, sensor_id, unidade, valor)
        VALUES (v_experiencia_id, CURRENT_TIMESTAMP, v_sensor_id, v_unidade, p_valor);

        SET v_leitura_id = LAST_INSERT_ID();

        UPDATE experiencias
        SET estado = 'ATIVA'
        WHERE id = v_experiencia_id
          AND estado = 'CRIADA';

        SELECT v_leitura_id AS leitura_id;
    END IF;
END//

CREATE PROCEDURE sp_ultimas_leituras_sensor(IN p_sensor_id BIGINT, IN p_limite INT)
BEGIN
    SELECT
        l.id,
        l.experiencia_id,
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

CREATE PROCEDURE sp_ultimas_leituras_experiencia(IN p_experiencia_id BIGINT, IN p_limite INT)
BEGIN
    SELECT
        l.id,
        l.experiencia_id,
        exp.nome AS experiencia,
        e.nome AS estacao,
        e.device_id,
        s.nome AS sensor,
        s.tipo,
        l.unidade,
        l.valor,
        l.data_registo
    FROM leituras_sensor l
    INNER JOIN experiencias exp ON exp.id = l.experiencia_id
    INNER JOIN sensores s ON s.id = l.sensor_id
    INNER JOIN estacoes e ON e.id = s.estacao_id
    WHERE l.experiencia_id = p_experiencia_id
    ORDER BY l.data_registo DESC
    LIMIT p_limite;
END//

CREATE PROCEDURE sp_registar_log(
    IN p_tipo_log_nome VARCHAR(50),
    IN p_utilizador_id BIGINT,
    IN p_estacao_id BIGINT,
    IN p_experiencia_id BIGINT,
    IN p_nivel VARCHAR(20),
    IN p_acao VARCHAR(100),
    IN p_mensagem TEXT,
    IN p_ip VARCHAR(45),
    IN p_dispositivo VARCHAR(150),
    IN p_dados JSON
)
BEGIN
    DECLARE v_tipo_log_id BIGINT;
    DECLARE v_log_id BIGINT;

    SELECT id
    INTO v_tipo_log_id
    FROM tipos_log
    WHERE nome = UPPER(p_tipo_log_nome)
    LIMIT 1;

    IF v_tipo_log_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de log não encontrado.';
    ELSE
        INSERT INTO logs (
            tipo_log_id,
            utilizador_id,
            estacao_id,
            experiencia_id,
            nivel,
            acao,
            mensagem,
            ip,
            dispositivo
        )
        VALUES (
            v_tipo_log_id,
            p_utilizador_id,
            p_estacao_id,
            p_experiencia_id,
            UPPER(COALESCE(p_nivel, 'INFO')),
            p_acao,
            p_mensagem,
            p_ip,
            p_dispositivo
        );

        SET v_log_id = LAST_INSERT_ID();

        IF p_dados IS NOT NULL THEN
            INSERT INTO logs_detalhes (log_id, dados)
            VALUES (v_log_id, p_dados);
        END IF;

        SELECT v_log_id AS log_id;
    END IF;
END//


CREATE PROCEDURE sp_registar_comando_sensor(
    IN p_professor_id BIGINT,
    IN p_sensor_id BIGINT,
    IN p_comando VARCHAR(255)
)
BEGIN
    DECLARE v_device_id VARCHAR(100);
    DECLARE v_tipo_sensor VARCHAR(50);
    DECLARE v_comando_id BIGINT;

    SELECT e.device_id, s.tipo
    INTO v_device_id, v_tipo_sensor
    FROM sensores s
    INNER JOIN estacoes e ON e.id = s.estacao_id
    WHERE s.id = p_sensor_id
      AND s.ativo = TRUE
      AND e.ativa = TRUE
    LIMIT 1;

    IF v_device_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Sensor não encontrado ou inativo.';
    ELSE
        INSERT INTO comandos_sensor (
            professor_id,
            sensor_id,
            device_id,
            tipo_sensor,
            comando,
            estado
        )
        VALUES (
            p_professor_id,
            p_sensor_id,
            v_device_id,
            v_tipo_sensor,
            p_comando,
            'ENVIADO'
        );

        SET v_comando_id = LAST_INSERT_ID();

        SELECT v_comando_id AS comando_id;
    END IF;
END//

CREATE PROCEDURE sp_atualizar_estado_comando_sensor(
    IN p_comando_id BIGINT,
    IN p_estado VARCHAR(20),
    IN p_resposta TEXT
)
BEGIN
    IF UPPER(p_estado) NOT IN ('ENVIADO', 'CONFIRMADO', 'ERRO') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Estado de comando inválido.';
    ELSE
        UPDATE comandos_sensor
        SET estado = UPPER(p_estado),
            resposta = p_resposta,
            ultimo_erro = CASE
                WHEN UPPER(p_estado) = 'ERRO' THEN p_resposta
                ELSE NULL
            END,
            confirmado_em = CASE
                WHEN UPPER(p_estado) IN ('CONFIRMADO', 'ERRO') THEN CURRENT_TIMESTAMP
                ELSE confirmado_em
            END
        WHERE id = p_comando_id;
    END IF;
END//

DELIMITER ;

-- =========================================================
-- 11. VIEWS
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
    s.ativo AS sensor_ativo,
    s.remoto_ativo,
    s.fator_calibracao,
    s.offset_calibracao,
    s.criado_em AS sensor_criado_em,
    s.atualizado_em AS sensor_atualizado_em
FROM estacoes e
INNER JOIN sensores s ON s.estacao_id = e.id;

CREATE VIEW vw_experiencias_estacoes AS
SELECT
    exp.id AS experiencia_id,
    exp.nome AS experiencia,
    exp.estado,
    exp.data_inicio,
    exp.data_fim,
    g.id AS grupo_id,
    g.nome AS grupo,
    u.id AS criado_por_id,
    u.nome AS criado_por,
    e.id AS estacao_id,
    e.nome AS estacao,
    e.device_id,
    e.localizacao,
    ee.ordem,
    ee.observacao,
    ee.adicionada_em
FROM experiencias exp
INNER JOIN grupos g ON g.id = exp.grupo_id
INNER JOIN utilizadores u ON u.id = exp.criado_por
INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
INNER JOIN estacoes e ON e.id = ee.estacao_id;

CREATE VIEW vw_experiencias_ativas_estacoes AS
SELECT
    exp.id AS experiencia_id,
    exp.nome AS experiencia,
    exp.estado,
    exp.data_inicio,
    g.id AS grupo_id,
    g.nome AS grupo,
    e.id AS estacao_id,
    e.nome AS estacao,
    e.device_id,
    e.ativa AS estacao_ativa
FROM experiencias exp
INNER JOIN grupos g ON g.id = exp.grupo_id
INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
INNER JOIN estacoes e ON e.id = ee.estacao_id
WHERE exp.estado IN ('CRIADA', 'ATIVA')
  AND e.ativa = TRUE;

CREATE VIEW vw_historico_leituras AS
SELECT
    l.id AS leitura_id,
    l.experiencia_id,
    exp.nome AS experiencia,
    exp.estado AS estado_experiencia,
    g.id AS grupo_id,
    g.nome AS grupo,
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
INNER JOIN experiencias exp ON exp.id = l.experiencia_id
INNER JOIN grupos g ON g.id = exp.grupo_id
INNER JOIN sensores s ON s.id = l.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id;

CREATE VIEW vw_ultimas_leituras AS
SELECT
    l.id AS leitura_id,
    l.experiencia_id,
    exp.nome AS experiencia,
    e.nome AS estacao,
    e.device_id,
    s.nome AS sensor,
    s.tipo,
    l.unidade,
    l.valor,
    l.data_registo
FROM leituras_sensor l
INNER JOIN experiencias exp ON exp.id = l.experiencia_id
INNER JOIN sensores s ON s.id = l.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id
INNER JOIN (
    SELECT experiencia_id, sensor_id, MAX(data_registo) AS ultima_data
    FROM leituras_sensor
    GROUP BY experiencia_id, sensor_id
) ult ON ult.experiencia_id = l.experiencia_id
     AND ult.sensor_id = l.sensor_id
     AND ult.ultima_data = l.data_registo;

CREATE VIEW vw_experiencias_resumo AS
SELECT
    exp.id AS experiencia_id,
    exp.nome AS experiencia,
    exp.descricao,
    exp.estado,
    exp.data_inicio,
    exp.data_fim,
    g.nome AS grupo,
    u.nome AS criado_por,
    COUNT(DISTINCT ee.estacao_id) AS total_estacoes,
    COUNT(DISTINCT l.sensor_id) AS total_sensores_com_leituras,
    COUNT(l.id) AS total_leituras,
    MIN(l.data_registo) AS primeira_leitura,
    MAX(l.data_registo) AS ultima_leitura
FROM experiencias exp
INNER JOIN grupos g ON g.id = exp.grupo_id
INNER JOIN utilizadores u ON u.id = exp.criado_por
LEFT JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
LEFT JOIN leituras_sensor l ON l.experiencia_id = exp.id
GROUP BY
    exp.id,
    exp.nome,
    exp.descricao,
    exp.estado,
    exp.data_inicio,
    exp.data_fim,
    g.nome,
    u.nome;

CREATE VIEW vw_utilizadores_grupos AS
SELECT
    u.id AS utilizador_id,
    u.nome AS utilizador,
    u.email,
    u.role,
    u.ativo AS utilizador_ativo,
    g.id AS grupo_id,
    g.nome AS grupo,
    g.ativo AS grupo_ativo,
    rg.id AS role_grupo_id,
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

CREATE VIEW vw_alertas_sensor_resumo AS
SELECT
    a.id AS alerta_id,
    a.estado,
    a.severidade,
    a.titulo,
    a.mensagem,
    a.tipo_sensor,
    a.valor_lido,
    a.valor_min,
    a.valor_max,
    a.criado_em,
    exp.nome AS experiencia,
    g.nome AS grupo,
    e.nome AS estacao,
    e.device_id,
    s.nome AS sensor,
    r.operador,
    r.cooldown_minutos
FROM alertas_sensor a
INNER JOIN regras_alerta_sensor r ON r.id = a.regra_id
LEFT JOIN experiencias exp ON exp.id = a.experiencia_id
LEFT JOIN grupos g ON g.id = a.grupo_id
LEFT JOIN estacoes e ON e.id = a.estacao_id
LEFT JOIN sensores s ON s.id = a.sensor_id;

CREATE VIEW vw_logs_completos AS
SELECT
    l.id AS log_id,
    tl.nome AS tipo_log,
    l.nivel,
    l.acao,
    l.mensagem,
    u.id AS utilizador_id,
    u.email AS utilizador_email,
    e.id AS estacao_id,
    e.device_id,
    exp.id AS experiencia_id,
    exp.nome AS experiencia,
    l.ip,
    l.dispositivo,
    l.criado_em,
    ld.dados
FROM logs l
INNER JOIN tipos_log tl ON tl.id = l.tipo_log_id
LEFT JOIN utilizadores u ON u.id = l.utilizador_id
LEFT JOIN estacoes e ON e.id = l.estacao_id
LEFT JOIN experiencias exp ON exp.id = l.experiencia_id
LEFT JOIN logs_detalhes ld ON ld.log_id = l.id;

-- Views de compatibilidade usadas pela dashboard admin Spring
CREATE VIEW logs_sistema AS
SELECT
    l.id,
    tl.nome AS tipo,
    l.mensagem AS descricao,
    CASE
        WHEN l.nivel IN ('ERROR', 'CRITICAL') THEN 'ERRO'
        WHEN l.nivel = 'WARNING' THEN 'AVISO'
        ELSE 'INFO'
    END AS gravidade,
    l.criado_em
FROM logs l
INNER JOIN tipos_log tl ON tl.id = l.tipo_log_id;

CREATE VIEW pedidos_alteracao_sensor AS
SELECT
    id,
    sensor_id,
    solicitado_por,
    analisado_por,
    origem,
    estado,
    intervalo_rapido_ms,
    intervalo_estavel_ms,
    duracao_modo_rapido_ms,
    delta_significativo,
    motivo,
    resposta_professor,
    comando_id,
    criado_em,
    analisado_em,
    aplicado_em
FROM pedidos_configuracao_sensor;

CREATE VIEW vw_comandos_sensor AS
SELECT
    cs.id,
    cs.professor_id,
    u.nome AS professor,
    cs.sensor_id,
    s.nome AS sensor,
    s.tipo AS tipo_sensor_atual,
    e.id AS estacao_id,
    e.nome AS estacao,
    cs.device_id,
    cs.tipo_sensor,
    cs.comando,
    cs.estado,
    cs.tentativas_envio,
    cs.resposta,
    cs.ultimo_erro,
    cs.criado_em,
    cs.publicado_em,
    cs.confirmado_em
FROM comandos_sensor cs
INNER JOIN utilizadores u ON u.id = cs.professor_id
INNER JOIN sensores s ON s.id = cs.sensor_id
INNER JOIN estacoes e ON e.id = s.estacao_id;

-- Fim do schema. Executa populate.sql depois deste ficheiro.
