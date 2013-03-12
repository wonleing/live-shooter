# -*- coding: utf-8 -*-
# This is an auto-generated Django model module.
# You'll have to do the following manually to clean this up:
#     * Rearrange models' order
#     * Make sure each model has one field with primary_key=True
# Feel free to rename the models, but don't rename db_table values or field names.
#
# Also note: You'll have to insert the output of 'django-admin.py sqlcustom [appname]'
# into your database.

from django.db import models
import datetime
from django.conf import settings

class Followship(models.Model):
    seq = models.IntegerField(primary_key=True)
    userid = models.IntegerField("user ID")
    following = models.IntegerField("user been followed")
    class Meta:
        db_table = u'followship'
    def __unicode__(self):
        return str(self.userid) + "-" + str(self.following)

class Userlike(models.Model):
    seq = models.IntegerField(primary_key=True)
    userid = models.IntegerField("user ID")
    videoid = models.CharField("like this video", max_length=8)
    class Meta:
        db_table = u'userlike'
    def __unicode__(self):
        return str(self.userid) + "-" + str(self.videoid)

class User(models.Model):
    type_choice = (('admin','admin'), ('business','business'), ('confirmed','confirmed'), ('expired','expired'), ('free','free'))
    userid = models.IntegerField("user ID", primary_key=True)
    username = models.CharField("user sns login name", max_length=50, blank=True)
    nickname = models.CharField("user nickname on sns site", max_length=50, blank=True)
    icon = models.URLField("user sns icon link", max_length=200, blank=True)
    sns = models.CharField("sns site name", max_length=20, blank=True)
    type = models.CharField("user type", max_length=10, choices=type_choice, default='free', blank=False)
    class Meta:
        db_table = u'user'
    def __unicode__(self):
        return str(self.userid)

class Uservideo(models.Model):
    userid = models.IntegerField("user ID", blank=True)
    videoid = models.CharField("published this video", max_length=8, primary_key=True)
    class Meta:
        db_table = u'uservideo'
    def __unicode__(self):
        return str(self.userid) + "-" + str(self.videoid)

class Video(models.Model):
    def was_published_today(self):
        return self.createdate.date() == datetime.date.today()
    videoid = models.CharField("video ID", max_length=8, primary_key=True)
    title = models.CharField("video description published on sns", max_length=300, blank=True)
    snsid = models.CharField("publish ID on that sns site", max_length=50, blank=True)
    score = models.IntegerField("times of being liked", null=True, blank=True)
    createdate = models.DateTimeField("date of creation", null=True, blank=True)
    class Meta:
        db_table = u'video'
    def __unicode__(self):
        return str(self.videoid)

class Videocheck(models.Model):
    status_choice = (('new','new'), ('checked','checked'))
    videoid = models.CharField("video ID", max_length=8, primary_key=True)
    status = models.CharField("video status", max_length=10, choices=status_choice, default='new', blank=False)
    operator = models.CharField("last operator", max_length=20, blank=True)
    def publish_time(self):
        v = Video.objects.get(videoid=self.videoid)
        return v.createdate
    class Meta:
        db_table = u'videocheck'
    def __unicode__(self):
        return str(self.videoid)

