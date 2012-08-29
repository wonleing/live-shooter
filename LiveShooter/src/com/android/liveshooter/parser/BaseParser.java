package com.android.liveshooter.parser;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.android.liveshooter.service.LiveShooterException;

/**
 * @author Administrator
 *
 */
public abstract class BaseParser {
	protected final String ENCODE = "UTF-8";
	
	
	
	/**
	 * Invoke this method to parse XML data.
	 * @author Arashmen
	 * */
	public abstract Object executeToObject(InputStream in,Context context)throws LiveShooterException;
	
	
	/**
	 * return the element's text when it is invoked.
	 * @author Arashmen
	 * */
	protected String getElementTagText(XmlPullParser xpp) throws XmlPullParserException, IOException{
		xpp.next();
		return xpp.getText();
	}
	
	
}
