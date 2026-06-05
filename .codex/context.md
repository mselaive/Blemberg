# Blemberg Agent Context

Blemberg is a Spring Boot market-data service for NexusXVA.

Core boundaries:

- NexusXVA owns portfolios, pricing, exposure, and XVA workflows.
- Blemberg owns instrument validation and market-data inputs.
- Blemberg does not price options.
- Normal API reads must use persisted cache, not provider calls.

V1 providers:

- Twelve Data for equities, ETFs, snapshots, and daily bars.
- FRED for US Treasury risk-free rates.

Secrets:

- Use `TWELVE_DATA_API_KEY`.
- Use `FRED_API_KEY`.
- Do not commit API keys or real secrets.

Important assumptions:

- USD-only watchlist for V1.
- Historical realized volatility only; no implied volatility in V1.
- No Yahoo scraping.
- No frontend in V1.
