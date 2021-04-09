CREATE TYPE trade.trade_status as ENUM ('SUBMITTED', 'CANCELLED', 'COMPLETED', 'FAILED');
CREATE TYPE trade.trade_side as ENUM ('BUY', 'SELL');