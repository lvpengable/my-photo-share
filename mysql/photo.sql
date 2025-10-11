CREATE DATABASE photo_share;
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
    liker_device_id VARCHAR(255) NOT NULL,
    liker_ip varchar(45)  null,
    liked_at   datetime      null,
    primary key (photo_id, liker_device_id),
    constraint photo_likers_ibfk_1
        foreign key (photo_id) references photo_share.photo (id)
            on delete cascade
);

create table photo_share.photo_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    photo_id VARCHAR(36) NOT NULL,
    commenter_id VARCHAR(255),             -- 可为登录用户ID 或 设备ID
    commenter_name VARCHAR(255),           -- 昵称 / "匿名用户"
    comment_text TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (photo_id) REFERENCES photo(id) ON DELETE CASCADE
);