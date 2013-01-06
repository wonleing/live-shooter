#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import os, sys, random, optparse, logging, time, socket, thread, signal, vlc

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

PDIC = {}
segmentlength = 2
segnum = 3
ext = ".h264"
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
    def _check(self, cmd):
        if not os.path.exists(cmd):
            logger.error("Critical: %s is not installed" % cmd)
            sys.exit()
        
    def _genFilename(self):
        rand = "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))
        if os.path.exists(uploadpath + rand):
            logger.info("random filename already existed, genFilename again!")
            self._genFilename()
        logger.debug("genFilename returns ==%s== as the file name" % rand)
        return rand

    def _genPort(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(('localhost', 0))
        addr, port = s.getsockname()
        s.close()
        logger.debug("genPort returns ==%s== as the udp streaming port" % port)
        return port

    def _streaming(self, filename):
        infile = uploadpath + filename + ext
        outname = httpdir + filename
        exportname = exportdir + filename
        # Wait client upload stream via ftp with counter i
        i = 0
        logger.debug("Start lag timer...")
        while i<30 and not os.path.exists(infile):
            time.sleep(0.2)
            i += 1
        if i == 30:
            logger.error("%s upload from client timout. Please check the network" % infile)
            return False
        logger.debug("Got uploaded content in the ftp dir after %d rounds" % i)
        time.sleep(10) # For udp stream having enough cache to map
        lag = 0.2 * i + 10
        logger.debug("Start to stream %s after %.1f sec" % (infile, lag))
        ins = vlc.Instance()
        m = ins.media_new("file://%s" % infile, "sout=#transcode{width=%d,height=%d,vcodec=h264,vb=%d,venc=x264{aud,profile=baseline,\
        level=30,keyint=30,ref=1},acodec=aac,ab=%d,deinterlace}:std{access=livehttp{seglen=%d,delsegs=true,numsegs=%d,index=%s.m3u8,\
        index-url=%s-########.ts},mux=ts{use-key-frames},dst=%s-########.ts}"
        % (vinfo['width'], vinfo['hight'], vinfo['vbit'], vinfo['abit'], segmentlength, segnum, outname, exportname, outname))
        #m = ins.media_new("file://%s" % infile, "sout=udp:localhost:1234")
        p = ins.media_player_new()
        p.set_media(m)
        logger.debug("Init for %s streaming completed" % filename)
        global PDIC
        PDIC[filename] = p
        PDIC[filename+"_lag"] = lag
        logger.debug("current PDIC is: %s" %str(PDIC))
        p.play()

    def _createHtml(self, filename, videotitle, videodesc):
        t = open("template.html", "r")
        n = open(httpdir+filename+".html", "w")
        n.write(t.read().replace("FileName", filename).replace("VideoTitle", videotitle).replace("VideoDesc", videodesc))
        t.close()
        n.close()
        logger.debug("html page for %s created with Title %s" %(filename, videotitle))

    def _cleanup(self, filename, oauth):
        if oauth:
            #TBD, upload original video to oauth destination
            logger.debug("video uploaded to oauth web site")
        time.sleep(60)
        # Delete original uploaded video files?
        os.system("rm -rf %s" % (uploadpath+filename+ext))
        os.system("rm -rf %s*" %(httpdir+filename))
        logger.info("live video segments of %s completely deleted" %filename)

    def startRecord(self, videotitle="", videodesc=""):
        logger.info("got connection from phone. Title:%s, Description:%s" %(videotitle, videodesc))
        self._check("/usr/bin/vlc")
        filename = self._genFilename()
        #port = self._genPort()
        thread.start_new_thread(self._streaming, (filename,))
        #self._createHtml(filename, videotitle, videodesc)
        #self._streaming("norkpmix")
        #thread.start_new_thread(self._streaming, ("norkpmix",))
        return filename

    def finishRecord(self, filename, oauth=None):
        global PDIC
        time.sleep(PDIC[filename+"_lag"])
        PDIC[filename].stop()
        logger.debug("live streaming for %s finished" % filename)
        thread.start_new_thread(self._cleanup, (filename, oauth))
        PDIC.pop(filename)
        PDIC.pop(filename+"_lag")
        logger.debug("current PDIC is: %s" %str(PDIC))
        return True

    def _writeIndex(self, filename, seq):
        # Unused, since VLC can take care of it right now.
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


# Run the server's main loop
server.register_instance(StreamServer())
server.serve_forever()
