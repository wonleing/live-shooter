# -*- coding: utf-8 -*-
from django.shortcuts import render
from django.conf import settings
from django.http import Http404, HttpResponse
from liveshooter.models import Followship, Userlike, User, Uservideo, Video
from ftplib import FTP
import xmlrpclib, os
s=xmlrpclib.ServerProxy("%s:8000" %settings.XMLRPC_URL, encoding='latin-1')

def index(request):
    recommand_users = s.getRecommandUser(True)
    recommand_videos = s.getRecommandVideo(True)
    context = {
        'recommand_users': recommand_users,
        'recommand_videos': recommand_videos
    }
    return render(request, 'index.html', context)

def user(request, userid):
    try:
        user_profile = s.getUserProfile(int(userid), True)
        user_videos = s.getUserVideo(int(userid), True)
    except:
        raise Http404
    context = {
        'up': user_profile,
        'user_videos': user_videos
    }
    return render(request, 'user.html', context)

def follower(request, userid):
    try:
        user_profile = s.getUserProfile(int(userid), True)
        follower_list = s.getFollower(int(userid), True)
    except:
        raise Http404
    context = {
        'up': user_profile,
        'follower_list': follower_list,
    }
    return render(request, 'follower.html', context)

def following(request, userid):
    try:
        user_profile = s.getUserProfile(int(userid), True)
        following_list = s.getFollowing(int(userid), True)
    except:
        raise Http404
    context = {
        'up': user_profile,
        'following_list': following_list,
    }
    return render(request, 'following.html', context)

def addnew(request, userid):
    context = {
        'uid': userid,
    }
    return render(request, 'addnew.html', context)

def doadd(request):
    userid = int(request.POST.get('userid'))
    videotitle = request.POST.get('videotitle').replace("'", "`").encode('utf-8')
    try:
        videopath = request.FILES['videopath']
    except:
        return HttpResponse('<html><head><META HTTP-EQUIV="refresh" CONTENT="3;URL=user/%s"></head>Please choose a video</html>' %userid)
    videoid = s.genFilename()
    videoname = videoid + "." + videopath._get_name().split(".")[1]
    os.system("mv %s /var/ftp/pub/%s;chmod 644 /var/ftp/pub/%s" %(videopath.temporary_file_path(), videoname, videoname))
    if not s.addTitle(userid, videoid, videotitle):
         return HttpResponse('<html><head><META HTTP-EQUIV="refresh" CONTENT="3;URL=user/%s"></head>Add video title failed\n%s</html>' \
         %(userid, str(userid)+","+videoid+","+videotitle))
    url = s.finishUpload(videoid)
    try:
        from weibo import APIClient
        import urllib,httplib,sys
        APP_KEY = '997501600' 
        APP_SECRET = 'f236cdb4c1fbc8d243ab580c115ac9e1'  
        CALLBACK_URL = 'http://liveshooter.cn.mu/weibo_auth/callback.php'
        burl = 'http://54.248.182.51/'
        msg = videotitle + " " + burl + videoid
        snapshot = burl + videoid + "/" + videoid + ".jpeg"
        ACCOUNT = request.POST.get('account')
        PASSWORD = request.POST.get('password')
        client = APIClient(app_key=APP_KEY, app_secret=APP_SECRET, redirect_uri=CALLBACK_URL)
        conn = httplib.HTTPSConnection('api.weibo.com')
        postdata = urllib.urlencode({'client_id':APP_KEY,'response_type':'code','redirect_uri':CALLBACK_URL,'action':'submit',
        'userId':ACCOUNT,'passwd':PASSWORD,'isLoginSina':0,'from':'','regCallback':'','state':'','ticket':'','withOfficalFlag':0})
        conn.request('POST','/oauth2/authorize',postdata,{'Referer':client.get_authorize_url(),
        'Content-Type':'application/x-www-form-urlencoded'})
        res = conn.getresponse()
        code = res.getheader('location').split('=')[1]
        conn.close()
        r = client.request_access_token(code)
        client.set_access_token(r.access_token, r.expires_in)
        client.statuses.upload.post(status=msg, pic=urllib.urlopen(snapshot))
        message = "</br>Video link posted on your weibo successfully"
    except:
        message = "</br>Video link failed to post on your weibo"
    return HttpResponse('''<html><head><META HTTP-EQUIV="refresh" CONTENT="3;URL=user/%s"></head>
    Your video can be watched here: %s,%s</html>''' %(userid, url, message))
