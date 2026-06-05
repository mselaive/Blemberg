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
  "status": "SUCCESS",
  "startedAt": "2026-06-02T12:00:00Z",
  "finishedAt": "2026-06-02T12:00:08Z",
  "symbolsRequested": 25,
  "symbolsSucceeded": 25,
  "symbolsFailed": 0
}
```

`PARTIAL_SUCCESS` can happen if a provider fails for some symbols or rate limits are reached.

## Get Cached Snapshots

```bash
curl "http://localhost:8081/api/market-data/snapshots?symbols=AAPL,MSFT"
```

Example response:

```json
[
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
]
```

This endpoint reads persisted cache only. It does not call Twelve Data.

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
curl "http://localhost:8081/api/market-data/snapshots?symbols=AAPL,MSFT"
curl "http://localhost:8081/api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-06-01"
```
