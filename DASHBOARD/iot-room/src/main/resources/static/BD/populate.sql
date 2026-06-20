USE iot_room;

INSERT INTO estacoes (nome, localizacao, device_id)
VALUES
('Sala IoT', 'Laboratório', 'esp32_sala_01'),
('Estação Móvel 02', 'Laboratório', 'esp32_movel_02');

INSERT INTO sensores (nome, tipo, unidade, estacao_id)
VALUES
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 1),
('Sensor TDS', 'TDS', 'ppm', 1),
('Sensor Temperatura', 'TEMPERATURA', 'ºC', 2),
('Sensor TDS', 'TDS', 'ppm', 2);

-- Passwords de exemplo. Trocar por hashes reais gerados pela aplicação.
INSERT INTO utilizadores (nome, email, password_hash, tipo_utilizador)
VALUES
('Administrador', 'admin@sala-iot.local', '$2y$10$exemplo_hash_admin', 'ADMIN'),
('Bruno Marinho', 'bruno@sala-iot.local', '$2y$10$exemplo_hash_user', 'USER'),
('Investigador', 'investigador@sala-iot.local', '$2y$10$exemplo_hash_user2', 'USER');

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
    'Experiência inicial para testar sensores de temperatura e TDS.',
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

-- Leituras dummy dos sensores. Todos os valores começam a 0.00.
CALL sp_registar_leitura_ativa('esp32_sala_01', 'TEMPERATURA', 0.00);
CALL sp_registar_leitura_ativa('esp32_sala_01', 'TDS', 0.00);
CALL sp_registar_leitura_ativa('esp32_movel_02', 'TEMPERATURA', 0.00);
CALL sp_registar_leitura_ativa('esp32_movel_02', 'TDS', 0.00);

CALL sp_registar_log(
    'SISTEMA',
    1,
    NULL,
    NULL,
    'INFO',
    'DATABASE_INIT',
    'Base de dados inicializada com sucesso.',
    NULL,
    'init_iot_room_reorganizado.sql',
    JSON_OBJECT('database', 'iot_room', 'version', 'reorganizado')
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
    'init_iot_room_reorganizado.sql',
    JSON_OBJECT('experiencia_id', 1, 'total_estacoes', 2)
);

SELECT * FROM estacoes;
SELECT * FROM sensores;
SELECT * FROM utilizadores;
SELECT * FROM grupos;
SELECT * FROM roles_grupo;
SELECT * FROM utilizador_grupos;
SELECT * FROM permissoes_grupo_estacao;
SELECT * FROM permissoes_utilizador_estacao;
SELECT * FROM experiencias;
SELECT * FROM experiencia_estacoes;
SELECT * FROM leituras_sensor;
SELECT * FROM avisos;
SELECT * FROM forum_topicos;
SELECT * FROM forum_respostas;
SELECT * FROM regras_alerta_sensor;
SELECT * FROM alertas_sensor;
SELECT * FROM tipos_log;
SELECT * FROM logs;
SELECT * FROM logs_detalhes;

SELECT * FROM vw_sensores_estacoes;
SELECT * FROM vw_experiencias_estacoes;
SELECT * FROM vw_experiencias_ativas_estacoes;
SELECT * FROM vw_historico_leituras;
SELECT * FROM vw_ultimas_leituras;
SELECT * FROM vw_experiencias_resumo;
SELECT * FROM vw_utilizadores_grupos;
SELECT * FROM vw_acessos_estacoes;
SELECT * FROM vw_alertas_sensor_resumo;
SELECT * FROM vw_logs_completos;
