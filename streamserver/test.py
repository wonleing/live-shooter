#!/usr/bin/python
import xmlrpclib, os
from ftplib import FTP

IP = "liveshooter.cn.mu"
ftp = FTP(IP, "live", "shooter")
uname = "test@sina.com"
usns = "sina"
nickname = "DemonLeon"
icon = "http://tp4.sinaimg.cn/1435494115/180/5613100011/1"
videotitle = "My first video"
snsid = "fowqjfsadkfjaldf"
vsample = "/home/leon/download/raiders.mp4"
ftpdir = "/var/ftp/pub/"
s=xmlrpclib.ServerProxy("http://%s:8000" %IP)

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

if s.likeVideo(userid, videoid):
    print "user", userid, "liked video", videoid

if s.followUser(1, 1):
    print "user 1 followed himself"

ul = s.getFollowing(1)
print "user 1 following these users:", str(ul)

if s.followVideo(userid, 'inittest'):
    print "user", userid, "followed the owner of video inittest"

ul = s.getFollower(1)
print "user 1 is followed by these users:", str(ul)

ru = s.getRecommandUser()
print "recommad user list is", str(ru)

rv = s.getRecommandVideo()
print "recommad video list is", str(rv)

if s.unfollowUser(1, 1):
    print "user 1 unfollowed himself"

if s.unfollowVideo(userid, 'inittest'):
    print "user", userid, "unfollowed the owner of video inittest"

up = s.getUserProfile(userid)
print "retrieved user", userid, "profile", str(up)

vl = s.getUserVideo(userid)
print "user", userid, "has uploaded following video", str(vl)

vl = s.getFeed(userid)
print "user", userid, "feed list is:", str(vl)

if s.unlikeVideo(userid, videoid):
    print "user", userid, "unliked video", videoid

if s.changeUserType(1, userid, 'business'):
    print "Admin 1 changed user", userid, "type to business"

#if s.deleteVideo(1, videoid):
#    print "Admin 1 deleted video", videoid

print "API testing finished!"
