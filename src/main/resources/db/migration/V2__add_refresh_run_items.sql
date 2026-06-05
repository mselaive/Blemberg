CREATE TABLE refresh_run_items (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES refresh_runs(id),
    job_name VARCHAR(96) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    symbol VARCHAR(32),
    tenor_code VARCHAR(8),
    status VARCHAR(32) NOT NULL,
    error_code VARCHAR(64),
    message VARCHAR(500),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_run_items_run_id ON refresh_run_items(run_id);
CREATE INDEX idx_refresh_run_items_run_status ON refresh_run_items(run_id, status);
