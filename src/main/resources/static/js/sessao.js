// Sessão compartilhada entre as telas internas (recursos, monitoramento).
// Não há token/sessão de verdade: o usuário logado fica no localStorage e o
// id dele viaja como ?usuarioId= nas rotas que precisam saber quem está agindo.

// Lê o usuário logado. Se não houver, manda de volta pra tela de login.
function exigirLogin() {
  const bruto = localStorage.getItem("usuario");
  if (!bruto) {
    window.location.href = "/";
    return null;
  }
  return JSON.parse(bruto);
}

function ehAdmin(usuario) {
  return usuario && usuario.nivelAcesso === "ADMIN";
}

function logout() {
  localStorage.removeItem("usuario");
  window.location.href = "/";
}

// Monta a barra de navegação no topo. `ativa` = "recursos" ou "monitoramento".
function montarNav(usuario, ativa) {
  const nav = document.getElementById("nav");
  if (!nav) return;

  const nivel = ehAdmin(usuario) ? "ADMIN" : "COMUM";
  nav.innerHTML = `
    <div class="nav-brand">☁️ <span>WagaClaud</span></div>
    <div class="nav-links">
      <a href="/recursos.html" class="${ativa === "recursos" ? "ativo" : ""}">Recursos</a>
      <a href="/monitoramento.html" class="${ativa === "monitoramento" ? "ativo" : ""}">Monitoramento</a>
    </div>
    <div class="nav-user">
      <span class="nav-nome">${usuario.nome}</span>
      <span class="nav-nivel ${nivel === "ADMIN" ? "admin" : ""}">${nivel}</span>
      <button class="nav-sair" onclick="logout()">Sair</button>
    </div>`;
}
