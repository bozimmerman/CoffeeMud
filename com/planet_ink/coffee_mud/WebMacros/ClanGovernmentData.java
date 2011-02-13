package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2011 Bo Zimmerman

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
public class ClanGovernmentData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("GOVERNMENT");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			int lastID=CMath.s_int(last);
			Clan.Government G=CMLib.clans().getStockGovernment(lastID);
			if(G!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("NAME"))
                {
                    String old=httpReq.getRequestParameter("NAME");
                    if(old==null) old=G.name;
                    str.append(old+", ");
                }
                if(parms.containsKey("AUTOROLE"))
                {
                    String old=httpReq.getRequestParameter("AUTOROLE");
                    if(old==null) old=""+G.autoRole;
                    int autoPos=CMath.s_int(old);
                    for(Clan.Position pos : G.positions)
                        str.append("<OPTION VALUE="+pos.roleID+" "+((autoPos==pos.roleID)?"SELECTED":"")+">"+pos.name);
                }
                if(parms.containsKey("ACCEPTPOS"))
                {
                    String old=httpReq.getRequestParameter("ACCEPTPOS");
                    if(old==null) old=""+G.acceptPos;
                    int autoPos=CMath.s_int(old);
                    for(Clan.Position pos : G.positions)
                        str.append("<OPTION VALUE="+pos.roleID+" "+((autoPos==pos.roleID)?"SELECTED":"")+">"+pos.name);
                }
				if(parms.containsKey("SHORTDESC"))
				{
					String old=httpReq.getRequestParameter("SHORTDESC");
					if(old==null) old=G.shortDesc;
					str.append(old+", ");
				}
				if(parms.containsKey("REQUIREDMASK"))
				{
					String old=httpReq.getRequestParameter("REQUIREDMASK");
					if(old==null) old=G.requiredMaskStr;
					str.append(old+", ");
				}
				if(parms.containsKey("ISPUBLIC"))
				{
					String old=httpReq.getRequestParameter("ISPUBLIC");
					if(old==null) old=G.isPublic?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("ISDEFAULT"))
				{
					String old=httpReq.getRequestParameter("ISDEFAULT");
					if(old==null) old=G.isDefault?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("ISFAMILYONLY"))
				{
					String old=httpReq.getRequestParameter("ISFAMILYONLY");
					if(old==null) old=G.isFamilyOnly?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("OVERRIDEMINMEMBERS"))
				{
					String old=httpReq.getRequestParameter("OVERRIDEMINMEMBERS");
					if(old==null) old=G.overrideMinMembers==null?"":G.overrideMinMembers.toString();
					str.append(old+", ");
				}
				if(parms.containsKey("CONQUESTENABLED"))
				{
					String old=httpReq.getRequestParameter("CONQUESTENABLED");
					if(old==null) old=G.conquestEnabled?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("CONQUESTITEMLOYALTY"))
				{
					String old=httpReq.getRequestParameter("CONQUESTITEMLOYALTY");
					if(old==null) old=G.conquestItemLoyalty?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("CONQUESTDEITYBASIS"))
				{
					String old=httpReq.getRequestParameter("CONQUESTDEITYBASIS");
					if(old==null) old=G.conquestDeityBasis?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("MAXVOTEDAYS"))
				{
					String old=httpReq.getRequestParameter("MAXVOTEDAYS");
					if(old==null) old=Integer.toString(G.maxVoteDays);
					str.append(CMath.s_int(old)+", ");
				}
				if(parms.containsKey("VOTEQUORUMPCT"))
				{
					String old=httpReq.getRequestParameter("VOTEQUORUMPCT");
					if(old==null) old=Integer.toString(G.voteQuorumPct);
					str.append(CMath.s_int(old)+", ");
				}
                if(parms.containsKey("AUTOPROMOTEBY"))
                {
                    String old=httpReq.getRequestParameter("AUTOPROMOTEBY");
                    if(old==null) old=""+G.autoPromoteBy.toString();
                    for(Clan.AutoPromoteFlag flag : Clan.AutoPromoteFlag.values())
                        str.append("<OPTION VALUE="+flag.toString()+" "+((old.equals(flag.toString()))?"SELECTED":"")+">"+flag.toString());
                }
                if(parms.containsKey("VOTEFUNCS"))
                {
                    String old=httpReq.getRequestParameter("VOTEFUNCS");
    				Set<String> voteFuncs=new HashSet<String>();
    				if(old==null)
    				{
    					if(G.positions.length>0)
    					{
    						Clan.Position P=G.positions[0];
    						for(Clan.Function func : Clan.Function.values())
    							if(P.functionChart[func.ordinal()]==Clan.Authority.MUST_VOTE_ON)
    								voteFuncs.add(func.toString());
    					}
    				}
    				else
    				{
    					voteFuncs.add(old);
    					int x=1;
    					while(httpReq.getRequestParameter("VOTEFUNCS"+x)!=null)
    					{
    						voteFuncs.add(httpReq.getRequestParameter("VOTEFUNCS"+x));
    						x++;
    					}
    				}
    				for(Clan.Function func : Clan.Function.values())
    				{
						str.append("<OPTION VALUE=\""+func.toString()+"\"");
						if(voteFuncs.contains(func.toString())) str.append(" SELECTED");
						str.append(">"+func.toString());
    				}
                }
				if(parms.containsKey("LONGDESC"))
				{
					String old=httpReq.getRequestParameter("LONGDESC");
					if(old==null) old=G.longDesc;
					str.append(old+", ");
				}

				// ******************************************************************************************
				// do govt positions
				// ******************************************************************************************
				String cmpos=httpReq.getRequestParameter("GOVTPOSITION");
				Clan.Position gPos = null;
				if((cmpos!=null)&&(cmpos.length()>0))
					gPos=G.getPosition(cmpos);
				
				if(parms.containsKey("POSITIONSTART"))
				{
					if(httpReq.getRequestParameter("GOVTPOSITION")!=null)
						httpReq.removeRequestParameter("GOVTPOSITION");
					return "";
				}
                if(parms.containsKey("NUMPOSITIONS"))
                    str.append(""+G.positions.length+", ");
				if(parms.containsKey("POSITIONNEXT"))
				{
					String lastPos="";
					for(int p=0;p<G.positions.length;p++)
					{
						if((cmpos==null)||((cmpos.length()>0)&&(cmpos.equals(lastPos))&&(!(""+p).equals(lastPos))))
						{
							httpReq.addRequestParameters("GOVTPOSITION",(""+p));
							return "";
						}
						lastPos=(""+p);
					}
					httpReq.addRequestParameters("GOVTPOSITION","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					return " @break@";
				}
				if((gPos!=null)&&parms.containsKey("GPOSID_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSID_"+cmpos);
					if(old==null) old=gPos.ID;
					str.append(old+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSNAME_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSNAME_"+cmpos);
					if(old==null) old=gPos.name;
					str.append(old+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSPLURALNAME_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSPLURALNAME_"+cmpos);
					if(old==null) old=gPos.pluralName;
					str.append(old+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSRANK_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSRANK_"+cmpos);
					if(old==null) old=Integer.toString(gPos.rank);
					str.append(CMath.s_int(old)+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSMAX_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSMAX_"+cmpos);
					if(old==null) old=Integer.toString(gPos.max);
					str.append(CMath.s_int(old)+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSINNERMASK_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSINNERMASK_"+cmpos);
					if(old==null) old=gPos.innerMaskStr;
					str.append(old+", ");
				}
				if((gPos!=null)&&parms.containsKey("GPOSISPUBLIC_"+cmpos))
				{
					String old=httpReq.getRequestParameter("GPOSISPUBLIC_"+cmpos);
					if(old==null) old=gPos.isPublic?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
                if((gPos!=null)&&parms.containsKey("GPOSPOWER_"+cmpos+"_"))
                {
                    String old=httpReq.getRequestParameter("GPOSPOWER_"+cmpos+"_");
    				Set<String> powerFuncs=new HashSet<String>();
    				if(old==null)
    				{
						for(Clan.Function func : Clan.Function.values())
							if(gPos.functionChart[func.ordinal()]==Clan.Authority.CAN_DO)
								powerFuncs.add(func.toString());
    				}
    				else
    				{
    					powerFuncs.add(old);
    					int x=1;
    					while(httpReq.getRequestParameter("GPOSPOWER_"+cmpos+"_"+x)!=null)
    					{
    						powerFuncs.add(httpReq.getRequestParameter("GPOSPOWER_"+cmpos+"_"+x));
    						x++;
    					}
    				}
    				for(Clan.Function func : Clan.Function.values())
    				{
						str.append("<OPTION VALUE=\""+func.toString()+"\"");
						if(powerFuncs.contains(func.toString())) str.append(" SELECTED");
						str.append(">"+func.toString());
    				}
                }
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
