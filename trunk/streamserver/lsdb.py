DB_type = 'mysql'

class DB:
    def __init__(self):
        try:
            DB_type == 'mysql'
            import MySQLdb
            self.cx = MySQLdb.connect('localhost', 'live', 'shooter@123', 'liveshooter')
        except:
            import sqlite3
            self.cx = sqlite3.connect("./liveshooter.db")
        self.cu = self.cx.cursor()

    def __del__(self):
        try:
            self.cx.close()
        except:
            pass

    def selectUserID(self, uname, usns):
        self.cu.execute("select userid from user where username='%s' and sns='%s'" %(uname, usns))
        ret = self.cu.fetchall()
        if ret:
            return ret[0][0]
        return False

    def createUser(self, uname, usns, nickname, icon):
        # All new created users are free, change user type with modifyUser
        try:
            self.cu.execute("insert into user (username, sns, type, nickname, icon) values ('%s', '%s', 'free', '%s', '%s')" \
            %(uname, usns, nickname, icon))
            self.cx.commit()
        except:
            return False
        return self.selectUserID(uname, usns)

    def updateUser(self, userid, nickname, icon):
        self.cu.execute("update user set nickname='%s', icon='%s' where userid=%d" %(nickname, icon, userid))
        self.cx.commit()
        return True

    def addVideo(self, userid, videoid, videotitle):
        try:
            self.cu.execute("insert into uservideo (userid, videoid) values (%d, '%s')" %(userid, videoid))
            self.cu.execute("insert into video (videoid, title, snsid, score) values ('%s', '%s', '', 0)" %(videoid, videotitle))
            self.cu.execute("insert into videocheck (videoid, status) values ('%s', 'new')" %videoid)
        except:
            return False
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

    def unlikeVideo(self, userid, videoid):
        try:
            self.cu.execute("delete from userlike where userid=%d and videoid='%s'" %(userid, videoid))
            self.cx.commit()
        except:
            return False
        self.cu.execute("update video set score=score-1 where videoid='%s'" %videoid)
        self.cx.commit()
        return True

    def followUser(self, userid, targetid):
        try:
            self.cu.execute("insert into followship (userid, following) values (%d, %d)" %(userid, targetid))
            self.cx.commit()
        except:
            return False
        return True

    def unfollowUser(self, userid, targetid):
        self.cu.execute("delete from followship where userid=%d and following=%d" %(userid, targetid))
        self.cx.commit()
        return True

    def getVideoUser(self, videoid):
        self.cu.execute("select userid from uservideo where videoid='%s'" %videoid)
        return self.cu.fetchall()[0][0]

    def getUserVideo(self, userid):
        self.cu.execute("select v.* from video as v,uservideo as uv where uv.videoid=v.videoid and uv.userid=%d \
        order by createdate desc limit 100" %userid)
        return self.cu.fetchall()

    def getUserProfile(self, userid):
        self.cu.execute("select * from user where userid=%d" %userid) 
        return self.cu.fetchall()[0]

    def getLikeVideo(self, userid):
        self.cu.execute("select v.* from video as v,userlike as ul where ul.videoid=v.videoid and ul.userid=%d \
        order by createdate desc limit 100" %userid)
        return self.cu.fetchall()

    def getFollowing(self, userid):
        self.cu.execute("select u.* from followship as f, user as u where f.userid=%d and f.following=u.userid" %userid)
        return self.cu.fetchall()

    def getFollower(self, userid):
        self.cu.execute("select u.* from followship as f, user as u where f.following=%d and f.userid=u.userid" %userid)
        return self.cu.fetchall()

    def getFeed(self, userid):
        #mysql has a bug, cannot order by createdate here
        self.cu.execute("select v.*, u.* from video as v, userlike as ul, user as u, uservideo as uv where ul.userid=%d and \
        ul.videoid=v.videoid and uv.videoid=v.videoid and u.userid=uv.userid union \
        select v.*, u.* from video as v, uservideo as uv, user as u where uv.videoid=v.videoid and u.userid=uv.userid and uv.userid in \
        (select following from followship where userid=%d) limit 100" %(userid, userid))
        return self.cu.fetchall()

    def getTopUser(self, type, num):
        self.cu.execute("select u.*, count(*) from user as u, followship as f where f.following in (select userid from user where type='%s') \
        and u.userid=f.following group by f.following order by count(*) desc limit %d" %(type, num))
        return self.cu.fetchall()

    def getTopVideo(self, date):
        self.cu.execute("select v.*, u.* from video as v, user as u, uservideo as uv where v.videoid=uv.videoid and \
        u.userid=uv.userid and v.createdate>'%s' order by v.score desc, v.createdate desc limit 100" %date)
        return self.cu.fetchall()

