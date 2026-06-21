USE iot_room;

-- =========================================================
-- DADOS INICIAIS / POPULAÇÃO
-- =========================================================
INSERT INTO estacoes (nome, localizacao, device_id)
VALUES
('Sala IoT', 'Laboratório', 'esp32_sala_01'),
('Estação Móvel 02', 'Laboratório', 'esp32_movel_02');

INSERT INTO sensores (nome, tipo, unidade, estacao_id)
VALUES
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 1),
('Sensor TDS', 'TDS', 'ppm', 1),
('Sensor pH', 'PH', 'pH', 1),
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 2),
('Sensor TDS', 'TDS', 'ppm', 2),
('Sensor pH', 'PH', 'pH', 2);

-- Passwords de exemplo. Trocar por hashes reais gerados pela aplicação.
INSERT INTO utilizadores (nome, email, password_hash, role)
VALUES
('Administrador', 'admin@sala-iot.local', '$2y$10$exemplo_hash_admin', 'ADMIN'),
('Bruno Marinho', 'bruno@sala-iot.local', '$2y$10$exemplo_hash_professor', 'PROFESSOR'),
('Aluno Exemplo', 'aluno@sala-iot.local', '$2y$10$exemplo_hash_aluno', 'ALUNO');

INSERT INTO roles_grupo (nome, descricao)
VALUES
('OWNER', 'Responsável principal pelo grupo'),
('MANAGER', 'Pode gerir utilizadores e permissões do grupo'),
('OPERATOR', 'Pode consultar dados e operar estações'),
('VIEWER', 'Pode apenas consultar dados');

INSERT INTO grupos (professor_id, nome, descricao)
VALUES
(2, 'Grupo Ambiente', 'Grupo responsável pela monitorização ambiental'),
(2, 'Grupo Investigacao', 'Grupo de investigação ligado à qualidade da água');

UPDATE utilizadores SET grupo_id = 1 WHERE id = 3;

INSERT INTO utilizador_grupos (utilizador_id, grupo_id, role_grupo_id)
VALUES
(2, 1, 1),
(3, 1, 3),
(2, 2, 1),
(3, 2, 4);

INSERT INTO permissoes_grupo_estacao (grupo_id, estacao_id)
VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2);

INSERT INTO permissoes_utilizador_estacao (utilizador_id, estacao_id)
VALUES
(2, 1),
(2, 2),
(3, 1),
(3, 2);

INSERT INTO experiencias (nome, descricao, data_inicio, estado, grupo_id, criado_por)
VALUES
(
    'Experiência Inicial Sala IoT',
    'Experiência inicial para testar sensores de temperatura, TDS e pH.',
    CURRENT_TIMESTAMP,
    'CRIADA',
    1,
    2
);

INSERT INTO experiencia_estacoes (experiencia_id, estacao_id, ordem, observacao)
VALUES
(1, 1, 1, 'Estação principal da sala'),
(1, 2, 2, 'Estação móvel auxiliar');

INSERT INTO tipos_log (nome, descricao)
VALUES
('LOGIN', 'Eventos de autenticação de utilizadores'),
('LOGOUT', 'Saída de utilizadores'),
('MQTT', 'Mensagens recebidas ou erros MQTT'),
('SENSOR', 'Eventos relacionados com sensores'),
('COMANDO_SENSOR', 'Comandos remotos enviados aos sensores ou estações'),
('LEITURA', 'Registo de leituras dos sensores'),
('EXPERIENCIA', 'Eventos relacionados com experiências'),
('ESTACAO', 'Eventos relacionados com estações'),
('UTILIZADOR', 'Eventos relacionados com utilizadores'),
('GRUPO', 'Eventos relacionados com grupos'),
('PERMISSOES', 'Alterações de permissões'),
('API', 'Chamadas e erros da API'),
('SISTEMA', 'Eventos internos do sistema'),
('ERRO', 'Erros gerais'),
('ALERTA', 'Alertas de valores fora do intervalo'),
('BACKUP', 'Eventos de cópia de segurança'),
('SEGURANCA', 'Eventos de segurança');

INSERT INTO avisos (criado_por, titulo, mensagem, ativo)
VALUES
(2, 'Sistema inicializado', 'Base de dados inicial pronta para testes.', TRUE);

INSERT INTO forum_topicos (titulo, mensagem, criado_por, grupo_id, experiencia_id)
VALUES
('Discussão inicial', 'Tópico inicial para acompanhamento da experiência.', 2, 1, 1);

INSERT INTO forum_respostas (topico_id, autor_id, mensagem)
VALUES
(1, 3, 'Confirmo acesso ao grupo e à experiência.');

INSERT INTO regras_alerta_sensor (
    professor_id,
    grupo_id,
    tipo_sensor,
    operador,
    valor_max,
    titulo,
    mensagem,
    severidade,
    cooldown_minutos
)
VALUES (
    2,
    1,
    'TEMPERATURA',
    'ACIMA_DE',
    30.00,
    'Temperatura elevada',
    'A temperatura ultrapassou os 30 ºC.',
    'AVISO',
    10
);

INSERT INTO regras_alerta_sensor (
    professor_id,
    grupo_id,
    tipo_sensor,
    operador,
    valor_min,
    valor_max,
    titulo,
    mensagem,
    severidade,
    cooldown_minutos
)
VALUES (
    2,
    1,
    'TDS',
    'FORA_INTERVALO',
    300.00,
    700.00,
    'TDS fora do intervalo',
    'O valor de TDS está fora do intervalo esperado.',
    'CRITICO',
    10
);

INSERT INTO regras_alerta_sensor (
    professor_id,
    grupo_id,
    tipo_sensor,
    operador,
    valor_min,
    valor_max,
    titulo,
    mensagem,
    severidade,
    cooldown_minutos
)
VALUES (
    2,
    1,
    'PH',
    'FORA_INTERVALO',
    6.50,
    8.50,
    'pH fora do intervalo',
    'O valor de pH está fora do intervalo esperado.',
    'AVISO',
    10
);

INSERT INTO configuracoes_sistema (chave, valor, tipo, descricao)
VALUES
('modo_manutencao', 'false', 'BOOLEAN', 'Modo manutenção'),
('permitir_calibracao_professor', 'true', 'BOOLEAN', 'Permitir calibração por professores'),
('permitir_controlo_remoto_professor', 'true', 'BOOLEAN', 'Permitir controlo remoto por professores'),
('permitir_pedidos_intervalo', 'true', 'BOOLEAN', 'Permitir pedidos de alteração de intervalos'),

('intervalo_minimo_ms', '1000', 'INTEIRO', 'Intervalo mínimo permitido em milissegundos'),
('intervalo_maximo_ms', '300000', 'INTEIRO', 'Intervalo máximo permitido em milissegundos'),
('intervalo_rapido_padrao_ms', '1000', 'INTEIRO', 'Intervalo rápido padrão'),
('intervalo_estavel_padrao_ms', '30000', 'INTEIRO', 'Intervalo estável padrão'),
('duracao_modo_rapido_padrao_ms', '120000', 'INTEIRO', 'Duração padrão do modo rápido'),

('retencao_leituras_dias', '365', 'INTEIRO', 'Dias de retenção das leituras'),
('retencao_logs_dias', '180', 'INTEIRO', 'Dias de retenção dos logs'),

('mqtt_host', '192.168.4.1', 'TEXTO', 'Host do broker MQTT'),
('mqtt_porta', '1883', 'INTEIRO', 'Porta do broker MQTT'),
('mqtt_topico_base', 'esp32', 'TEXTO', 'Tópico base MQTT'),

('timeout_sem_comunicacao_segundos', '120', 'INTEIRO', 'Tempo até considerar sensor sem comunicação')
ON DUPLICATE KEY UPDATE
    valor = VALUES(valor),
    tipo = VALUES(tipo),
    descricao = VALUES(descricao),
    atualizado_em = CURRENT_TIMESTAMP;
    
INSERT IGNORE INTO configuracoes_modo_sensor (
    sensor_id,
    intervalo_rapido_ms,
    intervalo_estavel_ms,
    duracao_modo_rapido_ms,
    delta_significativo
)
SELECT
    s.id,
    1000,
    30000,
    120000,
    CASE
        WHEN s.tipo = 'TEMPERATURA' THEN 0.20
        WHEN s.tipo = 'TDS' THEN 5.00
        WHEN s.tipo = 'PH' THEN 0.10
        ELSE 1.00
    END
FROM sensores s;

-- Leituras dummy dos sensores. Todos os valores começam a 0.00.
CALL sp_registar_leitura_ativa('esp32_sala_01', 'TEMPERATURA', 0.00);
CALL sp_registar_leitura_ativa('esp32_sala_01', 'TDS', 0.00);
CALL sp_registar_leitura_ativa('esp32_sala_01', 'PH', 0.00);
CALL sp_registar_leitura_ativa('esp32_movel_02', 'TEMPERATURA', 0.00);
CALL sp_registar_leitura_ativa('esp32_movel_02', 'TDS', 0.00);
CALL sp_registar_leitura_ativa('esp32_movel_02', 'PH', 0.00);

CALL sp_registar_log(
    'SISTEMA',
    1,
    NULL,
    NULL,
    'INFO',
    'DATABASE_INIT',
    'Base de dados inicializada com sucesso.',
    NULL,
    'init_iot_room_completo.sql',
    JSON_OBJECT('database', 'iot_room', 'version', 'completo_ph_comandos')
);

CALL sp_registar_log(
    'EXPERIENCIA',
    2,
    NULL,
    1,
    'INFO',
    'EXPERIENCIA_CRIADA',
    'Experiência inicial criada e associada a duas estações.',
    NULL,
    'init_iot_room_completo.sql',
    JSON_OBJECT('experiencia_id', 1, 'total_estacoes', 2)
);

-- Verificação rápida
SELECT 'POPULATE_OK' AS estado, COUNT(*) AS total_sensores FROM sensores;
