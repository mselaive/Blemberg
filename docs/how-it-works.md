# How Blemberg Works

Blemberg is a market-data service for NexusXVA. It answers two questions:

- Is this underlying symbol known and active?
- Which market-data inputs should NexusXVA use for European option pricing?

Blemberg does not price options. NexusXVA owns portfolio storage, pricing, exposure, and XVA workflows.

## Runtime Flow

1. Blemberg starts with Spring Boot and connects to PostgreSQL.
2. Flyway creates the schema and seeds the initial instrument watchlist.
3. Provider refreshes call Twelve Data and FRED.
4. Successful provider responses are stored in PostgreSQL.
5. Public read endpoints serve cached data from PostgreSQL.

Normal NexusXVA requests do not call Twelve Data or FRED directly. This keeps pricing requests fast, predictable, and protected from provider rate limits.

## Data Sources

- Twelve Data: quotes and daily historical bars for equities and ETFs.
- FRED: US Treasury risk-free rates.

API keys are read from environment variables:

```bash
TWELVE_DATA_API_KEY=...
FRED_API_KEY=...
```

For local Docker usage, put them in `.env` at the repository root. Do not commit `.env`.

## Stored Data

Main tables:

- `instruments`: public symbols, names, asset class, exchange, currency, provider mapping, active flag.
- `market_price_snapshots`: cached latest quote data.
- `market_daily_bars`: daily OHLCV bars used for volatility.
- `risk_free_rates`: FRED Treasury rates stored as decimals.
- `dividend_yields`: dividend yield assumptions.
- `refresh_runs`: refresh status and failures.
- `refresh_run_items`: per-job diagnostics for provider, symbol, tenor, status, and sanitized error messages.

## Pricing Inputs

The pricing-input endpoint combines:

- `spot`: latest cached `last_price`.
- `volatility`: 60-day historical realized volatility from daily closes.
- `riskFreeRate`: interpolated or nearest Treasury rate for the maturity.
- `dividendYield`: provider/manual value, or `0.0` with `UNKNOWN_ZERO`.
- `stale`: `true` if any required data is older than the freshness policy.

Volatility is historical realized volatility, not implied volatility.

## Refresh Behavior

Refreshes are available through:

```http
POST /api/admin/market-data/refresh
```

Schedulers also refresh:

- Snapshots hourly.
- Daily bars after US market close.
- FRED rates daily.

If a provider fails, Blemberg keeps the latest usable cached value and records the failure in `refresh_runs`.

Refresh diagnostics are stored in `refresh_run_items`. Use:

```http
GET /api/admin/market-data/refresh-runs
GET /api/admin/market-data/refresh/{runId}
GET /api/admin/market-data/refresh/latest
```

The manual refresh is optimized for development with the Twelve Data free tier. It refreshes FRED tenors tolerantly, ensures dividend defaults, then processes priority symbols as `quote + daily bars`: AAPL, AMZN, MSFT, and SPY before the rest of the watchlist. If Twelve Data rate limits are reached, remaining work is marked as `SKIPPED_RATE_LIMIT` instead of becoming opaque provider failures.

## Diagnostic Endpoints

- `GET /api/market-data/snapshots?symbols=AAPL,MSFT`: returns available cached snapshots plus `missingSymbols`.
- `GET /api/market-data/daily-bars?symbol=AAPL&limit=90`: returns cached bars or a clean `404`.
- `GET /api/market-data/risk-free-rates`: returns cached FRED Treasury rates as decimals, or an empty list.
- `GET /actuator/mappings`: exposed for local endpoint diagnostics.
- `GET /v3/api-docs`: returns clean `501 Not Implemented`; OpenAPI is intentionally not enabled in V1.
