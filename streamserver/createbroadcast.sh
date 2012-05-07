#!/bin/bash
[ ! -e segmenter ] && echo "Please run setup first and make sure segmenter is built" && exit 1
[ $# != 2 ] && echo "Usage: $0 <video_name> <path_to_video_segments>" && exit 1
[ -e "/tmp/$1.info" ] || ./midentify.sh $2 > /tmp/$1.info
source /tmp/$1.info
httpdir="/var/www/uploads/"
exportdir="http://localhost/uploads/"
ACCESS="file"
MUX="ts"
vbit=$(($ID_VIDEO_BITRATE/1024))
abit=$(($ID_AUDIO_BITRATE/1024))
tmpfile="/tmp/$1.$MUX"

vlc -I dummy --sout "#transcode{width=$ID_VIDEO_WIDTH,height=$ID_VIDEO_HEIGHT,vcodec=h264,vb=$vbit,acodec=mp4a,ab=$abit}:std{mux=$MUX,dst=$tmpfile,access=$ACCESS}" $2 vlc://quit

./segmenter $tmpfile 10 $httpdir$1 $httpdir$1.m3u8 $exportdir
cp template.html "$httpdir$1.html"
sed -i "s/VideoName/$1/g" $httpdir$1.html
echo "Video Streaming is done"
