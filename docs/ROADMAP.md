# Roadmap — Cloud Dashboard

> Documento interno de planejamento. Última atualização: 18/05/2026
> Pode (e deve) ser alterado conforme o projeto evolui.

---

## Equipe

| Apelido  | Foco principal                        |
|----------|---------------------------------------|
| Bruck    | Arquitetura, Services, Git, review    |
| Maju     | Telas Swing (JFrame/JDialog), design  |
| Marcelo  | Entidades filhas, DAOs, dados mock    |

---

## Workflow Git

### Branches

```
main          ← só código testado e funcional (nunca commita direto aqui)
  └── develop ← branch de integração (merges das features vão pra cá)
        ├── feat/model-usuario
        ├── feat/dao-recurso
        ├── feat/tela-login
        └── ...
```

**Regras:**
- Ninguém commita direto na `main` nem na `develop`.
- Toda tarefa vira uma branch `feat/nome-da-feat` criada a partir da `develop`.
- Quando a feature tá pronta, abre um **Pull Request** (PR) para `develop`.
- Todo PR precisa de no mínimo **1 reviewer** (de preferência o Bruck).
- Depois que o reviewer aprova, o autor do PR faz o merge.
- A `main` só recebe merge da `develop` nos marcos de entrega (final de cada semana).

### Fluxo diário

```
1. git checkout develop
2. git pull origin develop
3. git checkout -b feat/minha-tarefa      ← cria branch nova
4. ... codar, testar ...
5. git add .
6. git commit -m "feat: descricao curta"
7. git push origin feat/minha-tarefa
8. Abrir PR no GitHub: feat/minha-tarefa → develop
9. Avisar no grupo pra alguém revisar
```

### Padrão de commits

```
feat: criar classe Usuario com enum NivelAcesso
feat: implementar UsuarioDAOMemoria
fix: corrigir buscarPorEmail retornando null
refactor: extrair validação de senha pro service
```

### Resolução de conflitos

Se o `git pull` ou o PR acusar conflito:
1. **Não entre em pânico.** Leia a mensagem do Git.
2. Abra o arquivo marcado — o Git mostra `<<<<<<< HEAD` e `>>>>>>>` indicando as duas versões.
3. Escolha qual versão manter (ou combine as duas).
4. Salve, `git add`, `git commit`.
5. Se travou, chama o Bruck.

---

## Estrutura de pacotes (NetBeans)

```
src/
  └── com.clouddashboard/
        ├── model/
        │     ├── enums/
        │     │     ├── NivelAcesso.java
        │     │     ├── StatusRecurso.java
        │     │     └── TipoDisco.java
        │     ├── Usuario.java
        │     ├── Recurso.java          (abstract)
        │     ├── VirtualMachine.java   (extends Recurso)
        │     ├── Armazenamento.java    (extends Recurso)
        │     └── Monitoramento.java
        ├── dao/
        │     ├── UsuarioDAO.java          (interface)
        │     ├── RecursoDAO.java          (interface)
        │     ├── MonitoramentoDAO.java    (interface)
        │     ├── UsuarioDAOMemoria.java
        │     ├── RecursoDAOMemoria.java
        │     └── MonitoramentoDAOMemoria.java
        ├── service/
        │     ├── AutenticacaoService.java
        │     ├── RecursoService.java
        │     └── MonitoramentoService.java
        ├── view/
        │     ├── TelaLogin.java
        │     ├── TelaCadastro.java
        │     ├── TelaDashboard.java
        │     ├── PainelVMs.java
        │     ├── PainelStorage.java
        │     ├── PainelMonitoramento.java
        │     ├── TelaGerenciarUsuarios.java
        │     ├── DialogCriarVM.java
        │     └── DialogCriarStorage.java
        └── util/
              └── DadosMock.java
```

---

## Semana 0 — Setup (Dias 1–3)

**Objetivo:** Todos com ambiente rodando, repo clonado, estrutura de pacotes criada, enums prontos.

### O que fazer

1. **Bruck** cria o repositório no GitHub, adiciona Maju e Marcelo como colaboradores, cria as branches `main` e `develop`, e faz o commit inicial com a estrutura de pacotes vazia.
2. **Maju e Marcelo** instalam o JDK + NetBeans, clonam o repo, e testam que compila.
3. **Bruck** faz um mini-tutorial de Git pros dois (clone, branch, commit, push, PR) — pode ser por call ou presencial na aula.
4. **Qualquer um** cria os 3 enums como primeira tarefa prática no Git (serve de treino pra abrir a primeira branch e PR).

### Branches dessa fase

| Branch                | Quem    | O que faz                                          |
|-----------------------|---------|----------------------------------------------------|
| `feat/setup-projeto`  | Bruck   | Estrutura de pacotes + .gitignore + README inicial  |
| `feat/enums`          | Qualquer| NivelAcesso, StatusRecurso, TipoDisco               |

### Marco de entrega (fim do dia 3)

- [ ] Todos clonaram e fizeram pelo menos 1 commit + PR
- [ ] Enums estão na `develop`
- [ ] Projeto compila no NetBeans de todos

---

## Semana 1 — Model + DAO (Dias 4–10)

**Objetivo:** Todas as entidades e DAOs em memória prontos e testados via `main()`.

### Tarefas detalhadas

#### Bruck

| Branch                    | Tarefa                                                         | Depende de    |
|---------------------------|----------------------------------------------------------------|---------------|
| `feat/model-recurso`      | Classe `Recurso` (abstrata): id, nome, status, dataCriacao, dono (Usuario), getResumo() abstrato | enums         |
| `feat/dao-interfaces`     | Interfaces `UsuarioDAO`, `RecursoDAO`, `MonitoramentoDAO`      | model-recurso |
| `feat/dao-usuario-memoria`| `UsuarioDAOMemoria` com HashMap static                         | dao-interfaces|

**Obs:** Bruck faz `Recurso` primeiro porque Maju e Marcelo dependem dele pra fazer as classes filhas. Priorizar essa entrega até o dia 5.

#### Maju

| Branch                    | Tarefa                                                         | Depende de     |
|---------------------------|----------------------------------------------------------------|----------------|
| `feat/model-usuario`      | Classe `Usuario`: id, nome, email, senha, nivelAcesso          | enums          |
| `feat/model-vm`           | Classe `VirtualMachine extends Recurso`: qtdCpu, memoriaRamGB, SO, iniciar(), parar(), getResumo() | model-recurso (Bruck) |
| `feat/dao-recurso-memoria`| `RecursoDAOMemoria` com HashMap static                         | dao-interfaces (Bruck)|

#### Marcelo

| Branch                    | Tarefa                                                         | Depende de     |
|---------------------------|----------------------------------------------------------------|----------------|
| `feat/model-armazenamento`| Classe `Armazenamento extends Recurso`: capacidadeGB, usadoGB, tipoDisco, vmAnexada (pode ser null), expandir(), reduzir(), getResumo() | model-recurso (Bruck) |
| `feat/model-monitoramento`| Classe `Monitoramento`: id, recurso, metrica, valor, timestamp, isCritico(), getValorFormatado() | model-recurso (Bruck) |
| `feat/dao-monit-memoria`  | `MonitoramentoDAOMemoria` com HashMap static                   | dao-interfaces (Bruck)|

### Ordem de merges na develop

```
1. feat/model-usuario        (Maju - sem dependência)
2. feat/model-recurso        (Bruck - depende dos enums)
3. feat/model-vm             (Maju - depende de Recurso)
4. feat/model-armazenamento  (Marcelo - depende de Recurso)
5. feat/model-monitoramento  (Marcelo - depende de Recurso)
6. feat/dao-interfaces       (Bruck)
7. feat/dao-usuario-memoria  (Bruck)
8. feat/dao-recurso-memoria  (Maju)
9. feat/dao-monit-memoria    (Marcelo)
```

### Marco de entrega (fim do dia 10)

- [ ] Todas as classes model compilam e têm getters/setters
- [ ] Todos os DAOs em memória implementam suas interfaces
- [ ] Existe um `main()` de teste que: cria usuários, cria VMs, cria storages, faz busca por id/email, lista todos, deleta, e printa os resultados
- [ ] Tudo mergeado na `develop`

---

## Semana 2 — Services + Tela de Login (Dias 11–17)

**Objetivo:** Camada de lógica de negócio pronta + primeira tela funcional (login).

### Tarefas detalhadas

#### Bruck

| Branch                       | Tarefa                                                              | Depende de |
|------------------------------|---------------------------------------------------------------------|------------|
| `feat/service-autenticacao`  | `AutenticacaoService`: login(email, senha), cadastrar(Usuario), temPermissao(Usuario, String). Login busca por email no DAO e compara senha. | DAOs prontos |
| `feat/service-recurso`       | `RecursoService`: criarVM(...), criarStorage(...), anexarDisco(Armazenamento, VM), desanexarDisco(Armazenamento), listarRecursos(Usuario), deletarRecurso(int id). Validações básicas (campos não-vazios, CPU > 0, etc). | DAOs prontos |

#### Maju

| Branch                  | Tarefa                                                                    | Depende de              |
|-------------------------|---------------------------------------------------------------------------|-------------------------|
| `feat/tela-login`       | `TelaLogin.java` (JFrame): campos email e senha, botão "Entrar", botão "Cadastrar". Chama `AutenticacaoService.login()`. Se sucesso → abre `TelaDashboard`. Se falha → JOptionPane de erro. | service-autenticacao (Bruck) |
| `feat/tela-cadastro`    | `TelaCadastro.java` (JFrame): form com nome, email, senha, confirmar senha. Chama `AutenticacaoService.cadastrar()`. Se sucesso → volta pra TelaLogin. | service-autenticacao (Bruck) |

**Obs:** Maju pode começar montando o layout visual das telas (arrastar componentes no NetBeans) enquanto espera o Service do Bruck. Quando o Service estiver pronto, ela só conecta os botões.

#### Marcelo

| Branch                  | Tarefa                                                                    | Depende de   |
|-------------------------|---------------------------------------------------------------------------|--------------|
| `feat/dados-mock`       | Classe `DadosMock.java`: método estático `inicializar()` que popula os DAOs com dados pré-criados. Pelo menos: 1 admin, 2 usuários comuns, 3 VMs (distribuídas entre os usuários), 2 storages (1 anexado numa VM, 1 solto). | DAOs prontos |
| `feat/service-monitoramento` | `MonitoramentoService`: gerarMetricasFake(Recurso) que cria 10 registros aleatórios (CPU 10-95%, RAM 20-80%), listarPorRecurso(int id), listarCriticos(). | DAOs prontos |

### Ordem de merges na develop

```
1. feat/service-autenticacao   (Bruck - desbloqueia telas da Maju)
2. feat/service-recurso        (Bruck)
3. feat/dados-mock             (Marcelo)
4. feat/service-monitoramento  (Marcelo)
5. feat/tela-login             (Maju)
6. feat/tela-cadastro          (Maju)
```

### Marco de entrega (fim do dia 17)

- [ ] Abrir o app → TelaLogin aparece
- [ ] Login com usuário mock (admin@cloud.com / 123) → abre TelaDashboard (pode estar vazia)
- [ ] Login com senha errada → mostra mensagem de erro
- [ ] Cadastrar novo usuário → volta pro login → consegue logar com ele
- [ ] DadosMock popula o sistema automaticamente ao iniciar

---

## Semana 3 — Telas de CRUD (Dias 18–24)

**Objetivo:** Sistema funcional completo — criar, listar, deletar VMs e storages, anexar disco.

### Tarefas detalhadas

#### Bruck

| Branch                       | Tarefa                                                            | Depende de        |
|------------------------------|-------------------------------------------------------------------|-------------------|
| `feat/tela-dashboard`        | `TelaDashboard.java` (JFrame principal): JTabbedPane com abas "Minhas VMs", "Meu Storage", "Monitoramento". Se o usuário logado for ADMIN, mostra aba extra "Usuários". Recebe o `Usuario` logado como parâmetro do construtor. | Telas semana 2    |
| `feat/tela-gerenciar-usuarios`| `TelaGerenciarUsuarios.java`: JTable listando todos os usuários + botões editar/deletar. Só admin acessa. | tela-dashboard    |

#### Maju

| Branch                    | Tarefa                                                              | Depende de      |
|---------------------------|---------------------------------------------------------------------|-----------------|
| `feat/painel-vms`         | `PainelVMs.java` (JPanel dentro da aba): JTable mostrando VMs do usuário (nome, CPU, RAM, status). Botões: "Criar VM", "Iniciar", "Parar", "Deletar". | tela-dashboard (Bruck) |
| `feat/dialog-criar-vm`    | `DialogCriarVM.java` (JDialog): form com nome, qtd CPU, RAM, SO. Chama `RecursoService.criarVM()`. Atualiza a JTable ao fechar. | painel-vms       |

#### Marcelo

| Branch                    | Tarefa                                                              | Depende de      |
|---------------------------|---------------------------------------------------------------------|-----------------|
| `feat/painel-storage`     | `PainelStorage.java` (JPanel dentro da aba): JTable mostrando storages do usuário (nome, capacidade, usado, tipo, VM anexada ou "livre"). Botões: "Criar Storage", "Anexar em VM", "Desanexar", "Deletar". | tela-dashboard (Bruck) |
| `feat/dialog-criar-storage`| `DialogCriarStorage.java` (JDialog): form com nome, capacidade, tipo disco. Opcionalmente já seleciona uma VM pra anexar (JComboBox com VMs do usuário). | painel-storage   |

### Ordem de merges na develop

```
1. feat/tela-dashboard                (Bruck - desbloqueia painéis)
2. feat/painel-vms                    (Maju)
3. feat/dialog-criar-vm               (Maju)
4. feat/painel-storage                (Marcelo)
5. feat/dialog-criar-storage          (Marcelo)
6. feat/tela-gerenciar-usuarios       (Bruck)
```

### Marco de entrega (fim do dia 24)

- [ ] Login → Dashboard com abas funcionando
- [ ] Criar VM → aparece na tabela → Iniciar/Parar muda status → Deletar remove
- [ ] Criar Storage → aparece na tabela → Anexar em VM → Desanexar → Deletar
- [ ] Admin vê aba "Usuários" → consegue listar/deletar
- [ ] Usuário comum NÃO vê aba "Usuários"

---

## Semana 4 — Monitoramento + Polish (Dias 25–31)

**Objetivo:** Monitoramento funcionando + sistema polido e apresentável.

### Tarefas detalhadas

#### Bruck

| Branch                    | Tarefa                                                                |
|---------------------------|-----------------------------------------------------------------------|
| `feat/painel-monitoramento`| `PainelMonitoramento.java`: selecionar um recurso (JComboBox) → gerar métricas fake → mostrar em JTable. Linhas com `isCritico() == true` ficam em vermelho. Botão "Gerar novas métricas". |
| `feat/bugs-finais`        | Testar tudo junto, corrigir bugs, merge final `develop → main`.       |

#### Maju

| Branch                    | Tarefa                                                                |
|---------------------------|-----------------------------------------------------------------------|
| `feat/polish-telas`       | Melhorar visual das telas: alinhar componentes, padronizar tamanhos de botões, ícones (se quiser), títulos nas janelas, cores consistentes. |
| `feat/validacoes-forms`   | Adicionar validações nos formulários: campo vazio, CPU negativa, email sem @, senha curta, capacidade de disco <= 0. Mostrar JOptionPane com mensagem clara. |

#### Marcelo

| Branch                    | Tarefa                                                                |
|---------------------------|-----------------------------------------------------------------------|
| `feat/mensagens-feedback` | Mensagens de sucesso/erro em todas as ações (criar, deletar, anexar, desanexar). JOptionPane consistente. |
| `feat/atualizar-diagramas`| Atualizar diagramas no Astah se algo mudou durante a implementação.   |
| (sem branch)              | Ajudar nos testes e bugs com o Bruck.                                 |

### Marco de entrega (fim do dia 31)

- [ ] Monitoramento funciona: seleciona recurso, gera métricas, mostra críticos em vermelho
- [ ] Todas as ações têm feedback (sucesso/erro)
- [ ] Formulários validam inputs inválidos
- [ ] Telas estão visualmente consistentes
- [ ] Diagramas refletem o código final
- [ ] `main` tem a versão final

---

## Dicas de sobrevivência

### Se alguém travar numa tarefa
1. Tenta por 30 min sozinho (lê o erro, pesquisa).
2. Manda no grupo com print do erro + o que já tentou.
3. Se não resolver em 1h, faz call rápida.

### Se uma dependência atrasar
Se o Bruck não entregou o Service e a Maju precisa dele pra tela:
- Maju monta o **layout visual** da tela (arrastar componentes no NetBeans).
- Deixa os botões com `ActionListener` vazio ou com `System.out.println("TODO: chamar service")`.
- Quando o Service mergear na develop, ela atualiza a branch dela e conecta.

### Sync semanal
No mínimo 1x por semana (pode ser na aula de POO2):
- Cada um mostra o que fez (compartilha tela 2 min).
- Identifica bloqueios.
- Ajusta o plano se necessário (edita esse documento!).

### Prioridade se faltar tempo

Se a semana 4 ficar apertada, corte nessa ordem (do menos ao mais importante):

1. ~~Monitoramento~~ → cortar inteiro se precisar
2. ~~TelaGerenciarUsuarios~~ → admin funciona sem isso
3. ~~Validações de formulário~~ → feio mas funciona
4. **CRUD de VM e Storage** → isso é o core, não cortar
5. **Login** → sem isso não tem projeto
