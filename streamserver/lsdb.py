import sqlite3

class DB:
    def __init__(self):
        self.cx = sqlite3.connect("./liveshooter.db")
        self.cu = self.cx.cursor()

    def __del__(self):
        try:
            self.cx.close()
        except:
            pass

    def createUser(self, uname, usns):
        # All new created users are free, change user type with modifyUser
        self.cu.execute("insert into users (username, sns, type) values ('%s', '%s', 'free')" %(uname, usns))
        self.cx.commit()
        return self.selectUserID(uname, usns)

    def selectUserID(self, uname, usns):
        self.cu.execute("select userid from users where username='%s' and sns='%s'" %(uname, usns))
        ret = self.cu.fetchall()
        if ret:
            return ret[0][0]
        return False

    def addVideo(self, userid, videoid, videotitle):
        self.cu.execute("insert into uservideo (userid, videoid) values (%d, '%s')" %(userid, videoid))
        self.cu.execute("insert into video (videoid, title, score) values ('%s', '%s', 0)" %(videoid, videotitle))
        return True

    def shareVideo(self, videoid, snsid):
        self.cu.execute("update video set snsid='%s' where videoid='%s'" %(snsid, videoid))
        self.cx.commit()
        return True

    def likeVideo(self, userid, videoid):
        try:
            self.cu.execute("insert into userlike (userid, videoid) values (%d, '%s')" %(userid, videoid))
            self.cx.commit()
        except:
            return False
        self.cu.execute("update video set score=score+1 where videoid='%s'" %videoid)
        self.cx.commit()
        return True

    def getUserVideo(self, userid):
        self.cu.execute("select v.* from video as v,uservideo as uv where uv.videoid=v.videoid and uv.userid=%d" %userid)
        return self.cu.fetchall()

    def getLikeVideo(self, userid):
        self.cu.execute("select v.* from video as v,userlike as ul where ul.videoid=v.videoid and ul.userid=%d" %userid)
        return self.cu.fetchall()

    def getFollowing(self, userid):
        self.cu.execute("select following from followship where userid=%d" %userid)
        return self.cu.fetchall()

    def getFollower(self, userid):
        self.cu.execute("select userid from followship where following=%d" %userid)
        return self.cu.fetchall()

