CREATE TABLE IF NOT EXISTS trade.trade
(
  trade_uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
  account_uuid UUID NOT NULL,
  symbol TEXT NOT NULL,
  quantity NUMERIC(9) NOT NULL,
  side trade.trade_side NOT NULL,
  price NUMERIC(11,2) NOT NULL,
  status trade.trade_status NOT NULL DEFAULT 'SUBMITTED'::trade.trade_status,
  created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  created_by TEXT,
  updated_by TEXT,
  PRIMARY KEY(trade_uuid),
  FOREIGN KEY (account_uuid) REFERENCES trade.account (account_uuid)
);