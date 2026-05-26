INSERT INTO stocks (product_id, product_name, quantity, image_name)
VALUES (1, '노트북', 100, 'notebook.webp'),
       (2, '마우스', 100, 'mouse.webp'),
       (3, '키보드', 100, 'keyboard.webp'),
       (4, '모니터', 100, 'monitor.webp'),
       (5, '헤드셋', 100, 'headset.webp')
ON CONFLICT (product_id) DO NOTHING;
