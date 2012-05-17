#!/usr/bin/python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler
import os, sys, random, optparse

u = """%prog -s <server_ip>"""
parser = optparse.OptionParser(u)
parser.add_option('-s', '--server', help='stream server ip addres', dest='serverip')
(options, leftargs) = parser.parse_args()
if options.serverip == None:
    parser.print_help()
    sys.exit()

print "Starting service on ", options.serverip
server = SimpleXMLRPCServer((options.serverip, 8000))
server.register_introspection_functions()

class StreamServer:
    def __init__(self):
        self.uploadpath = "/var/www/uploads/"
        
    def genFilename(self):
        rand = self._rand()
        if os.path.exists(self.uploadpath + rand):
            self.genFilename()
        return rand

    def genSegment(self, filename, segmentname):
        #TBD, move createbroadcast.sh logic here
        try:
            os.system("./createbroadcast.sh %s %s" % (filename, self.uploadpath+segmentname))
        except:
            return False
        return True

    def _rand(self):
        return "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))

server.register_instance(StreamServer())

# Run the server's main loop
server.serve_forever()
