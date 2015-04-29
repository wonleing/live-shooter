Project description
This is an user video uploaded based social network project, running by us. The server run on Amazon EC2 cloudy linux machine, exported standard xmlrpc APIS. Android and IOS phones call these APIs to view and upload videos, share and like vidoes, follow/unflow users, get recommand users and videos etc. Web site and Admin interface also call these APIs to do similar works and even check/delete videos for management purpose.

You can see the video tutorial here: English_version Chinese_version​

API doc sample
​The API doc is open for developers who interested in our project, but in order not to make this pragraph too long, we only take one simple API as example, contact us for the full version doc.

getFeed(userid)
​Usage：           When visiting user feed page, this API can return the video info list of that user's feed
Parameters：   userid  -- ID of the user visting feed page, type: int
Return value：Json list of user liked videos and videos uploaded by that user's following user. Video info format is [return values of getUserVideo, return values of getUserProfile], We now return the top 100 of the whole feed list if it is longer than 100. e.g. [["STHik5N9", "My first video", "fowqjfsadkfjaldf", 1, "2013-02-02 14:16:33", 2, "test@sina.com", "DemonLeon", "http://tp4.sinaimg.cn/1435494115/180/5613100011/1", "sina", "free"]["d62ik5N4", "My 2nd video", "fo34jfsadkfjaldf", 1, "2013-02-02 14:16:33", 2, "test@sina.com", "DemonLeon", "http://tp4.sinaimg.cn/1435494115/180/5613100011/1", "sina", "free"]]
Unit test sample
​The unit test script can be download HERE. It tests against the server xmlrpc APIs, written by python and run-able in any python env. You can simply modify it with your info and test logic, or just write a new one with your favorite coding language.

The main target of sharing this script is show you how easy to use our server API, you can see the test result on standard output and see the changes on our website. Test sample

Web site and admin interface
​Before and after the test, you can compare to see the changes of our web site HERE. Your new created user, uploaded video, follower and followings. You can also surf around to see your relationship and their videos in this socail network.

In order to make sure all the uploaded video is leagal, we also provided admin interface, where administror can check or delete all the uploaded video. It is quite easy to understand and use, so we just provide you the demo link of the admin interface​ ​HERE. You can login with video1/video123 and make changes on your newly upload videos



We are looking for interns
We must make sure you have enough ability and patience to read English doc before having you be a part of us(This is essential for our develop works). Just like other long English tech doc, it detailed some very import info about this position, how to get pay and how to apply.

We are about to be a start up company base in Beijing, developing this top new concept android/ios mobile application as the very first project of us. Our co-founders are all from very famous IT companies, because we believe only the smart people can do the smart things. Our project is on-going and you can briefly know what it is from our project wiki page.

http://code.google.com/p/live-shooter/ (especially the wiki section)

For now it is still a private project, and we have more internal docs which are absolutely not public.

What you can get if you choose to become our intern:
- Free work style. You don't have to go to office every day, since we can do it online instead.

- Our professional direction and suggestion. We top engineer will have more focus on you than other big companies.

- Top practical technology and international comprehension of software development.

- Bonus of finishing work module. Pick up work modules from schedule and estimate time and price. We will pay you when you finish it according to the quality.

- Possibility of share option. When our product got profit, you may get your share from it.

- Random free meals/drinks/party etc.

What you need for getting the opportunity:
- Top smart. Your logic should be very clear, love to study and good at study.

- Knowledge on Android/Java/IOS development. You don't have to be very skilled, but know how to use the serials of dev tools at least.

- English reading, good at asking questions and able to search your answer with English.(This can provide you preciser answers)

- Be a good team worker, take mail and IM communication seriously.

- Passion, patience and plenty of time.

You may not able to get lots of money from this intern position, but what you may get is more valuable than it. We small but very international formed, we start from scratch but potential to be a winner. So please take a serious consideration and send me(wonleing@163.com or live-shooter@googlegroups.com) a mail for applying this internship if you have intrest on it.

Leon Wang

Live-shooter co-founder



Coding Policy
1. 严格控制代码质量，结构层次清楚。lib, resource, logic严格分开

2. 变量名称要让人望名知意，不能做到的地方请用注释标明变量含义（尤其是在循环中）

3. 不直接使用任何自动生成的代码，如必须使用，必须人工review

4. 不使用任何一行自己不懂的代码，不随意copy/paste

5. 能够用循环解决的相似逻辑尽量用循环解决

6. 尽量减少代码总长度，代码行数越多，bug的机会也越多，不必要存在的代码/resource要及时删除

7. (Welcome to add more...)

Meeting policy
决策必须平等广泛：公司任何的策略的制定要具备公平性、合理性、可执行性，防止一言堂现象的产生，以事实为依据，采用最优决策。

执行必须严格效率：目标确认以后，要依据流程及方法有效率的执行，并执行到底。

会议流程及方法—头脑风暴：

会前准备：

明确会议议题（列表形式）。
选出主持人支持会议。
会议过程：
根据主持人提出议题广泛发表意见及想法，由主持人记录。
以事实为依据讨论并明确解决方法。
分析问题方法—逻辑树:
在有可执行结果时，广泛发表意见记录，选取最优决策并执行。
在不明确结果时，应大胆假设，并执行。

