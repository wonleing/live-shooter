drop table users;
create table users(
userid INTEGER PRIMARY KEY AUTOINCREMENT,
username varchar(50),
nickname varchar(50),
icon varchar(200),
sns varchar(20),
type varchar(10));

drop table uservideo;
create table uservideo(
userid int,
videoid varchar(8) primary key);

drop table video;
create table video(
videoid varchar(8) primary key,
title varchar(300),
snsid varchar(50),
score int,
createdate datetime default current_timestamp);

drop table userlike;
create table userlike(
userid int,
videoid varchar(8),
primary key (userid, videoid));

drop table followship;
create table followship(
userid int,
following int,
primary key (userid, following));

insert into users (username, nickname, icon, sns, type) values ("wonleing@sina.com", "DemonLeon", "", "sina", "pay");
insert into uservideo (userid, videoid) values (1, "inittest");
insert into video (videoid, title, snsid, score) values ("inittest", "This is a test", "23asdfoiajdsf", 0);
insert into userlike (userid, videoid) values (2, "inittest");
insert into followship (userid, following) values (1, 2);
