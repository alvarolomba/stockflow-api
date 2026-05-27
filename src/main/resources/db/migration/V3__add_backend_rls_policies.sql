create policy backend_users_access on users
    for all
    using (true)
    with check (true);

create policy backend_products_access on products
    for all
    using (true)
    with check (true);

create policy backend_inventory_movements_access on inventory_movements
    for all
    using (true)
    with check (true);

create policy backend_customer_orders_access on customer_orders
    for all
    using (true)
    with check (true);

create policy backend_order_items_access on order_items
    for all
    using (true)
    with check (true);
