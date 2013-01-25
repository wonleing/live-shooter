#!/bin/bash
pkill streamserver.py
svn up
#cat initdb.sql | sqlite3 liveshooter.db
mysql -u live -pshooter@123 -h localhost liveshooter < initdb_mysql.sql
nohup ./streamserver.py -s 10.146.27.163 -p liveshooter.cn.mu > /dev/null 2>&1 &
