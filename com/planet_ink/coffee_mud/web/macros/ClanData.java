package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
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
public class ClanData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include PREMISE, RECALL, DONATION, TAX, EXP, STATUS,
	// ACCEPTANCE, TYPE, POINTS, CLANIDRELATIONS, MEMBERSTART, MEMBERNEXT,
	// MEMBERNAME, MEMBERPOS

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("CLAN");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Clan C=Clans.getClan(last);
			if(C!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("PREMISE"))
					str.append(C.getPremise()+", ");
				if(parms.containsKey("RECALL"))
				{
					Room R=CMMap.getRoom(C.getRecall());
					if(R!=null)	str.append(R.displayText()+", ");
					else str.append("None, ");
				}
				if(parms.containsKey("DONATION"))
				{
					Room R=CMMap.getRoom(C.getDonation());
					if(R!=null)	str.append(R.displayText()+", ");
					else str.append("None, ");
				}
				if(parms.containsKey("TAX"))
					str.append(""+((int)Math.round(C.getTaxes()*100.0))+"%, ");
				if(parms.containsKey("EXP"))
					str.append(""+C.getExp()+", ");
				if(parms.containsKey("STATUS"))
					str.append(Util.capitalize(Clans.CLANSTATUS_DESC[C.getStatus()].toLowerCase())+", ");
				if(parms.containsKey("ACCEPTANCE"))
					str.append(MUDZapper.zapperDesc(C.getAcceptanceSettings())+", ");
				if(parms.containsKey("TYPE"))
					str.append(C.typeName()+", ");
				if(parms.containsKey("CLANIDRELATIONS"))
					str.append(Util.capitalize(Clan.REL_DESCS[C.getClanRelations(httpReq.getRequestParameter("CLANID"))].toLowerCase())+", ");
				if(parms.containsKey("POINTS"))
					str.append(""+C.calculateMapPoints()+", ");
				if(parms.containsKey("MEMBERSTART"))
				{
					if(httpReq.getRequestParameter("CLANMEMBER")!=null)
						httpReq.removeRequestParameter("CLANMEMBER");
					return "";
				}
				if(parms.containsKey("MEMBERNEXT"))
				{
					String member=httpReq.getRequestParameter("CLANMEMBER");
					String lastID="";
					String posFilter=httpReq.getRequestParameter("CLANPOSFILTER");
					if(posFilter==null) posFilter="";
					DVector members=C.getMemberList((posFilter.length()>0)?Util.s_int(posFilter):-1);
					for(int x=0;x<members.size();x++)
					{
						String name=(String)members.elementAt(x,1);
						if((member==null)||((member.length()>0)&&(member.equals(lastID))&&(!name.equals(lastID))))
						{
							httpReq.addRequestParameters("CLANMEMBER",name);
							return "";
						}
						lastID=name;
					}
					httpReq.addRequestParameters("CLANMEMBER","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					else
						return " @break@";
				}
				if(parms.containsKey("MEMBERNAME"))
				{
					String member=httpReq.getRequestParameter("CLANMEMBER");
					str.append(member+", ");
				}
				if(parms.containsKey("MEMBERPOS"))
				{
					String member=httpReq.getRequestParameter("CLANMEMBER");
					if(member!=null)
					{
						DVector members=C.getMemberList();
						for(int x=0;x<members.size();x++)
						{
							String name=(String)members.elementAt(x,1);
							if(name.equals(member))
							{
								Integer I=(Integer)members.elementAt(x,2);
								str.append(Util.capitalize(Clan.ROL_DESCS[C.getGovernment()][I.intValue()].toLowerCase())+", ");
								break;
							}
						}
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
