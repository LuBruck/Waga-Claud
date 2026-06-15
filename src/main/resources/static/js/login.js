// Tela após login bem-sucedido. Monitoramento é a única tela pronta hoje;
// trocar aqui quando a tela de recursos (Maju) entrar.
const TELA_INICIAL = "/monitoramento.html";

// Se já tem usuário logado guardado, pula direto pra tela inicial.
if (localStorage.getItem("usuario")) {
  window.location.href = TELA_INICIAL;
}

// ---- alternância entre abas ----
function mostrarLogin() {
  document.getElementById("formLogin").classList.remove("hidden");
  document.getElementById("formCadastro").classList.add("hidden");
  document.getElementById("tabLogin").classList.add("active");
  document.getElementById("tabCadastro").classList.remove("active");
  limparFeedback();
}

function mostrarCadastro() {
  document.getElementById("formCadastro").classList.remove("hidden");
  document.getElementById("formLogin").classList.add("hidden");
  document.getElementById("tabCadastro").classList.add("active");
  document.getElementById("tabLogin").classList.remove("active");
  limparFeedback();
}

// ---- login ----
async function fazerLogin(event) {
  event.preventDefault();
  const email = document.getElementById("loginEmail").value.trim();
  const senha = document.getElementById("loginSenha").value;

  try {
    const resp = await fetch("/api/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, senha }),
    });

    if (!resp.ok) {
      const erro = await resp.text();
      mostrarErro(erro || "Não foi possível entrar.");
      return;
    }

    const usuario = await resp.json();
    // Guarda o usuário logado — as outras telas leem o id daqui.
    localStorage.setItem("usuario", JSON.stringify(usuario));
    window.location.href = TELA_INICIAL;
  } catch (e) {
    mostrarErro("Erro de conexão com o servidor.");
  }
}

// ---- cadastro ----
async function fazerCadastro(event) {
  event.preventDefault();
  const nome = document.getElementById("cadNome").value.trim();
  const email = document.getElementById("cadEmail").value.trim();
  const senha = document.getElementById("cadSenha").value;

  try {
    const resp = await fetch("/api/cadastro", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, email, senha }),
    });

    if (!resp.ok) {
      const erro = await resp.text();
      mostrarErro(erro || "Não foi possível cadastrar.");
      return;
    }

    mostrarSucesso("Conta criada! Agora é só entrar.");
    mostrarLogin();
    document.getElementById("loginEmail").value = email;
  } catch (e) {
    mostrarErro("Erro de conexão com o servidor.");
  }
}

// ---- feedback ----
function mostrarErro(msg) {
  document.getElementById("feedback").innerHTML =
    `<div class="msg erro">${msg}</div>`;
}

function mostrarSucesso(msg) {
  document.getElementById("feedback").innerHTML =
    `<div class="msg sucesso">${msg}</div>`;
}

function limparFeedback() {
  document.getElementById("feedback").innerHTML = "";
}
