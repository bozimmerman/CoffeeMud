package com.planet_ink.coffee_mud.utils;
import java.util.*;

public class Scripts
{
	private Scripts(){}
	
	private static String language="en";
	private static String country="US";
	private static Locale currentLocale;
	private static ResourceBundle messages;

	
	private static void setLocale(String lang, String state)
	{
		country=state;
		language=lang;
		currentLocale = new Locale(language, country);
		messages = ResourceBundle.getBundle("resources/messages", currentLocale);
	}
	
	private String get(String tag)
	{
		if(messages==null) setLocale("en","us");
		return messages.getString(tag);
	}
	
}
