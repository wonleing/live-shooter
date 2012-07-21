#!/usr/bin/python
import xmlrpclib, os
# run following command in other terminal, CTRL+C when you want to finish test
# i=0;while ( true );do echo $i >> /var/ftp/pub/vwnexpch.mp4;((i+=1));done

s=xmlrpclib.ServerProxy("http://127.0.0.1:8000")
if s.genSegment("vwnexpch", "test video with number filler"):
    s.finishRecord("vwnexpch")
