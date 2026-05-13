package com.arenapernambuco.config;

import com.arenapernambuco.model.Evento;
import com.arenapernambuco.repository.EventoFirebaseRepository;
import com.arenapernambuco.repository.EventoMemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Profile("firebase")
public class FirebaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirebaseSeeder.class);

    private final EventoFirebaseRepository firebaseRepo;
    private final EventoMemoryRepository memoryRepo;

    public FirebaseSeeder(EventoFirebaseRepository firebaseRepo, EventoMemoryRepository memoryRepo) {
        this.firebaseRepo = firebaseRepo;
        this.memoryRepo = memoryRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Evento> eventosExistentes = firebaseRepo.buscarTodos();

        if (eventosExistentes.isEmpty()) {
            log.info("Firebase vazio — populando com dados iniciais...");
            memoryRepo.buscarTodos().forEach(evento -> {
                firebaseRepo.salvar(evento);
                log.info("  Salvo: [{}] {}", evento.id(), evento.titulo());
            });
            log.info("Seed concluído: {} eventos gravados no Firebase.", memoryRepo.buscarTodos().size());
            return;
        }

        // Firebase já tem dados — migrar capacidade/inscritos zerados com valores do seed
        Map<String, Evento> seedPorId = memoryRepo.buscarTodos().stream()
                .collect(Collectors.toMap(Evento::id, e -> e));

        int atualizados = 0;
        for (Evento existente : eventosExistentes) {
            Evento seed = seedPorId.get(existente.id());
            if (seed == null) continue;

            boolean capacidadeZerada = existente.capacidade() == 0 && seed.capacidade() > 0;
            boolean inscritosZerados = existente.inscritos() == 0 && seed.inscritos() > 0;

            if (capacidadeZerada || inscritosZerados) {
                int novaCapacidade = capacidadeZerada ? seed.capacidade() : existente.capacidade();
                int novosInscritos = inscritosZerados ? seed.inscritos() : existente.inscritos();
                Evento corrigido = new Evento(
                        existente.id(), existente.titulo(), existente.dataHora(),
                        existente.categoria(), existente.codigoVerificacao(),
                        existente.descricaoCurta(), existente.descricaoCompleta(),
                        existente.imagemUrl(), existente.ativo(),
                        novaCapacidade, novosInscritos);
                firebaseRepo.atualizar(existente.id(), corrigido);
                log.info("  Migrado [{}]: capacidade={} inscritos={}",
                        existente.id(), novaCapacidade, novosInscritos);
                atualizados++;
            }
        }

        if (atualizados > 0) {
            log.info("Migração concluída: {} evento(s) atualizados.", atualizados);
        } else {
            log.info("Firebase já contém {} evento(s) com dados completos. Seed ignorado.",
                    eventosExistentes.size());
        }
    }
}
