package com.planet_ink.coffee_mud.common;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class INI extends Properties
{
	public boolean loaded=false;


	public INI(String filename)
	{
		try
		{
			this.load(new FileInputStream(filename));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

	public INI(Properties p, String filename)
	{
		super(p);
		
		try
		{
			this.load(new FileInputStream(filename));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}


	public static INI loadPropPage(String iniFile)
	{
		INI page=null;
		if (page==null || !page.loaded)
		{
			page=new INI(iniFile);
			if(!page.loaded)
				return null;
		}
		return page;
	}
	public static Vector loadEnumerablePage(String iniFile)
	{
		StringBuffer str=Resources.getFile(iniFile,true);
		if((str==null)||(str.length()==0)) return new Vector();
		Vector page=Resources.getFileLineVector(str);
		for(int p=0;p<(page.size()-1);p++)
		{
			String s=((String)page.elementAt(p)).trim();
			if(s.startsWith("#")||s.startsWith("!")) continue;
			if((s.endsWith("\\"))&&(!s.endsWith("\\\\")))
			{
				s=s.substring(0,s.length()-1)+((String)page.elementAt(p+1)).trim();
				page.removeElementAt(p+1);
				page.setElementAt(s,p);
				p=p-1;
			}
		}
		return page;
	}

	/** retrieve a particular .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=propertyGetter(p,"TAG");
	* @param tagToGet	the property tag to retreive.
	* @return String	the value of the .ini file tag
	*/
	public String getStr(String tagToGet)
	{
		String thisTag=this.getProperty(tagToGet);
		if(thisTag==null) return "";
		return thisTag;
	}

	public boolean getBoolean(String tagToGet)
	{
		String thisVal=getStr(tagToGet);
		if(thisVal.toUpperCase().startsWith("T"))
			return true;
		return false;
	}

	/** retrieve a particular .ini file entry as an integer
	*
	* <br><br><b>Usage:</b>  int i=propertyGetterOfInteger(p,"TAG");
	* @param tagToGet	the property tag to retreive.
	* @return int	the value of the .ini file tag
	*/
	public int getInt(String tagToGet)
	{
		try
		{
			return Integer.parseInt(getStr(tagToGet));
		}
		catch(Throwable t)
		{
			return 0;
		}
	}
}
