const usuario = exigirLogin();
const usuarioId = usuario ? usuario.id : null;
const admin = ehAdmin(usuario);

let vmsCache = [];
let discosCache = [];

document.addEventListener("DOMContentLoaded", () => {
  montarNav(usuario, "recursos");
  carregarRecursos();
});

function ehVM(r) {
  return r != null && r.qtdCpu !== undefined;
}

function escapeHtml(txt) {
  return String(txt ?? "")
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function feedback(mensagem, tipo) {
  const el = document.getElementById("feedback");
  el.textContent = mensagem;
  el.className = tipo;
  el.style.display = "block";
  clearTimeout(el._timer);
  el._timer = setTimeout(() => { el.style.display = "none"; }, 4000);
}

async function carregarRecursos() {
  try {
    const res = await fetch(`/api/recursos?usuarioId=${usuarioId}`);
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro ao listar recursos: ${msg || res.status}`, "error");
      return;
    }
    const recursos = await res.json();
    vmsCache = recursos.filter(ehVM);
    discosCache = recursos.filter(r => !ehVM(r));
    renderVMs();
    renderDiscos();
  } catch (e) {
    feedback("Não foi possível conectar ao servidor. Verifique se o app está rodando.", "error");
    console.error(e);
  }
}

function renderVMs() {
  const container = document.getElementById("vmsContainer");
  const badge = document.getElementById("badgeVms");
  badge.textContent = `${vmsCache.length} VM${vmsCache.length === 1 ? "" : "s"}`;

  if (vmsCache.length === 0) {
    container.innerHTML = '<p class="empty">Nenhuma máquina virtual ainda. Crie uma acima.</p>';
    return;
  }

  const linhas = vmsCache.map(vm => {
    const qtdDiscos = discosCache.filter(d => d.vmAnexada && d.vmAnexada.id === vm.id).length;
    const acaoPower = vm.status === "ATIVO"
      ? `<button class="btn-sm" onclick="pararVM(${vm.id})">Parar</button>`
      : `<button class="btn-sm" onclick="iniciarVM(${vm.id})">Iniciar</button>`;
    const btnDeletarVm = admin
      ? `<button class="btn-sm btn-danger" onclick="deletar(${vm.id}, 'VM')">Deletar</button>`
      : "";
    const acoes = acaoPower + btnDeletarVm;
    return `
      <tr>
        <td>${vm.id}</td>
        <td>${escapeHtml(vm.nome)}</td>
        <td><span class="status ${vm.status}">${vm.status}</span></td>
        <td>${vm.qtdCpu} vCPU</td>
        <td>${vm.memoriaRamGB} GB</td>
        <td>${escapeHtml(vm.so)}</td>
        <td>${qtdDiscos} disco${qtdDiscos === 1 ? "" : "s"}</td>
        ${admin ? `<td class="dono">${escapeHtml(vm.dono?.nome ?? "—")}</td>` : ""}
        <td><div class="row-actions">${acoes}</div></td>
      </tr>`;
  }).join("");

  container.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>ID</th><th>Nome</th><th>Status</th><th>CPU</th><th>RAM</th>
          <th>SO</th><th>Discos</th>${admin ? "<th>Dono</th>" : ""}<th>Ações</th>
        </tr>
      </thead>
      <tbody>${linhas}</tbody>
    </table>`;
}

function renderDiscos() {
  const container = document.getElementById("discosContainer");
  const badge = document.getElementById("badgeDiscos");
  const soltos = discosCache.filter(d => !d.vmAnexada).length;
  badge.textContent = `${discosCache.length} disco${discosCache.length === 1 ? "" : "s"}` +
    (soltos > 0 ? ` · ${soltos} solto${soltos === 1 ? "" : "s"}` : "");

  if (discosCache.length === 0) {
    container.innerHTML = '<p class="empty">Nenhum disco ainda. Crie um acima.</p>';
    return;
  }

  const linhas = discosCache.map(d => {
    const tipoClass = d.tipoDisco === "SSD" ? "tag-ssd" : "tag-hdd";
    const anexado = d.vmAnexada
      ? `<span class="anexo-vm">VM '${escapeHtml(d.vmAnexada.nome)}'</span>`
      : '<span class="anexo-solto">solto</span>';

    let acaoAnexo;
    if (d.vmAnexada) {
      acaoAnexo = `<button class="btn-sm" onclick="desanexar(${d.id})">Desanexar</button>`;
    } else {
      const vmsCompat = vmsCache.filter(vm => (vm.dono?.id ?? null) === (d.dono?.id ?? null));
      if (vmsCompat.length === 0) {
        acaoAnexo = '<span class="anexo-solto">sem VM compatível</span>';
      } else {
        const opcoes = vmsCompat.map(vm =>
          `<option value="${vm.id}">${escapeHtml(vm.nome)} (#${vm.id})</option>`).join("");
        acaoAnexo = `
          <select id="anexarSel-${d.id}">${opcoes}</select>
          <button class="btn-sm" onclick="anexar(${d.id})">Anexar</button>`;
      }
    }

    const acaoTamanho =
      `<button class="btn-sm" onclick="expandir(${d.id})">Expandir</button>` +
      `<button class="btn-sm" onclick="reduzir(${d.id})">Reduzir</button>`;

    const btnDeletar = admin
      ? `<button class="btn-sm btn-danger" onclick="deletar(${d.id}, 'disco')">Deletar</button>`
      : "";

    return `
      <tr>
        <td>${d.id}</td>
        <td>${escapeHtml(d.nome)}</td>
        <td><span class="status ${d.status}">${d.status}</span></td>
        <td><span class="tag ${tipoClass}">${d.tipoDisco}</span></td>
        <td>${d.usadoGB}/${d.capacidadeGB} GB</td>
        <td>${anexado}</td>
        ${admin ? `<td class="dono">${escapeHtml(d.dono?.nome ?? "—")}</td>` : ""}
        <td><div class="row-actions">${acaoTamanho}${acaoAnexo}${btnDeletar}</div></td>
      </tr>`;
  }).join("");

  container.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>ID</th><th>Nome</th><th>Status</th><th>Tipo</th><th>Uso</th>
          <th>Anexado a</th>${admin ? "<th>Dono</th>" : ""}<th>Ações</th>
        </tr>
      </thead>
      <tbody>${linhas}</tbody>
    </table>`;
}

async function criarVM(event) {
  event.preventDefault();
  const nome = document.getElementById("vmNome").value.trim();
  const qtdCpu = Number(document.getElementById("vmCpu").value);
  const memoriaRamGB = Number(document.getElementById("vmRam").value);
  const so = document.getElementById("vmSo").value.trim();

  if (!nome) return feedback("Informe o nome da VM.", "error");
  if (!(qtdCpu > 0)) return feedback("A quantidade de CPU deve ser maior que zero.", "error");
  if (!(memoriaRamGB > 0)) return feedback("A memória RAM deve ser maior que zero.", "error");
  if (!so) return feedback("Informe o sistema operacional.", "error");

  const btn = event.submitter;
  btn.disabled = true;
  try {
    const res = await fetch(`/api/vms?usuarioId=${usuarioId}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, qtdCpu, memoriaRamGB, so }),
    });
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro ao criar VM: ${msg || res.status}`, "error");
      return;
    }
    feedback(`VM '${nome}' criada (com disco padrão de 50 GB).`, "success");
    event.target.reset();
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  } finally {
    btn.disabled = false;
  }
}

async function criarStorage(event) {
  event.preventDefault();
  const nome = document.getElementById("discoNome").value.trim();
  const capacidadeGB = Number(document.getElementById("discoCap").value);
  const usadoGB = Number(document.getElementById("discoUsado").value);
  const tipoDisco = document.getElementById("discoTipo").value;

  if (!nome) return feedback("Informe o nome do disco.", "error");
  if (!(capacidadeGB > 0)) return feedback("A capacidade deve ser maior que zero.", "error");
  if (usadoGB < 0 || usadoGB > capacidadeGB)
    return feedback("O espaço usado deve estar entre 0 e a capacidade.", "error");

  const btn = event.submitter;
  btn.disabled = true;
  try {
    const res = await fetch(`/api/storages?usuarioId=${usuarioId}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, capacidadeGB, usadoGB, tipoDisco }),
    });
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro ao criar disco: ${msg || res.status}`, "error");
      return;
    }
    feedback(`Disco '${nome}' criado.`, "success");
    event.target.reset();
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  } finally {
    btn.disabled = false;
  }
}

async function anexar(discoId) {
  const sel = document.getElementById(`anexarSel-${discoId}`);
  if (!sel) return;
  const vmId = Number(sel.value);
  try {
    const res = await fetch("/api/discos/anexar", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ discoId, vmId }),
    });
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro ao anexar: ${msg || res.status}`, "error");
      return;
    }
    feedback("Disco anexado à VM.", "success");
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  }
}

async function desanexar(discoId) {
  try {
    const res = await fetch("/api/discos/desanexar", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ discoId }),
    });
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro ao desanexar: ${msg || res.status}`, "error");
      return;
    }
    feedback("Disco desanexado (agora está solto).", "success");
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  }
}

async function iniciarVM(id) {
  await acaoSimples(`/api/vms/${id}/iniciar`, "PUT", null, "VM iniciada.");
}

async function pararVM(id) {
  await acaoSimples(`/api/vms/${id}/parar`, "PUT", null, "VM parada.");
}

async function expandir(id) {
  const gb = Number(prompt("Quantos GB deseja adicionar?"));
  if (!(gb > 0)) return feedback("Informe um valor maior que zero.", "error");
  await acaoSimples(`/api/discos/${id}/expandir`, "PUT", { gb }, `Disco expandido em ${gb} GB.`);
}

async function reduzir(id) {
  const gb = Number(prompt("Quantos GB deseja remover?"));
  if (!(gb > 0)) return feedback("Informe um valor maior que zero.", "error");
  await acaoSimples(`/api/discos/${id}/reduzir`, "PUT", { gb }, `Disco reduzido em ${gb} GB.`);
}

async function acaoSimples(url, metodo, body, msgSucesso) {
  try {
    const opts = { method: metodo };
    if (body) {
      opts.headers = { "Content-Type": "application/json" };
      opts.body = JSON.stringify(body);
    }
    const res = await fetch(url, opts);
    if (!res.ok) {
      const msg = await res.text();
      feedback(`Erro: ${msg || res.status}`, "error");
      return;
    }
    feedback(msgSucesso, "success");
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  }
}

async function deletar(id, rotulo) {
  if (!confirm(`Tem certeza que deseja deletar ${rotulo} #${id}?`)) return;
  try {
    const res = await fetch(`/api/recursos/${id}?usuarioId=${usuarioId}`, {
      method: "DELETE",
    });
    if (res.status === 403) {
      feedback("Apenas administradores podem deletar recursos.", "error");
      return;
    }
    if (!res.ok && res.status !== 204) {
      const msg = await res.text();
      feedback(`Erro ao deletar: ${msg || res.status}`, "error");
      return;
    }
    feedback(`${rotulo} #${id} deletado.`, "success");
    await carregarRecursos();
  } catch (e) {
    feedback("Erro de conexão com o servidor.", "error");
    console.error(e);
  }
}
