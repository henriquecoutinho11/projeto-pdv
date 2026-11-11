# Projeto PDV Web Core - Plataforma Genérica para Restaurantes

Sistema de Ponto de Venda (PDV) Web Responsivo e Modular para estabelecimentos alimentícios (restaurantes, lanchonetes, cafeterias, etc.), desenvolvido em **Java com Spring Boot** sob a **Arquitetura MVC** e persistência via **JPA / Hibernate**.

Como as definições de tipo de restaurante e cardápio específico ainda estão em fase de alinhamento, este repositório traz a **especificação geral de arquitetura** e o núcleo (Core Domain) configurável e extensível do sistema.

## 📄 Especificação do Projeto
A especificação geral de engenharia do projeto está disponível no arquivo:
👉 **[ESPECIFICACAO_GERAL_PROJETO.md](file:///c:/Users/isabelly.pardim/OneDrive/Desktop/projeto-pdv/ESPECIFICACAO_GERAL_PROJETO.md)**

## 🛠️ Tecnologias Utilizadas
- **Linguagem:** Java 17+
- **Framework Back-End:** Spring Boot 3.x (Spring MVC, Spring Data JPA)
- **Persistência de Dados:** Jakarta Persistence (JPA) + Hibernate ORM
- **Banco de Dados:** H2 Database (Desenvolvimento) / PostgreSQL (Produção)
- **Front-End / View:** HTML5, CSS3 Responsivo (Grid/Flexbox), JavaScript + Thymeleaf
- **Gerenciador de Dependências:** Maven

## 📐 Padrões Arquiteturais e Conceitos (Aula 08)
- **Arquitetura MVC (Model-View-Controller)**
- **Decomposição do Model:** Entity, Service e Repository
- **Padrão Comportamental Observer:** Processamento reativo via `ApplicationEventPublisher`
- **Ciclo de Vida de Entidades JPA & Controle Transacional ACID**
- **Matriz de Responsabilidades:** Separação estrita de camadas e prevenção de anti-patterns (*God Controller*, *Fat View*, *Anemic Model*).