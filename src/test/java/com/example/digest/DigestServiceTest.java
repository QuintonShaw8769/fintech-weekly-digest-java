package com.example.digest;

public final class DigestServiceTest {
    public static void main(String[] args) {
        LayeredConfig cfg = new LayeredConfig("test", "https://example.test/digest", "0 9 * * 1");
        DigestService service = new DigestService(null, cfg);
        if (!service.shouldNotify(100_000, 4_999)) throw new AssertionError("below five percent should notify");
        if (service.shouldNotify(100_000, 5_000)) throw new AssertionError("five percent risk must be held");
        System.out.println("DigestServiceTest passed");
    }
}
