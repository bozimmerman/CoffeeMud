package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.collections.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
	private static final Resources[] rscs			 =new Resources[256];
	private static boolean 	 		 compress		 =false;
	private static Object 			 propResourceSync=new Object();
	
	private static Map<String,Map<String,String>> propResources;

	/**
	 * Internal tree map that uses case-insensitive string keys.
	 */
	private final Map<String,Object> resources=new STreeMap<String,Object>(new Comparator<String>()
	{
		@Override
		public int compare(String o1, String o2)
		{
			if(o1==null)
			{
				if(o2==null)
					return 0;
				return -1;
			}
			else
			if(o2==null)
				return 1;
			return o1.compareToIgnoreCase(o2);
		}
	});

	/**
	 * Internal class that serves the purpose of both identifying
	 * a compressed text resource, and holding the bytes of that compression.
	 * @author Bo Zimmerman
	 *
	 */
	private static class CompressedResource
	{
		public byte[] data;
		
		/**
		 * Constructs a CompressedResource object from the given bytes
		 * @param d
		 */
		public CompressedResource(byte[] d) 
		{ 
			data=d;
		}
	}

	/**
	 * Constructs a new CMLib object for the current thread group.
	 */
	public Resources()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(rscs[c]==null)
			rscs[c]=this;
	}

	/**
	 * Forces the current thread group to share a Resources object with the one at the given
	 * threadcode.  The one at the threadcode should already have been created before
	 * calling.
	 * @param code the threadcode with an existing Resources
	 */
	public static void shareWith(char code)
	{
		if(Thread.currentThread().getThreadGroup().getName().charAt(0)==code)
			initialize();
		else
		if(rscs[code]!=null)
			rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)]=rscs[code];
		else
		{
			initialize();
			rscs[code]=rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];

		}
	}

	/**
	 * Creates and returns a new Resources object for the current calling thread
	 * @return a new Resources object for the current calling thread
	 */
	public static final Resources initialize() 
	{ 
		return new Resources(); 
	}
	
	/**
	 * Returns the Resources instance tied to this particular thread group, or a new one if not yet created.
	 * @return the Resources instance tied to this particular thread group, or a new one if if not yet created.
	 */
	public static final Resources instance()
	{
		final Resources r=r();
		if(r==null)
			return new Resources();
		return r;
	}
	
	/**
	 * Returns the Resources instance tied to the given thread group, or null if not yet created.
	 * @param c the code for the thread group to return (0-255)
	 * @return the Resources instance tied to the given thread group, or null if not yet created.
	 */
	public static final Resources instance(final char c)
	{ 
		return rscs[c];
	}
	
	/**
	 * Returns the Resources instance tied to this particular thread group, or null if not yet created.
	 * @return the Resources instance tied to this particular thread group, or null if not yet created.
	 */
	private static final Resources r()
	{ 
		return rscs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}

	/**
	 * Returns the Resources instance tied to this particular thread group, or null if not yet created.
	 * @return the Resources instance tied to this particular thread group, or null if not yet created.
	 */
	public static final Resources staticInstance()
	{ 
		if(rscs[0]==null)
			rscs[0]=newResources(); 
		return rscs[0];
	}

	/**
	 * Creates and returns a new Resources object for the current calling thread
	 * @return a new Resources object for the current calling thread
	 */
	public static final Resources newResources()
	{ 
		return new Resources();
	}

	/**
	 * Removes all resources for the current calling thread group
	 */
	public static final void clearResources()
	{
		r()._clearResources();
	}
	
	/**
	 * Saves any cached resource properties for the current calling thread group.
	 * Removes all resources for the current calling thread group.
	 */
	public static final void shutdown()
	{
		Resources.savePropResources();
		r()._clearResources();
	}

	/**
	 * Removes the current resources for the current calling thread group.
	 * @param ID the resource ID to remove, case insensitive as always
	 */
	public static final void removeResource(final String ID)
	{ 	
		r()._removeResource(ID);
	}

	/**
	 * Checks the current resources for the current calling thread group for the ID.
	 * @param ID the resource ID to check for
	 * @return true if found, false otherwise
	 */
	public static final boolean isResource(final String ID)
	{ 	
		return r()._isResource(ID);
	}
	
	/**
	 * Does a case-insensitive instring search of all resources for the
	 * current calling thread group and returns an iterator of all FULL keys
	 * that match.
	 * @param srch the instring string to search for
	 * @return an iterator of all matching full keys
	 */
	public static final Iterator<String> findResourceKeys(final String srch)
	{
		return r()._findResourceKeys(srch);
	}
	
	/**
	 * Returns the raw resource object for the given case-insensitive ID, from
	 * the resources for the current calling thread group.
	 * @param ID the resource ID to return
	 * @return the raw object at that resource ID, or null of not found
	 */
	public static final Object getResource(final String ID)
	{
		return r()._getResource(ID);
	}
	
	/**
	 * Adds or replaces a raw resource object at the given case-insensitive ID, into
	 * the resources for the current calling thread group.
	 * @param ID the resource ID to store the given object at
	 * @param obj the object to store at the given ID
	 */
	public static final void submitResource(final String ID, final Object obj)
	{
		r()._submitResource(ID,obj);
	}
	
	/**
	 * Checks the resources for the current calling thread group for a file resource
	 * of the given name.
	 * @param filename the resource filename to check for (/resources/[FILENAME])
	 * @return true if the file exists as a current resource, and false otherwise
	 */
	public static final boolean isFileResource(final String filename)
	{
		return r()._isFileResource(filename);
	}

	/**
	 * Returns the stringbuffer content for the given resource filename, from
	 * the resources for the current calling thread group.  This method
	 * will normalize text data line endings for mud display. 
	 * @param filename the resource filename (/resources/[FILENAME])
	 * @param reportErrors if true, file errors will be logged
	 * @return the StringBuffer of the file at that resource filename, or null of not found
	 */
	public static final StringBuffer getFileResource(final String filename, final boolean reportErrors)
	{
		return r()._getFileResource(filename,reportErrors);
	}

	/**
	 * Returns the stringbuffer content for the given resource filename, from
	 * the resources for the current calling thread group. This method returns
	 * the raw file text, with unnormalized line endings.
	 * @param filename the resource filename (/resources/[FILENAME])
	 * @param reportErrors if true, file errors will be logged
	 * @return the StringBuffer of the file at that resource filename, or null of not found
	 */
	public static final StringBuffer getRawFileResource(final String filename, final boolean reportErrors)
	{
		return r()._getRawFileResource(filename,reportErrors);
	}
	
	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem on behalf of the given user/player, without touching the cache.
	 * Returns false if the user was not permitted to save files at that location.
	 * @param filename the resource filename to save to (/resources/[FILENAME])
	 * @param whom the mob whose permissions to check, or null to always save
	 * @param myRsc the string data to store in the file
	 * @return true if the file was saved, or false if there were permission or other problems
	 */
	public static final boolean saveFileResource(final String filename, final MOB whom, final StringBuffer myRsc)
	{
		return r()._saveFileResource(filename,whom,myRsc);
	}
	
	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem while also updating the internal cache for the resources 
	 * of the calling threads thread group.
	 * Returns false if there was a filesystem error.
	 * @param filename the resource filename to save to (/resources/[FILENAME])
	 * @param obj the string data to store in the file, stringbuffer, byte array, etc
	 * @return true if the file was saved, or false if there were problems
	 */
	public static final boolean updateFileResource(final String filename, final Object obj)
	{
		return r()._updateFileResource(filename,obj);
	}
	
	/**
	 * Opens the given CMFile as a properties type file, ignoring comment lines, and looking
	 * for a property entry that matches [match]=[whatever], removing it if found, and if found,
	 * re-saving the file.
	 * @param F the properties file to potentially modify
	 * @param match the property file entry to remove
	 * @return true if the property was removed, and false if nothing was done
	 */
	public static final boolean findRemoveProperty(final CMFile F, final String match)
	{
		return r()._findRemoveProperty(F,match);
	}

	/**
	 * Scans the given stringbuffer for the first occurrence of an end-of-line and
	 * returns the end of line character(s) encountered.  This could be \n, \r, \n\r, 
	 * or \r\n
	 * @param buf the stringbuffer to scan
	 * @return the end of line market
	 */
	public static final String getEOLineMarker(final StringBuffer buf)
	{
		for(int i=0;i<buf.length()-1;i++)
		{
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
		}
		return "\n\r";
	}

	/**
	 * Scans the given stringbuffer for end of line markers, and adds each line
	 * encountered to a string list, returning that list object.
	 * @param buf the stringbuffer to scan for lines
	 * @return a list of all the lines in the buffer
	 */
	public static final List<String> getFileLineVector(final StringBuffer buf)
	{
		final Vector<String> V=new Vector<String>();
		if(buf==null)
			return V;
		final StringBuffer str=new StringBuffer("");
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
			if((buf.charAt(i)=='\r')||(buf.charAt(i)=='\n'))
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

	/**
	 * Adds resources/ before the given path, and always adds a / at the end.
	 * @param path a path string
	 * @return resources/ before the given path, and always adds a / at the end
	 */
	public static final String buildResourcePath(final String path)
	{
		if((path==null)||(path.length()==0))
			return "resources/";
		return "resources/"+path+"/";
	}

	/**
	 * A multi-list is, in code, a string-key map of string lists.  In a file, it is represented
	 * by a string key on one line, followed by the list entries, followed by a blank line.
	 * Obviously, no blank list entries are permitted.
	 * 
	 * This method will build a stringbuffer from the multi-list object, and then save it to
	 * the given resource filename.  This method does nothing to the cache.
	 * 
	 * @param filename the resource filename to save to 
	 * @param lists the multi-list
	 */
	public static final void updateMultiListFile(String filename, final Map<String, List<String>> lists)
	{
		final StringBuilder str=new StringBuilder("");
		for(final String ml : lists.keySet())
		{
			final List<String> subList=lists.get(ml);
			str.append(ml+"\r\n");
			if(subList!=null)
			{
				for(int v=0;v<subList.size();v++)
				{
					final String listEntry = subList.get(v);
					if((listEntry != null) && (listEntry.length()>0))
					{
						str.append(listEntry).append("\r\n");
					}
				}
			}
			str.append("\r\n");
		}
		String prefix="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			prefix=filename.substring(0,2);
			filename=filename.substring(2);
		}
		new CMFile(prefix+makeFileResourceName(filename),null).saveText(str);
	}

	/**
	 * Returns the string/object map assigned to the given object key.  If
	 * createIfNecc is set, then a missing map will be created and returned.
	 * Otherwise this returns false.  The internal object map is Weak, so no worries about
	 * leaving behind unused garbage.  Just be careful with the returned map, which
	 * is NOT weak.
	 * @see Resources#getPersonalMap(Object, boolean)
	 * @param key the key to look for
	 * @param createIfNecc true to create the missing map
	 * @return the map, or null
	 */
	public static final Map<String,Object> getPersonalMap(final Object key, final boolean createIfNecc)
	{
		final Resources r = r();
		if(!r._isResource("SYSTEM_PERSONAL_MAPS"))
			r._submitResource("SYSTEM_PERSONAL_MAPS", new java.util.WeakHashMap<Object,Map<String,Object>>());
		@SuppressWarnings("unchecked")
		final Map<Object,Map<String,Object>> rsc=(Map<Object,Map<String,Object>>)r()._getResource("SYSTEM_PERSONAL_MAPS");
		if(rsc != null)
		{
			synchronized(rsc)
			{
				final Map<String,Object> map = rsc.get(key);
				if(map != null)
					return map;
				if(createIfNecc)
				{
					final Map<String,Object> myMap = new TreeMap<String,Object>();
					rsc.put(key,myMap);
					return myMap;
				}
			}
		}
		return null;
	}

	/**
	 * Removes and Deletes the string/object map assigned to the given object key. 
	 * @see Resources#getPersonalMap(Object, boolean)
	 * @param key the key to look for
	 */
	public static final void removePersonalMap(final Object key)
	{
		@SuppressWarnings("unchecked")
		final Map<Object,Map<String,Object>> rsc=(Map<Object,Map<String,Object>>)getResource("SYSTEM_PERSONAL_MISC");
		if(rsc != null)
		{
			synchronized(rsc)
			{
				rsc.remove(key);
			}
		}
	}
	
	/**
	 * A multi-list is, in code, a string-key map of string lists.  In a file, it is represented
	 * by a string key on one line, followed by the list entries, followed by a blank line.
	 * Obviously, no blank list entries are permitted.
	 * 
	 * This method Removes the parsed multi-list object from the memory cache for this thread
	 * group.
	 * 
	 * @param filename the filename of the parsed multi-list file
	 * @return true
	 */
	public static final boolean removeMultiLists(final String filename)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		removeResource(key);
		return true;
	}

	/**
	 * A multi-list is, in code, a string-key map of string lists.  In a file, it is represented
	 * by a string key on one line, followed by the list entries, followed by a blank line.
	 * Obviously, no blank list entries are permitted.
	 * 
	 * This method retrieves the parsed multi-list object from the memory cache of this thread
	 * group if found. If not found, it attempts to load it from the given resource filename.
	 * 
	 * @param filename the filename of the parsed multi-list file
	 * @param createIfNot true to create a multilist object if not loaded, false if not
	 * @return the multi-list map object
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, List<String>> getCachedMultiLists(final String filename, boolean createIfNot)
	{
		final String key = "PARSED_MULTI: "+filename.toUpperCase();
		Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null)
		{
			H=Resources.getMultiLists(filename);
			if((H==null) && (createIfNot))
				H=new Hashtable<String,List<String>>();
			if(H!=null)
				Resources.submitResource(key,H);
		}
		return H;
	}

	/**
	 * A multi-list is, in code, a string-key map of string lists.  In a file, it is represented
	 * by a string key on one line, followed by the list entries, followed by a blank line.
	 * Obviously, no blank list entries are permitted.
	 * 
	 * This method re-saves the cached multi-list object in this thread groups resource 
	 * cache to the given resource filename.
	 * 
	 * @param filename the filename of the parsed multi-list file
	 * @return the multi-list map object, full or empty
	 */
	@SuppressWarnings("unchecked")
	public static final boolean updateCachedMultiLists(final String filename)
	{
		final String key;
		if(filename.startsWith("::")||filename.startsWith("//"))
			key = "PARSED_MULTI: "+filename.substring(2).toUpperCase();
		else
			key = "PARSED_MULTI: "+filename.toUpperCase();
		final Map<String,List<String>> H=(Map<String,List<String>>)getResource(key);
		if(H==null)
			return false;
		updateMultiListFile(filename, H);
		return true;
	}

	/**
	 * A multi-list is, in code, a string-key map of string lists.  In a file, it is represented
	 * by a string key on one line, followed by the list entries, followed by a blank line.
	 * Obviously, no blank list entries are permitted.
	 * 
	 * This method retrieves the parsed multi-list object from the given resource filename.
	 * 
	 * @param filename the filename of the parsed multi-list file
	 * @return the multi-list map object, full or empty
	 */
	public static final Map<String, List<String>> getMultiLists(String filename)
	{
		final Hashtable<String,List<String>> oldH=new Hashtable<String,List<String>>();
		List<String> V=new Vector<String>();
		try
		{
			String prefix="";
			if(filename.startsWith("::")||filename.startsWith("//"))
			{
				prefix=filename.substring(0,2);
				filename=filename.substring(2);
			}
			V=getFileLineVector(new CMFile(prefix+"resources/"+filename,null).text());
		}
		catch(final Exception e)
		{
		}
		
		if((V!=null)&&(V.size()>0))
		{
			String journal="";
			List<String> set=new Vector<String>();
			for(int v=0;v<V.size();v++)
			{
				final String s=V.get(v);
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

	/**
	 * Adds resources/ to the beginning of the given filename.  Just a way
	 * to normalize all resource filenames.
	 * @param filename the filename to resource normalize
	 * @return the filename, resource normalized
	 */
	public static final String makeFileResourceName(final String filename)
	{
		if(filename==null)
			return "resources/";
		if(filename.startsWith("resources/")||filename.startsWith("/resources/"))
			return filename;
		if(filename.startsWith("/"))
			return "resources"+filename;
		return "resources/"+filename;
	}

	/**
	 * Turn on or off resource compression.
	 * @param truefalse true to turn on compression, false to turn it off
	 */
	public static final void setCompression(final boolean truefalse)
	{
		compress=truefalse;
	}

	/**
	 * Returns whether resource objects are being compressed
	 * @return true if they are being compressed, false otherwise
	 */
	public static final boolean _compressed()
	{
		return compress;
	}

	/**
	 * Instantly and permanently removes all resources.
	 */
	public final void _clearResources()
	{
		resources.clear();
	}

	/**
	 * Returns an iterator of all resource keys that pass a substring
	 * search of the given srch string.
	 * @param srch the substring srch string
	 * @return an iterator of the resource keys that pass the search
	 */
	public final Iterator<String> _findResourceKeys(final String srch)
	{
		final String lowerSrch=srch.toLowerCase();
		final boolean allOfThem=(lowerSrch.length()==0);
		
		return new FilteredIterator<String>(resources.keySet().iterator(), new Filterer<String>()
		{
			@Override
			public boolean passesFilter(String obj)
			{
				return (allOfThem) || ((obj != null) && (obj.toLowerCase().indexOf(lowerSrch)>=0));
			}
		});
	}

	/**
	 * Returns the resource object with the given case insensitive ID.
	 * If the stringbuffer resource is compressed, it uncompresses it first.
	 * @param ID the key of the object to return 
	 * @return the object found, or null
	 */
	public final Object _getResource(final String ID)
	{
		final Object O = resources.get(ID);
		if(O!=null)
		{
			if(compress && (O instanceof CompressedResource))
				return new StringBuffer(CMLib.encoder().decompressString(((CompressedResource)O).data));
			return O;
		}
		return null;
	}

	/**
	 * Checks the resource object with the given case insensitive ID.
	 * @param ID the key of the object to look for
	 * @return true if found, false otherwise
	 */
	public final boolean _isResource(final String ID)
	{
		return resources.containsKey(ID);
	}

	/**
	 * Prepares an object for storage in resources by trimming any vectors,
	 * and compressing any stringbuffers, if necessary.
	 * @param obj the object to prepare for storage
	 * @return the prepared object
	 */
	@SuppressWarnings("rawtypes")
	public static final Object prepareObject(final Object obj)
	{
		if(obj instanceof Vector)
			((Vector)obj).trimToSize();
		if(obj instanceof DVector)
			((DVector)obj).trimToSize();
		if(!compress)
			return obj;
		if(obj instanceof StringBuffer)
			return CMLib.encoder().compressString(((StringBuffer)obj).toString());
		return obj;
	}

	/**
	 * Adds or updates the given resource object at the given resource id/key.
	 * 
	 * @param ID the key to store the resource as
	 * @param obj the object to store
	 * @return the object as stored.
	 */
	public final Object _submitResource(final String ID, final Object obj)
	{
		if(ID==null)
			Log.errOut("Resources",new Exception("Null ID"));
		final Object prepared=prepareObject(obj);
		if(prepared != obj)
			resources.put(ID,new CompressedResource((byte[])prepared));
		else
			resources.put(ID,prepared);
		return prepared;
	}

	/**
	 * Adds or updates the given resource object at the given resource id/key.
	 * 
	 * @param ID the key to store the resource as
	 * @param obj the object to store
	 * @returnthe object as stored.
	 */
	private final Object _updateResource(final String ID, final Object obj)
	{
		return _submitResource(ID, obj);
	}

	/**
	 * Removes the given resource with the given ID, if found.
	 * 
	 * @param ID the key the resource is stored as
	 */
	public final void _removeResource(final String ID)
	{
		resources.remove(ID);
	}

	/**
	 * Returns true if there is a resource file object stored under the
	 * given filename OR if there exists a file with the given filename 
	 * (and is thus potentially a file resource).
	 * @param filename the filename to look in the cache or filesystem for
	 * @return true if the cache entry or file is found, false otherwise
	 */
	public final boolean _isFileResource(final String filename)
	{
		if(_getResource(filename)!=null)
			return true;
		if(new CMFile(makeFileResourceName(filename),null).exists())
			return true;
		return false;
	}

	/**
	 * Returns the string-like object given as a StringBuffer.
	 * @param o the string, stringbuffer, or stringbuilder to convert to stringbuffer
	 * @return the stringbuffer, or null if it could not be converted.
	 */
	public final StringBuffer _toStringBuffer(final Object o)
	{
		if(o!=null)
		{
			if(o instanceof StringBuffer)
				return (StringBuffer)o;
			else
			if(o instanceof String)
				return new StringBuffer((String)o);
			else
			if(o instanceof StringBuilder)
				return new StringBuffer((StringBuilder)o);
		}
		return null;
	}

	/**
	 * Returns the stringbuffer content for the given resource filename.
	 * Will normalize line endings for mud display.
	 * @param filename the resource filename (/resources/[FILENAME])
	 * @param reportErrors if true, file errors will be logged
	 * @return the StringBuffer of the file at that resource filename, or null of not found
	 */
	public final StringBuffer _getFileResource(final String filename, final boolean reportErrors)
	{
		final Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		final StringBuffer buf=new CMFile(makeFileResourceName(filename),null,reportErrors?CMFile.FLAG_LOGERRORS:0).text();
		if(!CMProps.getBoolVar(CMProps.Bool.FILERESOURCENOCACHE))
			_submitResource(filename,buf);
		return buf;
	}

	/**
	 * Returns the stringbuffer content for the given resource filename.
	 * Will NOT normalize line endings for mud display.
	 * @param filename the resource filename (/resources/[FILENAME])
	 * @param reportErrors if true, file errors will be logged
	 * @return the StringBuffer of the file at that resource filename, or null of not found
	 */
	public final StringBuffer _getRawFileResource(final String filename, final boolean reportErrors)
	{
		final Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		String charSet=CMProps.getVar(CMProps.Str.CHARSETINPUT);
		if((charSet==null)||(charSet.length()==0))
			charSet=Charset.defaultCharset().name();
		StringBuffer buf;
		try
		{
			buf = new StringBuffer(new String(new CMFile(makeFileResourceName(filename),null,reportErrors?CMFile.FLAG_LOGERRORS:0).raw(),charSet));
		}
		catch (UnsupportedEncodingException e)
		{
			Log.errOut(e);
			buf=new StringBuffer("");
		}
		if(!CMProps.getBoolVar(CMProps.Bool.FILERESOURCENOCACHE))
			_submitResource(filename,buf);
		return buf;
	}

	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem while also updating the internal cache for the resources.
	 * Returns false if there was a filesystem error.
	 * @param filename the resource filename to save to (/resources/[FILENAME])
	 * @param obj the string data to store in the file, stringbuffer, byte array, etc
	 * @return true if the file was saved, or false if there were problems
	 */
	public final boolean _updateFileResource(final String filename, final Object obj)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.FILERESOURCENOCACHE))
			_updateResource(CMFile.vfsifyFilename(filename), obj);
		return _saveFileResource(filename,null,_toStringBuffer(obj));
	}

	/**
	 * Saves the given stringbuffer of data to the given resource filename, to 
	 * the filesystem on behalf of the given user/player, without touching the cache.
	 * Returns false if the user was not permitted to save files at that location.
	 * @param filename the resource filename to save to (/resources/[FILENAME])
	 * @param whoM the mob whose permissions to check, or null to always save
	 * @param myRsc the string data to store in the file
	 * @return true if the file was saved, or false if there were permission or other problems
	 */
	public final boolean _saveFileResource(String filename, final MOB whoM, final StringBuffer myRsc)
	{
		final boolean vfsFile=filename.trim().startsWith("::");
		final boolean localFile=filename.trim().startsWith("//");
		filename=CMFile.vfsifyFilename(filename);
		if(!filename.startsWith("resources/"))
			filename="resources/"+filename;
		filename=(vfsFile?"::":localFile?"//":"")+filename;
		return new CMFile(filename,whoM).saveRaw(myRsc);
	}

	/**
	 * Opens the given CMFile as a properties type file, ignoring comment lines, and looking
	 * for a property entry that matches [match]=[whatever], removing it if found, and if found,
	 * re-saving the file.
	 * @param F the properties file to potentially modify
	 * @param match the property file entry to remove
	 * @return true if the property was removed, and false if nothing was done
	 */
	public final boolean _findRemoveProperty(final CMFile F, final String match)
	{
		boolean removed=false;
		final StringBuffer text=F.textUnformatted();
		int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
		while(x>=0)
		{
			if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
			&&(text.substring(x+match.length()).trim().startsWith("=")))
			{
				final int zb1=text.lastIndexOf("\n",x);
				final int zb2=text.lastIndexOf("\r",x);
				int zb=(zb2>zb1)?zb2:zb1;
				if(zb<0)
					zb=0; else zb++;
				final int ze1=text.indexOf("\n",x);
				final int ze2=text.indexOf("\r",x);
				int ze=ze2+1;
				if((ze1>zb)&&(ze1==ze2+1))
					ze=ze1+1;
				else
				if((ze2<0)&&(ze1>0))
					ze=ze1+1;
				if(ze<=0)
					ze=text.length();
				if(!text.substring(zb).trim().startsWith("#"))
				{
					text.delete(zb,ze);
					x=-1;
					removed=true;
				}
			}
			x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
		}
		if(removed)
			F.saveRaw(text);
		return removed;
	}

	/**
	 * The "Resource Properties" is a special VFS file containing normal properties divided into
	 * sections headed by a bracketed [BLOCK].  The filename is ::/coffeemud_properties.ini.  These
	 * properties tend to be internal data maintained by the system for internal use, and resaved
	 * as necessary.
	 * 
	 * This method retrieves all properties in a given section into a string map.  If they are
	 * not cached, this method will cache all properties of all sections.
	 * 
	 * @param section the section in the resource properties to get entries from
	 * @return a map of the entries in the given section
	 */
	public static final Map<String,String> getAllPropResources(String section)
	{
		if(propResources==null)
		{
			synchronized(propResourceSync)
			{
				if(propResources==null)
				{
					final CMFile file=new CMFile("::/coffeemud_properties.ini",null,CMFile.FLAG_FORCEALLOW);
					propResources=new TreeMap<String,Map<String,String>>();
					if(file.exists())
					{
						Map<String,String> currSecMap=new TreeMap<String,String>();
						propResources.put("", currSecMap);
						final List<String> lines=Resources.getFileLineVector(file.text());
						for(String line : lines)
						{
							line=line.trim();
							if(line.startsWith("[")&&(line.endsWith("]")))
							{
								final String currentSection=line.substring(1, line.length()-1).toUpperCase().trim();
								if(propResources.containsKey(currentSection))
									currSecMap=propResources.get(currentSection);
								else
									currSecMap=new TreeMap<String,String>();
								propResources.put(currentSection, currSecMap);
							}
							else
							if(line.startsWith("#"))
								continue;
							else
							{
								final int eqSepIndex=line.indexOf('=');
								if(eqSepIndex<0)
									continue;
								try
								{
									final String key=line.substring(0,eqSepIndex);
									final String value=URLDecoder.decode(line.substring(eqSepIndex+1),"UTF-8");
									currSecMap.put(key.toUpperCase().trim(), value);
								}
								catch(final UnsupportedEncodingException e) 
								{
								}
							}
						}
					}
				}
			}
		}
		if(section.length()>0)
			section=Thread.currentThread().getThreadGroup().getName().charAt(0)+section.toUpperCase().trim();
		synchronized(propResources)
		{
			if(!propResources.containsKey(section))
			{
				propResources.put(section, new TreeMap<String,String>());
			}
			return propResources.get(section);
		}
	}

	/**
	 * The "Resource Properties" is a special VFS file containing normal properties divided into
	 * sections headed by a bracketed [BLOCK].  The filename is ::/coffeemud_properties.ini.  These
	 * properties tend to be internal data maintained by the system for internal use, and resaved
	 * as necessary.
	 * 
	 * This method checks for a given property key in a given section and returns whether it was
	 * found.
	 * 
	 * @param section the section to look in
	 * @param key the property key to look for in that section
	 * @return true if the key was found, false otherwise
	 */
	public static final boolean isPropResource(String section, String key)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			return secMap.containsKey(key);
		}
	}

	/**
	 * The "Resource Properties" is a special VFS file containing normal properties divided into
	 * sections headed by a bracketed [BLOCK].  The filename is ::/coffeemud_properties.ini.  These
	 * properties tend to be internal data maintained by the system for internal use, and resaved
	 * as necessary.
	 * 
	 * This method returns the value of the given property key in the given section, or "" if it 
	 * was not found.
	 * 
	 * @param section the section of the resource properties to look in
	 * @param key the key in the section to look for
	 * @return the value of the property key, or ""
	 */
	public static final String getPropResource(String section, String key)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			if(!secMap.containsKey(key))
				return "";
			return secMap.get(key);
		}
	}

	/**
	 * The "Resource Properties" is a special VFS file containing normal properties divided into
	 * sections headed by a bracketed [BLOCK].  The filename is ::/coffeemud_properties.ini.  These
	 * properties tend to be internal data maintained by the system for internal use, and resaved
	 * as necessary.
	 * 
	 * This method sets or removes the value of the given property key in the given section.  
	 * It does not re-save.  If the value is null or "", the property key is removed.
	 * 
	 * @param section the section of the resource properties to add the key to
	 * @param key the key in the section to set
	 * @param value the new value of the key, or "" to remove
	 */
	public static final void setPropResource(String section, String key, String value)
	{
		final Map<String,String> secMap = getAllPropResources(section);
		key=key.toUpperCase().trim();
		synchronized(secMap)
		{
			if((value==null)||(value.length()==0))
				secMap.remove(key);
			else
				secMap.put(key, value);
		}
	}

	/**
	 * The "Resource Properties" is a special VFS file containing normal properties divided into
	 * sections headed by a bracketed [BLOCK].  The filename is ::/coffeemud_properties.ini.  These
	 * properties tend to be internal data maintained by the system for internal use, and resaved
	 * as necessary.
	 * 
	 * This method re-saves the cached resource properties object back to the filesystem.
	 */
	public static final void savePropResources()
	{
		if(propResources!=null)
		{
			synchronized(propResourceSync)
			{
				if(propResources!=null)
				{
					final StringBuilder str=new StringBuilder("");
					for(final String section : propResources.keySet())
					{
						final Map<String,String> secMap=propResources.get(section);
						if(secMap.size()==0)
							continue;
						if(str.length()>0)
							str.append("\n");
						str.append("["+section+"]\n");
						for(final String key : secMap.keySet())
						{
							try
							{
								final String value=URLEncoder.encode(secMap.get(key),"UTF-8");
								str.append(key).append("=").append(value).append("\n");
							}
							catch (final UnsupportedEncodingException e) 
							{
							}
						}
					}
					final CMFile file=new CMFile("::/coffeemud_properties.ini",null,CMFile.FLAG_FORCEALLOW);
					file.saveText(str);
				}
			}
		}
	}
}
