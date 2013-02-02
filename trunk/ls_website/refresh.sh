#!/bin/bash
pkill manage.py
mv settings.py settings.py.bak
svn up
mv settings.py.bak settings.py
nohup ./manage.py runserver 0.0.0.0:8080 > /dev/null 2>&1 &
