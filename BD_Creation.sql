/* =========================================================
   DATABASE_51619 - VERSAO COMPLETA MELHORADA
   ========================================================= */

------------------------------------------------------------
-- 1. CRIAR DATABASE
------------------------------------------------------------

IF NOT EXISTS (
    SELECT *
    FROM sys.databases
    WHERE name = 'DATABASE_51619'
)
BEGIN
    CREATE DATABASE DATABASE_51619;
END
GO

USE DATABASE_51619;
GO

------------------------------------------------------------
-- 2. REMOVER OBJETOS ANTIGOS (CASO EXISTAM)
------------------------------------------------------------

IF OBJECT_ID('VW_Leituras_Completas', 'V') IS NOT NULL
    DROP VIEW VW_Leituras_Completas;
GO

IF OBJECT_ID('VW_Media_Sensores', 'V') IS NOT NULL
    DROP VIEW VW_Media_Sensores;
GO

IF OBJECT_ID('VW_Atividade_Utilizadores', 'V') IS NOT NULL
    DROP VIEW VW_Atividade_Utilizadores;
GO

IF OBJECT_ID('VW_Ultima_Leitura', 'V') IS NOT NULL
    DROP VIEW VW_Ultima_Leitura;
GO

IF OBJECT_ID('SP_InserirLeitura', 'P') IS NOT NULL
    DROP PROCEDURE SP_InserirLeitura;
GO

IF OBJECT_ID('SP_LeiturasPorSensor', 'P') IS NOT NULL
    DROP PROCEDURE SP_LeiturasPorSensor;
GO

IF OBJECT_ID('SP_SensoresEstacao', 'P') IS NOT NULL
    DROP PROCEDURE SP_SensoresEstacao;
GO

IF OBJECT_ID('FN_MediaSensor', 'FN') IS NOT NULL
    DROP FUNCTION FN_MediaSensor;
GO

IF OBJECT_ID('FN_MaxSensor', 'FN') IS NOT NULL
    DROP FUNCTION FN_MaxSensor;
GO

IF OBJECT_ID('TRG_Log_Nova_Leitura', 'TR') IS NOT NULL
    DROP TRIGGER TRG_Log_Nova_Leitura;
GO

------------------------------------------------------------
-- 3. TABELAS
------------------------------------------------------------

CREATE TABLE UserGroup (
    ID_Group INT PRIMARY KEY IDENTITY(1,1),
    Nome_Group VARCHAR(50) UNIQUE NOT NULL
);
GO

CREATE TABLE LoginUser (
    ID_Login INT PRIMARY KEY IDENTITY(1,1),

    Username VARCHAR(50) UNIQUE NOT NULL,

    Password_Hash VARCHAR(256) NOT NULL,

    ID_Group INT NOT NULL,

    CONSTRAINT FK_Login_Group
        FOREIGN KEY (ID_Group)
        REFERENCES UserGroup(ID_Group)
        ON DELETE CASCADE,

    CONSTRAINT CHK_Login_Username
        CHECK (LEN(Username) >= 4)
);
GO

CREATE TABLE Element (
    ID_Element INT PRIMARY KEY IDENTITY(1,1),

    Nome VARCHAR(100) NOT NULL,

    Num_Aluno INT UNIQUE NOT NULL,

    ID_Group INT NOT NULL,

    CONSTRAINT FK_Element_Group
        FOREIGN KEY (ID_Group)
        REFERENCES UserGroup(ID_Group)
);
GO

CREATE TABLE Station (
    ID_Station INT PRIMARY KEY IDENTITY(1,1),

    Localizacao VARCHAR(100) NOT NULL,

    ID_Group INT UNIQUE NOT NULL,

    CONSTRAINT FK_Station_Group
        FOREIGN KEY (ID_Group)
        REFERENCES UserGroup(ID_Group)
        ON DELETE CASCADE
);
GO

CREATE TABLE SensorType (
    ID_SensorType INT PRIMARY KEY IDENTITY(1,1),

    Nome_Tipo VARCHAR(50) UNIQUE NOT NULL,

    Unidade_Medida VARCHAR(20) NOT NULL
);
GO

CREATE TABLE Sensor (
    ID_Sensor INT PRIMARY KEY IDENTITY(1,1),

    ID_SensorType INT NOT NULL,

    ID_Station INT NOT NULL,

    Ativo BIT DEFAULT 1,

    CONSTRAINT FK_Sensor_Station
        FOREIGN KEY (ID_Station)
        REFERENCES Station(ID_Station)
        ON DELETE CASCADE,

    CONSTRAINT FK_Sensor_SensorType
        FOREIGN KEY (ID_SensorType)
        REFERENCES SensorType(ID_SensorType)
);
GO

CREATE TABLE SensorData (
    ID_Data INT PRIMARY KEY IDENTITY(1,1),

    Valor DECIMAL(10,2) NOT NULL,

    [Timestamp] DATETIME DEFAULT GETDATE(),

    ID_Sensor INT NOT NULL,

    CONSTRAINT FK_Data_Sensor
        FOREIGN KEY (ID_Sensor)
        REFERENCES Sensor(ID_Sensor)
        ON DELETE CASCADE,

    CONSTRAINT CHK_Data_Valor
        CHECK (Valor >= 0)
);
GO

CREATE TABLE Logs (
    ID_Log INT PRIMARY KEY IDENTITY(1,1),

    Acao VARCHAR(255),

    Data_Hora DATETIME DEFAULT GETDATE(),

    ID_Element INT NULL,

    CONSTRAINT FK_Logs_Element
        FOREIGN KEY (ID_Element)
        REFERENCES Element(ID_Element)
);
GO

CREATE TABLE Alerts (
    ID_Alert INT PRIMARY KEY IDENTITY(1,1),

    ID_Sensor INT NOT NULL,

    Valor DECIMAL(10,2),

    Mensagem VARCHAR(255),

    Data_Alerta DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_Alerts_Sensor
        FOREIGN KEY (ID_Sensor)
        REFERENCES Sensor(ID_Sensor)
        ON DELETE CASCADE
);
GO

------------------------------------------------------------
-- 4. INDEXES
------------------------------------------------------------

CREATE INDEX IDX_Element_Nome
ON Element(Nome);
GO

CREATE INDEX IDX_SensorData_Timestamp
ON SensorData([Timestamp]);
GO

CREATE INDEX IDX_SensorData_Sensor
ON SensorData(ID_Sensor);
GO

CREATE INDEX IDX_SensorData_Sensor_Timestamp
ON SensorData(ID_Sensor, [Timestamp] DESC);
GO

CREATE INDEX IDX_Logs_DataHora
ON Logs(Data_Hora);
GO

CREATE INDEX IDX_Station_Localizacao
ON Station(Localizacao);
GO

------------------------------------------------------------
-- 5. VIEWS
------------------------------------------------------------

CREATE VIEW VW_Leituras_Completas AS
SELECT
    d.ID_Data,
    stp.Nome_Tipo,
    d.Valor,
    stp.Unidade_Medida,
    d.[Timestamp],
    st.Localizacao,
    g.Nome_Group
FROM SensorData d
INNER JOIN Sensor s
    ON d.ID_Sensor = s.ID_Sensor
INNER JOIN SensorType stp
    ON s.ID_SensorType = stp.ID_SensorType
INNER JOIN Station st
    ON s.ID_Station = st.ID_Station
INNER JOIN UserGroup g
    ON st.ID_Group = g.ID_Group;
GO

CREATE VIEW VW_Media_Sensores AS
SELECT
    s.ID_Sensor,
    stp.Nome_Tipo,
    AVG(d.Valor) AS Media_Valores
FROM Sensor s
INNER JOIN SensorData d
    ON s.ID_Sensor = d.ID_Sensor
INNER JOIN SensorType stp
    ON s.ID_SensorType = stp.ID_SensorType
GROUP BY s.ID_Sensor, stp.Nome_Tipo;
GO

CREATE VIEW VW_Atividade_Utilizadores AS
SELECT
    e.Nome,
    l.Acao,
    l.Data_Hora
FROM Logs l
INNER JOIN Element e
    ON l.ID_Element = e.ID_Element;
GO

CREATE VIEW VW_Ultima_Leitura AS
SELECT
    stp.Nome_Tipo,
    d.Valor,
    d.[Timestamp]
FROM Sensor s
INNER JOIN SensorType stp
    ON s.ID_SensorType = stp.ID_SensorType
INNER JOIN SensorData d
    ON s.ID_Sensor = d.ID_Sensor
WHERE d.[Timestamp] =
(
    SELECT MAX(d2.[Timestamp])
    FROM SensorData d2
    WHERE d2.ID_Sensor = s.ID_Sensor
);
GO

------------------------------------------------------------
-- 6. FUNCTIONS
------------------------------------------------------------

CREATE FUNCTION FN_MediaSensor
(
    @ID_Sensor INT
)
RETURNS DECIMAL(10,2)
AS
BEGIN

    DECLARE @Media DECIMAL(10,2);

    SELECT @Media = AVG(Valor)
    FROM SensorData
    WHERE ID_Sensor = @ID_Sensor;

    RETURN @Media;

END;
GO

CREATE FUNCTION FN_MaxSensor
(
    @ID_Sensor INT
)
RETURNS DECIMAL(10,2)
AS
BEGIN

    DECLARE @Max DECIMAL(10,2);

    SELECT @Max = MAX(Valor)
    FROM SensorData
    WHERE ID_Sensor = @ID_Sensor;

    RETURN @Max;

END;
GO

------------------------------------------------------------
-- 7. STORED PROCEDURES
------------------------------------------------------------

CREATE PROCEDURE SP_InserirLeitura
    @Valor DECIMAL(10,2),
    @ID_Sensor INT
AS
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM Sensor
        WHERE ID_Sensor = @ID_Sensor
    )
    BEGIN
        RAISERROR('Sensor nao existe.',16,1);
        RETURN;
    END

    INSERT INTO SensorData(Valor, ID_Sensor)
    VALUES (@Valor, @ID_Sensor);

END;
GO

CREATE PROCEDURE SP_LeiturasPorSensor
    @ID_Sensor INT
AS
BEGIN

    SELECT *
    FROM SensorData
    WHERE ID_Sensor = @ID_Sensor
    ORDER BY [Timestamp] DESC;

END;
GO

CREATE PROCEDURE SP_SensoresEstacao
    @ID_Station INT
AS
BEGIN

    SELECT
        s.ID_Sensor,
        stp.Nome_Tipo,
        stp.Unidade_Medida
    FROM Sensor s
    INNER JOIN SensorType stp
        ON s.ID_SensorType = stp.ID_SensorType
    WHERE s.ID_Station = @ID_Station;

END;
GO

------------------------------------------------------------
-- 8. TRIGGERS
------------------------------------------------------------

CREATE TRIGGER TRG_Log_Nova_Leitura
ON SensorData
AFTER INSERT
AS
BEGIN

    INSERT INTO Logs(Acao)
    SELECT CONCAT(
        'Nova leitura inserida | Sensor ',
        ID_Sensor,
        ' | Valor ',
        Valor
    )
    FROM inserted;

END;
GO

CREATE TRIGGER TRG_Alerta_Valor_Alto
ON SensorData
AFTER INSERT
AS
BEGIN

    INSERT INTO Alerts(ID_Sensor, Valor, Mensagem)
    SELECT
        ID_Sensor,
        Valor,
        'Valor acima do limite'
    FROM inserted
    WHERE Valor > 1000;

END;
GO

------------------------------------------------------------
-- 9. SEGURANCA
------------------------------------------------------------

IF NOT EXISTS (
    SELECT *
    FROM sys.server_principals
    WHERE name = 'leitor_db'
)
BEGIN
    CREATE LOGIN leitor_db
    WITH PASSWORD = 'Password123!';
END
GO

IF NOT EXISTS (
    SELECT *
    FROM sys.database_principals
    WHERE name = 'leitor_db'
)
BEGIN
    CREATE USER leitor_db
    FOR LOGIN leitor_db;
END
GO

ALTER ROLE db_datareader
ADD MEMBER leitor_db;
GO

------------------------------------------------------------
-- 10. DADOS
------------------------------------------------------------

INSERT INTO UserGroup (Nome_Group)
VALUES
('Alpha_Tech'),
('Beta_Sensing'),
('Gamma_Solar'),
('Delta_Eco'),
('Epsilon_IoT');
GO

INSERT INTO LoginUser
(
    Username,
    Password_Hash,
    ID_Group
)
VALUES
(
    'user_alpha',
    CONVERT(VARCHAR(256),
        HASHBYTES('SHA2_256','Alpha123'),
    2),
    1
),
(
    'user_beta',
    CONVERT(VARCHAR(256),
        HASHBYTES('SHA2_256','Beta123'),
    2),
    2
),
(
    'user_gamma',
    CONVERT(VARCHAR(256),
        HASHBYTES('SHA2_256','Gamma123'),
    2),
    3
),
(
    'user_delta',
    CONVERT(VARCHAR(256),
        HASHBYTES('SHA2_256','Delta123'),
    2),
    4
),
(
    'user_epsilon',
    CONVERT(VARCHAR(256),
        HASHBYTES('SHA2_256','Epsilon123'),
    2),
    5
);
GO

INSERT INTO Element (Nome, Num_Aluno, ID_Group)
VALUES
('Joao Silva',2023001,1),
('Maria Santos',2023002,1),
('Jose Costa',2023003,1),
('Ricardo Pereira',2023004,2),
('Ana Marta',2023005,2),
('Luis Rocha',2023006,3),
('Catarina Vale',2023007,3),
('Paulo Bento',2023008,4),
('Sofia Cruz',2023009,4),
('Miguel Antunes',2023010,5);
GO

INSERT INTO Station(Localizacao, ID_Group)
VALUES
('Laboratorio 1 - Piso 0',1),
('Estufa Exterior - Zona Sul',2),
('Telhado Bloco C',3),
('Jardim Central',4),
('Sala de Servidores',5);
GO

INSERT INTO SensorType
(Nome_Tipo, Unidade_Medida)
VALUES
('Temperatura','C'),
('Humidade','%'),
('Luminosidade','Lux'),
('Humidade Solo','%'),
('UV Index','Index'),
('Velocidade Vento','km/h'),
('Qualidade Ar','AQI'),
('CO2','ppm'),
('Temperatura Rack','C'),
('Consumo Energia','W');
GO

INSERT INTO Sensor
(ID_SensorType, ID_Station)
VALUES
(1,1),
(2,1),
(3,2),
(4,2),
(5,3),
(6,3),
(7,4),
(8,4),
(9,5),
(10,5);
GO

INSERT INTO SensorData
(Valor, ID_Sensor)
VALUES
(21.50,1),
(22.10,1),
(45.20,2),
(46.00,2),
(1200.00,3),
(1150.00,3),
(12.50,4),
(12.80,4),
(5.20,5),
(5.50,5),
(12.40,6),
(15.10,6),
(52.00,7),
(55.00,7),
(410.00,8),
(415.00,8),
(19.50,9),
(20.10,9),
(150.50,10),
(155.20,10);
GO

INSERT INTO Logs(Acao, ID_Element)
VALUES
('Login efetuado',1),
('Configuracao de sensor',4),
('Leitura manual disparada',6),
('Exportacao CSV',8),
('Login efetuado',10);
GO

------------------------------------------------------------
-- 11. TESTES
------------------------------------------------------------

EXEC SP_InserirLeitura
    @Valor = 23.80,
    @ID_Sensor = 1;
GO

EXEC SP_LeiturasPorSensor
    @ID_Sensor = 1;
GO

EXEC SP_SensoresEstacao
    @ID_Station = 1;
GO

SELECT *
FROM VW_Leituras_Completas;
GO

SELECT *
FROM VW_Media_Sensores;
GO

SELECT *
FROM VW_Ultima_Leitura;
GO

SELECT dbo.FN_MediaSensor(1) AS MediaSensor;
GO

SELECT dbo.FN_MaxSensor(1) AS MaxSensor;
GO

SELECT *
FROM Alerts;
GO