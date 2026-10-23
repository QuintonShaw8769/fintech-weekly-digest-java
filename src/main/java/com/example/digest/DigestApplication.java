package com.example.digest;

public final class DigestApplication {
    public static void main(String[] args) throws Exception {
        LayeredConfig config = LayeredConfig.fromEnvironment();
        DigestService service = new DigestService(new InfraiClient(config.apiKey()), config);
        System.out.println("scheduled: " + service.schedule("weekly-digest-schedule"));
        if (service.shouldNotify(125000, 2200)) {
            System.out.println("published: " + service.publish("Weekly settled 125000 cents; chargebacks 2200 cents", "weekly-digest-2026-01"));
        }
    }
}
