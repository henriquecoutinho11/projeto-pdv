/**
 * ==============================================================================
 * Antunes Lanches - Totem de Autoatendimento (Vanilla JS Puro)
 * Módulos:
 *  1. Relógio Digital & Carrossel Rotativo
 *  2. Máquina de Estados de Telas: Welcome -> Consumo -> Cardápio (CARD-12)
 *  3. Vitrine Interativa: Categorias, Rolagem Suave, Busca & Filtros Rápidos
 *  4. Modal de Customização McDonald's Style: Adicionais, Remoções, Combos & Cálculo Dinâmico
 *  5. Validação de Estoque & Compilação de Objeto JSON para Carrinho
 *  6. Temporizador de Inatividade (60s) & Modais de Apoio
 * ==============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // --------------------------------------------------------------------------
    // 1. ELEMENTOS DO DOM
    // --------------------------------------------------------------------------
    const clockElement = document.getElementById('totem-clock');
    const screenWelcome = document.getElementById('screen-welcome');
    const screenDining = document.getElementById('screen-dining');
    const screenMenu = document.getElementById('screen-menu');
    
    // Botões de Topo / Cabeçalho e Rodapé
    const btnStartOrder = document.getElementById('btn-start-order');
    const btnRestart = document.getElementById('btn-restart');
    const btnHelp = document.getElementById('btn-help');
    
    // Carrossel
    const promoSlides = document.querySelectorAll('.promo-slide');
    const promoDots = document.querySelectorAll('.carousel-dot');
    
    // Cards de Consumo
    const diningCards = document.querySelectorAll('.dining-card');
    const diningFeedback = document.getElementById('dining-feedback');
    const currentDiningLabel = document.getElementById('current-dining-label');
    const btnChangeDining = document.getElementById('btn-change-dining');

    // Vitrine de Produtos & Busca
    const categoryNavItems = document.querySelectorAll('.category-nav-item');
    const catalogContainer = document.getElementById('menu-catalog-container');
    const searchInput = document.getElementById('menu-search-input');
    const btnClearSearch = document.getElementById('btn-clear-search');
    const searchEmptyState = document.getElementById('search-empty-state');
    const btnResetSearchEmpty = document.getElementById('btn-reset-search-empty');
    const quickFilterButtons = document.querySelectorAll('.quick-filter-btn');
    const productCards = document.querySelectorAll('.product-card');

    // Modal de Customização ("Monte seu Lanche")
    const modalCustomizacao = document.getElementById('modal-customizacao');
    const btnCloseCustomModal = document.getElementById('btn-close-custom-modal');
    const btnCancelCustomModal = document.getElementById('btn-cancel-custom-modal');
    const btnConfirmAddCart = document.getElementById('btn-confirm-add-cart');

    const modalProductImg = document.getElementById('modal-product-img');
    const modalProductCategory = document.getElementById('modal-product-category');
    const modalProductTitle = document.getElementById('custom-modal-title');
    const modalProductDesc = document.getElementById('modal-product-desc');
    const modalProductBasePrice = document.getElementById('modal-product-base-price');
    const modalDynamicTotal = document.getElementById('modal-dynamic-total');
    const modalQtyValue = document.getElementById('modal-qty-value');
    const btnQtyMinus = document.getElementById('btn-qty-minus');
    const btnQtyPlus = document.getElementById('btn-qty-plus');
    const modalObservacoes = document.getElementById('modal-item-observacoes');

    // Seções de Opções do Modal
    const sectionComboUpgrade = document.getElementById('section-combo-upgrade');
    const containerAdicionais = document.getElementById('container-adicionais');
    const containerRemocoes = document.getElementById('container-remocoes');
    const radioTamanhosBatata = document.querySelectorAll('input[name="tamanhoBatata"]');
    const radioBebidas = document.querySelectorAll('input[name="bebida"]');

    // Modais do Sistema (Inatividade & Ajuda)
    const inactivityModal = document.getElementById('inactivity-modal');
    const inactivityCountdown = document.getElementById('inactivity-countdown');
    const btnKeepSession = document.getElementById('btn-keep-session');
    const btnCancelSession = document.getElementById('btn-cancel-session');
    
    const helpModal = document.getElementById('help-modal');
    const btnCloseHelp = document.getElementById('btn-close-help');
    const btnCallAttendant = document.getElementById('btn-call-attendant');

    // Toast de Feedback
    const totemToast = document.getElementById('totem-toast');
    const toastTitle = document.getElementById('toast-title');
    const toastMessage = document.getElementById('toast-message');

    // --------------------------------------------------------------------------
    // 2. ESTADO GLOBAL DA APLICAÇÃO
    // --------------------------------------------------------------------------
    const STATE = {
        currentSlide: 0,
        carouselInterval: null,
        inactivityTimeout: 60, // 60 segundos totais
        warningWindow: 10,     // Últimos 10 segundos para aviso
        idleTimer: null,
        countdownInterval: null,
        remainingSeconds: 60,
        isActiveOrder: false,
        tipoConsumo: sessionStorage.getItem('tipoConsumo') || 'COMER_AQUI',
        activeFilter: 'all',
        searchTerm: '',
        toastTimeout: null,
        
        // Produto atualmente em customização no modal
        customizingItem: {
            produtoId: null,
            nome: '',
            descricao: '',
            imagemUrl: '',
            categoriaId: null,
            categoriaNome: '',
            precoBase: 0.00,
            quantidade: 1,
            adicionaisIds: [],
            adicionaisNomes: [],
            remocoesIds: [],
            remocoesNomes: [],
            tamanhoBatataId: null,
            tamanhoBatataNome: '',
            tamanhoBatataPreco: 0.00,
            bebidaId: null,
            bebidaNome: '',
            bebidaPreco: 0.00,
            observacoes: '',
            precoUnitario: 0.00,
            subtotal: 0.00
        }
    };

    // --------------------------------------------------------------------------
    // 3. UTILITÁRIOS DE FORMATAÇÃO E MOEDA
    // --------------------------------------------------------------------------
    function formatarMoeda(valor) {
        const num = typeof valor === 'number' ? valor : parseFloat(valor) || 0;
        return num.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function normalizarTexto(texto) {
        if (!texto) return '';
        return texto.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
    }

    // --------------------------------------------------------------------------
    // 4. RELÓGIO DIGITAL AO VIVO (HH:mm)
    // --------------------------------------------------------------------------
    function updateClock() {
        if (!clockElement) return;
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        clockElement.textContent = `${hours}:${minutes}`;
    }
    updateClock();
    setInterval(updateClock, 1000);

    // --------------------------------------------------------------------------
    // 5. CARROSSEL DE OFERTAS ROTATIVO (TELA BOAS-VINDAS)
    // --------------------------------------------------------------------------
    function showSlide(index) {
        if (!promoSlides.length) return;
        STATE.currentSlide = (index + promoSlides.length) % promoSlides.length;

        promoSlides.forEach((slide, i) => {
            slide.classList.toggle('active', i === STATE.currentSlide);
        });

        promoDots.forEach((dot, i) => {
            dot.classList.toggle('active', i === STATE.currentSlide);
        });
    }

    function nextSlide() {
        showSlide(STATE.currentSlide + 1);
    }

    function startCarousel() {
        if (STATE.carouselInterval) clearInterval(STATE.carouselInterval);
        STATE.carouselInterval = setInterval(nextSlide, 4500);
    }

    function stopCarousel() {
        if (STATE.carouselInterval) {
            clearInterval(STATE.carouselInterval);
            STATE.carouselInterval = null;
        }
    }

    promoDots.forEach((dot) => {
        dot.addEventListener('click', (e) => {
            e.stopPropagation();
            const index = parseInt(dot.getAttribute('data-index'), 10);
            showSlide(index);
            startCarousel();
        });
    });

    startCarousel();

    // --------------------------------------------------------------------------
    // 6. MÁQUINA DE ESTADOS: NAVEGAÇÃO ENTRE TELAS DO TOTEM
    // --------------------------------------------------------------------------
    function transitionToScreen(screenName) {
        // Esconder todas as telas
        screenWelcome.classList.add('hidden');
        screenDining.classList.add('hidden');
        screenMenu.classList.add('hidden');

        if (screenName === 'welcome') {
            screenWelcome.classList.remove('hidden');
            btnRestart.style.display = 'none';
            STATE.isActiveOrder = false;
            stopInactivityTimer();
            startCarousel();
        } else if (screenName === 'dining') {
            stopCarousel();
            screenDining.classList.remove('hidden');
            btnRestart.style.display = 'inline-flex';
            STATE.isActiveOrder = true;
            resetInactivityTimer();
        } else if (screenName === 'menu') {
            stopCarousel();
            screenMenu.classList.remove('hidden');
            btnRestart.style.display = 'inline-flex';
            STATE.isActiveOrder = true;
            updateDiningLabelUI();
            resetInactivityTimer();
        }
    }

    function updateDiningLabelUI() {
        if (!currentDiningLabel) return;
        if (STATE.tipoConsumo === 'PARA_VIAGEM') {
            currentDiningLabel.textContent = '🛍️ Para Viagem';
        } else {
            currentDiningLabel.textContent = '🍽️ Consumo no Salão';
        }
    }

    // Iniciar pedido a partir da tela de descanso
    if (btnStartOrder) {
        btnStartOrder.addEventListener('click', (e) => {
            e.stopPropagation();
            transitionToScreen('dining');
        });
    }

    if (screenWelcome) {
        screenWelcome.addEventListener('click', () => {
            if (!STATE.isActiveOrder) {
                transitionToScreen('dining');
            }
        });
    }

    // Alterar modalidade a partir do cardápio
    if (btnChangeDining) {
        btnChangeDining.addEventListener('click', (e) => {
            e.stopPropagation();
            transitionToScreen('dining');
        });
    }

    // --------------------------------------------------------------------------
    // 7. SELEÇÃO DA MODALIDADE DE CONSUMO (Para Comer Aqui vs Para Viagem)
    // --------------------------------------------------------------------------
    diningCards.forEach(card => {
        card.addEventListener('click', () => {
            const modalidade = card.getAttribute('data-modalidade');
            selectDiningOption(modalidade, card);
        });

        card.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                const modalidade = card.getAttribute('data-modalidade');
                selectDiningOption(modalidade, card);
            }
        });
    });

    async function selectDiningOption(modalidade, selectedCard) {
        STATE.tipoConsumo = modalidade;
        sessionStorage.setItem('tipoConsumo', modalidade);

        // Feedback visual no card
        diningCards.forEach(c => c.classList.remove('selected'));
        selectedCard.classList.add('selected');

        // Feedback de confirmação
        if (diningFeedback) {
            diningFeedback.classList.remove('hidden');
            const feedbackText = modalidade === 'COMER_AQUI' 
                ? 'Opção: Para Comer Aqui! Abrindo o cardápio...' 
                : 'Opção: Para Viagem! Abrindo o cardápio...';
            diningFeedback.querySelector('.badge').textContent = feedbackText;
        }

        // Salvar na sessão do servidor Spring Boot (background assíncrono)
        fetch('/totem/modalidade', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tipoConsumo: modalidade })
        }).catch(() => {});

        // Transição suave para o cardápio digital (CARD-12)
        setTimeout(() => {
            if (diningFeedback) diningFeedback.classList.add('hidden');
            transitionToScreen('menu');
        }, 600);
    }

    // Se o documento foi aberto com parâmetro de cardápio direto
    if (document.body.getAttribute('data-abrir-cardapio') === 'true') {
        transitionToScreen('menu');
    }

    // --------------------------------------------------------------------------
    // 8. NAVEGAÇÃO POR CATEGORIAS (Barra Lateral com Rolagem Suave)
    // --------------------------------------------------------------------------
    categoryNavItems.forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-target');
            const targetSection = document.getElementById(targetId);

            categoryNavItems.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            if (targetSection && catalogContainer) {
                targetSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });

    // IntersectionObserver para atualizar a categoria ativa conforme a rolagem do cliente
    if ('IntersectionObserver' in window && catalogContainer) {
        const categorySections = document.querySelectorAll('.menu-category-section');
        const observerOptions = {
            root: catalogContainer,
            rootMargin: '-20% 0px -60% 0px',
            threshold: 0
        };

        const categoryObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const catId = entry.target.getAttribute('data-category-id');
                    categoryNavItems.forEach(btn => {
                        const match = btn.getAttribute('data-category-id') === catId;
                        btn.classList.toggle('active', match);
                    });
                }
            });
        }, observerOptions);

        categorySections.forEach(section => categoryObserver.observe(section));
    }

    // --------------------------------------------------------------------------
    // 9. BUSCA RÁPIDA & FILTROS TOUCH NO CARDÁPIO
    // --------------------------------------------------------------------------
    function applyMenuFilters() {
        const query = normalizarTexto(STATE.searchTerm);
        const activeFilter = STATE.activeFilter;
        let visibleCount = 0;

        productCards.forEach(card => {
            const title = normalizarTexto(card.querySelector('.product-title')?.textContent || '');
            const desc = normalizarTexto(card.querySelector('.product-desc')?.textContent || '');
            const badge = card.getAttribute('data-badge') || '';
            
            const matchesSearch = query === '' || title.includes(query) || desc.includes(query);
            const matchesFilter = activeFilter === 'all' || badge === activeFilter;

            if (matchesSearch && matchesFilter) {
                card.style.display = '';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Ocultar seções de categorias vazias durante busca/filtro
        document.querySelectorAll('.menu-category-section').forEach(section => {
            const hasVisibleProducts = Array.from(section.querySelectorAll('.product-card'))
                .some(card => card.style.display !== 'none');
            section.style.display = hasVisibleProducts ? '' : 'none';
        });

        // Exibir empty state se nenhum item corresponder
        if (searchEmptyState) {
            searchEmptyState.classList.toggle('hidden', visibleCount > 0);
        }
    }

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            STATE.searchTerm = e.target.value;
            if (btnClearSearch) {
                btnClearSearch.classList.toggle('hidden', STATE.searchTerm.length === 0);
            }
            applyMenuFilters();
        });
    }

    if (btnClearSearch) {
        btnClearSearch.addEventListener('click', () => {
            if (searchInput) {
                searchInput.value = '';
                STATE.searchTerm = '';
                btnClearSearch.classList.add('hidden');
                searchInput.focus();
                applyMenuFilters();
            }
        });
    }

    if (btnResetSearchEmpty) {
        btnResetSearchEmpty.addEventListener('click', () => {
            if (searchInput) {
                searchInput.value = '';
                STATE.searchTerm = '';
                if (btnClearSearch) btnClearSearch.classList.add('hidden');
            }
            STATE.activeFilter = 'all';
            quickFilterButtons.forEach(btn => btn.classList.toggle('active', btn.getAttribute('data-filter') === 'all'));
            applyMenuFilters();
        });
    }

    quickFilterButtons.forEach(button => {
        button.addEventListener('click', () => {
            quickFilterButtons.forEach(b => b.classList.remove('active'));
            button.classList.add('active');
            STATE.activeFilter = button.getAttribute('data-filter') || 'all';
            applyMenuFilters();
        });
    });

    // --------------------------------------------------------------------------
    // 10. INTERAÇÃO DOS CARDS DE PRODUTOS & CONTROLE DE ESTOQUE ZERO
    // --------------------------------------------------------------------------
    productCards.forEach(card => {
        const isOutOfStock = card.classList.contains('is-out-of-stock') || card.getAttribute('data-stock') === '0';
        const btnAdd = card.querySelector('.btn-add-product');

        // Se o produto está esgotado, bloquear qualquer clique e alertar o cliente
        if (isOutOfStock) {
            card.addEventListener('click', (e) => {
                e.stopPropagation();
                showToast('Produto Esgotado 🚫', 'Este item está temporariamente indisponível.');
            });
            return;
        }

        // Se está disponível, abrir modal de personalização
        card.addEventListener('click', (e) => {
            // Se o clique foi no botão ou no próprio card, abre a personalização
            e.stopPropagation();
            openCustomizationModal(card);
        });

        if (btnAdd) {
            btnAdd.addEventListener('click', (e) => {
                e.stopPropagation();
                openCustomizationModal(card);
            });
        }
    });

    // --------------------------------------------------------------------------
    // 11. MODAL DE PERSONALIZAÇÃO ("MONTE SEU LANCHE" - McDONALD'S STYLE)
    // --------------------------------------------------------------------------
    function openCustomizationModal(productCard) {
        const productId = parseInt(productCard.getAttribute('data-product-id'), 10);
        const categoryId = parseInt(productCard.getAttribute('data-category-id'), 10);
        const title = productCard.querySelector('.product-title')?.textContent || 'Produto Antunes';
        const desc = productCard.querySelector('.product-desc')?.textContent || '';
        const priceText = productCard.querySelector('.product-price-value')?.textContent.replace(',', '.') || '0.00';
        const basePrice = parseFloat(priceText) || 0.00;
        const imgSrc = productCard.querySelector('.product-art')?.getAttribute('src') || '/img/burger-hero.svg';

        // Atualizar estado da customização
        STATE.customizingItem = {
            produtoId: productId,
            nome: title,
            descricao: desc,
            imagemUrl: imgSrc,
            categoriaId: categoryId,
            categoriaNome: getCategoryName(categoryId),
            precoBase: basePrice,
            quantidade: 1,
            adicionaisIds: [],
            adicionaisNomes: [],
            remocoesIds: [],
            remocoesNomes: [],
            tamanhoBatataId: 11, // Batata Média default
            tamanhoBatataNome: 'Batata Média',
            tamanhoBatataPreco: 0.00,
            bebidaId: 13,        // Coca-Cola default
            bebidaNome: 'Coca-Cola Lata 350ml',
            bebidaPreco: 0.00,
            observacoes: '',
            precoUnitario: basePrice,
            subtotal: basePrice
        };

        // Preencher cabeçalho do modal
        if (modalProductTitle) modalProductTitle.textContent = title;
        if (modalProductDesc) modalProductDesc.textContent = desc;
        if (modalProductImg) modalProductImg.src = imgSrc;
        if (modalProductCategory) modalProductCategory.textContent = STATE.customizingItem.categoriaNome;
        if (modalProductBasePrice) modalProductBasePrice.textContent = `R$ ${formatarMoeda(basePrice)}`;
        if (modalQtyValue) modalQtyValue.textContent = '1';
        if (modalObservacoes) modalObservacoes.value = '';

        // Resetar opções do modal
        resetModalOptionsUI(categoryId);

        // Calcular e exibir valor dinâmico
        recalcularValorCustomizado();

        // Abrir modal com foco
        if (modalCustomizacao) {
            modalCustomizacao.classList.add('active');
            document.body.style.overflow = 'hidden'; // Bloquear rolagem do fundo
        }
    }

    function getCategoryName(categoryId) {
        switch (categoryId) {
            case 1: return 'Hambúrguer Artesanal';
            case 2: return 'Acompanhamento';
            case 3: return 'Bebida';
            case 4: return 'Combo Especial';
            case 5: return 'Sobremesa & Shake';
            default: return 'Cardápio';
        }
    }

    function resetModalOptionsUI(categoryId) {
        // Desmarcar todos os adicionais
        const checkboxesAdicionais = containerAdicionais?.querySelectorAll('.option-checkbox');
        checkboxesAdicionais?.forEach(cb => {
            cb.checked = false;
            cb.closest('.option-check-card')?.classList.remove('selected');
        });

        // Resetar botões de remoção (nenhum removido por padrão)
        const buttonsRemocoes = containerRemocoes?.querySelectorAll('.btn-removal-toggle');
        buttonsRemocoes?.forEach(btn => {
            btn.classList.remove('active-removed');
        });

        // Seção Combo: se for lanche ou combo, habilitar; para bebidas ou sobremesas simples, esconder
        if (sectionComboUpgrade) {
            const showComboSection = (categoryId === 1 || categoryId === 4);
            sectionComboUpgrade.style.display = showComboSection ? 'block' : 'none';
        }

        // Resetar rádios de tamanho de batata e bebida para as opções padrão (Incluso)
        radioTamanhosBatata.forEach(radio => {
            const isMedium = radio.value === '11';
            radio.checked = isMedium;
            radio.closest('.combo-radio-card')?.classList.toggle('active', isMedium);
        });

        radioBebidas.forEach(radio => {
            const isCoca = radio.value === '13';
            radio.checked = isCoca;
            radio.closest('.combo-radio-card')?.classList.toggle('active', isCoca);
        });
    }

    function closeCustomizationModal() {
        if (modalCustomizacao) {
            modalCustomizacao.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    if (btnCloseCustomModal) {
        btnCloseCustomModal.addEventListener('click', (e) => {
            e.stopPropagation();
            closeCustomizationModal();
        });
    }

    if (btnCancelCustomModal) {
        btnCancelCustomModal.addEventListener('click', (e) => {
            e.stopPropagation();
            closeCustomizationModal();
        });
    }

    // Fechar ao clicar no backdrop escuro do modal
    if (modalCustomizacao) {
        modalCustomizacao.addEventListener('click', (e) => {
            if (e.target === modalCustomizacao) {
                closeCustomizationModal();
            }
        });
    }

    // --------------------------------------------------------------------------
    // 12. CÁLCULO DINÂMICO & INSTANTÂNEO DE ADICIONAIS E COMBOS (McDONALD'S STYLE)
    // --------------------------------------------------------------------------
    // Eventos nos checkboxes de adicionais (+ Bacon, + Cheddar, etc.)
    containerAdicionais?.querySelectorAll('.option-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', () => {
            const card = checkbox.closest('.option-check-card');
            card?.classList.toggle('selected', checkbox.checked);
            recalcularValorCustomizado();
        });
    });

    // Eventos nos botões de remoção ("Sem cebola", "Sem picles", etc.)
    containerRemocoes?.querySelectorAll('.btn-removal-toggle').forEach(button => {
        button.addEventListener('click', () => {
            // Alternar estado de ingrediente removido
            button.classList.toggle('active-removed');
            const isRemoved = button.classList.contains('active-removed');
            
            // Feedback tátil visual
            const label = button.querySelector('.removal-label');
            if (label) {
                const baseName = button.getAttribute('data-nome') || '';
                label.textContent = isRemoved ? `✓ ${baseName} (Removido)` : baseName;
            }
            recalcularValorCustomizado();
        });
    });

    // Eventos nos seletores de rádio para tamanho de batata
    radioTamanhosBatata.forEach(radio => {
        radio.addEventListener('change', () => {
            radioTamanhosBatata.forEach(r => r.closest('.combo-radio-card')?.classList.remove('active'));
            radio.closest('.combo-radio-card')?.classList.add('active');
            
            const preco = parseFloat(radio.getAttribute('data-preco')) || 0.00;
            const nome = radio.getAttribute('data-nome') || '';
            STATE.customizingItem.tamanhoBatataId = parseInt(radio.value, 10);
            STATE.customizingItem.tamanhoBatataNome = nome;
            STATE.customizingItem.tamanhoBatataPreco = preco;
            
            recalcularValorCustomizado();
        });
    });

    // Eventos nos seletores de rádio para bebidas
    radioBebidas.forEach(radio => {
        radio.addEventListener('change', () => {
            radioBebidas.forEach(r => r.closest('.combo-radio-card')?.classList.remove('active'));
            radio.closest('.combo-radio-card')?.classList.add('active');

            const preco = parseFloat(radio.getAttribute('data-preco')) || 0.00;
            const nome = radio.getAttribute('data-nome') || '';
            STATE.customizingItem.bebidaId = parseInt(radio.value, 10);
            STATE.customizingItem.bebidaNome = nome;
            STATE.customizingItem.bebidaPreco = preco;

            recalcularValorCustomizado();
        });
    });

    // Stepper de Quantidade
    if (btnQtyMinus) {
        btnQtyMinus.addEventListener('click', (e) => {
            e.stopPropagation();
            if (STATE.customizingItem.quantidade > 1) {
                STATE.customizingItem.quantidade -= 1;
                if (modalQtyValue) modalQtyValue.textContent = STATE.customizingItem.quantidade;
                recalcularValorCustomizado();
            }
        });
    }

    if (btnQtyPlus) {
        btnQtyPlus.addEventListener('click', (e) => {
            e.stopPropagation();
            if (STATE.customizingItem.quantidade < 20) {
                STATE.customizingItem.quantidade += 1;
                if (modalQtyValue) modalQtyValue.textContent = STATE.customizingItem.quantidade;
                recalcularValorCustomizado();
            }
        });
    }

    function recalcularValorCustomizado() {
        const item = STATE.customizingItem;
        let somaAdicionais = 0.00;
        const adicionaisIds = [];
        const adicionaisNomes = [];

        // 1. Somar adicionais marcados
        containerAdicionais?.querySelectorAll('.option-checkbox:checked').forEach(cb => {
            const preco = parseFloat(cb.getAttribute('data-preco')) || 0.00;
            const id = parseInt(cb.value, 10);
            const nome = cb.getAttribute('data-nome') || '';
            somaAdicionais += preco;
            adicionaisIds.push(id);
            adicionaisNomes.push(`${nome} (+R$ ${formatarMoeda(preco)})`);
        });

        // 2. Coletar ingredientes removidos
        const remocoesIds = [];
        const remocoesNomes = [];
        containerRemocoes?.querySelectorAll('.btn-removal-toggle.active-removed').forEach(btn => {
            const id = parseInt(btn.getAttribute('data-opcao-id'), 10);
            const nome = btn.getAttribute('data-nome') || '';
            remocoesIds.push(id);
            remocoesNomes.push(nome);
        });

        // 3. Upgrades de combo (se a seção estiver visível)
        let upgradeTamanho = 0.00;
        let upgradeBebida = 0.00;
        const isComboVisible = sectionComboUpgrade && sectionComboUpgrade.style.display !== 'none';

        if (isComboVisible) {
            upgradeTamanho = item.tamanhoBatataPreco || 0.00;
            upgradeBebida = item.bebidaPreco || 0.00;
        }

        // 4. Preço Unitário e Subtotal
        const precoUnitario = item.precoBase + somaAdicionais + upgradeTamanho + upgradeBebida;
        const subtotal = precoUnitario * item.quantidade;

        // Atualizar estado
        item.adicionaisIds = adicionaisIds;
        item.adicionaisNomes = adicionaisNomes;
        item.remocoesIds = remocoesIds;
        item.remocoesNomes = remocoesNomes;
        item.precoUnitario = precoUnitario;
        item.subtotal = subtotal;

        // Atualizar display animado
        if (modalDynamicTotal) {
            modalDynamicTotal.textContent = formatarMoeda(subtotal);
            modalDynamicTotal.classList.remove('animate-price');
            void modalDynamicTotal.offsetWidth; // Trigger reflow para reiniciar animação
            modalDynamicTotal.classList.add('animate-price');
        }
    }

    // --------------------------------------------------------------------------
    // 13. COMPILAÇÃO DO OBJETO JSON & INCLUSÃO NO CARRINHO
    // --------------------------------------------------------------------------
    if (btnConfirmAddCart) {
        btnConfirmAddCart.addEventListener('click', (e) => {
            e.stopPropagation();
            adicionarItemAoPedido();
        });
    }

    function adicionarItemAoPedido() {
        const item = STATE.customizingItem;
        if (!item.produtoId) return;

        // Obter observações textuais digitadas
        const obsDigitada = modalObservacoes?.value.trim() || '';
        item.observacoes = obsDigitada;

        // Compilar objeto JSON estruturado pronto para o carrinho e backend (CustomizacaoItemDTO)
        const itemCompilado = {
            idItemTemp: Date.now() + Math.random().toString(36).substr(2, 5),
            produtoId: item.produtoId,
            produtoNome: item.nome,
            imagemUrl: item.imagemUrl,
            categoriaNome: item.categoriaNome,
            precoBaseProduto: item.precoBase,
            quantidade: item.quantidade,
            adicionaisIds: item.adicionaisIds,
            adicionaisNomes: item.adicionaisNomes,
            remocoesIds: item.remocoesIds,
            remocoes: item.remocoesNomes,
            tamanhoBatataId: item.tamanhoBatataId,
            tamanhoBatataNome: item.tamanhoBatataNome,
            bebidaId: item.bebidaId,
            bebidaNome: item.bebidaNome,
            observacoes: item.observacoes,
            precoUnitario: item.precoUnitario,
            subtotal: item.subtotal
        };

        // Salvar no carrinho da sessão (SessionStorage / LocalStorage)
        salvarItemNoStorage(itemCompilado);

        // Feedback sonoro/visual para o cliente
        showToast('Item Adicionado! 🛒', `${item.nome} (${item.quantidade}x) incluído no carrinho.`);

        // Fechar modal de customização
        closeCustomizationModal();
    }

    function salvarItemNoStorage(novoItem) {
        let carrinho = [];
        try {
            const salvo = sessionStorage.getItem('pdv_carrinho');
            if (salvo) {
                carrinho = JSON.parse(salvo);
            }
        } catch (err) {
            console.warn('Erro ao ler carrinho do storage:', err);
            carrinho = [];
        }

        carrinho.push(novoItem);
        sessionStorage.setItem('pdv_carrinho', JSON.stringify(carrinho));
        console.log('Objeto JSON compilado para o carrinho:', novoItem);
    }

    function showToast(title, message) {
        if (!totemToast) return;
        if (toastTitle) toastTitle.textContent = title;
        if (toastMessage) toastMessage.textContent = message;

        totemToast.classList.remove('hidden');
        totemToast.classList.add('visible');

        if (STATE.toastTimeout) clearTimeout(STATE.toastTimeout);
        STATE.toastTimeout = setTimeout(() => {
            totemToast.classList.remove('visible');
            setTimeout(() => totemToast.classList.add('hidden'), 300);
        }, 3200);
    }

    // --------------------------------------------------------------------------
    // 14. TEMPORIZADOR DE INATIVIDADE (60 Segundos com Contagem nos últimos 10s)
    // --------------------------------------------------------------------------
    function resetInactivityTimer() {
        if (!STATE.isActiveOrder) return;

        if (STATE.idleTimer) clearTimeout(STATE.idleTimer);
        if (STATE.countdownInterval) clearInterval(STATE.countdownInterval);

        closeInactivityModal();
        STATE.remainingSeconds = STATE.inactivityTimeout;

        const timeUntilWarning = (STATE.inactivityTimeout - STATE.warningWindow) * 1000;
        STATE.idleTimer = setTimeout(() => {
            showInactivityWarning();
        }, timeUntilWarning);
    }

    function stopInactivityTimer() {
        if (STATE.idleTimer) clearTimeout(STATE.idleTimer);
        if (STATE.countdownInterval) clearInterval(STATE.countdownInterval);
        closeInactivityModal();
    }

    function showInactivityWarning() {
        if (!STATE.isActiveOrder) return;

        let secondsLeft = STATE.warningWindow;
        if (inactivityCountdown) inactivityCountdown.textContent = secondsLeft;
        openInactivityModal();

        STATE.countdownInterval = setInterval(() => {
            secondsLeft -= 1;
            if (inactivityCountdown) inactivityCountdown.textContent = secondsLeft;

            if (secondsLeft <= 0) {
                clearInterval(STATE.countdownInterval);
                resetTotemToWelcome();
            }
        }, 1000);
    }

    function openInactivityModal() {
        if (inactivityModal) inactivityModal.classList.add('active');
    }

    function closeInactivityModal() {
        if (inactivityModal) inactivityModal.classList.remove('active');
    }

    // Ouvintes de atividade do usuário
    const activityEvents = ['touchstart', 'touchmove', 'mousedown', 'mousemove', 'keydown', 'scroll', 'click'];
    activityEvents.forEach(eventType => {
        window.addEventListener(eventType, () => {
            if (!inactivityModal || !inactivityModal.classList.contains('active')) {
                resetInactivityTimer();
            }
        }, { passive: true });
    });

    if (btnKeepSession) {
        btnKeepSession.addEventListener('click', (e) => {
            e.stopPropagation();
            resetInactivityTimer();
        });
    }

    if (btnCancelSession) {
        btnCancelSession.addEventListener('click', (e) => {
            e.stopPropagation();
            resetTotemToWelcome();
        });
    }

    // --------------------------------------------------------------------------
    // 15. RESET TOTAL DO TOTEM (Recomeçar Atendimento)
    // --------------------------------------------------------------------------
    function resetTotemToWelcome() {
        stopInactivityTimer();
        closeHelpModal();
        closeCustomizationModal();

        sessionStorage.removeItem('tipoConsumo');
        sessionStorage.removeItem('pdv_carrinho');

        diningCards.forEach(c => c.classList.remove('selected'));
        if (diningFeedback) diningFeedback.classList.add('hidden');
        if (searchInput) searchInput.value = '';
        STATE.searchTerm = '';
        STATE.activeFilter = 'all';

        transitionToScreen('welcome');

        // Notificar backend
        fetch('/totem/reset', { method: 'POST' }).catch(() => {});
        showSlide(0);
        startCarousel();
    }

    if (btnRestart) {
        btnRestart.addEventListener('click', (e) => {
            e.stopPropagation();
            resetTotemToWelcome();
        });
    }

    // --------------------------------------------------------------------------
    // 16. MODAL DE AJUDA
    // --------------------------------------------------------------------------
    function openHelpModal() {
        if (helpModal) helpModal.classList.add('active');
    }

    function closeHelpModal() {
        if (helpModal) helpModal.classList.remove('active');
    }

    if (btnHelp) {
        btnHelp.addEventListener('click', (e) => {
            e.stopPropagation();
            openHelpModal();
        });
    }

    if (btnCloseHelp) {
        btnCloseHelp.addEventListener('click', (e) => {
            e.stopPropagation();
            closeHelpModal();
        });
    }

    if (btnCallAttendant) {
        btnCallAttendant.addEventListener('click', (e) => {
            e.stopPropagation();
            btnCallAttendant.textContent = 'Atendente Chamado! Aguarde um instante...';
            btnCallAttendant.disabled = true;
            setTimeout(() => {
                closeHelpModal();
                btnCallAttendant.textContent = 'Chamar Atendente no Totem';
                btnCallAttendant.disabled = false;
            }, 2500);
        });
    }
});
