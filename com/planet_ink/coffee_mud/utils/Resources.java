package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;


public class Resources
{
	private static Vector resourceIDs=new Vector();
	private static Vector resource=new Vector();
									   

	public static void clearResources()
	{
		resourceIDs=new Vector();
		resource=new Vector();
	}
	
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
	
	public static Vector getFileLineVector(StringBuffer buf)
	{
		Vector V=new Vector();
		
		String str="";
		for(int i=0;i<buf.length();i++)
		{
			if((buf.charAt(i)=='\n')&&(buf.charAt(i+1)=='\r'))
			{
				i++;
				V.addElement(str);
				str="";
			}
			else
				str+=buf.charAt(i);
		}
		if(str.length()>0)
			V.addElement(str);
		return V;
	}
	
	public static StringBuffer getFile(String filename)
	{
		StringBuffer buf=new StringBuffer("");
		try
		{
			FileReader F=new FileReader(filename);
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
			return null;
		}
		return buf;
	}
	
	public static StringBuffer getFileResource(String filename)
	{
		
		Object rsc=getResource(filename);
		if(rsc!=null)
		{
			if(rsc instanceof StringBuffer)
				return (StringBuffer)rsc;
			else
			if(rsc instanceof String)
				return new StringBuffer((String)rsc);
		}
		
		StringBuffer buf=getFile("resources"+File.separatorChar+filename);
		if(buf==null) buf=new StringBuffer("");
		submitResource(filename,buf);
		return buf;
	}
	
	public static void saveFileResource(String filename)
	{
		StringBuffer myRsc=getFileResource(filename);
		if(myRsc==null){
			Log.errOut("Resources","Unable to read file resource '"+filename+"'.");
			return;
		}
		try
		{
			File F=new File("resources"+File.separatorChar+filename);
			FileWriter FW=new FileWriter(F);
			FW.write(myRsc.toString());
			FW.close();
		}
		catch(IOException e)
		{
			Log.errOut("Resources",e);
		}
	}
	
}
