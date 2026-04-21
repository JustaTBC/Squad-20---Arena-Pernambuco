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
- **Banco de dados:** ainda NÃO implementado (dados em memória/List)

## Estrutura do Projeto

```
src/main/java/com/arenapernambuco/
├── ArenaPernambucoApplication.java   # Entry point Spring Boot
├── model/
│   └── Evento.java                   # Record com: id, titulo, dataHora, categoria, codigoVerificacao, descricaoCurta
├── service/
│   └── EventoService.java            # Lógica de negócio; dados mockados em List.of(...)
└── web/
    └── SiteController.java           # Controller MVC: /, /eventos, /verificar (GET e POST)

src/main/resources/
├── application.properties
├── static/css/style.css
└── templates/
    ├── index.html       # Página inicial (home)
    ├── eventos.html     # Listagem de eventos com filtro por categoria
    ├── verificar.html   # Verificação de código de evento
    └── fragments.html   # Header e footer reutilizáveis
```

## Usuários do Sistema

- **Compradores de ingressos** — cidadãos que querem participar de eventos
- **Cidadãos interessados** — querem visualizar programação
- **Administradores** — gerenciam eventos e visualizam estatísticas

## Histórias de Usuário (Status)

Documento: https://docs.google.com/document/d/1Ip4to0OEqmnKjZvN2xUTTQf7qFtkw_aETBb_hnfstgM/edit

| # | História | Status |
|---|----------|--------|
| H1 | Visualizar próximos eventos com filtro/ordenação | Parcialmente implementada |
| H2 | Verificar código de evento | Parcialmente implementada |
| H3 | (Ver documento) | Parcialmente implementada |
| H4+ | Dashboard admin com estatísticas | Não implementada |
| H5+ | Cadastro de eventos por administrador | Não implementada |
| H6+ | Agendamento de visitas / participação em eventos | Não implementada |

## MVP Funcionalidades

- [x] Visualização de eventos com filtro por categoria
- [x] Verificação de código de evento
- [ ] Filtros/ordenação avançados
- [ ] Dashboard administrativo com estatísticas (médias, tendência central, dispersão)
- [ ] Cadastro de novos eventos por administrador
- [ ] Agendamento de visitas / participação em eventos
- [ ] Sugestão de novos eventos por cidadãos
- [ ] Compra de ingressos (simulada)
- [ ] Banco de dados real (substituir mock em memória)

## Fora do Escopo

- Integrações com sistemas governamentais
- Validade jurídica de operações
- Automações avançadas ou pagamentos reais

## Convenções do Código

- Usar português para nomes de domínio (Evento, EventoService, etc.)
- Seguir padrão MVC do Spring Boot
- Testes com JUnit (pasta src/test/)
- Commits em PORTUGUÊS
