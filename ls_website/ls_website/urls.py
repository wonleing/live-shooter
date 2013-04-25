from django.conf.urls import patterns, include, url
from django.contrib import admin
from liveshooter import views
admin.autodiscover()

urlpatterns = patterns('',
    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^$', views.index, name='index'),
    url(r'^user/(?P<userid>\d+)/$', views.user, name='user'),
    url(r'^video/(?P<videoid>.+)/$', views.video, name='video'),
    url(r'^follower/(?P<userid>\d+)/$', views.follower, name='follower'),
    url(r'^following/(?P<userid>\d+)/$', views.following, name='following'),
    url(r'^addnew/(?P<userid>\d+)/$', views.addnew, name='addnew'),
    url(r'^doadd$', views.doadd, name='doadd'),
    url(r'^login$', views.login, name='login'),
    url(r'^dologin$', views.dologin, name='dologin'),
    url(r'^logout$', views.logout, name='logout'),
    url(r'^follow$', views.follow, name='follow'),
    url(r'^unfollow$', views.unfollow, name='unfollow'),
    url(r'^likevideo$', views.likevideo, name='likevideo'),
    url(r'^unlikevideo$', views.unlikevideo, name='unlikevideo'),
    url(r'^addcomment$', views.addcomment, name='addcomment'),
)
