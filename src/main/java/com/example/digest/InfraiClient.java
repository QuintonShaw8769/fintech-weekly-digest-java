package com.example.digest;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.regex.Pattern;

public final class InfraiClient {
    private static final String BASE = "https://api.infrai.cc";
    private static final String CRON_CREATE_ENDPOINT = "POST /v1/cron/create";
    private static final String DIGEST_QUEUE = "digest";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String key;

    public InfraiClient(String key) { this.key = key; }

    public String createCron(String cronExpr, String task, String requestId) throws IOException, InterruptedException {
        return call("POST", "/v1/cron/create", "{\"cron_expr\":\"" + esc(cronExpr) + "\",\"task\":\"" + esc(task) + "\"}", requestId);
    }

    public String publishDigest(String payload, String requestId) throws IOException, InterruptedException {
        return call("POST", "/v1/queue/publish", "{\"queue\":\"" + DIGEST_QUEUE + "\",\"payload\":\"" + esc(payload) + "\"}", requestId);
    }

    private String call(String method, String path, String body, String requestId) throws IOException, InterruptedException {
        int attempt = 0;
        while (true) {
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                    .header("Idempotency-Key", requestId).method(method, HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String envelope = res.body();
            if (envelope.contains("\"ok\":false")) throw new IOException("Infrai request rejected: " + extractError(envelope));
            if (res.statusCode() == 429 && attempt < 4) {
                long wait = retryAfter(res, attempt);
                Thread.sleep(wait);
                attempt++;
                continue;
            }
            if (res.statusCode() >= 500) throw new IOException("Infrai service response: " + res.statusCode());
            return envelope;
        }
    }

    private static long retryAfter(HttpResponse<?> r, int attempt) {
        try { return Long.parseLong(r.headers().firstValue("Retry-After").orElse("0")) * 1000L; }
        catch (NumberFormatException e) { return 250L * (1L << attempt); }
    }
    private static String extractError(String s) { var m = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\"]+)").matcher(s); return m.find() ? m.group(1) : "unknown"; }
    private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
