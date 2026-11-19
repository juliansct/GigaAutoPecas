DROP DATABASE IF EXISTS `gigapeças_db`;
CREATE DATABASE `gigapeças_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `gigapeças_db`;

CREATE TABLE funcionario (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    usuario  VARCHAR(50)  NOT NULL,
    senha    VARCHAR(255) NOT NULL,
    cargo    VARCHAR(50)  NOT NULL,
    -- TRUE = está com senha provisória (cadastro novo ou redefinida pelo gerente)
    --        e precisa criar a própria senha no próximo login
    precisa_trocar_senha  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_funcionario_usuario UNIQUE (usuario)
);

CREATE TABLE categoria (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(60)  NOT NULL,
    descricao  VARCHAR(255) NULL,
    CONSTRAINT uq_categoria_nome UNIQUE (nome)
);

CREATE TABLE fornecedor (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia  VARCHAR(100) NOT NULL,
    cnpj           VARCHAR(18)  NULL,
    telefone       VARCHAR(20)  NULL,
    email          VARCHAR(100) NULL,
    CONSTRAINT uq_fornecedor_cnpj UNIQUE (cnpj)
);

CREATE TABLE peca (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras       VARCHAR(30)    NULL,
    nome                VARCHAR(120)   NOT NULL,
    aplicacao           VARCHAR(150)   NULL,
    preco_custo         DECIMAL(10,2)  NOT NULL DEFAULT 0,
    preco_venda         DECIMAL(10,2)  NOT NULL DEFAULT 0,
    quantidade_estoque  INT            NOT NULL DEFAULT 0,
    estoque_minimo      INT            NOT NULL DEFAULT 5,
    categoria_id        INT            NULL,
    fornecedor_id       INT            NULL,
    CONSTRAINT uq_peca_codigo UNIQUE (codigo_barras),
    CONSTRAINT ck_peca_estoque CHECK (quantidade_estoque >= 0),
    CONSTRAINT fk_peca_categoria  FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)  ON DELETE SET NULL,
    CONSTRAINT fk_peca_fornecedor FOREIGN KEY (fornecedor_id)
        REFERENCES fornecedor(id) ON DELETE SET NULL
);

CREATE TABLE venda (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    data_hora       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    funcionario_id  INT            NULL,
    total           DECIMAL(10,2)  NOT NULL,
    forma_pagamento ENUM('DINHEIRO', 'PIX', 'DEBITO', 'CREDITO') NOT NULL DEFAULT 'DINHEIRO',
    INDEX idx_venda_data (data_hora),
    CONSTRAINT fk_venda_funcionario FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id) ON DELETE SET NULL
);

CREATE TABLE item_venda (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    venda_id        INT            NOT NULL,
    peca_id         INT            NULL,
    nome_peca       VARCHAR(120)   NOT NULL,
    quantidade      INT            NOT NULL,
    preco_unitario  DECIMAL(10,2)  NOT NULL,
    custo_unitario  DECIMAL(10,2)  NOT NULL,
    CONSTRAINT ck_item_qtd CHECK (quantidade > 0),
    CONSTRAINT fk_item_venda FOREIGN KEY (venda_id)
        REFERENCES venda(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_peca FOREIGN KEY (peca_id)
        REFERENCES peca(id) ON DELETE SET NULL
);

CREATE TABLE movimentacao_estoque (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    peca_id         INT                       NOT NULL,
    tipo            ENUM('ENTRADA', 'SAIDA')  NOT NULL,
    quantidade      INT                       NOT NULL,
    data_hora       DATETIME                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    funcionario_id  INT                       NULL,
    fornecedor_id   INT                       NULL,
    venda_id        INT                       NULL,
    INDEX idx_mov_peca_data (peca_id, data_hora),
    CONSTRAINT ck_mov_qtd CHECK (quantidade > 0),
    CONSTRAINT fk_mov_peca FOREIGN KEY (peca_id)
        REFERENCES peca(id) ON DELETE CASCADE,
    CONSTRAINT fk_mov_funcionario FOREIGN KEY (funcionario_id)
        REFERENCES funcionario(id) ON DELETE SET NULL,
    CONSTRAINT fk_mov_fornecedor FOREIGN KEY (fornecedor_id)
        REFERENCES fornecedor(id) ON DELETE SET NULL,
    CONSTRAINT fk_mov_venda FOREIGN KEY (venda_id)
        REFERENCES venda(id) ON DELETE CASCADE
);

INSERT INTO categoria (nome, descricao) VALUES
    ('Freios',      'Pastilhas, discos, lonas, fluido'),
    ('Filtros',     'Óleo, ar, combustível, cabine'),
    ('Suspensão',   'Amortecedores, molas, bandejas, pivôs'),
    ('Motor',       'Correias, velas, juntas, bombas'),
    ('Elétrica',    'Baterias, alternadores, sensores'),
    ('Iluminação',  'Lâmpadas, faróis, lanternas');

START TRANSACTION;

INSERT INTO funcionario (nome, usuario, senha, cargo, precisa_trocar_senha) VALUES
    ('Mariana Costa',    'mariana', SHA2(UUID(), 256), 'Gerente de Vendas', TRUE),
    ('Carla Mendes',     'carla',   SHA2(UUID(), 256), 'Vendedor',          TRUE),
    ('João Pedro Silva', 'joao',    SHA2(UUID(), 256), 'Estoquista',        TRUE);

INSERT INTO fornecedor (nome_fantasia, cnpj, telefone, email) VALUES
    ('Distribuidora Freios SP', '12.345.678/0001-90', '(11) 3333-4444', 'vendas@freiossp.com.br'),
    ('AutoFiltros Brasil', '98.765.432/0001-10', '(19) 3222-1111', 'contato@autofiltros.com.br'),
    ('Elétrica Total Peças', '45.678.123/0001-55', '(11) 2555-7788', 'pedidos@eletricatotal.com.br'),
    ('Suspensão Forte Ltda', '33.221.554/0001-02', '(31) 3444-9090', 'comercial@suspensaoforte.com.br');

INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, quantidade_estoque, estoque_minimo, categoria_id, fornecedor_id) VALUES
    ('7891234500011', 'Pastilha de Freio Dianteira', 'Gol G5 / Fox / Voyage', 62.50, 119.90, 35, 6, (SELECT id FROM categoria WHERE nome = 'Freios'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ('7891234500104', 'Disco de Freio Ventilado (par)', 'Onix / Prisma', 145.00, 259.90, 9, 3, (SELECT id FROM categoria WHERE nome = 'Freios'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ('7891234500111', 'Fluido de Freio DOT 4 500ml', 'Universal', 14.00, 29.90, 27, 8, (SELECT id FROM categoria WHERE nome = 'Freios'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ('7891234500028', 'Filtro de Óleo', 'Onix 1.0 / Prisma', 14.90, 32.00, 81, 10, (SELECT id FROM categoria WHERE nome = 'Filtros'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500128', 'Filtro de Ar do Motor', 'HB20 1.0 / 1.6', 22.00, 45.90, 20, 8, (SELECT id FROM categoria WHERE nome = 'Filtros'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500135', 'Filtro de Combustível', 'Palio / Uno / Siena', 18.50, 38.90, 6, 6, (SELECT id FROM categoria WHERE nome = 'Filtros'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500142', 'Filtro de Cabine (ar-condicionado)', 'Corolla 2015-2019', 27.00, 59.90, 11, 5, (SELECT id FROM categoria WHERE nome = 'Filtros'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500035', 'Amortecedor Traseiro', 'HB20 2013-2019', 189.00, 329.90, 4, 4, (SELECT id FROM categoria WHERE nome = 'Suspensão'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ('7891234500159', 'Pivô de Suspensão', 'Gol / Parati / Saveiro', 38.00, 74.90, 1, 4, (SELECT id FROM categoria WHERE nome = 'Suspensão'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ('7891234500166', 'Bieleta Dianteira', 'Civic 2012-2016', 42.00, 84.90, 10, 4, (SELECT id FROM categoria WHERE nome = 'Suspensão'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ('7891234500059', 'Correia Dentada', 'Corsa 1.0 / Celta', 55.00, 104.50, 8, 5, (SELECT id FROM categoria WHERE nome = 'Motor'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500042', 'Vela de Ignição (jogo com 4)', 'Palio / Uno Fire', 48.00, 89.90, 13, 4, (SELECT id FROM categoria WHERE nome = 'Motor'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ('7891234500173', 'Bomba d''Água', 'Fiesta / Ka 1.0', 95.00, 179.90, 6, 3, (SELECT id FROM categoria WHERE nome = 'Motor'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ('7891234500066', 'Bateria 60Ah', 'Universal', 320.00, 549.00, 5, 3, (SELECT id FROM categoria WHERE nome = 'Elétrica'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ('7891234500180', 'Sensor de Rotação', 'Gol G5 / Fox', 68.00, 139.90, 3, 3, (SELECT id FROM categoria WHERE nome = 'Elétrica'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ('7891234500073', 'Lâmpada H4 (par)', 'Universal', 12.00, 29.90, 28, 10, (SELECT id FROM categoria WHERE nome = 'Iluminação'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ('7891234500197', 'Lâmpada Pingo T10 LED (par)', 'Universal', 6.50, 19.90, 58, 10, (SELECT id FROM categoria WHERE nome = 'Iluminação'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ('7891234500203', 'Palheta Limpador (par)', 'Universal 18/20"', 19.00, 44.90, 2, 6, NULL, NULL);


INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, fornecedor_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500104'), 'ENTRADA', 10, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'ENTRADA', 24, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'ENTRADA', 60, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'ENTRADA', 25, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'ENTRADA', 20, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500142'), 'ENTRADA', 12, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'ENTRADA', 8, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'ENTRADA', 14, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500166'), 'ENTRADA', 10, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '33.221.554/0001-02')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'ENTRADA', 12, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500042'), 'ENTRADA', 16, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500173'), 'ENTRADA', 6, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'ENTRADA', 8, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500180'), 'ENTRADA', 5, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'ENTRADA', 40, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'ENTRADA', 50, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'ENTRADA', 18, DATE_SUB(CURDATE(), INTERVAL 30 DAY) + INTERVAL '09:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), NULL),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'ENTRADA', 30, DATE_SUB(CURDATE(), INTERVAL 15 DAY) + INTERVAL '10:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '98.765.432/0001-10')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'ENTRADA', 12, DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '10:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'ENTRADA', 20, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '10:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '45.678.123/0001-55')),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'ENTRADA', 12, DATE_SUB(CURDATE(), INTERVAL 8 DAY) + INTERVAL '10:00' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'joao'), (SELECT id FROM fornecedor WHERE cnpj = '12.345.678/0001-90'));

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:08' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 45.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'Filtro de Ar do Motor', 1, 45.90, 22.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:08' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:45' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 186.80, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 1, 38.90, 18.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 1, 19.90, 6.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'Filtro de Óleo', 4, 32.00, 14.90);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:45' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:45' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '11:45' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '10:44' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 45.90, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'Filtro de Ar do Motor', 1, 45.90, 22.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 29 DAY) + INTERVAL '10:44' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 28 DAY) + INTERVAL '09:24' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 29.90, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 1, 29.90, 14.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 28 DAY) + INTERVAL '09:24' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 27 DAY) + INTERVAL '13:36' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 282.50, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'Filtro de Óleo', 2, 32.00, 14.90),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 1, 38.90, 18.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 4, 44.90, 19.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 27 DAY) + INTERVAL '13:36' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 27 DAY) + INTERVAL '13:36' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 27 DAY) + INTERVAL '13:36' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 119.90, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '09:38' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 69.70, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 1, 29.90, 14.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 2, 19.90, 6.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '09:38' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '09:38' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 269.30, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 2, 29.90, 12.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 4, 44.90, 19.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 1, 29.90, 14.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 25 DAY) + INTERVAL '11:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '11:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 653.50, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'Correia Dentada', 1, 104.50, 55.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'Bateria 60Ah', 1, 549.00, 320.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '11:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '11:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '17:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 179.70, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 1, 29.90, 12.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 2, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '17:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 24 DAY) + INTERVAL '17:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 23 DAY) + INTERVAL '16:31' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 329.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'Amortecedor Traseiro', 1, 329.90, 189.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 23 DAY) + INTERVAL '16:31' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 21 DAY) + INTERVAL '09:24' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 155.60, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 4, 38.90, 18.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 21 DAY) + INTERVAL '09:24' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '16:48' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 199.80, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500180'), 'Sensor de Rotação', 1, 139.90, 68.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500142'), 'Filtro de Cabine (ar-condicionado)', 1, 59.90, 27.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500180'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '16:48' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500142'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '16:48' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '08:46' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 59.80, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 2, 29.90, 12.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '08:46' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 19 DAY) + INTERVAL '12:53' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 119.90, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 19 DAY) + INTERVAL '12:53' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 19 DAY) + INTERVAL '16:58' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 119.60, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 4, 29.90, 12.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 19 DAY) + INTERVAL '16:58' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 18 DAY) + INTERVAL '11:03' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 59.80, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 2, 29.90, 14.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 18 DAY) + INTERVAL '11:03' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 18 DAY) + INTERVAL '16:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 119.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 18 DAY) + INTERVAL '16:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '14:13' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 434.40, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'Amortecedor Traseiro', 1, 329.90, 189.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'Correia Dentada', 1, 104.50, 55.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '14:13' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '14:13' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '09:15' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 239.60, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 2, 44.90, 19.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 2, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '09:15' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '09:15' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '08:14' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 106.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 1, 74.90, 38.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'Filtro de Óleo', 1, 32.00, 14.90);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '08:14' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 17 DAY) + INTERVAL '08:14' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 16 DAY) + INTERVAL '11:34' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 91.80, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'Filtro de Ar do Motor', 2, 45.90, 22.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 16 DAY) + INTERVAL '11:34' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 15 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 419.70, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'Amortecedor Traseiro', 1, 329.90, 189.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 2, 44.90, 19.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 15 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 15 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '08:25' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 44.90, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 1, 44.90, 19.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '08:25' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '15:08' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 79.60, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 4, 19.90, 6.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '15:08' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '08:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 194.80, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 1, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '08:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY) + INTERVAL '08:41' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 12 DAY) + INTERVAL '14:57' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 89.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500042'), 'Vela de Ignição (jogo com 4)', 1, 89.90, 48.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500042'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 12 DAY) + INTERVAL '14:57' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 11 DAY) + INTERVAL '15:18' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 549.00, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'Bateria 60Ah', 1, 549.00, 320.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 11 DAY) + INTERVAL '15:18' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 11 DAY) + INTERVAL '17:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 19.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 1, 19.90, 6.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 11 DAY) + INTERVAL '17:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '08:32' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 179.40, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'Correia Dentada', 1, 104.50, 55.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 1, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '08:32' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '08:32' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '14:07' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 44.90, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 1, 44.90, 19.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '14:07' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '11:42' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 164.30, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'Correia Dentada', 1, 104.50, 55.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 2, 29.90, 14.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500059'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '11:42' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 10 DAY) + INTERVAL '11:42' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 9 DAY) + INTERVAL '08:29' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 209.70, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'Palheta Limpador (par)', 2, 44.90, 19.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500203'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 9 DAY) + INTERVAL '08:29' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 9 DAY) + INTERVAL '08:29' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL '09:56' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 59.80, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 2, 29.90, 12.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL '09:56' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL '08:42' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 149.80, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 2, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL '08:42' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '12:07' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 329.90, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'Amortecedor Traseiro', 1, 329.90, 189.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500035'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '12:07' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '16:31' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 39.80, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 2, 19.90, 6.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '16:31' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '08:21' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 45.90, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'Filtro de Ar do Motor', 1, 45.90, 22.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500128'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL '08:21' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:34' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 275.50, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 4, 38.90, 18.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 4, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:34' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:34' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '13:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 113.80, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'Lâmpada H4 (par)', 1, 29.90, 12.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'Filtro de Óleo', 2, 32.00, 14.90),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 1, 19.90, 6.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500073'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '13:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500028'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '13:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '13:49' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '10:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 198.70, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'Lâmpada Pingo T10 LED (par)', 1, 19.90, 6.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500180'), 'Sensor de Rotação', 1, 139.90, 68.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 1, 38.90, 18.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500197'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '10:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500180'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '10:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL '10:47' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '14:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 119.90, 'CREDITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'Pastilha de Freio Dianteira', 1, 119.90, 62.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500011'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '14:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '12:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 179.80, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500042'), 'Vela de Ignição (jogo com 4)', 2, 89.90, 48.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500042'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '12:52' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '12:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 77.80, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 2, 38.90, 18.50);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL '12:55' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL '10:37' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 958.70, 'PIX');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 2, 74.90, 38.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500104'), 'Disco de Freio Ventilado (par)', 1, 259.90, 145.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'Bateria 60Ah', 1, 549.00, 320.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL '10:37' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500104'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL '10:37' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500066'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL '10:37' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '14:57' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), 134.70, 'DINHEIRO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'Fluido de Freio DOT 4 500ml', 2, 29.90, 14.00),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 1, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500111'), 'SAIDA', 2, DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '14:57' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '14:57' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'mariana'), @v);

INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) VALUES (DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), 113.80, 'DEBITO');
SET @v = LAST_INSERT_ID();
INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) VALUES
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'Filtro de Combustível', 1, 38.90, 18.50),
    (@v, (SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'Pivô de Suspensão', 1, 74.90, 38.00);
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, data_hora, funcionario_id, venda_id) VALUES
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500135'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v),
    ((SELECT id FROM peca WHERE codigo_barras = '7891234500159'), 'SAIDA', 1, DATE_SUB(CURDATE(), INTERVAL 0 DAY) + INTERVAL '13:27' HOUR_MINUTE, (SELECT id FROM funcionario WHERE usuario = 'carla'), @v);

COMMIT;
