package com.planet_ink.coffee_mud.web;

import java.io.*;
import java.util.*;

import com.planet_ink.coffee_mud.utils.*;

/*
 * grabs a file, given a base directory and a relative path
 *  base directory is assumed NOT to end in a '/';
 *  if relative path doesn't begin with one, one is prefixed
 *
 * performs machine-specific translation on file name to correct
 *  separator characters
 * checks that the file's canonical path is under a directory served
 *  by this webserver
 *  support for virtual directory remappings
 */
public class FileGrabber
{
	private static char sep=File.separatorChar;
	private static String sepStr = File.separator;
	
//	private Vector permittedDirectories = new Vector();
	private Hashtable virtualDirectories = new Hashtable();
	
//	private String baseDir = null;
	
	private HTTPserver webServer = null;
	
	public FileGrabber(HTTPserver a_webServer)
	{
		webServer = a_webServer;
	}

	// static functions
	
	public static String fixFileName(String fn)
	{
		if (sep == '/')
			return fn;
		return fn.replace('/',sep);
	}

	public static String fixDirName(String fn)
	{
		fn = fixFileName(fn);
		if (fn.endsWith(sepStr))
		{
			if (fn.length() > 1)
				fn = fn.substring(0,fn.length()-2);
			else
				fn = "";
		}
		return fn;
	}
/*
	public boolean doesFileExist()
	{
	
	}
*/
	//assumes pathName has been fixed!
	// actPath may be relative to coffeemud root; the canonical
	// form is stored
	private boolean addPermittedDirectory(String virtPath, String actPath)
	{
		if (!virtPath.endsWith("/"))
			virtPath += "/";
	
	
		String cn;
		try
		{
			File f = new File(actPath);
			
			if (f == null)
				throw new IOException("could not open " + actPath);
				
			if (!f.exists())
				throw new IOException("path " + actPath + " does not exist!");

			if (!f.isDirectory())
				throw new IOException("path " + actPath + " is not a directory!");

			cn = f.getCanonicalPath();
		
			if (!cn.endsWith(sepStr))
				cn += sep;
		}
		catch(Exception e)
		{
			Log.errOut(webServer.getName(), "ERROR: addPermittedDirectory() - " + e.getMessage() );
			return false;
		}	

		
//		permittedDirectories.addElement( cn.toLowerCase() );
		virtualDirectories.put( virtPath, cn );
//Log.errOut(webServer.getName(), "dbg: added " + virtPath +" , "+actPath+"=="+cn);

		return true;
	}
	


	public boolean setBaseDirectory(String basePath)
	{
//		baseDir = fixDirName(pathName);
		return addPermittedDirectory("/",fixDirName(basePath));
	}
	
	public boolean addVirtualDirectory(String virtPath, String actPath)
	{
		return addPermittedDirectory(virtPath,fixDirName(actPath));
	}
	
	public GrabbedFile grabFile(String fn)
	{
		GrabbedFile gf = new GrabbedFile();
		if (!fn.startsWith("/"))
			fn = '/' + fn;

		
		String baseDir = "";
		String fn2="";
		try
		{
			try
			{

				String searchPath = fn;
				if (!searchPath.endsWith("/"))
					searchPath += '/';
			
//Log.errOut(webServer.getName(), "dbgI - " + searchPath);
				while (searchPath.length() > 1 && !virtualDirectories.containsKey(searchPath))
				{
//					searchPath = searchPath.substring(0,searchPath.lastIndexOf('/')+1);


					fn2 = searchPath.substring(searchPath.lastIndexOf('/',searchPath.lastIndexOf('/')-1)) + fn2;
//Log.errOut(webServer.getName(), "dbgFN2 - " + fn2);

					searchPath = searchPath.substring(0,searchPath.lastIndexOf('/',
						searchPath.lastIndexOf('/')-1)+1);
//Log.errOut(webServer.getName(), "dbgL - " + searchPath);
				}
				baseDir = (String)virtualDirectories.get(searchPath);
				if (baseDir == null)
				{
					Log.errOut(webServer.getName(), "No path? '" + fn +"'");
					throw new IOException("No path.");
				}
//Log.errOut(webServer.getName(), baseDir);
			}
			catch (Exception e)
			{
				gf.file = null;
				gf.state = GrabbedFile.INTERNAL_ERROR;
				return gf;
			}


			String filename = fixFileName(fn2);

			try
			{
				String fullFilename = new String(baseDir + filename);
//Log.errOut(webServer.getName(), "qqq: " + filename + "  " + fullFilename);
				
//				if (!filename.startsWith(sepStr))
//					fullFilenameBuf.append(sepStr);
//				fullFilenameBuf.append(filename);
	
//				gf.file = new File(fullFilenameBuf.toString());
				gf.file = new File(fullFilename);
			}
			catch (Exception e)
			{
				gf.file = null;
				gf.state = GrabbedFile.BAD_FILENAME;
				return gf;
			}
			
			if (gf.file == null)	//?
			{
//pointless - it's null already!				gf.file = null;
				gf.state = GrabbedFile.NOT_FOUND;
				return gf;
			}
			


			String canonName = gf.file.getCanonicalPath();
///			boolean foundPath = false;

			if (gf.file.isDirectory())
			{
				if (!canonName.endsWith(sepStr))
					canonName += sep;
			}
/*
			for (int i=0; i<permittedDirectories.size(); ++i)
			{
//				if ( canonName.toLowerCase().startsWith((String)permittedDirectories.elementAt(i)) )
				if ( canonName.startsWith((String)permittedDirectories.elementAt(i)) )
				{
					foundPath = true;
					break;
				}
			}
			if (!foundPath)
*/
			if (!canonName.startsWith(baseDir))
			{
				Log.errOut(webServer.getName(), "ALERT: attempt to access '" + fn +"'");
				gf.file = null;
				gf.state = GrabbedFile.SECURITY_VIOLATION;
				return gf;
			}
			
			
			if (!gf.file.exists())
			{
				gf.file = null;
				gf.state = GrabbedFile.NOT_FOUND;
				return gf;
			}
	
			
			
			if (gf.file.isDirectory())
				gf.state = GrabbedFile.IS_DIRECTORY;
			else
				gf.state = GrabbedFile.OK;
			
		}
		catch (Exception e)
		{
			gf.file = null;
			gf.state = GrabbedFile.INTERNAL_ERROR;
		}
			
		return gf;
	}
}
