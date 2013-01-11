#!/usr/bin/python
import xmlrpclib, os, time
from ftplib import FTP

IP = "liveshooter.cn.mu"
ftp = FTP(IP, "live", "shooter")
uname = "test@sina.com"
usns = "sina"
videotitle = "My first video"
snsid = "fowqjfsadkfjaldf"
vsample = "/home/leon/download/raiders.mp4"
ftpdir = "/var/ftp/pub/"
s=xmlrpclib.ServerProxy("http://%s:8000" %IP)

userid = s.loginUser(uname, usns)
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

ul = s.getFollowing(userid)
print "user", userid, "following these users:", str(ul)

ul = s.getFollower(userid)
print "user", userid, "is followed by these users:", str(ul)

vl = s.getUserVideo(userid)
print "user", userid, "has uploaded following video", str(vl)

vl = s.getFeed(userid)
print "user", userid, "feed list is:", str(vl)
print "API testing finished!"
