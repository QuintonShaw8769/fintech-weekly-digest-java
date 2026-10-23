package com.example.digest;

public record LayeredConfig(String apiKey, String cronTaskUrl, String cronExpr) {
    public static LayeredConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String task = System.getenv().getOrDefault("DIGEST_TASK_URL", "https://example.test/digest/run");
        String expr = System.getenv().getOrDefault("DIGEST_CRON_EXPR", "0 9 * * 1");
        return new LayeredConfig(key, task, expr);
    }
}
