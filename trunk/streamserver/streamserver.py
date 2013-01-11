#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
import os, sys, random, optparse, logging, time, lsdb, thread

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

class StreamServer:
    def __init__(self):
        self.db = lsdb.DB()
        
    def _createHtml(self, videoid):
        t = open("template.html", "r")
        os.mkdir(httpdir+videoid)
        n = open(httpdir+videoid+"/index.html", "w")
        n.write(t.read().replace("FileName", videoid))
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
        vinfo = os.popen("./midentify.sh %s" %infile).read().split("\n")
        for k in vinfo:
            if "WIDTH" in k:
                width = int(k.split("=")[1])
            if "HEIGHT" in k:
                height = int(k.split("=")[1])
            if "VIDEO_BITRATE" in k:
                vbit = int(k.split("=")[1])/1000
            if "AUDIO_BITRATE" in k:
                abit = int(k.split("=")[1])/1000
        os.system("vlc %s --sout='#transcode{width=%d,height=%d,vcodec=h264,vb=%d,venc=x264{aud,profile=baseline,\
        level=30,keyint=30,ref=1},acodec=aac,ab=%d,deinterlace}:std{access=livehttp{seglen=%d,delsegs=true,numsegs=%d,index=%s.m3u8,\
        index-url=%s-########.ts},mux=ts{use-key-frames},dst=%s-########.ts}' vlc://quit -I dummy" \
        % (infile, width, height, vbit, abit, segmentlength, segnum, outname, exportname, outname))
        self._cleanup(videoid)

    def loginUser(self, uname, usns):
        ret = self.db.selectUserID(uname, usns)
        if not ret:
            logger.info("User %s at %s is 1st time of using our app" %(uname, usns))
            ret = self.db.createUser(uname, usns)
        logger.info("User %d logged in" %ret)
        return ret

    def genFilename(self):
        logger.info("start to upload client video file to server ftp")
        rand = "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))
        if os.path.exists(uploadpath + rand):
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
        logger.debug("Transcode for %s to HLS completed" % videoid)
        return exportdir + videoid

    def shareVideo(self, videoid, snsid): 
        logger.debug("video %s published on sns web with id: %s" % (videoid, snsid))
        return self.db.shareVideo(videoid, snsid)

    def likeVideo(self, userid, videoid):
        logger.debug("user %d like video %s, try add feed and score" % (userid, videoid))
        if self.db.likeVideo(userid, videoid):
            logger.debug("feed added, score added")
            return True
        else:
            logger.debug("already liked before, wont add feed or score")
            return False

    def getFollowing(self, userid):
        ul = ()
        for u in self.db.getFollowing(userid):
            ul += u
        logger.debug("%d following list is: %s" % (userid, str(ul)))
        return ul

    def getFollower(self, userid):
        ul = ()
        for u in self.db.getFollower(userid):
            ul += u
        logger.debug("%d follower list is: %s" % (userid, str(ul)))
        return ul

    def getUserVideo(self, userid):
        # returned format is: [(vid, vtitle, snsid, score, createdate),(...)...]
        return self.db.getUserVideo(userid)

    def getFeed(self, userid):
        fl = self.db.getLikeVideo(userid)
        for u in self.getFollowing(userid):
            fl += self.getUserVideo(u)
        logger.debug("%d feed list is %s" % (userid, str(fl)))
        return fl


# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
