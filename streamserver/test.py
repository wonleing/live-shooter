#!/usr/bin/python
import xmlrpclib, os, time
uname = "test@sina.com"
usns = "sina"
videotitle = "My first video"
snsid = "fowqjfsadkfjaldf"
vsample = "/tmp/raiders.mp4"
ftpdir = "/var/ftp/pub/"
s=xmlrpclib.ServerProxy("http://127.0.0.1:8000")

userid = s.loginUser(uname, usns)
print "create user:", userid

videoid = s.genFilename()
print "upload video file name is:", videoid

if s.addTitle(userid, videoid, videotitle):
    print "add tiltle %s for %s" %(videotitle, videoid)

os.system("cp %s %s%s.mp4" %(vsample, ftpdir, videoid))
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
