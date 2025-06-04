create table address
(
    id   bigserial not null primary key,
    client_id bigint not null,
    street varchar(50)
);