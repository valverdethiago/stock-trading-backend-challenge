CREATE TYPE trade.state as ENUM ('AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA', 'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD', 'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY');
CREATE TABLE IF NOT EXISTS trade.address
(
  address_uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  street TEXT NOT NULL,
  city TEXT NOT NULL,
  state trade.state NOT NULL,
  zipcode NUMERIC(9) NOT NULL,
  created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  created_by TEXT,
  updated_by TEXT,
  PRIMARY KEY(address_uuid)
);

CREATE TABLE IF NOT EXISTS trade.account
(
  account_uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
  address_uuid UUID,
  username TEXT NOT NULL,
  email TEXT NOT NULL,
  created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  updated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  created_by TEXT,
  updated_by TEXT,
  PRIMARY KEY(account_uuid),
  UNIQUE(username),
  FOREIGN KEY (address_uuid) REFERENCES trade.address (address_uuid)
);