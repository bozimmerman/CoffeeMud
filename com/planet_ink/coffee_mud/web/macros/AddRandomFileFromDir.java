package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.web.*;
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
public class AddRandomFileFromDir extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms==null)||(parms.size()==0)) return "";
		StringBuffer buf=new StringBuffer("");
		Vector fileList=new Vector();
		boolean LINKONLY=false;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
			if(((String)e.nextElement()).equalsIgnoreCase("LINKONLY"))
				LINKONLY=true;
		for(Enumeration e=parms.elements();e.hasMoreElements();)
		{
			String filePath=(String)e.nextElement();
			if(filePath.equalsIgnoreCase("LINKONLY")) continue;
			File directory=httpReq.grabFile(filePath);
			if((!filePath.endsWith(""+File.separatorChar))&&(!filePath.endsWith("/")))
				filePath+="/";
			if((directory!=null)&&(directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
					fileList.addElement(filePath+list[l]);
			}
			else
				Log.sysOut("AddRFDir","Directory error: "+filePath);
		}
		if(fileList.size()==0) 
			return buf.toString();
		if(LINKONLY)
			buf.append((String)fileList.elementAt(Dice.roll(1,fileList.size(),-1)));
		else
			buf.append(httpReq.getPageContent((String)fileList.elementAt(Dice.roll(1,fileList.size(),-1))));
		return buf.toString();
	}
}
