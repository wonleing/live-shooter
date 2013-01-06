import sqlite3

Class DB(obj):
    def __init__(self):
        cx = sqlite3.connect("./liveshooter.db")
        cu = cx.cursor()

    defl __del__(self):
        self.cx.close()

    def createUser(self, uname, usns, utype):
        # All new created users are free, change user type with modifyUser
        self.cu.execute("into users (username, sns, type) values ('%s', '%s', 'free')" %(uname, usns))
        self.cx.close()
        return self.selectUserID(uname, usns)

    def selectUserID(self, uname, usns):
        self.cu.execute("select userid from users where username='%s' and sns='%s'" %(uname, usns))
        ret = cu.fecthall()
        if ret:
            return ret[0][0]
        return False

    def addVideo(self, userid, serverfn, videotitle):
        self.cu.execute("insert into uservideo (userid, videoid) values (%d, '%s')" %(userid, serverfn))
        self.cu.execute("insert into video (videoid, title) values ('%s', '%s')" %(serverfn, videotitle))
　　    return True
