#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import os, sys, random, optparse, logging, time, thread

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
videoinfo = {'width':352, 'hight':288, 'vbit':300, 'abit':64}
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
    def _check(self, cmd):
        if not os.path.exists(cmd):
            logger.error("Critical: %s is not installed" % cmd)
            sys.exit()
        
    def _rand(self):
        return "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))

    def genFilename(self):
        self._check("/usr/bin/vlc")
        rand = self._rand()
        if os.path.exists(uploadpath + rand):
            logger.info("random filename already existed, genFilename again!")
            self.genFilename()
        logger.debug("genFilename returns ==%s== as the file name" % rand)
        return rand

    def genSegment(self, filename, videotitle):
        infile = uploadpath + filename + ".mp4"
        logger.debug("Starting to handle incoming video %s" %infile)
        os.system("sudo chmod 666 %s" % infile)
        self._createHtml(filename, videotitle)
        seq = 1
        while os.path.getsize(infile):
            f = open(infile,"rb")
            content = f.read()
            f.close()
            f = open(infile,"w")
            f.write("")
            f.close()
            tmpseg = uploadpath + filename + "_" + str(seq) + ".mp4"
            nf = open(tmpseg,"wb")
            nf.write(content)
            nf.close()
            logger.debug("%s is splited out from the video stream" % tmpseg)
            outfile = httpdir + filename + "_" + str(seq) + ".ts" 
            thread.start_new_thread(self._videoconvert, (tmpseg, outfile))
            self._writeIndex(filename, str(seq))
            seq += 1
            time.sleep(5)
        return True

    def _writeIndex(self, filename, seq):
        m3file = httpdir + filename + ".m3u8"
        m3tmp = "/tmp/" + filename + ".m3u8"
        if not os.path.exists(m3file):
            logger.debug("creating m3u8 index %s" % m3file)
            m = open(m3file, "w")
            m.write("#EXTM3U\n#EXT-X-TARGETDURATION:%s\n#EXT-X-ENDLIST" % segmentlength)
            m.close()
        logger.debug("update m3u8 index for %s_%s" % (filename, seq))
        m = open(m3file, "r")
        oc = m.readlines()
        m.close()
        if len(oc) > 10:
            logger.debug("deleteing old ts entries")
            oc = oc[:2] + oc[4:]
        nc = oc[:-1] + ['#EXTINF:%s,\n' % segmentlength,'%s%s_%s.ts\n' % (exportdir, filename, seq)] + [oc[-1]]
        logger.debug("new M3U8 content is %s", "".join(nc))
        m = open(m3file, "w")
        m.write("".join(nc))
        m.close()

    def _createHtml(self, filename, videotitle):
        t = open("template.html", "r")
        n = open(httpdir+filename+".html", "w")
        n.write(t.read().replace("FileName", filename).replace("VideoTitle", videotitle))
        t.close()
        n.close()
        logger.debug("html page for %s created. Title is %s" %(filename, videotitle))

    def _videoconvert(self, infile, outfile):
        try:
            logger.debug("Start video convert from %s to %s" %(infile, outfile))
            os.system('vlc -I dummy --sout \
            "#transcode{width=%d,height=%d,vcodec=h264,vb=%d,acodec=mp4a,ab=%d}:std{mux=ts,dst=%s,access=file}" %s vlc://quit'
            % (videoinfo['width'], videoinfo['hight'], videoinfo['vbit'], videoinfo['abit'], outfile, infile))
            return True
        except:
            logger.error("vlc transcode failed on %s" % infile)
            return False

    def finishRecord(self, filename, oauth=None):
        if oauth:
            #TBD, join all mp4 or ts file segments and upload to oauth destination
            logger.debug("video uploaded to oauth web site")
        else:
            time.sleep(30)
        # Delete original upload mp4 files?
        os.system("rm -rf %s*.mp4" % (uploadpath+filename))
        os.system("rm -rf %s*" %(httpdir+filename))
        logger.info("live video segments of %s completely deleted" %filename)
        return True


# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
