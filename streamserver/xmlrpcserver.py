#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import os, sys, random, optparse, logging, time

u = """%prog -s <server_ip>"""
parser = optparse.OptionParser(u)
parser.add_option('-s', '--server', help='stream server ip addres', dest='serverip')
(options, leftargs) = parser.parse_args()
if options.serverip == None:
    parser.print_help()
    sys.exit()

print "Starting service on", options.serverip
server = SimpleXMLRPCServer((options.serverip, 8000))
server.register_introspection_functions()

segmentlength = 5
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
logger.setLevel(logging.INFO)
logger.setLevel(logging.ERROR)

class StreamServer:
    def _check(self, cmd)
        if not os.path.exists(cmd):
            logger.error("Critical: %s is not installed" % cmd)
            sys.exit()
        
    def _rand(self):
        return "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))

    def genFilename(self):
        self._check("/usr/bin/mplayer")
        self._check("/usr/bin/vlc")
        rand = self._rand()
        if os.path.exists(uploadpath + rand):
            self.genFilename()
        return rand

    def genSegment(self, filename, videotitle):
        infile = uploadpath + filename + ".mp4"
        outfile = httpdir + filename + "_1.ts" 
        os.system("sudo chmod a+r %s" % infile)
        self.videoinfo = {}
        for l in os.popen("mplayer -vo null -ao null -frames 0 -identify %s 2>/dev/null | grep ^ID_" % infile).readlines()
            videoinfo[l.split("=")[0]] = l.split("=")[1][:-1]
        if not self._videoconvert(self.videoinfo, infile, outfile):
            return False

        logger.debug("vlc trancode for %s completed" % infile)
        m = open(httpdir+filename+".m3u8", "w")
        m.write("#EXTM3U\n#EXT-X-TARGETDURATION:%s\n#EXTINF:%s,\n%s%s_1.ts"
               % (segmentlength, segmentlength, exportdir, filname))
        m.close()
        t = open("template.html", "r")
        n = open(httpdir+filename+".html", "w")
        n.write(t.read().replace("FileName", filename).replace("VideoTitle", "videotitle"))
        t.close()
        n.close()
        return True

    def updateSegment(self, filename, seq):
        infile = uploadpath + filename + "_" + seq + ".mp4"
        outfile = httpdir + filename + "_" + seq + ".ts"
        m3file = httpdir + filename + ".m3u8"
        m3tmp = "/tmp/" + filename + ".m3u8"
        os.system("sudo chmod a+r %s" % infile)
        if not self._videoconvert(self.videoinfo, infile, outfile):
            return False

        logger.debug("vlc trancode for %s completed" % infile)
        om = open(m3file, "r")
        nm = open(m3tmp,"w")
        oc = om.readlines()
        om.close()
        if len(oc) > 10:
            oc = oc[:2] + oc[4:]
        nc = oc[:-1] + ['#EXTINF:%s' % segmentlength,'%s%s_%s.ts' % (exportdir, filname, seq)] + oc[-1]
        nm.write(nc)
        nm.close()
        os.system("mv %s %s" %(m3tmp, m3file))

    def _videocovert(self, videoinfo, infile, outfile):
        try:
            os.system('vlc -I dummy --sout
            "#transcode{width=%s,height=%s,vcodec=h264,vb=%d,acodec=mp4a,ab=%d}:std{mux=ts,dst=%s,access=file}" %s vlc://quit'
            % (videoinfo['ID_VIDEO_WIDTH'],
               videoinfo['ID_VIDEO_HEIGHT'],
               int(videoinfo['ID_VIDEO_BITRATE'])/1024,
               int(videoinfo['ID_AUDIO_BITRATE'])/1024,
               outfile,
               infile))
            return True
        except:
            logger.error("vlc transcode failed on %s" % infile)
            return False

    def finishRecord(self, filename, oauth=None):
        if oauth:
            #TBD, join all mp4 or ts file segments and upload to oauth desination
        else:
            time.sleep(30)
        # Delete original upload mp4 files?
        os.system("rm -rf %s*.mp4" % (uploadpath+filename))
        os.system("rm -rf %s*" %(httpdir+filename))


server.register_instance(StreamServer())

# Run the server's main loop
server.serve_forever()
