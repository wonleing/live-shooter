#!/bin/bash
[ ! -e segmenter ] && echo "Please run setup first and make sure segmenter is built" && exit 1
[ $# != 2 ] && echo "Usage: $0 <video_name> <ip>" && exit 1
ftpdir="/var/ftp/pub/"
httpdir="/var/www/live-shooter/"
exportdir="http://$2/live-shooter/"
file=$ftpdir$1.mp4
sudo chmod 777 $file
[ -e "/tmp/$1.info" ] || ./midentify.sh $file > /tmp/$1.info
[ ! -s /tmp/$1.info ] && echo "Error: mplayer is not installed" && exit 1
source /tmp/$1.info
ACCESS="file"
MUX="ts"
vbit=$(($ID_VIDEO_BITRATE/1024))
abit=$(($ID_AUDIO_BITRATE/1024))
tmpfile="/tmp/$1.$MUX"

vlc -I dummy --sout "#transcode{width=$ID_VIDEO_WIDTH,height=$ID_VIDEO_HEIGHT,vcodec=h264,vb=$vbit,acodec=mp4a,ab=$abit}:std{mux=$MUX,dst=$tmpfile,access=$ACCESS}" $file vlc://quit || exit 1

./segmenter $tmpfile 10 $httpdir$1 $httpdir$1.m3u8 $exportdir || exit 1
cp template.html "$httpdir$1.html"
sed -i "s/VideoName/$1/g" $httpdir$1.html
sed -i "s,$httpdir,,g" $httpdir$1.m3u8
echo "Video $httpdir$1.m3u8 is generated/updated"
