#!/bin/bash
#Ubuntu server setup
#sudo dpkg --force-depends --purge libsqlite3-0:amd64 libsqlite3-0:i386 
#apt-get download libsqlite3-0:amd64 libsqlite3-0:i386
#sudo dpkg --install libsqlite3-0*amd64.deb libsqlite3-0*i386.deb
sudo apt-get update
sudo apt-get install vsftpd subversion vlc sqlite3 openssh-server apache2 mplayer python-mysqldb libav-tools libavcodec-extra-53

sudo touch /var/log/liveshooter.log
sudo mkdir -p /var/ftp/pub /var/www
sudo chmod 777 /var/log/liveshooter.log /var/ftp/pub /var/www /etc/vsftpd.conf /etc/shells
#sudo useradd live -d /var/ftp/pub
#Manual work: sudo passwd live (input shooter)

sudo echo """listen=YES
anonymous_enable=NO
local_enable=YES
write_enable=YES
anon_upload_enable=YES
dirmessage_enable=YES
use_localtime=YES
xferlog_enable=YES
connect_from_port_20=YES
secure_chroot_dir=/var/run/vsftpd/empty
pam_service_name=ftp
rsa_cert_file=/etc/ssl/private/vsftpd.pem
local_root=/var/ftp/pub
pasv_enable=YES
pasv_min_port=14000
pasv_max_port=14050
port_enable=YES
pasv_address=liveshooter.cn.mu
pasv_addr_resolve=NO
local_umask=000""" > /etc/vsftpd.conf

sudo echo """/bin/false
/bin/csh
/bin/sh
/usr/bin/es
/usr/bin/ksh
/bin/ksh
/usr/bin/rc
/usr/bin/tcsh
/bin/tcsh
/usr/bin/esh
/bin/dash
/bin/bash
/bin/rbash
/usr/bin/screen
/usr/sbin/nologin""" > /etc/shells

#cat initdb.sql | sqlite3 liveshooter.db
#execute follow 3 sql lines with mysql root user
#mysql> CREATE DATABASE liveshooter;
#mysql> CREATE USER 'live'@'localhost' IDENTIFIED BY 'shooter@123';
#mysql> GRANT ALL ON liveshooter.* TO 'live'@'localhost';
mysql -u live -pshooter@123 -h localhost liveshooter < initdb_mysql.sql
sudo /etc/init.d/vsftpd restart
sudo /etc/init.d/ssh restart
sudo /etc/init.d/apache2 restart

echo 'Start the service with pid:'
nohup ./streamserver.py -s 10.146.27.163 -p liveshooter.cn.mu > /dev/null 2>&1 &
