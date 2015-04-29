# Project description #

This is an user video uploaded based social network project, running by us. The server run on Amazon EC2 cloudy linux machine, exported standard xmlrpc APIS. Android and IOS phones call these APIs to view and upload videos, share and like vidoes, follow/unflow users, get recommand users and videos etc. Web site and Admin interface also call these APIs to do similar works and even check/delete videos for management purpose.

You can see the video tutorial here:
[English\_version](http://v.youku.com/v_show/id_XNTE3NDI3Nzgw.html)
[Chinese\_version​](http://liveshooter.cn.mu/6Ma9fjFQ/)

## API doc sample ##

​The API doc is open for developers who interested in our project, but in order not to make this pragraph too long, we only take one simple API as example, contact us for the full version doc.
```
getFeed(userid)
​Usage：           When visiting user feed page, this API can return the video info list of that user's feed
Parameters：   userid  -- ID of the user visting feed page, type: int
Return value：Json list of user liked videos and videos uploaded by that user's following user. Video info format is [return values of getUserVideo, return values of getUserProfile], We now return the top 100 of the whole feed list if it is longer than 100. e.g. [["STHik5N9", "My first video", "fowqjfsadkfjaldf", 1, "2013-02-02 14:16:33", 2, "test@sina.com", "DemonLeon", "http://tp4.sinaimg.cn/1435494115/180/5613100011/1", "sina", "free"]["d62ik5N4", "My 2nd video", "fo34jfsadkfjaldf", 1, "2013-02-02 14:16:33", 2, "test@sina.com", "DemonLeon", "http://tp4.sinaimg.cn/1435494115/180/5613100011/1", "sina", "free"]]
```

## Unit test sample ##

​The unit test script can be download [HERE](http://liveshooter.cn.mu/test.py). It tests against the server xmlrpc APIs, written by python and run-able in any python env. You can simply modify it with your info and test logic, or just write a new one with your favorite coding language.

The main target of sharing this script is show you how easy to use our server API, you can see the test result on standard output and see the changes on our website.
Test sample

## Web site and admin interface ##

​Before and after the test, you can compare to see the changes of our web site [HERE](http://liveshooter.cn.mu). Your new created user, uploaded video, follower and followings. You can also surf around to see your relationship and their videos in this socail network.

In order to make sure all the uploaded video is leagal, we also provided admin interface, where administror can check or delete all the uploaded video. It is quite easy to understand and use, so we just provide you the demo link of the admin interface​ ​[HERE](http://liveshooter.cn.mu/liveshooter/admin). You can login with video1/video123 and make changes on your newly upload videos