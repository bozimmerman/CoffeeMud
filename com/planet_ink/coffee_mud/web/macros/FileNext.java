package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class FileNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public static String fixFileName(ExternalHTTPRequests httpReq, String fn)
	{
		if (!fn.startsWith("/"))
			fn = '/' + fn;
		Hashtable dirs=httpReq.getVirtualDirectories();
		if(dirs==null) return "";
		String fn2="";
		String searchPath=fn;
		if(!searchPath.endsWith("/"))
			searchPath=searchPath+"/";
		while ((searchPath.length() > 1) && (!dirs.containsKey(searchPath)))
		{
			fn2 = searchPath.substring(searchPath.lastIndexOf('/',searchPath.lastIndexOf('/')-1)) + fn2;
			searchPath = searchPath.substring(0,searchPath.lastIndexOf('/',
			searchPath.lastIndexOf('/')-1)+1);
		}
		String baseDir=(String)dirs.get(searchPath);
		if(baseDir==null) return "";
		fn2=baseDir + fn2;
		fn2=Util.replaceAll(fn2,"//","/");
		if (File.separatorChar == '/')	return fn2;
		return fn2.replace('/',File.separatorChar);
	}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String path=httpReq.getRequestParameter("PATH");
		if(path==null) path="";
		String last=httpReq.getRequestParameter("FILE");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("FILE");
			return "";
		}
		if(!path.endsWith("/"))
		{
			path+="/";
			httpReq.addRequestParameters("PATH",path);
		}
		path=fixFileName(httpReq,path);
		if(path.length()==0)
			return "[path security error]";
		File directory=new File(path);
		Vector fileList=new Vector();
		if((directory.canRead())&&(directory.isDirectory()))
		{
			String[] list=directory.list();
			for(int l=0;l<list.length;l++)
				if(list[l].trim().length()>0)
					fileList.addElement(list[l]);
			
		}
		String lastID="";
		for(int q=0;q<fileList.size();q++)
		{
			String name=(String)fileList.elementAt(q);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!name.equals(lastID))))
			{
				httpReq.addRequestParameters("FILE",name);
				return "";
			}
			lastID=name;
		}
		httpReq.addRequestParameters("FILE","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		else
			return " @break@";
	}

}
