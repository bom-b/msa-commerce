INSERT INTO products (id, product_name, image_name, price)
VALUES (1, '노트북', 'notebook.webp', 1200000),
       (2, '마우스', 'mouse.webp', 35000),
       (3, '키보드', 'keyboard.webp', 89000),
       (4, '모니터', 'monitor.webp', 450000),
       (5, '헤드셋', 'headset.webp', 120000)
ON CONFLICT (id) DO NOTHING;

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));

INSERT INTO stocks (product_id, quantity)
VALUES (1, 100),
       (2, 100),
       (3, 100),
       (4, 100),
       (5, 100)
ON CONFLICT (product_id) DO NOTHING;
