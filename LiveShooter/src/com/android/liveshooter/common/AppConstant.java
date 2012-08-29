package com.android.liveshooter.common;

import android.os.Environment;

public interface AppConstant {
    

	
/**
 * number from -1 to 9 
 * */
	int NO_0 = 0;
	int NO_1 = 1;
	int NO_2 = 2;
	int NO_3 = 3;
	int NO_4 = 4;
	int NO_5 = 5;
	int NO_6 = 6;
	int NO_7 = 7;
	int NO_8 = 8;
	int NO_9 = 9;
	int NO_10 = 10;
	int NO_WAP_30 = 30;
	int NO_WAP_50 = 50;
	int NO_ERROR = -1;
	int NO_END = -1;

/**
 * seconds count
 * */	
	int SECOND_HALF= 500;
	int SECOND_1 = 1000;
	

/**
 * common character for reusing
 * */
	char CHARACTER_SLASH = '/';
	char CHARACTER_EQUAL = '=';
	char CHARACTER_AND = '&';
	char CHARACTER_QUESTION = '?';
	char CHARACTER_BARS = '-';
	
/**
 * buffering size
 * */
	int BUFFER_SIZE = 1024;
	
/**
 * the path of save images
 */
	String IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VGongyi/imagefiles";
	
	String TAG_AD = "advertisement";


}
   
  
