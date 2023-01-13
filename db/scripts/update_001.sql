create table person (
    id serial primary key not null,
    login varchar,
    password varchar
);

insert into person (login, password) values
('parsentev', '123'),
('ban', '123'),
('ivan', '123');