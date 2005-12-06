package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.MOB;
import com.planet_ink.coffee_mud.interfaces.Room;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
import java.util.*;



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
	private static DVector resources=new DVector(3);
    
	public static void clearResources()
	{
		resources=new DVector(3);
	}
    
    public static String buildResourcePath(String path)
    {
        if((path==null)||(path.length()==0)) return "resources"+CMFile.pathSeparator;
        return "resources"+CMFile.pathSeparator+path+CMFile.pathSeparator;
    }
	
	public static void updateMultiList(String filename, String whom, int vfsBits, Hashtable lists)
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
		Resources.saveFile(filename,whom,vfsBits,str);
	}
	
	public static Hashtable getMultiLists(String filename)
	{
		Hashtable oldH=new Hashtable();
		Vector V=new Vector();
		try{
			V=Resources.getFileLineVector(CMFile.getFile(Resources.buildResourcePath(null)+filename,false));
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
		for(int i=0;i<resources.size();i++)
		{
			String key=(String)resources.elementAt(i,1);
			if((srch.length()==0)||(key.toUpperCase().indexOf(srch.toUpperCase())>=0))
				V.addElement(key);
		}
		return V;
	}
	
	private static Object fetchResource(int x)
	{
		if((x<resources.size())&&(x>=0))
		{
			if(!compress) return resources.elementAt(x,2);
			if((((Boolean)resources.elementAt(x,3)).booleanValue())
            &&(resources.elementAt(x,2) instanceof byte[]))
				return new StringBuffer(CMEncoder.decompressString((byte[])resources.elementAt(x,2)));
			return resources.elementAt(x,2);
		}
		return null;
	}
	
	public static Object getResource(String ID)
	{
		for(int i=0;i<resources.size();i++)
			if(((String)resources.elementAt(i,1)).equalsIgnoreCase(ID))
				return fetchResource(i);
		return null;
	}

	public static Object prepareObject(Object obj)
	{
		if(!compress) return obj;
		if(obj instanceof StringBuffer)
			return CMEncoder.compressString(((StringBuffer)obj).toString());
		return obj;
	}
	
	public static void submitResource(String ID, Object obj)
	{
		if(getResource(ID)!=null)
			return;
        Object prepared=prepareObject(obj);
		resources.addElement(ID,prepared,new Boolean(prepared!=obj));
	}
	
	public static void updateResource(String ID, Object obj)
	{
		for(int i=0;i<resources.size();i++)
			if(((String)resources.elementAt(i,1)).equalsIgnoreCase(ID))
			{
                Object prepared=prepareObject(obj);
				resources.setElementAt(i,2,prepared);
                resources.setElementAt(i,3,new Boolean(prepared!=obj));
				return;
			}
	}
	
	public static void removeResource(String ID)
	{
		try{
			for(int i=0;i<resources.size();i++)
				if(((String)resources.elementAt(i,1)).equalsIgnoreCase(ID))
				{
					resources.removeElementAt(i);
					return;
				}
		}catch(ArrayIndexOutOfBoundsException e){}
	}
	
	public static Vector getFileLineVector(StringBuffer buf)
	{
		Vector V=new Vector();
        if(buf==null) return V;
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
	
	public static String makeFileResourceName(String filename)
	{
	    return buildResourcePath(null)+filename;
	}
	public static boolean isFileResource(String filename)
	{
	    if(getResource(filename)!=null) return true;
	    if(CMFile.getFile(makeFileResourceName(filename),false)!=null)
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
		
		StringBuffer buf=CMFile.getFile(makeFileResourceName(filename),reportErrors);
		if(buf==null) buf=new StringBuffer("");
		submitResource(filename,buf);
		return buf;
	}
	
    public static boolean saveFileResource(String filename)
    {return saveFileResource(filename,null,-1,getFileResource(filename,false));}
	public static boolean saveFileResource(String filename, String whom, int vfsBits, StringBuffer myRsc)
	{
        boolean vfsFile=filename.trim().startsWith("::");
        boolean localFile=filename.trim().startsWith("||");
        filename=CMFile.fixFilename(filename);
        if(!filename.startsWith("resources"+CMFile.pathSeparator))
            CMFile.saveFile((vfsFile?"::":localFile?"||":"")+"resources"+CMFile.pathSeparator+filename,whom,vfsBits,myRsc);
        else
            CMFile.saveFile((vfsFile?"::":localFile?"||":"")+filename,whom,vfsBits,myRsc);
        return false;
	}
	
	public static void setCompression(boolean truefalse)
	{	compress=truefalse;}
	public static boolean compressed(){return compress;}
	
}
