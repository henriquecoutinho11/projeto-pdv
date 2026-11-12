# Script para criar os cards do Projeto PDV (Antunes Lanches) no GitHub Issues e GitHub Projects V2

$token = ((Get-Content .env) -replace 'GITHUB_TOKEN\s*=\s*', '').Trim()
$repoOwner = "henriquecoutinho11"
$repoName = "projeto-pdv"
$projectId = "PVT_kwHOBN3OSM4BrMJX" # Project 4 (Projeto PDV)

$restHeaders = @{
    Authorization = "Bearer $token"
    "Accept" = "application/vnd.github+json"
    "User-Agent" = "PowerShell-Script"
}

$gqlHeaders = @{
    Authorization = "Bearer $token"
    "User-Agent" = "PowerShell-Script"
    "Content-Type" = "application/json; charset=utf-8"
}

# IDs dos Campos do Project V2
$statusFieldId = "PVTSSF_lAHOBN3OSM4BrMJXzhpf8-0"
$statusOptions = @{
    "Backlog" = "f75ad846"
    "Ready" = "61e4505c"
    "InProgress" = "47fc9ee4"
    "InReview" = "df73e18b"
    "Done" = "98236657"
}

$priorityFieldId = "PVTSSF_lAHOBN3OSM4BrMJXzhpf9XY"
$priorityOptions = @{
    "P0" = "79628723"
    "P1" = "0a877460"
    "P2" = "da944a9c"
}

$sizeFieldId = "PVTSSF_lAHOBN3OSM4BrMJXzhpf9Xg"
$sizeOptions = @{
    "XS" = "6c6483d2"
    "S" = "f784b110"
    "M" = "7515a9f1"
    "L" = "817d0097"
    "XL" = "db339eb2"
}

$assigneesPool = @("henriquecoutinho11", "Isabelly-Caroline")

$cards = @(
    # ==================== BACK-END ====================
    @{
        Title = "[BACKEND] Configuração da Arquitetura MVC, Persistência JPA e Banco de Dados"
        Labels = @("backend", "arquitetura-core")
        Priority = "P0"
        Size = "M"
        Status = "Ready"
        Body = @"
## 🎯 Contexto e Propósito
Configuração da espinha dorsal da aplicação **Antunes Lanches** utilizando **Spring Boot 3.x**, **Java 17+** e persistência **JPA / Hibernate**, em estrita conformidade com a arquitetura MVC e separação de camadas definida na especificação técnica (Aula 08).

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Inicialização do projeto Spring Boot com dependências Maven (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `h2database`).
- [ ] Estruturação rígida de pacotes:
  - `com.restaurante.pdv.config`
  - `com.restaurante.pdv.controller`
  - `com.restaurante.pdv.dto`
  - `com.restaurante.pdv.event`
  - `com.restaurante.pdv.exception`
  - `com.restaurante.pdv.model.entity`
  - `com.restaurante.pdv.model.repository`
  - `com.restaurante.pdv.model.service`
- [ ] Configuração do `application.yml` para banco de dados H2 (console habilitado para desenvolvimento) e profile preparado para PostgreSQL (produção).
- [ ] Criação de tratador global de exceções com `@ControllerAdvice` (`GlobalExceptionHandler`) para respostas padronizadas com RFC 7807 (`ProblemDetail`).
- [ ] Habilitação de controle transacional `@EnableTransactionManagement`.

---

## ✅ Critérios de Aceite
- [ ] A aplicação compila e inicializa sem erros na porta padrão 8080.
- [ ] O console do H2 Database está acessível em `/h2-console`.
- [ ] Pacotes e diretórios seguem rigorosamente a matriz de responsabilidades.
"@
    },
    @{
        Title = "[BACKEND] Modelagem e CRUD de Produtos, Categorias e Insumos"
        Labels = @("backend", "admin-gestao")
        Priority = "P0"
        Size = "M"
        Status = "Ready"
        Body = @"
## 🎯 Contexto e Propósito
Modelagem relacional e implementação do domínio de produtos e categorias para o cardápio da lanchonete **Antunes Lanches**, provendo a base para exibição e venda de combos, hambúrgueres, acompanhamentos e bebidas.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Implementar a entidade `@Entity Categoria` (`id`, `nome`, `descricao`, `ativo`).
- [ ] Implementar a entidade `@Entity Produto` (`id`, `nome`, `descricao`, `preco`, `quantidadeEstoque`, `imagemUrl`, `ativo`, `categoria`).
- [ ] Mapeamento `@ManyToOne` entre `Produto` e `Categoria` com chave estrangeira indexada.
- [ ] Anotações de validação de estado puro (`@NotBlank`, `@DecimalMin`, `@Min`, `@NotNull`).
- [ ] Criação das interfaces de persistência `CategoriaRepository` e `ProdutoRepository` estendendo `JpaRepository`.
- [ ] Desenvolvimento da camada `ProdutoService` com métodos transacionais (`buscarAtivos()`, `salvar()`, `atualizarEstoque()`, `desativar()`).
- [ ] Criação do `AdminProdutoController` com endpoints REST para CRUD completo.

---

## ✅ Critérios de Aceite
- [ ] Categorias e produtos podem ser cadastrados, listados e desativados via API.
- [ ] Não é permitido cadastrar produtos com preço negativo ou sem categoria vinculada.
- [ ] Camada de Service isolada de dependências de visualização web.
"@
    },
    @{
        Title = "[BACKEND] Motor de Customização de Lanches e Combos (McDonald's Style)"
        Labels = @("backend", "fast-food", "totem-cliente")
        Priority = "P1"
        Size = "L"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Implementar a regra de negócio central de customização de pedidos fast-food inspirada no modelo do McDonald's (hambúrgueres com adicionais pagos, remoções de itens e escolha de itens de combo).

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Modelagem de adicionais/ingredientes (`ItemOpcao` ou `Adicional` com nome e valor adicional, ex: +Bacon R$ 3,50, +Cheddar R$ 3,00).
- [ ] Suporte a remoção de ingredientes (ex: ""Sem cebola"", ""Sem picles"", ""Sem molho"") gravados na observação do `ItemPedido`.
- [ ] Modelagem de Combos com opções de tamanho de batata (Média / Grande) e bebida (Refrigerante, Suco, Água).
- [ ] Regra na camada `PedidoService` para validação e cálculo do subtotal de cada `ItemPedido`:
  `subtotal = (precoBaseProduto + somaAdicionais + diferencaTamanho) * quantidade`.
- [ ] DTO de entrada `CustomizacaoItemDTO` estruturado para receber adicionais, remoções e observações.

---

## ✅ Critérios de Aceite
- [ ] Adicionais somam corretamente ao valor unitário do produto.
- [ ] Remoções de ingredientes não afetam o valor e ficam explicitamente registradas no `ItemPedido.observacoes`.
- [ ] Preços de adicionais e opções são validados contra o banco de dados (não aceitar valores vindos livremente do cliente).
"@
    },
    @{
        Title = "[BACKEND] Processamento Transacional de Pedidos e Carrinho de Compras"
        Labels = @("backend", "totem-cliente")
        Priority = "P0"
        Size = "L"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Implementar o motor de checkout e fechamento de pedidos com integridade transacional ACID, garantindo que o pedido e seus múltiplos itens sejam persistidos atomicamente.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação da entidade `@Entity Pedido` com relacionamento `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` com `ItemPedido`.
- [ ] Implementação do método `PedidoService.processarNovoPedido(NovoPedidoDTO dto)` anotado com `@Transactional`.
- [ ] Verificação de integridade: checar se os produtos existem, se estão ativos e se há estoque disponível.
- [ ] Recálculo estrito dos valores no servidor (rejeitando cálculos manipulados enviados pelo front-end).
- [ ] Criação do `PedidoController` com endpoint `POST /api/pedidos` retornando código HTTP `201 Created` e DTO detalhado com status inicial `RECEBIDO`.

---

## ✅ Critérios de Aceite
- [ ] Caso qualquer item falhe na validação ou estoque, a transação sofre rollback total.
- [ ] O valor total do pedido bate com a soma exata dos subtotais dos itens.
- [ ] O pedido gerado recebe um ID único persistido no banco de dados.
"@
    },
    @{
        Title = "[BACKEND] Módulo de Pagamentos: Integração PIX e Cartão (Crédito/Débito)"
        Labels = @("backend", "totem-cliente")
        Priority = "P1"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Disponibilizar a camada de processamento de pagamentos para totens de autoatendimento do **Antunes Lanches**, suportando fluxo instantâneo via **PIX (QR Code)** e simulação de terminal de **Cartão de Crédito/Débito**.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação da enum `@Enumerated(EnumType.STRING) FormaPagamento` (`PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `DINHEIRO`).
- [ ] Implementação de `PagamentoService` com métodos:
  - `gerarCobrancaPix(Long pedidoId)`: gera payload EMV de PIX copia-e-cola e string base64 do QR Code para exibição no totem.
  - `confirmarPagamento(Long pedidoId, ConfirmacaoPagamentoDTO dto)`: valida o recebimento e atualiza o pedido.
- [ ] Mecanismo de expiração do QR Code PIX (5 minutos).
- [ ] Simulação de webhook/endpoint para notificação de pagamento aprovado (`POST /api/pagamentos/{pedidoId}/confirmar`).

---

## ✅ Critérios de Aceite
- [ ] Endpoint de geração de PIX retorna payload e imagem QR Code legível.
- [ ] Pedido só pode ter status alterado para preparo na cozinha após a confirmação de pagamento efetuada.
- [ ] Formas de pagamento inválidas são rejeitadas com erro `400 Bad Request`.
"@
    },
    @{
        Title = "[BACKEND] Emissão de Senha Sequencial Diária e Identificação do Cliente"
        Labels = @("backend", "totem-cliente", "fast-food")
        Priority = "P1"
        Size = "S"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Garantir o fluxo de chamada de pedidos estilo McDonald's, onde cada cliente recebe uma senha sequencial diária (ex: `#001`, `#042`) associada ao seu nome/CPF para chamada no painel do salão.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação do gerador de senhas diárias (`SenhaSequencialService`):
  - Sequência numérica reiniciada automaticamente a cada virada de dia (00:00h).
  - Controle de concorrência com `AtomicInteger` ou sequence no banco de dados para evitar senhas duplicadas.
- [ ] Inclusão dos campos `numeroSenha` (Integer), `nomeCliente` (String opcional) e `cpfCliente` (String formatada opcional) na entidade `Pedido`.
- [ ] Validador customizado de CPF para notas fiscais.
- [ ] Inclusão do número da senha formatada (ex: `#042`) no retorno do DTO do pedido.

---

## ✅ Critérios de Aceite
- [ ] Dois pedidos finalizados simultaneamente não recebem o mesmo número de senha.
- [ ] A contagem recomeça em `#001` no início de cada novo dia operacional.
- [ ] CPF inválido é rejeitado quando preenchido, mas o campo permanece opcional para o cliente.
"@
    },
    @{
        Title = "[BACKEND] Implementação do Padrão Observer via Spring ApplicationEventPublisher"
        Labels = @("backend", "arquitetura-core")
        Priority = "P0"
        Size = "M"
        Status = "Ready"
        Body = @"
## 🎯 Contexto e Propósito
Aplicar o padrão comportamental **Observer (GoF)** via `ApplicationEventPublisher` do Spring Framework para desacoplar a finalização da compra dos serviços de apoio (notificação da cozinha KDS e geração de comprovante digital), conforme especificação da Aula 08.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação da classe de evento imutável `PedidoCriadoEvent(Long pedidoId, Integer numeroSenha, LocalDateTime dataHora)`.
- [ ] Injeção de `ApplicationEventPublisher` no `PedidoService` e publicação do evento logo após o commit da transação.
- [ ] Criação do listener reativo `CozinhaObserverListener` com `@EventListener` (ou `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`) para enfileirar o pedido para a cozinha.
- [ ] Criação do listener `NotificacaoComprovanteListener` para gerar a ficha de impressão / recibo digital.
- [ ] Testes unitários comprovando que a falha em um observer secundário não corrompe a persistência do pedido.

---

## ✅ Critérios de Aceite
- [ ] `PedidoService` não possui referências diretas às classes visuais ou da cozinha.
- [ ] A publicação do evento ocorre com sucesso e dispara todos os listeners registrados.
- [ ] Arquitetura desacoplada e aderente à especificação do projeto.
"@
    },
    @{
        Title = "[BACKEND] API do KDS (Kitchen Display System) e Workflow de Status"
        Labels = @("backend", "cozinha-kds")
        Priority = "P1"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Desenvolver os endpoints de suporte ao painel operacional da cozinha (KDS), gerenciando o ciclo de vida e a máquina de estados dos pedidos em preparo.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Definição da enum `@Enumerated(EnumType.STRING) StatusPedido` com os estados:
  `RECEBIDO` ➔ `EM_PREPARO` ➔ `PRONTO` ➔ `ENTREGUE` (e `CANCELADO`).
- [ ] Validação da máquina de estados no `PedidoService`: transições ilegais (ex: de `RECEBIDO` direto para `ENTREGUE`) disparam exceção de negócio `TransalcaoStatusInvalidaException`.
- [ ] Endpoints REST no `KdsController`:
  - `GET /api/kds/pedidos-ativos`: lista pedidos nos estados `RECEBIDO` e `EM_PREPARO` ordenados por tempo de espera.
  - `PATCH /api/kds/pedidos/{id}/status`: avança o status do pedido para a próxima etapa.
- [ ] Cálculo do tempo decorrido desde a abertura do pedido em minutos para controle de SLA.

---

## ✅ Critérios de Aceite
- [ ] Endpoints retornam a lista completa dos itens e personalizações de cada pedido (ex: tags ""SEM CEBOLA"").
- [ ] Tentativa de pular etapas de status inválidas retorna `422 Unprocessable Entity`.
- [ ] Mudança de status para `PRONTO` disponibiliza a senha para o painel de retirada do salão.
"@
    },
    @{
        Title = "[BACKEND] Controle de Estoque com Concorrência Otimista (@Version)"
        Labels = @("backend", "admin-gestao")
        Priority = "P1"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Garantir o controle de baixa automática do estoque de produtos e insumos com suporte a concorrência otimista (optimistic locking), prevenindo vendas duplicadas de lanches esgotados quando múltiplos totens estiverem operando ao mesmo tempo.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Adição da anotação `@Version` na entidade `Produto` (`private Long version`).
- [ ] Implementação de lógica de decremento atômico de estoque no momento da confirmação do pedido.
- [ ] Tratamento específico para captura de `OptimisticLockException` no `GlobalExceptionHandler`.
- [ ] Notificação e retorno amigável para o cliente do totem caso o produto acabe no instante exato da compra.
- [ ] Alerta de estoque baixo quando o produto atingir menos de 5 unidades.

---

## ✅ Critérios de Aceite
- [ ] Teste de concorrência com duas requisições simultâneas para o último item do estoque resulta em 1 sucesso e 1 erro controlado.
- [ ] Estoque não aceita valores negativos (`@Min(0)`).
- [ ] Baixa de estoque ocorre atomicamente vinculada ao pedido.
"@
    },
    @{
        Title = "[BACKEND] API de Relatórios Gerenciais e Fechamento de Vendas"
        Labels = @("backend", "admin-gestao")
        Priority = "P2"
        Size = "S"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Fornecer dados consolidados e métricas financeiras/operacionais para a área administrativa do **Antunes Lanches**.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação de queries JPQL customizadas no `PedidoRepository` para agregações:
  - Total faturado por dia/período.
  - Quantidade de pedidos por status e por forma de pagamento.
  - Ranking dos produtos mais vendidos (curva ABC).
  - Tempo médio de preparo na cozinha (diferença entre `dataHora` e status `PRONTO`).
- [ ] Desenvolvimento de DTOs analíticos (`RelatorioFaturamentoDTO`, `ItemMaisVendidoDTO`).
- [ ] Criação do controller `AdminRelatorioController` com endpoints protegidos para consulta dos dados consolidados.

---

## ✅ Critérios de Aceite
- [ ] Queries executam com bom desempenho utilizando agregações no banco (`SUM`, `COUNT`, `AVG`).
- [ ] Endpoint de fechamento de caixa diário bate exatamente com a soma dos pedidos pagos.
"@
    },

    # ==================== FRONT-END ====================
    @{
        Title = "[FRONTEND] Design System & Layout Responsivo Base (Antunes Lanches)"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P0"
        Size = "M"
        Status = "Ready"
        Body = @"
## 🎯 Contexto e Propósito
Desenvolver a identidade visual e o Design System responsivo da lanchonete **Antunes Lanches**, com foco na experiência de autoatendimento touch screen (totem vertical 1080x1920) e compatibilidade total com desktop e dispositivos móveis.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Criação da folha de estilos CSS modular (`base.css`, `components.css`, `totem.css`):
  - Paleta de cores temática de fast-food (vermelho vivo, amarelo ouro, preto fosco e branco).
  - Tipografia de alta legibilidade para leitura rápida à distância.
  - Espaçamentos e botões grandes (alvo mínimo de toque de 48x48px para ergonomia touch).
- [ ] Layout fluido utilizando CSS Grid e Flexbox com media queries para:
  - Totem Vertical Kiosk (1080x1920)
  - Desktop / Terminal de Caixa (1920x1080)
  - Tablets e Smartphones (responsividade móvel).
- [ ] Componentização de cabeçalho com logo do Antunes Lanches, rodapé com botão de ajuda/cancelar e área central dinâmica via Thymeleaf.

---

## ✅ Critérios de Aceite
- [ ] O layout adapta-se perfeitamente aos diferentes formatos de tela sem quebra de elementos.
- [ ] Todos os elementos interativos possuem feedback visual de toque/clique (`:active`, `:focus`).
"@
    },
    @{
        Title = "[FRONTEND] Tela de Boas-Vindas e Escolha de Consumo (Totem McDonald's)"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P1"
        Size = "S"
        Status = "Ready"
        Body = @"
## 🎯 Contexto e Propósito
Criar a tela inicial de entrada do Totem do Antunes Lanches no padrão consagrado do McDonald's, capturando a atenção do cliente e iniciando a jornada de compra com a escolha do tipo de consumo.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Tela de proteção/atração com banner promocional rotativo com fotos apetitosas de hambúrgueres e combos.
- [ ] Botão de toque central ""Toque aqui para começar"".
- [ ] Tela de modalidade de consumo com dois cards grandes e ilustrados:
  - 🍽️ **""Para Comer Aqui""** (Consumo no salão da lanchonete).
  - 🛍️ **""Para Viagem""** (Embalagem reforçada para viagem).
- [ ] Implementação de script JS de inatividade: se o totem ficar parado por mais de 60 segundos sem interação, exibe aviso e retorna automaticamente à tela de início limpa.

---

## ✅ Critérios de Aceite
- [ ] Transições suaves e sem atraso perceptível ao toque.
- [ ] A escolha do tipo de consumo é salva na sessão do cliente para o pedido final.
- [ ] O temporizador de reset restaura o totem de forma limpa.
"@
    },
    @{
        Title = "[FRONTEND] Vitrine de Produtos & Navegação por Categorias do Cardápio"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P0"
        Size = "L"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Construir o catálogo interativo do cardápio do Antunes Lanches, permitindo que o cliente navegue facilmente por categorias e localize produtos de forma visual e intuitiva.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Barra lateral de categorias com navegação rápida:
  - 🍔 **Hambúrgueres Artesanais**
  - 🍟 **Acompanhamentos & Batatas**
  - 🥤 **Bebidas & Sucos**
  - 🍦 **Sobremesas & Shakes**
  - ⭐ **Combos Especiais**
- [ ] Grid de exibição de produtos com fotos em alta resolução, nome, breve descrição dos ingredientes e preço formatado (`R$ 00,00`).
- [ ] Badges visuais destacados: ""Novo"", ""Mais Vendido"", ""Oferta do Dia"".
- [ ] Campo de busca rápida e filtros rápidos.
- [ ] Indicação visual clara de produto esgotado (desabilitando o botão de adicionar).

---

## ✅ Critérios de Aceite
- [ ] A troca de categorias é instantânea e rola suavemente até os produtos.
- [ ] A renderização dos produtos via Thymeleaf e JavaScript é veloz e fluida.
- [ ] Itens sem estoque não permitem abertura de personalização ou inclusão no carrinho.
"@
    },
    @{
        Title = "[FRONTEND] Modal Interativo de Personalização de Lanches (""Monte seu Lanche"")"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P0"
        Size = "L"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Desenvolver o modal interativo de customização estilo totem McDonald's, onde o cliente pode turbinar seu lanche, remover ingredientes indesejados e escolher opções de combo com atualização de preço em tempo real.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Modal em tela cheia (ou gaveta expansiva) exibindo a foto do lanche selecionado e descrição completa.
- [ ] Seção **""Adicionais do Chefe""**:
  - Checkboxes ou seletores de quantidade (+ Queijo Cheddar, + Bacon Crocante, + Hambúrguer Extra, + Molho Especial Antunes).
- [ ] Seção **""Personalize seus Ingredientes""**:
  - Botões de alternância rápidos para remoção de itens padrão (""Sem cebola"", ""Sem picles"", ""Sem salada"", ""Sem molho"").
- [ ] Seção **""Transforme em Combo""**:
  - Seleção do tamanho da batata (Média / Grande) e escolha da bebida.
- [ ] Barra de rodapé com cálculo dinâmico e animado do valor total e botão grande ""Adicionar ao Pedido"".

---

## ✅ Critérios de Aceite
- [ ] Qualquer alteração de adicional recalcula instantaneamente o valor final na tela.
- [ ] As opções marcadas são compiladas em um objeto JSON pronto para o carrinho.
- [ ] Interface simples de fechar ou cancelar sem alterar o carrinho existente.
"@
    },
    @{
        Title = "[FRONTEND] Carrinho de Compras Flutuante & Drawer de Resumo do Pedido"
        Labels = @("frontend", "totem-cliente")
        Priority = "P1"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Criar o componente de carrinho de compras acessível a qualquer momento pelo cliente, exibindo com transparência todos os itens selecionados e suas personalizações.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Barra inferior/lateral persistente exibindo: quantidade de itens e valor total acumulado.
- [ ] Drawer expansível exibindo a listagem detalhada:
  - Nome do produto e foto em miniatura.
  - Subitens e tags de customização (ex: ""+ Bacon"", ""SEM CEBOLA"").
  - Botões de controle de quantidade (+ / -) e botão de remoção (lixeira).
  - Subtotal calculado para cada item.
- [ ] Botão fixo com destaque visual: ""Finalizar Pedido"" (redirecionando para checkout).
- [ ] Estado vazio amigável caso não haja itens no carrinho.

---

## ✅ Critérios de Aceite
- [ ] O carrinho atualiza imediatamente ao adicionar ou editar um produto.
- [ ] Alterar quantidade atualiza o total imediatamente.
- [ ] Ao remover todos os itens, o carrinho retorna ao estado inicial.
"@
    },
    @{
        Title = "[FRONTEND] Tela de Identificação do Cliente com Teclado Virtual Touch"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P1"
        Size = "S"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Disponibilizar uma tela rápida de identificação no totem de autoatendimento, permitindo inclusão de CPF e nome com interface touch ergonômica sem dependência de teclado físico.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Campo para digitação de **Nome para Chamada** (com teclado QWERTY virtual touch na tela).
- [ ] Campo para digitação de **CPF na Nota** com teclado numérico grande (estilo discador telefônico/caixa eletrônico) e máscara automática (`000.000.000-00`).
- [ ] Botão de atalho destacado: **""Não desejo informar CPF""** (para acelerar o atendimento).
- [ ] Botão de avanço rápido para a etapa de pagamento.

---

## ✅ Critérios de Aceite
- [ ] O teclado virtual touch responde com agilidade e teclas grandes.
- [ ] Máscara de CPF funciona corretamente ao digitar e apagar.
- [ ] O cliente pode prosseguir sem preencher nenhum dos dois campos caso prefira não se identificar.
"@
    },
    @{
        Title = "[FRONTEND] Tela de Pagamento Interativo (QR Code PIX & Cartão)"
        Labels = @("frontend", "totem-cliente")
        Priority = "P0"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Prover a interface interativa de pagamento no totem do Antunes Lanches, orientando o cliente passo a passo conforme a modalidade escolhida.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Seleção de modalidade com botões visuais ilustrados:
  - 💠 **PIX** (Pagamento Instantâneo)
  - 💳 **Cartão de Crédito ou Débito**
- [ ] **Fluxo PIX:**
  - Renderização do QR Code em tamanho grande com alto contraste para leitura rápida por smartphone.
  - Temporizador regressivo de validade (5:00 min).
  - Feedback sonoro/visual assim que o pagamento for detectado.
- [ ] **Fluxo Cartão:**
  - Animação demonstrativa de aproximação (contactless) ou inserção do cartão na maquininha lateral do totem.
  - Mensagem de status (""Aguardando senha"", ""Processando..."").

---

## ✅ Critérios de Aceite
- [ ] O QR Code é exibido com clareza e permite leitura imediata pela câmera do celular.
- [ ] A tela exibe indicador de carregamento durante a confirmação da transação.
- [ ] Ao confirmar o pagamento, direciona automaticamente para a tela de confirmação e senha.
"@
    },
    @{
        Title = "[FRONTEND] Tela de Confirmação do Pedido e Ficha de Senha Digital"
        Labels = @("frontend", "totem-cliente", "fast-food")
        Priority = "P1"
        Size = "S"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Criar a tela final de experiência do cliente no totem, fornecendo com destaque a senha de atendimento para retirada e as instruções do pedido.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Exibição em caixa alta e fonte gigante da **Senha de Atendimento** (ex: `#042`).
- [ ] Ícone animado de confirmação (check verde com efeito de confete/sucesso).
- [ ] Resumo simplificado do pedido com número de itens e modalidade (""Para Comer no Local"" ou ""Para Viagem"").
- [ ] Mensagem orientativa: *""Acompanhe sua senha no painel da lanchonete e retire no balcão quando estiver pronta!""*.
- [ ] Botão ""Imprimir Comprovante"" e botão ""Concluir"".
- [ ] Contagem regressiva de 15 segundos para redirecionamento automático à tela de início para o próximo cliente.

---

## ✅ Critérios de Aceite
- [ ] A senha gerada corresponde exatamente ao número retornado pelo backend.
- [ ] A tela limpa o carrinho e qualquer estado residual do pedido anterior.
"@
    },
    @{
        Title = "[FRONTEND] Painel KDS da Cozinha (Display Operacional em Tempo Real)"
        Labels = @("frontend", "cozinha-kds")
        Priority = "P0"
        Size = "L"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Desenvolver o painel de produção da cozinha (Kitchen Display System - KDS) em interface web responsiva para tablets/monitores dos chapeiros e operadores do **Antunes Lanches**.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Layout em colunas ou grid com cartões de pedidos:
  - Cabeçalho do card: Senha (`#042`), tipo (Local/Viagem) e temporizador de espera.
  - Código de cores por tempo de espera:
    - 🟢 Verde: < 5 minutos.
    - 🟡 Amarelo: entre 5 e 10 minutos.
    - 🔴 Vermelho: > 10 minutos (alerta de atraso).
  - Lista de itens e insumos:
    - Tags de personalização com contraste elevado (ex: etiqueta vermelha **""SEM CEBOLA""**, etiqueta verde **""+ BACON""**).
- [ ] Botão de 1 toque no card para avançar etapa:
  `Recebido` ➔ `Iniciar Preparo` ➔ `Marcar como Pronto`.
- [ ] Atualização automática da lista de pedidos (polling periódico ou WebSocket).

---

## ✅ Critérios de Aceite
- [ ] Permite que a equipe da cozinha avance o status do pedido com um único toque na tela.
- [ ] Destaque inequívoco para remoções de ingredientes para evitar erros operacionais.
- [ ] Pedidos concluídos saem da tela da cozinha automaticamente.
"@
    },
    @{
        Title = "[FRONTEND] Painel de Chamada de Senhas para TV do Salão"
        Labels = @("frontend", "cozinha-kds", "fast-food")
        Priority = "P2"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Desenvolver a interface pública estilo McDonald's para exibição em TVs do salão, comunicando aos clientes quais pedidos estão sendo preparados e quais estão prontos para retirada no balcão.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Layout de alto contraste com duas colunas visuais:
  - ⏳ **EM PREPARO** (Lista de senhas em produção na cozinha).
  - 🔔 **PRONTO PARA RETIRADA** (Lista de senhas prontas, com número e nome do cliente).
- [ ] Efeito visual quando um pedido mudar para `PRONTO`:
  - Animação pulsante no card da nova senha.
  - Reprodução de efeito sonoro suave de notificação (""Ding-dong"").
- [ ] Exibição no rodapé da data, hora atual e mensagens institucionais do Antunes Lanches.
- [ ] Modo tela cheia (Full Screen API) para uso em Smart TVs e navegadores dedicados.

---

## ✅ Critérios de Aceite
- [ ] A interface é legível a longa distância em monitores de 32"" a 65"".
- [ ] Atualização fluida sem necessidade de recarregar a página manualmente (F5).
"@
    },
    @{
        Title = "[FRONTEND] Painel Administrativo de Gestão do Cardápio e Estoque"
        Labels = @("frontend", "admin-gestao")
        Priority = "P1"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Criar a área de retaguarda administrativa para o gerente do Antunes Lanches gerenciar o cardápio, produtos, fotos, preços e controlar a disponibilidade de estoque.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Tabela responsiva com listagem de produtos com filtros por categoria e status (Ativo/Inativo).
- [ ] Modal de cadastro e edição de produtos:
  - Nome, descrição, categoria, preço de venda, estoque atual e URL da foto.
- [ ] Botão de ação rápida ""Pausar Venda"" (toggle instantâneo para desativar produto esgotado no totem).
- [ ] Validações de formulário no front-end com mensagens amigáveis de erro.

---

## ✅ Critérios de Aceite
- [ ] Gerente consegue cadastrar um novo lanche e ele passa a ser exibido no totem.
- [ ] Desativar um produto remove-o imediatamente da vitrine de vendas.
"@
    },
    @{
        Title = "[FRONTEND] Dashboard Administrativo de Métricas e Faturamento"
        Labels = @("frontend", "admin-gestao")
        Priority = "P2"
        Size = "M"
        Status = "Backlog"
        Body = @"
## 🎯 Contexto e Propósito
Criar o dashboard analítico com métricas de desempenho comercial e operacional para os donos e gerentes da lanchonete Antunes Lanches.

---

## 🛠️ Escopo Técnico e Implementação
- [ ] Cards de KPIs principais no topo:
  - 💰 Faturamento Total do Dia (R$).
  - 📦 Total de Pedidos Concluídos.
  - ⏱️ Tempo Médio de Espera na Cozinha.
  - 🍔 Ticket Médio por Pedido.
- [ ] Gráfico de pizza/rosca: Distribuição de pagamentos (PIX vs. Cartão de Crédito vs. Débito).
- [ ] Gráfico de barras: Top 5 hambúrgueres mais vendidos da casa.
- [ ] Seletor de período (Hoje, Últimos 7 dias, Este Mês).

---

## ✅ Critérios de Aceite
- [ ] Métricas batem com os dados retornados pela API analítica do backend.
- [ ] Gráficos renderizam corretamente em computadores e tablets de gestão.
"@
    }
)

Write-Host "Iniciando criacao de $($cards.Count) cards para o Projeto PDV (Antunes Lanches)..."

# Embaralhar distribuicao de responsaveis 50% Henrique, 50% Isabelly
$assigneesList = @()
for ($i = 0; $i -lt ($cards.Count / 2); $i++) {
    $assigneesList += "henriquecoutinho11"
    $assigneesList += "Isabelly-Caroline"
}
# Aleatorizar a ordem
$randomAssignees = $assigneesList | Sort-Object { Get-Random }

$cardIndex = 0
foreach ($card in $cards) {
    $assignedUser = $randomAssignees[$cardIndex]
    $cardIndex++
    
    Write-Host "`n------------------------------------------------------------"
    Write-Host "Criando Card [$cardIndex]: $($card.Title)"
    Write-Host "Responsavel: $assignedUser | Prioridade: $($card.Priority) | Tamanho: $($card.Size) | Status: $($card.Status)"
    Write-Host "Labels: $($card.Labels -join ', ')"

    # 1. Criar Issue no GitHub
    $issueObj = [ordered]@{
        title = $card.Title
        body = $card.Body
        assignees = @($assignedUser)
        labels = $card.Labels
    }
    $issueJson = $issueObj | ConvertTo-Json -Depth 10
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($issueJson)

    try {
        $issue = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/issues" -Method Post -Headers $restHeaders -Body $bodyBytes -ContentType "application/json; charset=utf-8"
        Write-Host "-> Issue criada com sucesso: #$($issue.number) (Node ID: $($issue.node_id))"
        
        # 2. Adicionar Issue ao GitHub Projects V2
        $addMutation = @"
mutation {
  addProjectV2ItemById(input: {projectId: "$projectId", contentId: "$($issue.node_id)"}) {
    item {
      id
    }
  }
}
"@
        $gqlBodyBytes = [System.Text.Encoding]::UTF8.GetBytes((@{ query = $addMutation } | ConvertTo-Json))
        $addRes = Invoke-RestMethod -Uri "https://api.github.com/graphql" -Method Post -Headers $gqlHeaders -Body $gqlBodyBytes
        $itemId = $addRes.data.addProjectV2ItemById.item.id
        Write-Host "-> Adicionado ao GitHub Project: Item ID $itemId"

        # 3. Atualizar Status no Project V2
        $statusOptionId = $statusOptions[$card.Status]
        if ($statusOptionId) {
            $statusMutation = @"
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "$projectId"
    itemId: "$itemId"
    fieldId: "$statusFieldId"
    value: {
      singleSelectOptionId: "$statusOptionId"
    }
  }) {
    projectV2Item { id }
  }
}
"@
            $statusGqlBytes = [System.Text.Encoding]::UTF8.GetBytes((@{ query = $statusMutation } | ConvertTo-Json))
            Invoke-RestMethod -Uri "https://api.github.com/graphql" -Method Post -Headers $gqlHeaders -Body $statusGqlBytes | Out-Null
            Write-Host "-> Status definido como: $($card.Status)"
        }

        # 4. Atualizar Prioridade no Project V2
        $priorityOptionId = $priorityOptions[$card.Priority]
        if ($priorityOptionId) {
            $priorityMutation = @"
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "$projectId"
    itemId: "$itemId"
    fieldId: "$priorityFieldId"
    value: {
      singleSelectOptionId: "$priorityOptionId"
    }
  }) {
    projectV2Item { id }
  }
}
"@
            $priorityGqlBytes = [System.Text.Encoding]::UTF8.GetBytes((@{ query = $priorityMutation } | ConvertTo-Json))
            Invoke-RestMethod -Uri "https://api.github.com/graphql" -Method Post -Headers $gqlHeaders -Body $priorityGqlBytes | Out-Null
            Write-Host "-> Prioridade definida como: $($card.Priority)"
        }

        # 5. Atualizar Tamanho (Size) no Project V2
        $sizeOptionId = $sizeOptions[$card.Size]
        if ($sizeOptionId) {
            $sizeMutation = @"
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "$projectId"
    itemId: "$itemId"
    fieldId: "$sizeFieldId"
    value: {
      singleSelectOptionId: "$sizeOptionId"
    }
  }) {
    projectV2Item { id }
  }
}
"@
            $sizeGqlBytes = [System.Text.Encoding]::UTF8.GetBytes((@{ query = $sizeMutation } | ConvertTo-Json))
            Invoke-RestMethod -Uri "https://api.github.com/graphql" -Method Post -Headers $gqlHeaders -Body $sizeGqlBytes | Out-Null
            Write-Host "-> Estimativa (Size) definida como: $($card.Size)"
        }

        Start-Sleep -Milliseconds 600

    } catch {
        Write-Host "ERRO ao processar card: $($card.Title)" -ForegroundColor Red
        Write-Host $_.Exception.Message
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host "Response Body:" $reader.ReadToEnd()
        }
    }
}

Write-Host "`nProcesso concluido com sucesso!"
