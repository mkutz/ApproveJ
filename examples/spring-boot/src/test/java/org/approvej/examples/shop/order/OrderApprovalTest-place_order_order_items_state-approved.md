query: SELECT oi.id, oi.quantity, oi.unit_price FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.customer_name = 'Carol'

| id   | quantity | unit_price |
|------|----------|------------|
| [id] | 1        | 49.99      |
