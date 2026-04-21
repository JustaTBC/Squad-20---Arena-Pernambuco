# Arena Pernambuco — Especificação Completa de Redesign e Novas Features

**Data:** 2026-04-20  
**Projeto:** Squad 20 — Arena Pernambuco  
**Stack:** Java 17 + Spring Boot 3.4.4 + Thymeleaf + Maven  
**Abordagem:** Mock-first (dados em memória), Firebase plugável depois

---

## 1. Arquitetura de Pacotes e Camadas

```
src/main/java/com/arenapernambuco/
├── ArenaPernambucoApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── FirebaseConfig.java          (Tarefa futura — Firebase)
├── controller/
│   ├── PortalController.java        GET /
│   ├── EventoController.java        GET /eventos, /eventos/{id}
│   ├── VerificacaoController.java   GET/POST /verificar
│   └── AdminController.java         /admin/** (futuro)
├── dto/
│   ├── EventoDTO.java
│   └── EventoFiltroDTO.java
├── exception/
│   ├── EventoNaoEncontradoException.java
│   └── GlobalExceptionHandler.java
├── model/
│   └── Evento.java
├── repository/
│   ├── EventoRepository.java        Interface
│   ├── EventoMemoryRepository.java  Implementação mock
│   └── EventoFirebaseRepository.java (Tarefa futura — Firebase)
└── service/
    └── EventoService.java
```

### Princípios

- **Repository Pattern:** Interface `EventoRepository` com implementação `EventoMemoryRepository`. Ao plugar Firebase, basta criar `EventoFirebaseRepository` e ativar via profile — zero mudança em controllers/services.
- **DTO Layer:** Controllers nunca expõem o model diretamente aos templates.
- **Controller split:** Cada controller tem responsabilidade única (portal, eventos, verificação, admin).
- **@ControllerAdvice:** Tratamento de erros centralizado, sem stacktrace para o usuário.

---

## 2. Spring Security e Controle de Acesso

### Dependência

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### Papéis

| Papel | Acesso | Login necessário? |
|---|---|---|
| VISITANTE | `/`, `/eventos`, `/eventos/{id}` | Não (anônimo) |
| PARTICIPANTE | Tudo do visitante + `/verificar` | Sim |
| ADMIN | Tudo + `/admin/**` | Sim |

### Regras de Rota

```
Rotas públicas (permitAll):
  /, /eventos, /eventos/*, /css/**, /js/**, /images/**

Rotas autenticadas:
  /verificar       → PARTICIPANTE ou ADMIN
  /admin/**        → somente ADMIN

Login:
  Formulário customizado em /login
  Redirect após login: página anterior ou /eventos
  Logout: POST /logout → redireciona para /

CSRF: habilitado (padrão Spring Security)
```

### Usuários in-memory (dev)

| Usuário | Senha | Papel |
|---|---|---|
| participante@arena.com | senha123 | ROLE_PARTICIPANTE |
| admin@arena.com | admin123 | ROLE_ADMIN |

Senhas com `BCryptPasswordEncoder`.

### Fluxo do Portal

1. Usuário acessa `/` → vê 3 cards (Visitante, Participante, Admin)
2. "Visitante" → redireciona para `/eventos`
3. "Participante" → redireciona para `/login?papel=participante`
4. "Administrador" → redireciona para `/login?papel=admin`
5. Após login → redireciona para rota apropriada

---

## 3. Frontend / UI — Design System

### Paleta de Cores (CSS Variables)

```css
--bg-primary:    #0a0a0f;
--bg-card:       #13131a;
--bg-card-hover: #1a1a25;
--accent:        #FF6B35;
--accent-hover:  #ff8555;
--secondary:     #7C3AED;
--secondary-hover: #9461f7;
--text-primary:  #FFFFFF;
--text-muted:    #9ca3af;
--border:        #2a2a3a;
--success:       #22c55e;
--danger:        #ef4444;
```

### Tipografia (Google Fonts via CDN)

- `Bebas Neue` — títulos, hero, headings
- `Inter` — corpo, labels, parágrafos

### Bibliotecas JS via CDN

| Biblioteca | Uso |
|---|---|
| AOS | Cards e seções com fade-up ao scroll |
| Typed.js | Hero: efeito de digitação ("Esportes... Shows... Cultura...") |
| tsParticles | Fundo do portal/hero com partículas conectadas |
| GSAP | Transição fade-in ao carregar, hover effects |
| Lenis | Smooth scroll global |
| Barba.js | Transições suaves entre páginas |

### Templates

| Template | Descrição |
|---|---|
| `portal.html` | Hero fullscreen + tsParticles + 3 cards com GSAP hover + Typed.js |
| `login.html` | Formulário glassmorphism sobre partículas |
| `eventos.html` | Filtros sticky + grid de cards com AOS fade-up escalonado + badges |
| `evento-detalhe.html` | Hero com imagem + overlay gradient, conteúdo abaixo |
| `verificar.html` | Card central com formulário, resultado animado |
| `erro-404.html` | Mensagem amigável + CTA voltar |
| `erro-403.html` | Acesso negado + CTA login |
| `erro-500.html` | Erro genérico + CTA home |
| `fragments.html` | Header fixo (transparente → sólido ao scroll), nav, footer |

### Responsividade

- Mobile-first: grid 1 coluna mobile, 2-3 colunas desktop
- Cards do portal empilham verticalmente em mobile
- Header: hamburger menu em telas < 768px
- Fontes escalam com `clamp()`

### Inspirações Externas

- **uiverse.io:** Botões glow/pulse, inputs estilizados, cards com borda luminosa, spinners (HTML+CSS puro, diretamente utilizável)
- **magicui.design:** Gradient text animation (replicar em CSS)
- **animate-ui.com:** Spotlight/glow cursor effect (replicar em vanilla JS)
- **reactbits.dev:** Shimmer loading states (replicar em CSS)

### Referências Visuais Aprovadas

- Ref 1 (e-sports): Cards com overlay escuro + borda brilhante no hover
- Ref 2 (SportHead): Tipografia bold e impactante
- Ref 3 (UGC Esports): Partículas de fundo, stats com contadores animados, CTA vibrante

---

## 4. Model e Dados

### Evento.java (record)

```java
public record Evento(
    String id,
    String titulo,
    LocalDateTime dataHora,
    String categoria,
    String codigoVerificacao,
    String descricaoCurta,
    String descricaoCompleta,
    String imagemUrl,
    boolean ativo
)
```

### EventoDTO.java

```java
public record EventoDTO(
    String id,
    String titulo,
    String dataFormatada,
    String categoria,
    String descricaoCurta,
    String imagemUrl,
    String badgeCor
)
```

### EventoFiltroDTO.java

```java
public record EventoFiltroDTO(
    String categoria,
    LocalDate data,
    String ordem
)
```

### Categorias e Cores

| Categoria | Cor |
|---|---|
| Futebol | `#22c55e` |
| Música | `#FF6B35` |
| Corporativo | `#3b82f6` |
| Cultural | `#7C3AED` |
| Teatro | `#ec4899` |

### EventoRepository (interface)

```java
public interface EventoRepository {
    List<Evento> buscarTodos();
    List<Evento> buscarAtivos();
    Optional<Evento> buscarPorId(String id);
    Optional<Evento> buscarPorCodigo(String codigo);
    List<Evento> filtrar(EventoFiltroDTO filtro);
}
```

### EventoService

- Recebe `EventoRepository` por injeção
- Converte `Evento` → `EventoDTO` (formatação de data, cor do badge)
- `filtrar(EventoFiltroDTO)` com lógica de filtro/ordenação
- `buscarDetalhePorId(String id)` → lança `EventoNaoEncontradoException` se não encontrado

### Dados Mock

8-10 eventos cobrindo todas as categorias, datas futuras variadas, imagens via placeholder, descrições completas.

---

## 5. Tratamento de Erros e Segurança

### GlobalExceptionHandler

| Exceção | HTTP | Template |
|---|---|---|
| `EventoNaoEncontradoException` | 404 | `erro-404.html` |
| `AccessDeniedException` | 403 | `erro-403.html` |
| `Exception` (genérica) | 500 | `erro-500.html` |

- Nunca expor stacktrace, nome de classe ou detalhes internos
- Páginas de erro amigáveis com CTA para voltar
- Log da exceção real no server (slf4j)

### Validação de Inputs (Borda)

| Input | Onde | Validação |
|---|---|---|
| `codigo` | `VerificacaoController` | trim, não-vazio, máx 20 chars, regex alfanumérico + hífen |
| `id` | `EventoController` | não-nulo, formato válido |
| `categoria` | `EventoController` | whitelist de categorias válidas |
| `data` | `EventoController` | parse seguro com fallback null |
| `ordem` | `EventoController` | whitelist ("proximos", "recentes"), default "proximos" |

### Spring Security — Proteções Automáticas

- CSRF habilitado (token via `th:action` em Thymeleaf)
- Session fixation protection
- Headers: X-Frame-Options, X-Content-Type-Options, etc.
- Senhas com BCrypt

### .gitignore

```
*.env
*-credentials.json
firebase-adminsdk*.json
application-local.properties
```

---

## 6. Ordem de Implementação

1. **Tarefa 1** — Portal de entrada + Spring Security (já com design final)
2. **Tarefa 3** — Completar H1, H2, H3 (filtros, detalhe do evento, já com design final)
3. **Tarefa 4** — Redesign dos templates remanescentes (index, fragments, verificar)
4. **Tarefa 2** — Firebase Realtime Database (após fornecimento de credenciais)
5. **Tarefa 5** — Limpeza, testes, security audit

---

## Decisões Técnicas

- **Thymeleaf mantido** — sem migração para SPA/React
- **Bibliotecas frontend apenas vanilla JS via CDN** — zero build frontend
- **Repository Pattern** — permite trocar mock por Firebase sem refatoração
- **Dados mock-first** — desenvolvimento imediato sem bloqueio por credenciais
- **uiverse.io** como fonte de componentes HTML+CSS reutilizáveis
