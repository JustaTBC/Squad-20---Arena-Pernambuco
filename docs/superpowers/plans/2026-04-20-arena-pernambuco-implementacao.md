# Arena Pernambuco — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesenhar e expandir o sistema Arena Pernambuco com portal de acesso por papéis, Spring Security, filtros avançados, página de detalhe de evento, design dark theme com animações, e arquitetura preparada para Firebase.

**Architecture:** Arquitetura MVC em camadas (Controller → Service → Repository → Model) com Repository Pattern para desacoplar persistência. Dados em memória (EventoMemoryRepository) substituíveis por Firebase sem alterar camadas superiores. Spring Security com 3 papéis (VISITANTE anônimo, PARTICIPANTE, ADMIN). Frontend com Thymeleaf + bibliotecas vanilla JS via CDN (AOS, Typed.js, tsParticles, GSAP, Lenis, Barba.js).

**Tech Stack:** Java 17, Spring Boot 3.4.4, Spring Security, Thymeleaf, Maven, JUnit 5, MockMvc

**Spec:** `docs/superpowers/specs/2026-04-20-arena-pernambuco-redesign-design.md`

**Skills a utilizar durante execução:**
- `superpowers:test-driven-development` — testes antes da implementação (backend)
- `frontend-design:frontend-design` — templates e design system
- `webapp-testing` — testar app no browser
- `web-design-guidelines` — revisão de UI
- `security-review` — auditoria de segurança antes de finalizar
- `superpowers:verification-before-completion` — antes de declarar completo
- `superpowers:requesting-code-review` — após tarefas major

**Agentes a utilizar:**
- `frontend-developer` — implementação de templates complexos
- `security-auditor` — auditoria de segurança
- `ui-ux-designer` — revisão visual/acessibilidade

---

## Estrutura de Arquivos

### Arquivos a CRIAR

```
src/main/java/com/arenapernambuco/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── PortalController.java
│   ├── EventoController.java
│   └── VerificacaoController.java
├── dto/
│   ├── EventoDTO.java
│   └── EventoFiltroDTO.java
├── exception/
│   ├── EventoNaoEncontradoException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── EventoRepository.java
│   └── EventoMemoryRepository.java

src/main/resources/
├── static/
│   └── js/
│       └── main.js
├── templates/
│   ├── portal.html
│   ├── login.html
│   ├── evento-detalhe.html
│   ├── erro-404.html
│   ├── erro-403.html
│   └── erro-500.html

src/test/java/com/arenapernambuco/
├── repository/
│   └── EventoMemoryRepositoryTest.java
├── service/
│   └── EventoServiceTest.java
├── controller/
│   ├── PortalControllerTest.java
│   ├── EventoControllerTest.java
│   └── VerificacaoControllerTest.java
└── config/
    └── SecurityConfigTest.java
```

### Arquivos a MODIFICAR

```
pom.xml                                          — adicionar spring-boot-starter-security
.gitignore                                       — adicionar regras para Firebase/env
src/main/java/.../model/Evento.java              — adicionar campos descricaoCompleta, imagemUrl, ativo
src/main/java/.../service/EventoService.java     — refatorar para usar Repository + DTOs
src/main/resources/static/css/style.css          — redesign completo dark theme
src/main/resources/templates/fragments.html      — header/footer redesenhados
src/main/resources/templates/index.html           — DELETAR (substituído por portal.html)
src/main/resources/templates/eventos.html        — redesign com grid + filtros + AOS
src/main/resources/templates/verificar.html      — redesign dark theme
```

### Arquivos a DELETAR

```
src/main/java/.../web/SiteController.java        — substituído pelos 3 controllers novos
src/main/resources/templates/index.html           — substituído por portal.html
```

---

## Task 1: Model, DTOs e Repository (Foundation)

**Files:**
- Modify: `src/main/java/com/arenapernambuco/model/Evento.java`
- Create: `src/main/java/com/arenapernambuco/dto/EventoDTO.java`
- Create: `src/main/java/com/arenapernambuco/dto/EventoFiltroDTO.java`
- Create: `src/main/java/com/arenapernambuco/repository/EventoRepository.java`
- Create: `src/main/java/com/arenapernambuco/repository/EventoMemoryRepository.java`
- Test: `src/test/java/com/arenapernambuco/repository/EventoMemoryRepositoryTest.java`

- [ ] **Step 1: Escrever testes do EventoMemoryRepository**

```java
package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventoMemoryRepositoryTest {

    private EventoMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EventoMemoryRepository();
    }

    @Test
    void buscarTodos_retornaTodosOsEventos() {
        List<Evento> todos = repository.buscarTodos();
        assertFalse(todos.isEmpty());
        assertTrue(todos.size() >= 8);
    }

    @Test
    void buscarAtivos_retornaApenasAtivos() {
        List<Evento> ativos = repository.buscarAtivos();
        assertTrue(ativos.stream().allMatch(Evento::ativo));
    }

    @Test
    void buscarPorId_existente_retornaEvento() {
        Optional<Evento> evento = repository.buscarPorId("1");
        assertTrue(evento.isPresent());
        assertEquals("1", evento.get().id());
    }

    @Test
    void buscarPorId_inexistente_retornaVazio() {
        Optional<Evento> evento = repository.buscarPorId("999");
        assertTrue(evento.isEmpty());
    }

    @Test
    void buscarPorCodigo_existente_retornaEvento() {
        Optional<Evento> evento = repository.buscarPorCodigo("AP-FUT-001");
        assertTrue(evento.isPresent());
    }

    @Test
    void buscarPorCodigo_caseInsensitive() {
        Optional<Evento> evento = repository.buscarPorCodigo("ap-fut-001");
        assertTrue(evento.isPresent());
    }

    @Test
    void buscarPorCodigo_nulo_retornaVazio() {
        Optional<Evento> evento = repository.buscarPorCodigo(null);
        assertTrue(evento.isEmpty());
    }

    @Test
    void buscarPorCodigo_vazio_retornaVazio() {
        Optional<Evento> evento = repository.buscarPorCodigo("   ");
        assertTrue(evento.isEmpty());
    }

    @Test
    void filtrar_porCategoria_retornaFiltrado() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Futebol", null, null);
        List<Evento> resultado = repository.filtrar(filtro);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(e -> e.categoria().equalsIgnoreCase("Futebol")));
    }

    @Test
    void filtrar_porData_retornaEventosDaData() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, LocalDate.of(2026, 5, 3), null);
        List<Evento> resultado = repository.filtrar(filtro);
        assertTrue(resultado.stream().allMatch(e -> e.dataHora().toLocalDate().equals(LocalDate.of(2026, 5, 3))));
    }

    @Test
    void filtrar_semFiltros_retornaTodosAtivos() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, null);
        List<Evento> resultado = repository.filtrar(filtro);
        assertEquals(repository.buscarAtivos().size(), resultado.size());
    }

    @Test
    void filtrar_ordenaPorDataProximoPrimeiro() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, "proximos");
        List<Evento> resultado = repository.filtrar(filtro);
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(resultado.get(i).dataHora().isBefore(resultado.get(i + 1).dataHora())
                    || resultado.get(i).dataHora().isEqual(resultado.get(i + 1).dataHora()));
        }
    }

    @Test
    void filtrar_ordenaPorDataRecentePrimeiro() {
        EventoFiltroDTO filtro = new EventoFiltroDTO(null, null, "recentes");
        List<Evento> resultado = repository.filtrar(filtro);
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(resultado.get(i).dataHora().isAfter(resultado.get(i + 1).dataHora())
                    || resultado.get(i).dataHora().isEqual(resultado.get(i + 1).dataHora()));
        }
    }
}
```

- [ ] **Step 2: Executar testes para confirmar que falham**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test -Dtest=EventoMemoryRepositoryTest -pl . 2>&1 | tail -20`
Expected: FAIL — classes não existem

- [ ] **Step 3: Atualizar Evento.java com novos campos**

```java
package com.arenapernambuco.model;

import java.time.LocalDateTime;

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
) {
}
```

- [ ] **Step 4: Criar EventoFiltroDTO.java**

```java
package com.arenapernambuco.dto;

import java.time.LocalDate;

public record EventoFiltroDTO(
        String categoria,
        LocalDate data,
        String ordem
) {
}
```

- [ ] **Step 5: Criar EventoDTO.java**

```java
package com.arenapernambuco.dto;

public record EventoDTO(
        String id,
        String titulo,
        String dataFormatada,
        String categoria,
        String descricaoCurta,
        String descricaoCompleta,
        String imagemUrl,
        String badgeCor
) {
}
```

- [ ] **Step 6: Criar EventoRepository.java (interface)**

```java
package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;

import java.util.List;
import java.util.Optional;

public interface EventoRepository {
    List<Evento> buscarTodos();
    List<Evento> buscarAtivos();
    Optional<Evento> buscarPorId(String id);
    Optional<Evento> buscarPorCodigo(String codigo);
    List<Evento> filtrar(EventoFiltroDTO filtro);
}
```

- [ ] **Step 7: Criar EventoMemoryRepository.java**

```java
package com.arenapernambuco.repository;

import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.model.Evento;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EventoMemoryRepository implements EventoRepository {

    private final List<Evento> eventos = List.of(
            new Evento("1", "Campeonato Pernambucano 2026",
                    LocalDateTime.of(2026, 4, 25, 16, 0),
                    "Futebol", "AP-FUT-001",
                    "Jogo oficial do campeonato estadual.",
                    "Grande clássico do futebol pernambucano com transmissão ao vivo. Venha torcer pelo seu time na Arena Pernambuco, com capacidade para mais de 46 mil torcedores.",
                    "https://picsum.photos/seed/futebol1/800/400",
                    true),
            new Evento("2", "Show Nacional — Alceu Valença",
                    LocalDateTime.of(2026, 5, 3, 21, 0),
                    "Música", "AP-MUS-001",
                    "Arena lotada com o mestre da música pernambucana.",
                    "Alceu Valença apresenta seus maiores sucessos em uma noite inesquecível na Arena Pernambuco. Repertório inclui clássicos como 'Anunciação' e 'La Belle de Jour'.",
                    "https://picsum.photos/seed/show1/800/400",
                    true),
            new Evento("3", "Feira de Negócios e Inovação",
                    LocalDateTime.of(2026, 4, 28, 9, 0),
                    "Corporativo", "AP-CORP-001",
                    "Evento empresarial com startups e investidores.",
                    "A maior feira de negócios do Nordeste reúne mais de 200 expositores, palestras com líderes do mercado e área de networking. Ideal para empreendedores e profissionais de tecnologia.",
                    "https://picsum.photos/seed/feira1/800/400",
                    true),
            new Evento("4", "Festival Cultural Recife-Olinda",
                    LocalDateTime.of(2026, 5, 10, 14, 0),
                    "Cultural", "AP-CULT-001",
                    "Celebração da cultura pernambucana com atrações diversas.",
                    "Festival que celebra a riqueza cultural de Pernambuco com apresentações de maracatu, frevo, forró e artesanato local. Gastronomia regional e oficinas para todas as idades.",
                    "https://picsum.photos/seed/cultural1/800/400",
                    true),
            new Evento("5", "Espetáculo Teatral — O Auto da Compadecida",
                    LocalDateTime.of(2026, 5, 17, 19, 30),
                    "Teatro", "AP-TEA-001",
                    "Adaptação do clássico de Ariano Suassuna.",
                    "Companhia de teatro pernambucana apresenta a adaptação do clássico de Ariano Suassuna. Espetáculo premiado com cenografia moderna e elenco de primeira.",
                    "https://picsum.photos/seed/teatro1/800/400",
                    true),
            new Evento("6", "Copa do Nordeste — Semifinal",
                    LocalDateTime.of(2026, 5, 24, 18, 0),
                    "Futebol", "AP-FUT-002",
                    "Semifinal da Copa do Nordeste 2026.",
                    "Decisão eletrizante da Copa do Nordeste. Os melhores times da região se enfrentam na Arena Pernambuco em busca de uma vaga na grande final.",
                    "https://picsum.photos/seed/futebol2/800/400",
                    true),
            new Evento("7", "Rock in Arena PE",
                    LocalDateTime.of(2026, 6, 7, 17, 0),
                    "Música", "AP-MUS-002",
                    "Festival de rock com bandas nacionais e locais.",
                    "O maior festival de rock de Pernambuco com 3 palcos, 12 bandas, praça de alimentação e área de convivência. Line-up inclui nomes consagrados e revelações da cena independente.",
                    "https://picsum.photos/seed/rock1/800/400",
                    true),
            new Evento("8", "Hackathon Smart Cities PE",
                    LocalDateTime.of(2026, 6, 14, 8, 0),
                    "Corporativo", "AP-CORP-002",
                    "Maratona de programação com foco em cidades inteligentes.",
                    "48 horas de desenvolvimento de soluções tecnológicas para problemas reais de Pernambuco. Prêmios para os 3 melhores projetos. Mentoria com especialistas do mercado.",
                    "https://picsum.photos/seed/hack1/800/400",
                    true),
            new Evento("9", "Exposição de Arte Contemporânea",
                    LocalDateTime.of(2026, 6, 21, 10, 0),
                    "Cultural", "AP-CULT-002",
                    "Mostra de artistas pernambucanos emergentes.",
                    "Exposição com obras de 30 artistas pernambucanos emergentes. Instalações interativas, esculturas e pinturas que retratam a identidade nordestina contemporânea.",
                    "https://picsum.photos/seed/arte1/800/400",
                    true),
            new Evento("10", "Evento Cancelado para Teste",
                    LocalDateTime.of(2026, 7, 1, 20, 0),
                    "Música", "AP-MUS-099",
                    "Este evento foi cancelado.",
                    "Evento de teste marcado como inativo para validar filtro de eventos ativos.",
                    "https://picsum.photos/seed/cancelado/800/400",
                    false)
    );

    @Override
    public List<Evento> buscarTodos() {
        return eventos;
    }

    @Override
    public List<Evento> buscarAtivos() {
        return eventos.stream()
                .filter(Evento::ativo)
                .sorted(Comparator.comparing(Evento::dataHora))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Evento> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return eventos.stream()
                .filter(e -> e.id().equals(id.trim()))
                .findFirst();
    }

    @Override
    public Optional<Evento> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return eventos.stream()
                .filter(e -> e.codigoVerificacao().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    @Override
    public List<Evento> filtrar(EventoFiltroDTO filtro) {
        var stream = buscarAtivos().stream();

        if (filtro.categoria() != null && !filtro.categoria().isBlank()) {
            stream = stream.filter(e -> e.categoria().equalsIgnoreCase(filtro.categoria()));
        }

        if (filtro.data() != null) {
            stream = stream.filter(e -> e.dataHora().toLocalDate().equals(filtro.data()));
        }

        Comparator<Evento> comparator = Comparator.comparing(Evento::dataHora);
        if ("recentes".equalsIgnoreCase(filtro.ordem())) {
            comparator = comparator.reversed();
        }

        return stream.sorted(comparator).collect(Collectors.toList());
    }
}
```

- [ ] **Step 8: Executar testes para confirmar que passam**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test -Dtest=EventoMemoryRepositoryTest -pl . 2>&1 | tail -20`
Expected: PASS — todos os 13 testes

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/arenapernambuco/model/Evento.java \
       src/main/java/com/arenapernambuco/dto/ \
       src/main/java/com/arenapernambuco/repository/ \
       src/test/java/com/arenapernambuco/repository/
git commit -m "feat: criar Model, DTOs e Repository com dados em memória

Adiciona campos descricaoCompleta, imagemUrl e ativo ao Evento.
Cria EventoDTO, EventoFiltroDTO, interface EventoRepository e
implementação EventoMemoryRepository com 10 eventos de exemplo.
Inclui testes unitários para o repositório."
```

---

## Task 2: Refatorar EventoService para usar Repository + DTOs

**Files:**
- Modify: `src/main/java/com/arenapernambuco/service/EventoService.java`
- Test: `src/test/java/com/arenapernambuco/service/EventoServiceTest.java`

- [ ] **Step 1: Escrever testes do EventoService**

```java
package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.repository.EventoMemoryRepository;
import com.arenapernambuco.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventoServiceTest {

    private EventoService service;

    @BeforeEach
    void setUp() {
        EventoRepository repository = new EventoMemoryRepository();
        service = new EventoService(repository);
    }

    @Test
    void listarAtivos_retornaDTOs() {
        List<EventoDTO> lista = service.listarAtivos();
        assertFalse(lista.isEmpty());
        assertNotNull(lista.get(0).id());
        assertNotNull(lista.get(0).dataFormatada());
        assertNotNull(lista.get(0).badgeCor());
    }

    @Test
    void listarAtivos_naoRetornaInativos() {
        List<EventoDTO> lista = service.listarAtivos();
        assertTrue(lista.stream().noneMatch(e -> e.id().equals("10")));
    }

    @Test
    void filtrar_porCategoria() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Futebol", null, null);
        List<EventoDTO> lista = service.filtrar(filtro);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(e -> e.categoria().equals("Futebol")));
    }

    @Test
    void filtrar_categoriaInvalida_retornaVazio() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Invalida", null, null);
        List<EventoDTO> lista = service.filtrar(filtro);
        assertTrue(lista.isEmpty());
    }

    @Test
    void buscarDetalhePorId_existente_retornaDTO() {
        EventoDTO dto = service.buscarDetalhePorId("1");
        assertNotNull(dto);
        assertEquals("1", dto.id());
        assertNotNull(dto.descricaoCompleta());
    }

    @Test
    void buscarDetalhePorId_inexistente_lancaExcecao() {
        assertThrows(RuntimeException.class, () -> service.buscarDetalhePorId("999"));
    }

    @Test
    void verificarPorCodigo_existente_retornaDTO() {
        var resultado = service.verificarPorCodigo("AP-FUT-001");
        assertTrue(resultado.isPresent());
    }

    @Test
    void verificarPorCodigo_inexistente_retornaVazio() {
        var resultado = service.verificarPorCodigo("XXXX");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void badgeCor_futebol_verde() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Futebol", null, null);
        List<EventoDTO> lista = service.filtrar(filtro);
        assertEquals("#22c55e", lista.get(0).badgeCor());
    }

    @Test
    void badgeCor_musica_laranja() {
        EventoFiltroDTO filtro = new EventoFiltroDTO("Música", null, null);
        List<EventoDTO> lista = service.filtrar(filtro);
        assertEquals("#FF6B35", lista.get(0).badgeCor());
    }

    @Test
    void dataFormatada_formatoCorreto() {
        List<EventoDTO> lista = service.listarAtivos();
        assertTrue(lista.get(0).dataFormatada().matches("\\d{2}/\\d{2}/\\d{4} às \\d{2}h\\d{2}"));
    }
}
```

- [ ] **Step 2: Executar testes para confirmar que falham**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test -Dtest=EventoServiceTest -pl . 2>&1 | tail -20`
Expected: FAIL — métodos não existem

- [ ] **Step 3: Refatorar EventoService.java**

```java
package com.arenapernambuco.service;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventoService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH'h'mm", new Locale("pt", "BR"));

    private static final Map<String, String> CORES_CATEGORIA = Map.of(
            "futebol", "#22c55e",
            "música", "#FF6B35",
            "corporativo", "#3b82f6",
            "cultural", "#7C3AED",
            "teatro", "#ec4899"
    );

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public List<EventoDTO> listarAtivos() {
        return repository.buscarAtivos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EventoDTO> filtrar(EventoFiltroDTO filtro) {
        return repository.filtrar(filtro).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EventoDTO buscarDetalhePorId(String id) {
        Evento evento = repository.buscarPorId(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
        return toDTO(evento);
    }

    public Optional<EventoDTO> verificarPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo).map(this::toDTO);
    }

    private EventoDTO toDTO(Evento e) {
        return new EventoDTO(
                e.id(),
                e.titulo(),
                e.dataHora().format(FORMATTER),
                e.categoria(),
                e.descricaoCurta(),
                e.descricaoCompleta(),
                e.imagemUrl(),
                CORES_CATEGORIA.getOrDefault(e.categoria().toLowerCase(), "#6b7280")
        );
    }
}
```

- [ ] **Step 4: Criar EventoNaoEncontradoException (necessário para compilar)**

```java
package com.arenapernambuco.exception;

public class EventoNaoEncontradoException extends RuntimeException {

    private final String eventoId;

    public EventoNaoEncontradoException(String eventoId) {
        super("Evento não encontrado: " + eventoId);
        this.eventoId = eventoId;
    }

    public String getEventoId() {
        return eventoId;
    }
}
```

- [ ] **Step 5: Executar testes para confirmar que passam**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test -Dtest=EventoServiceTest -pl . 2>&1 | tail -20`
Expected: PASS — todos os 11 testes

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/arenapernambuco/service/EventoService.java \
       src/main/java/com/arenapernambuco/exception/ \
       src/test/java/com/arenapernambuco/service/
git commit -m "refactor: refatorar EventoService para usar Repository + DTOs

Injeta EventoRepository, converte Evento para EventoDTO com
formatação de data pt-BR e cor do badge por categoria.
Cria EventoNaoEncontradoException. Testes unitários inclusos."
```

---

## Task 3: Spring Security + GlobalExceptionHandler

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/com/arenapernambuco/config/SecurityConfig.java`
- Create: `src/main/java/com/arenapernambuco/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/arenapernambuco/config/SecurityConfigTest.java`

- [ ] **Step 1: Adicionar spring-boot-starter-security ao pom.xml**

Adicionar dentro de `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Escrever testes do SecurityConfig**

```java
package com.arenapernambuco.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rotaPublica_portal_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaPublica_eventos_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaPublica_eventosDetalhe_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/eventos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaProtegida_verificar_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void rotaProtegida_verificar_acessivelComParticipante() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rotaProtegida_verificar_acessivelComAdmin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk());
    }

    @Test
    void rotaAdmin_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void rotaAdmin_negadoParaParticipante() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rotaAdmin_acessivelComAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void paginaLogin_acessivelSemLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 3: Executar testes para confirmar que falham**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test -Dtest=SecurityConfigTest -pl . 2>&1 | tail -20`
Expected: FAIL — SecurityConfig e controllers não existem ainda

- [ ] **Step 4: Criar SecurityConfig.java**

```java
package com.arenapernambuco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/eventos", "/eventos/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/erro-*").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/verificar/**").hasAnyRole("PARTICIPANTE", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/eventos", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/erro-403")
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var participante = User.builder()
                .username("participante@arena.com")
                .password(encoder.encode("senha123"))
                .roles("PARTICIPANTE")
                .build();

        var admin = User.builder()
                .username("admin@arena.com")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(participante, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 5: Criar GlobalExceptionHandler.java**

```java
package com.arenapernambuco.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EventoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEventoNaoEncontrado(EventoNaoEncontradoException ex, Model model) {
        log.warn("Evento não encontrado: {}", ex.getEventoId());
        model.addAttribute("mensagem", "O evento solicitado não foi encontrado.");
        return "erro-404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        log.warn("Acesso negado: {}", ex.getMessage());
        model.addAttribute("mensagem", "Você não tem permissão para acessar esta página.");
        return "erro-403";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Erro interno", ex);
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return "erro-500";
    }
}
```

- [ ] **Step 6: Criar templates de erro mínimos (necessários para os testes passarem)**

Criar `src/main/resources/templates/erro-404.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title>Não encontrado</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main><h1>Página não encontrada</h1><p th:text="${mensagem}">Recurso não encontrado.</p><a href="/" class="btn btn--primary">Voltar ao início</a></main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

Criar `src/main/resources/templates/erro-403.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title>Acesso negado</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main><h1>Acesso negado</h1><p th:text="${mensagem}">Sem permissão.</p><a href="/login" class="btn btn--primary">Fazer login</a></main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

Criar `src/main/resources/templates/erro-500.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title>Erro</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main><h1>Erro interno</h1><p th:text="${mensagem}">Erro inesperado.</p><a href="/" class="btn btn--primary">Voltar ao início</a></main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

- [ ] **Step 7: Os testes de SecurityConfig dependem dos controllers. Criar stubs mínimos dos 3 controllers para os testes passarem (serão completados na Task 4).**

Criar `src/main/java/com/arenapernambuco/controller/PortalController.java`:
```java
package com.arenapernambuco.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping("/")
    public String portal() {
        return "portal";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/erro-403")
    public String erro403() {
        return "erro-403";
    }
}
```

Criar `src/main/java/com/arenapernambuco/controller/EventoController.java`:
```java
package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.dto.EventoFiltroDTO;
import com.arenapernambuco.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Controller
public class EventoController {

    private static final Set<String> CATEGORIAS_VALIDAS =
            Set.of("Futebol", "Música", "Corporativo", "Cultural", "Teatro");

    private static final Set<String> ORDENS_VALIDAS = Set.of("proximos", "recentes");

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public String listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String data,
            @RequestParam(required = false) String ordem,
            Model model) {

        String categoriaSanitizada = CATEGORIAS_VALIDAS.contains(categoria) ? categoria : null;
        String ordemSanitizada = ORDENS_VALIDAS.contains(ordem) ? ordem : "proximos";
        LocalDate dataParsed = parseData(data);

        EventoFiltroDTO filtro = new EventoFiltroDTO(categoriaSanitizada, dataParsed, ordemSanitizada);
        List<EventoDTO> eventos = eventoService.filtrar(filtro);

        model.addAttribute("eventos", eventos);
        model.addAttribute("categoriaAtiva", categoriaSanitizada);
        model.addAttribute("dataAtiva", data);
        model.addAttribute("ordemAtiva", ordemSanitizada);
        model.addAttribute("categorias", CATEGORIAS_VALIDAS);
        return "eventos";
    }

    @GetMapping("/eventos/{id}")
    public String detalhe(@PathVariable String id, Model model) {
        EventoDTO evento = eventoService.buscarDetalhePorId(id);
        model.addAttribute("evento", evento);
        return "evento-detalhe";
    }

    private LocalDate parseData(String data) {
        if (data == null || data.isBlank()) return null;
        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
```

Criar `src/main/java/com/arenapernambuco/controller/VerificacaoController.java`:
```java
package com.arenapernambuco.controller;

import com.arenapernambuco.dto.EventoDTO;
import com.arenapernambuco.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VerificacaoController {

    private static final int CODIGO_MAX_LENGTH = 20;

    private final EventoService eventoService;

    public VerificacaoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/verificar")
    public String formulario() {
        return "verificar";
    }

    @PostMapping("/verificar")
    public String verificar(@RequestParam(required = false) String codigo, Model model) {
        String codigoLimpo = sanitizarCodigo(codigo);

        if (codigoLimpo.isEmpty()) {
            model.addAttribute("ok", false);
            model.addAttribute("codigoInformado", "");
            return "verificar";
        }

        Optional<EventoDTO> encontrado = eventoService.verificarPorCodigo(codigoLimpo);
        if (encontrado.isPresent()) {
            model.addAttribute("ok", true);
            model.addAttribute("evento", encontrado.get());
        } else {
            model.addAttribute("ok", false);
            model.addAttribute("codigoInformado", codigoLimpo);
        }
        return "verificar";
    }

    private String sanitizarCodigo(String codigo) {
        if (codigo == null) return "";
        String limpo = codigo.trim();
        if (limpo.length() > CODIGO_MAX_LENGTH) {
            limpo = limpo.substring(0, CODIGO_MAX_LENGTH);
        }
        if (!limpo.matches("[a-zA-Z0-9\\-]+")) return "";
        return limpo;
    }
}
```

- [ ] **Step 8: Criar templates stub mínimos para portal.html, login.html e evento-detalhe.html (necessários para compilar e testes passarem — serão redesenhados na Task 6)**

Criar `src/main/resources/templates/portal.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title>Arena Pernambuco</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main><h1>Arena Pernambuco</h1><p>Selecione seu perfil de acesso.</p></main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

Criar `src/main/resources/templates/login.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title>Login — Arena Pernambuco</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main>
<h1>Login</h1>
<form th:action="@{/login}" method="post">
  <label for="username">E-mail</label>
  <input type="text" id="username" name="username" required/>
  <label for="password">Senha</label>
  <input type="password" id="password" name="password" required/>
  <button type="submit" class="btn btn--primary">Entrar</button>
</form>
</main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

Criar `src/main/resources/templates/evento-detalhe.html`:
```html
<!DOCTYPE html>
<html lang="pt-BR" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"/><title th:text="${evento.titulo} + ' — Arena Pernambuco'">Evento</title><link rel="stylesheet" th:href="@{/css/style.css}"/></head>
<body>
<div th:replace="~{fragments :: header}"></div>
<main>
<h1 th:text="${evento.titulo}">Evento</h1>
<p th:text="${evento.dataFormatada}">Data</p>
<p th:text="${evento.categoria}">Categoria</p>
<p th:text="${evento.descricaoCompleta}">Descrição</p>
</main>
<div th:replace="~{fragments :: footer}"></div>
</body>
</html>
```

- [ ] **Step 9: Deletar SiteController.java (substituído pelos 3 novos controllers)**

Deletar `src/main/java/com/arenapernambuco/web/SiteController.java`

- [ ] **Step 10: Executar todos os testes**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test 2>&1 | tail -30`
Expected: PASS — SecurityConfigTest + testes anteriores

- [ ] **Step 11: Commit**

```bash
git add pom.xml \
       src/main/java/com/arenapernambuco/config/ \
       src/main/java/com/arenapernambuco/controller/ \
       src/main/java/com/arenapernambuco/exception/GlobalExceptionHandler.java \
       src/main/resources/templates/portal.html \
       src/main/resources/templates/login.html \
       src/main/resources/templates/evento-detalhe.html \
       src/main/resources/templates/erro-*.html \
       src/test/java/com/arenapernambuco/config/
git rm src/main/java/com/arenapernambuco/web/SiteController.java
git commit -m "feat: adicionar Spring Security, controllers e tratamento de erros

Configura SecurityConfig com 3 papéis (VISITANTE anônimo,
PARTICIPANTE, ADMIN). Cria PortalController, EventoController e
VerificacaoController. Remove SiteController. Adiciona
GlobalExceptionHandler e templates de erro."
```

---

## Task 4: Testes de Controller

**Files:**
- Test: `src/test/java/com/arenapernambuco/controller/EventoControllerTest.java`
- Test: `src/test/java/com/arenapernambuco/controller/VerificacaoControllerTest.java`
- Test: `src/test/java/com/arenapernambuco/controller/PortalControllerTest.java`

- [ ] **Step 1: Escrever testes do EventoController**

```java
package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarEventos_retorna200() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(view().name("eventos"))
                .andExpect(model().attributeExists("eventos"));
    }

    @Test
    void listarEventos_comFiltroCategoria() throws Exception {
        mockMvc.perform(get("/eventos").param("categoria", "Futebol"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categoriaAtiva", "Futebol"));
    }

    @Test
    void listarEventos_categoriaInvalida_ignoraFiltro() throws Exception {
        mockMvc.perform(get("/eventos").param("categoria", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("categoriaAtiva", (Object) null));
    }

    @Test
    void detalheEvento_existente_retorna200() throws Exception {
        mockMvc.perform(get("/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("evento-detalhe"))
                .andExpect(model().attributeExists("evento"));
    }

    @Test
    void detalheEvento_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/eventos/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erro-404"));
    }

    @Test
    void listarEventos_comOrdenacao() throws Exception {
        mockMvc.perform(get("/eventos").param("ordem", "recentes"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ordemAtiva", "recentes"));
    }

    @Test
    void listarEventos_ordemInvalida_usaPadrao() throws Exception {
        mockMvc.perform(get("/eventos").param("ordem", "invalido"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ordemAtiva", "proximos"));
    }
}
```

- [ ] **Step 2: Escrever testes do VerificacaoController**

```java
package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void formulario_retorna200() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().isOk())
                .andExpect(view().name("verificar"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoValido_retornaOk() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "AP-FUT-001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", true))
                .andExpect(model().attributeExists("evento"));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoInexistente_retornaFalse() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "XXXX")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoVazio_retornaFalse() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    @WithMockUser(roles = "PARTICIPANTE")
    void verificar_codigoComScriptInjection_rejeitado() throws Exception {
        mockMvc.perform(post("/verificar")
                        .param("codigo", "<script>alert(1)</script>")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("ok", false));
    }

    @Test
    void verificar_semLogin_redirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/verificar"))
                .andExpect(status().is3xxRedirection());
    }
}
```

- [ ] **Step 3: Escrever testes do PortalController**

```java
package com.arenapernambuco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void portal_retorna200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal"));
    }

    @Test
    void login_retorna200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}
```

- [ ] **Step 4: Executar todos os testes**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test 2>&1 | tail -30`
Expected: PASS — todos os testes de controller + anteriores

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/arenapernambuco/controller/
git commit -m "test: adicionar testes dos controllers com MockMvc

Testa rotas públicas, protegidas, filtros, validação de input,
sanitização contra XSS e códigos inexistentes."
```

---

## Task 5: Atualizar .gitignore

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Adicionar regras de segurança ao .gitignore**

Adicionar ao final do `.gitignore`:
```
# Firebase
*-credentials.json
firebase-adminsdk*.json
serviceAccountKey.json

# Environment
*.env
.env.*
application-local.properties

# Node (caso adicione Tailwind no futuro)
node_modules/
```

- [ ] **Step 2: Commit**

```bash
git add .gitignore
git commit -m "chore: atualizar .gitignore com regras de segurança

Adiciona exclusões para credenciais Firebase, arquivos .env
e application-local.properties."
```

---

## Task 6: Design System — CSS + Fragments + JS

**Skill requerida:** `frontend-design:frontend-design`

**Files:**
- Modify: `src/main/resources/static/css/style.css` — redesign completo
- Modify: `src/main/resources/templates/fragments.html` — header/footer dark theme
- Create: `src/main/resources/static/js/main.js` — inicialização das libs JS

- [ ] **Step 1: Reescrever style.css com design system dark theme**

O CSS completo deve incluir:
- CSS variables com a paleta definida na spec (--bg-primary: #0a0a0f, --accent: #FF6B35, etc.)
- Google Fonts import (Bebas Neue + Inter)
- Reset e base styles (body dark, typography)
- Header fixo com transparência que fica sólido ao scroll
- Hamburger menu para mobile (< 768px)
- Sistema de botões (btn--primary glow, btn--ghost, btn--secondary)
- Cards com glassmorphism e borda luminosa no hover
- Grid responsivo para cards de evento (1col mobile, 2-3col desktop)
- Badge system por categoria (cores conforme spec)
- Hero section fullscreen com overlay
- Formulários estilizados (inputs glassmorphism)
- Resultado verificação (sucesso/erro animados)
- Páginas de erro
- Footer
- Utilitários (text-gradient, shimmer, glow)
- Media queries mobile-first com clamp() para fontes
- Animações CSS: fade-in, slide-up, glow-pulse, shimmer, gradient-text

**Referências visuais a seguir:** Ref 1 (cards overlay), Ref 3 (hero com partículas, stats), uiverse.io (botões glow, inputs)

- [ ] **Step 2: Reescrever fragments.html com header/footer dark theme**

Header deve incluir:
- Logo "Arena Pernambuco" com gradient text
- Nav links: Início, Eventos, Verificar (condicional com `sec:authorize`)
- Botão CTA "Ver Eventos"
- Indicador de usuário logado (se autenticado)
- Botão Sair (se autenticado)
- Hamburger menu toggle para mobile
- `xmlns:sec` para integração com Spring Security tags

Footer deve incluir:
- Texto de copyright
- Links úteis
- Dark theme consistente

- [ ] **Step 3: Criar main.js com inicialização das bibliotecas**

```javascript
document.addEventListener('DOMContentLoaded', () => {
    // AOS init
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 800,
            easing: 'ease-out-cubic',
            once: true,
            offset: 50
        });
    }

    // Lenis smooth scroll
    if (typeof Lenis !== 'undefined') {
        const lenis = new Lenis({ duration: 1.2, easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)) });
        function raf(time) { lenis.raf(time); requestAnimationFrame(raf); }
        requestAnimationFrame(raf);
    }

    // GSAP page fade-in
    if (typeof gsap !== 'undefined') {
        gsap.from('main', { opacity: 0, y: 20, duration: 0.6, ease: 'power2.out' });
    }

    // Header scroll effect
    const header = document.querySelector('.site-header');
    if (header) {
        window.addEventListener('scroll', () => {
            header.classList.toggle('scrolled', window.scrollY > 50);
        });
    }

    // Mobile menu toggle
    const menuToggle = document.querySelector('.menu-toggle');
    const nav = document.querySelector('.nav');
    if (menuToggle && nav) {
        menuToggle.addEventListener('click', () => {
            nav.classList.toggle('nav--open');
            menuToggle.classList.toggle('active');
        });
    }
});
```

- [ ] **Step 4: Testar visualmente no browser**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw spring-boot:run`
Abrir http://localhost:8080 e verificar:
- Header fixo com transparência
- Footer dark theme
- Estilo geral dark aplicado em todas as páginas

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/css/style.css \
       src/main/resources/templates/fragments.html \
       src/main/resources/static/js/main.js
git commit -m "feat: redesign completo com dark theme e design system

Nova paleta escura, fontes Bebas Neue + Inter, header fixo com
transparência, botões com glow, cards glassmorphism, grid responsivo,
badges por categoria, animações CSS. JS com AOS, Lenis, GSAP."
```

---

## Task 7: Template Portal (Tela de Entrada)

**Skill requerida:** `frontend-design:frontend-design`

**Files:**
- Modify: `src/main/resources/templates/portal.html` — redesign completo
- Delete: `src/main/resources/templates/index.html` — substituído por portal.html

- [ ] **Step 1: Reescrever portal.html com design final**

Template deve incluir:
- Hero fullscreen com container de tsParticles (`<div id="tsparticles">`)
- Título grande em Bebas Neue com gradient text
- Subtítulo com Typed.js digitando: "Esportes...", "Shows...", "Cultura...", "Negócios..."
- Seção de stats animados (estilo Ref 3): "50+ Eventos", "46mil Capacidade", "5 Categorias"
- 3 cards de seleção de papel com AOS fade-up escalonado:
  - Visitante (ícone, descrição, link `/eventos`)
  - Participante (ícone, descrição, link `/login`)
  - Administrador (ícone, descrição, link `/login`)
- Cards com GSAP hover scale effect
- Script inline no final: inicialização de tsParticles (partículas conectadas, cor accent) e Typed.js
- CDN links no `<head>`: AOS CSS, Google Fonts
- CDN scripts antes de `</body>`: AOS JS, Typed.js, tsParticles, GSAP, Lenis, main.js

- [ ] **Step 2: Deletar index.html**

Deletar `src/main/resources/templates/index.html`

- [ ] **Step 3: Testar visualmente no browser**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw spring-boot:run`
Abrir http://localhost:8080 e verificar:
- Partículas de fundo funcionando
- Typed.js digitando categorias
- 3 cards com hover effect
- Responsivo em mobile (DevTools → toggle device toolbar)
- Links dos cards redirecionam corretamente

- [ ] **Step 4: Commit**

```bash
git rm src/main/resources/templates/index.html
git add src/main/resources/templates/portal.html
git commit -m "feat: criar portal de entrada com partículas e seleção de papel

Hero fullscreen com tsParticles, Typed.js, stats animados e
3 cards de seleção (Visitante, Participante, Admin). Remove index.html."
```

---

## Task 8: Template Login

**Files:**
- Modify: `src/main/resources/templates/login.html` — redesign completo

- [ ] **Step 1: Reescrever login.html com design glassmorphism**

Template deve incluir:
- Fundo com tsParticles (mesmo config do portal)
- Card centralizado com glassmorphism (backdrop-filter: blur, bg semi-transparente)
- Formulário Spring Security: `th:action="@{/login}"` method="post"
- Campos: username (email) e password com inputs estilizados
- Botão submit com glow effect
- Mensagem de erro condicional: `th:if="${param.error}"` → "E-mail ou senha inválidos"
- Mensagem de logout: `th:if="${param.logout}"` → "Você saiu com sucesso"
- Link "Entrar como visitante" → `/eventos`

- [ ] **Step 2: Testar visualmente no browser**

Abrir http://localhost:8080/login e verificar:
- Glassmorphism do card
- Login com `participante@arena.com` / `senha123` → redireciona para `/eventos`
- Login com `admin@arena.com` / `admin123` → redireciona para `/eventos`
- Login com credenciais erradas → mensagem de erro
- Link "Entrar como visitante" funciona

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/login.html
git commit -m "feat: redesenhar tela de login com glassmorphism

Card centralizado com backdrop-filter, inputs estilizados,
mensagens de erro/logout, link para acesso como visitante."
```

---

## Task 9: Template Eventos (Listagem com Filtros)

**Files:**
- Modify: `src/main/resources/templates/eventos.html` — redesign completo

- [ ] **Step 1: Reescrever eventos.html com grid de cards e filtros**

Template deve incluir:
- Barra de filtros sticky no topo:
  - Select de categoria (Todos + cada categoria) com `th:selected`
  - Input date para filtro por data com `th:value`
  - Select de ordenação (Mais próximos / Mais recentes) com `th:selected`
  - Botão "Filtrar" que submete form GET
- Grid de cards de evento (CSS grid, 1-3 colunas):
  - Cada card com AOS `data-aos="fade-up"` e `data-aos-delay` escalonado por índice (th:attr)
  - Imagem do evento no topo do card
  - Badge de categoria com cor dinâmica (`th:style="'background:' + ${e.badgeCor}"`)
  - Título, data formatada, descrição curta
  - Link "Ver detalhes" → `/eventos/{id}`
- Estado vazio: mensagem "Nenhum evento encontrado para os filtros selecionados" com CTA limpar filtros
- Manter filtros ativos após submit via `th:value` e `th:selected`

- [ ] **Step 2: Testar visualmente no browser**

Abrir http://localhost:8080/eventos e verificar:
- Grid de cards com imagens
- Badges coloridas por categoria
- AOS fade-up ao scroll
- Filtro por categoria funciona e mantém seleção
- Filtro por data funciona
- Ordenação funciona
- Estado vazio com mensagem amigável
- Responsivo (1 coluna mobile, 3 colunas desktop)
- Clicar em "Ver detalhes" leva para `/eventos/{id}`

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/eventos.html
git commit -m "feat: redesenhar listagem de eventos com grid, filtros e AOS

Grid responsivo de cards com imagem, badge colorida, filtros por
categoria/data/ordenação, estado vazio amigável, animações AOS."
```

---

## Task 10: Template Detalhe do Evento

**Files:**
- Modify: `src/main/resources/templates/evento-detalhe.html` — redesign completo

- [ ] **Step 1: Reescrever evento-detalhe.html com hero image**

Template deve incluir:
- Hero section com imagem de fundo do evento (th:style background-image)
- Overlay gradient escuro sobre a imagem
- Título grande sobre o hero
- Badge de categoria
- Data formatada
- Seção de conteúdo abaixo do hero:
  - Descrição completa
  - Informações adicionais (código de verificação se logado)
  - Botão "Verificar ingresso" (link para /verificar, visível se logado)
  - Botão "Voltar aos eventos" (link para /eventos)
- AOS fade-up no conteúdo

- [ ] **Step 2: Testar visualmente no browser**

Abrir http://localhost:8080/eventos/1 e verificar:
- Hero com imagem de fundo e overlay
- Informações do evento corretas
- Botão "Voltar aos eventos" funciona
- Acessar `/eventos/999` mostra página 404 amigável

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/evento-detalhe.html
git commit -m "feat: criar página de detalhe do evento com hero image

Hero com imagem de fundo + overlay, badge de categoria, descrição
completa, botão verificar ingresso e voltar aos eventos."
```

---

## Task 11: Template Verificação (Redesign)

**Files:**
- Modify: `src/main/resources/templates/verificar.html` — redesign dark theme

- [ ] **Step 1: Reescrever verificar.html com dark theme**

Template deve manter a lógica atual mas com novo visual:
- Card centralizado glassmorphism
- Input estilizado com label
- Botão com glow
- Resultado sucesso: card verde com ícone check, dados do evento
- Resultado erro: card vermelho com ícone X, mensagem amigável
- Animação no resultado (fade-in com GSAP ou AOS)
- CSRF token via `th:action`

- [ ] **Step 2: Testar visualmente no browser**

Login como participante, acessar /verificar e testar:
- Código válido "AP-FUT-001" → resultado verde com dados do evento
- Código inválido "XXXX" → resultado vermelho com mensagem
- Código vazio → resultado vermelho

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/verificar.html
git commit -m "feat: redesenhar verificação de evento com dark theme

Card glassmorphism, resultado animado com ícones de sucesso/erro,
inputs estilizados, consistente com design system."
```

---

## Task 12: Templates de Erro (Redesign)

**Files:**
- Modify: `src/main/resources/templates/erro-404.html`
- Modify: `src/main/resources/templates/erro-403.html`
- Modify: `src/main/resources/templates/erro-500.html`

- [ ] **Step 1: Redesenhar erro-404.html**

Template com:
- Número "404" grande em Bebas Neue com gradient text
- Mensagem amigável
- CTA "Voltar ao início" e "Ver eventos"
- Consistente com dark theme

- [ ] **Step 2: Redesenhar erro-403.html**

Template com:
- Número "403" grande em Bebas Neue
- Mensagem "Acesso restrito"
- CTA "Fazer login" e "Voltar ao início"

- [ ] **Step 3: Redesenhar erro-500.html**

Template com:
- Número "500" grande
- Mensagem "Algo deu errado"
- CTA "Voltar ao início"

- [ ] **Step 4: Testar visualmente no browser**

- Acessar `/eventos/999` → ver 404 estilizado
- Acessar `/admin` sem login → ver redirect para login
- Login como participante, acessar `/admin` → ver 403 estilizado

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/erro-*.html
git commit -m "feat: redesenhar páginas de erro com dark theme

Páginas 404, 403 e 500 com tipografia grande, mensagens
amigáveis e CTAs, consistentes com design system."
```

---

## Task 13: Adicionar dependência Thymeleaf Extras Spring Security

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Adicionar thymeleaf-extras-springsecurity6 ao pom.xml**

Adicionar dentro de `<dependencies>`:
```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

Isso permite usar `sec:authorize` nos templates para mostrar/esconder elementos baseado no papel do usuário.

- [ ] **Step 2: Atualizar fragments.html para usar sec:authorize**

Adicionar ao header:
- Link "Verificar" visível apenas para PARTICIPANTE/ADMIN
- Link "Admin" visível apenas para ADMIN
- Botão "Sair" visível quando autenticado
- Nome do usuário logado visível quando autenticado

- [ ] **Step 3: Executar todos os testes**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test 2>&1 | tail -30`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add pom.xml src/main/resources/templates/fragments.html
git commit -m "feat: adicionar Thymeleaf Spring Security extras

Integra sec:authorize nos templates para controle de visibilidade
por papel. Header mostra links condicionais e indicador de login."
```

---

## Task 14: Revisão de Segurança

**Skill requerida:** `security-review`
**Agente requerido:** `security-auditor`

- [ ] **Step 1: Executar skill security-review**

Verificar:
- CSRF em todos os formulários POST
- Inputs sanitizados (código, categoria, data, ordem)
- Rotas protegidas corretamente
- Sem stacktrace exposto ao usuário
- .gitignore com regras de segurança
- Sem credenciais hardcoded expostas (os usuários in-memory são para dev, aceitos)
- Headers de segurança do Spring Security ativos

- [ ] **Step 2: Executar agente security-auditor**

Auditoria completa do código Java e templates.

- [ ] **Step 3: Corrigir issues encontrados (se houver)**

- [ ] **Step 4: Commit (se houve correções)**

```bash
git add -A
git commit -m "fix: correções de segurança identificadas na auditoria"
```

---

## Task 15: Teste Final e Verificação

**Skill requerida:** `superpowers:verification-before-completion`, `webapp-testing`
**Agente requerido:** `ui-ux-designer`

- [ ] **Step 1: Executar todos os testes**

Run: `cd Squad-20---Arena-Pernambuco && ./mvnw test 2>&1 | tail -30`
Expected: PASS — todos os testes

- [ ] **Step 2: Testar app completa no browser com webapp-testing**

Fluxos a testar:
1. Portal → clicar Visitante → Eventos → filtrar → detalhe → voltar
2. Portal → clicar Participante → Login → Verificar → código válido → OK
3. Portal → clicar Participante → Login → Verificar → código inválido → erro
4. Portal → clicar Admin → Login admin → acesso admin
5. Tentar /verificar sem login → redirect para login
6. Tentar /admin como participante → 403
7. Acessar /eventos/999 → 404
8. Responsividade mobile em todas as páginas
9. Animações: AOS, Typed.js, tsParticles, GSAP

- [ ] **Step 3: Revisão de UI/UX com agente ui-ux-designer**

Verificar acessibilidade, contraste, hierarquia visual, consistência.

- [ ] **Step 4: Executar skill verification-before-completion**

- [ ] **Step 5: Executar skill requesting-code-review**

---

## Resumo

| Task | Descrição | Testes |
|------|-----------|--------|
| 1 | Model, DTOs, Repository | 13 testes unitários |
| 2 | Refatorar EventoService | 11 testes unitários |
| 3 | Spring Security + ExceptionHandler + Controllers | 10 testes de segurança |
| 4 | Testes de Controller | 16 testes MockMvc |
| 5 | .gitignore | — |
| 6 | CSS + Fragments + JS (design system) | Visual |
| 7 | Portal template | Visual |
| 8 | Login template | Visual |
| 9 | Eventos template (filtros + grid) | Visual |
| 10 | Detalhe evento template | Visual |
| 11 | Verificação template | Visual |
| 12 | Erro templates | Visual |
| 13 | Thymeleaf Security extras | Testes existentes |
| 14 | Revisão de segurança | Security audit |
| 15 | Teste final e verificação | Todos |

**Total estimado:** ~50 testes automatizados + validação visual completa
