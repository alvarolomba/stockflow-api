alter table if exists users enable row level security;
alter table if exists products enable row level security;
alter table if exists inventory_movements enable row level security;
alter table if exists customer_orders enable row level security;
alter table if exists order_items enable row level security;

drop policy if exists backend_users_access on users;
drop policy if exists backend_products_access on products;
drop policy if exists backend_inventory_movements_access on inventory_movements;
drop policy if exists backend_customer_orders_access on customer_orders;
drop policy if exists backend_order_items_access on order_items;
