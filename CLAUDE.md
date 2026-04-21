# Arena Pernambuco — SQUAD 20

## Contexto do Projeto

Sistema de gestão e divulgação de eventos para a Arena Pernambuco.
Tema: Governo / Setor Público, Cidades Inteligentes, Inovação / Tecnologia.

**Problema:** Baixa ocupação da Arena Pernambuco por falta de comunicação entre cidadãos, organizadores e administração.

**Objetivo:** Aplicação web que conecte pessoas interessadas a eventos com a programação da Arena.

## Stack Tecnológica

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3.4.4
- **Template Engine:** Thymeleaf
- **Build:** Maven
- **Servidor:** Porta 8080
- **Banco de dados:** Firebase Realtime Database (implementado — perfil `firebase`)
- **Segurança:** Spring Security com 3 papéis: anônimo, PARTICIPANTE, ADMIN

## Estrutura do Projeto

```
src/main/java/com/arenapernambuco/
├── ArenaPernambucoApplication.java
├── config/
│   ├── SecurityConfig.java           # Spring Security — roles, rotas protegidas, login customizado
│   ├── FirebaseConfig.java           # Inicializa FirebaseApp via classpath, expõe DatabaseReference bean
│   └── FirebaseSeeder.java           # Seed automático: popula Firebase se estiver vazio
├── controller/
│   ├── PortalController.java         # GET /  →  portal.html
│   ├── EventoController.java         # GET /eventos, /eventos/{id}
│   └── VerificacaoController.java    # GET/POST /verificar
├── dto/
│   ├── EventoDTO.java                # Projeção para views (dataFormatada, badgeCor)
│   └── EventoFiltroDTO.java          # Filtros: categoria, data, ordem
├── exception/
│   ├── EventoNaoEncontradoException.java
│   └── GlobalExceptionHandler.java   # @ControllerAdvice → erro-404/500
├── model/
│   └── Evento.java                   # Record com 9 campos
├── repository/
│   ├── EventoRepository.java         # Interface
│   ├── EventoMemoryRepository.java   # Implementação em memória (fallback / testes)
│   └── EventoFirebaseRepository.java # Implementação Firebase (@Primary, @Profile("firebase"))
└── service/
    └── EventoService.java            # Converte Evento → EventoDTO, filtros, verificação

src/main/resources/
├── application.properties            # spring.profiles.active=firebase
├── projeto-arena-pernambuco-firebase-adminsdk-fbsvc-6910d1e348.json  # GITIGNORED
├── static/
│   ├── css/style.css                 # Design system dark theme completo (1342 linhas)
│   └── js/main.js                    # AOS, Lenis, GSAP, header scroll, menu mobile
└── templates/
    ├── fragments.html   # Header/footer com sec:authorize
    ├── portal.html      # Página inicial com tsParticles + Typed.js + role cards
    ├── login.html       # Formulário Spring Security + hints de credenciais
    ├── eventos.html     # Grid com filtros
    ├── evento-detalhe.html
    ├── verificar.html
    ├── erro-404.html
    ├── erro-403.html
    └── erro-500.html
```

## Perfis Spring

| Perfil | Repositório ativo | Uso |
|--------|------------------|-----|
| `firebase` (padrão) | `EventoFirebaseRepository` | Produção / desenvolvimento |
| qualquer outro | `EventoMemoryRepository` | Testes (`@ActiveProfiles("memory")`) |

## Credenciais de demonstração

| Papel | E-mail | Senha |
|-------|--------|-------|
| PARTICIPANTE | participante@arena.com | senha123 |
| ADMIN | admin@arena.com | admin123 |

## Usuários do Sistema

- **Visitantes** — visualizam eventos sem login
- **Participantes** — verificam código de evento (`/verificar`)
- **Administradores** — acesso total (`/admin`)

## Histórias de Usuário (Status)

Documento: https://docs.google.com/document/d/1Ip4to0OEqmnKjZvN2xUTTQf7qFtkw_aETBb_hnfstgM/edit

| # | História | Status |
|---|----------|--------|
| H1 | Visualizar próximos eventos com filtro/ordenação | Implementada |
| H2 | Verificar código de evento | Implementada |
| H3 | Página de detalhe do evento | Implementada |
| H4+ | Dashboard admin com estatísticas | Não implementada |
| H5+ | Cadastro de eventos por administrador | Não implementada |
| H6+ | Agendamento de visitas / participação em eventos | Não implementada |

## MVP Funcionalidades

- [x] Portal de entrada com seleção de papel (visitante / participante / admin)
- [x] Visualização de eventos com filtro por categoria, data e ordenação
- [x] Página de detalhe do evento
- [x] Verificação de código de evento (`/verificar`)
- [x] Spring Security com 3 papéis e login customizado
- [x] Firebase Realtime Database como banco de dados real
- [x] Design dark theme responsivo (e-sports/arena)
- [x] Animações: AOS, GSAP, tsParticles, Typed.js, Lenis
- [ ] Dashboard administrativo com estatísticas (médias, tendência central, dispersão)
- [ ] Cadastro de novos eventos por administrador
- [ ] Agendamento de visitas / participação em eventos
- [ ] Sugestão de novos eventos por cidadãos
- [ ] Compra de ingressos (simulada)

## Fora do Escopo

- Integrações com sistemas governamentais
- Validade jurídica de operações
- Automações avançadas ou pagamentos reais

## Convenções do Código

- Usar português para nomes de domínio (Evento, EventoService, etc.)
- Seguir padrão MVC do Spring Boot com Repository Pattern e DTOs
- Testes com JUnit 5 + MockMvc (pasta src/test/) — 47 testes passando
- Commits em PORTUGUÊS
- NUNCA commitar `*.json` de credenciais Firebase (já no .gitignore)
