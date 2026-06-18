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
