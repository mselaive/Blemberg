# Running Blemberg Locally

## Requirements

- Java 21+
- Maven
- Docker Desktop or another Docker daemon
- Twelve Data API key
- FRED API key

## Environment File

Create `.env` in the repository root:

```bash
TWELVE_DATA_API_KEY=your_twelve_data_key
FRED_API_KEY=your_fred_key

BLEMBERG_APP_PORT=8081
BLEMBERG_POSTGRES_PORT=55432
```

Docker Compose reads `.env` automatically.

## Start With Docker

```bash
docker compose up --build
```

Default host ports:

- API: `http://localhost:8081`
- PostgreSQL: `localhost:55432`

These ports are intentionally different from common NexusXVA ports.

## Verify The App

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## First Data Refresh

After the app is up, run:

```bash
curl -X POST http://localhost:8081/api/admin/market-data/refresh
```

Before this refresh, instruments exist but snapshots, historical bars, and rates may not exist yet. The manual refresh prioritizes AAPL, AMZN, MSFT, and SPY so NexusXVA can validate pricing integration quickly even with the Twelve Data free tier.

Inspect the latest refresh:

```bash
curl http://localhost:8081/api/admin/market-data/refresh-runs
curl http://localhost:8081/api/admin/market-data/refresh/latest
```

Smoke-test cached market data:

```bash
curl "http://localhost:8081/api/market-data/snapshots?symbols=AAPL,SPY,QQQ,MSFT"
curl "http://localhost:8081/api/market-data/daily-bars?symbol=AAPL&limit=5"
curl http://localhost:8081/api/market-data/risk-free-rates
curl "http://localhost:8081/api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-06-01"
curl "http://localhost:8081/api/market-data/pricing-inputs/european-option?symbol=AMZN&maturityDate=2027-06-01"
```

## Stop

Stop containers:

```bash
docker compose down
```

Stop and remove the database volume:

```bash
docker compose down -v
```

## Run Tests

```bash
mvn test
```

Quiet mode:

```bash
mvn -q test
```

With `-q`, Maven prints little or nothing when tests pass. Reports are written to:

```text
target/surefire-reports/
```

The PostgreSQL Testcontainers test is skipped if Docker is not available.
