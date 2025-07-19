create table refresh_tokens(
    id bigint auto_increment primary key,
    token varchar(255) not null,
    user_id bigint not null,
    created_at  datetime default current_timestamp not null,
    expiry_date  datetime default current_timestamp not null,
    is_valid tinyint(1) default 1,

    constraint refresh_tokens_users_id_fk
        foreign key (user_id) references users (id)
);