import xmlrpclib
s=xmlrpclib.ServerProxy("http://127.0.0.1:8000")
s.genSegment("xpwluetf", "Rider test video")
s.updateSegment("xpwluetf", "2")
s.updateSegment("xpwluetf", "3")
s.updateSegment("xpwluetf", "4")
s.updateSegment("xpwluetf", "5")
s.finishRecord("testdel")
