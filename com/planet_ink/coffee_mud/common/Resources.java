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
    public static final int VFS_TEXT=0;
    public static final int VFS_BINARY=1;
    public static final int VFS_READONLY=2;
    public static final int VFS_HIDDEN=4;
    
    public static final int VFS_INFO_FILENAME=0;
    public static final int VFS_INFO_BITS=1;
    public static final int VFS_INFO_DATE=2;
    public static final int VFS_INFO_WHOM=3;
    public static final int VFS_INFO_DATA=4;
    
	private static boolean compress=false;
	private static DVector resources=new DVector(3);
    private static Vector vfs=null;
    
	public static void clearResources()
	{
		resources=new DVector(3);
        vfs=null;
	}
    
    public static String buildPath(String path)
    { return path+File.separatorChar;}
    
    public static String buildResourcePath(String path)
    {
        if((path==null)||(path.length()==0)) return "resources"+File.separatorChar;
        return "resources"+File.separatorChar+path+File.separatorChar;
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
		Resources.saveFileResource(filename,whom,vfsBits,str);
	}
	
	public static Hashtable getMultiLists(String filename)
	{
		Hashtable oldH=new Hashtable();
		Vector V=new Vector();
		try{
			V=Resources.getFileLineVector(Resources.getFile(Resources.buildResourcePath(null)+filename,false));
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
    public static Vector getVFSDirectory()
    {
        if(vfs==null)
            vfs=CMClass.DBEngine().DBReadVFSDirectory();
        return vfs;
    }
    public static String unvfsFilename(String filename)
    {
        if(filename.startsWith("::"))
            filename=filename.substring(2);
        else
        if(filename.trim().startsWith("::"))
            filename=filename.trim().substring(2);
        return filename;
    }
    public static boolean isVFSFile(String filename)
    {
        if(filename.trim().startsWith("::"))
            return true;
        Vector vfs=getVFSDirectory();
        filename=Util.replaceAll(unvfsFilename(filename),"\\","/");
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return true;
        }
        return false;
    }
    public static Vector getVFSFileInfo(String filename)
    {
        Vector vfs=getVFSDirectory();
        filename=Util.replaceAll(unvfsFilename(filename),"\\","/");
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return file;
        }
        return null;
    }
    
    public static Object getVFSFileData(String filename)
    {
        Vector vfs=getVFSDirectory();
        filename=Util.replaceAll(unvfsFilename(filename),"\\","/");
        Vector file=null;
        for(Enumeration e=vfs.elements();e.hasMoreElements();)
        {
            file=(Vector)e.nextElement();
            if(((String)file.firstElement()).equalsIgnoreCase(filename))
                return CMClass.DBEngine().DBReadVFSFile((String)file.firstElement());
        }
        return null;
    }
	public static StringBuffer getFile(String filename, boolean reportErrors)
	{
        StringBuffer buf=new StringBuffer("");
        if(isVFSFile(filename))
        {
            filename=unvfsFilename(filename);
            Vector info=getVFSFileInfo(filename);
            if(info!=null)
            {
                int bits=((Integer)info.elementAt(VFS_INFO_BITS)).intValue();
                if(Util.bset(bits,VFS_BINARY))
                    return buf;
                Object data=getVFSFileData(filename);
                if(data==null) return buf;
                if(data instanceof String)
                    return new StringBuffer((String)data);
                if(data instanceof StringBuffer)
                    return (StringBuffer)data;
            }
            return null;
        }
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
					buf.append("\n\r");
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
	    return buildResourcePath(null)+filename;
	}
	public static boolean isFileResource(String filename)
	{
	    if(getResource(filename)!=null) return true;
	    if(getFile(makeFileResourceName(filename),false)!=null)
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
		
		StringBuffer buf=getFile(makeFileResourceName(filename),reportErrors);
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
	
	public static boolean saveFile(String filename, String whom, int vfsBits, StringBuffer myRsc)
	{
		if(myRsc==null)
		{
			Log.errOut("Resources","Unable to save file '"+filename+"': No Data.");
			return false;
		}
        if(isVFSFile(filename))
        {
            Vector info=getVFSFileInfo(filename);
            if(info!=null)
            {
                filename=(String)info.firstElement();
                if(vfsBits<0) vfsBits=((Integer)info.elementAt(VFS_INFO_BITS)).intValue();
                if(vfs!=null) vfs.removeElement(info);
                CMClass.DBEngine().DBDeleteVFSFile(filename);
            }
            if(vfsBits<0) vfsBits=0;
            if(whom==null) whom="unknown";
            info=new Vector();
            info.addElement(filename);
            info.addElement(new Integer(vfsBits));
            info.addElement(new Long(System.currentTimeMillis()));
            info.addElement(whom);
            CMClass.DBEngine().DBCreateVFSFile(filename,vfsBits,whom,myRsc);
            return true;
        }
		try
		{
			File F=new File(filename);
			FileWriter FW=new FileWriter(F);
			FW.write(saveBufNormalize(myRsc).toString());
			FW.close();
            return true;
		}
		catch(IOException e)
		{
			Log.errOut("Resources","Save "+filename+": "+e.getMessage());
		}
        return false;
	}
    public static boolean saveFileResource(String filename)
    {return saveFileResource(filename,null,-1,getFileResource(filename,false));}
	public static boolean saveFileResource(String filename, String whom, int vfsBits, StringBuffer myRsc)
	{
        boolean vfsFile=filename.trim().startsWith("::");
        filename=unvfsFilename(filename);
        if(!filename.startsWith("resources"+File.separatorChar))
            saveFile((vfsFile?"::":"")+"resources"+File.separatorChar+filename,whom,vfsBits,myRsc);
        else
            saveFile((vfsFile?"::":"")+filename,whom,vfsBits,myRsc);
        return false;
	}
	
	public static void setCompression(boolean truefalse)
	{	compress=truefalse;}
	public static boolean compressed(){return compress;}
	
}
