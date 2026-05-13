package com.arenapernambuco.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("firebase")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream credentials = resolveCredentials();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentials))
                .setDatabaseUrl(databaseUrl)
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("Firebase inicializado com sucesso: {}", databaseUrl);
        return app;
    }

    private InputStream resolveCredentials() {
        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (json != null && !json.isBlank()) {
            log.info("Credenciais Firebase carregadas da variável de ambiente.");
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }

        InputStream stream = getClass().getResourceAsStream(credentialsPath);
        if (stream == null) {
            throw new IllegalStateException(
                "Credenciais Firebase não encontradas. Defina FIREBASE_CREDENTIALS_JSON ou coloque o arquivo em: " + credentialsPath);
        }
        log.info("Credenciais Firebase carregadas do classpath: {}", credentialsPath);
        return stream;
    }

    @Bean
    public DatabaseReference eventosRef(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp).getReference("eventos");
    }

    @Bean
    public DatabaseReference sugestoesRef(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp).getReference("sugestoes");
    }

    @Bean
    public DatabaseReference ingressosRef(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp).getReference("ingressos");
    }
}
