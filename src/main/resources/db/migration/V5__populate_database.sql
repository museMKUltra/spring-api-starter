INSERT INTO categories (name)
VALUES ('Fruits'),
       ('Vegetables'),
       ('Dairy'),
       ('Bakery'),
       ('Meat'),
       ('Seafood'),
       ('Beverages'),
       ('Snacks'),
       ('Frozen Foods'),
       ('Pantry');
INSERT INTO products (name, price, description, category_id)
VALUES ('Banana (1 dozen)', 2.49, 'Fresh Cavendish bananas, perfect for snacks or smoothies.', 1),
       ('Broccoli Crown (500g)', 1.99, 'Crisp green broccoli, rich in vitamins and fiber.', 2),
       ('Whole Milk (1L)', 2.79, 'Fresh whole cow’s milk, ideal for cereal or coffee.', 3),
       ('Sourdough Bread (loaf)', 3.99, 'Handcrafted sourdough bread with a crisp crust and tangy flavor.', 4),
       ('Chicken Breast (500g)', 5.49, 'Boneless, skinless chicken breast, high in protein and lean.', 5),
       ('Salmon Fillet (200g)', 7.99, 'Fresh Atlantic salmon fillet, rich in omega-3 fatty acids.', 6),
       ('Orange Juice (1L)', 3.49, '100% pure squeezed orange juice with no added sugar.', 7),
       ('Potato Chips (150g)', 2.29, 'Crispy salted potato chips made from real potatoes.', 8),
       ('Frozen Peas (1kg)', 4.49, 'Premium garden peas, flash-frozen to lock in freshness.', 9),
       ('Spaghetti Pasta (500g)', 1.89, 'Classic Italian durum wheat spaghetti, perfect for all sauces.', 10);
