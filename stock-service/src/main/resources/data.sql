INSERT INTO stocks (product_id, product_name, quantity, image_name, price)
VALUES (1, '노트북', 100, 'notebook.webp', 1200000),
       (2, '마우스', 100, 'mouse.webp', 35000),
       (3, '키보드', 100, 'keyboard.webp', 89000),
       (4, '모니터', 100, 'monitor.webp', 450000),
       (5, '헤드셋', 100, 'headset.webp', 120000)
ON CONFLICT (product_id) DO NOTHING;
