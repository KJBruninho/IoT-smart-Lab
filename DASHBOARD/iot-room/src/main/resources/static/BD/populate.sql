USE iot_room;

-- =========================================================
-- Utilizadores de teste:
--   ADMIN     -> a@a / password
--   PROFESSOR -> p@p / password
--   PROFESSOR -> j@j / password
--   ALUNOS    -> b@b, c@c, d@d, e@e / password
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM logs_detalhes;
DELETE FROM logs;
DELETE FROM security_audit_logs;
DELETE FROM refresh_tokens;
DELETE FROM dispositivos_confiaveis;
DELETE FROM pedidos_configuracao_sensor;
DELETE FROM comandos_sensor;
DELETE FROM alertas_sensor;
DELETE FROM regras_alerta_sensor;
DELETE FROM forum_respostas;
DELETE FROM forum_topicos;
DELETE FROM avisos;
DELETE FROM leituras_sensor;
DELETE FROM experiencia_estacoes;
DELETE FROM experiencias;
DELETE FROM permissoes_utilizador_estacao;
DELETE FROM permissoes_grupo_estacao;
DELETE FROM utilizador_grupos;
DELETE FROM grupos;
DELETE FROM roles_grupo;
DELETE FROM configuracoes_modo_sensor;
DELETE FROM configuracoes_sistema;
DELETE FROM tipos_log;
DELETE FROM sensores;
DELETE FROM estacoes;
DELETE FROM utilizadores;

ALTER TABLE estacoes AUTO_INCREMENT = 1;
ALTER TABLE sensores AUTO_INCREMENT = 1;
ALTER TABLE utilizadores AUTO_INCREMENT = 1;
ALTER TABLE refresh_tokens AUTO_INCREMENT = 1;
ALTER TABLE dispositivos_confiaveis AUTO_INCREMENT = 1;
ALTER TABLE grupos AUTO_INCREMENT = 1;
ALTER TABLE roles_grupo AUTO_INCREMENT = 1;
ALTER TABLE experiencias AUTO_INCREMENT = 1;
ALTER TABLE leituras_sensor AUTO_INCREMENT = 1;
ALTER TABLE avisos AUTO_INCREMENT = 1;
ALTER TABLE forum_topicos AUTO_INCREMENT = 1;
ALTER TABLE forum_respostas AUTO_INCREMENT = 1;
ALTER TABLE regras_alerta_sensor AUTO_INCREMENT = 1;
ALTER TABLE alertas_sensor AUTO_INCREMENT = 1;
ALTER TABLE comandos_sensor AUTO_INCREMENT = 1;
ALTER TABLE configuracoes_modo_sensor AUTO_INCREMENT = 1;
ALTER TABLE configuracoes_sistema AUTO_INCREMENT = 1;
ALTER TABLE pedidos_configuracao_sensor AUTO_INCREMENT = 1;
ALTER TABLE tipos_log AUTO_INCREMENT = 1;
ALTER TABLE logs AUTO_INCREMENT = 1;
ALTER TABLE security_audit_logs AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 01. ESTAÇÕES
-- =========================================================
INSERT INTO estacoes (nome, localizacao, device_id, ativa) VALUES
('Sala IoT Principal', 'Laboratório 1 - Piso 0', 'esp32_sala_01', TRUE),
('Estação Móvel Água', 'Carrinho móvel - Laboratório Química', 'esp32_movel_02', TRUE),
('Estufa Biologia', 'Estufa - Ala Norte', 'esp32_estufa_03', TRUE),
('Aquário Experimental', 'Laboratório de Biologia Marinha', 'esp32_aquario_04', TRUE);

-- =========================================================
-- 02. SENSORES
-- =========================================================
INSERT INTO sensores (nome, tipo, unidade, estacao_id, ativo, remoto_ativo, fator_calibracao, offset_calibracao) VALUES
('Temperatura Sala', 'TEMPERATURA', 'ºC', 1, TRUE, TRUE, 1.000000, 0.000000),
('TDS Sala', 'TDS', 'ppm', 1, TRUE, TRUE, 1.000000, 0.000000),
('pH Sala', 'PH', 'pH', 1, TRUE, TRUE, 1.000000, 0.000000),
('Temperatura Móvel Água', 'TEMPERATURA', 'ºC', 2, TRUE, TRUE, 1.000000, 0.000000),
('TDS Móvel Água', 'TDS', 'ppm', 2, TRUE, TRUE, 1.000000, 0.000000),
('pH Móvel Água', 'PH', 'pH', 2, TRUE, TRUE, 1.000000, 0.000000),
('Temperatura Estufa', 'TEMPERATURA', 'ºC', 3, TRUE, TRUE, 1.000000, 0.000000),
('TDS Estufa', 'TDS', 'ppm', 3, TRUE, TRUE, 1.000000, 0.000000),
('pH Estufa', 'PH', 'pH', 3, TRUE, TRUE, 1.000000, 0.000000),
('Temperatura Aquário', 'TEMPERATURA', 'ºC', 4, TRUE, TRUE, 1.000000, 0.000000),
('TDS Aquário', 'TDS', 'ppm', 4, TRUE, TRUE, 1.000000, 0.000000),
('pH Aquário', 'PH', 'pH', 4, TRUE, TRUE, 1.000000, 0.000000);

-- =========================================================
-- 03. UTILIZADORES
-- =========================================================
-- Todos têm password: password
INSERT INTO utilizadores (nome, email, password_hash, role, ativo) VALUES
('Admin IoT Room', 'a@a', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'ADMIN', TRUE),
('Professor Principal', 'p@p', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'PROFESSOR', TRUE),
('Professor João', 'j@j', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'PROFESSOR', TRUE),
('Aluno Bruno', 'b@b', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'ALUNO', TRUE),
('Aluna Carla', 'c@c', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'ALUNO', TRUE),
('Aluno Diogo', 'd@d', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'ALUNO', TRUE),
('Aluna Eva', 'e@e', '$2y$10$eXSOsLQnWNaWrqpJL4KvYunVEm8SZNGwEaSR.SmmbSNi7E6M68Zfi', 'ALUNO', TRUE);

-- =========================================================
-- 04. ROLES, GRUPOS E PERMISSÕES
-- =========================================================
INSERT INTO roles_grupo (nome, descricao) VALUES
('OWNER', 'Responsável principal pelo grupo'),
('MANAGER', 'Pode gerir membros, experiências e permissões'),
('OPERATOR', 'Pode consultar dados e operar sensores autorizados'),
('VIEWER', 'Pode consultar dados e histórico');

INSERT INTO grupos (professor_id, nome, descricao, ativo) VALUES
(2, 'Grupo Ambiente Sala A', 'Monitorização ambiental da sala principal.', TRUE),
(2, 'Grupo Qualidade da Água', 'Ensaios de TDS e pH em amostras de água.', TRUE),
(3, 'Grupo Estações Móveis', 'Comparação entre estações móveis e fixas.', TRUE);

UPDATE utilizadores SET grupo_id = 1 WHERE id IN (4, 5);
UPDATE utilizadores SET grupo_id = 2 WHERE id IN (6);
UPDATE utilizadores SET grupo_id = 3 WHERE id IN (7);

INSERT INTO utilizador_grupos (utilizador_id, grupo_id, role_grupo_id) VALUES
(2, 1, 1),
(4, 1, 3),
(5, 1, 4),
(2, 2, 1),
(6, 2, 3),
(3, 3, 1),
(7, 3, 3),
(4, 3, 4);

INSERT INTO permissoes_grupo_estacao (grupo_id, estacao_id) VALUES
(1, 1),
(1, 3),
(2, 2),
(2, 4),
(3, 1),
(3, 2),
(3, 3),
(3, 4);

INSERT INTO permissoes_utilizador_estacao (utilizador_id, estacao_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4),
(3, 1), (3, 2), (3, 3), (3, 4),
(4, 1), (4, 3),
(5, 1),
(6, 2), (6, 4),
(7, 2), (7, 3), (7, 4);

-- =========================================================
-- 05. EXPERIÊNCIAS
-- =========================================================
INSERT INTO experiencias (nome, descricao, data_inicio, estado, grupo_id, criado_por) VALUES
('Conforto térmico da sala IoT', 'Acompanhamento de temperatura, TDS e pH para demonstração em aula.', NOW() - INTERVAL 2 DAY, 'ATIVA', 1, 2),
('Qualidade da água - amostras A/B', 'Comparação de TDS e pH entre a estação móvel e o aquário experimental.', NOW() - INTERVAL 1 DAY, 'ATIVA', 2, 2),
('Comparação entre estações', 'Experiência preparada para comparar todos os dispositivos.', NOW(), 'CRIADA', 3, 3);

INSERT INTO experiencia_estacoes (experiencia_id, estacao_id, ordem, observacao) VALUES
(1, 1, 1, 'Estação principal dentro da sala'),
(1, 3, 2, 'Estufa usada para comparação térmica'),
(2, 2, 1, 'Estação móvel junto às amostras'),
(2, 4, 2, 'Aquário experimental'),
(3, 1, 1, 'Sala IoT'),
(3, 2, 2, 'Estação móvel'),
(3, 3, 3, 'Estufa'),
(3, 4, 4, 'Aquário');

-- =========================================================
-- 06. LEITURAS REALISTAS
-- =========================================================
INSERT INTO leituras_sensor (experiencia_id, data_registo, sensor_id, unidade, valor) VALUES
(1, NOW() - INTERVAL 70 MINUTE, 1, 'ºC', 22.10),
(1, NOW() - INTERVAL 60 MINUTE, 1, 'ºC', 22.25),
(1, NOW() - INTERVAL 50 MINUTE, 1, 'ºC', 22.35),
(1, NOW() - INTERVAL 40 MINUTE, 1, 'ºC', 22.50),
(1, NOW() - INTERVAL 30 MINUTE, 1, 'ºC', 22.70),
(1, NOW() - INTERVAL 20 MINUTE, 1, 'ºC', 22.95),
(1, NOW() - INTERVAL 10 MINUTE, 1, 'ºC', 23.10),
(1, NOW() - INTERVAL 0 MINUTE, 1, 'ºC', 23.30),
(1, NOW() - INTERVAL 70 MINUTE, 2, 'ppm', 455.00),
(1, NOW() - INTERVAL 60 MINUTE, 2, 'ppm', 460.00),
(1, NOW() - INTERVAL 50 MINUTE, 2, 'ppm', 458.00),
(1, NOW() - INTERVAL 40 MINUTE, 2, 'ppm', 462.00),
(1, NOW() - INTERVAL 30 MINUTE, 2, 'ppm', 470.00),
(1, NOW() - INTERVAL 20 MINUTE, 2, 'ppm', 468.00),
(1, NOW() - INTERVAL 10 MINUTE, 2, 'ppm', 472.00),
(1, NOW() - INTERVAL 0 MINUTE, 2, 'ppm', 475.00),
(1, NOW() - INTERVAL 70 MINUTE, 3, 'pH', 7.18),
(1, NOW() - INTERVAL 60 MINUTE, 3, 'pH', 7.20),
(1, NOW() - INTERVAL 50 MINUTE, 3, 'pH', 7.21),
(1, NOW() - INTERVAL 40 MINUTE, 3, 'pH', 7.19),
(1, NOW() - INTERVAL 30 MINUTE, 3, 'pH', 7.22),
(1, NOW() - INTERVAL 20 MINUTE, 3, 'pH', 7.24),
(1, NOW() - INTERVAL 10 MINUTE, 3, 'pH', 7.23),
(1, NOW() - INTERVAL 0 MINUTE, 3, 'pH', 7.25),
(1, NOW() - INTERVAL 70 MINUTE, 7, 'ºC', 25.30),
(1, NOW() - INTERVAL 60 MINUTE, 7, 'ºC', 25.70),
(1, NOW() - INTERVAL 50 MINUTE, 7, 'ºC', 26.20),
(1, NOW() - INTERVAL 40 MINUTE, 7, 'ºC', 26.80),
(1, NOW() - INTERVAL 30 MINUTE, 7, 'ºC', 27.40),
(1, NOW() - INTERVAL 20 MINUTE, 7, 'ºC', 28.00),
(1, NOW() - INTERVAL 10 MINUTE, 7, 'ºC', 28.70),
(1, NOW() - INTERVAL 0 MINUTE, 7, 'ºC', 29.20),
(1, NOW() - INTERVAL 70 MINUTE, 8, 'ppm', 520.00),
(1, NOW() - INTERVAL 60 MINUTE, 8, 'ppm', 530.00),
(1, NOW() - INTERVAL 50 MINUTE, 8, 'ppm', 535.00),
(1, NOW() - INTERVAL 40 MINUTE, 8, 'ppm', 545.00),
(1, NOW() - INTERVAL 30 MINUTE, 8, 'ppm', 560.00),
(1, NOW() - INTERVAL 20 MINUTE, 8, 'ppm', 575.00),
(1, NOW() - INTERVAL 10 MINUTE, 8, 'ppm', 590.00),
(1, NOW() - INTERVAL 0 MINUTE, 8, 'ppm', 610.00),
(1, NOW() - INTERVAL 70 MINUTE, 9, 'pH', 6.82),
(1, NOW() - INTERVAL 60 MINUTE, 9, 'pH', 6.80),
(1, NOW() - INTERVAL 50 MINUTE, 9, 'pH', 6.78),
(1, NOW() - INTERVAL 40 MINUTE, 9, 'pH', 6.75),
(1, NOW() - INTERVAL 30 MINUTE, 9, 'pH', 6.72),
(1, NOW() - INTERVAL 20 MINUTE, 9, 'pH', 6.70),
(1, NOW() - INTERVAL 10 MINUTE, 9, 'pH', 6.68),
(1, NOW() - INTERVAL 0 MINUTE, 9, 'pH', 6.65),
(2, NOW() - INTERVAL 70 MINUTE, 4, 'ºC', 20.60),
(2, NOW() - INTERVAL 60 MINUTE, 4, 'ºC', 20.70),
(2, NOW() - INTERVAL 50 MINUTE, 4, 'ºC', 20.75),
(2, NOW() - INTERVAL 40 MINUTE, 4, 'ºC', 20.80),
(2, NOW() - INTERVAL 30 MINUTE, 4, 'ºC', 20.95),
(2, NOW() - INTERVAL 20 MINUTE, 4, 'ºC', 21.05),
(2, NOW() - INTERVAL 10 MINUTE, 4, 'ºC', 21.10),
(2, NOW() - INTERVAL 0 MINUTE, 4, 'ºC', 21.20),
(2, NOW() - INTERVAL 70 MINUTE, 5, 'ppm', 320.00),
(2, NOW() - INTERVAL 60 MINUTE, 5, 'ppm', 340.00),
(2, NOW() - INTERVAL 50 MINUTE, 5, 'ppm', 360.00),
(2, NOW() - INTERVAL 40 MINUTE, 5, 'ppm', 390.00),
(2, NOW() - INTERVAL 30 MINUTE, 5, 'ppm', 420.00),
(2, NOW() - INTERVAL 20 MINUTE, 5, 'ppm', 455.00),
(2, NOW() - INTERVAL 10 MINUTE, 5, 'ppm', 490.00),
(2, NOW() - INTERVAL 0 MINUTE, 5, 'ppm', 530.00),
(2, NOW() - INTERVAL 70 MINUTE, 6, 'pH', 7.45),
(2, NOW() - INTERVAL 60 MINUTE, 6, 'pH', 7.42),
(2, NOW() - INTERVAL 50 MINUTE, 6, 'pH', 7.38),
(2, NOW() - INTERVAL 40 MINUTE, 6, 'pH', 7.35),
(2, NOW() - INTERVAL 30 MINUTE, 6, 'pH', 7.30),
(2, NOW() - INTERVAL 20 MINUTE, 6, 'pH', 7.25),
(2, NOW() - INTERVAL 10 MINUTE, 6, 'pH', 7.18),
(2, NOW() - INTERVAL 0 MINUTE, 6, 'pH', 7.10),
(2, NOW() - INTERVAL 70 MINUTE, 10, 'ºC', 24.10),
(2, NOW() - INTERVAL 60 MINUTE, 10, 'ºC', 24.00),
(2, NOW() - INTERVAL 50 MINUTE, 10, 'ºC', 23.90),
(2, NOW() - INTERVAL 40 MINUTE, 10, 'ºC', 23.85),
(2, NOW() - INTERVAL 30 MINUTE, 10, 'ºC', 23.80),
(2, NOW() - INTERVAL 20 MINUTE, 10, 'ºC', 23.75),
(2, NOW() - INTERVAL 10 MINUTE, 10, 'ºC', 23.70),
(2, NOW() - INTERVAL 0 MINUTE, 10, 'ºC', 23.65),
(2, NOW() - INTERVAL 70 MINUTE, 11, 'ppm', 680.00),
(2, NOW() - INTERVAL 60 MINUTE, 11, 'ppm', 700.00),
(2, NOW() - INTERVAL 50 MINUTE, 11, 'ppm', 725.00),
(2, NOW() - INTERVAL 40 MINUTE, 11, 'ppm', 750.00),
(2, NOW() - INTERVAL 30 MINUTE, 11, 'ppm', 780.00),
(2, NOW() - INTERVAL 20 MINUTE, 11, 'ppm', 810.00),
(2, NOW() - INTERVAL 10 MINUTE, 11, 'ppm', 845.00),
(2, NOW() - INTERVAL 0 MINUTE, 11, 'ppm', 880.00),
(2, NOW() - INTERVAL 70 MINUTE, 12, 'pH', 8.10),
(2, NOW() - INTERVAL 60 MINUTE, 12, 'pH', 8.15),
(2, NOW() - INTERVAL 50 MINUTE, 12, 'pH', 8.20),
(2, NOW() - INTERVAL 40 MINUTE, 12, 'pH', 8.28),
(2, NOW() - INTERVAL 30 MINUTE, 12, 'pH', 8.35),
(2, NOW() - INTERVAL 20 MINUTE, 12, 'pH', 8.42),
(2, NOW() - INTERVAL 10 MINUTE, 12, 'pH', 8.50),
(2, NOW() - INTERVAL 0 MINUTE, 12, 'pH', 8.60);

-- =========================================================
-- 07. CONFIGURAÇÕES
-- =========================================================
INSERT INTO configuracoes_sistema (chave, valor, tipo, descricao, atualizado_por) VALUES
('modo_manutencao', 'false', 'BOOLEAN', 'Modo manutenção', 1),
('permitir_calibracao_professor', 'true', 'BOOLEAN', 'Permitir calibração por professores', 1),
('permitir_controlo_remoto_professor', 'true', 'BOOLEAN', 'Permitir controlo remoto por professores', 1),
('permitir_pedidos_intervalo', 'true', 'BOOLEAN', 'Permitir pedidos de alteração de intervalos', 1),
('intervalo_minimo_ms', '1000', 'INTEIRO', 'Intervalo mínimo permitido em milissegundos', 1),
('intervalo_maximo_ms', '300000', 'INTEIRO', 'Intervalo máximo permitido em milissegundos', 1),
('intervalo_rapido_padrao_ms', '1000', 'INTEIRO', 'Intervalo rápido padrão', 1),
('intervalo_estavel_padrao_ms', '30000', 'INTEIRO', 'Intervalo estável padrão', 1),
('duracao_modo_rapido_padrao_ms', '120000', 'INTEIRO', 'Duração padrão do modo rápido', 1),
('retencao_leituras_dias', '365', 'INTEIRO', 'Dias de retenção das leituras', 1),
('retencao_logs_dias', '180', 'INTEIRO', 'Dias de retenção dos logs', 1),
('mqtt_host', '100.78.90.21', 'TEXTO', 'Host do broker MQTT', 1),
('mqtt_porta', '1883', 'INTEIRO', 'Porta do broker MQTT', 1),
('mqtt_topico_base', 'esp32', 'TEXTO', 'Tópico base MQTT', 1),
('timeout_sem_comunicacao_segundos', '120', 'INTEIRO', 'Tempo até considerar sensor sem comunicação', 1);

INSERT INTO configuracoes_modo_sensor (
    sensor_id, intervalo_rapido_ms, intervalo_estavel_ms, duracao_modo_rapido_ms,
    delta_significativo, atualizado_por
)
SELECT
    s.id,
    CASE WHEN s.tipo = 'TEMPERATURA' THEN 1000 ELSE 1500 END,
    CASE WHEN s.tipo = 'TEMPERATURA' THEN 30000 WHEN s.tipo = 'TDS' THEN 45000 ELSE 60000 END,
    120000,
    CASE
        WHEN s.tipo = 'TEMPERATURA' THEN 0.20
        WHEN s.tipo = 'TDS' THEN 5.00
        WHEN s.tipo = 'PH' THEN 0.10
        ELSE 1.00
    END,
    2
FROM sensores s;

-- =========================================================
-- 08. ALERTAS, AVISOS E FÓRUM
-- =========================================================
INSERT INTO avisos (criado_por, titulo, mensagem, ativo, expira_em) VALUES
(2, 'Aula prática ativa', 'A experiência de conforto térmico está disponível para consulta.', TRUE, NOW() + INTERVAL 7 DAY),
(2, 'Amostras de água', 'Confirmar calibração dos sensores TDS antes de recolher novos dados.', TRUE, NOW() + INTERVAL 5 DAY),
(1, 'Manutenção preventiva', 'Verificar alimentação das estações móveis no final da aula.', TRUE, NOW() + INTERVAL 10 DAY);

INSERT INTO forum_topicos (titulo, mensagem, criado_por, grupo_id, experiencia_id, estado) VALUES
('Interpretação dos dados de temperatura', 'A estufa apresenta uma subida gradual. Comparem com a sala principal.', 2, 1, 1, 'ABERTO'),
('Valores TDS no aquário', 'O TDS subiu acima do intervalo esperado. Validem a calibração.', 2, 2, 2, 'ABERTO'),
('Checklist das estações móveis', 'Registar aqui problemas encontrados nas estações móveis.', 3, 3, 3, 'ABERTO');

INSERT INTO forum_respostas (topico_id, autor_id, mensagem) VALUES
(1, 4, 'A curva da estufa parece acompanhar o aumento de temperatura ambiente.'),
(1, 5, 'Na sala principal os valores ficaram mais estáveis.'),
(2, 6, 'Vou repetir a leitura TDS depois de lavar a sonda.'),
(3, 7, 'A estação móvel está a comunicar, mas a bateria baixou rapidamente.');

INSERT INTO regras_alerta_sensor (
    professor_id, grupo_id, experiencia_id, estacao_id, tipo_sensor, operador,
    valor_min, valor_max, titulo, mensagem, severidade, cooldown_minutos
) VALUES
(2, 1, 1, NULL, 'TEMPERATURA', 'ACIMA_DE', NULL, 28.00, 'Temperatura elevada', 'Temperatura acima do limite definido para a experiência.', 'AVISO', 10),
(2, 2, 2, NULL, 'TDS', 'FORA_INTERVALO', 300.00, 800.00, 'TDS fora do intervalo', 'O TDS está fora do intervalo esperado.', 'CRITICO', 10),
(2, 2, 2, NULL, 'PH', 'FORA_INTERVALO', 6.50, 8.50, 'pH fora do intervalo', 'O pH está fora do intervalo seguro.', 'AVISO', 10);

INSERT INTO alertas_sensor (
    regra_id, leitura_id, professor_id, experiencia_id, grupo_id, estacao_id, sensor_id,
    tipo_sensor, valor_lido, valor_min, valor_max, titulo, mensagem, severidade, estado
) VALUES
(1, (SELECT id FROM leituras_sensor WHERE sensor_id = 7 ORDER BY data_registo DESC LIMIT 1), 2, 1, 1, 3, 7, 'TEMPERATURA', 29.20, NULL, 28.00, 'Temperatura elevada', 'A estufa ultrapassou 28 ºC.', 'AVISO', 'NOVO'),
(2, (SELECT id FROM leituras_sensor WHERE sensor_id = 11 ORDER BY data_registo DESC LIMIT 1), 2, 2, 2, 4, 11, 'TDS', 880.00, 300.00, 800.00, 'TDS fora do intervalo', 'O aquário ultrapassou o máximo esperado de TDS.', 'CRITICO', 'LIDO'),
(3, (SELECT id FROM leituras_sensor WHERE sensor_id = 12 ORDER BY data_registo DESC LIMIT 1), 2, 2, 2, 4, 12, 'PH', 8.60, 6.50, 8.50, 'pH fora do intervalo', 'O pH no aquário ultrapassou o valor máximo.', 'AVISO', 'NOVO');

-- =========================================================
-- 09. PEDIDOS E COMANDOS DE SENSOR
-- =========================================================
INSERT INTO comandos_sensor (
    professor_id, sensor_id, device_id, tipo_sensor, comando, estado,
    tentativas_envio, publicado_em, confirmado_em, resposta
) VALUES
(2, 2, 'esp32_sala_01', 'TDS', 'CALIBRAR_TDS:1.040000', 'CONFIRMADO', 1, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 119 MINUTE, 'OK'),
(2, 3, 'esp32_sala_01', 'PH', 'PH_OFFSET:-0.050000', 'ENVIADO', 1, NOW() - INTERVAL 30 MINUTE, NULL, NULL),
(2, 11, 'esp32_aquario_04', 'TDS', 'SET_MODO:rapido=1000;estavel=30000;duracao=120000;delta=5.0', 'ENVIADO', 1, NOW() - INTERVAL 15 MINUTE, NULL, NULL);

INSERT INTO pedidos_configuracao_sensor (
    sensor_id, solicitado_por, analisado_por, origem, estado,
    intervalo_rapido_ms, intervalo_estavel_ms, duracao_modo_rapido_ms,
    delta_significativo, motivo, resposta_professor, comando_id,
    criado_em, analisado_em
) VALUES
(11, 6, 2, 'PROFESSOR', 'APROVADO', 1000, 30000, 120000, 5.0000, 'Amostras com alteração rápida de TDS.', 'Aprovado para modo rápido durante a aula.', 3, NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 20 MINUTE),
(12, 6, NULL, 'PROFESSOR', 'PENDENTE', 1000, 45000, 120000, 0.1000, 'Pretendo acompanhar alteração de pH em tempo quase real.', NULL, NULL, NOW() - INTERVAL 10 MINUTE, NULL),
(7, NULL, NULL, 'ESP32', 'PENDENTE', 1000, 20000, 120000, 0.2000, 'Variação térmica detetada pela estação.', NULL, NULL, NOW() - INTERVAL 5 MINUTE, NULL);

-- =========================================================
-- 10. TIPOS DE LOG E LOGS
-- =========================================================
INSERT INTO tipos_log (nome, descricao) VALUES
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

INSERT INTO logs (tipo_log_id, utilizador_id, estacao_id, experiencia_id, nivel, acao, mensagem, ip, dispositivo, criado_em) VALUES
((SELECT id FROM tipos_log WHERE nome='SISTEMA'), 1, NULL, NULL, 'INFO', 'DATABASE_POPULATE', 'Dados melhorados carregados com sucesso.', '127.0.0.1', 'populate_melhorado.sql', NOW()),
((SELECT id FROM tipos_log WHERE nome='EXPERIENCIA'), 2, 1, 1, 'INFO', 'EXPERIENCIA_ATIVA', 'Experiência de conforto térmico ativa.', '127.0.0.1', 'populate_melhorado.sql', NOW() - INTERVAL 2 DAY),
((SELECT id FROM tipos_log WHERE nome='ALERTA'), 2, 4, 2, 'WARNING', 'ALERTA_TDS', 'TDS elevado no aquário experimental.', '127.0.0.1', 'populate_melhorado.sql', NOW() - INTERVAL 20 MINUTE),
((SELECT id FROM tipos_log WHERE nome='COMANDO_SENSOR'), 2, 1, 1, 'INFO', 'COMANDO_CONFIRMADO', 'Comando de calibração TDS confirmado.', '127.0.0.1', 'populate_melhorado.sql', NOW() - INTERVAL 119 MINUTE);

INSERT INTO logs_detalhes (log_id, dados) VALUES
(1, JSON_OBJECT('ficheiro', 'populate_melhorado.sql', 'utilizadores_teste', JSON_ARRAY('a@a','p@p','j@j','b@b','c@c','d@d','e@e'))),
(3, JSON_OBJECT('sensor_id', 11, 'valor_lido', 880.00, 'limite_max', 800.00));

INSERT INTO security_audit_logs (utilizador_id, tipo, detalhe, app_client, ip, user_agent) VALUES
(1, 'SEED_ADMIN_CREATED', 'Utilizador admin de teste criado pelo populate.', 'WEB', '127.0.0.1', 'populate_melhorado.sql'),
(2, 'SEED_PROFESSOR_CREATED', 'Professor principal de teste criado pelo populate.', 'WEB', '127.0.0.1', 'populate_melhorado.sql'),
(NULL, 'SEED_DATASET_READY', 'Dataset de demonstração carregado.', 'SYSTEM', '127.0.0.1', 'populate_melhorado.sql');

-- =========================================================
-- VERIFICAÇÃO RÁPIDA
-- =========================================================
SELECT
    'POPULATE_MELHORADO_OK' AS estado,
    (SELECT COUNT(*) FROM utilizadores) AS utilizadores,
    (SELECT COUNT(*) FROM estacoes) AS estacoes,
    (SELECT COUNT(*) FROM sensores) AS sensores,
    (SELECT COUNT(*) FROM leituras_sensor) AS leituras,
    (SELECT COUNT(*) FROM experiencias) AS experiencias;
