package com.arenapernambuco.config;

import com.arenapernambuco.repository.EventoFirebaseRepository;
import com.arenapernambuco.repository.EventoMemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
        var eventosExistentes = firebaseRepo.buscarTodos();
        if (!eventosExistentes.isEmpty()) {
            log.info("Firebase já contém {} evento(s). Seed ignorado.", eventosExistentes.size());
            return;
        }

        log.info("Firebase vazio — populando com dados iniciais...");
        memoryRepo.buscarTodos().forEach(evento -> {
            firebaseRepo.salvar(evento);
            log.info("  Salvo: [{}] {}", evento.id(), evento.titulo());
        });
        log.info("Seed concluído: {} eventos gravados no Firebase.", memoryRepo.buscarTodos().size());
    }
}
