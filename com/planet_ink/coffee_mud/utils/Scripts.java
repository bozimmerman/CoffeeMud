package com.planet_ink.coffee_mud.utils;
import java.util.*;

public class Scripts
{
	private Scripts(){}
	
	private static String language="en";
	private static String country="TX";
	private static Locale currentLocale;
	private static ResourceBundle messages;

	
	public static void setLocale(String lang, String state)
	{
		if((lang!=null)&&(state!=null)&&(lang.length()>0)&&(state.length()>0))
		{
			country=state;
			language=lang;
		}
		currentLocale = new Locale(language, country);
		messages = ResourceBundle.getBundle("resources/messages", currentLocale);
	}
	
	public static String get(String tag)
	{
		if(messages==null) setLocale(language,country);
		return messages.getString(tag);
	}
	
	public static String get(String tag, String replaceX)
	{
		if(messages==null) setLocale(language,country);
		String msg=messages.getString(tag);
		if(msg!=null) msg=Util.replaceAll(msg,"@x1",replaceX);
		else msg="";
		return msg;
	}
	public static String get(String tag, String replaceX, String replaceX2)
	{
		if(messages==null) setLocale(language,country);
		String msg=messages.getString(tag);
		if(msg!=null)
		{
			msg=Util.replaceAll(msg,"@x1",replaceX);
			msg=Util.replaceAll(msg,"@x2",replaceX2);
		}
		else msg="";
		return msg;
	}
	public static String get(String tag, String replaceX, String replaceX2, String replaceX3)
	{
		if(messages==null) setLocale(language,country);
		String msg=messages.getString(tag);
		if(msg!=null)
		{
			msg=Util.replaceAll(msg,"@x1",replaceX);
			msg=Util.replaceAll(msg,"@x2",replaceX2);
			msg=Util.replaceAll(msg,"@x3",replaceX3);
		}
		else msg="";
		return msg;
	}
	public static void clear()
	{
		messages=null;
	}
	
}
