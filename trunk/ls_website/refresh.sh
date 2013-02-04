#!/bin/bash
pkill -P `ps aux | grep manage.py | head -1 | awk {'print $2'}`
mv settings.py settings.py.bak
svn up
mv settings.py.bak settings.py
nohup ./manage.py runserver 0.0.0.0:8080 > /dev/null 2>&1 &
