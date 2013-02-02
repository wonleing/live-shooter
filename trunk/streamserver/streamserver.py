#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
import os, sys, random, string, optparse, logging, time, lsdb, thread, json, datetime

u = """./%prog -s <server_ip>\nDebug: ./%prog -s 127.0.0.1 > /dev/null 2>&1"""
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
uploadpath = "/var/ftp/pub/"
httpdir = "/var/www/"
exportdir = "http://" + options.publicdns + "/"
logfile = "/var/log/liveshooter.log"
logger = logging.getLogger('Live shooter')
hdlr = logging.FileHandler(logfile)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.DEBUG)

class TimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime.datetime):
            return obj.isoformat()
        return json.JSONEncoder.default(self, obj)

class StreamServer:
    def __init__(self):
        self.db = lsdb.DB()
        
    def _createHtml(self, videoid):
        t = open("template.html", "r")
        os.mkdir(httpdir+videoid)
        n = open(httpdir+videoid+"/index.html", "w")
        n.write(t.read().replace("FileName", videoid).replace("BASEURL", options.publicdns))
        t.close()
        n.close()
        logger.debug("html page for %s created" %videoid)

    def _cleanup(self, videoid):
        # Delete original uploaded video files?
        os.system("rm -rf %s" % (uploadpath+videoid))
        logger.info("orignal uploaded video %s completely deleted" %videoid)

    def _streaming(self, videoid):
        infile = uploadpath + videoid
        outname = httpdir + videoid + "/" + videoid
        exportname = exportdir + videoid + "/" + videoid
        vinfo = os.popen("./midentify.sh %s" %infile).readlines()
        for k in vinfo:
            if "WIDTH" in k:
                width = int(k.split("=")[1])
            if "HEIGHT" in k:
                height = int(k.split("=")[1])
            if "VIDEO_BITRATE" in k:
                vbit = int(k.split("=")[1])/1000
            if "AUDIO_BITRATE" in k:
                abit = int(k.split("=")[1])/1000
        logger.debug("video height: %d, width: %d, vbit: %d, abit: %d" %(width, height, vbit, abit))
        os.system("vlc -I dummy --video-filter=scene --vout=dummy --no-audio --scene-ratio=120 --start-time=1 --stop-time=2 \
        --scene-path=%s --scene-prefix=%s --scene-format=jpeg --scene-replace %s vlc://quit &>/dev/null" \
        % (httpdir+videoid+"/", videoid, infile))
        os.system("vlc %s --sout='#transcode{width=%d,height=%d,vcodec=h264,vb=%d,venc=x264{aud,profile=baseline,\
        level=30,keyint=30,ref=1},acodec=aac,ab=%d,deinterlace}:std{access=livehttp{seglen=%d,delsegs=true,numsegs=%d,index=%s.m3u8,\
        index-url=%s-########.ts},mux=ts{use-key-frames},dst=%s-########.ts}' vlc://quit -I dummy" \
        % (infile, width, height, vbit, abit, segmentlength, segnum, outname, exportname, outname))
        logger.debug("Transcode for %s to HLS completed" % infile)
        self._cleanup(videoid)

    def loginUser(self, uname, usns, nickname, uicon):
        ret = self.db.selectUserID(uname, usns)
        if not ret:
            ret = self.db.createUser(uname, usns, nickname, uicon)
            logger.info("User %s at %s is 1st time of using our app, created userid %d" %(uname, usns, ret))
        else:
            self.db.updateUser(ret, nickname, uicon)
            logger.info("User %s at %s already has userid %d, updated its nickname and icon" %(uname, usns, ret))
        return ret

    def genFilename(self):
        rand = "".join(random.sample(string.ascii_letters+string.digits, 8))
        if os.path.exists( httpdir + rand):
            logger.debug("random filename already existed, genFilename again!")
            self._genFilename()
        logger.debug("genFilename returns ==%s== as the file name" % rand)
        return rand

    def addTitle(self, userid, videoid, videotitle=""):
        logger.debug("user %d add title %s to video %s" %(userid, videotitle, videoid))
        return self.db.addVideo(userid, videoid, videotitle)

    def finishUpload(self, videoid):
        logger.debug("upload completed, start to covert %s into HLS" % videoid)
        self._createHtml(videoid)
        try:
            thread.start_new_thread(self._streaming, (videoid,))
        except:
            return False
        return exportdir + videoid

    def shareVideo(self, videoid, snsid): 
        logger.debug("video %s published on sns web with id: %s" % (videoid, snsid))
        return self.db.shareVideo(videoid, snsid)

    def likeVideo(self, userid, videoid):
        logger.debug("user %d like video %s, try add feed and score" % (userid, videoid))
        return self.db.likeVideo(userid, videoid)

    def unlikeVideo(self, userid, videoid):
        logger.debug("user %d unlike video %s, remove feed and score" % (userid, videoid))
        return self.db.unlikeVideo(userid, videoid)

    def followUser(self, userid, targetid):
        logger.debug("user %d followed user %d" %(userid, targetid))
        return self.db.followUser(userid, targetid)

    def followVideo(self, userid, videoid):
        logger.debug("user %d followed the owner of video %s" %(userid, videoid))
        targetid = self.db.getVideoUser(videoid)
        return self.db.followUser(userid, targetid)

    def unfollowUser(self, userid, targetid):
        logger.debug("user %d unfollowed user %d" %(userid, targetid))
        return self.db.unfollowUser(userid, targetid)

    def unfollowVideo(self, userid, videoid):
        logger.debug("user %d unfollowed the owner of video %s" %(userid, videoid))
        targetid = self.db.getVideoUser(videoid)
        return self.db.unfollowUser(userid, targetid)

    def getFollowing(self, userid, nojson=False):
        logger.debug("get user %d following list" %userid)
        ret = self.db.getFollowing(userid)
        if nojson:
            return ret
        return json.dumps(ret)

    def getFollower(self, userid, nojson=False):
        logger.debug("get user %d follower list" %userid)
        ret = self.db.getFollower(userid)
        if nojson:
            return ret
        return json.dumps(ret)

    def getUserProfile(self, targetid, nojson=False):
        logger.debug("retrieving user %d's profile" %targetid)
        follower_no = len(self.db.getFollower(targetid))
        following_no = len(self.db.getFollowing(targetid))
        ret = self.db.getUserProfile(targetid) + (follower_no, following_no)
        if nojson:
            return ret
        return json.dumps(ret)

    def getUserVideo(self, userid, nojson=False):
        logger.debug("retrieving user %d's video list" %userid)
        ret = self.db.getUserVideo(userid)
        if nojson:
            return ret
        return json.dumps(ret, cls=TimeEncoder)

    def getFeed(self, userid, nojson=False):
        logger.debug("return feed list of user %d" %userid)
        fl = self.db.getFeed(userid)
        if nojson:
            return fl
        return json.dumps(fl, cls=TimeEncoder)

    def getRecommandUser(self, nojson=False):
        logger.debug("return top 1 admin, top 50 business, top 50 confirmed, top 50 free users")
        ru = self.db.getTopUser('admin', 1)
        ru += self.db.getTopUser('business', 50)
        ru += self.db.getTopUser('confirmed', 50)
        ru += self.db.getTopUser('free', 50)
        if nojson:
            return ru
        return json.dumps(ru)

    def getRecommandVideo(self, nojson=False):
        logger.debug("return top 100 video of this week")
        d = datetime.timedelta(days=7)
        ret = self.db.getTopVideo(datetime.date.today()-d)
        if nojson:
            return ret
        return json.dumps(ret, cls=TimeEncoder)

# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
