package com.planet_ink.coffee_mud.common;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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
