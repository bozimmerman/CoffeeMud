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
	
	public static void clear()
	{
		messages=null;
	}
	
}
