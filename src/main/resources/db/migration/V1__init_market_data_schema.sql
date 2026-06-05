CREATE TABLE instruments (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    asset_class VARCHAR(32) NOT NULL,
    exchange VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_symbol VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_instruments_asset_class ON instruments(asset_class);
CREATE INDEX idx_instruments_active ON instruments(active);

CREATE TABLE market_price_snapshots (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL REFERENCES instruments(symbol),
    last_price NUMERIC(20, 8) NOT NULL,
    open NUMERIC(20, 8),
    high NUMERIC(20, 8),
    low NUMERIC(20, 8),
    previous_close NUMERIC(20, 8),
    volume BIGINT,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    as_of TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_market_price_snapshots_symbol_as_of ON market_price_snapshots(symbol, as_of DESC);

CREATE TABLE market_daily_bars (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL REFERENCES instruments(symbol),
    bar_date DATE NOT NULL,
    open NUMERIC(20, 8) NOT NULL,
    high NUMERIC(20, 8) NOT NULL,
    low NUMERIC(20, 8) NOT NULL,
    close NUMERIC(20, 8) NOT NULL,
    volume BIGINT,
    provider VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_market_daily_bars_symbol_date UNIQUE(symbol, bar_date)
);

CREATE INDEX idx_market_daily_bars_symbol_date ON market_daily_bars(symbol, bar_date DESC);

CREATE TABLE risk_free_rates (
    id BIGSERIAL PRIMARY KEY,
    tenor_code VARCHAR(8) NOT NULL,
    tenor_months INTEGER NOT NULL,
    fred_series_id VARCHAR(32) NOT NULL,
    rate_decimal NUMERIC(12, 8) NOT NULL,
    observation_date DATE NOT NULL,
    as_of TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_risk_free_rates_tenor_as_of ON risk_free_rates(tenor_months, as_of DESC);

CREATE TABLE dividend_yields (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL REFERENCES instruments(symbol),
    dividend_yield NUMERIC(12, 8) NOT NULL,
    method VARCHAR(32) NOT NULL,
    as_of TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_dividend_yields_symbol_as_of ON dividend_yields(symbol, as_of DESC);

CREATE TABLE refresh_runs (
    id UUID PRIMARY KEY,
    job_name VARCHAR(96) NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    symbols_requested INTEGER NOT NULL DEFAULT 0,
    symbols_succeeded INTEGER NOT NULL DEFAULT 0,
    symbols_failed INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(1000)
);

INSERT INTO instruments(symbol, name, asset_class, exchange, currency, provider, provider_symbol, active) VALUES
('AAPL', 'Apple Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'AAPL', TRUE),
('MSFT', 'Microsoft Corporation', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'MSFT', TRUE),
('NVDA', 'NVIDIA Corporation', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'NVDA', TRUE),
('AMZN', 'Amazon.com Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'AMZN', TRUE),
('GOOGL', 'Alphabet Inc. Class A', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'GOOGL', TRUE),
('META', 'Meta Platforms Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'META', TRUE),
('TSLA', 'Tesla Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'TSLA', TRUE),
('AVGO', 'Broadcom Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'AVGO', TRUE),
('ORCL', 'Oracle Corporation', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'ORCL', TRUE),
('AMD', 'Advanced Micro Devices Inc.', 'EQUITY', 'NASDAQ', 'USD', 'TWELVE_DATA', 'AMD', TRUE),
('JPM', 'JPMorgan Chase & Co.', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'JPM', TRUE),
('BAC', 'Bank of America Corporation', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'BAC', TRUE),
('GS', 'Goldman Sachs Group Inc.', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'GS', TRUE),
('MS', 'Morgan Stanley', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'MS', TRUE),
('C', 'Citigroup Inc.', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'C', TRUE),
('WFC', 'Wells Fargo & Company', 'EQUITY', 'NYSE', 'USD', 'TWELVE_DATA', 'WFC', TRUE),
('SPY', 'SPDR S&P 500 ETF Trust', 'ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'SPY', TRUE),
('QQQ', 'Invesco QQQ Trust', 'ETF', 'NASDAQ', 'USD', 'TWELVE_DATA', 'QQQ', TRUE),
('DIA', 'SPDR Dow Jones Industrial Average ETF Trust', 'ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'DIA', TRUE),
('IWM', 'iShares Russell 2000 ETF', 'ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'IWM', TRUE),
('VTI', 'Vanguard Total Stock Market ETF', 'ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'VTI', TRUE),
('TLT', 'iShares 20+ Year Treasury Bond ETF', 'ETF', 'NASDAQ', 'USD', 'TWELVE_DATA', 'TLT', TRUE),
('GLD', 'SPDR Gold Shares', 'METAL_ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'GLD', TRUE),
('SLV', 'iShares Silver Trust', 'METAL_ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'SLV', TRUE),
('CPER', 'United States Copper Index Fund', 'METAL_ETF', 'NYSE_ARCA', 'USD', 'TWELVE_DATA', 'CPER', TRUE);

INSERT INTO dividend_yields(symbol, dividend_yield, method, as_of)
SELECT symbol, 0.0, 'UNKNOWN_ZERO', now()
FROM instruments;
