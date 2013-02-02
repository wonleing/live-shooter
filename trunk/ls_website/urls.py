from django.conf.urls.defaults import patterns, include, url
from django.contrib import admin
from liveshooter import views
admin.autodiscover()

urlpatterns = patterns('',
    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^$', views.index, name='index'),
    url(r'^user/(?P<userid>\d+)/$', views.user, name='user'),
    url(r'^follower/(?P<userid>\d+)/$', views.follower, name='follower'),
    url(r'^following/(?P<userid>\d+)/$', views.following, name='following'),
)
