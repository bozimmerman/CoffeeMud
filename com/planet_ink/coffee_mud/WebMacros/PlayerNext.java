package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class PlayerNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("PLAYER");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeRequestParameter("PLAYER");
			return "";
		}
		String lastID="";
		String sort=httpReq.getRequestParameter("SORTBY");
		if(sort==null) sort="";
		Vector V=(Vector)httpReq.getRequestObjects().get("PLAYERLISTVECTOR"+sort);
		if(V==null)
		{
			V=CMLib.database().getUserList();
			int code=PlayerData.getBasicCode(sort);
			if((sort.length()>0)
			&&(code>=0)
			&&(V.size()>1))
			{
				Vector unV=V;
				V=new Vector();
				while(unV.size()>0)
				{
					MOB M=CMLib.map().getLoadPlayer((String)unV.firstElement());
					if(M==null) return " @break@";
					String loweStr=PlayerData.getBasic(M,code);
					if(loweStr.endsWith(", ")) 
						loweStr=loweStr.substring(0,loweStr.length()-2);
					MOB lowestM=M;
					for(int i=1;i<unV.size();i++)
					{
						M=CMLib.map().getLoadPlayer((String)unV.elementAt(i));
						if(M==null) return " @break@";
						String val=PlayerData.getBasic(M,code);
						if(val.endsWith(", "))
							val=val.substring(0,val.length()-2);
						if((CMath.isNumber(val)&&CMath.isNumber(loweStr)))
						{
							if(CMath.s_long(val)<CMath.s_long(loweStr))
							{
								loweStr=val;
								lowestM=M;
							}
						}
						else
						if(val.compareTo(loweStr)<0)
						{
							loweStr=val;
							lowestM=M;
						}
					}
					unV.removeElement(lowestM.Name());
					V.addElement(lowestM.Name());
				}
			}
			httpReq.getRequestObjects().put("PLAYERLISTVECTOR"+sort,V);
		}
		
		for(int i=0;i<V.size();i++)
		{
			String user=(String)V.elementAt(i);
			if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!user.equals(lastID))))
			{
				httpReq.addRequestParameters("PLAYER",user);
				return "";
			}
			lastID=user;
		}
		httpReq.addRequestParameters("PLAYER","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}

}
