package com.example.digest;

import java.io.IOException;

public final class DigestService {
    private final InfraiClient infrai;
    private final LayeredConfig config;
    public DigestService(InfraiClient infrai, LayeredConfig config) { this.infrai = infrai; this.config = config; }

    public boolean shouldNotify(long settledCents, long chargebackCents) {
        return settledCents > 0 && chargebackCents * 100 < settledCents * 5;
    }

    public String schedule(String requestId) throws IOException, InterruptedException {
        return infrai.createCron(config.cronExpr(), config.cronTaskUrl(), requestId);
    }
    public String publish(String digest, String requestId) throws IOException, InterruptedException {
        return infrai.publishDigest(digest, requestId);
    }
}
