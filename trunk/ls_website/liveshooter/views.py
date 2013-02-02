from django.shortcuts import render
from django.conf import settings
from django.http import Http404
from liveshooter.models import Followship, Userlike, User, Uservideo, Video
import xmlrpclib
s=xmlrpclib.ServerProxy("%s:8000" %settings.MEDIA_URL)

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

