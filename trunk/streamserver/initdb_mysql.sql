ALTER DATABASE liveshooter DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
drop table if exists user, uservideo, video, userlike, followship, videocheck;
create table user(
userid int NOT NULL AUTO_INCREMENT primary key,
username varchar(50),
nickname varchar(50),
icon varchar(200),
sns varchar(20),
type varchar(10));

create table uservideo(
userid int,
videoid varchar(8) primary key);

create table video(
videoid varchar(8) primary key,
title varchar(300),
snsid varchar(50),
score int,
createdate TIMESTAMP DEFAULT localtime);

create table userlike(
seq int NOT NULL AUTO_INCREMENT primary key,
userid int,
videoid varchar(8),
UNIQUE (userid, videoid));

create table followship(
seq int NOT NULL AUTO_INCREMENT primary key,
userid int,
following int,
UNIQUE (userid, following));

create table videocheck(
videoid varchar(8) primary key,
status varchar(10),
operator varchar(20));

-- ALTER table video CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
-- ALTER table user CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
