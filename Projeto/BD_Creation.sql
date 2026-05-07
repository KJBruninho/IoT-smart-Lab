-- 1. Criar a Base de Dados
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'DATABASE_51619')
BEGIN
    CREATE DATABASE DATABASE_51619;
END
GO

USE DATABASE_51619;
GO

-- 2. Criar Tabelas
CREATE TABLE [Group] (
    ID_Group INT PRIMARY KEY IDENTITY(1,1),
    Nome_Group VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE Login (
    ID_Login INT PRIMARY KEY IDENTITY(1,1),
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password_Hash VARCHAR(255) NOT NULL,
    ID_Group INT UNIQUE NOT NULL,
    CONSTRAINT FK_Login_Group FOREIGN KEY (ID_Group) REFERENCES [Group](ID_Group) ON DELETE CASCADE
);

CREATE TABLE Element (
    ID_Element INT PRIMARY KEY IDENTITY(1,1),
    Nome VARCHAR(100) NOT NULL,
    Num_Aluno INT UNIQUE NOT NULL,
    ID_Group INT NOT NULL,
    CONSTRAINT FK_Element_Group FOREIGN KEY (ID_Group) REFERENCES [Group](ID_Group)
);

CREATE TABLE Station (
    ID_Station INT PRIMARY KEY IDENTITY(1,1),
    Localizacao VARCHAR(100),
    ID_Group INT UNIQUE NOT NULL,
    CONSTRAINT FK_Station_Group FOREIGN KEY (ID_Group) REFERENCES [Group](ID_Group)
);

CREATE TABLE Sensor (
    ID_Sensor INT PRIMARY KEY IDENTITY(1,1),
    Tipo_Sensor VARCHAR(50) NOT NULL,
    Unidade_Medida VARCHAR(10),
    ID_Station INT NOT NULL,
    CONSTRAINT FK_Sensor_Station FOREIGN KEY (ID_Station) REFERENCES Station(ID_Station)
);

CREATE TABLE [Data] (
    ID_Data INT PRIMARY KEY IDENTITY(1,1),
    Valor FLOAT NOT NULL,
    [Timestamp] DATETIME DEFAULT GETDATE(),
    ID_Sensor INT NOT NULL,
    CONSTRAINT FK_Data_Sensor FOREIGN KEY (ID_Sensor) REFERENCES Sensor(ID_Sensor)
);

CREATE TABLE Logs (
    ID_Log INT PRIMARY KEY IDENTITY(1,1),
    Acao VARCHAR(255),
    Data_Hora DATETIME DEFAULT GETDATE(),
    ID_Element INT,
    CONSTRAINT FK_Logs_Element FOREIGN KEY (ID_Element) REFERENCES Element(ID_Element)
);
GO

-- 3. Inserir Dados (DML)

-- Grupos
INSERT INTO [Group] (Nome_Group) VALUES 
('Alpha_Tech'), ('Beta_Sensing'), ('Gamma_Solar'), ('Delta_Eco'), ('Epsilon_IoT');

-- Logins
INSERT INTO Login (Username, Password_Hash, ID_Group) VALUES 
('user_alpha', 'hash123', 1), ('user_beta', 'hash456', 2),
('user_gamma', 'hash789', 3), ('user_delta', 'hashabc', 4),
('user_epsilon', 'hashdef', 5);

-- Elementos
INSERT INTO Element (Nome, Num_Aluno, ID_Group) VALUES 
('Joao Silva', 2023001, 1), ('Maria Santos', 2023002, 1), ('José Costa', 2023003, 1),
('Ricardo Pereira', 2023004, 2), ('Ana Marta', 2023005, 2),
('Luis Rocha', 2023006, 3), ('Catarina Vale', 2023007, 3),
('Paulo Bento', 2023008, 4), ('Sofia Cruz', 2023009, 4),
('Miguel Antunes', 2023010, 5), ('Beatriz Luz', 2023011, 5), ('Nuno Reis', 2023012, 5);

-- Estacoes
INSERT INTO Station (Localizacao, ID_Group) VALUES 
('Laboratório 1 - Piso 0', 1),
('Estufa Exterior - Zona Sul', 2),
('Telhado Bloco C', 3),
('Jardim Central', 4),
('Sala de Servidores', 5);

-- Sensores
INSERT INTO Sensor (Tipo_Sensor, Unidade_Medida, ID_Station) VALUES 
('Temperatura', '°C', 1), ('Humidade', '%', 1),
('Luminosidade', 'Lux', 2), ('Humidade Solo', '%', 2),
('UV Index', 'Index', 3), ('Velocidade Vento', 'km/h', 3),
('Qualidade Ar', 'AQI', 4), ('CO2', 'ppm', 4),
('Temperatura Rack', '°C', 5), ('Consumo Energia', 'W', 5);

-- Leituras (Data)
INSERT INTO [Data] (Valor, ID_Sensor) VALUES 
(21.5, 1), (22.1, 1), (21.8, 1), (22.3, 1),
(45.2, 2), (46.0, 2), (44.9, 2), (45.5, 2),
(1200, 3), (1150, 3), (1300, 3), (1050, 3),
(12.5, 4), (12.8, 4), (12.2, 4), (11.9, 4),
(5.2, 5), (5.5, 5), (4.9, 5), (5.1, 5),
(12.4, 6), (15.1, 6), (10.2, 6), (18.5, 6),
(52, 7), (55, 7), (48, 7), (50, 7),
(410, 8), (415, 8), (405, 8), (412, 8),
(19.5, 9), (19.8, 9), (20.1, 9), (19.2, 9),
(150.5, 10), (155.2, 10), (148.9, 10), (152.0, 10);

-- Logs
INSERT INTO Logs (Acao, ID_Element) VALUES 
('Login efetuado', 1),
('Configuracao de sensor', 4),
('Leitura manual disparada', 6),
('Exportacao de dados CSV', 8),
('Login efetuado', 10);
GO