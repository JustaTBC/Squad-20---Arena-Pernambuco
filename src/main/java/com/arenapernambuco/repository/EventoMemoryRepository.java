package com.arenapernambuco.repository;

import com.arenapernambuco.exception.EventoNaoEncontradoException;
import com.arenapernambuco.model.Evento;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EventoMemoryRepository implements EventoRepository {

    private final List<Evento> eventos = Collections.synchronizedList(new ArrayList<>(List.of(
            new Evento("1", "Campeonato Pernambucano — Final",
                    LocalDateTime.of(2026, 5, 10, 16, 0),
                    "Futebol", "AP-FUT-001",
                    "Grande final do Campeonato Pernambucano de 2026.",
                    "A grande decisão do Campeonato Pernambucano acontece na Arena Pernambuco com portões abertos e transmissão ao vivo. Venha torcer pelo seu time!",
                    "https://picsum.photos/seed/fut1/800/400", true, 45000, 38000),

            new Evento("2", "Show Alceu Valença",
                    LocalDateTime.of(2026, 6, 14, 20, 0),
                    "Música", "AP-MUS-001",
                    "Alceu Valença em grande show na Arena.",
                    "O mestre do frevo e da MPB Alceu Valença traz seu repertório clássico para uma noite inesquecível na Arena Pernambuco.",
                    "https://picsum.photos/seed/mus1/800/400", true, 30000, 22000),

            new Evento("3", "Feira de Inovação Recife",
                    LocalDateTime.of(2026, 7, 5, 9, 0),
                    "Corporativo", "AP-COR-001",
                    "Feira de negócios e inovação tecnológica.",
                    "Reúne startups, investidores e empresas do ecossistema de inovação de Pernambuco para networking, palestras e exposições.",
                    "https://picsum.photos/seed/cor1/800/400", true, 5000, 3200),

            new Evento("4", "Festival de Teatro do Recife",
                    LocalDateTime.of(2026, 8, 20, 19, 0),
                    "Teatro", "AP-TEA-001",
                    "Espetáculos teatrais de companhias locais.",
                    "O Festival reúne as melhores companhias teatrais de Pernambuco em uma semana de apresentações na Arena Pernambuco.",
                    "https://picsum.photos/seed/tea1/800/400", true, 8000, 5500),

            new Evento("5", "Exposição Arte Contemporânea PE",
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    "Cultural", "AP-CUL-001",
                    "Mostra de arte contemporânea pernambucana.",
                    "Artistas locais expõem obras de pintura, escultura e instalações interativas no espaço cultural da Arena.",
                    "https://picsum.photos/seed/cul1/800/400", true, 3000, 1800),

            new Evento("6", "Copa do Nordeste — Semifinal",
                    LocalDateTime.of(2026, 5, 10, 21, 0),
                    "Futebol", "AP-FUT-002",
                    "Semifinal da Copa do Nordeste na Arena.",
                    "As equipes semifinalistas se enfrentam em busca de uma vaga na grande final da Copa do Nordeste 2026.",
                    "https://picsum.photos/seed/fut2/800/400", true, 45000, 41000),

            new Evento("7", "Show Nação Zumbi",
                    LocalDateTime.of(2026, 10, 18, 21, 0),
                    "Música", "AP-MUS-002",
                    "Nação Zumbi ao vivo na Arena Pernambuco.",
                    "A banda símbolo do manguebeat apresenta seus maiores sucessos e faixas do novo álbum em show exclusivo.",
                    "https://picsum.photos/seed/mus2/800/400", true, 20000, 14000),

            new Evento("8", "Seminário Cidades Inteligentes",
                    LocalDateTime.of(2026, 11, 8, 8, 30),
                    "Corporativo", "AP-COR-002",
                    "Debate sobre cidades inteligentes no Nordeste.",
                    "Especialistas, gestores públicos e acadêmicos discutem soluções tecnológicas para os desafios urbanos do Nordeste.",
                    "https://picsum.photos/seed/cor2/800/400", true, 2000, 980),

            new Evento("9", "Festival Gastronômico Arena",
                    LocalDateTime.of(2026, 12, 5, 11, 0),
                    "Cultural", "AP-CUL-002",
                    "Gastronomia regional na Arena Pernambuco.",
                    "Chefs renomados e cozinheiros da culinária popular apresentam pratos típicos em um festival aberto ao público.",
                    "https://picsum.photos/seed/cul2/800/400", true, 10000, 7200),

            new Evento("10", "Evento Cancelado — Teste",
                    LocalDateTime.of(2026, 12, 31, 23, 59),
                    "Cultural", "AP-CUL-003",
                    "Evento inativo para testes.",
                    "Este evento foi marcado como inativo e não deve aparecer nas listagens públicas.",
                    "https://picsum.photos/seed/cul3/800/400", false, 1000, 0)
    )));

    @Override
    public List<Evento> buscarTodos() {
        return Collections.unmodifiableList(eventos);
    }

    @Override
    public List<Evento> buscarAtivos() {
        return eventos.stream()
                .filter(Evento::ativo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Evento> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return eventos.stream()
                .filter(e -> e.id().equals(id))
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
    public Evento salvar(Evento evento) {
        if (buscarPorId(evento.id()).isPresent()) {
            throw new IllegalArgumentException("Evento com id '" + evento.id() + "' já existe");
        }
        eventos.add(evento);
        return evento;
    }

    @Override
    public Evento atualizar(String id, Evento evento) {
        synchronized (eventos) {
            for (int i = 0; i < eventos.size(); i++) {
                if (eventos.get(i).id().equals(id)) {
                    eventos.set(i, evento);
                    return evento;
                }
            }
        }
        throw new EventoNaoEncontradoException(id);
    }

    @Override
    public void remover(String id) {
        boolean removido = eventos.removeIf(e -> e.id().equals(id));
        if (!removido) {
            throw new EventoNaoEncontradoException(id);
        }
    }

    @Override
    public Evento salvar(Evento evento) {
        if (buscarPorId(evento.id()).isPresent()) {
            throw new IllegalArgumentException("Evento com id '" + evento.id() + "' já existe");
        }
        eventos.add(evento);
        return evento;
    }

    @Override
    public Evento atualizar(String id, Evento evento) {
        synchronized (eventos) {
            for (int i = 0; i < eventos.size(); i++) {
                if (eventos.get(i).id().equals(id)) {
                    eventos.set(i, evento);
                    return evento;
                }
            }
        }
        throw new EventoNaoEncontradoException(id);
    }

    @Override
    public void remover(String id) {
        boolean removido = eventos.removeIf(e -> e.id().equals(id));
        if (!removido) {
            throw new EventoNaoEncontradoException(id);
        }
    }
}
