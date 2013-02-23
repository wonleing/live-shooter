#!/bin/bash
pkill -P `ps aux | grep manage.py | head -1 | awk {'print $2'}`
svn up
cp settings.py.save settings.py
nohup ./manage.py runserver 0.0.0.0:8080 > /dev/null 2>&1 &
