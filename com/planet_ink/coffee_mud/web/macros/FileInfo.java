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
public class FileInfo extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String path=httpReq.getRequestParameter("PATH");
		if(path==null) path="";
		String file=httpReq.getRequestParameter("FILE");
		if(file==null) file="";
		if(path.indexOf("..")>=0)
			return "[path security error]";
		if(file.indexOf("..")>=0)
			return "[file security error]";
		if(file.indexOf(File.separatorChar)>=0)
			return "[file security error]";
		if(file.indexOf("/")>=0)
			return "[file security error]";
		if(!path.endsWith("/"))
		{
			path+="/";
			httpReq.addRequestParameters("PATH",path);
		}
		String last=FileNext.fixFileName(httpReq,path);
		if(last.length()==0)
			return "[path security error]";
		last+=file;
		if(last.length()>0)
		{
			try
			{
				File F=new File(last);
				if(parms.containsKey("ISDIRECTORY"))
					return ""+F.isDirectory();
				if(parms.containsKey("ISFILE"))
					return ""+F.isFile();
				if(parms.containsKey("NAME"))
					return ""+F.getName();
				if(parms.containsKey("DATA"))
				{
					StringBuffer buf=new StringBuffer("");
					FileReader FR=new FileReader(F);
					BufferedReader reader=new BufferedReader(FR);
					String line="";
					while((line!=null)&&(reader.ready()))
					{
						line=reader.readLine();
						if(line!=null)
							buf.append(line+"\n");
					}
					reader.close();
					FR.close();
					return buf.toString();
				}
			}
			catch(Exception e)
			{
				return "[error]";
			}
		}
		return "";
	}
}
