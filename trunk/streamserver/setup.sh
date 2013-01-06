#!/bin/bash
#Ubuntu server setup
sudo dpkg --force-depends --purge libsqlite3-0:amd64 libsqlite3-0:i386 
apt-get download libsqlite3-0:amd64 libsqlite3-0:i386
sudo dpkg --install libsqlite3-0*amd64.deb libsqlite3-0*i386.deb
sudo apt-get install vsftpd subversion vlc sqlite3

#Fedora server setup, install RPMFusion.org/Configuration Fusion.rpm first
#sudo yum install vsftpd subversion vlc sqlite3

sudo touch /var/log/liveshooter.log
sudo mkdir -p /var/ftp/pub /var/www/live-shooter
sudo chmod 777 /var/log/liveshooter.log /var/ftp/pub /var/www/live-shooter
sudo useradd live -d /var/ftp/pub
#Manual work: sudo passwd live (input shooter)

sudo echo """
listen=YES
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
""" > /etc/vsftpd.conf

sudo echo """
/bin/false
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
/usr/sbin/nologin
""" > /etc/shells

cat initdb.sql | sqlite3 liveshooter.db

# Then you can run ./streamserver -s <serverip>
