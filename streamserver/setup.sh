#!/bin/bash
sudo apt-get install libavformat-dev libx264-dev libfaac-dev libmp3lame-dev libbz2-dev zlib1g-dev libfaad-dev mplayer
sudo make
cp /usr/share/mplayer/midentify.sh . 
