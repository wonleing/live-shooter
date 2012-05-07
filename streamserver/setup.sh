#!/bin/bash
#Ubuntu server setup
#sudo apt-get install libavformat-dev libx264-dev libfaac-dev libmp3lame-dev libbz2-dev zlib1g-dev libfaad-dev mplayer

#Fedora server setup, install RPMFusion.org/Configuration Fusion.rpm first
sudo yum install ffmpeg-devel mplayer vlc
cd /usr/include
sudo ln -s ffmpeg/libavformat libavformat
sudo ln -s ffmpeg/libavcodec libavcodec
sudo ln -s ffmpeg/libavutil libavutil
cd -
sudo make 2>&1 | grep -v warning
