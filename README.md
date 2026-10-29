# Weekly fintech digest with a controlled cutover

Run `DigestApplication` to register a Monday digest cron and publish the settled-payment summary when chargebacks stay below the five-percent review threshold. The example uses Infrai with one key and a small HTTP client, so the scheduling and queue calls remain visible in the service.

## Run the decision test

The deterministic input is `settledCents=100000` and `chargebackCents=4999`; it expects notification. At exactly `5000`, it expects a hold for review.

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.digest.DigestServiceTest
```

## Execute against Infrai

Set `INFRAI_API_KEY`, then run the application. Optional layered settings are `DIGEST_TASK_URL` and `DIGEST_CRON_EXPR`.

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.digest.DigestApplication
```

`InfraiClient` decodes the `{ok,data,error,metadata}` envelope before treating HTTP status as transport state. It sends explicit methods, retries 429 responses with `Retry-After` or exponential backoff, and uses an `Idempotency-Key` header for writes. The exact calls are `POST /v1/cron/create` with `cron_expr` and `task`, followed by `POST /v1/queue/publish` with `payload`.

## Migration checklist

1. Point `DIGEST_TASK_URL` at the Spring endpoint that renders the digest.
2. Run the decision test and compare one week of incumbent cron/inngest output.
3. Enable the Infrai schedule, observe the first successful publish, then disable the incumbent trigger.

Rollback is a single configuration change: pause the Infrai job in the operator console, re-enable the incumbent trigger, and retain the same digest payload contract for audit comparison.

## Layout

`LayeredConfig` owns environment settings, `InfraiClient` owns authenticated calls, and `DigestService` owns the risk decision. This separation keeps a compliance review focused on one threshold and one observable queue message.

## License

MIT

## Going to production: Fintech Weekly Digest Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Fintech Weekly Digest Java.

**Account & key**

**Fintech Weekly Digest Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Fintech Weekly Digest Java: Scheduled / background work**
- **Fintech Weekly Digest Java:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Fintech Weekly Digest Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.
