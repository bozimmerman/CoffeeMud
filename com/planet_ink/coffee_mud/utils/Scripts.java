package com.planet_ink.coffee_mud.utils;
import java.util.*;

public class Scripts
{
	private Scripts(){}
	
	private static String language="en";
	private static String country="TX";
	private static Locale currentLocale;
	private static Vector allScripts=null;
	private static Hashtable scripts=null;
	
	public static void setLocale(String lang, String state)
	{
		if((lang!=null)&&(state!=null)&&(lang.length()>0)&&(state.length()>0))
		{
			country=state;
			language=lang;
		}
		currentLocale = new Locale(language, country);
		allScripts=Resources.getFileLineVector(Resources.getFile("resources/messages_"+language+"_"+country+".scripts"));
	}

	public static String[] load(String ID)
	{
		if(allScripts==null) setLocale(language,country);
		if(scripts==null) scripts=new Hashtable();
		if(scripts.containsKey(ID))
			return (String[])scripts.get(ID);
		Vector script=new Vector();
		boolean reading=false;
		for(int i=0;i<allScripts.size();i++)
		{
			String str=(String)allScripts.elementAt(i);
			if(str.startsWith("["))
			{
				reading=false;
				if(str.substring(1).startsWith(ID+"]"))
				   reading=true;
			}
			else
			if(reading)
				script.addElement(str);
		}
		String[] strings=new String[script.size()+1];
		for(int i=0;i<script.size();i++)
			strings[i]=(String)script.elementAt(i);
		scripts.put(ID,strings);
		return strings;
	}
	
	public static void clear()
	{
		scripts=null;
		allScripts=null;
	}
	
}
