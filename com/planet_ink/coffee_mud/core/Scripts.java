package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.util.regex.Pattern;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
	private static Hashtable parserSections=null;
	
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

	public static String replaceWithDefinitions(DVector global, DVector local, String str)
	{
		for(int v=0;v<local.size();v++)
			str=CMStrings.replaceAll(str,(String)local.elementAt(v,1),(String)local.elementAt(v,2));
		for(int v=0;v<global.size();v++)
			str=CMStrings.replaceAll(str,(String)global.elementAt(v,1),(String)global.elementAt(v,2));
		return str;
	}
	
    public static DVector getLanguageParser(String parser)
    {
    	if(parserSections==null)
    	{
    		parserSections=new Hashtable();
    		CMFile F=new CMFile("resources/parser.properties",null,false,true);
    		if(!F.exists()){ Log.errOut("Parser text not found! This mud is in deep doo-doo!"); return null;}
    		StringBuffer alldata=F.text();
    		Vector V=Resources.getFileLineVector(alldata);
    		String s=null;
    		DVector currentSection=null;
    		DVector globalDefinitions=new DVector(2);
    		DVector localDefinitions=new DVector(2);
    		for(int v=0;v<V.size();v++)
    		{
    			s=((String)V.elementAt(v)).trim();
    			if((s.startsWith("#"))||(s.trim().length()==0)) continue;
    			if(s.startsWith("["))
    			{
    				int x=s.lastIndexOf("]");
    				currentSection=new DVector(3);
    				parserSections.put(s.substring(1,x).toUpperCase(),currentSection);
    				localDefinitions.clear();
    			}
    			else
				if(s.toUpperCase().startsWith("DEFINE"))
				{
					int regstart=s.indexOf('"');
					if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					int regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					String variable=s.substring(regstart+1,regend).toUpperCase();
					s=s.substring(regend+1).trim();
					if(!s.toUpperCase().startsWith("AS")){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					regstart=s.indexOf('"');
					if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					String replacement=s.substring(regstart+1,regend);
					replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
					if(currentSection!=null)
					{
						localDefinitions.removeElement(variable);
						localDefinitions.addElement(variable,replacement);
					}
					else
					{
						globalDefinitions.removeElement(variable);
						globalDefinitions.addElement(variable,replacement);
					}
				}
				else
				if(s.toUpperCase().startsWith("REPLACE"))
				{
					String cmd="REPLACE";
					int regstart=s.indexOf('"');
					if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					int regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					String expression=s.substring(regstart+1,regend);
					expression=replaceWithDefinitions(globalDefinitions,localDefinitions,expression);
					s=s.substring(regend+1).trim();
					if(!s.toUpperCase().startsWith("WITH")){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					regstart=s.indexOf('"');
					if(regstart<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					regend=s.indexOf('"',regstart+1);
					while((regend>regstart)&&(s.charAt(regend-1)=='\\'))
						regend=s.indexOf('"',regend+1);
					if(regend<0){ Log.errOut("Scripts","Malformed parser, line "+v); continue;}
					String replacement=s.substring(regstart+1,regend);
					replacement=replaceWithDefinitions(globalDefinitions,localDefinitions,replacement);
					try
					{
				        Pattern expPattern=Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
						currentSection.addElement(cmd,expPattern,replacement);
					}
					catch(Exception e){Log.errOut("Scripts",e);}
				}
				else
					Log.errOut("Scripts","Unknown parser command, line "+v);
    		}
    	}
    	return (DVector)parserSections.get(parser);
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
			Log.errOut("Scripts","Unknown file: resources/scripts/"+language.toUpperCase()+"_"+country.toUpperCase()+"/"+ID+".properties");
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
