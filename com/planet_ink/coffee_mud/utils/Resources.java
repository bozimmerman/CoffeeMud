package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;


public class Resources
{
	private static Vector resourceIDs=new Vector();
	private static Vector resource=new Vector();
									   

	public static Object getResource(String ID)
	{
		for(int i=0;i<resourceIDs.size();i++)
			if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
				return resource.elementAt(i);
		return null;
	}
	
	public static void submitResource(String ID, Object obj)
	{
		if(getResource(ID)!=null)
			return;
		resourceIDs.addElement(ID);
		resource.addElement(obj);
	}
	
	public static void updateResource(String ID, Object obj)
	{
		for(int i=0;i<resourceIDs.size();i++)
			if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
			{
				resource.setElementAt(obj,i);
				return;
			}
	}
	
	public static void removeResource(String ID)
	{
		for(int i=0;i<resourceIDs.size();i++)
			if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
			{
				resourceIDs.removeElementAt(i);
				resource.removeElementAt(i);
				return;
			}
	}
	
	public static StringBuffer getFileResource(String filename)
	{
		
		Object rsc=getResource(filename);
		if((rsc!=null)&&(rsc instanceof StringBuffer))
			return (StringBuffer)rsc;
		
		StringBuffer buf=new StringBuffer("");
		try
		{
			FileReader F=new FileReader("resources"+File.separatorChar+filename);
			BufferedReader reader=new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
					buf.append(line+"\n\r");
			}
			F.close();
		}
		catch(Exception e)
		{
			Log.errOut("Resource",e.getMessage());
		}
		submitResource(filename,buf);
		return buf;
	}
	
}
