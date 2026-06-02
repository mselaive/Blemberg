Blemberg V1 como Market Data Service para NexusXVA
Summary
Crearemos un repo separado llamado Blemberg, un mini market-data service en Java Spring Boot, Docker y PostgreSQL, con la misma arquitectura modular que NexusXVA. Su objetivo no es ser un Bloomberg real, sino resolver el hueco actual: validar que un underlyingSymbol existe y entregar los inputs mínimos para pricing Black-Scholes a nivel portfolio.

Decisión recomendada: Twelve Data + FRED. Twelve Data cubre equities/ETFs/quotes/históricos con free tier de 8 API credits/min y 800/day; FRED entrega series económicas como tasas Treasury vía API oficial. No usaremos opciones/IV real en V1 porque los datos de options suelen ser premium; usaremos volatilidad histórica realizada como proxy inicial.

Fuentes verificadas:

Twelve Data pricing/free tier: https://twelvedata.com/pricing
Twelve Data batch requests: https://support.twelvedata.com/en/articles/5203360-bulk-requests
FRED observations API: https://fred.stlouisfed.org/docs/api/fred/series_observations.html
Alpha Vantage historical options premium: https://www.alphavantage.co/documentation/
Key Changes
Crear Blemberg como servicio REST separado:

Spring Boot, Java 21+, Maven, Docker, PostgreSQL, Flyway, JPA, Testcontainers.
Misma separación: api, application, domain, infrastructure.
No compartir base de datos con NexusXVA; integración solo por HTTP.
Datos mínimos que Blemberg debe manejar:

Reference data: symbol, name, assetClass, exchange, currency, provider, providerSymbol, active.
Market snapshot: symbol, lastPrice, open, high, low, previousClose, volume, currency, asOf, source.
Historical bars: daily OHLCV para calcular volatilidad.
Risk-free curve: FRED Treasury series DGS1MO, DGS3MO, DGS6MO, DGS1, DGS2, DGS5, DGS10.
Derived pricing inputs: spot, historicalVolatility, riskFreeRate, asOf, staleness.
Watchlist inicial recomendada:

Tech/equities: AAPL, MSFT, NVDA, AMZN, GOOGL, META, TSLA, AVGO, ORCL, AMD.
Banks: JPM, BAC, GS, MS, C, WFC.
ETFs/fondos/index proxies: SPY, QQQ, DIA, IWM, VTI, TLT.
Metals via ETF proxies: GLD, SLV, CPER.
La idea es usar ETFs negociables como proxy de índices/metales para que encajen mejor con opciones y pricing.
Public API V1
GET /api/instruments

Lista instrumentos conocidos.
Soporta filtro opcional por assetClass, active, symbol.
GET /api/instruments/{symbol}

Valida existencia del símbolo.
Si no existe o no está activo: 404 ApiError.
GET /api/market-data/snapshots?symbols=AAPL,MSFT,QQQ

Devuelve últimos snapshots guardados.
No llama al proveedor en request normal; lee cache persistida.
GET /api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-12-31

Devuelve:
spot: último precio válido.
volatility: volatilidad histórica anualizada.
riskFreeRate: tasa decimal, interpolada o tenor cercano según maturity.
asOf, source, stale.
Este endpoint será el contrato natural para NexusXVA portfolio-level pricing.
POST /api/admin/market-data/refresh

Trigger manual para dev.
En producción/dev normal también habrá scheduler.
Data + Refresh Policy
Scheduler:

Quotes/snapshots: cada 1 hora para la watchlist.
Historical daily bars: 1 vez al día.
FRED rates: 1 vez al día.
Rate limiter obligatorio para respetar Twelve Data free tier.
Volatilidad:

V1 calcula historicalVolatility con log returns diarios.
Default: ventana de 60 trading days.
Fórmula: stddev(log(close_t / close_t-1)) * sqrt(252).
Nombrarla siempre como histórica/realizada, no implied volatility.
Risk-free:

FRED entrega tasas en porcentaje; Blemberg las guarda y expone como decimal.
Para maturity entre tenores, usar interpolación lineal simple.
Si falta una tasa, usar tenor disponible más cercano y marcar rateMethod.
Prompt Para El Nuevo Repo
You are building Blemberg, a small market-data service that will coexist with NexusXVA.

Context:
- NexusXVA is a Java Spring Boot XVA/risk platform.
- NexusXVA already persists portfolios and European option positions.
- Portfolio positions store trade terms only: underlyingSymbol, optionType, strike, maturityDate, quantity.
- NexusXVA intentionally does not store market data such as spot, volatility, or riskFreeRate inside positions.
- The current problem is that NexusXVA accepts symbols like AAPL or FAKE without checking whether the underlying exists.
- Blemberg must solve this as a separate service, not by adding market data persistence to NexusXVA.

Goal:
Build Blemberg V1 as a Spring Boot REST API that validates instruments and provides pricing inputs for European option pricing.

Architecture:
- Java 21+, Spring Boot, Maven.
- PostgreSQL, Flyway, Spring Data JPA.
- Docker and Docker Compose.
- Testcontainers for integration tests.
- Modular monolith structure:
  - shared
  - instruments/reference-data
  - marketdata
  - providers
  - scheduler/jobs
- Keep controllers thin.
- Application services own use cases and transaction boundaries.
- Domain contains validation and calculation policies.
- Infrastructure contains JPA and external provider adapters.

Data Sources:
- Use Twelve Data as the primary provider for equities, ETFs, prices, and historical bars.
- Use FRED for US Treasury risk-free rate series.
- Do not use Yahoo scraping.
- Do not implement options chains or implied volatility in V1.
- Compute historical realized volatility from daily close prices.

Initial Watchlist:
AAPL, MSFT, NVDA, AMZN, GOOGL, META, TSLA, AVGO, ORCL, AMD,
JPM, BAC, GS, MS, C, WFC,
SPY, QQQ, DIA, IWM, VTI, TLT,
GLD, SLV, CPER.

Core API:
- GET /api/instruments
- GET /api/instruments/{symbol}
- GET /api/market-data/snapshots?symbols=AAPL,MSFT
- GET /api/market-data/pricing-inputs/european-option?symbol=AAPL&maturityDate=2027-12-31
- POST /api/admin/market-data/refresh

Pricing input response must include:
- symbol
- spot
- volatility
- volatilityMethod
- riskFreeRate
- rateMethod
- currency
- asOf
- source
- stale

Persistence:
- instruments table for reference data.
- market_price_snapshots table for latest quotes.
- market_daily_bars table for historical OHLCV.
- risk_free_rates table for FRED Treasury rates.
- refresh_runs table for provider refresh status/errors.

Rules:
- Normalize symbols to uppercase.
- Store provider-specific symbol mapping separately from public symbol.
- Do not expose provider internals in API errors.
- If data is stale, return it with stale=true unless no usable data exists.
- If no usable data exists, return a clean ApiError.
- Blemberg does not price options; NexusXVA owns pricing.
- Blemberg may calculate historical volatility and select/interpolate risk-free rates because those are market-data transformations.

Testing:
- Unit tests for symbol normalization, volatility calculation, and risk-free tenor selection.
- API tests for instrument lookup, unknown symbol, snapshots, and pricing-input response.
- Integration tests with PostgreSQL/Testcontainers.
- Provider adapter tests should use mocked HTTP responses, not real API calls.
- Scheduler/rate-limit behavior should be tested without calling external providers.

Deliverables:
- Working Spring Boot app.
- Docker Compose with PostgreSQL.
- README explaining setup, required API keys, endpoints, and how NexusXVA will consume Blemberg.
- Docs explaining market-data assumptions, especially historical volatility vs implied volatility.
Test Plan
Blemberg tests:

Unknown symbol returns 404 ApiError.
Known symbol returns active reference data.
Snapshot endpoint returns cached data only.
Pricing inputs return finite positive spot and volatility.
FRED percentage rates are converted to decimals.
Historical volatility calculation matches known fixture.
Provider failures are stored in refresh status and do not leak raw provider errors.
Future NexusXVA integration tests:

Creating/updating a position with unknown underlyingSymbol rejects cleanly.
Portfolio-level pricing can request market data from Blemberg and reuse Black-Scholes.
NexusXVA remains owner of portfolios/pricing; Blemberg remains owner of market data.
Assumptions
Blemberg V1 is for learning/demo/internal use, not commercial redistribution of market data.
Free tier limits are acceptable because refresh can run hourly and the watchlist is small.
QQQ is the Nasdaq proxy for V1, not direct Nasdaq index data.
Metals are represented initially through ETFs like GLD, SLV, CPER.
Implied volatility and real options chains are postponed.
No auth, tenants, Kafka, Kubernetes, or frontend in Blemberg V1.
