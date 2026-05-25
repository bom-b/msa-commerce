INSERT INTO stocks (product_id, product_name, quantity)
VALUES (1, '노트북', 100),
       (2, '마우스', 100),
       (3, '키보드', 100),
       (4, '모니터', 100),
       (5, '헤드셋', 100)
ON CONFLICT (product_id) DO NOTHING;
