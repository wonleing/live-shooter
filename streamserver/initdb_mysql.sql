drop table if exists users, uservideo, video, userlike, followship;
create table users(
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
createdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

create table userlike(
userid int,
videoid varchar(8),
primary key (userid, videoid));

create table followship(
userid int,
following int,
primary key (userid, following));
