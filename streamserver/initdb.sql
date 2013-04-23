drop table user;
create table user(
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
createdate datetime default (datetime('now','localtime')));

drop table userlike;
create table userlike(
seq INTEGER PRIMARY KEY AUTOINCREMENT,
userid int,
videoid varchar(8),
UNIQUE (userid, videoid));

drop table followship;
create table followship(
seq INTEGER PRIMARY KEY AUTOINCREMENT,
userid int,
following int,
UNIQUE (userid, following));

drop table videocheck;
create table videocheck(
videoid varchar(8) primary key,
status varchar(10),
operator varchar(20));
