create sequence phone_SEQ start with 1 increment by 1;

create table phone
(
    id bigserial not null primary key,
    number varchar(50) not null,
    client_id bigint not null
);