package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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



/*
   Copyright 2000-2010 Bo Zimmerman

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
    private static Resources[] rscs=new Resources[256];
    private static boolean 	   compress=false;
    
	private STreeMap<String,Object> resources=new STreeMap<String,Object>(new Comparator<String>(){
		public int compare(String o1, String o2) {
			if(o1==null)
			{
				if(o2==null) return 0;
				return -1;
			}
			else
			if(o2==null)
				return 1;
			return o1.compareToIgnoreCase(o2);
		}
	});
	
	private static class CompressedResource
	{
		public byte[] data;
		public CompressedResource(byte[] d) { data=d;}
	}
	
    public Resources()
    {
        super();
        char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(rscs==null) rscs=new Resources[256];
        if(rscs[c]==null) rscs[c]=this;
    }
    public static Resources instance()
    {
        Resources r=r();
        if(r==null) r=new Resources();
        return r;
    }
    public static Resources instance(char c){ return rscs[c];}
    private static Resources r(){ return rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
    
    public static Resources newResources(){ return new Resources();}

    public static void clearResources(){r()._clearResources();}
    public static void removeResource(String ID){ r()._removeResource(ID);}
    public static Iterator<String> findResourceKeys(String srch){return r()._findResourceKeys(srch);}
    public static Object getResource(String ID){return r()._getResource(ID);}
    public static void submitResource(String ID, Object obj){r()._submitResource(ID,obj);}
    public static boolean isFileResource(String filename){return r()._isFileResource(filename);}
    public static StringBuffer getFileResource(String filename, boolean reportErrors){return r()._getFileResource(filename,reportErrors);}
    public static boolean saveFileResource(String filename, MOB whom, StringBuffer myRsc){return r()._saveFileResource(filename,whom,myRsc);}
    public static boolean updateFileResource(String filename, Object obj){return r()._updateFileResource(filename,obj);}
    public static boolean findRemoveProperty(CMFile F, String match){return r()._findRemoveProperty(F,match);}

    public static String getLineMarker(StringBuffer buf)
    {
        for(int i=0;i<buf.length()-1;i++)
            switch(buf.charAt(i))
            {
            case '\n':
                if(buf.charAt(i+1)=='\r')
                    return "\n\r";
                return "\n";
            case '\r':
                if(buf.charAt(i+1)=='\n')
                    return "\r\n";
                return "\r";
            }
        return "\n\r";
    }
    
    public static List<String> getFileLineVector(StringBuffer buf)
    {
        Vector<String> V=new Vector<String>();
        if(buf==null) return V;
        StringBuffer str=new StringBuffer("");
        for(int i=0;i<buf.length();i++)
        {
            if(((buf.charAt(i)=='\n')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\r'))
               ||((buf.charAt(i)=='\r')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\n')))
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
        V.trimToSize();
        return V;
    }

    public static String buildResourcePath(String path)
    {
        if((path==null)||(path.length()==0)) return "resources/";
        return "resources/"+path+"/";
    }

    public static void updateMultiList(String filename, Map<String, List<String>> lists)
    {
        StringBuffer str=new StringBuffer("");
        for(String ml : lists.keySet())
        {
            List<String> V=lists.get(ml);
            str.append(ml+"\r\n");
            if(V!=null)
            for(int v=0;v<V.size();v++)
                str.append(((String)V.get(v))+"\r\n");
            str.append("\r\n");
        }
        new CMFile(filename,null,false).saveText(str);
    }

    public static Map<String, List<String>> getMultiLists(String filename)
    {
        Hashtable<String,List<String>> oldH=new Hashtable<String,List<String>>();
        List<String> V=new Vector<String>();
        try{
            V=getFileLineVector(new CMFile("resources/"+filename,null,false).text());
        }catch(Exception e){}
        if((V!=null)&&(V.size()>0))
        {
            String journal="";
            List<String> set=new Vector<String>();
            for(int v=0;v<V.size();v++)
            {
                String s=(String)V.get(v);
                if(s.trim().length()==0)
                    journal="";
                else
                if(journal.length()==0)
                {
                    journal=s;
                    set=new Vector<String>();
                    oldH.put(journal,set);
                }
                else
                    set.add(s);
            }
        }
        return oldH;
    }

    public static String makeFileResourceName(String filename)
    {
        return "resources/"+filename;
    }

    public static void setCompression(boolean truefalse)
    {   compress=truefalse;}

    public static boolean _compressed(){return compress;}

	public void _clearResources()
	{
		resources.clear();
	}

	public Iterator<String> _findResourceKeys(final String srch)
	{
		final String lowerSrch=srch.toLowerCase();
		final boolean allOfThem=(lowerSrch.length()==0);
		return new FilteredIterator<String>(resources.keySet().iterator(), new Filterer<String>(){
			public boolean passesFilter(String obj) {
				return (allOfThem) || (obj.toLowerCase().indexOf(lowerSrch)>=0);
			}
		});
	}

	public Object _getResource(String ID)
	{
		Object O = resources.get(ID);
		if(O!=null)
		{
			if(compress && (O instanceof CompressedResource))
				return new StringBuffer(CMLib.encoder().decompressString(((CompressedResource)O).data));
			return O;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Object prepareObject(Object obj)
	{
        if(obj instanceof Vector) ((Vector)obj).trimToSize();
        if(obj instanceof DVector) ((DVector)obj).trimToSize();
		if(!compress) return obj;
		if(obj instanceof StringBuffer)
			return CMLib.encoder().compressString(((StringBuffer)obj).toString());
		return obj;
	}

	public Object _submitResource(String ID, Object obj)
	{
        Object prepared=prepareObject(obj);
        if(prepared != obj)
        	resources.put(ID,new CompressedResource((byte[])prepared));
        else
        	resources.put(ID,prepared);
        return prepared;
	}

	private Object _updateResource(String ID, Object obj)
	{
		return _submitResource(ID, obj);
	}

	public void _removeResource(String ID)
	{
		resources.remove(ID);
	}

	public boolean _isFileResource(String filename)
	{
	    if(_getResource(filename)!=null) return true;
	    if(new CMFile(makeFileResourceName(filename),null,false).exists())
	    	return true;
	    return false;
	}

	public StringBuffer _toStringBuffer(Object o)
	{
		if(o!=null)
		{
			if(o instanceof StringBuffer)
				return (StringBuffer)o;
			else
			if(o instanceof String)
				return new StringBuffer((String)o);
		}
		return null;
	}
	
	public StringBuffer _getFileResource(String filename, boolean reportErrors)
	{
		Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		StringBuffer buf=new CMFile(makeFileResourceName(filename),null,reportErrors).text();
    	if(!CMProps.getBoolVar(CMProps.SYSTEMB_FILERESOURCENOCACHE))
			_submitResource(filename,buf);
		return buf;
	}

    public boolean _updateFileResource(String filename, Object obj)
    {
    	if(!CMProps.getBoolVar(CMProps.SYSTEMB_FILERESOURCENOCACHE))
	    	_updateResource(CMFile.vfsifyFilename(filename), obj);
    	return _saveFileResource(filename,null,_toStringBuffer(obj));
    }

	public boolean _saveFileResource(String filename, MOB whom, StringBuffer myRsc)
	{
        boolean vfsFile=filename.trim().startsWith("::");
        boolean localFile=filename.trim().startsWith("//");
        filename=CMFile.vfsifyFilename(filename);
        if(!filename.startsWith("resources/"))
            filename="resources/"+filename;
        filename=(vfsFile?"::":localFile?"//":"")+filename;
        return new CMFile(filename,whom,false).saveRaw(myRsc);
	}

    public boolean _findRemoveProperty(CMFile F, String match)
    {
        boolean removed=false;
        StringBuffer text=F.textUnformatted();
        int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
        while(x>=0)
        {
            if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
            &&(text.substring(x+match.length()).trim().startsWith("=")))
            {
                int zb1=text.lastIndexOf("\n",x);
                int zb2=text.lastIndexOf("\r",x);
                int zb=(zb2>zb1)?zb2:zb1;
                if(zb<0) zb=0; else zb++;
                int ze1=text.indexOf("\n",x);
                int ze2=text.indexOf("\r",x);
                int ze=ze2+1;
                if((ze1>zb)&&(ze1==ze2+1)) ze=ze1+1;
                else
                if((ze2<0)&&(ze1>0)) ze=ze1+1;
                if(ze<=0) ze=text.length();
                if(!text.substring(zb).trim().startsWith("#"))
                {
                    text.delete(zb,ze);
                    x=-1;
                    removed=true;
                }
            }
            x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
        }
        if(removed) F.saveRaw(text);
        return removed;
    }
}
