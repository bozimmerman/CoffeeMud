package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scripts
{
	private Scripts(){}
	
	private static String language="en";
	private static String country="TX";
	private static Locale currentLocale;
	private static HashMap scripts=null;
	
	public static void setLocale(String lang, String state)
	{
		if((lang!=null)&&(state!=null)&&(lang.length()>0)&&(state.length()>0))
		{
			country=state;
			language=lang;
		}
		currentLocale = new Locale(language, country);
		scripts=new HashMap();
	}

	public static ResourceBundle load(String ID)
	{
		if(scripts==null)
			setLocale(language,country);
		if(scripts.containsKey(ID))
			return (ResourceBundle)scripts.get(ID);
		ResourceBundle buf=null;
		try{
			buf = ResourceBundle.getBundle("resources/scripts/"+language.toUpperCase()+"_"+country.toUpperCase()+"/"+ID,currentLocale);
		}catch(Exception e){}
		if(buf==null)
			Log.errOut("Scripts","Unknown file: resources/scripts/"+language.toUpperCase()+"_"+country.toUpperCase()+"/"+ID+".ini");
		else
		{
			synchronized(scripts)
			{
				scripts.put(ID,buf);
			}
		}
		return buf;
	}
	
	public static void clear()
	{
		scripts=null;
	}
	
}
