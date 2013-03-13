#!/usr/bin/python
# -*- coding: utf-8 -*-
import xmlrpclib, sys 
from ftplib import FTP

if len(sys.argv) != 5:
    print "usage: "+sys.argv[0]+" <sns_user_name> <nickname> <icon_link> <path_to_video>"
    sys.exit(1)
IP = "127.0.0.1"
ftp = FTP(IP, "live", "shooter")
uname = sys.argv[1]
usns = uname.split('@')[1].split('.')[0]
nickname = sys.argv[2]
icon = sys.argv[3]
videotitle = "%s`s first video" %nickname
vsample = sys.argv[4]
ftpdir = "/var/ftp/pub/"
s=xmlrpclib.ServerProxy("http://%s:8000" %IP)

userid = s.loginUser(uname, usns, nickname, icon)
print "create user:", userid

videoid = s.genFilename()
print "upload video file name is:", videoid
snsid = "FAKE_SNSID_OF_VIDEO_"+videoid

if s.addTitle(userid, videoid, videotitle):
    print "add tiltle %s for %s" %(videotitle, videoid)

ftp.storbinary('STOR %s' %videoid, open(vsample, 'rb'))
url = s.finishUpload(videoid)
print "Transcode completed, now you can watch %s" %url

if s.shareVideo(videoid, snsid):
    print "add sns string", snsid, "to video", videoid

if s.likeVideo(userid, videoid) and s.likeVideo(1, videoid):
    print "Admin and user", userid, "liked video", videoid

if s.followUser(userid, 1) and s.followVideo(1, videoid):
    print "user", userid, "and Admin followed each other"

print "Data inserting finished!"
