#!/usr/bin/python
import xmlrpclib, os, time

s=xmlrpclib.ServerProxy("http://127.0.0.1:8000")
filename = s.startRecord("mytitle","mydesc")
os.system("cp /home/leon/download/smile.flv /var/ftp/pub/%s.mp4" %filename)
print "upload completed"
time.sleep(60)
s.finishRecord(filename)
print "Finished"
