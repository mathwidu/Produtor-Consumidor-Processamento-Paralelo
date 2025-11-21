package com.trabalho.finalpc.common.config; // Pacote de configuração (comum a serviços)

import java.net.InetAddress; // Para obter o hostname local como fallback
import java.util.Locale;     // Para sanitizar strings de forma previsível (minúsculas e hifens)

/**
 * Eu centralizo a leitura de variáveis de ambiente aqui para padronizar configuração.
 * Minhas decisões:
 * - Fornecer defaults sensatos (localhost:5672, guest/guest) para facilitar execução local e vídeo.
 * - Permitir override por ENV para ID do produtor/consumidor, host/porta e credenciais.
 */
public final class AppConfig { // Classe utilitária (somente métodos estáticos)

    // Nomes padronizados das variáveis de ambiente (contrato de configuração)
    public static final String ENV_RABBITMQ_HOST = "RABBITMQ_HOST"; // host do broker
    public static final String ENV_RABBITMQ_PORT = "RABBITMQ_PORT"; // porta AMQP (padrão 5672)
    public static final String ENV_RABBITMQ_USER = "RABBITMQ_USER"; // usuário (opcional)
    public static final String ENV_RABBITMQ_PASS = "RABBITMQ_PASS"; // senha (opcional)
    public static final String ENV_PRODUCER_ID = "PRODUCER_ID";     // id lógico do produtor
    public static final String ENV_CONSUMER_ID = "CONSUMER_ID";     // id lógico do consumidor

    private AppConfig() { /* Impede instanciação */ }

    // FUTURO: adicionar getRabbitHost(), getRabbitPort(), getRabbitUser(), getRabbitPass()
    // (deixar claro que neste passo implementamos apenas os IDs)

    /** Retorna o host do broker (ex.: "localhost"). Default: localhost */
    public static String getRabbitHost() {
        String v = safeEnv(ENV_RABBITMQ_HOST);
        return v != null ? v : "localhost";
    }

    /** Retorna a porta do broker (ex.: 5672). Default: 5672 */
    public static int getRabbitPort() {
        String v = safeEnv(ENV_RABBITMQ_PORT);
        try { return v != null ? Integer.parseInt(v) : 5672; } catch (NumberFormatException e) { return 5672; }
    }

    /** Retorna o usuário (se aplicável). Default: guest */
    public static String getRabbitUser() {
        String v = safeEnv(ENV_RABBITMQ_USER);
        return v != null ? v : "guest";
    }

    /** Retorna a senha (se aplicável). Default: guest */
    public static String getRabbitPass() {
        String v = safeEnv(ENV_RABBITMQ_PASS);
        return v != null ? v : "guest";
    }

    /**
     * Retorna o identificador lógico do produtor.
     * Política de resolução (legível para o vídeo):
     * 1) Se PRODUCER_ID estiver definida, usamos (controle total).
     * 2) Senão, usamos "producer-" + hostname sanitizado (bom para múltiplos terminais).
     * 3) Senão, caímos em um fallback previsível: "producer-1".
     */
    public static String getProducerId() {
        String fromEnv = safeEnv(ENV_PRODUCER_ID);
        if (fromEnv != null) {
            return fromEnv;
        }
        String host = resolveHostname();
        if (host != null && !host.isBlank()) {
            return "producer-" + sanitize(host);
        }
        return "producer-1"; // Fallback previsível
    }

    /**
     * Retorna o identificador lógico do consumidor.
     * Política de resolução análoga ao produtor (consistência):
     * 1) CONSUMER_ID > 2) consumer-<hostname> > 3) "consumer-1"
     */
    public static String getConsumerId() {
        String fromEnv = safeEnv(ENV_CONSUMER_ID);
        if (fromEnv != null) {
            return fromEnv;
        }
        String host = resolveHostname();
        if (host != null && !host.isBlank()) {
            return "consumer-" + sanitize(host);
        }
        return "consumer-1"; // Fallback previsível
    }

    // ============= Helpers internos (privados) =============

    /** Lê ENV com tolerância: trim, vazio vira null, ignora SecurityException. */
    private static String safeEnv(String name) {
        try {
            String val = System.getenv(name);
            if (val == null) return null;
            val = val.trim();
            return val.isEmpty() ? null : val;
        } catch (SecurityException ignored) {
            return null; // Em ambientes muito restritos, falhar silenciosamente e usar fallback
        }
    }

    /** Obtém o hostname da máquina (ou container) — útil para derivar IDs. */
    private static String resolveHostname() {
        // 1) Tenta ENV HOSTNAME (comum em Linux/containers)
        String envHost = safeEnv("HOSTNAME");
        if (envHost != null) {
            return envHost;
        }
        // 2) Tenta API Java padrão
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return null; // Se falhar, retornamos null e permitimos o fallback estático
        }
    }

    /** Sanitiza nomes para letras/números/hífens em minúsculas (evita caracteres estranhos nos logs). */
    private static String sanitize(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        return lower.replaceAll("[^a-z0-9-]", "-");
    }
}
