package com.arenapernambuco.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, RequestCounter> contadores = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/verificar".equals(request.getRequestURI())) {

            String ip = request.getRemoteAddr();
            RequestCounter counter = contadores.compute(ip, (key, existing) -> {
                Instant now = Instant.now();
                if (existing == null || existing.isExpired(now)) {
                    return new RequestCounter(now, 1);
                }
                existing.incrementar();
                return existing;
            });

            if (counter.getCount() > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Muitas tentativas. Tente novamente em alguns minutos.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    public void limparContadores() {
        contadores.clear();
    }

    private static class RequestCounter {
        private final Instant windowStart;
        private int count;

        RequestCounter(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }

        boolean isExpired(Instant now) {
            return now.isAfter(windowStart.plusSeconds(WINDOW_SECONDS));
        }

        void incrementar() {
            count++;
        }

        int getCount() {
            return count;
        }
    }
}
