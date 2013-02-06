#!/usr/bin/python
#coding=utf-8
import xmlrpclib, os
from ftplib import FTP

IP = "127.0.0.1"
ftp = FTP(IP, "live", "shooter")
uname = "cacino@sina.com"
usns = uname.split('@')[1].split('.')[0]
nickname = "嘚嘚迷糊阁"
icon = "http://tp4.sinaimg.cn/1293220651/50/1263886532/0"
videotitle = "Small sample mpeg4 video"
snsid = "SNSID_OF_THIS_VIDEO"
vsample = "/home/leon/download/sample_mpeg4.m4v"
ftpdir = "/var/ftp/pub/"
s=xmlrpclib.ServerProxy("http://%s:8000" %IP)

######Provison initial data#####
shell_command = '''
cat initdb.sql | sqlite3 liveshooter.db
tl="./insertdata_local.py"
rm -rf /var/www/*
ln -s /home/leon/project/jwplayer /var/www/
$tl wonleing@sina.com DemonLeon http://tp4.sinaimg.cn/1435494115/180/5613100011/1 /home/leon/download/raiders.mp4
$tl test@163.com Babe http://tp3.sinaimg.cn/1686872410/50/5651977239/0 /home/leon/download/smile.flv
$tl abcd@tencent.com Bigbong http://tp2.sinaimg.cn/2558350057/50/5632921435/1 /home/leon/download/Venice-h264.3gp
'''
os.system(shell_command)
################################

userid = s.loginUser(uname, usns, nickname, icon)
print "create user:", userid

videoid = s.genFilename()
print "upload video file name is:", videoid

if s.addTitle(userid, videoid, videotitle):
    print "add tiltle %s for %s" %(videotitle, videoid)

ftp.storbinary('STOR %s' %videoid, open(vsample, 'rb'))
url = s.finishUpload(videoid)
print "Transcode completed, now you can watch %s" %url

if s.shareVideo(videoid, snsid):
    print "add sns string", snsid, "to video", videoid

if s.likeVideo(1, videoid):
    print "user 1 liked video", videoid

if s.followUser(3, 2):
    print "user 3 followed 2"

ul = s.getFollowing(1)
print "user 1 following these users:", str(ul)

if s.followVideo(1, videoid):
    print "1 followed the owner of %s" %videoid

ul = s.getFollower(1)
print "user 1 is followed by these users:", str(ul)

ru = s.getRecommandUser()
print "recommad user list is", str(ru)

rv = s.getRecommandVideo()
print "recommad video list is", str(rv)

if s.unfollowUser(3, 2):
    print "user 3 unfollowed 2"

if s.unfollowVideo(1, videoid):
    print "1 unfollowed the owner of %s" %videoid

up = s.getUserProfile(userid)
print "retrieved user", userid, "profile", str(up)

vl = s.getUserVideo(userid)
print "user", userid, "has uploaded following video", str(vl)

vl = s.getFeed(userid)
print "user", userid, "feed list is:", str(vl)

if s.unlikeVideo(userid, videoid):
    print "user", userid, "unliked video", videoid

fl = s.getSNSfollowing('DemonLeon', 'sina')
print "DemonLeon have following sina friends in our site", fl

print "API testing finished!"
