drop table users;
create table users(
userid INTEGER PRIMARY KEY,
username varchar(50),
sns varchar(20),
type varchar(10));

drop table uservideo;
create table uservideo(
userid int,
videoid varchar(8));

drop table video;
create table video(
videoid int,
title varchar(300),
snsid varchar(20),
score int);

insert into users (username, sns, type) values ("wonleing@sina.com", "sina", "pay");
insert into uservideo (userid, videoid) values (1, "inittest");
insert into video (videoid, title) values ("inittest", "This is a test");
