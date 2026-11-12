-- Carga inicial de dados para o Cardápio Antunes Lanches (CARD-02 e CARD-03)
-- Utiliza MERGE INTO para ser idempotente e não falhar se data.sql for executado múltiplas vezes nos testes

-- Categorias
MERGE INTO categorias (id, nome, descricao, ativo) KEY(id) VALUES 
(1, 'Hambúrgueres', 'Lanches artesanais preparados na hora com carnes selecionadas', true),
(2, 'Acompanhamentos', 'Porções crocantes de batatas, anéis de cebola e nuggets', true),
(3, 'Bebidas', 'Refrigerantes, sucos naturais e águas', true),
(4, 'Combos', 'Combinações completas com lanche, acompanhamento e bebida', true),
(5, 'Sobremesas', 'Milkshakes, sorvetes e sobremesas artesanais', true);

-- Produtos
MERGE INTO produtos (id, nome, descricao, preco, quantidade_estoque, imagem_url, ativo, categoria_id) KEY(id) VALUES
(1, 'X-Burguer Tradicional', 'Pão brioche, hambúrguer 160g, queijo prato derretido e maionese da casa', 22.90, 50, '/images/x-burguer.jpg', true, 1),
(2, 'X-Salada Antunes', 'Pão brioche, hambúrguer 160g, queijo, alface, tomate e maionese especial', 25.90, 45, '/images/x-salada.jpg', true, 1),
(3, 'Antunes Smash Duplo', 'Dois smashes de 90g, queijo cheddar duplo, bacon crocante e molho especial', 29.90, 30, '/images/smash-duplo.jpg', true, 1),
(4, 'Batata Frita Crocante', 'Porção de 200g de batata palito bem crocante e sequinha', 14.00, 100, '/images/batata-frita.jpg', true, 2),
(5, 'Anéis de Cebola (Onion Rings)', 'Porção com 10 anéis de cebola empanados e fritos', 16.00, 40, '/images/onion-rings.jpg', true, 2),
(6, 'Coca-Cola 350ml', 'Lata de refrigerante trincando de gelada', 6.50, 120, '/images/coca-cola.jpg', true, 3),
(7, 'Suco Natural de Laranja 500ml', 'Suco 100% natural de laranja feito na hora sem adição de açúcar', 9.00, 60, '/images/suco-laranja.jpg', true, 3),
(8, 'Combo Antunes Especial', 'X-Salada Antunes + Batata Frita Crocante + Coca-Cola 350ml', 42.00, 25, '/images/combo-especial.jpg', true, 4),
(9, 'Milkshake de Chocolate 400ml', 'Milkshake cremoso feito com sorvete de baunilha e calda de chocolate', 18.00, 35, '/images/milkshake.jpg', true, 5);

-- Opções Adicionais, Insumos e Opções de Combos (CARD-03)
MERGE INTO opcoes_adicionais (id, nome, descricao, preco_extra, tipo, permite_remocao, ativo) KEY(id) VALUES
-- Adicionais pagos
(1, 'Bacon Crocante', 'Tiras de bacon fatiadas e crocantes', 3.50, 'ADICIONAL', false, true),
(2, 'Queijo Cheddar Extra', 'Fatia generosa de cheddar cremoso derretido', 3.00, 'ADICIONAL', false, true),
(3, 'Hambúrguer 160g Adicional', 'Hambúrguer artesanal extra grelhado no fogo', 7.00, 'ADICIONAL', false, true),
(4, 'Molho Especial da Casa', 'Porção extra do molho secreto Antunes', 2.00, 'ADICIONAL', false, true),
(5, 'Cebola Caramelizada', 'Cebolas lentamente douradas e caramelizadas', 3.00, 'ADICIONAL', false, true),
-- Ingredientes padrão para remoção
(6, 'Cebola', 'Cebola fresca em rodelas', 0.00, 'REMOCAO', true, true),
(7, 'Picles', 'Picles artesanal crocante agridoce', 0.00, 'REMOCAO', true, true),
(8, 'Alface', 'Alface americana fresca', 0.00, 'REMOCAO', true, true),
(9, 'Tomate', 'Rodelas de tomate fresco', 0.00, 'REMOCAO', true, true),
(10, 'Maionese da Casa', 'Maionese temperada artesanal', 0.00, 'REMOCAO', true, true),
-- Tamanho de Batata / Acompanhamento para Combos
(11, 'Batata Média', 'Acompanhamento de batata palito tamanho médio padrão', 0.00, 'TAMANHO_BATATA', false, true),
(12, 'Batata Grande', 'Upgrade para tamanho grande crocante', 4.00, 'TAMANHO_BATATA', false, true),
-- Bebidas elegíveis para Combos
(13, 'Coca-Cola 350ml', 'Lata gelada de Coca-Cola', 0.00, 'BEBIDA', false, true),
(14, 'Guaraná Antarctica 350ml', 'Lata gelada de Guaraná Antarctica', 0.00, 'BEBIDA', false, true),
(15, 'Suco Natural de Laranja 500ml', 'Suco 100% natural com upgrade', 2.50, 'BEBIDA', false, true),
(16, 'Água Mineral sem Gás 500ml', 'Garrafa de água mineral', 0.00, 'BEBIDA', false, true);

-- Vínculo de Produtos com Opções Disponíveis (produtos_opcoes)
-- Lanches (IDs 1, 2, 3): Adicionais (1..5) e Ingredientes Removíveis (6..10)
MERGE INTO produtos_opcoes (produto_id, opcao_id) KEY(produto_id, opcao_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10),
-- Combo Antunes Especial (ID 8): Adicionais (1..5), Remoções (6..10), Tamanhos (11, 12), Bebidas (13, 14, 15, 16)
(8, 1), (8, 2), (8, 3), (8, 4), (8, 5), (8, 6), (8, 7), (8, 8), (8, 9), (8, 10),
(8, 11), (8, 12), (8, 13), (8, 14), (8, 15), (8, 16);

-- Ajustar os geradores de sequências auto-incremento para evitar colisões com IDs criados dinamicamente
ALTER TABLE categorias ALTER COLUMN id RESTART WITH 100;
ALTER TABLE produtos ALTER COLUMN id RESTART WITH 100;
ALTER TABLE opcoes_adicionais ALTER COLUMN id RESTART WITH 100;
