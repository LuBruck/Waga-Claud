# Roadmap — WagaClaud (versão Spring Boot)

> Documento interno de planejamento. Última atualização: 22/05/2026
> Pode (e deve) ser alterado conforme o projeto evolui.
> **Entrega final: 16/06/2026.** Início: 24/05/2026 (~3,5 semanas).

---

## Stack e decisões de arquitetura

| Item | Escolha |
|------|---------|
| Linguagem | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Build | Maven (`pom.xml`) — gerado pelo Spring Initializr |
| IDE | VS Code (+ "Extension Pack for Java" e "Spring Boot Extension Pack") |
| Banco (desenvolvimento) | PostgreSQL — database `wagaclaud_dev` (pode quebrar à vontade) |
| Banco (definitivo) | PostgreSQL — database `wagaclaud` (populado pra demonstração/entrega) |
| Front | HTML + CSS puro (gerado por IA), JavaScript `fetch` pra consumir a API |

### Camadas (a "regra de ouro" continua valendo)

O sistema mantém a separação em camadas do projeto Swing, só com nomes novos:

```
Front (HTML/CSS/JS)
   ↓ HTTP (fetch)
Controller (@RestController — expõe a API REST)
   ↓ chama
Service (@Service — lógica de negócio: validação, permissão, orquestração)
   ↓ usa (injetado via @Autowired)
Repository (interface extends JpaRepository — persistência)
   ↓ persiste
Model (@Entity — entidades JPA)
```

**Regra de ouro:** nenhuma camada pula a vizinha. O Controller NUNCA chama o Repository
direto — sempre passa pelo Service. O Service NUNCA monta JSON nem lida com HTTP — isso é
trabalho do Controller.

### Decisões JPA já fechadas

- **Herança:** `@Inheritance(strategy = InheritanceType.JOINED)` na classe `Recurso`.
  Vira uma tabela-mãe `recurso` + tabelas filhas `virtual_machine` e `armazenamento`
  ligadas por chave estrangeira. Modelo relacional normalizado (sem colunas NULL sobrando).
- **Repositories separados por entidade concreta** (não um genérico). Cada um pode ter suas
  queries próprias — ex: `findByVmAnexadaIsNull()` pra buscar discos soltos, que um
  repository genérico de `Recurso` não conseguiria declarar.
- **Sem DTO** nesta versão. Os Controllers retornam as entidades direto (decisão de
  simplicidade — pode evoluir depois se sobrar tempo).
- **Lombok nas entidades:** em vez de escrever getters/setters/construtores à mão, cada
  entidade usa as anotações do Lombok no topo da classe — tipicamente `@Getter`, `@Setter`,
  `@NoArgsConstructor` (o JPA exige um construtor sem argumentos). Isso enxuga muito o código.
  Onde o roadmap diz "Lombok gera getters/setters", é a isso que se refere.
- **Dois ambientes via profiles do Spring** (mesmo código, configs diferentes):
  - `application-dev.properties` → database `wagaclaud_dev`, `ddl-auto=update` (o JPA
    cria/ajusta as tabelas sozinho conforme as entidades mudam — cômodo durante o dev).
  - `application-prod.properties` → database `wagaclaud`, `ddl-auto=validate` (o JPA só
    confere que as tabelas batem com as entidades, sem mexer — o banco definitivo não se
    reconfigura sozinho).
  - Troca de ambiente: `--spring.profiles.active=dev` ou `prod` ao rodar. O Java não muda.
- **Dados iniciais:** classe `DataLoader` (implements `CommandLineRunner`) substitui o antigo
  `DadosMock`. Popula o banco ao subir o app.

> **Nota importante:** a camada Model (entidades, herança, relações) é praticamente igual
> à do projeto Swing. O que mudou foi tudo em volta. Quem entendeu o modelo antigo já
> entende 80% deste.

---

## Equipe e divisão por feature

Diferente do roadmap antigo (que dividia por camada), aqui cada um é dono de uma **feature
ponta-a-ponta**: faz Model → Repository → Service → Controller → ligação no front da sua
fatia. Isso ajuda no aprendizado (cada um vê o sistema inteiro funcionando) e é a forma
mais natural de dividir num time pequeno com prazo curto.

| Apelido | Feature (ponta-a-ponta) | Papel extra |
|---------|--------------------------|-------------|
| **Bruck** | **Autenticação** — `Usuario`, login, cadastro, permissão | Âncora: faz o setup inicial, entrega as classes-base (`Recurso`, `Usuario`), revisa PRs e cuida da integração final |
| **Maju** | **Recursos** — `VirtualMachine` + `Armazenamento`, criar/listar/deletar, anexar/desanexar disco | — |
| **Marcelo** | **Monitoramento** — `Monitoramento`, métricas fake, listar críticos | — |

### Dependência crítica entre as features

As features de Maju (Recursos) e Marcelo (Monitoramento) **dependem da classe `Recurso`**
(abstrata) e de `Usuario`, que são da feature do Bruck. Por isso o Bruck precisa entregar
essas duas classes-base **logo no início da Semana 1**, antes de tudo. Enquanto isso, Maju
e Marcelo adiantam o que dá (estrutura, enums, esqueleto das próprias classes).

Trabalho majoritariamente remoto, com syncs nas aulas de POO2.

---

## Workflow Git

Mantém o mesmo fluxo do projeto Swing — funcionou bem e a equipe já conhece.

### Branches

```
main          ← só código testado e funcional (nunca commita direto aqui)
  └── develop ← branch de integração (merges das features vão pra cá)
        ├── feat/auth-model-usuario
        ├── feat/recursos-vm
        ├── feat/monit-service
        └── ...
```

**Regras:**
- Ninguém commita direto na `main` nem na `develop`.
- Toda tarefa vira uma branch `feat/nome-da-feat` criada a partir da `develop`.
- Quando a feature tá pronta, abre um **Pull Request** (PR) para `develop`.
- Todo PR precisa de no mínimo **1 reviewer** (de preferência o Bruck).
- Depois que o reviewer aprova, o autor do PR faz o merge.
- A `main` só recebe merge da `develop` nos marcos de entrega (fim de cada semana).

### Convenção de nomes de branch

Como agora a divisão é por feature, vale prefixar a branch com a feature pra organizar:

```
feat/auth-...      → tarefas do Bruck (autenticação)
feat/recursos-...  → tarefas da Maju
feat/monit-...     → tarefas do Marcelo
feat/setup-...     → infra/configuração
feat/front-...     → telas e integração
```

### Padrão de commits

```
feat: criar entidade Usuario com anotacoes JPA
feat: adicionar endpoint POST /api/login
fix: corrigir findByEmail retornando Optional vazio
refactor: extrair validacao de senha pro service
```

### O que vai pro Git

VAI: `pom.xml`, `src/`, `docs/`, `README.md`, `ROADMAP.md`, `CLAUDE.md`, `.gitignore`,
os arquivos do front (`.html`, `.css`, `.js`)

NÃO VAI (.gitignore cuida): `target/`, `.vscode/` (configs pessoais), `*.class`,
`application-prod.properties` se tiver senha do banco real (usar variável de ambiente)

### Resolução de conflitos

Se o `git pull` ou o PR acusar conflito:
1. **Não entre em pânico.** Leia a mensagem do Git.
2. Abra o arquivo marcado — o Git mostra `<<<<<<< HEAD` e `>>>>>>>` indicando as duas versões.
3. Escolha qual versão manter (ou combine as duas).
4. Salve, `git add`, `git commit`.
5. Se travou, chama o Bruck.

---

## Estrutura de pacotes (Spring Boot / Maven)

Package raiz: `com.wagaclaud`. Estrutura gerada pelo Spring Initializr e organizada por camada.

```
WagaClaud/                              ← raiz do repositório
  ├── src/
  │     ├── main/
  │     │     ├── java/
  │     │     │     └── com/wagaclaud/
  │     │     │           ├── WagaCloudApplication.java   ← @SpringBootApplication (main)
  │     │     │           ├── model/
  │     │     │           │     ├── enums/
  │     │     │           │     │     ├── NivelAcesso.java
  │     │     │           │     │     ├── StatusRecurso.java
  │     │     │           │     │     └── TipoDisco.java
  │     │     │           │     ├── Usuario.java          (@Entity)
  │     │     │           │     ├── Recurso.java          (@Entity, abstract, JOINED)
  │     │     │           │     ├── VirtualMachine.java   (@Entity, extends Recurso)
  │     │     │           │     ├── Armazenamento.java    (@Entity, extends Recurso)
  │     │     │           │     └── Monitoramento.java    (@Entity)
  │     │     │           ├── repository/
  │     │     │           │     ├── UsuarioRepository.java          (interface)
  │     │     │           │     ├── VirtualMachineRepository.java   (interface)
  │     │     │           │     ├── ArmazenamentoRepository.java    (interface)
  │     │     │           │     └── MonitoramentoRepository.java    (interface)
  │     │     │           ├── service/
  │     │     │           │     ├── AutenticacaoService.java
  │     │     │           │     ├── RecursoService.java
  │     │     │           │     └── MonitoramentoService.java
  │     │     │           ├── controller/
  │     │     │           │     ├── AuthController.java
  │     │     │           │     ├── RecursoController.java
  │     │     │           │     └── MonitoramentoController.java
  │     │     │           └── config/
  │     │     │                 └── DataLoader.java       ← popula dados iniciais
  │     │     └── resources/
  │     │           ├── application.properties            ← config comum
  │     │           ├── application-dev.properties         ← H2 (desenvolvimento)
  │     │           ├── application-prod.properties        ← PostgreSQL (produção)
  │     │           └── static/                            ← FRONT mora aqui
  │     │                 ├── index.html
  │     │                 ├── css/
  │     │                 └── js/
  │     └── test/                                          ← testes (opcional p/ este projeto)
  ├── docs/                              ← diagramas (.asta), imagens
  ├── target/                           ← build do Maven (NÃO vai pro Git)
  ├── pom.xml                           ← config Maven + dependências
  ├── README.md
  ├── ROADMAP.md                        ← este arquivo
  ├── CLAUDE.md
  └── .gitignore
```

### Por que o front fica em `resources/static/`?

O Spring Boot serve automaticamente qualquer arquivo dentro de `static/` direto pela web.
Coloca o `index.html` lá e ele aparece em `http://localhost:8080/`. O JavaScript desse HTML
chama os endpoints (`fetch('/api/recursos')`) — front e back rodam no **mesmo servidor**,
o que mata o problema de CORS e simplifica muito a integração. É a opção mais simples pra
um projeto de faculdade.

### Dependências do `pom.xml` (escolhidas no Spring Initializr)

- **Spring Web** — pra criar os `@RestController`
- **Spring Data JPA** — pros Repositories e o mapeamento das entidades (o Hibernate já vem
  embutido aqui — é ele que implementa o JPA por baixo, não precisa declarar à parte)
- **PostgreSQL Driver** — pra conectar no banco
- **Lombok** — gera getters, setters, construtores e mais via anotações (`@Getter`,
  `@Setter`, `@NoArgsConstructor`, etc.), eliminando código repetitivo nas entidades
- **Spring Boot DevTools** (opcional) — reinicia o app sozinho quando você salva (acelera o dev)

> **Setup do Lombok no VS Code:** além da dependência no `pom.xml`, instale a extensão
> "Lombok Annotations Support for VS Code" — sem ela, o editor reclama que os getters/setters
> "não existem" (mesmo o código compilando), porque o Lombok os gera só na compilação.

---

## Semana 0 — Setup (24/05 – 26/05)

**Objetivo:** todo mundo com o ambiente rodando, repo clonado, projeto Spring Boot subindo
em `localhost:8080`, e a estrutura de pacotes criada. Ninguém escreve lógica ainda — é só
deixar o terreno pronto.

### O que fazer

1. **Bruck** gera o projeto no Spring Initializr (`start.spring.io`):
   - Project: **Maven** · Language: **Java** · Spring Boot: **3.x** · Java: **21**
   - Group: `com.wagaclaud` · Artifact: `wagaclaud`
   - Dependências: **Spring Web**, **Spring Data JPA**, **PostgreSQL Driver**, **Lombok**,
     **Spring Boot DevTools**
   - Baixa o `.zip`, descompacta, cria a estrutura de pacotes (model, repository, service,
     controller, config), configura os profiles `dev` e `prod`, e sobe tudo pro GitHub com
     `main` e `develop` criadas.
2. **Maju e Marcelo** instalam o **JDK 21**, o **VS Code** + extensões ("Extension Pack for
   Java", "Spring Boot Extension Pack" e "Lombok Annotations Support"), clonam o repo e abrem
   no VS Code.
3. **Cada um** instala o **PostgreSQL** na própria máquina e cria os dois databases:
   `wagaclaud_dev` (pra desenvolver) e `wagaclaud` (definitivo). Pode usar o pgAdmin (vem
   junto) ou o comando `CREATE DATABASE wagaclaud_dev;`.
4. **Cada um** roda `mvn spring-boot:run` (perfil dev) e confirma que o app sobe conectando
   no PostgreSQL e que `http://localhost:8080` responde (mesmo que seja a página de erro
   padrão do Spring — o que importa é o servidor estar de pé E conectado no banco).
5. **Bruck** faz um mini-tutorial rápido pros dois: como rodar o projeto no VS Code, onde
   ficam os pacotes, e o fluxo de Git (branch → commit → push → PR). Pode ser por call.
6. **Qualquer um** cria os 3 enums (`NivelAcesso`, `StatusRecurso`, `TipoDisco`) como
   primeira tarefa prática — serve de treino pra abrir a primeira branch e o primeiro PR.
   Enums não mudam em relação ao projeto antigo.

### Branches dessa fase

| Branch | Quem | O que faz |
|--------|------|-----------|
| `feat/setup-projeto` | Bruck | Projeto do Initializr + estrutura de pacotes + `.gitignore` + profiles `dev`/`prod` + README inicial |
| `feat/setup-enums` | Qualquer | `NivelAcesso`, `StatusRecurso`, `TipoDisco` |

### Configuração dos profiles PostgreSQL

`application-dev.properties` (banco de desenvolvimento):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wagaclaud_dev
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_LOCAL
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

`application-prod.properties` (banco definitivo):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wagaclaud
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_LOCAL
spring.jpa.hibernate.ddl-auto=validate
```

> `ddl-auto=update` no dev deixa o JPA criar/ajustar as tabelas conforme você muda as
> entidades — cômodo enquanto o modelo ainda está mudando. No definitivo, `validate` só
> confere que tudo bate, sem mexer no banco.
> **Atenção à senha:** não commite a senha real. Cada um usa a senha do seu PostgreSQL local;
> pra entrega, pode usar variável de ambiente ou deixar documentado no README como configurar.

### Marco de entrega (fim do dia 26/05)

- [ ] Todos clonaram e fizeram pelo menos 1 commit + PR
- [ ] PostgreSQL instalado e os dois databases criados em todas as máquinas
- [ ] `mvn spring-boot:run` sobe o app conectando no PostgreSQL na máquina de todo mundo
- [ ] `localhost:8080` responde em todas as máquinas
- [ ] Os 3 enums estão na `develop`

---

## Semana 1 — Fundação + features começando (27/05 – 02/06)

**Objetivo:** Model + Repository + Service das três features prontos e testados. Ao fim da
semana, a lógica de negócio inteira funciona — falta só expor via REST (Controller) e ligar
no front, que é a Semana 2.

**Ritmo agressivo de propósito:** como o prazo é curto, empilhamos as 3 camadas internas
nesta semana. Por isso a ordem importa muito — siga a dependência abaixo.

### A dependência crítica (leia antes de começar)

`Recurso` (abstrata) e `Usuario` são a base de tudo. O **Bruck entrega essas duas primeiro**
(meta: até 28/05). Enquanto isso, Maju e Marcelo adiantam o que não depende delas. Assim que
caírem na `develop`, todos seguem em paralelo.

```
Bruck: Usuario + Recurso  ──┬──► Maju: VirtualMachine + Armazenamento
   (entregar até 28/05)     └──► Marcelo: Monitoramento
```

### Bruck — feature Autenticação (+ classes-base)

| Branch | Tarefa | Depende de |
|--------|--------|------------|
| `feat/auth-model-usuario` | `Usuario` (@Entity + Lombok): id, nome, email, senha, nivelAcesso (@Enumerated) | enums |
| `feat/auth-model-recurso` | `Recurso` (@Entity, abstract, @Inheritance JOINED): id, nome, status, dataCriacao, dono (@ManyToOne Usuario), abstract getResumo() | enums |
| `feat/auth-repository` | `UsuarioRepository extends JpaRepository<Usuario, Integer>` com `findByEmail` | model-usuario |
| `feat/auth-service` | `AutenticacaoService` (@Service): login(email, senha), cadastrar(Usuario), temPermissao(Usuario, acao). Recebe o repository via @Autowired. | auth-repository |

> **Prioridade:** os dois primeiros (`model-usuario` e `model-recurso`) vêm antes de tudo —
> são o que destrava Maju e Marcelo. Repository e Service do Bruck vêm depois.

### Maju — feature Recursos

| Branch | Tarefa | Depende de |
|--------|--------|------------|
| `feat/recursos-model-vm` | `VirtualMachine extends Recurso` (@Entity): qtdCpu, memoriaRamGB, SO, @OneToMany discos, iniciar(), parar(), getResumo() | model-recurso (Bruck) |
| `feat/recursos-model-armazenamento` | `Armazenamento extends Recurso` (@Entity): capacidadeGB, usadoGB, tipoDisco, @ManyToOne vmAnexada, expandir(), reduzir(), getResumo() | model-recurso (Bruck) |
| `feat/recursos-repository` | `VirtualMachineRepository` (findByDono, findByStatus) + `ArmazenamentoRepository` (findByDono, findByVmAnexadaIsNull) | os 2 models acima |
| `feat/recursos-service` | `RecursoService` (@Service): criarVM (já cria o disco padrão), criarStorage, anexarDisco, desanexarDisco, listarRecursos, deletarRecurso. @Autowired dos 2 repositories. Validações básicas. | recursos-repository |

> **Enquanto espera o Bruck:** Maju pode estudar como funciona herança no JPA e deixar o
> esqueleto das classes pronto (atributos + anotações Lombok) — só falta o `extends Recurso`
> compilar quando a base chegar.

### Marcelo — feature Monitoramento

| Branch | Tarefa | Depende de |
|--------|--------|------------|
| `feat/monit-model` | `Monitoramento` (@Entity): id, recurso (@ManyToOne), metrica, valor, timestamp (LocalDateTime), isCritico(), getValorFormatado() | model-recurso (Bruck) |
| `feat/monit-repository` | `MonitoramentoRepository`: findByRecursoId, findByValorGreaterThan | monit-model |
| `feat/monit-service` | `MonitoramentoService` (@Service): gerarMetricasFake(Recurso) cria ~10 registros aleatórios (CPU 10-95%, RAM 20-80%), listarPorRecurso, listarCriticos. @Autowired do repository. | monit-repository |
| `feat/setup-dataloader` | `DataLoader` (implements CommandLineRunner): popula o banco ao subir — 1 admin (admin@cloud.com / 123), 2 usuários comuns, 3 VMs (cada uma com disco padrão), 1 disco solto. Substitui o antigo DadosMock. | services prontos |

> **Enquanto espera o Bruck:** Marcelo pode adiantar o estudo do `CommandLineRunner` e
> rascunhar quais dados o `DataLoader` vai inserir.

### Ordem de merges na develop

```
1. feat/auth-model-usuario          (Bruck)
2. feat/auth-model-recurso          (Bruck — destrava Maju e Marcelo)
3. feat/recursos-model-vm           (Maju)
4. feat/recursos-model-armazenamento(Maju)
5. feat/monit-model                 (Marcelo)
6. feat/auth-repository             (Bruck)
7. feat/recursos-repository         (Maju)
8. feat/monit-repository            (Marcelo)
9. feat/auth-service                (Bruck)
10. feat/recursos-service           (Maju)
11. feat/monit-service              (Marcelo)
12. feat/setup-dataloader           (Marcelo — por último, precisa dos services)
```

### Como testar sem Controller ainda

Como ainda não tem API, dá pra validar a lógica de dois jeitos:
1. Subir o app (perfil dev) e olhar as tabelas no **pgAdmin** (ou outro cliente PostgreSQL)
   — confirmar que as tabelas `recurso`, `virtual_machine`, `armazenamento`, `usuario`,
   `monitoramento` foram criadas e que o `DataLoader` inseriu os dados.
2. Colocar logs temporários no `DataLoader` (ex: `System.out.println` chamando
   `recursoService.listarRecursos(...)`) pra ver os Services respondendo.

### Marco de entrega (fim do dia 02/06)

- [ ] Todas as entidades JPA mapeadas — tabelas criadas corretamente no PostgreSQL
- [ ] Herança JOINED funcionando (tabela `recurso` + filhas ligadas por FK)
- [ ] Os 4 repositories declarados e injetáveis
- [ ] Os 3 services com a lógica de negócio implementada
- [ ] `DataLoader` popula o banco ao iniciar o app
- [ ] Dá pra confirmar os dados no pgAdmin

---

## Semana 2 — API REST completa (03/06 – 09/06)

**Objetivo:** expor as três features via `@RestController`. Ao fim da semana, a API inteira
responde JSON e foi testada — sem front ainda. O front só começa quando TODO o back estiver
pronto e testado (decisão da equipe: evita ligar tela em endpoint que não existe).

Cada um expõe a própria feature. Como os Services já estão prontos (Semana 1), o Controller
é uma camada fina: recebe o request, chama o Service, devolve o resultado. Pouca lógica nova
— a parte difícil já foi feita.

### Bruck — AuthController

| Branch | Endpoints | Depende de |
|--------|-----------|------------|
| `feat/auth-controller` | `POST /api/login` (recebe email+senha, chama AutenticacaoService.login) · `POST /api/cadastro` (chama cadastrar) | auth-service |

### Maju — RecursoController

| Branch | Endpoints | Depende de |
|--------|-----------|------------|
| `feat/recursos-controller` | `GET /api/recursos` (lista do usuário) · `POST /api/vms` (criarVM) · `POST /api/storages` (criarStorage) · `PUT /api/discos/anexar` · `PUT /api/discos/desanexar` · `DELETE /api/recursos/{id}` | recursos-service |

### Marcelo — MonitoramentoController

| Branch | Endpoints | Depende de |
|--------|-----------|------------|
| `feat/monit-controller` | `GET /api/monitoramento/{recursoId}` (lista métricas do recurso) · `POST /api/monitoramento/gerar` (gera métricas fake) · `GET /api/monitoramento/criticos` | monit-service |

### Como testar cada endpoint (sem front)

Use a extensão **REST Client** do VS Code (cria um arquivo `.http` e dispara requests direto
do editor) ou o navegador pros endpoints `GET`. Exemplo de teste:

```http
### Testar login
POST http://localhost:8080/api/login
Content-Type: application/json

{ "email": "admin@cloud.com", "senha": "123" }

### Listar recursos (o usuarioId vem do login — ver nota abaixo)
GET http://localhost:8080/api/recursos?usuarioId=1
```

**Regra da semana:** um endpoint só é considerado "pronto" quando alguém disparou o request
e viu o JSON correto voltar. Anota os endpoints testados num checklist no grupo.

### Ordem de merges na develop

```
1. feat/auth-controller    (Bruck)
2. feat/recursos-controller(Maju)
3. feat/monit-controller   (Marcelo)
```

(Sem dependência entre eles — cada Controller usa só o próprio Service, então podem ir em
paralelo e mergear em qualquer ordem.)

### Detalhe técnico: como o Controller sabe quem é o usuário logado?

Sem login com sessão/token de verdade (que seria complexo demais pro prazo), a abordagem
mais simples é o front **mandar o id do usuário logado** em cada request que precisa
(ex: `GET /api/recursos?usuarioId=1`). O front guarda esse id depois do login. Não é seguro
pra produção real, mas é suficiente e honesto pra um projeto de faculdade — vale anotar essa
limitação no README.

### Marco de entrega (fim do dia 09/06)

- [ ] `POST /api/login` retorna o usuário (ou erro) — testado
- [ ] `POST /api/cadastro` cria usuário — testado
- [ ] CRUD de VM e Storage funcionando via API — testado
- [ ] Anexar/desanexar disco via API — testado
- [ ] Endpoints de monitoramento (gerar, listar, críticos) — testados
- [ ] Toda a API documentada num arquivo `.http` commitado (serve de "manual" pro front)

---

## Semana 3 — Front, integração e entrega (10/06 – 16/06)

**Objetivo:** ligar tudo. A API inteira já funciona (Semana 2) e o banco já é PostgreSQL
desde o início, então esta semana é sobre dar **cara** ao sistema: telas HTML/CSS conectadas
aos endpoints, polish, e a entrega final no banco definitivo.

### Front — quem faz o quê

O **Bruck centraliza o front** (estrutura das telas, CSS comum, organização dos arquivos em
`resources/static/`), mas **decide o visual junto com Maju e Marcelo** — cada um opina e
ajuda a ligar a tela da sua própria feature. As telas HTML/CSS são geradas com auxílio de IA;
o trabalho real da equipe é o **JavaScript de integração** (`fetch` chamando a API e
mostrando o resultado na tela).

| Branch | Tela / responsável | Liga nos endpoints |
|--------|--------------------|--------------------|
| `feat/front-base` | Bruck — estrutura: `index.html`, CSS comum, navegação entre telas, guardar o id do usuário logado após o login | — |
| `feat/front-login` | Bruck — tela de login + cadastro | `POST /api/login`, `POST /api/cadastro` |
| `feat/front-recursos` | Maju — tela de VMs e Storage: listar, criar, deletar, anexar/desanexar disco | endpoints de `/api/recursos`, `/api/vms`, `/api/storages`, `/api/discos/*` |
| `feat/front-monitoramento` | Marcelo — tela de monitoramento: selecionar recurso, gerar métricas, listar críticos (em vermelho) | endpoints de `/api/monitoramento/*` |

> **Padrão de integração:** cada tela tem um `.js` que usa `fetch('/api/...')`, pega o JSON,
> e monta o HTML (tabelas, listas) com o resultado. Front e back rodam no mesmo servidor
> (`localhost:8080`), então não tem CORS pra resolver. O arquivo `.http` da Semana 2 serve de
> manual: cada endpoint testado lá vira um `fetch` aqui.

### Polish e robustez (todos, em paralelo com o front)

| Branch | Quem | Tarefa |
|--------|------|--------|
| `feat/front-validacoes` | Maju | Validações nos formulários do front: campo vazio, CPU negativa, email sem @, senha curta, capacidade ≤ 0. Mostrar mensagem clara antes de mandar pro back. |
| `feat/front-feedback` | Marcelo | Mensagens de sucesso/erro em todas as ações (criar, deletar, anexar, desanexar). Tratamento dos erros que a API retornar. |
| `feat/polish-visual` | Bruck (com opinião dos outros) | Padronizar visual: alinhamento, cores consistentes, títulos, deixar apresentável. |

### Banco definitivo + entrega final (Bruck)

| Branch | Tarefa |
|--------|--------|
| `feat/entrega-banco-definitivo` | Rodar o app com o perfil `prod` (database `wagaclaud`), confirmar que o `DataLoader` popula o banco definitivo e que tudo funciona igual ao dev. Conferir que a herança JOINED gerou as tabelas certas (`recurso` + filhas com FK). |
| `feat/bugs-finais` | Testar o fluxo completo de ponta a ponta, corrigir bugs, e fazer o merge final `develop → main`. |

### Ordem sugerida da semana

```
10–11/06  Front base + login (Bruck)  ·  Maju e Marcelo já ligando suas telas
12–13/06  Telas de recursos e monitoramento ligadas e funcionando
14/06     Validações + feedback + polish visual
15/06     Testar tudo no banco definitivo (wagaclaud), corrigir bugs
16/06     Merge final develop → main · ENTREGA
```

### Marco de entrega (fim do dia 16/06)

- [ ] Login pelo front → guarda o usuário → navega pras telas
- [ ] Criar/listar/deletar VM e Storage pela tela → reflete no banco
- [ ] Anexar/desanexar disco pela tela
- [ ] Monitoramento: gerar métricas, ver críticos em vermelho
- [ ] Admin vê funcionalidade extra; usuário comum não (permissão)
- [ ] Validações e mensagens de feedback funcionando
- [ ] Sistema roda no banco **definitivo** (`wagaclaud`) com dados persistindo de verdade
- [ ] `main` tem a versão final, com README explicando como rodar (incluindo configurar o banco)

---

## Dicas de sobrevivência

### Como ninguém sabe Spring — minimizar o atrito

- O fluxo Controller → Service → Repository é sempre o mesmo padrão. Quando uma feature
  funcionar ponta-a-ponta (a do Bruck, na Semana 1-2), ela vira o **molde**: as outras duas
  copiam a estrutura e trocam só a entidade. Não reinvente — copie o que funcionou.
- Erro do tipo "tabela não existe" ou "coluna não bate" quase sempre é anotação JPA faltando
  ou `ddl-auto` errado. Confira o profile ativo antes de entrar em pânico.
- `@Autowired` não funcionou (deu null)? Quase sempre é porque a classe não tem `@Service`,
  `@RestController` ou `@Repository`, ou está fora do pacote `com.wagaclaud`.

### Se alguém travar numa tarefa

1. Tenta por 30 min sozinho (lê o erro completo, pesquisa a mensagem exata).
2. Manda no grupo com print do erro + o que já tentou.
3. Se não resolver em 1h, faz call rápida com o Bruck.

### Se uma dependência atrasar

Se o Bruck não entregou `Recurso`/`Usuario` e Maju/Marcelo precisam:
- Adiantam o estudo e o esqueleto das próprias classes (atributos + anotações Lombok).
- Deixam o `extends Recurso` comentado até a base chegar.
- Quando a base mergear na develop, atualizam a branch e descomentam.

### Sync semanal

No mínimo 1x por semana (pode ser na aula de POO2):
- Cada um mostra o que fez (compartilha tela 2 min).
- Identifica bloqueios.
- Ajusta o plano se necessário (edita esse documento!).

---

## Prioridade se faltar tempo

Se a Semana 3 apertar, corte nessa ordem (do menos ao mais importante):

1. ~~Polish visual~~ → feio mas funciona
2. ~~Validações de formulário no front~~ → o back ainda valida o essencial
3. ~~Monitoramento~~ → corta a feature inteira do Marcelo se precisar (ele realoca pro front)
4. ~~Mensagens de feedback elaboradas~~ → um `alert()` simples resolve
5. **CRUD de VM e Storage** → isso é o core, não cortar
6. **Login + cadastro** → sem isso não tem como demonstrar o resto
7. **Banco SQL (PostgreSQL) funcionando** → requisito OBRIGATÓRIO, nunca cortar

> O que **nunca** sai: o sistema rodando sobre PostgreSQL com as entidades persistindo de
> verdade. É o requisito da disciplina. Todo o resto é negociável; isso não.
