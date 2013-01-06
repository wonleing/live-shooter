all:
	gcc -Wall -g segmenter.c -o segmenter -lavformat -lavcodec -lavutil -lm -x264

clean:
	rm segmenter

install: segmenter
	cp segmenter /usr/local/bin/

uninstall:
	rm /usr/local/bin/segmenter
