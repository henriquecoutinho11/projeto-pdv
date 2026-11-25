/**
 * Painel Público de Chamada de Senhas (TV do Salão) - Antunes Lanches
 * Polling em tempo real, efeito visual pulsante e som "Ding-dong" ao marcar pedido como PRONTO.
 */
(function () {
    'use strict';

    const POLL_INTERVAL_MS = 2500;

    const listPreparoEl = document.getElementById('tv-list-preparo');
    const listProntoEl = document.getElementById('tv-list-pronto');
    const emptyPreparoEl = document.getElementById('tv-empty-preparo');
    const emptyProntoEl = document.getElementById('tv-empty-pronto');
    const countPreparoEl = document.getElementById('tv-count-preparo');
    const countProntoEl = document.getElementById('tv-count-pronto');

    const destaqueEl = document.getElementById('tv-chamada-destaque');
    const destaqueSenhaEl = document.getElementById('tv-destaque-senha');
    const destaqueClienteEl = document.getElementById('tv-destaque-cliente');

    const syncStatusEl = document.getElementById('tv-sync-status');
    const syncTextEl = syncStatusEl ? syncStatusEl.querySelector('.tv-sync-text') : null;
    const dateEl = document.getElementById('tv-date');
    const clockEl = document.getElementById('tv-clock');

    const btnSound = document.getElementById('btn-tv-sound');
    const soundLabelEl = document.getElementById('tv-sound-label');
    const iconSoundOn = document.getElementById('tv-icon-sound-on');
    const iconSoundOff = document.getElementById('tv-icon-sound-off');
    const btnFullscreen = document.getElementById('btn-tv-fullscreen');

    let somAtivo = true;
    let prontosConhecidos = null;
    let ultimoDestaqueId = null;
    let audioCtx = null;

    function atualizarDataHora() {
        const agora = new Date();
        if (clockEl) {
            clockEl.textContent = agora.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }
        if (dateEl) {
            dateEl.textContent = agora.toLocaleDateString('pt-BR', {
                weekday: 'long',
                day: '2-digit',
                month: 'long',
                year: 'numeric'
            });
        }
    }

    /**
     * Reproduz um som suave "Ding-dong" via Web Audio API sem depender de arquivos externos.
     */
    function tocarDingDong() {
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

            // Tom 1: Ding (880 Hz)
            const osc1 = audioCtx.createOscillator();
            const gain1 = audioCtx.createGain();
            osc1.type = 'sine';
            osc1.frequency.setValueAtTime(880, now);
            gain1.gain.setValueAtTime(0.001, now);
            gain1.gain.exponentialRampToValueAtTime(0.25, now + 0.03);
            gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
            osc1.connect(gain1);
            gain1.connect(audioCtx.destination);
            osc1.start(now);
            osc1.stop(now + 0.46);

            // Tom 2: Dong (659.25 Hz)
            const osc2 = audioCtx.createOscillator();
            const gain2 = audioCtx.createGain();
            osc2.type = 'sine';
            osc2.frequency.setValueAtTime(659.25, now + 0.25);
            gain2.gain.setValueAtTime(0.001, now + 0.25);
            gain2.gain.exponentialRampToValueAtTime(0.25, now + 0.28);
            gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.85);
            osc2.connect(gain2);
            gain2.connect(audioCtx.destination);
            osc2.start(now + 0.25);
            osc2.stop(now + 0.86);
        } catch (e) {
            // Ignora erro caso o navegador exija interação prévia do usuário
        }
    }

    function escapeHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatarSenha(pedido) {
        if (pedido.senhaFormatada) return pedido.senhaFormatada;
        const num = pedido.numeroSenha != null ? pedido.numeroSenha : pedido.id || 0;
        return `#${String(num).padStart(3, '0')}`;
    }

    function renderizarPainel(emPreparo, prontos, novosProntosIds) {
        if (countPreparoEl) countPreparoEl.textContent = emPreparo.length;
        if (countProntoEl) countProntoEl.textContent = prontos.length;

        // Coluna Em Preparo
        if (emPreparo.length === 0) {
            listPreparoEl.innerHTML = '';
            emptyPreparoEl.classList.remove('hidden');
        } else {
            emptyPreparoEl.classList.add('hidden');
            listPreparoEl.innerHTML = emPreparo.map(p => {
                const senha = formatarSenha(p);
                const nome = p.nomeCliente && p.nomeCliente.trim() ? p.nomeCliente : 'Pedido';
                return `
                    <div class="tv-card-preparo">
                        <div class="tv-senha-num">${escapeHtml(senha)}</div>
                        <div class="tv-senha-nome">${escapeHtml(nome)}</div>
                    </div>
                `;
            }).join('');
        }

        // Coluna Pronto Para Retirada
        if (prontos.length === 0) {
            destaqueEl.classList.add('hidden');
            listProntoEl.innerHTML = '';
            emptyProntoEl.classList.remove('hidden');
            ultimoDestaqueId = null;
        } else {
            emptyProntoEl.classList.add('hidden');

            const principal = prontos[0];
            destaqueEl.classList.remove('hidden');
            destaqueSenhaEl.textContent = formatarSenha(principal);
            destaqueClienteEl.textContent = principal.nomeCliente && principal.nomeCliente.trim()
                ? principal.nomeCliente
                : 'Retire seu lanche';

            if (novosProntosIds.size > 0 || ultimoDestaqueId !== principal.id) {
                destaqueEl.classList.remove('pulsar');
                void destaqueEl.offsetWidth; // Reflow para reiniciar animação CSS
                destaqueEl.classList.add('pulsar');
                ultimoDestaqueId = principal.id;
            }

            listProntoEl.innerHTML = prontos.map(p => {
                const senha = formatarSenha(p);
                const nome = p.nomeCliente && p.nomeCliente.trim() ? p.nomeCliente : 'Retirar no Balcão';
                const isNovo = novosProntosIds.has(p.id);
                return `
                    <div class="tv-card-pronto ${isNovo ? 'novo-pronto' : ''}">
                        <div class="tv-senha-num">${escapeHtml(senha)}</div>
                        <div class="tv-senha-nome">${escapeHtml(nome)}</div>
                    </div>
                `;
            }).join('');
        }
    }

    async function atualizarSenhas() {
        try {
            const response = await fetch('/api/painel-senhas', {
                headers: { 'Accept': 'application/json' }
            });
            if (!response.ok) {
                throw new Error('Falha ao carregar senhas');
            }

            const data = await response.json();
            const emPreparo = Array.isArray(data.emPreparo) ? data.emPreparo : [];
            const prontos = Array.isArray(data.prontos) ? data.prontos : [];

            const idsAtuaisProntos = new Set(prontos.map(p => p.id));
            const novosProntosIds = new Set();

            if (prontosConhecidos !== null) {
                prontos.forEach(p => {
                    if (!prontosConhecidos.has(p.id)) {
                        novosProntosIds.add(p.id);
                    }
                });
                if (novosProntosIds.size > 0) {
                    tocarDingDong();
                }
            }
            prontosConhecidos = idsAtuaisProntos;

            if (syncStatusEl && syncTextEl) {
                syncStatusEl.classList.remove('offline');
                syncTextEl.textContent = 'Ao Vivo';
            }

            renderizarPainel(emPreparo, prontos, novosProntosIds);
        } catch (err) {
            if (syncStatusEl && syncTextEl) {
                syncStatusEl.classList.add('offline');
                syncTextEl.textContent = 'Reconectando...';
            }
        }
    }

    if (btnSound) {
        btnSound.addEventListener('click', () => {
            somAtivo = !somAtivo;
            btnSound.classList.toggle('muted', !somAtivo);
            iconSoundOn.classList.toggle('hidden', !somAtivo);
            iconSoundOff.classList.toggle('hidden', somAtivo);
            if (soundLabelEl) {
                soundLabelEl.textContent = somAtivo ? 'Som Ativo' : 'Som Mudo';
            }
            if (somAtivo) {
                tocarDingDong();
            }
        });
    }

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
    atualizarDataHora();
    setInterval(atualizarDataHora, 1000);
    atualizarSenhas();
    setInterval(atualizarSenhas, POLL_INTERVAL_MS);
})();
