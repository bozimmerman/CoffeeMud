package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
	
	public static void updateMultiList(String filename, Hashtable lists)
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration e=lists.keys();e.hasMoreElements();)
		{
			String ml=(String)e.nextElement();
			Vector V=(Vector)lists.get(ml);
			str.append(ml+"\r\n");
			if(V!=null)
			for(int v=0;v<V.size();v++)
				str.append(((String)V.elementAt(v))+"\r\n");
			str.append("\r\n");
		}
		Resources.saveFileResource(filename,str);
	}
	
	public static Hashtable getMultiLists(String filename)
	{
		Hashtable oldH=new Hashtable();
		Vector V=new Vector();
		try{
			V=Resources.getFileLineVector(Resources.getFile("resources"+File.separatorChar+filename,false));
		}catch(Exception e){}
		if((V!=null)&&(V.size()>0))
		{
			String journal="";
			Vector set=new Vector();
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				if(s.trim().length()==0)
					journal="";
				else
				if(journal.length()==0)
				{
					journal=s;
					set=new Vector();
					oldH.put(journal,set);
				}
				else
					set.addElement(s);
			}
		}
		return oldH;
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
		try{
			for(int i=0;i<resourceIDs.size();i++)
				if(((String)resourceIDs.elementAt(i)).equalsIgnoreCase(ID))
				{
					resourceIDs.removeElementAt(i);
					resource.removeElementAt(i);
					return;
				}
		}catch(ArrayIndexOutOfBoundsException e){}
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
	
	public static StringBuffer getFileRaw(String filename)
	{ return getFileRaw(filename,true);}
	public static StringBuffer getFileRaw(String filename, boolean reportErrors)
	{
		StringBuffer buf=new StringBuffer("");
		try
		{
			FileReader F=new FileReader(filename);
			char c=' ';
			while(F.ready())
			{
				c=(char)F.read();
				if(c<0) break;
				buf.append(c);
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
				{
					buf.append(line);
					buf.append("\n");
				}
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
	
	public static String makeFileResourceName(String filename)
	{
	    return "resources"+File.separatorChar+filename;
	}
	public static StringBuffer getFileResource(String filename)
	{ return getFileResource(filename,true);}
	public static boolean isFileResource(String filename)
	{
	    if(getResource(filename)!=null) return true;
	    if(getFile(makeFileResourceName(filename))!=null)
	    	return true;
	    return false;
	}
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
		
		StringBuffer buf=getFile(makeFileResourceName(filename));
		if(buf==null) buf=new StringBuffer("");
		submitResource(filename,buf);
		return buf;
	}
	
	public static StringBuffer saveBufNormalize(StringBuffer myRsc)
	{
	    for(int i=0;i<myRsc.length();i++)
	        if(myRsc.charAt(i)=='\n')
	        {
	    	    for(i=myRsc.length()-1;i>=0;i--)
	    	        if(myRsc.charAt(i)=='\r')
	    	            myRsc.deleteCharAt(i);
	    	    return myRsc;
	        }
	    for(int i=0;i<myRsc.length();i++)
	        if(myRsc.charAt(i)=='\r')
	            myRsc.setCharAt(i,'\n');
	    return myRsc;
	}
	
	public static void saveFileResource(String filename)
	{saveFileResource(filename,getFileResource(filename));}
	public static void saveFile(String filename, StringBuffer myRsc)
	{
		if(myRsc==null)
		{
			Log.errOut("Resources","Unable to save file '"+filename+"': No Data.");
			return;
		}
		try
		{
			File F=new File(filename);
			FileWriter FW=new FileWriter(F);
			FW.write(saveBufNormalize(myRsc).toString());
			FW.close();
		}
		catch(IOException e)
		{
			Log.errOut("Resources",e);
		}
	}
	public static void saveFileResource(String filename, StringBuffer myRsc)
	{
		if(myRsc==null)
		{
			Log.errOut("Resources","Unable to save file resource '"+filename+"': No Data.");
			return;
		}
		try
		{
			File F=new File("resources"+File.separatorChar+filename);
			FileWriter FW=new FileWriter(F);
			FW.write(saveBufNormalize(myRsc).toString());
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
