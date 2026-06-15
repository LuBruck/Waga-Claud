const BASE_URL = '/api/monitoramento';

function mostrarFeedback(mensagem, tipo) {
  const el = document.getElementById('feedback');
  el.textContent = mensagem;
  el.className = tipo;
  el.style.display = 'block';

  clearTimeout(el._timer);
  el._timer = setTimeout(() => {
    el.style.display = 'none';
  }, 4000);
}

function renderizarTabela(metricas, titulo) {
  const container = document.getElementById('tabelaContainer');
  const tituloEl  = document.getElementById('tabelaTitulo');
  const badgeEl   = document.getElementById('tabelaBadge');

  tituloEl.textContent = titulo;

  if (!metricas || metricas.length === 0) {
    badgeEl.textContent = '0 registros';
    badgeEl.className = 'badge';
    container.innerHTML = '<p class="empty">Nenhuma métrica encontrada.</p>';
    return;
  }

  const criticos = metricas.filter(m => m.valor >= 95).length;
  badgeEl.textContent = `${metricas.length} registro${metricas.length > 1 ? 's' : ''}${criticos > 0 ? ` · ${criticos} crítico${criticos > 1 ? 's' : ''}` : ''}`;
  badgeEl.className = criticos > 0 ? 'badge critico' : 'badge';

  const linhas = metricas.map(m => {
    const isCritico = m.valor >= 95;
    const tagClass  = m.metrica === 'CPU' ? 'tag-cpu' : 'tag-ram';
    const valorClass = isCritico ? 'valor-critico' : 'valor-ok';
    const valorFormatado = m.valor.toFixed(1) + '%';
    const data = m.timestamp
      ? new Date(m.timestamp).toLocaleString('pt-BR')
      : '—';

    return `
      <tr>
        <td>${m.id}</td>
        <td><span class="tag ${tagClass}">${m.metrica}</span></td>
        <td class="${valorClass}">${valorFormatado}${isCritico ? ' ⚠' : ''}</td>
        <td>${data}</td>
        <td>${m.recurso?.nome ?? `Recurso #${m.recurso?.id ?? '?'}`}</td>
      </tr>`;
  }).join('');

  container.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Métrica</th>
          <th>Valor</th>
          <th>Timestamp</th>
          <th>Recurso</th>
        </tr>
      </thead>
      <tbody>${linhas}</tbody>
    </table>`;
}

async function listarPorRecurso() {
  const id = document.getElementById('inputListar').value.trim();

  if (!id) {
    mostrarFeedback('Informe o ID do recurso antes de buscar.', 'error');
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/${id}`);

    if (!res.ok) {
      const msg = await res.text();
      mostrarFeedback(`Erro ao buscar métricas: ${msg || res.status}`, 'error');
      return;
    }

    const dados = await res.json();
    renderizarTabela(dados, `Métricas do recurso #${id}`);

    if (dados.length === 0) {
      mostrarFeedback(`Nenhuma métrica encontrada para o recurso #${id}.`, 'error');
    } else {
      mostrarFeedback(`${dados.length} métrica(s) carregada(s) para o recurso #${id}.`, 'success');
    }

  } catch (err) {
    mostrarFeedback('Não foi possível conectar ao servidor. Verifique se o app está rodando.', 'error');
    console.error(err);
  }
}

async function gerarMetricas() {
  const id  = document.getElementById('inputGerar').value.trim();
  const btn = document.querySelector('[onclick="gerarMetricas()"]');

  if (!id) {
    mostrarFeedback('Informe o ID do recurso antes de gerar métricas.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Gerando…';

  try {
    const res = await fetch(`${BASE_URL}/gerar?recursoId=${id}`, {
      method: 'POST'
    });

    if (!res.ok) {
      const msg = await res.text();
      mostrarFeedback(`Erro ao gerar métricas: ${msg || res.status}`, 'error');
      return;
    }

    mostrarFeedback(`Métricas geradas com sucesso para o recurso #${id}!`, 'success');

    document.getElementById('inputListar').value = id;
    await listarPorRecurso();

  } catch (err) {
    mostrarFeedback('Não foi possível conectar ao servidor. Verifique se o app está rodando.', 'error');
    console.error(err);
  } finally {
    btn.disabled = false;
    btn.textContent = 'Gerar';
  }
}


async function listarCriticos() {
  try {
    const res = await fetch(`${BASE_URL}/criticos`);

    if (!res.ok) {
      const msg = await res.text();
      mostrarFeedback(`Erro ao buscar críticos: ${msg || res.status}`, 'error');
      return;
    }

    const dados = await res.json();
    renderizarTabela(dados, 'Métricas críticas (≥ 95%)');

    if (dados.length === 0) {
      mostrarFeedback('Nenhuma métrica crítica no momento.', 'success');
    } else {
      mostrarFeedback(`⚠ ${dados.length} métrica(s) crítica(s) encontrada(s)!`, 'error');
    }

  } catch (err) {
    mostrarFeedback('Não foi possível conectar ao servidor. Verifique se o app está rodando.', 'error');
    console.error(err);
  }
}
