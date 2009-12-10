package com.planet_ink.coffee_mud.core.http;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.io.File;
import java.io.IOException;

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
@SuppressWarnings("unchecked")
public class FileGrabber
{
	private static char sep=java.io.File.separatorChar;
	private static String sepStr = java.io.File.separator;

//	protected Vector permittedDirectories = new Vector();
	private Hashtable virtualDirectories = new Hashtable();

//	protected String baseDir = null;

	private HTTPserver webServer = null;

	public FileGrabber(HTTPserver a_webServer)
	{
		webServer = a_webServer;
	}

	public static String fixDirName(String fn)
	{
		fn = fn.replace(File.separatorChar,'/');
		if (fn.endsWith(sepStr))
		{
			if (fn.length() > 1)
				fn = fn.substring(0,fn.length()-2);
			else
				fn = "";
		}
		return fn;
	}

	public Hashtable getVirtualDirectories()
	{
		return virtualDirectories;
	}
	//assumes pathName has been fixed!
	// actPath may be relative to coffeemud root; the canonical
	// form is stored
	protected boolean addPermittedDirectory(String virtPath, String actPath)
	{
        virtPath=virtPath.replace(File.separatorChar,'/');
		if (!virtPath.endsWith("/")) virtPath += "/";

		String cn;
		try
		{
			CMFile f = new CMFile(actPath,null,false);

			if (!f.exists())
				throw new IOException("path '" + f.getVFSPathAndName() + "' does not exist!");

			if (!f.isDirectory())
				throw new IOException("path '" + f.getVFSPathAndName() + "' is not a directory!");

			cn = CMFile.vfsifyFilename(f.getCanonicalPath());

			if (!cn.endsWith(sepStr))
				cn += sep;
		}
		catch(Exception e)
		{
			Log.errOut(webServer.getName(),e);
			return false;
		}

		cn=cn.replace(File.separatorChar,'/');
		virtualDirectories.put( virtPath, cn );
		return true;
	}



	public boolean setBaseDirectory(String basePath)
	{
		return addPermittedDirectory("/",fixDirName(basePath));
	}

	public boolean addVirtualDirectory(String virtPath, String actPath)
	{
		return addPermittedDirectory(virtPath,fixDirName(actPath));
	}

	public GrabbedFile grabFile(String fn)
	{
		GrabbedFile gf = new GrabbedFile();
        fn=fn.replace(File.separatorChar,'/');
		if (!fn.startsWith("/")) fn = '/' + fn;

		String baseDir = "";
		String fn2="";
		try
		{
			try
			{

				String searchPath = fn;
                String ssp=null;
                int x=0;
				if (!searchPath.endsWith("/")) searchPath += '/';
				while (searchPath.length() > 1 && !virtualDirectories.containsKey(searchPath))
				{
                    x=searchPath.lastIndexOf('/',searchPath.lastIndexOf('/')-1);
                    ssp=searchPath.substring(x);
                    if(fn2.startsWith("/")&&(ssp.endsWith("/")))
    					fn2 = searchPath.substring(x) + fn2.substring(1);
                    else
                        fn2 = searchPath.substring(x) + fn2;
					searchPath = searchPath.substring(0,x+1);
				}
				baseDir = (String)virtualDirectories.get(searchPath);
				if (baseDir == null)
				{
					Log.errOut(webServer.getName(), "No path? '" + fn +"'");
					throw new IOException("No path.");
				}
			}
			catch (Exception e)
			{
				gf.file = null;
				gf.state = GrabbedFile.STATE_INTERNAL_ERROR;
				return gf;
			}


			String filename = fn2.replace(File.separatorChar,'/');

			try
			{
                String fullFilename=null;
                if(filename.startsWith("/")&&baseDir.endsWith("/"))
                    fullFilename = ( baseDir.substring( 0, baseDir.length() - 1 ) + filename );
                else
                    fullFilename=baseDir+filename;
                if(fullFilename.endsWith("/")) fullFilename=fullFilename.substring(0,fullFilename.length()-1);
				gf.file = new CMFile(fullFilename,null,true);
			}
			catch (Exception e)
			{
				gf.file = null;
				gf.state = GrabbedFile.STATE_BAD_FILENAME;
				return gf;
			}

			if (gf.file == null)	//?
			{
				gf.state = GrabbedFile.STATE_NOT_FOUND;
				return gf;
			}



			String canonName = baseDir+CMFile.vfsifyFilename(gf.file.getCanonicalPath());

			if (gf.file.isDirectory())
			{
				if (!canonName.endsWith(sepStr))
					canonName += sep;
			}
			if (!canonName.startsWith(baseDir))
			{
				Log.errOut(webServer.getName(), "ALERT: attempt to access '" + fn +"'");
				gf.file = null;
				gf.state = GrabbedFile.STATE_SECURITY_VIOLATION;
				return gf;
			}
			if (!gf.file.exists())
			{
				gf.file = null;
				gf.state = GrabbedFile.STATE_NOT_FOUND;
				return gf;
			}



			if (gf.file.isDirectory())
				gf.state = GrabbedFile.STATE_IS_DIRECTORY;
			else
				gf.state = GrabbedFile.STATE_OK;

		}
		catch (Exception e)
		{
			gf.file = null;
			gf.state = GrabbedFile.STATE_INTERNAL_ERROR;
		}

		return gf;
	}
}
