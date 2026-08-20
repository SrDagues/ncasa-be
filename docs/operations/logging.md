# Backend logging runbook

The backend writes one ECS JSON object per line to standard output and to
`${NCASA_LOG_FILE}`, which defaults to `logs/ncasa.log`.

Production must mount the log directory on persistent storage. Active files rotate
daily or at 25 MB. Compressed archives are retained for up to 30 days with a total
archive limit of 1 GB.

## Follow current logs

```bash
tail -F logs/ncasa.log | jq
```

## Find a request

Use the value returned in the `X-Request-ID` response header or in an API error:

```bash
jq 'select(.requestId == "REQUEST_ID")' logs/ncasa.log
```

## Filter operational events

```bash
jq 'select(.event.action == "http_request_completed")' logs/ncasa.log
jq 'select(.log.level == "ERROR")' logs/ncasa.log
jq 'select(.userId == "USER_ID")' logs/ncasa.log
jq 'select(."process.thread.name" == "THREAD_NAME")' logs/ncasa.log
```

`@timestamp` is UTC and can be used to sort or restrict a search. `requestId` is the
correlation identifier; thread names are diagnostic because server threads are reused.

## Search rotated archives

```bash
zgrep 'REQUEST_ID' logs/ncasa.log.*.gz
```

## Docker

Console output has the same JSON structure and can be followed without opening the
file inside the container:

```bash
docker logs --follow CONTAINER_NAME | jq
```

Do not log request bodies, query strings, email addresses, authorization headers,
cookies, passwords, JWTs, refresh tokens or invitation tokens.
