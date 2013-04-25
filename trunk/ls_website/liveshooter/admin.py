# -*- coding: utf-8 -*-
from liveshooter.models import Followship, Userlike, User, Uservideo, Video, Videocheck
from django.contrib import admin
from django.conf import settings
import os

class FollowshipAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    readonly_fields = ['seq', 'userid', 'following']

class UserlikeAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    readonly_fields = ['seq', 'userid', 'videoid']

class UserAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    readonly_fields = ('userid', 'username', 'sns')
    fields = ['userid', 'username', 'sns', 'nickname', 'icon', 'type']
    list_filter = ('type', 'sns')

class UservideoAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    readonly_fields = ['userid', 'videoid']

class VideoAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    readonly_fields = ('videoid',)
    fields = ['videoid', 'title', 'snsid', 'score', 'createdate']
    list_display = ('videoid', 'createdate', 'was_published_today')

class VideocheckAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False
    def save_model(self, request, obj, form, change):
        obj.operator = request.user.username
        obj.save()
    def delete_model(self, request, obj):
        try:
            Video.objects.get(videoid=obj.videoid).delete()
            Uservideo.objects.get(videoid=obj.videoid).delete()
            Videocheck.objects.get(videoid=obj.videoid).delete()
            Userlike.objects.filter(videoid=obj.videoid).delete()
        except:
            pass
        os.system("sudo rm -rf %s%s" %(settings.MEDIA_ROOT, obj.videoid))
    def play_link(self, obj):
        short_description = 'Play it now'
        return '<a href="%s%s" target="_blank">play now</a>' % (settings.MEDIA_URL, obj.videoid)
    play_link.allow_tags = True
    def make_checked(self, request, queryset):
        rows_updated = queryset.update(status='checked', operator=request.user.username)
        self.message_user(request, "%s video(s) successfully marked as checked." % rows_updated)
    make_checked.short_description = "Mark selected videos as checked"
    def delete_selected(self, request, queryset):
        for element in queryset:
            self.delete_model(request, element)
    delete_selected.short_description = "Delete selected elements"
    readonly_fields = ('videoid', 'operator')
    fields = ['videoid', 'status', 'operator']
    list_display = ('videoid', 'status', 'publish_time', 'play_link')
    list_filter = ('status',)
    actions = [make_checked, delete_selected]
    #admin.site.disable_action('delete_selected')

for i in ("Followship", "Userlike", "User", "Uservideo", "Video", "Videocheck"):
    admin.site.register(eval(i), eval(i+"Admin"))

