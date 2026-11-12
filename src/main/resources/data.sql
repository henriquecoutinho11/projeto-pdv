-- Carga inicial de dados para o Cardápio Antunes Lanches (CARD-02)
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

-- Ajustar os geradores de sequências auto-incremento para evitar colisões com IDs criados dinamicamente
ALTER TABLE categorias ALTER COLUMN id RESTART WITH 100;
ALTER TABLE produtos ALTER COLUMN id RESTART WITH 100;
