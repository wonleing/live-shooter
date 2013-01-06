#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from lsdb import DB
import os, sys, random, optparse, logging, time, vlc

u = """./%prog -s <server_ip>\nDebug: ./%prog -s 127.0.0.1 > /dev/null 2>&1"""
parser = optparse.OptionParser(u)
parser.add_option('-s', '--server', help='stream server ip addres', dest='serverip')
(options, leftargs) = parser.parse_args()
if options.serverip == None:
    parser.print_help()
    sys.exit()

print "Starting service on", options.serverip
server = SimpleXMLRPCServer((options.serverip, 8000))
server.register_introspection_functions()

segmentlength = 10
segnum = 5
ext = ".mp4"
vinfo = {'width':480, 'hight':360, 'vbit':700, 'abit':96}
uploadpath = "/var/ftp/pub/"
httpdir = "/var/www/live-shooter/"
exportdir = "http://" + options.serverip + "/live-shooter/"
logfile = "/var/log/liveshooter.log"
logger = logging.getLogger('Live shooter')
hdlr = logging.FileHandler(logfile)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.DEBUG)

class StreamServer:
        
    def _createHtml(self, videoid):
        t = open("template.html", "r")
        n = open(httpdir+filename+".html", "w")
        n.write(t.read().replace("FileName", videoid))
        t.close()
        n.close()
        logger.debug("html page for %s created" %videoid)

    def _cleanup(self, videoid):
        # Delete original uploaded video files?
        os.system("rm -rf %s" % (uploadpath+videoid+ext))
        logger.info("orignal uploaded video %s completely deleted" %videoid)

    def loginUser(self, uname, usns):
        ret = DB.selectUserID(uname, usns)
        if not ret:
            return DB.createUser(uname, usns)
        return ret

    def genFilename(self):
        logger.info("start to upload client video file to server ftp")
        rand = "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))
        if os.path.exists(uploadpath + rand):
            logger.debug("random filename already existed, genFilename again!")
            self._genFilename()
        logger.debug("genFilename returns ==%s== as the file name" % rand)
        return rand

    def addTitle(self, uid, videoid, videotitle=""):
        logger.debug("user %d add title %s to video %s" %(uid, videotitle, videoid))
        return DB.addVideo(uid, videoid, videotitle)

    def finishUpload(self, videoid):
        logger.debug("upload completed, start to covert %s into HLS" % videoid)
        infile = uploadpath + videoid + ext
        outname = httpdir + videoid
        exportname = exportdir + videoid
        try:
            ins = vlc.Instance()
            m = ins.media_new("file://%s" % infile, "sout=#transcode{width=%d,height=%d,vcodec=h264,vb=%d,venc=x264{aud,profile=baseline,\
            level=30,keyint=30,ref=1},acodec=aac,ab=%d,deinterlace}:std{access=livehttp{seglen=%d,delsegs=true,numsegs=%d,index=%s.m3u8,\
            index-url=%s-########.ts},mux=ts{use-key-frames},dst=%s-########.ts}"
            % (vinfo['width'], vinfo['hight'], vinfo['vbit'], vinfo['abit'], segmentlength, segnum, outname, exportname, outname))
            p = ins.media_player_new()
            p.set_media(m)
        except:
            return False
        logger.debug("Transcode for %s to HLS completed" % infile)
        self._createHtml(self, videoid)
        self._cleanup(self, videoid)
        return exportname + ".html"

    def shareVideo(self, uid, videoid): 
        global PDIC
        time.sleep(PDIC[filename+"_lag"])
        PDIC[filename].stop()
        logger.debug("live streaming for %s finished" % filename)
        #thread.start_new_thread(self._cleanup, (filename, oauth))
        PDIC.pop(filename)
        PDIC.pop(filename+"_lag")
        logger.debug("current PDIC is: %s" %str(PDIC))
        return True

# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
