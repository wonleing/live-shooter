import socket,time,threading,os
address = ("192.168.188.2", 8001)
file = "/home/leon/download/smile.flv"
BUFSIZE = 8196
threads = 1

def sender(threadNumber):
    if os.path.exists(file):
        f = open(file, "r")
        stream = True
    else:
        sys.exit()
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(address)
    while stream:
        stream = f.read(BUFSIZE)
        if stream:
            s.send(stream)
            #print "Sent %d bytes by Thread-%s" % (len(stream), threadNumber)
            time.sleep(0.1) 
        else:
            break
    f.close()
        
for n in range(threads):
    thread = threading.Thread(target=sender, args=(n,))
    thread.start()
