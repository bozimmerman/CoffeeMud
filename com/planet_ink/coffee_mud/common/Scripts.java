package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
