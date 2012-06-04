#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import os, sys, random, optparse, logging

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

logfile = "/var/log/liveshooter.log"
logger = logging.getLogger('Live shooter')
hdlr = logging.FileHandler(logfile)
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.INFO)
logger.setLevel(logging.DEBUG)

class StreamServer:
    def __init__(self):
        self.uploadpath = "/var/ftp/pub/"
        
    def genFilename(self):
        rand = self._rand()
        if os.path.exists(self.uploadpath + rand):
            self.genFilename()
        return rand

    def genSegment(self, filename):
        #TBD, move createbroadcast.sh logic here
        logger.debug("./createbroadcast.sh %s %s>> %s 2>&1" % (filename, options.serverip, logfile))
        ret = os.system("./createbroadcast.sh %s %s>> %s 2>&1" % (filename, options.serverip, logfile))
        if ret == 0:
            return True
        else:
            return False

    def _rand(self):
        return "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))

server.register_instance(StreamServer())

# Run the server's main loop
server.serve_forever()
