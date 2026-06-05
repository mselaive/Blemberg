# API Requests

Base URL for local Docker:

```text
http://localhost:8081
```

## Health

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## Actuator Mappings

```bash
curl http://localhost:8081/actuator/mappings
```

This is exposed for local diagnostics. It should return `200 OK` with Spring actuator mapping data.

## OpenAPI

OpenAPI is not enabled in Blemberg V1.

```bash
curl -i http://localhost:8081/v3/api-docs
```

Expected status:

```text
501 Not Implemented
```

Expected body is clean JSON with `message: "OpenAPI is not enabled in Blemberg V1."`.

## List Instruments

```bash
curl "http://localhost:8081/api/instruments"
```

Filters:

```bash
curl "http://localhost:8081/api/instruments?active=true"
curl "http://localhost:8081/api/instruments?assetClass=EQUITY"
curl "http://localhost:8081/api/instruments?symbol=AAPL"
```

Example response:

```json
[
  {
    "symbol": "AAPL",
    "active": true,
    "name": "Apple Inc.",
    "assetClass": "EQUITY",
    "exchange": "NASDAQ",
    "currency": "USD",
    "provider": "TWELVE_DATA",
    "providerSymbol": "AAPL"
  }
]
```

## Get Instrument

```bash
curl http://localhost:8081/api/instruments/AAPL
```

Unknown symbol:

```bash
curl -i http://localhost:8081/api/instruments/FAKE
```

Expected status:

```text
404 Not Found
```

## Manual Market Data Refresh

```bash
curl -X POST http://localhost:8081/api/admin/market-data/refresh
```

Example response:

```json
{
  "runId": "5f0d60dc-0a08-44e4-bad8-df5511ee8036",
  "status": "PARTIAL_SUCCESS",
  "startedAt": "2026-06-02T12:00:00Z",
  "finishedAt": "2026-06-02T12:00:08Z",
  "symbolsRequested": 25,
  "symbolsSucceeded": 1,
  "symbolsFailed": 24,
  "jobSummaries": [
    {
      "jobName": "QUOTES",
      "requested": 25,
      "succeeded": 1,
      "failed": 0,
      "skippedRateLimit": 24
    }
  ],
  "errors": [
    {
      "jobName": "QUOTES",
      "provider": "TWELVE_DATA",
      "symbol": "MSFT",
      "status": "SKIPPED_RATE_LIMIT",
      "errorCode": "RATE_LIMIT",
      "message": "Twelve Data free-tier rate limit reached; item was not attempted."
    }
  ]
}
```

`POST /refresh` is a fast dev bootstrap. It prioritizes AAPL first, then continues through the watchlist until the free-tier budget is reached. `PARTIAL_SUCCESS` can happen if provider calls fail or Twelve Data rate limits are reached.

## Get Refresh Detail

```bash
curl http://localhost:8081/api/admin/market-data/refresh/{runId}
```

Example:

```bash
curl http://localhost:8081/api/admin/market-data/refresh/5f0d60dc-0a08-44e4-bad8-df5511ee8036
```

This returns all refresh items by job, provider, symbol or tenor.

## Get Latest Refresh Detail

```bash
curl http://localhost:8081/api/admin/market-data/refresh/latest
```

Use this when a refresh partially fails and you need to inspect which symbols or tenors failed.

## List Recent Refresh Runs

```bash
curl http://localhost:8081/api/admin/market-data/refresh-runs
```

Optional limit:

```bash
curl "http://localhost:8081/api/admin/market-data/refresh-runs?limit=5"
```

The response is a list of recent runs. Each item includes run status, timestamps, symbol counts, `jobSummaries`, and sanitized errors.

## Get Cached Snapshots

```bash
curl "http://localhost:8081/api/market-data/snapshots?symbols=AAPL,MSFT"
```

Example response:

```json
{
  "snapshots": [
    {
      "symbol": "AAPL",
      "lastPrice": 190.0,
      "open": 188.5,
      "high": 191.2,
      "low": 187.9,
      "previousClose": 188.1,
      "volume": 53200000,
      "currency": "USD",
      "asOf": "2026-06-02T12:00:00Z",
      "source": "TWELVE_DATA",
      "stale": false
    }
  ],
  "missingSymbols": ["MSFT"]
}
```

This endpoint reads persisted cache only. It does not call Twelve Data. If at least one requested symbol has a cached snapshot, the API returns `200 OK` with available snapshots and `missingSymbols`. If none exist, it returns `404 Snapshot not found`.

## Get Daily Bars

```bash
curl "http://localhost:8081/api/market-data/daily-bars?symbol=AAPL&limit=90"
```

Example response:

```json
[
  {
    "symbol": "AAPL",
    "barDate": "2026-06-01",
    "open": 188.5,
    "high": 191.2,
    "low": 187.9,
    "close": 190.0,
    "volume": 53200000,
    "source": "TWELVE_DATA"
  }
]
```

If the symbol is known but has no cached bars, the endpoint returns `404 Daily bars not found`.

## Get Risk-Free Rates

```bash
curl http://localhost:8081/api/market-data/risk-free-rates
```

Example response:

```json
[
  {
    "tenorCode": "1Y",
    "tenorMonths": 12,
    "fredSeriesId": "DGS1",
    "rateDecimal": 0.045,
    "observationDate": "2026-06-01",
    "asOf": "2026-06-02T12:00:00Z"
  }
]
```

FRED percentages are stored as decimals. For example, `4.5` from FRED is stored and returned as `0.045`. If no rates are cached yet, this endpoint returns an empty array.

## Get European Option Pricing Inputs

```bash
curl "http://localhost:8081/api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-06-01"
```

Example response:

```json
{
  "symbol": "AAPL",
  "spot": 190.0,
  "volatility": 0.22,
  "volatilityMethod": "HISTORICAL_REALIZED_60D",
  "riskFreeRate": 0.045,
  "rateMethod": "LINEAR_INTERPOLATION",
  "dividendYield": 0.0,
  "dividendYieldMethod": "UNKNOWN_ZERO",
  "currency": "USD",
  "asOf": "2026-06-02T12:00:00Z",
  "source": "BLEMBERG",
  "stale": false
}
```

If no usable cached data exists:

```text
404 Not Found
```

Run a manual refresh first if this happens in a fresh database.

## Common Local Sequence

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/instruments/AAPL
curl -X POST http://localhost:8081/api/admin/market-data/refresh
curl http://localhost:8081/api/admin/market-data/refresh-runs
curl http://localhost:8081/api/admin/market-data/refresh/latest
curl "http://localhost:8081/api/market-data/snapshots?symbols=AAPL,MSFT"
curl "http://localhost:8081/api/market-data/daily-bars?symbol=AAPL&limit=5"
curl http://localhost:8081/api/market-data/risk-free-rates
curl "http://localhost:8081/api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-06-01"
```
