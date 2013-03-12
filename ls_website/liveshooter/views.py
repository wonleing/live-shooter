from django.shortcuts import render
from django.conf import settings
from django.http import Http404, HttpResponse
from liveshooter.models import Followship, Userlike, User, Uservideo, Video
from ftplib import FTP
import xmlrpclib, os
s=xmlrpclib.ServerProxy("%s:8000" %settings.XMLRPC_URL)

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
    videotitle = request.POST.get('videotitle').replace("'", "`")
    videopath = request.FILES['videopath']
    videoid = s.genFilename()
    videoname = videoid + "." + videopath._get_name().split(".")[1]
    snsid = "FAKE_SNSID_OF_VIDEO_"+videoid
    s.addTitle(userid, videoid, videotitle)
    os.system("mv %s /var/ftp/pub/%s;chmod 666 /var/ftp/pub/%s" %(videopath.temporary_file_path(), videoname, videoname))
    url = s.finishUpload(videoid)
    return HttpResponse('<html><head><META HTTP-EQUIV="refresh" CONTENT="3;URL=../user/%s"></head>Your video can be watched here: %s</html>' 
    %(userid, url))
