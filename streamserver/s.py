import os, random, socket
address = ('192.168.188.2', 8001)
BUFSIZ = 8196
mux = ".mp4"
tcpSerSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
tcpSerSock.bind(address)
tcpSerSock.listen(999)

print 'Server started, waiting for connection...'
while True:
    tcpCliSock, addr = tcpSerSock.accept()
    print 'Got connection from:', addr
    newname = "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))
    print 'Generated new file:', newname
    f = open(newname+mux, "a")
    while True: 
        try:  
            tcpCliSock.settimeout(5)
            data = tcpCliSock.recv(BUFSIZ)
            if not data:
                break
            f.write(data)
        except socket.timeout:
            print 'connection from %s time out' % addr
    f.close()
    tcpCliSock.close()
tcpSerSock.close()
