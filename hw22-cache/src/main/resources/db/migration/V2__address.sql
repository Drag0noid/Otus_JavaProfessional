create sequence address_SEQ start with 1 increment by 1;

create table address
(
    id   bigserial not null primary key,
    client_id bigint not null,
    street varchar(50)
);