/**
 * Painel Administrativo - Antunes Lanches (CARD-16 / Frontend 006)
 * Gestão de Cardápio, Controle de Estoque & Dashboard Analítico
 * Integrado com as APIs REST do Spring Boot e Sincronização Instantânea com o Totem.
 */
(function () {
    'use strict';

    // Canal de Sincronização em Tempo Real (BroadcastChannel entre abas/telas)
    const syncChannel = typeof BroadcastChannel !== 'undefined' ? new BroadcastChannel('antunes-pdv-sync') : null;

    // Estado da Aplicação
    const state = {
        currentTab: 'dashboard',
        currentPeriod: 'hoje',
        customStartDate: null,
        customEndDate: null,
        produtos: [],
        categorias: [],
        filtroTexto: '',
        filtroCategoria: 'ALL',
        filtroStatus: 'ALL',
        resumo: null,
        fechamento: null
    };

    // Cores para Gráficos (Design System Fast-Food Moderno)
    const PAYMENT_COLORS = {
        'PIX': '#00BFA5',
        'CARTAO_CREDITO': '#FFC107',
        'CARTAO_DEBITO': '#29B6F6',
        'DINHEIRO': '#81C784',
        'OUTROS': '#AB47BC'
    };

    // --------------------------------------------------------------------------
    // 1. INICIALIZAÇÃO
    // --------------------------------------------------------------------------
    document.addEventListener('DOMContentLoaded', () => {
        initClock();
        initTabs();
        initDashboard();
        initCatalog();
        initFechamento();
        initSyncChannel();

        // Carregar dados iniciais do servidor
        carregarCategorias();
        carregarProdutos();
        carregarMetricasDashboard();
    });

    // --------------------------------------------------------------------------
    // 2. RELÓGIO E STATUS
    // --------------------------------------------------------------------------
    function initClock() {
        const clockEl = document.getElementById('admin-clock');
        if (!clockEl) return;

        function update() {
            const now = new Date();
            clockEl.textContent = now.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }
        update();
        setInterval(update, 1000);
    }

    function initSyncChannel() {
        if (!syncChannel) return;
        syncChannel.onmessage = (event) => {
            const data = event.data;
            if (data && (data.type === 'ORDER_CREATED' || data.type === 'ORDER_STATUS_CHANGED')) {
                // Novo pedido ou alteração de status na cozinha: atualizar dashboard silenciosamente
                carregarMetricasDashboard(true);
            }
        };
    }

    function notificarAlteracaoTotem(tipo, idProduto) {
        if (syncChannel) {
            syncChannel.postMessage({
                type: 'PRODUCT_CATALOG_CHANGED',
                action: tipo,
                produtoId: idProduto,
                timestamp: Date.now()
            });
        }
    }

    // --------------------------------------------------------------------------
    // 3. NAVEGAÇÃO DE ABAS
    // --------------------------------------------------------------------------
    function initTabs() {
        const tabButtons = document.querySelectorAll('.admin-tab-btn');
        tabButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTab = btn.getAttribute('data-tab');
                switchTab(targetTab);
            });
        });
    }

    function switchTab(tabName) {
        state.currentTab = tabName;

        document.querySelectorAll('.admin-tab-btn').forEach(b => {
            b.classList.toggle('active', b.getAttribute('data-tab') === tabName);
        });

        document.querySelectorAll('.admin-tab-section').forEach(sec => {
            sec.classList.toggle('active', sec.id === `tab-${tabName}`);
        });

        if (tabName === 'dashboard') {
            carregarMetricasDashboard();
        } else if (tabName === 'cardapio') {
            carregarProdutos();
        } else if (tabName === 'fechamento') {
            consultarFechamentoHoje();
        }
    }

    // --------------------------------------------------------------------------
    // 4. DASHBOARD ANALÍTICO & MÉTRICAS
    // --------------------------------------------------------------------------
    function initDashboard() {
        const periodBtns = document.querySelectorAll('.period-btn');
        const customBox = document.getElementById('custom-date-box');
        const btnApplyCustom = document.getElementById('btn-apply-custom-date');
        const btnRefresh = document.getElementById('btn-refresh-dashboard');

        periodBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const period = btn.getAttribute('data-period');
                periodBtns.forEach(b => b.classList.toggle('active', b === btn));

                if (period === 'custom') {
                    if (customBox) customBox.classList.remove('hidden');
                } else {
                    if (customBox) customBox.classList.add('hidden');
                    state.currentPeriod = period;
                    carregarMetricasDashboard();
                }
            });
        });

        if (btnApplyCustom) {
            btnApplyCustom.addEventListener('click', () => {
                const start = document.getElementById('input-date-start')?.value;
                const end = document.getElementById('input-date-end')?.value;

                if (!start || !end) {
                    showToast('Data Inválida', 'Selecione a data de início e de fim.', 'warning');
                    return;
                }
                if (start > end) {
                    showToast('Período Inválido', 'A data inicial não pode ser maior que a final.', 'warning');
                    return;
                }

                state.currentPeriod = 'custom';
                state.customStartDate = `${start}T00:00:00`;
                state.customEndDate = `${end}T23:59:59`;
                carregarMetricasDashboard();
            });
        }

        if (btnRefresh) {
            btnRefresh.addEventListener('click', () => {
                carregarMetricasDashboard();
                showToast('Atualizado', 'Métricas e faturamento recalculados.', 'success');
            });
        }
    }

    function obterParametrosPeriodo() {
        const hoje = new Date();
        const formatDate = (d) => d.toISOString().split('T')[0];

        if (state.currentPeriod === 'hoje') {
            const dataStr = formatDate(hoje);
            return `inicio=${dataStr}T00:00:00&fim=${dataStr}T23:59:59`;
        } else if (state.currentPeriod === '7dias') {
            const seteDiasAtras = new Date();
            seteDiasAtras.setDate(hoje.getDate() - 7);
            return `inicio=${formatDate(seteDiasAtras)}T00:00:00&fim=${formatDate(hoje)}T23:59:59`;
        } else if (state.currentPeriod === 'mes') {
            const primeiroDia = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
            return `inicio=${formatDate(primeiroDia)}T00:00:00&fim=${formatDate(hoje)}T23:59:59`;
        } else if (state.currentPeriod === 'custom' && state.customStartDate && state.customEndDate) {
            return `inicio=${state.customStartDate}&fim=${state.customEndDate}`;
        }
        return '';
    }

    async function carregarMetricasDashboard(silencioso = false) {
        const params = obterParametrosPeriodo();
        const url = `/api/admin/relatorios/resumo${params ? '?' + params : ''}`;

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Falha ao carregar resumo de vendas');

            const data = await response.json();
            state.resumo = data;
            renderizarDashboard(data);

            const lastUpdateEl = document.getElementById('dashboard-last-update');
            if (lastUpdateEl) {
                lastUpdateEl.textContent = `Atualizado às ${new Date().toLocaleTimeString('pt-BR')}`;
            }
        } catch (err) {
            console.error('Erro ao carregar métricas:', err);
            if (!silencioso) {
                showToast('Erro', 'Não foi possível carregar as métricas analíticas.', 'error');
            }
        }
    }

    function renderizarDashboard(resumo) {
        // 1. Atualizar KPIs do topo
        const kpiFaturamento = document.getElementById('kpi-faturamento');
        const kpiPedidos = document.getElementById('kpi-pedidos');
        const kpiTempo = document.getElementById('kpi-tempo');
        const kpiTicket = document.getElementById('kpi-ticket');

        if (kpiFaturamento) {
            kpiFaturamento.textContent = formatarMoeda(resumo.totalFaturado || 0);
        }
        if (kpiPedidos) {
            kpiPedidos.textContent = resumo.totalPedidos || 0;
        }
        if (kpiTempo) {
            const minutos = resumo.tempoMedioPreparoMinutos != null ? resumo.tempoMedioPreparoMinutos.toFixed(1) : '0,0';
            kpiTempo.textContent = `${minutos.replace('.', ',')} min`;
        }
        if (kpiTicket) {
            kpiTicket.textContent = formatarMoeda(resumo.ticketMedio || 0);
        }

        // 2. Gráfico de Rosca de Formas de Pagamento
        renderizarGraficoPagamentos(resumo.vendasPorFormaPagamento || [], resumo.totalFaturado || 0);

        // 3. Gráfico de Barras: Top 5 Hamburgueres mais vendidos
        renderizarTopProdutos(resumo.topProdutosMaisVendidos || []);

        // 4. Tabela de Detalhamento de Pagamentos
        renderizarTabelaPagamentos(resumo.vendasPorFormaPagamento || [], resumo.totalFaturado || 0);
    }

    // Renderização do Donut Chart via Canvas 2D
    function renderizarGraficoPagamentos(formas, totalFaturado) {
        const canvas = document.getElementById('paymentDonutCanvas');
        const centerValEl = document.getElementById('donut-center-total');
        const legendEl = document.getElementById('payment-legend');

        if (centerValEl) {
            centerValEl.textContent = `R$ ${formatarMoeda(totalFaturado)}`;
        }

        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        const centerX = width / 2;
        const centerY = height / 2;
        const radius = Math.min(centerX, centerY) - 16;
        const innerRadius = radius * 0.65;

        ctx.clearRect(0, 0, width, height);

        if (!formas || formas.length === 0 || totalFaturado <= 0) {
            // Desenhar círculo cinza vazio
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
            ctx.arc(centerX, centerY, innerRadius, 2 * Math.PI, 0, true);
            ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
            ctx.fill();

            if (legendEl) {
                legendEl.innerHTML = '<div class="legend-empty-state">Nenhum pagamento registrado no período.</div>';
            }
            return;
        }

        let startAngle = -Math.PI / 2;
        let legendHtml = '';

        formas.forEach(forma => {
            const pct = (forma.percentual != null ? forma.percentual : 0) / 100;
            const sliceAngle = pct * 2 * Math.PI;
            const endAngle = startAngle + sliceAngle;
            const color = PAYMENT_COLORS[forma.formaPagamento] || '#FFC107';

            // Desenhar fatia do anel
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, startAngle, endAngle);
            ctx.arc(centerX, centerY, innerRadius, endAngle, startAngle, true);
            ctx.closePath();
            ctx.fillStyle = color;
            ctx.fill();

            // Adicionar borda sutil entre fatias
            ctx.lineWidth = 2;
            ctx.strokeStyle = '#1E1E1E';
            ctx.stroke();

            startAngle = endAngle;

            // Montar legenda
            const valorFormatado = formatarMoeda(forma.totalFaturado || 0);
            const percentualFormatado = (forma.percentual || 0).toFixed(1).replace('.', ',');
            const nomeDescricao = formatarNomePagamento(forma.descricao || forma.formaPagamento);

            legendHtml += `
                <div class="legend-item">
                    <div class="legend-item-left">
                        <span class="legend-color-dot" style="background-color: ${color};"></span>
                        <div>
                            <div class="legend-name">${nomeDescricao}</div>
                            <div class="legend-sub">${forma.quantidadePedidos || 0} pedidos</div>
                        </div>
                    </div>
                    <div class="legend-item-right">
                        <div class="legend-amount">R$ ${valorFormatado}</div>
                        <div class="legend-pct">${percentualFormatado}%</div>
                    </div>
                </div>
            `;
        });

        if (legendEl) {
            legendEl.innerHTML = legendHtml;
        }
    }

    // Renderização do Top 5 mais vendidos com barras horizontais proporcionais
    function renderizarTopProdutos(produtos) {
        const container = document.getElementById('top-products-list');
        if (!container) return;

        if (!produtos || produtos.length === 0) {
            container.innerHTML = `
                <div class="top-products-empty">
                    <p style="color: var(--color-text-muted); font-size: 0.9rem; text-align: center; padding: 2rem 0;">
                        Nenhuma venda de produto registrada no período selecionado.
                    </p>
                </div>
            `;
            return;
        }

        const maxQtd = Math.max(...produtos.map(p => p.quantidadeVendida || 1), 1);

        let html = '';
        produtos.slice(0, 5).forEach((item, index) => {
            const rank = index + 1;
            const percentWidth = Math.max(12, Math.round(((item.quantidadeVendida || 0) / maxQtd) * 100));
            const valorTotalStr = formatarMoeda(item.valorTotal || 0);

            html += `
                <div class="top-product-item">
                    <div class="top-prod-header">
                        <div class="top-prod-title">
                            <span class="top-prod-rank ${rank === 1 ? 'rank-1' : ''}">${rank}</span>
                            <span>${escapeHtml(item.nomeProduto)}</span>
                        </div>
                        <div class="top-prod-stats">
                            <span class="top-prod-qty">${item.quantidadeVendida} un</span>
                            <span class="top-prod-total">R$ ${valorTotalStr}</span>
                        </div>
                    </div>
                    <div class="top-prod-bar-bg">
                        <div class="top-prod-bar-fill" style="width: ${percentWidth}%;"></div>
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    function renderizarTabelaPagamentos(formas, totalFaturado) {
        const tbody = document.getElementById('tbody-pagamentos-resumo');
        if (!tbody) return;

        if (!formas || formas.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4" style="color: var(--color-text-muted);">Nenhum pagamento registrado no período.</td></tr>`;
            return;
        }

        let html = '';
        formas.forEach(forma => {
            const color = PAYMENT_COLORS[forma.formaPagamento] || '#FFC107';
            const pct = (forma.percentual || 0).toFixed(1).replace('.', ',');
            const valorStr = formatarMoeda(forma.totalFaturado || 0);
            const nomeDesc = formatarNomePagamento(forma.descricao || forma.formaPagamento);

            html += `
                <tr>
                    <td>
                        <span style="display: inline-flex; align-items: center; gap: 0.5rem; font-weight: 600;">
                            <span style="width: 10px; height: 10px; border-radius: 50%; background-color: ${color};"></span>
                            ${nomeDesc}
                        </span>
                    </td>
                    <td class="text-center font-bold">${forma.quantidadePedidos || 0}</td>
                    <td class="text-right font-bold text-gold">R$ ${valorStr}</td>
                    <td class="text-right font-bold">${pct}%</td>
                    <td style="width: 180px;">
                        <div style="background: rgba(255,255,255,0.06); height: 8px; border-radius: 4px; overflow: hidden;">
                            <div style="background: ${color}; width: ${forma.percentual || 0}%; height: 100%;"></div>
                        </div>
                    </td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    }

    // --------------------------------------------------------------------------
    // 5. GESTÃO DE CARDÁPIO E ESTOQUE
    // --------------------------------------------------------------------------
    function initCatalog() {
        const inputSearch = document.getElementById('admin-search-products');
        const btnClearSearch = document.getElementById('btn-clear-product-search');
        const selectCat = document.getElementById('filter-categoria');
        const selectStatus = document.getElementById('filter-status');
        const btnRefresh = document.getElementById('btn-refresh-products');
        const btnResetFilters = document.getElementById('btn-reset-filters');
        const btnOpenCreate = document.getElementById('btn-open-create-product');

        // Busca com debounce
        if (inputSearch) {
            inputSearch.addEventListener('input', (e) => {
                state.filtroTexto = e.target.value.trim().toLowerCase();
                if (btnClearSearch) {
                    btnClearSearch.classList.toggle('hidden', state.filtroTexto === '');
                }
                aplicarFiltrosTabela();
            });
        }

        if (btnClearSearch) {
            btnClearSearch.addEventListener('click', () => {
                if (inputSearch) inputSearch.value = '';
                state.filtroTexto = '';
                btnClearSearch.classList.add('hidden');
                aplicarFiltrosTabela();
            });
        }

        if (selectCat) {
            selectCat.addEventListener('change', (e) => {
                state.filtroCategoria = e.target.value;
                aplicarFiltrosTabela();
            });
        }

        if (selectStatus) {
            selectStatus.addEventListener('change', (e) => {
                state.filtroStatus = e.target.value;
                aplicarFiltrosTabela();
            });
        }

        if (btnRefresh) {
            btnRefresh.addEventListener('click', () => {
                carregarProdutos();
                showToast('Cardápio Atualizado', 'Lista de produtos sincronizada com o banco.', 'success');
            });
        }

        if (btnResetFilters) {
            btnResetFilters.addEventListener('click', () => {
                if (inputSearch) inputSearch.value = '';
                if (selectCat) selectCat.value = 'ALL';
                if (selectStatus) selectStatus.value = 'ALL';
                state.filtroTexto = '';
                state.filtroCategoria = 'ALL';
                state.filtroStatus = 'ALL';
                if (btnClearSearch) btnClearSearch.classList.add('hidden');
                aplicarFiltrosTabela();
            });
        }

        if (btnOpenCreate) {
            btnOpenCreate.addEventListener('click', () => {
                abrirModalProduto();
            });
        }

        // Delegação de eventos para ações na tabela (Pausar, Editar, Ajustar Estoque)
        const tbody = document.getElementById('tbody-products');
        if (tbody) {
            tbody.addEventListener('click', (e) => {
                const btnToggle = e.target.closest('.btn-action-toggle');
                if (btnToggle) {
                    const id = btnToggle.getAttribute('data-id');
                    const ativo = btnToggle.getAttribute('data-ativo') === 'true';
                    toggleProdutoAtivo(id, ativo);
                    return;
                }

                const btnEdit = e.target.closest('.btn-action-edit');
                if (btnEdit) {
                    const id = btnEdit.getAttribute('data-id');
                    const produto = state.produtos.find(p => String(p.id) === String(id));
                    if (produto) {
                        abrirModalProduto(produto);
                    }
                    return;
                }

                const btnStock = e.target.closest('.btn-quick-stock');
                if (btnStock) {
                    const id = btnStock.getAttribute('data-id');
                    const nome = btnStock.getAttribute('data-name');
                    const estoque = parseInt(btnStock.getAttribute('data-stock') || '0', 10);
                    abrirModalEstoque(id, nome, estoque);
                    return;
                }
            });
        }

        initProductModal();
        initStockModal();
    }

    async function carregarCategorias() {
        try {
            const response = await fetch('/api/admin/categorias');
            if (!response.ok) return;
            const categorias = await response.json();
            state.categorias = categorias;

            // Atualizar select do filtro
            const selectFilter = document.getElementById('filter-categoria');
            if (selectFilter) {
                const currentVal = selectFilter.value;
                let optionsHtml = '<option value="ALL">Todas as Categorias</option>';
                categorias.forEach(cat => {
                    optionsHtml += `<option value="${cat.id}">${escapeHtml(cat.nome)}</option>`;
                });
                selectFilter.innerHTML = optionsHtml;
                selectFilter.value = currentVal;
            }

            // Atualizar select do formulário de produto
            const selectModal = document.getElementById('prod-categoria');
            if (selectModal) {
                let optionsHtml = '<option value="">Selecione uma categoria...</option>';
                categorias.forEach(cat => {
                    optionsHtml += `<option value="${cat.id}">${escapeHtml(cat.nome)}</option>`;
                });
                selectModal.innerHTML = optionsHtml;
            }
        } catch (e) {
            console.error('Erro ao carregar categorias:', e);
        }
    }

    async function carregarProdutos() {
        try {
            const response = await fetch('/api/admin/produtos');
            if (!response.ok) throw new Error('Falha ao listar produtos');

            const produtos = await response.json();
            state.produtos = produtos;
            renderizarTabelaProdutos(produtos);
            aplicarFiltrosTabela();
        } catch (err) {
            console.error('Erro ao carregar produtos:', err);
            showToast('Erro de Conexão', 'Não foi possível carregar o catálogo de produtos.', 'error');
        }
    }

    function renderizarTabelaProdutos(produtos) {
        const tbody = document.getElementById('tbody-products');
        if (!tbody) return;

        let html = '';
        produtos.forEach(prod => {
            const imgUrl = prod.imagemUrl || '/img/burger-hero.svg';
            const catNome = prod.categoriaNome || 'Geral';
            const precoFormatado = formatarMoeda(prod.preco || 0);
            const estoque = prod.quantidadeEstoque != null ? prod.quantidadeEstoque : 0;
            const ativo = prod.ativo === true;

            let stockClass = 'stock-ok';
            if (estoque === 0) stockClass = 'stock-empty';
            else if (estoque <= 10) stockClass = 'stock-low';

            html += `
                <tr class="product-row" 
                    data-id="${prod.id}" 
                    data-categoria-id="${prod.categoriaId}" 
                    data-ativo="${ativo}" 
                    data-estoque="${estoque}">
                    <td class="product-img-cell">
                        <div class="table-thumb-box">
                            <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(prod.nome)}" class="table-product-thumb" onerror="this.src='/img/burger-hero.svg'">
                        </div>
                    </td>
                    <td>
                        <div class="product-name-box">
                            <span class="product-row-name">${escapeHtml(prod.nome)}</span>
                            <span class="product-row-id">#${prod.id}</span>
                        </div>
                        <p class="product-row-desc">${escapeHtml(prod.descricao || 'Sem descrição cadastrada')}</p>
                    </td>
                    <td>
                        <span class="category-badge">${escapeHtml(catNome)}</span>
                    </td>
                    <td class="text-right">
                        <span class="price-val">R$ ${precoFormatado}</span>
                    </td>
                    <td class="text-center">
                        <div class="stock-cell-content">
                            <span class="stock-badge ${stockClass}">${estoque} un</span>
                            <button type="button" class="btn-quick-stock" 
                                    data-id="${prod.id}" 
                                    data-name="${escapeHtml(prod.nome)}" 
                                    data-stock="${estoque}" 
                                    title="Ajustar estoque rapidamente">✏️</button>
                        </div>
                    </td>
                    <td class="text-center">
                        <span class="status-badge ${ativo ? 'status-active' : 'status-paused'}">
                            ${ativo ? 'Ativo' : 'Pausado'}
                        </span>
                    </td>
                    <td class="text-center">
                        <div class="table-actions">
                            <button type="button" 
                                    class="btn-action-toggle ${ativo ? 'btn-pause' : 'btn-resume'}" 
                                    data-id="${prod.id}" 
                                    data-ativo="${ativo}" 
                                    title="${ativo ? 'Pausar venda imediatamente no Totem' : 'Reativar venda no Totem'}">
                                <span>${ativo ? '⏸️ Pausar' : '▶️ Ativar'}</span>
                            </button>
                            <button type="button" class="btn-action-edit" data-id="${prod.id}" title="Editar informações do produto">
                                ✏️ Editar
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    }

    function aplicarFiltrosTabela() {
        const rows = document.querySelectorAll('#tbody-products .product-row');
        const emptyState = document.getElementById('products-empty-state');
        const counterEl = document.getElementById('products-counter-text');

        let visiveis = 0;
        const total = rows.length;

        rows.forEach(row => {
            const nome = (row.querySelector('.product-row-name')?.textContent || '').toLowerCase();
            const desc = (row.querySelector('.product-row-desc')?.textContent || '').toLowerCase();
            const id = (row.getAttribute('data-id') || '').toLowerCase();
            const catId = row.getAttribute('data-categoria-id');
            const ativo = row.getAttribute('data-ativo') === 'true';
            const estoque = parseInt(row.getAttribute('data-estoque') || '0', 10);

            // Filtro por Texto
            const matchTexto = state.filtroTexto === '' || 
                               nome.includes(state.filtroTexto) || 
                               desc.includes(state.filtroTexto) || 
                               id === state.filtroTexto;

            // Filtro por Categoria
            const matchCat = state.filtroCategoria === 'ALL' || catId === state.filtroCategoria;

            // Filtro por Status
            let matchStatus = true;
            if (state.filtroStatus === 'ATIVO') matchStatus = ativo;
            else if (state.filtroStatus === 'PAUSADO') matchStatus = !ativo;
            else if (state.filtroStatus === 'ESTOQUE_BAIXO') matchStatus = (estoque <= 10 && estoque > 0);
            else if (state.filtroStatus === 'ESGOTADO') matchStatus = (estoque === 0);

            if (matchTexto && matchCat && matchStatus) {
                row.style.display = '';
                visiveis++;
            } else {
                row.style.display = 'none';
            }
        });

        if (emptyState) {
            emptyState.classList.toggle('hidden', visiveis > 0);
        }

        if (counterEl) {
            counterEl.textContent = `Exibindo ${visiveis} de ${total} produtos cadastrados`;
        }
    }

    // Toggle rápido "Pausar Venda" / "Reativar"
    async function toggleProdutoAtivo(id, estaAtivo) {
        const endpoint = estaAtivo 
            ? `/api/admin/produtos/${id}/desativar` 
            : `/api/admin/produtos/${id}/reativar`;

        // Atualização Otimista no DOM
        const row = document.querySelector(`.product-row[data-id="${id}"]`);
        const novoStatus = !estaAtivo;

        if (row) {
            row.setAttribute('data-ativo', String(novoStatus));
            const statusBadge = row.querySelector('.status-badge');
            const btnToggle = row.querySelector('.btn-action-toggle');

            if (statusBadge) {
                statusBadge.className = `status-badge ${novoStatus ? 'status-active' : 'status-paused'}`;
                statusBadge.textContent = novoStatus ? 'Ativo' : 'Pausado';
            }
            if (btnToggle) {
                btnToggle.setAttribute('data-ativo', String(novoStatus));
                btnToggle.className = `btn-action-toggle ${novoStatus ? 'btn-pause' : 'btn-resume'}`;
                btnToggle.innerHTML = `<span>${novoStatus ? '⏸️ Pausar' : '▶️ Ativar'}</span>`;
            }
        }

        try {
            const response = await fetch(endpoint, { method: 'PATCH' });
            if (!response.ok) throw new Error('Falha na resposta do servidor');

            // Atualizar cache local
            const item = state.produtos.find(p => String(p.id) === String(id));
            if (item) item.ativo = novoStatus;

            const msg = novoStatus 
                ? `Produto #${id} reativado com sucesso no Totem!` 
                : `Venda pausada! Produto #${id} removido da vitrine do Totem.`;
            showToast('Cardápio Atualizado', msg, novoStatus ? 'success' : 'warning');

            // Notificar totem via broadcast channel
            notificarAlteracaoTotem(novoStatus ? 'REATIVADO' : 'PAUSADO', id);
            aplicarFiltrosTabela();
        } catch (err) {
            console.error('Erro ao alternar status do produto:', err);
            showToast('Erro ao Atualizar', 'Não foi possível alterar o status do produto.', 'error');
            // Reverter alteração otimista
            carregarProdutos();
        }
    }

    // --------------------------------------------------------------------------
    // 6. MODAL DE CADASTRO E EDIÇÃO DE PRODUTO
    // --------------------------------------------------------------------------
    function initProductModal() {
        const modal = document.getElementById('modal-product-backdrop');
        const btnClose = document.getElementById('btn-close-product-modal');
        const btnCancel = document.getElementById('btn-cancel-product-modal');
        const form = document.getElementById('form-product');
        const inputImagem = document.getElementById('prod-imagem');
        const previewImg = document.getElementById('prod-image-preview');
        const toggleAtivo = document.getElementById('prod-ativo');
        const labelAtivo = document.getElementById('label-prod-ativo');
        const shortcutBtns = document.querySelectorAll('.btn-img-shortcut');

        if (btnClose) btnClose.addEventListener('click', fecharModalProduto);
        if (btnCancel) btnCancel.addEventListener('click', fecharModalProduto);

        // Fechar ao clicar fora
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) fecharModalProduto();
            });
        }

        // Preview em tempo real da imagem
        if (inputImagem && previewImg) {
            inputImagem.addEventListener('input', (e) => {
                const url = e.target.value.trim();
                previewImg.src = url || '/img/burger-hero.svg';
            });
        }

        // Atalhos rápidos de imagem
        shortcutBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const src = btn.getAttribute('data-src');
                if (inputImagem) {
                    inputImagem.value = src;
                    if (previewImg) previewImg.src = src;
                }
            });
        });

        // Toggle label ativo/inativo
        if (toggleAtivo && labelAtivo) {
            toggleAtivo.addEventListener('change', () => {
                labelAtivo.textContent = toggleAtivo.checked 
                    ? 'Ativo (Visível no Totem)' 
                    : 'Pausado (Oculto no Totem)';
            });
        }

        // Submissão do formulário com validações completas
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await salvarProduto();
            });
        }
    }

    function abrirModalProduto(produto = null) {
        const modal = document.getElementById('modal-product-backdrop');
        const titleEl = document.getElementById('modal-product-title');
        const btnSaveText = document.getElementById('save-product-text');
        const form = document.getElementById('form-product');

        limparErrosFormulario();

        if (produto) {
            // Edição
            titleEl.textContent = `Editar Produto: ${produto.nome}`;
            if (btnSaveText) btnSaveText.textContent = 'Salvar Alterações';

            document.getElementById('product-form-id').value = produto.id;
            document.getElementById('prod-nome').value = produto.nome || '';
            document.getElementById('prod-categoria').value = produto.categoriaId || '';
            document.getElementById('prod-preco').value = produto.preco != null ? produto.preco : '';
            document.getElementById('prod-estoque').value = produto.quantidadeEstoque != null ? produto.quantidadeEstoque : 0;
            document.getElementById('prod-imagem').value = produto.imagemUrl || '';
            document.getElementById('prod-descricao').value = produto.descricao || '';
            
            const isAtivo = produto.ativo !== false;
            const toggle = document.getElementById('prod-ativo');
            if (toggle) toggle.checked = isAtivo;
            const label = document.getElementById('label-prod-ativo');
            if (label) label.textContent = isAtivo ? 'Ativo (Visível no Totem)' : 'Pausado (Oculto no Totem)';

            const preview = document.getElementById('prod-image-preview');
            if (preview) preview.src = produto.imagemUrl || '/img/burger-hero.svg';
        } else {
            // Cadastro
            titleEl.textContent = 'Cadastrar Novo Produto';
            if (btnSaveText) btnSaveText.textContent = 'Cadastrar Produto';
            form.reset();
            document.getElementById('product-form-id').value = '';
            document.getElementById('prod-ativo').checked = true;
            document.getElementById('label-prod-ativo').textContent = 'Ativo (Visível no Totem)';
            document.getElementById('prod-image-preview').src = '/img/burger-hero.svg';
            document.getElementById('prod-imagem').value = '/img/burger-hero.svg';
        }

        modal.classList.remove('hidden');
        document.getElementById('prod-nome')?.focus();
    }

    function fecharModalProduto() {
        const modal = document.getElementById('modal-product-backdrop');
        if (modal) modal.classList.add('hidden');
    }

    function validarFormularioProduto() {
        limparErrosFormulario();
        let valido = true;

        const nome = document.getElementById('prod-nome').value.trim();
        const categoria = document.getElementById('prod-categoria').value;
        const precoStr = document.getElementById('prod-preco').value.trim();
        const estoqueStr = document.getElementById('prod-estoque').value.trim();

        if (!nome) {
            mostrarErroCampo('prod-nome', 'error-prod-nome', 'O nome do produto é obrigatório.');
            valido = false;
        }

        if (!categoria) {
            mostrarErroCampo('prod-categoria', 'error-prod-categoria', 'Selecione uma categoria para o produto.');
            valido = false;
        }

        const preco = parseFloat(precoStr);
        if (isNaN(preco) || preco <= 0) {
            mostrarErroCampo('prod-preco', 'error-prod-preco', 'O preço de venda deve ser maior que zero (R$).');
            valido = false;
        }

        const estoque = parseInt(estoqueStr, 10);
        if (isNaN(estoque) || estoque < 0) {
            mostrarErroCampo('prod-estoque', 'error-prod-estoque', 'A quantidade em estoque não pode ser negativa.');
            valido = false;
        }

        return valido;
    }

    function mostrarErroCampo(inputId, errorId, mensagem) {
        const input = document.getElementById(inputId);
        const error = document.getElementById(errorId);
        if (input) input.classList.add('is-invalid');
        if (error) error.textContent = mensagem;
    }

    function limparErrosFormulario() {
        document.querySelectorAll('.form-input, .form-select').forEach(el => el.classList.remove('is-invalid'));
        document.querySelectorAll('.form-error-msg').forEach(el => el.textContent = '');
    }

    async function salvarProduto() {
        if (!validarFormularioProduto()) return;

        const id = document.getElementById('product-form-id').value;
        const nome = document.getElementById('prod-nome').value.trim();
        const categoriaId = parseInt(document.getElementById('prod-categoria').value, 10);
        const preco = parseFloat(document.getElementById('prod-preco').value);
        const quantidadeEstoque = parseInt(document.getElementById('prod-estoque').value, 10);
        const imagemUrl = document.getElementById('prod-imagem').value.trim() || '/img/burger-hero.svg';
        const descricao = document.getElementById('prod-descricao').value.trim();
        const ativo = document.getElementById('prod-ativo').checked;

        const payload = {
            nome,
            descricao,
            preco,
            quantidadeEstoque,
            imagemUrl,
            ativo,
            categoriaId
        };

        const isEdicao = !!id;
        const url = isEdicao ? `/api/admin/produtos/${id}` : '/api/admin/produtos';
        const method = isEdicao ? 'PUT' : 'POST';

        const btnSave = document.getElementById('btn-save-product');
        if (btnSave) btnSave.disabled = true;

        try {
            const response = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const erroData = await response.json().catch(() => null);
                throw new Error(erroData?.detail || erroData?.message || 'Falha ao salvar produto');
            }

            const salvo = await response.json();
            fecharModalProduto();
            showToast('Produto Salvo', `O produto "${salvo.nome}" foi salvo com sucesso!`, 'success');

            // Atualizar catálogo e notificar totem
            await carregarProdutos();
            notificarAlteracaoTotem(isEdicao ? 'EDITADO' : 'CRIADO', salvo.id);
        } catch (err) {
            console.error('Erro ao salvar produto:', err);
            showToast('Erro ao Salvar', err.message || 'Ocorreu um erro ao salvar o produto.', 'error');
        } finally {
            if (btnSave) btnSave.disabled = false;
        }
    }

    // --------------------------------------------------------------------------
    // 7. MODAL DE AJUSTE RÁPIDO DE ESTOQUE
    // --------------------------------------------------------------------------
    function initStockModal() {
        const modal = document.getElementById('modal-stock-backdrop');
        const btnClose = document.getElementById('btn-close-stock-modal');
        const btnCancel = document.getElementById('btn-cancel-stock-modal');
        const form = document.getElementById('form-stock');
        const inputQty = document.getElementById('stock-new-qty');

        if (btnClose) btnClose.addEventListener('click', fecharModalEstoque);
        if (btnCancel) btnCancel.addEventListener('click', fecharModalEstoque);

        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) fecharModalEstoque();
            });
        }

        // Stepper buttons
        document.getElementById('btn-stock-minus')?.addEventListener('click', () => alterarStepper(-5));
        document.getElementById('btn-stock-minus-one')?.addEventListener('click', () => alterarStepper(-1));
        document.getElementById('btn-stock-plus-one')?.addEventListener('click', () => alterarStepper(1));
        document.getElementById('btn-stock-plus')?.addEventListener('click', () => alterarStepper(5));

        function alterarStepper(delta) {
            if (!inputQty) return;
            let val = parseInt(inputQty.value || '0', 10);
            val = Math.max(0, val + delta);
            inputQty.value = val;
        }

        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await salvarAjusteEstoque();
            });
        }
    }

    function abrirModalEstoque(id, nome, estoqueAtual) {
        const modal = document.getElementById('modal-stock-backdrop');
        const subTitle = document.getElementById('modal-stock-subtitle');
        const inputId = document.getElementById('stock-product-id');
        const inputQty = document.getElementById('stock-new-qty');
        const errorEl = document.getElementById('error-stock-qty');

        if (subTitle) subTitle.textContent = `Produto: #${id} - ${nome}`;
        if (inputId) inputId.value = id;
        if (inputQty) inputQty.value = estoqueAtual;
        if (errorEl) errorEl.textContent = '';

        modal.classList.remove('hidden');
        if (inputQty) inputQty.focus();
    }

    function fecharModalEstoque() {
        const modal = document.getElementById('modal-stock-backdrop');
        if (modal) modal.classList.add('hidden');
    }

    async function salvarAjusteEstoque() {
        const id = document.getElementById('stock-product-id').value;
        const inputQty = document.getElementById('stock-new-qty');
        const errorEl = document.getElementById('error-stock-qty');

        const novaQtd = parseInt(inputQty.value, 10);
        if (isNaN(novaQtd) || novaQtd < 0) {
            if (errorEl) errorEl.textContent = 'A quantidade não pode ser negativa.';
            return;
        }

        try {
            const response = await fetch(`/api/admin/produtos/${id}/estoque`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quantidadeEstoque: novaQtd })
            });

            if (!response.ok) throw new Error('Falha ao atualizar estoque');

            fecharModalEstoque();
            showToast('Estoque Atualizado', `Estoque do produto #${id} atualizado para ${novaQtd} un.`, 'success');
            await carregarProdutos();
            notificarAlteracaoTotem('ESTOQUE_ATUALIZADO', id);
        } catch (err) {
            console.error('Erro ao atualizar estoque:', err);
            showToast('Erro', 'Não foi possível atualizar o estoque do produto.', 'error');
        }
    }

    // --------------------------------------------------------------------------
    // 8. FECHAMENTO DE CAIXA DIÁRIO
    // --------------------------------------------------------------------------
    function initFechamento() {
        const dateInput = document.getElementById('fechamento-data-input');
        const btnConsultar = document.getElementById('btn-carregar-fechamento');

        if (dateInput) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }

        if (btnConsultar) {
            btnConsultar.addEventListener('click', () => {
                const data = dateInput ? dateInput.value : null;
                carregarFechamento(data);
            });
        }
    }

    function consultarFechamentoHoje() {
        const dateInput = document.getElementById('fechamento-data-input');
        const data = dateInput?.value || new Date().toISOString().split('T')[0];
        carregarFechamento(data);
    }

    async function carregarFechamento(dataStr) {
        try {
            const url = `/api/admin/relatorios/fechamento-caixa${dataStr ? '?data=' + dataStr : ''}`;
            const response = await fetch(url);
            if (!response.ok) throw new Error('Falha ao carregar fechamento de caixa');

            const dados = await response.json();
            state.fechamento = dados;
            renderizarFechamento(dados);
        } catch (err) {
            console.error('Erro ao carregar fechamento de caixa:', err);
            showToast('Erro', 'Não foi possível carregar o fechamento de caixa para a data informada.', 'error');
        }
    }

    function renderizarFechamento(dados) {
        const dataRefEl = document.getElementById('fechamento-data-ref');
        const totalFatEl = document.getElementById('fechamento-total-faturado');
        const totalPedEl = document.getElementById('fechamento-total-pedidos');
        const ticketMedEl = document.getElementById('fechamento-ticket-medio');
        const tbody = document.getElementById('tbody-fechamento-detalhes');

        if (dataRefEl) {
            dataRefEl.textContent = dados.dataFechamento ? formatarDataSimples(dados.dataFechamento) : 'Hoje';
        }
        if (totalFatEl) {
            totalFatEl.textContent = `R$ ${formatarMoeda(dados.totalFaturado || 0)}`;
        }
        if (totalPedEl) {
            totalPedEl.textContent = dados.totalPedidos || 0;
        }
        if (ticketMedEl) {
            ticketMedEl.textContent = `R$ ${formatarMoeda(dados.ticketMedio || 0)}`;
        }

        if (!tbody) return;

        const detalhes = dados.detalhamentoPorForma || [];
        if (detalhes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center py-4" style="color: var(--color-text-muted);">Nenhuma venda registrada na data selecionada.</td></tr>`;
            return;
        }

        let html = '';
        detalhes.forEach(item => {
            const valorStr = formatarMoeda(item.totalFaturado || 0);
            const pct = (item.percentual || 0).toFixed(1).replace('.', ',');
            const nomeDesc = formatarNomePagamento(item.descricao || item.formaPagamento);

            html += `
                <tr>
                    <td style="font-weight: 600;">${nomeDesc}</td>
                    <td class="text-center font-bold">${item.quantidadePedidos || 0}</td>
                    <td class="text-right font-bold text-gold">R$ ${valorStr}</td>
                    <td class="text-right font-bold">${pct}%</td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    }

    // --------------------------------------------------------------------------
    // 9. TOAST NOTIFICATIONS
    // --------------------------------------------------------------------------
    function showToast(titulo, mensagem, tipo = 'info') {
        const container = document.getElementById('admin-toast-container');
        if (!container) return;

        const icons = {
            success: '✅',
            warning: '⚠️',
            error: '❌',
            info: 'ℹ️'
        };

        const toast = document.createElement('div');
        toast.className = `admin-toast toast-${tipo}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[tipo] || 'ℹ️'}</span>
            <div class="toast-body">
                <div class="toast-title">${escapeHtml(titulo)}</div>
                <div class="toast-msg">${escapeHtml(mensagem)}</div>
            </div>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    // --------------------------------------------------------------------------
    // 10. UTILITÁRIOS DE FORMATAÇÃO
    // --------------------------------------------------------------------------
    function formatarMoeda(valor) {
        const num = typeof valor === 'number' ? valor : parseFloat(valor || 0);
        return num.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function formatarDataSimples(dataStr) {
        if (!dataStr) return '';
        const partes = dataStr.split('-');
        if (partes.length === 3) {
            return `${partes[2]}/${partes[1]}/${partes[0]}`;
        }
        return dataStr;
    }

    function formatarNomePagamento(forma) {
        if (!forma) return 'Outros';
        const str = String(forma).toUpperCase();
        if (str.includes('PIX')) return 'PIX Instantâneo';
        if (str.includes('CREDITO') || str.includes('CRÉDITO')) return 'Cartão de Crédito';
        if (str.includes('DEBITO') || str.includes('DÉBITO')) return 'Cartão de Débito';
        if (str.includes('DINHEIRO')) return 'Dinheiro em Espécie';
        return forma;
    }

    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

})();
