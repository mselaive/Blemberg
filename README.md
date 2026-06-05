# Blemberg

Blemberg is a small Spring Boot market-data service for NexusXVA. It validates known underlyings and serves cached market-data inputs for European option pricing.

It is not a Bloomberg replacement and does not price options. NexusXVA remains the owner of portfolios, Black-Scholes pricing, exposure, and XVA workflows.

## Stack

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Data JPA
- Docker Compose
- Testcontainers

## Data Sources

V1 uses:

- Twelve Data Basic/free for equities, ETFs, quotes, and daily historical bars.
- FRED API for US Treasury rates.

Required environment variables:

```bash
export TWELVE_DATA_API_KEY=your_twelve_data_key
export FRED_API_KEY=your_fred_key
```

The application never stores API keys in the repository. Provider refreshes write successful values to PostgreSQL; normal read endpoints serve cached data only.

## Run Locally

Start PostgreSQL and the app:

```bash
docker compose up --build
```

By default Docker publishes:

- Blemberg API: `http://localhost:8081`
- Blemberg PostgreSQL: `localhost:55432`

These host ports intentionally avoid the common NexusXVA ports. Override them only if needed:

```bash
BLEMBERG_APP_PORT=18081 BLEMBERG_POSTGRES_PORT=55433 docker compose up --build
```

Or run the app against a local PostgreSQL:

```bash
mvn spring-boot:run
```

Default local database settings:

- URL: `jdbc:postgresql://localhost:55432/blemberg`
- Username: `blemberg`
- Password: `blemberg`

## API

Health:

```http
GET /actuator/health
```

List instruments:

```http
GET /api/instruments?assetClass=EQUITY&active=true&symbol=AAPL
```

Get an instrument:

```http
GET /api/instruments/AAPL
```

Get cached snapshots:

```http
GET /api/market-data/snapshots?symbols=AAPL,MSFT
```

Get European option pricing inputs:

```http
GET /api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-06-01
```

Manual refresh:

```http
POST /api/admin/market-data/refresh
```

Refresh detail:

```http
GET /api/admin/market-data/refresh/{runId}
GET /api/admin/market-data/refresh/latest
```

## NexusXVA Contract

Pricing inputs include:

- `spot`
- `volatility`
- `riskFreeRate`
- `dividendYield`
- `currency`
- `asOf`
- `source`
- `stale`

Method/audit fields are also returned: `volatilityMethod`, `rateMethod`, and `dividendYieldMethod`.

## Tests

Run:

```bash
mvn test
```

The Testcontainers PostgreSQL migration test is configured with `disabledWithoutDocker = true`, so it is skipped when Docker is not available.

## Notes

- Symbols are normalized to uppercase.
- The seeded V1 watchlist is USD-only.
- Twelve Data calls are rate-limited with defaults of `8` credits/minute and `800` credits/day.
- Historical volatility is realized volatility from daily closes, not implied volatility.
- FRED Treasury percentages are stored and returned as decimals.

See [docs/market-data-assumptions.md](docs/market-data-assumptions.md) for the financial assumptions behind V1.

## Docs

- [How it works](docs/how-it-works.md)
- [Running locally](docs/running-locally.md)
- [API requests](docs/api-requests.md)
- [Instrument watchlist](docs/instruments.md)
- [NexusXVA integration](docs/nexusxva-integration.md)
- [Market-data assumptions](docs/market-data-assumptions.md)
