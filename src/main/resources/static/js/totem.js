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
    const screenIdentification = document.getElementById('screen-identification');

    // Barra Inferior Persistente do Carrinho
    const floatingCartBar = document.getElementById('floating-cart-bar');
    const floatingCartCount = document.getElementById('floating-cart-count');
    const floatingCartSummary = document.getElementById('floating-cart-summary');
    const floatingCartTotal = document.getElementById('floating-cart-total');
    const btnToggleCartDrawer = document.getElementById('btn-toggle-cart-drawer');
    const btnFloatingCheckout = document.getElementById('btn-floating-checkout');

    // Drawer Expansível do Carrinho
    const cartDrawerModal = document.getElementById('cart-drawer-modal');
    const drawerItemCount = document.getElementById('drawer-item-count');
    const btnClearCart = document.getElementById('btn-clear-cart');
    const btnCloseCartDrawer = document.getElementById('btn-close-cart-drawer');
    const cartEmptyState = document.getElementById('cart-empty-state');
    const btnEmptyCartMenu = document.getElementById('btn-empty-cart-menu');
    const cartItemsList = document.getElementById('cart-items-list');
    const cartDrawerFooter = document.getElementById('cart-drawer-footer');
    const drawerSubtotal = document.getElementById('drawer-subtotal');
    const drawerDiscount = document.getElementById('drawer-discount');
    const drawerTotalAmount = document.getElementById('drawer-total-amount');
    const btnDrawerContinue = document.getElementById('btn-drawer-continue');
    const btnDrawerCheckout = document.getElementById('btn-drawer-checkout');

    // Tela de Identificação do Cliente
    const idSummaryModalidade = document.getElementById('id-summary-modalidade');
    const idSummaryItems = document.getElementById('id-summary-items');
    const idSummaryTotal = document.getElementById('id-summary-total');
    const cardFieldNome = document.getElementById('card-field-nome');
    const cardFieldCpf = document.getElementById('card-field-cpf');
    const touchDisplayNome = document.getElementById('touch-display-nome');
    const touchDisplayCpf = document.getElementById('touch-display-cpf');
    const nomeDisplayText = document.getElementById('nome-display-text');
    const cpfDisplayText = document.getElementById('cpf-display-text');
    const cursorNome = document.getElementById('cursor-nome');
    const cursorCpf = document.getElementById('cursor-cpf');
    const btnClearNome = document.getElementById('btn-clear-nome');
    const btnClearCpf = document.getElementById('btn-clear-cpf');
    const btnSkipCpf = document.getElementById('btn-skip-cpf');
    const cpfFeedback = document.getElementById('cpf-feedback');
    const btnIdBackMenu = document.getElementById('btn-id-back-menu');
    const btnIdSkipAll = document.getElementById('btn-id-skip-all');
    const btnIdAdvancePayment = document.getElementById('btn-id-advance-payment');

    // Teclados Virtuais Touch
    const keyboardSwitcherTitle = document.getElementById('keyboard-title');
    const tabKbNome = document.getElementById('tab-kb-nome');
    const tabKbCpf = document.getElementById('tab-kb-cpf');
    const keyboardQwerty = document.getElementById('keyboard-qwerty');
    const keyboardNumpad = document.getElementById('keyboard-numpad');
    const btnKbNextCpf = document.getElementById('btn-kb-next-cpf');
    
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
    function carregarCarrinhoDoStorage() {
        try {
            const salvo = sessionStorage.getItem('pdv_carrinho');
            return salvo ? JSON.parse(salvo) : [];
        } catch (e) {
            console.warn('Erro ao carregar carrinho inicial do sessionStorage:', e);
            return [];
        }
    }

    const STATE = {
        currentSlide: 0,
        carouselInterval: null,
        inactivityTimeout: 60, // 60 segundos totais
        warningWindow: 10,     // Últimos 10 segundos para aviso
        idleTimer: null,
        countdownInterval: null,
        remainingSeconds: 60,
        isActiveOrder: false,
        currentScreen: 'welcome',
        tipoConsumo: sessionStorage.getItem('tipoConsumo') || 'COMER_AQUI',
        activeFilter: 'all',
        searchTerm: '',
        toastTimeout: null,
        
        // Carrinho de Compras & Identificação do Cliente (CARD-13)
        carrinho: carregarCarrinhoDoStorage(),
        nomeCliente: sessionStorage.getItem('pdv_nome_cliente') || '',
        cpfCliente: sessionStorage.getItem('pdv_cpf_cliente') || '',
        activeInputField: 'nome',
        
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
        STATE.currentScreen = screenName;

        // Esconder todas as telas
        screenWelcome.classList.add('hidden');
        screenDining.classList.add('hidden');
        screenMenu.classList.add('hidden');
        if (screenIdentification) screenIdentification.classList.add('hidden');

        if (screenName === 'welcome') {
            screenWelcome.classList.remove('hidden');
            btnRestart.style.display = 'none';
            STATE.isActiveOrder = false;
            stopInactivityTimer();
            startCarousel();
            if (floatingCartBar) floatingCartBar.classList.add('hidden');
            fecharCartDrawer();
        } else if (screenName === 'dining') {
            stopCarousel();
            screenDining.classList.remove('hidden');
            btnRestart.style.display = 'inline-flex';
            STATE.isActiveOrder = true;
            resetInactivityTimer();
            if (floatingCartBar) floatingCartBar.classList.add('hidden');
        } else if (screenName === 'menu') {
            stopCarousel();
            screenMenu.classList.remove('hidden');
            btnRestart.style.display = 'inline-flex';
            STATE.isActiveOrder = true;
            updateDiningLabelUI();
            resetInactivityTimer();
            atualizarCarrinhoUI();
        } else if (screenName === 'identification') {
            stopCarousel();
            if (screenIdentification) screenIdentification.classList.remove('hidden');
            btnRestart.style.display = 'inline-flex';
            STATE.isActiveOrder = true;
            if (floatingCartBar) floatingCartBar.classList.add('hidden');
            fecharCartDrawer();
            atualizarResumoIdentificacao();
            selecionarCampoIdentificacao('nome');
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

        adicionarItemAoCarrinho(itemCompilado);
        closeCustomizationModal();
    }

    // --------------------------------------------------------------------------
    // 13.1 GESTÃO DO CARRINHO DE COMPRAS & DRAWER (CARD-13)
    // --------------------------------------------------------------------------
    function persistirCarrinho() {
        sessionStorage.setItem('pdv_carrinho', JSON.stringify(STATE.carrinho));
    }

    function adicionarItemAoCarrinho(novoItem) {
        STATE.carrinho.push(novoItem);
        persistirCarrinho();
        atualizarCarrinhoUI();
        showToast('Item Adicionado! 🛒', `${novoItem.produtoNome} (${novoItem.quantidade}x) incluído na sua sacola.`);
        console.log('Item adicionado ao carrinho:', novoItem);
    }

    function alterarQuantidadeItemCarrinho(idItemTemp, delta) {
        const item = STATE.carrinho.find(i => String(i.idItemTemp) === String(idItemTemp));
        if (!item) return;

        item.quantidade += delta;
        if (item.quantidade <= 0) {
            removerItemCarrinho(idItemTemp);
            return;
        }

        item.subtotal = item.precoUnitario * item.quantidade;
        persistirCarrinho();
        atualizarCarrinhoUI();
    }

    function removerItemCarrinho(idItemTemp) {
        STATE.carrinho = STATE.carrinho.filter(i => String(i.idItemTemp) !== String(idItemTemp));
        persistirCarrinho();
        atualizarCarrinhoUI();
        showToast('Item Removido 🗑️', 'O item foi retirado da sua sacola.');
    }

    function limparCarrinho() {
        STATE.carrinho = [];
        persistirCarrinho();
        atualizarCarrinhoUI();
        showToast('Sacola Vazia 🧹', 'Todos os itens foram removidos do carrinho.');
    }

    function abrirCartDrawer() {
        if (cartDrawerModal) {
            cartDrawerModal.classList.add('active');
            document.body.style.overflow = 'hidden';
            atualizarCarrinhoUI();
        }
    }

    function fecharCartDrawer() {
        if (cartDrawerModal) {
            cartDrawerModal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    function atualizarCarrinhoUI() {
        let totalQtd = 0;
        let totalValor = 0.00;

        STATE.carrinho.forEach(item => {
            totalQtd += item.quantidade;
            totalValor += item.subtotal;
        });

        // 1. Atualizar Barra Flutuante
        if (floatingCartCount) floatingCartCount.textContent = totalQtd;
        if (floatingCartSummary) {
            floatingCartSummary.textContent = totalQtd === 1 ? '1 item selecionado' : `${totalQtd} itens selecionados`;
        }
        if (floatingCartTotal) floatingCartTotal.textContent = formatarMoeda(totalValor);

        if (floatingCartBar) {
            const isMenuScreen = (STATE.currentScreen === 'menu');
            if (totalQtd > 0 && isMenuScreen) {
                floatingCartBar.classList.remove('hidden');
            } else {
                floatingCartBar.classList.add('hidden');
            }
        }

        // 2. Atualizar Drawer
        if (drawerItemCount) drawerItemCount.textContent = totalQtd;
        if (drawerSubtotal) drawerSubtotal.textContent = 'R$ ' + formatarMoeda(totalValor);
        if (drawerDiscount) drawerDiscount.textContent = 'R$ 0,00';
        if (drawerTotalAmount) drawerTotalAmount.textContent = formatarMoeda(totalValor);

        if (totalQtd === 0) {
            if (cartEmptyState) cartEmptyState.classList.remove('hidden');
            if (cartItemsList) {
                cartItemsList.classList.add('hidden');
                cartItemsList.innerHTML = '';
            }
            if (cartDrawerFooter) cartDrawerFooter.classList.add('hidden');
        } else {
            if (cartEmptyState) cartEmptyState.classList.add('hidden');
            if (cartItemsList) {
                cartItemsList.classList.remove('hidden');
                renderizarItensDrawer(cartItemsList);
            }
            if (cartDrawerFooter) cartDrawerFooter.classList.remove('hidden');
        }
    }

    function renderizarItensDrawer(container) {
        container.innerHTML = '';

        STATE.carrinho.forEach(item => {
            const card = document.createElement('article');
            card.className = 'cart-item-card';

            // Montar tags de personalização
            let tagsHtml = '';
            if (item.tamanhoBatataNome || item.bebidaNome) {
                const batata = item.tamanhoBatataNome ? `🍟 ${item.tamanhoBatataNome}` : '';
                const bebida = item.bebidaNome ? `🥤 ${item.bebidaNome}` : '';
                const comboInfo = [batata, bebida].filter(Boolean).join(' + ');
                if (comboInfo) {
                    tagsHtml += `<span class="cart-tag tag-combo">${comboInfo}</span>`;
                }
            }

            if (item.adicionaisNomes && item.adicionaisNomes.length > 0) {
                item.adicionaisNomes.forEach(add => {
                    tagsHtml += `<span class="cart-tag tag-adicional">+ ${add}</span>`;
                });
            }

            if (item.remocoes && item.remocoes.length > 0) {
                item.remocoes.forEach(rem => {
                    tagsHtml += `<span class="cart-tag tag-remocao">Sem ${rem}</span>`;
                });
            }

            if (item.observacoes) {
                tagsHtml += `<span class="cart-tag tag-obs">Obs: "${item.observacoes}"</span>`;
            }

            const imgFallback = item.imagemUrl || '/img/burger-hero.svg';

            card.innerHTML = `
                <div class="cart-item-media">
                    <img src="${imgFallback}" alt="${item.produtoNome}" class="cart-item-art" onerror="this.src='/img/burger-hero.svg'">
                </div>
                <div class="cart-item-details">
                    <div class="cart-item-head">
                        <h4 class="cart-item-name">${item.produtoNome}</h4>
                        <span class="cart-item-unit-price">R$ ${formatarMoeda(item.precoUnitario)} un</span>
                    </div>
                    ${tagsHtml ? `<div class="cart-item-tags">${tagsHtml}</div>` : ''}
                    <div class="cart-item-bottom">
                        <div class="cart-stepper" role="group" aria-label="Quantidade">
                            <button type="button" class="btn-cart-stepper btn-minus" data-id="${item.idItemTemp}" aria-label="Diminuir">−</button>
                            <span class="cart-stepper-val">${item.quantidade}</span>
                            <button type="button" class="btn-cart-stepper btn-plus" data-id="${item.idItemTemp}" aria-label="Aumentar">+</button>
                        </div>
                        <div class="cart-item-subtotal-box">
                            <span class="currency">R$</span>
                            <span class="amount">${formatarMoeda(item.subtotal)}</span>
                        </div>
                        <button type="button" class="btn-remove-item" data-id="${item.idItemTemp}" title="Remover item da sacola" aria-label="Remover ${item.produtoNome}">🗑️</button>
                    </div>
                </div>
            `;

            // Eventos nos botões do card
            card.querySelector('.btn-minus')?.addEventListener('click', (e) => {
                e.stopPropagation();
                alterarQuantidadeItemCarrinho(item.idItemTemp, -1);
            });

            card.querySelector('.btn-plus')?.addEventListener('click', (e) => {
                e.stopPropagation();
                alterarQuantidadeItemCarrinho(item.idItemTemp, 1);
            });

            card.querySelector('.btn-remove-item')?.addEventListener('click', (e) => {
                e.stopPropagation();
                removerItemCarrinho(item.idItemTemp);
            });

            container.appendChild(card);
        });
    }

    // Ouvintes dos botões do carrinho
    if (btnToggleCartDrawer) {
        btnToggleCartDrawer.addEventListener('click', (e) => {
            e.stopPropagation();
            abrirCartDrawer();
        });
    }

    if (btnCloseCartDrawer) {
        btnCloseCartDrawer.addEventListener('click', (e) => {
            e.stopPropagation();
            fecharCartDrawer();
        });
    }

    if (btnDrawerContinue) {
        btnDrawerContinue.addEventListener('click', (e) => {
            e.stopPropagation();
            fecharCartDrawer();
        });
    }

    if (btnEmptyCartMenu) {
        btnEmptyCartMenu.addEventListener('click', (e) => {
            e.stopPropagation();
            fecharCartDrawer();
            transitionToScreen('menu');
        });
    }

    if (btnClearCart) {
        btnClearCart.addEventListener('click', (e) => {
            e.stopPropagation();
            limparCarrinho();
        });
    }

    // Avançar para a Identificação a partir do Carrinho
    function irParaIdentificacao() {
        if (!STATE.carrinho || STATE.carrinho.length === 0) {
            showToast('Sacola Vazia ⚠️', 'Adicione itens ao seu pedido antes de finalizar.');
            return;
        }
        fecharCartDrawer();
        transitionToScreen('identification');
    }

    if (btnFloatingCheckout) {
        btnFloatingCheckout.addEventListener('click', (e) => {
            e.stopPropagation();
            irParaIdentificacao();
        });
    }

    if (btnDrawerCheckout) {
        btnDrawerCheckout.addEventListener('click', (e) => {
            e.stopPropagation();
            irParaIdentificacao();
        });
    }

    // --------------------------------------------------------------------------
    // 13.2 IDENTIFICAÇÃO DO CLIENTE & TECLADOS VIRTUAIS TOUCH (CARD-13)
    // --------------------------------------------------------------------------
    function atualizarResumoIdentificacao() {
        let totalQtd = 0;
        let totalValor = 0.00;

        STATE.carrinho.forEach(item => {
            totalQtd += item.quantidade;
            totalValor += item.subtotal;
        });

        if (idSummaryModalidade) {
            idSummaryModalidade.textContent = (STATE.tipoConsumo === 'PARA_VIAGEM') ? 'Para Viagem 🛍️' : 'Para Comer Aqui 🍽️';
        }
        if (idSummaryItems) {
            idSummaryItems.textContent = totalQtd === 1 ? '1 item' : `${totalQtd} itens`;
        }
        if (idSummaryTotal) {
            idSummaryTotal.textContent = 'R$ ' + formatarMoeda(totalValor);
        }
    }

    function selecionarCampoIdentificacao(campo) {
        STATE.activeInputField = campo;

        const isNome = (campo === 'nome');
        if (cardFieldNome) cardFieldNome.classList.toggle('active', isNome);
        if (cardFieldCpf) cardFieldCpf.classList.toggle('active', !isNome);

        if (cursorNome) cursorNome.classList.toggle('hidden', !isNome);
        if (cursorCpf) cursorCpf.classList.toggle('hidden', isNome);

        // Alternar teclado virtual
        if (isNome) {
            if (keyboardQwerty) keyboardQwerty.classList.remove('hidden');
            if (keyboardNumpad) keyboardNumpad.classList.add('hidden');
            if (tabKbNome) tabKbNome.classList.add('active');
            if (tabKbCpf) tabKbCpf.classList.remove('active');
            if (keyboardSwitcherTitle) keyboardSwitcherTitle.textContent = 'Teclado Virtual Touch: Nome';
        } else {
            if (keyboardQwerty) keyboardQwerty.classList.add('hidden');
            if (keyboardNumpad) keyboardNumpad.classList.remove('hidden');
            if (tabKbNome) tabKbNome.classList.remove('active');
            if (tabKbCpf) tabKbCpf.classList.add('active');
            if (keyboardSwitcherTitle) keyboardSwitcherTitle.textContent = 'Teclado Virtual Numérico: CPF na Nota';
        }
    }

    function formatarCpf(digitos) {
        if (!digitos) return '';
        const limpo = digitos.replace(/\D/g, '').slice(0, 11);
        let formatado = '';
        for (let i = 0; i < limpo.length; i++) {
            if (i === 3 || i === 6) formatado += '.';
            if (i === 9) formatado += '-';
            formatado += limpo[i];
        }
        return formatado;
    }

    function validarAlgoritmoCpf(cpfString) {
        if (!cpfString) return true; // Vazio é opcional
        const limpo = cpfString.replace(/\D/g, '');
        if (limpo.length !== 11) return false;
        if (/^(\d)\1{10}$/.test(limpo)) return false;

        let soma = 0;
        for (let i = 0; i < 9; i++) {
            soma += parseInt(limpo.charAt(i), 10) * (10 - i);
        }
        let d1 = 11 - (soma % 11);
        if (d1 >= 10) d1 = 0;
        if (d1 !== parseInt(limpo.charAt(9), 10)) return false;

        soma = 0;
        for (let i = 0; i < 10; i++) {
            soma += parseInt(limpo.charAt(i), 10) * (11 - i);
        }
        let d2 = 11 - (soma % 11);
        if (d2 >= 10) d2 = 0;
        return d2 === parseInt(limpo.charAt(10), 10);
    }

    function atualizarDisplaysIdentificacao() {
        // 1. Display Nome
        if (nomeDisplayText) {
            if (STATE.nomeCliente && STATE.nomeCliente.trim().length > 0) {
                nomeDisplayText.textContent = STATE.nomeCliente;
                nomeDisplayText.classList.remove('placeholder');
                if (btnClearNome) btnClearNome.classList.remove('hidden');
            } else {
                nomeDisplayText.textContent = 'Toque para digitar seu nome...';
                nomeDisplayText.classList.add('placeholder');
                if (btnClearNome) btnClearNome.classList.add('hidden');
            }
        }

        // 2. Display CPF
        const rawCpf = (STATE.cpfCliente || '').replace(/\D/g, '');
        const formatado = formatarCpf(rawCpf);

        if (cpfDisplayText) {
            if (formatado && formatado.length > 0) {
                cpfDisplayText.textContent = formatado;
                cpfDisplayText.classList.remove('placeholder');
                if (btnClearCpf) btnClearCpf.classList.remove('hidden');
            } else {
                cpfDisplayText.textContent = '___.___.___-__';
                cpfDisplayText.classList.add('placeholder');
                if (btnClearCpf) btnClearCpf.classList.add('hidden');
            }
        }

        // Feedback de validação do CPF
        if (cpfFeedback) {
            if (!rawCpf || rawCpf.length === 0) {
                cpfFeedback.textContent = '';
                cpfFeedback.className = 'cpf-validation-feedback';
            } else if (rawCpf.length < 11) {
                cpfFeedback.textContent = `Faltam ${11 - rawCpf.length} dígito(s)...`;
                cpfFeedback.className = 'cpf-validation-feedback';
            } else {
                const valido = validarAlgoritmoCpf(rawCpf);
                if (valido) {
                    cpfFeedback.textContent = 'CPF Válido ✅';
                    cpfFeedback.className = 'cpf-validation-feedback valid';
                } else {
                    cpfFeedback.textContent = 'CPF Inválido ⚠️';
                    cpfFeedback.className = 'cpf-validation-feedback invalid';
                }
            }
        }
    }

    function digitarNoTeclado(caractere) {
        if (STATE.activeInputField === 'nome') {
            if (STATE.nomeCliente.length < 40) {
                STATE.nomeCliente += caractere;
                sessionStorage.setItem('pdv_nome_cliente', STATE.nomeCliente);
                atualizarDisplaysIdentificacao();
            }
        } else if (STATE.activeInputField === 'cpf') {
            const rawCpf = STATE.cpfCliente.replace(/\D/g, '');
            if (/\d/.test(caractere) && rawCpf.length < 11) {
                STATE.cpfCliente = formatarCpf(rawCpf + caractere);
                sessionStorage.setItem('pdv_cpf_cliente', STATE.cpfCliente);
                atualizarDisplaysIdentificacao();
            }
        }
    }

    function apagarNoTeclado() {
        if (STATE.activeInputField === 'nome') {
            if (STATE.nomeCliente.length > 0) {
                STATE.nomeCliente = STATE.nomeCliente.slice(0, -1);
                sessionStorage.setItem('pdv_nome_cliente', STATE.nomeCliente);
                atualizarDisplaysIdentificacao();
            }
        } else if (STATE.activeInputField === 'cpf') {
            const rawCpf = STATE.cpfCliente.replace(/\D/g, '');
            if (rawCpf.length > 0) {
                STATE.cpfCliente = formatarCpf(rawCpf.slice(0, -1));
                sessionStorage.setItem('pdv_cpf_cliente', STATE.cpfCliente);
                atualizarDisplaysIdentificacao();
            }
        }
    }

    function limparCampoAtivo() {
        if (STATE.activeInputField === 'nome') {
            STATE.nomeCliente = '';
            sessionStorage.removeItem('pdv_nome_cliente');
        } else if (STATE.activeInputField === 'cpf') {
            STATE.cpfCliente = '';
            sessionStorage.removeItem('pdv_cpf_cliente');
        }
        atualizarDisplaysIdentificacao();
    }

    function pularCpf() {
        STATE.cpfCliente = '';
        sessionStorage.removeItem('pdv_cpf_cliente');
        atualizarDisplaysIdentificacao();
        showToast('CPF Ignorado ⚡', 'Nenhum CPF será registrado na nota fiscal.');
    }

    // Ouvintes de cliques nos campos e teclados touch
    cardFieldNome?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('nome');
    });

    touchDisplayNome?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('nome');
    });

    cardFieldCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('cpf');
    });

    touchDisplayCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('cpf');
    });

    tabKbNome?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('nome');
    });

    tabKbCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('cpf');
    });

    btnKbNextCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        selecionarCampoIdentificacao('cpf');
    });

    btnClearNome?.addEventListener('click', (e) => {
        e.stopPropagation();
        STATE.nomeCliente = '';
        sessionStorage.removeItem('pdv_nome_cliente');
        atualizarDisplaysIdentificacao();
    });

    btnClearCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        STATE.cpfCliente = '';
        sessionStorage.removeItem('pdv_cpf_cliente');
        atualizarDisplaysIdentificacao();
    });

    btnSkipCpf?.addEventListener('click', (e) => {
        e.stopPropagation();
        pularCpf();
    });

    // Teclas do Teclado QWERTY
    keyboardQwerty?.querySelectorAll('.key-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const key = btn.getAttribute('data-key');
            const action = btn.getAttribute('data-action');

            if (key) {
                digitarNoTeclado(key);
            } else if (action === 'backspace') {
                apagarNoTeclado();
            } else if (action === 'clear') {
                limparCampoAtivo();
            }
        });
    });

    // Teclas do Teclado Numérico (Numpad)
    keyboardNumpad?.querySelectorAll('.num-key-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const num = btn.getAttribute('data-num');
            const action = btn.getAttribute('data-action');

            if (num) {
                digitarNoTeclado(num);
            } else if (action === 'num-backspace') {
                apagarNoTeclado();
            } else if (action === 'num-clear') {
                limparCampoAtivo();
            }
        });
    });

    // Botões de Ação da Tela de Identificação
    btnIdBackMenu?.addEventListener('click', (e) => {
        e.stopPropagation();
        transitionToScreen('menu');
    });

    btnIdSkipAll?.addEventListener('click', (e) => {
        e.stopPropagation();
        STATE.nomeCliente = '';
        STATE.cpfCliente = '';
        sessionStorage.removeItem('pdv_nome_cliente');
        sessionStorage.removeItem('pdv_cpf_cliente');
        avancarParaPagamento();
    });

    btnIdAdvancePayment?.addEventListener('click', (e) => {
        e.stopPropagation();
        const rawCpf = (STATE.cpfCliente || '').replace(/\D/g, '');
        if (rawCpf.length > 0) {
            if (rawCpf.length < 11 || !validarAlgoritmoCpf(rawCpf)) {
                showToast('CPF Inválido ⚠️', 'Por favor, digite um CPF válido ou clique em "Não desejo informar CPF".');
                selecionarCampoIdentificacao('cpf');
                return;
            }
        }
        avancarParaPagamento();
    });

    function avancarParaPagamento() {
        const payload = {
            nomeCliente: STATE.nomeCliente.trim() || null,
            cpfCliente: STATE.cpfCliente ? formatarCpf(STATE.cpfCliente) : null
        };

        // Salvar na sessão do backend
        fetch('/totem/identificacao', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).catch(err => console.warn('Erro ao registrar identificação no servidor:', err));

        const identificacaoMsg = payload.nomeCliente 
            ? `Identificação salva: ${payload.nomeCliente}!` 
            : 'Avançando com cliente não identificado!';

        showToast('Identificação Concluída! ✅', `${identificacaoMsg} Redirecionando para o Pagamento...`);
        console.log('Dados do cliente para o pedido:', payload);
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
        fecharCartDrawer();

        sessionStorage.removeItem('tipoConsumo');
        sessionStorage.removeItem('pdv_carrinho');
        sessionStorage.removeItem('pdv_nome_cliente');
        sessionStorage.removeItem('pdv_cpf_cliente');

        diningCards.forEach(c => c.classList.remove('selected'));
        if (diningFeedback) diningFeedback.classList.add('hidden');
        if (searchInput) searchInput.value = '';
        STATE.searchTerm = '';
        STATE.activeFilter = 'all';
        STATE.carrinho = [];
        STATE.nomeCliente = '';
        STATE.cpfCliente = '';
        STATE.activeInputField = 'nome';

        atualizarCarrinhoUI();
        atualizarDisplaysIdentificacao();

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

    // Inicialização da UI do Carrinho e Identificação
    atualizarCarrinhoUI();
    atualizarDisplaysIdentificacao();
});
