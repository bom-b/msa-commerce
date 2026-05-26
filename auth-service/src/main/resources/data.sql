INSERT INTO users (id, username, password)
VALUES (1, 'test', 'test')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_balances (user_id, balance)
VALUES (1, 50000)
ON CONFLICT (user_id) DO NOTHING;
