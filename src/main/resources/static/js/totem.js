/**
 * ==============================================================================
 * Antunes Lanches - Totem de Autoatendimento (Vanilla JS Puro)
 * Funcionalidades: Relógio, Carrossel Rotativo, Transições,
 * Escolha de Consumo (Sessão) e Temporizador de Inatividade (60s).
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
    const btnStartOrder = document.getElementById('btn-start-order');
    const btnRestart = document.getElementById('btn-restart');
    const btnHelp = document.getElementById('btn-help');
    
    // Carrossel
    const promoSlides = document.querySelectorAll('.promo-slide');
    const promoDots = document.querySelectorAll('.carousel-dot');
    
    // Cards de Consumo
    const diningCards = document.querySelectorAll('.dining-card');
    const diningFeedback = document.getElementById('dining-feedback');

    // Modais
    const inactivityModal = document.getElementById('inactivity-modal');
    const inactivityCountdown = document.getElementById('inactivity-countdown');
    const btnKeepSession = document.getElementById('btn-keep-session');
    const btnCancelSession = document.getElementById('btn-cancel-session');
    
    const helpModal = document.getElementById('help-modal');
    const btnCloseHelp = document.getElementById('btn-close-help');
    const btnCallAttendant = document.getElementById('btn-call-attendant');

    // --------------------------------------------------------------------------
    // 2. ESTADO DA APLICAÇÃO
    // --------------------------------------------------------------------------
    const STATE = {
        currentSlide: 0,
        carouselInterval: null,
        inactivityTimeout: 60, // 60 segundos totais
        warningWindow: 10,     // Últimos 10 segundos para aviso
        idleTimer: null,
        countdownInterval: null,
        remainingSeconds: 60,
        isActiveOrder: false
    };

    // --------------------------------------------------------------------------
    // 3. RELÓGIO DIGITAL AO VIVO (HH:mm)
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
    // 4. CARROSSEL DE OFERTAS ROTATIVO
    // --------------------------------------------------------------------------
    function showSlide(index) {
        if (!promoSlides.length) return;
        
        STATE.currentSlide = (index + promoSlides.length) % promoSlides.length;

        promoSlides.forEach((slide, i) => {
            if (i === STATE.currentSlide) {
                slide.classList.add('active');
            } else {
                slide.classList.remove('active');
            }
        });

        promoDots.forEach((dot, i) => {
            if (i === STATE.currentSlide) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
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
    // 5. TRANSIÇÃO: BOAS-VINDAS -> MODALIDADE DE CONSUMO
    // --------------------------------------------------------------------------
    function startOrderFlow() {
        if (STATE.isActiveOrder) return;
        STATE.isActiveOrder = true;
        stopCarousel();

        screenWelcome.classList.add('hidden');
        screenDining.classList.remove('hidden');
        btnRestart.style.display = 'inline-flex';

        resetInactivityTimer();
    }

    if (btnStartOrder) {
        btnStartOrder.addEventListener('click', (e) => {
            e.stopPropagation();
            startOrderFlow();
        });
    }

    if (screenWelcome) {
        screenWelcome.addEventListener('click', () => {
            startOrderFlow();
        });
    }

    // --------------------------------------------------------------------------
    // 6. ESCOLHA DA MODALIDADE DE CONSUMO (Para Comer Aqui vs Para Viagem)
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
        // Feedback visual
        diningCards.forEach(c => c.classList.remove('selected'));
        selectedCard.classList.add('selected');

        // Salvar na sessão local (SessionStorage)
        sessionStorage.setItem('tipoConsumo', modalidade);

        // Feedback de confirmação
        if (diningFeedback) {
            diningFeedback.classList.remove('hidden');
            const feedbackText = modalidade === 'COMER_AQUI' 
                ? 'Opção: Para Comer Aqui! Redirecionando para o cardápio...' 
                : 'Opção: Para Viagem! Redirecionando para o cardápio...';
            diningFeedback.querySelector('.badge').textContent = feedbackText;
        }

        // Salvar na sessão do servidor Spring Boot
        try {
            await fetch('/totem/modalidade', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ tipoConsumo: modalidade })
            });
        } catch (err) {
            console.warn('Registro local gravado com sucesso. Servidor offline ou rota simulada:', err);
        }

        // Simulação de transição suave para a próxima tela (Cardápio - CARD-12)
        setTimeout(() => {
            // Se o endpoint do cardápio existir futuramente, redirecionaria para /totem/cardapio
            // Por ora, mantém estado confirmado e exibe aviso amigável
            if (diningFeedback) {
                diningFeedback.querySelector('.badge').textContent = 'Modalidade confirmada! Em breve: Cardápio Digital (CARD-12).';
            }
        }, 1200);
    }

    // --------------------------------------------------------------------------
    // 7. TEMPORIZADOR DE INATIVIDADE (60 Segundos com Contagem nos últimos 10s)
    // --------------------------------------------------------------------------
    function resetInactivityTimer() {
        if (!STATE.isActiveOrder) return;

        // Limpa timers anteriores
        if (STATE.idleTimer) clearTimeout(STATE.idleTimer);
        if (STATE.countdownInterval) clearInterval(STATE.countdownInterval);

        closeInactivityModal();
        STATE.remainingSeconds = STATE.inactivityTimeout;

        // Dispara aviso aos 50 segundos de inatividade (10 segundos restantes)
        const timeUntilWarning = (STATE.inactivityTimeout - STATE.warningWindow) * 1000;
        STATE.idleTimer = setTimeout(() => {
            showInactivityWarning();
        }, timeUntilWarning);
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

    // Eventos de atividade do usuário no totem
    const activityEvents = ['touchstart', 'touchmove', 'mousedown', 'mousemove', 'keydown', 'scroll', 'click'];
    activityEvents.forEach(eventType => {
        window.addEventListener(eventType, () => {
            // Se o modal de aviso não estiver aberto, qualquer toque reinicia a contagem
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
    // 8. RESET TOTAL DO TOTEM (Recomeçar Atendimento)
    // --------------------------------------------------------------------------
    function resetTotemToWelcome() {
        STATE.isActiveOrder = false;
        if (STATE.idleTimer) clearTimeout(STATE.idleTimer);
        if (STATE.countdownInterval) clearInterval(STATE.countdownInterval);

        closeInactivityModal();
        closeHelpModal();

        // Limpar storage da sessão
        sessionStorage.removeItem('tipoConsumo');

        // Limpar seleções visuais
        diningCards.forEach(c => c.classList.remove('selected'));
        if (diningFeedback) diningFeedback.classList.add('hidden');

        // Alternar telas
        screenDining.classList.add('hidden');
        screenWelcome.classList.remove('hidden');
        btnRestart.style.display = 'none';

        // Notificar backend de limpeza de sessão
        fetch('/totem/reset', { method: 'POST' }).catch(() => {});

        // Reiniciar carrossel
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
    // 9. MODAL DE AJUDA
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
