create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    full_name varchar(120) not null,
    password_hash varchar(255) not null,
    created_at timestamptz not null
);

create table products (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    sku varchar(80) not null,
    name varchar(160) not null,
    price_cents integer not null,
    stock_on_hand integer not null,
    stock_reserved integer not null,
    low_stock_threshold integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique(owner_id, sku)
);

create table inventory_movements (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    product_id bigint not null references products(id),
    type varchar(30) not null,
    quantity integer not null,
    reason varchar(255),
    created_at timestamptz not null
);

create table customer_orders (
    id bigserial primary key,
    owner_id bigint not null references users(id) on delete cascade,
    status varchar(30) not null,
    total_cents integer not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table order_items (
    id bigserial primary key,
    order_id bigint not null references customer_orders(id) on delete cascade,
    product_id bigint not null references products(id),
    quantity integer not null,
    unit_price_cents integer not null,
    line_total_cents integer not null
);

create index idx_products_owner_id on products(owner_id);
create index idx_inventory_movements_owner_id on inventory_movements(owner_id);
create index idx_inventory_movements_product_id on inventory_movements(product_id);
create index idx_customer_orders_owner_id on customer_orders(owner_id);
create index idx_customer_orders_status on customer_orders(status);
create index idx_order_items_order_id on order_items(order_id);
create index idx_order_items_product_id on order_items(product_id);
