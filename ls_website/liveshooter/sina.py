#!/usr/bin/python
from weibo import APIClient
import urllib,httplib,sys
 
APP_KEY = '997501600' 
APP_SECRET = 'f236cdb4c1fbc8d243ab580c115ac9e1'  
CALLBACK_URL = 'http://liveshooter.cn.mu/weibo_auth/callback.php'
 
class Weibo:
    def __init__(self):
        self.client = APIClient(app_key=APP_KEY, app_secret=APP_SECRET, redirect_uri=CALLBACK_URL)
        self.conn = httplib.HTTPSConnection('api.weibo.com')

    def auth(self, username, password):
        postdata = urllib.urlencode({'client_id':APP_KEY,'response_type':'code','redirect_uri':CALLBACK_URL,'action':'submit',\
        'userId':username,'passwd':password,'isLoginSina':0,'withOfficalFlag':0})
        self.conn.request('POST','/oauth2/authorize',postdata,{'Referer':self.client.get_authorize_url(),\
        'Content-Type':'application/x-www-form-urlencoded'})
        location = self.conn.getresponse().getheader('location')
        self.conn.close()
        if location:
            r = self.client.request_access_token(location.split('=')[1])
            return (r.access_token, r.expires_in)
        else:
            return None

    def setToken(self, access_token, expires_in):
        self.client.set_access_token(access_token, expires_in)

    def post(self, text, image):
        return self.client.statuses.upload.post(status=text, pic=urllib.urlopen(image))

    def profile(self):
        u = self.client.get.statuses__user_timeline().statuses[0].user
        return(u.screen_name, u.avatar_large)

    def getComment(self, mid):
        return self.client.comments.show.get(id=int(mid))

    def addComment(self, newcomment, mid):
        return self.client.comments.create.post(comment=newcomment, id=int(mid))
