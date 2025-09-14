create table photo_share.photo
(
    id            varchar(36)   not null
        primary key,
    filename      varchar(255)  not null,
    original_name varchar(255)  null,
    description   text          null,
    upload_time   datetime      null,
    likes         int default 0 null
);

create table photo_share.photo_likers
(
    photo_id varchar(36) not null,
    liker_ip varchar(45) not null,
    primary key (photo_id, liker_ip),
    constraint photo_likers_ibfk_1
        foreign key (photo_id) references photo_share.photo (id)
            on delete cascade
);