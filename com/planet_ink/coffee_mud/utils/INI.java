package com.planet_ink.coffee_mud.utils;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class INI extends Properties
{
	public boolean loaded=false;
	
	
	public INI(String filename)
	{
		try
		{
			this.load(new FileInputStream("coffeemud.ini"));
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
	
	public Hashtable loadHashListToObj(String filePath)
	{
		Hashtable h=new Hashtable();
		loadListToObj(h,filePath);
		return h;
	}
	public Vector loadVectorListToObj(String filePath)
	{
		Vector v=new Vector();
		loadListToObj(v,filePath);
		return v;
	}
	public void loadListToObj(Object toThis, String filePath)
	{
		StringBuffer objPathBuf=new StringBuffer(filePath);
		String objPath=objPathBuf.toString();
		int x=0;
		while((x=objPath.indexOf(File.separatorChar))>=0)
		{
			objPathBuf.setCharAt(x,'.');
			objPath=objPathBuf.toString();
		}
		File directory=new File(filePath);
		if((directory.canRead())&&(directory.isDirectory()))
		{
			String[] list=directory.list();
			for(int l=0;l<list.length;l++)
			{
				String item=list[l];
				if((item!=null)&&(item.length()>0))
				{
					if(item.toUpperCase().endsWith(".CLASS"))
					{
						item=item.substring(0,item.length()-6);
						try
						{
							Object O=(Object)Class.forName(objPath+item).newInstance();
							if(toThis instanceof Hashtable)
								((Hashtable)toThis).put(item.trim(),O);
							else
							if(toThis instanceof Vector)
								((Vector)toThis).addElement(O);
						}
						catch(Exception e)
						{
									
						}
					}
				}
			}
		}
	}
	
	public static String className(Object O)
	{
		if(O==null) return "";
		String name=O.getClass().getName();
		int lastDot=name.lastIndexOf(".");
		if(lastDot>=0)
			return name.substring(lastDot+1);
		else
			return name;
	}
}
