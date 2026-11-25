/**
 * Painel Operacional da Cozinha (KDS) - Antunes Lanches
 * Polling em tempo real, temporizador dinâmico de SLA e transição de status com 1 toque.
 */
(function () {
    'use strict';

    const POLL_INTERVAL_MS = 3000;
    const gridEl = document.getElementById('kds-grid');
    const emptyEl = document.getElementById('kds-empty');
    const syncStatusEl = document.getElementById('kds-sync-status');
    const syncTextEl = syncStatusEl ? syncStatusEl.querySelector('.kds-sync-text') : null;
    const clockEl = document.getElementById('kds-clock');
    const toastEl = document.getElementById('kds-toast');

    const countAtivosEl = document.getElementById('count-ativos');
    const countRecebidosEl = document.getElementById('count-recebidos');
    const countPreparoEl = document.getElementById('count-preparo');
    const countProntosEl = document.getElementById('count-prontos');

    const btnSound = document.getElementById('btn-kds-sound');
    const iconSoundOn = document.getElementById('icon-sound-on');
    const iconSoundOff = document.getElementById('icon-sound-off');
    const btnRefresh = document.getElementById('btn-kds-refresh');
    const btnFullscreen = document.getElementById('btn-kds-fullscreen');
    const filterButtons = document.querySelectorAll('.kds-filter-btn');

    let pedidosAtivos = [];
    let pedidosProntos = [];
    let filtroAtual = 'ATIVOS';
    let somAtivo = true;
    let idsConhecidos = null;
    let audioCtx = null;

    function atualizarRelogio() {
        if (!clockEl) return;
        const agora = new Date();
        clockEl.textContent = agora.toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    function tocarAlertaNovoPedido() {
        if (!somAtivo) return;
        try {
            const AudioContextClass = window.AudioContext || window.webkitAudioContext;
            if (!AudioContextClass) return;
            if (!audioCtx) {
                audioCtx = new AudioContextClass();
            }
            if (audioCtx.state === 'suspended') {
                audioCtx.resume();
            }

            const now = audioCtx.currentTime;
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(660, now);
            osc.frequency.setValueAtTime(880, now + 0.12);

            gain.gain.setValueAtTime(0.001, now);
            gain.gain.exponentialRampToValueAtTime(0.18, now + 0.03);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.35);

            osc.connect(gain);
            gain.connect(audioCtx.destination);
            osc.start(now);
            osc.stop(now + 0.36);
        } catch (e) {
            // Ignora bloqueio de autoplay até primeira interação
        }
    }

    function mostrarToast(mensagem, isError = false) {
        if (!toastEl) return;
        toastEl.textContent = mensagem;
        toastEl.classList.remove('hidden', 'error');
        if (isError) {
            toastEl.classList.add('error');
        }
        clearTimeout(toastEl._timer);
        toastEl._timer = setTimeout(() => {
            toastEl.classList.add('hidden');
        }, 3200);
    }

    function calcularSegundosEspera(dataHoraStr, fallbackMinutos) {
        if (dataHoraStr) {
            const inicio = new Date(dataHoraStr);
            if (!Number.isNaN(inicio.getTime())) {
                return Math.max(0, Math.floor((Date.now() - inicio.getTime()) / 1000));
            }
        }
        return Math.max(0, (fallbackMinutos || 0) * 60);
    }

    function calcularSla(segundos) {
        const minutos = Math.floor(segundos / 60);
        if (minutos < 5) return 'VERDE';
        if (minutos <= 10) return 'AMARELO';
        return 'VERMELHO';
    }

    function formatarCronometro(segundos) {
        const hrs = Math.floor(segundos / 3600);
        const mins = Math.floor((segundos % 3600) / 60);
        const secs = segundos % 60;
        const mm = String(mins).padStart(2, '0');
        const ss = String(secs).padStart(2, '0');
        return hrs > 0 ? `${String(hrs).padStart(2, '0')}:${mm}:${ss}` : `${mm}:${ss}`;
    }

    function escapeHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function renderizarTagsItem(item) {
        const tags = Array.isArray(item.tags) ? item.tags : [];
        if (tags.length === 0) return '';

        const htmlTags = tags.map(tag => {
            const upper = String(tag).toUpperCase();
            let cls = 'kds-tag-combo';
            if (upper.startsWith('SEM ')) {
                cls = 'kds-tag-remocao';
            } else if (upper.startsWith('+')) {
                cls = 'kds-tag-adicional';
            }
            return `<span class="kds-tag ${cls}">${escapeHtml(upper)}</span>`;
        }).join('');

        return `<div class="kds-tags">${htmlTags}</div>`;
    }

    function obterConfigAcao(status) {
        switch (status) {
            case 'RECEBIDO':
                return { label: 'Iniciar Preparo', cls: 'kds-btn-iniciar', next: 'EM_PREPARO' };
            case 'EM_PREPARO':
                return { label: 'Marcar como Pronto', cls: 'kds-btn-pronto', next: 'PRONTO' };
            case 'PRONTO':
                return { label: 'Marcar como Entregue', cls: 'kds-btn-entregue', next: 'ENTREGUE' };
            default:
                return null;
        }
    }

    function formatarStatusLabel(status) {
        switch (status) {
            case 'RECEBIDO': return 'Recebido';
            case 'EM_PREPARO': return 'Em Preparo';
            case 'PRONTO': return 'Pronto';
            default: return status;
        }
    }

    function renderizarCards() {
        const recebidos = pedidosAtivos.filter(p => p.status === 'RECEBIDO');
        const emPreparo = pedidosAtivos.filter(p => p.status === 'EM_PREPARO');

        if (countAtivosEl) countAtivosEl.textContent = pedidosAtivos.length;
        if (countRecebidosEl) countRecebidosEl.textContent = recebidos.length;
        if (countPreparoEl) countPreparoEl.textContent = emPreparo.length;
        if (countProntosEl) countProntosEl.textContent = pedidosProntos.length;

        let listaExibicao = pedidosAtivos;
        if (filtroAtual === 'RECEBIDO') {
            listaExibicao = recebidos;
        } else if (filtroAtual === 'EM_PREPARO') {
            listaExibicao = emPreparo;
        } else if (filtroAtual === 'PRONTO') {
            listaExibicao = pedidosProntos;
        }

        if (!listaExibicao || listaExibicao.length === 0) {
            gridEl.innerHTML = '';
            emptyEl.classList.remove('hidden');
            return;
        }

        emptyEl.classList.add('hidden');
        gridEl.innerHTML = listaExibicao.map(pedido => {
            const segundos = calcularSegundosEspera(pedido.dataHora, pedido.tempoEsperaMinutos);
            const sla = calcularSla(segundos);
            const senha = pedido.senhaFormatada || `#${String(pedido.numeroSenha || 0).padStart(3, '0')}`;
            const cliente = pedido.nomeCliente && pedido.nomeCliente.trim() ? pedido.nomeCliente : 'Cliente Balcão';
            const qtdItens = pedido.quantidadeItens || (pedido.itens ? pedido.itens.length : 0);
            const acao = obterConfigAcao(pedido.status);

            const itensHtml = (pedido.itens || []).map(item => `
                <li class="kds-item">
                    <div class="kds-item-main">
                        <span class="kds-item-qty">${item.quantidade}x</span>
                        <span class="kds-item-name">${escapeHtml(item.produtoNome)}</span>
                    </div>
                    ${renderizarTagsItem(item)}
                    ${item.observacoes ? `<div class="kds-item-obs">Obs: ${escapeHtml(item.observacoes)}</div>` : ''}
                </li>
            `).join('');

            return `
                <article class="kds-card sla-${sla}" data-pedido-id="${pedido.id}" data-hora="${escapeHtml(pedido.dataHora || '')}" data-fallback-min="${pedido.tempoEsperaMinutos || 0}">
                    <div class="kds-card-header">
                        <div class="kds-senha-group">
                            <span class="kds-senha">${escapeHtml(senha)}</span>
                            <span class="kds-status-badge kds-status-${pedido.status}">${formatarStatusLabel(pedido.status)}</span>
                        </div>
                        <div class="kds-timer sla-${sla}" data-timer>
                            ⏱ <span>${formatarCronometro(segundos)}</span>
                        </div>
                    </div>
                    <div class="kds-card-meta">
                        <span class="kds-cliente">${escapeHtml(cliente)}</span>
                        <span>${qtdItens} ${qtdItens === 1 ? 'item' : 'itens'} ${pedido.pago ? '• Pago' : '• Aguardando Pgto'}</span>
                    </div>
                    <ul class="kds-items">
                        ${itensHtml}
                    </ul>
                    ${acao ? `
                        <div class="kds-card-footer">
                            <button type="button" class="kds-action-btn ${acao.cls}" data-action-id="${pedido.id}" data-next-status="${acao.next}">
                                ${acao.label}
                            </button>
                        </div>
                    ` : ''}
                </article>
            `;
        }).join('');
    }

    function atualizarCronometrosLocais() {
        const cards = gridEl.querySelectorAll('.kds-card');
        cards.forEach(card => {
            const dataHora = card.getAttribute('data-hora');
            const fallbackMin = Number(card.getAttribute('data-fallback-min') || 0);
            const segundos = calcularSegundosEspera(dataHora, fallbackMin);
            const sla = calcularSla(segundos);

            const timerEl = card.querySelector('[data-timer]');
            const timerSpan = timerEl ? timerEl.querySelector('span') : null;
            if (timerSpan) {
                timerSpan.textContent = formatarCronometro(segundos);
            }

            card.classList.remove('sla-VERDE', 'sla-AMARELO', 'sla-VERMELHO');
            card.classList.add(`sla-${sla}`);
            if (timerEl) {
                timerEl.classList.remove('sla-VERDE', 'sla-AMARELO', 'sla-VERMELHO');
                timerEl.classList.add(`sla-${sla}`);
            }
        });
    }

    async function carregarPedidos() {
        try {
            const [resAtivos, resProntos] = await Promise.all([
                fetch('/api/kds/pedidos-ativos', { headers: { 'Accept': 'application/json' } }),
                fetch('/api/kds/pedidos-prontos', { headers: { 'Accept': 'application/json' } })
            ]);

            if (!resAtivos.ok || !resProntos.ok) {
                throw new Error('Falha ao consultar API do KDS');
            }

            const novosAtivos = await resAtivos.json();
            const novosProntos = await resProntos.json();

            const novosIds = new Set(novosAtivos.map(p => p.id));
            if (idsConhecidos !== null) {
                const temNovo = novosAtivos.some(p => !idsConhecidos.has(p.id));
                if (temNovo) {
                    tocarAlertaNovoPedido();
                }
            }
            idsConhecidos = novosIds;

            pedidosAtivos = novosAtivos;
            pedidosProntos = novosProntos;

            if (syncStatusEl && syncTextEl) {
                syncStatusEl.classList.remove('offline');
                syncTextEl.textContent = 'Ao Vivo';
            }

            renderizarCards();
        } catch (err) {
            if (syncStatusEl && syncTextEl) {
                syncStatusEl.classList.add('offline');
                syncTextEl.textContent = 'Reconectando...';
            }
        }
    }

    async function avancarStatusPedido(pedidoId, nextStatus, buttonEl) {
        if (buttonEl) {
            buttonEl.disabled = true;
            buttonEl.textContent = 'Atualizando...';
        }

        try {
            const response = await fetch(`/api/kds/pedidos/${pedidoId}/status`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(nextStatus ? { status: nextStatus } : {})
            });

            if (!response.ok) {
                let msg = 'Não foi possível atualizar o status do pedido.';
                try {
                    const errBody = await response.json();
                    if (errBody && errBody.detail) msg = errBody.detail;
                    else if (errBody && errBody.message) msg = errBody.message;
                } catch (_) { /* ignore */ }
                throw new Error(msg);
            }

            const atualizado = await response.json();
            const senha = atualizado.senhaFormatada || `#${atualizado.id}`;
            mostrarToast(`Pedido ${senha} atualizado para ${formatarStatusLabel(atualizado.status)}!`);
            await carregarPedidos();
        } catch (err) {
            mostrarToast(err.message || 'Erro ao atualizar pedido.', true);
            if (buttonEl) {
                buttonEl.disabled = false;
            }
            await carregarPedidos();
        }
    }

    // Delegação de clique nos botões de 1 toque
    gridEl.addEventListener('click', (event) => {
        const btn = event.target.closest('.kds-action-btn');
        if (!btn || btn.disabled) return;
        const pedidoId = btn.getAttribute('data-action-id');
        const nextStatus = btn.getAttribute('data-next-status');
        if (pedidoId) {
            avancarStatusPedido(pedidoId, nextStatus, btn);
        }
    });

    // Filtros
    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            filterButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            filtroAtual = btn.getAttribute('data-filter') || 'ATIVOS';
            renderizarCards();
        });
    });

    // Som
    if (btnSound) {
        btnSound.addEventListener('click', () => {
            somAtivo = !somAtivo;
            btnSound.classList.toggle('muted', !somAtivo);
            iconSoundOn.classList.toggle('hidden', !somAtivo);
            iconSoundOff.classList.toggle('hidden', somAtivo);
            if (somAtivo) {
                tocarAlertaNovoPedido();
            }
        });
    }

    // Atualizar manualmente
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            carregarPedidos();
        });
    }

    // Fullscreen
    if (btnFullscreen) {
        btnFullscreen.addEventListener('click', () => {
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(() => {});
            } else {
                document.exitFullscreen().catch(() => {});
            }
        });
    }

    // Inicialização
    atualizarRelogio();
    setInterval(atualizarRelogio, 1000);
    setInterval(atualizarCronometrosLocais, 1000);
    carregarPedidos();
    setInterval(carregarPedidos, POLL_INTERVAL_MS);
})();
