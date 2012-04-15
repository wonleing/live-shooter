package com.android.liveshooter.utils;

import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class GIFFrame {
	private Vector<Bitmap> frames;

	private int index;

	public GIFFrame() {
		frames = new Vector<Bitmap>(1);
		index = 0;
	}

	public void addImage(Bitmap image) {
		frames.addElement(image);
	}

	public int size() {
		return frames.size();
	}

	public Bitmap getImage() {
		if (size() == 0) {
			return null;
		} else {
			return frames.elementAt(index);
		}
	}

	public void nextFrame() {
		if (index + 1 < size()) {
			index++;
		} else {
			index = 0;
		}
	}

	public static GIFFrame CreateGifImage(byte abyte0[], int width, int height) {
		try {
			GIFFrame GF = new GIFFrame();
			Bitmap image = null;
			GIFDecoder gifdecoder = new GIFDecoder(abyte0);
			for (; gifdecoder.moreFrames(); gifdecoder.nextFrame()) {
				try {
					image = gifdecoder.decodeImage();
					if (GF != null && image != null) {
						GF.addImage(resizedBitmap(image, width, height));
					}
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			gifdecoder.clear();
			gifdecoder = null;
			return GF;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Bitmap resizedBitmap(Bitmap bitmap1, int newWidth,
			int newHeight) {
		float oldWidth = 0f;
		float oldHeight = 0f;
		float nw = (float) newWidth;
		float nh = (float) newHeight;
		int ioldWidth = bitmap1.getWidth();
		int ioldHeight = bitmap1.getHeight();
		oldWidth = (float) (bitmap1.getWidth());
		oldHeight = (float) (bitmap1.getHeight());
		Matrix matrix = new Matrix();
		matrix.postScale(nw / oldWidth, nh / oldHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap1, 0, 0, ioldWidth,
				ioldHeight, matrix, false);
		return resizedBitmap;
	}
}
