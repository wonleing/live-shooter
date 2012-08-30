import asyncore, socket, random
address = ("192.168.188.2", 8001)
BUFSIZE = 8196

class Host(asyncore.dispatcher):
    def __init__(self, address):
        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.bind(address)
        self.set_reuse_addr()
        self.listen(5)

    def handle_accept(self):
        pair = self.accept()
        if pair is not None:
            sock, addr = pair
            newname = "".join(random.sample('zyxwvutsrqponmlkjihgfedcba',8))
            print 'data from %s is written into %s' % (repr(addr), newname)
            WriteHandler(sock, newname)

class WriteHandler(asyncore.dispatcher_with_send):
    def __init__(self, sock, file):
        asyncore.dispatcher_with_send.__init__(self, sock)
        self.f = open(file, "a")

    def handle_read(self):
        data = self.recv(BUFSIZE)
        if data:
            self.f.write(data)
            print "received: " + str(len(data))

    def handle_close(self):
        self.f.close()

server = Host(address)
asyncore.loop()
