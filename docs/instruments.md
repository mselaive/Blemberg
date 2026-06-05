# Instruments

Blemberg V1 seeds a USD-only watchlist for NexusXVA portfolio-pricing V1.

Symbols are normalized to uppercase. Unknown symbols return `404`.

## Tech Equities

| Symbol | Name | Exchange | Currency |
| --- | --- | --- | --- |
| AAPL | Apple Inc. | NASDAQ | USD |
| MSFT | Microsoft Corporation | NASDAQ | USD |
| NVDA | NVIDIA Corporation | NASDAQ | USD |
| AMZN | Amazon.com Inc. | NASDAQ | USD |
| GOOGL | Alphabet Inc. Class A | NASDAQ | USD |
| META | Meta Platforms Inc. | NASDAQ | USD |
| TSLA | Tesla Inc. | NASDAQ | USD |
| AVGO | Broadcom Inc. | NASDAQ | USD |
| ORCL | Oracle Corporation | NYSE | USD |
| AMD | Advanced Micro Devices Inc. | NASDAQ | USD |

## Banks

| Symbol | Name | Exchange | Currency |
| --- | --- | --- | --- |
| JPM | JPMorgan Chase & Co. | NYSE | USD |
| BAC | Bank of America Corporation | NYSE | USD |
| GS | Goldman Sachs Group Inc. | NYSE | USD |
| MS | Morgan Stanley | NYSE | USD |
| C | Citigroup Inc. | NYSE | USD |
| WFC | Wells Fargo & Company | NYSE | USD |

## ETF And Index Proxies

| Symbol | Name | Exchange | Currency |
| --- | --- | --- | --- |
| SPY | SPDR S&P 500 ETF Trust | NYSE_ARCA | USD |
| QQQ | Invesco QQQ Trust | NASDAQ | USD |
| DIA | SPDR Dow Jones Industrial Average ETF Trust | NYSE_ARCA | USD |
| IWM | iShares Russell 2000 ETF | NYSE_ARCA | USD |
| VTI | Vanguard Total Stock Market ETF | NYSE_ARCA | USD |
| TLT | iShares 20+ Year Treasury Bond ETF | NASDAQ | USD |

## Metal ETF Proxies

| Symbol | Name | Exchange | Currency |
| --- | --- | --- | --- |
| GLD | SPDR Gold Shares | NYSE_ARCA | USD |
| SLV | iShares Silver Trust | NYSE_ARCA | USD |
| CPER | United States Copper Index Fund | NYSE_ARCA | USD |

## Query Examples

All active instruments:

```bash
curl "http://localhost:8081/api/instruments?active=true"
```

Only equities:

```bash
curl "http://localhost:8081/api/instruments?assetClass=EQUITY"
```

One symbol:

```bash
curl http://localhost:8081/api/instruments/AAPL
```
