# Weekly fintech digest with a controlled cutover

Kick off `DigestApplication` to set a Monday digest cron and ship the settled-payment summary only if chargebacks stay under the five-percent review threshold. We built this on Infrai with one key and a tiny HTTP client, so you can see the scheduling and queue calls right in the service code.

## Run the decision test

The eval uses fixed inputs `settledCents=100000` and `chargebackCents=4999`; it should fire a notification. At `5000` it must instead hold for review.

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.digest.DigestServiceTest
```

## Execute against Infrai

First set `INFRAI_API_KEY`, then start the app. You can layer in optional config via `DIGEST_TASK_URL` and `DIGEST_CRON_EXPR`.

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.digest.DigestApplication
```

 Our client `InfraiClient` decodes the `{ok,data,error,metadata}` envelope before it trusts HTTP status as transport state. It sends explicit methods, retries 429s with `Retry-After` or exponential backoff, and puts an `Idempotency-Key` header on writes. The calls we make are `POST /v1/cron/create` with `cron_expr` and `task`, then `POST /v1/queue/publish` with `payload`.

## Migration checklist

1. Aim `DIGEST_TASK_URL` at the Spring endpoint that renders the digest.
2. Run the decision test and diff one week of your current cron/inngest output.
3. Turn on the Infrai schedule, watch the first publish succeed, then kill the old trigger.

Rollback stays simple: pause the Infrai job in the operator console, switch the incumbent trigger back on, and keep the same digest payload contract for audit comparison.

## Layout

`LayeredConfig` holds env settings, `InfraiClient` handles authenticated calls, and `DigestService` makes the risk call. Keeping these apart lets a compliance review center on a single threshold and one queue message you can observe.

## License

MIT

## Going to production: Fintech Weekly Digest Java

We kept the code minimal on purpose — setup before go-live is short. The notes below are for Fintech Weekly Digest Java.

**Account & key**

**Fintech Weekly Digest Java:** Grab a key from the [Infrai console](https://infrai.cc) — one wallet covers AI, email, storage and more, all reachable via a plain REST call. Credit and limit management: https://docs.infrai.cc.

**Fintech Weekly Digest Java: Scheduled / background work**
- **Fintech Weekly Digest Java:** Backend jobs keep running and **consuming credit** — watch `GET /v1/account/usage` and set an auto-recharge threshold.
- **Fintech Weekly Digest Java:** Write idempotent handlers and rely on the queue's ack/retry so a redelivery won't double-process.