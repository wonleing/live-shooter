#!/usr/bin/python
# -*- coding: utf-8 -*-
from SimpleXMLRPCServer import SimpleXMLRPCServer
import os, sys, random, string, optparse, logging, time, lsdb, thread, json, datetime, urllib2

u = """./%prog -s <internal_ip> -p <public_dns>\nDebug: ./%prog -s 127.0.0.1"""
parser = optparse.OptionParser(u)
parser.add_option('-s', '--server', help='stream server internal ip address', dest='serverip')
parser.add_option('-p', '--public', help='public DNS', dest='publicdns')
(options, leftargs) = parser.parse_args()
if options.serverip == None:
    parser.print_help()
    sys.exit()
if options.publicdns == None:
    options.publicdns = options.serverip
print "Starting service on", options.serverip, "with public address:", options.publicdns
server = SimpleXMLRPCServer((options.serverip, 8000))
server.register_introspection_functions()

segmentlength = 10
segnum = 999
refresh_days = 100
max_vbit = 700
max_abit = 128
uploadpath = "/var/ftp/pub/"
httpdir = "/var/www/"
exportdir = "http://" + options.publicdns + "/"
logfile = "/var/log/liveshooter.log"
logger = logging.getLogger('Live shooter')
hdlr = logging.FileHandler(logfile)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.INFO)
sina_friend_url = 'https://api.weibo.com/2/friendships/friends.json?screen_name=%s&access_token=2.00taLJZBUj6VFBef805927ebBSuBKE'

class TimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime.datetime):
            return obj.isoformat()
        return json.JSONEncoder.default(self, obj)

class StreamServer:

    def _createHtml(self, videoid, ext):
        t = open("template.html", "r")
        os.mkdir(httpdir+videoid)
        n = open(httpdir+videoid+"/index.html", "w")
        n.write(t.read().replace("FileName", videoid).replace("FullName", videoid+ext))
        t.close()
        n.close()
        logger.info("html page for %s created" %videoid)

    def _streaming(self, videoid, ext):
        origfile = uploadpath + videoid + ext
        outname = httpdir + videoid + "/" + videoid
        exportname = exportdir + videoid + "/" + videoid
        vinfo = os.popen("./midentify.sh %s" %origfile).readlines()
        logger.info("./midentify.sh %s" %origfile)
        for k in vinfo:
            if "WIDTH" in k:
                width = int(k.split("=")[1])
            if "HEIGHT" in k:
                height = int(k.split("=")[1])
            if "VIDEO_BITRATE" in k:
                vbit = int(k.split("=")[1])/1000
                if vbit > max_vbit:
                    vbit = max_vbit
            if "AUDIO_BITRATE" in k:
                abit = int(k.split("=")[1])/1000
                if abit > max_abit:
                    abit = max_abit
        logger.debug("video width: %d, height: %d, vbit: %d, abit: %d" %(width, height, vbit, abit))
        if ext in (".flv", ".webm", ".ogv", ".mp4"):
            infile = outname + ext
            self._createHtml(videoid, ext)
            os.system("ffmpeg -i %s -ss 1 -vframes 1 %s.jpeg > /dev/null 2>&1" %(origfile, outname))
            os.system("cp %s %s" %(origfile, infile))
        else:
            infile = outname + ".flv"
            self._createHtml(videoid, ".flv")
            os.system("ffmpeg -i %s -ss 1 -vframes 1 %s.jpeg > /dev/null 2>&1" %(origfile, outname))
            os.system("ffmpeg -i %s -b %dk -s %dx%d -r 20 -acodec copy %s > /dev/null 2>&1" %(origfile, vbit, width, height, infile))
        os.system("vlc %s --sout='#transcode{width=%d,height=%d,vcodec=h264,vb=%d,fps=25,venc=x264{aud,profile=baseline,\
        level=30,keyint=30,ref=1},acodec=mp4a,ab=%d,deinterlace}:std{access=livehttp{seglen=%d,delsegs=true,numsegs=%d,index=%s.m3u8,\
        index-url=%s-########.ts},mux=ts{use-key-frames},dst=%s-########.ts}' vlc://quit -I dummy > /dev/null 2>&1;rm -rf %s" \
        % (origfile, width, height, vbit, abit, segmentlength, segnum, outname, exportname, outname, origfile))
        logger.info("Transcode for %s to HLS completed" % origfile)

    def loginUser(self, uname, usns, nickname, uicon):
        db = lsdb.DB()
        ret = db.selectUserID(uname, usns)
        if not ret:
            ret = db.createUser(uname, usns, nickname, uicon)
            logger.info("User %s at %s is 1st time of using our app, created userid %d" %(uname, usns, ret))
        else:
            db.updateUser(ret, nickname, uicon)
            logger.info("User %s at %s already has userid %d, updated its nickname and icon" %(uname, usns, ret))
        return ret

    def genFilename(self):
        rand = "".join(random.sample(string.ascii_letters+string.digits, 8))
        if os.path.exists( httpdir + rand):
            logger.info("random filename already existed, genFilename again!")
            self._genFilename()
        logger.info("genFilename returns %s as the file name" % rand)
        return rand

    def addTitle(self, userid, videoid, videotitle=""):
        logger.info("user %d create new video %s" %(userid, videoid))
        db = lsdb.DB()
        return db.addVideo(userid, videoid, videotitle)

    def finishUpload(self, videoid):
        logger.info("upload completed, start to covert %s into HLS" % videoid)
        for f in os.listdir(uploadpath):
            if f[:8] == videoid:
                videoname = f
                ext = f[8:]
        try:
            thread.start_new_thread(self._streaming, (videoid, ext))
        except:
            return False
        return exportdir + videoid

    def shareVideo(self, videoid, snsid): 
        logger.info("video %s published on sns web with id: %s" % (videoid, snsid))
        db = lsdb.DB()
        return db.shareVideo(videoid, snsid)

    def likeVideo(self, userid, videoid):
        logger.info("user %d like video %s, try add feed and score" % (userid, videoid))
        db = lsdb.DB()
        return db.likeVideo(userid, videoid)

    def unlikeVideo(self, userid, videoid):
        logger.info("user %d unlike video %s, remove feed and score" % (userid, videoid))
        db = lsdb.DB()
        return db.unlikeVideo(userid, videoid)

    def followUser(self, userid, targetid):
        if userid == targetid:
            logger.warning("invalid operation - %d follow himself" %userid)
            return False
        logger.info("user %d followed user %d" %(userid, targetid))
        db = lsdb.DB()
        return db.followUser(userid, targetid)

    def followVideo(self, userid, videoid):
        logger.info("user %d followed the owner of video %s" %(userid, videoid))
        db = lsdb.DB()
        targetid = db.getVideoUser(videoid)
        if userid == targetid:
            logger.warning("invalid operation - %d follow himself" %userid)
            return False
        return db.followUser(userid, targetid)

    def unfollowUser(self, userid, targetid):
        logger.info("user %d unfollowed user %d" %(userid, targetid))
        db = lsdb.DB()
        return db.unfollowUser(userid, targetid)

    def unfollowVideo(self, userid, videoid):
        logger.info("user %d unfollowed the owner of video %s" %(userid, videoid))
        db = lsdb.DB()
        targetid = db.getVideoUser(videoid)
        if targetid:
            return db.unfollowUser(userid, targetid)

    def getFollowing(self, userid, nojson=False):
        logger.debug("get user %d following list" %userid)
        db = lsdb.DB()
        ret = db.getFollowing(userid)
        if nojson:
            return ret
        return json.dumps(ret)

    def getFollower(self, userid, nojson=False):
        logger.debug("get user %d follower list" %userid)
        db = lsdb.DB()
        ret = db.getFollower(userid)
        if nojson:
            return ret
        return json.dumps(ret)

    def getUserProfile(self, targetid, nojson=False):
        logger.debug("retrieving user %d's profile" %targetid)
        db = lsdb.DB()
        follower_no = len(db.getFollower(targetid))
        following_no = len(db.getFollowing(targetid))
        ret = db.getUserProfile(targetid) + (follower_no, following_no)
        if nojson:
            return ret
        return json.dumps(ret)

    def getUserVideo(self, userid, nojson=False):
        logger.debug("retrieving user %d's video list" %userid)
        db = lsdb.DB()
        ret = db.getUserVideo(userid)
        if nojson:
            return ret
        return json.dumps(ret, cls=TimeEncoder)

    def getFeed(self, userid, nojson=False):
        logger.debug("return feed list of user %d" %userid)
        db = lsdb.DB()
        fl = db.getFeed(userid)
        if nojson:
            return fl
        return json.dumps(fl, cls=TimeEncoder)

    def getRecommandUser(self, nojson=False):
        logger.debug("return top 1 admin, top 50 business, top 50 confirmed, top 50 free users")
        db = lsdb.DB()
        ru = db.getTopUser('admin', 1)
        ru += db.getTopUser('business', 50)
        ru += db.getTopUser('confirmed', 50)
        ru += db.getTopUser('free', 50)
        if nojson:
            return ru
        return json.dumps(ru)

    def getRecommandVideo(self, nojson=False):
        logger.debug("return top 100 video of this week")
        d = datetime.timedelta(days=refresh_days)
        db = lsdb.DB()
        ret = db.getTopVideo(datetime.date.today()-d)
        if nojson:
            return ret
        return json.dumps(ret, cls=TimeEncoder)

    def getSNSfollowing(self, screen_name, usns, nojson=False):
        users = json.load(urllib2.urlopen(sina_friend_url %screen_name))['users']
        db = lsdb.DB()
        ret = []
        for u in users:
            profile = db.selectUserByNickname(u['screen_name'], usns)
            if profile:
                logger.debug("found user %d for %s's friends at %s" %(profile[0], screen_name, usns))
                ret.append(profile)
        if nojson:
            return ret
        return json.dumps(ret)


# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
