-- =====================================================================
-- PROJETO: GigaAutoPeças - Controle de Estoque
-- Disciplina: Linguagem de Programação III (IC805) - Profª Gizelle[cite: 1]
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gigapeças_db;
USE gigapeças_db;

-- 1. Tabela de Funcionários (Suporta o hash da senha SHA-256)
CREATE TABLE IF NOT EXISTS funcionario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    senha VARCHAR(64) NOT NULL,
    cargo VARCHAR(50)
);

-- 2. Tabela de Categorias (Ex: Freios, Filtros, Suspensão)
CREATE TABLE IF NOT EXISTS categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT
);

-- 3. Tabela de Fornecedores
CREATE TABLE IF NOT EXISTS fornecedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia VARCHAR(150) NOT NULL,
    cnpj VARCHAR(20) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(100)
);

-- 4. Tabela Principal de Peças (Estoque)
CREATE TABLE IF NOT EXISTS peca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras VARCHAR(50) UNIQUE NOT NULL,
    nome VARCHAR(150) NOT NULL,
    aplicacao VARCHAR(255),
    preco_custo DECIMAL(10,2) NOT NULL,
    preco_venda DECIMAL(10,2) NOT NULL,
    quantidade_estoque INT NOT NULL,
    estoque_minimo INT NOT NULL DEFAULT 5,
    categoria_id INT,
    fornecedor_id INT,
    CONSTRAINT fk_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id)
);

-- 5. Tabela de Histórico de Movimentações (Entrada e Saída)
CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id INT AUTO_INCREMENT PRIMARY KEY,
    peca_id INT,
    tipo ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade INT NOT NULL,
    data_movimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_peca_movimento FOREIGN KEY (peca_id) REFERENCES peca(id)
);



-- Inserindo Categorias
INSERT INTO categoria (nome, descricao) VALUES 
('Freios', 'Componentes do sistema de frenagem'),
('Filtros', 'Filtros de óleo, ar e combustível'),
('Suspensão', 'Amortecedores, molas e bandejas');

-- Inserindo Fornecedores
INSERT INTO fornecedor (nome_fantasia, cnpj, telefone, email) VALUES 
('Distribuidora FreioMaster Ltda', '12.345.678/0001-90', '(21) 98888-1111', 'contato@freiomaster.com'),
('AutoFiltros Brasil', '98.765.432/0001-12', '(21) 97777-2222', 'vendas@autofiltros.com');

-- Inserindo Peças no Estoque
INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, quantidade_estoque, estoque_minimo, categoria_id, fornecedor_id) 
VALUES ('7891001', 'Pastilha de Freio Dianteira', 'Fiat Palio / Siena', 45.00, 89.90, 12, 5, 1, 1);

INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, quantidade_estoque, estoque_minimo, categoria_id, fornecedor_id) 
VALUES ('7891002', 'Filtro de Óleo', 'Chevrolet Onix / Prisma', 18.50, 35.00, 8, 4, 2, 2);

INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, quantidade_estoque, estoque_minimo, categoria_id, fornecedor_id) 
VALUES ('7891003', 'Amortecedor Dianteiro', 'VW Gol G5 / Voyage', 120.00, 240.00, 6, 3, 3, 1);

-- Inserindo uma Movimentação Inicial de Exemplo
INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade) VALUES (1, 'ENTRADA', 12);