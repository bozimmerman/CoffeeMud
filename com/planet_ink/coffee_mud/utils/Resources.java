package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;


public class Resources
{
	private static boolean compress=false;
	private static Vector resourceIDs=new Vector();
	private static Vector resource=new Vector();
									   
	public static void clearResources()
	{
		resourceIDs=new Vector();
		resource=new Vector();
	}
	
	public static Vector findResourceKeys(String srch)
	{
		Vector V=new Vector();
		for(int i=0;i<resourceIDs.size();i++)
		{
			String key=(String)resourceIDs.elementAt(i);
			if((srch.length()==0)||(key.toUpperCase().indexOf(srch.toUpperCase())>=0))
				V.addElement(key);
		}
		return V;
	}
	
	private static Object fetchResource(int x)
	{
		if((x<resource.size())&&(x>=0))
		{
			if(!compress) return resource.elementAt(x);
			if(resource.elementAt(x) instanceof byte[])
				return new StringBuffer(Util.decompressString((byte[])resource.elementAt(x)));
			else
				return resource.elementAt(x);
		}
		return null;
	}
	
	public static Object getResource(String ID)
	{
		for(int i=0;i<resourceIDs.size();i++)
			if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
				return fetchResource(i);
		return null;
	}

	public static Object prepareObject(Object obj)
	{
		if(!compress) return obj;
		if(obj instanceof StringBuffer)
			return Util.compressString(((StringBuffer)obj).toString());
		return obj;
	}
	
	public static void submitResource(String ID, Object obj)
	{
		if(getResource(ID)!=null)
			return;
		resourceIDs.addElement(ID);
		resource.addElement(prepareObject(obj));
	}
	
	public static void updateResource(String ID, Object obj)
	{
		for(int i=0;i<resourceIDs.size();i++)
			if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
			{
				resource.setElementAt(prepareObject(obj),i);
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
		
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<buf.length()-1;i++)
		{
			if(((buf.charAt(i)=='\n')&&(buf.charAt(i+1)=='\r'))
			   ||((buf.charAt(i)=='\r')&&(buf.charAt(i+1)=='\n')))
			{
				i++;
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
			if(((buf.charAt(i)=='\r'))
			||((buf.charAt(i)=='\n')))
			{
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
				str.append(buf.charAt(i));
		}
		if(str.length()>0)
			V.addElement(str.toString());
		return V;
	}
	
	public static StringBuffer getFile(String filename)
	{ return getFile(filename,true);}
	public static StringBuffer getFile(String filename, boolean reportErrors)
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
			if(reportErrors)
				Log.errOut("Resource",e.getMessage());
			return null;
		}
		return buf;
	}
	
	public static StringBuffer getFileResource(String filename)
	{ return getFileResource(filename,true);}
	public static StringBuffer getFileResource(String filename, boolean reportErrors)
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
		
		StringBuffer buf=getFile("resources"+File.separatorChar+filename,reportErrors);
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
	
	public static void setCompression(boolean truefalse)
	{	compress=truefalse;}
	public static boolean compressed(){return compress;}
	
}
