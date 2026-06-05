# Market Data Assumptions

Blemberg V1 is for internal/demo usage. It serves cached market data to NexusXVA and avoids real-time provider calls during portfolio-pricing requests.

## Prices

Equity and ETF prices come from Twelve Data refresh jobs and are persisted as `market_price_snapshots`. The public snapshot and pricing-input endpoints read the latest stored value.

If cached data exists but is older than the configured freshness policy, Blemberg returns it with `stale=true`. If no usable cached value exists, Blemberg returns a clean API error.

## Volatility

V1 returns historical realized volatility, not implied volatility.

Formula:

```text
return_i = ln(close_i / close_i-1)
volatility = stddev(return_i) * sqrt(252)
```

Default window: 60 daily closes.

## Risk-Free Rates

US Treasury rates come from FRED series:

- `DGS1MO`
- `DGS3MO`
- `DGS6MO`
- `DGS1`
- `DGS2`
- `DGS5`
- `DGS10`

FRED values are percentages. Blemberg stores and returns them as decimals, so `4.50` becomes `0.045`.

For option maturities between available tenors, Blemberg uses linear interpolation. Outside the available curve, it uses the nearest tenor.

## Dividend Yield

If no reliable dividend source is available, Blemberg returns `dividendYield: 0.0` with `dividendYieldMethod: UNKNOWN_ZERO`.

## Out Of Scope

V1 does not include:

- Options chains
- Implied volatility
- Volatility surfaces
- FX conversion
- Non-USD portfolio pricing
- Yahoo scraping
