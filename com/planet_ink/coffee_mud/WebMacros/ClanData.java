package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ClanData extends StdWebMacro
{
	public String name() { return "ClanData"; }

	// valid parms include PREMISE, RECALL, DONATION, TAX, EXP, STATUS,
	// ACCEPTANCE, TYPE, POINTS, CLANIDRELATIONS, MEMBERSTART, MEMBERNEXT,
	// MEMBERNAME, MEMBERPOS

	public static StringBuffer members(Clan C, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("MEMBERSLIST"))
		{
			Vector themembers=new Vector();
			Vector theroles=new Vector();
			if(httpReq.isUrlParameter("MEMB1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("MEMB"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
					{
						MOB M=CMLib.players().getLoadPlayer(behav);
						if(M!=null)
						{
							themembers.addElement(behav);
							String role=httpReq.getUrlParameter("ROLE"+num);
							if(role!=null)
								theroles.addElement(Integer.valueOf(CMath.s_int(role)));
							else
								theroles.addElement(Integer.valueOf(C.getGovernment().getAcceptPos()));
						}
					}
					num++;
					behav=httpReq.getUrlParameter("MEMB"+num);
				}
			}
			else
			{
				List<MemberRecord> members=C.getMemberList();
				for(MemberRecord member : members)
				{
					themembers.addElement(member.name);
					theroles.addElement(Integer.valueOf(member.role));
				}
			}
			str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<themembers.size();i++)
			{
				String themember=(String)themembers.elementAt(i);
				Integer role=(Integer)theroles.elementAt(i);
				str.append("<TR><TD WIDTH=35%>");
				str.append("<SELECT ONCHANGE=\"EditMember(this);\" NAME=MEMB"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				str.append("<OPTION VALUE=\""+themember+"\" SELECTED>"+themember);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=65% COLSPAN=2>");
				str.append("<SELECT NAME=ROLE"+(i+1)+">");
				ClanGovernment govt=C.getGovernment();
				for(ClanPosition pos : govt.getPositions())
				{
					str.append("<OPTION VALUE=\""+pos.getRoleID()+"\"");
					if(pos.getRoleID()==role.intValue())
						str.append(" SELECTED");
					str.append(">"+pos.getName());
				}
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("</TR>");
			}
			str.append("<TR><TD WIDTH=35%>");
			str.append("<SELECT ONCHANGE=\"AddMember(this);\" NAME=MEMB"+(themembers.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Member");
			List<String> V=CMLib.database().getUserList();
			for(String mem : V)
				if(!themembers.contains(mem))
					str.append("<OPTION VALUE=\""+mem+"\">"+mem);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=65% COLSPAN=2>");
			str.append("<SELECT NAME=ROLE"+(themembers.size()+1)+">");
			ClanGovernment govt=C.getGovernment();
			for(ClanPosition pos : govt.getPositions())
			{
				str.append("<OPTION VALUE=\""+pos.getRoleID()+"\"");
				str.append(">"+pos.getName());
			}
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("</TR>");
			str.append("</TABLE>, ");
		}
		return str;
	}

	public static StringBuffer relations(Clan C, HTTPRequest httpReq, java.util.Map<String,String> parms, int borderSize)
	{
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("RELATIONS"))
		{
			Vector therelations=new Vector();
			Clan CC=null;
			if(httpReq.isUrlParameter("RELATION1"))
			{
				int num=1;
				String behav=httpReq.getUrlParameter("RELATION"+num);
				while(behav!=null)
				{
					if(behav.length()>0)
						therelations.addElement(Integer.valueOf(CMath.s_int(behav)));
					num++;
					behav=httpReq.getUrlParameter("RELATION"+num);
				}
			}
			else
			{
				for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
				{
					CC=(Clan)e.nextElement();
					if(CC==C) continue;
					therelations.addElement(Integer.valueOf(C.getClanRelations(CC.clanID())));
				}
			}
			str.append("<TABLE WIDTH=100% BORDER="+borderSize+" CELLSPACING=0 CELLPADDING=0>");
			int relat=-1;
			for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
			{
				CC=(Clan)e.nextElement();
				if(CC==C) continue;
				relat++;
				Integer relation=(Integer)therelations.elementAt(relat);
				str.append("<TR><TD WIDTH=35%><FONT COLOR=YELLOW><B>"+CC.getName()+"</B></FONT>");
				str.append("<TD WIDTH=65% COLSPAN=2>");
				str.append("<SELECT NAME=RELATION"+(relat+1)+">");
				for(int r=0;r<Clan.REL_DESCS.length;r++)
				{
					str.append("<OPTION VALUE=\""+r+"\"");
					if(r==relation.intValue())
						str.append(" SELECTED");
					str.append(">"+CMStrings.capitalizeAndLower(Clan.REL_DESCS[r]));
				}
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("</TR>");
			}
			str.append("</TABLE>, ");
		}
		return str;
	}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("CLAN");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Clan C=CMLib.clans().getClan(last);
			if(C!=null)
			{
				final boolean webify=parms.containsKey("WEBCOLOR");
				final boolean decolor=parms.containsKey("NOCOLOR");
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("PREMISE"))
				{
					String old=httpReq.getUrlParameter("PREMISE");
					if(old==null) old=C.getPremise();
					str.append(old+", ");
				}
				if(parms.containsKey("RECALLID"))
				{
					String old=httpReq.getUrlParameter("RECALLID");
					if(old==null) old=C.getRecall();
					str.append(old+", ");
				}
				if(parms.containsKey("RECALL"))
				{
					Room R=CMLib.map().getRoom(C.getRecall());
					if(R!=null)	str.append(R.displayText()+", ");
					else str.append("None, ");
				}
				if(parms.containsKey("MORGUEID"))
				{
					String old=httpReq.getUrlParameter("MORGUEID");
					if(old==null) old=C.getMorgue();
					str.append(old+", ");
				}
				if(parms.containsKey("MORGUE"))
				{
					Room R=CMLib.map().getRoom(C.getMorgue());
					if(R!=null)	str.append(R.displayText()+", ");
					else str.append("None, ");
				}
				if(parms.containsKey("AUTOPOSITION"))
				{
					int pos=C.getAutoPosition();
					str.append(C.getRoleName(pos,true,false)+", ");
				}
				if(parms.containsKey("CATEGORY"))
				{
					String old=httpReq.getUrlParameter("CATEGORY");
					if(old==null) old=C.getCategory();
					str.append(old);
				}
				if(parms.containsKey("MINMEMBERS"))
				{
					String old=httpReq.getUrlParameter("MINMEMBERS");
					if(old==null) old=""+C.getMinClanMembers();
					str.append(old);
				}
				if(parms.containsKey("ISRIVALROUS"))
				{
					String old=httpReq.getUrlParameter("ISRIVALROUS");
					if(old==null) old=C.isRivalrous()?"on":"";
					str.append(old.equalsIgnoreCase("on")?"checked, ":"");
				}
				if(parms.containsKey("AUTOPOSITIONID"))
				{
					String old=httpReq.getUrlParameter("AUTOPOSITIONID");
					if(old==null) old=""+C.getAutoPosition();
					int autoPos=CMath.s_int(old);
					ClanGovernment govt=C.getGovernment();
					for(ClanPosition pos : govt.getPositions())
						str.append("<OPTION VALUE="+pos.getRoleID()+" "+((autoPos==pos.getRoleID())?"SELECTED":"")+">"+pos.getName());
				}
				if(parms.containsKey("TROPHIES"))
				{
					if(C.getTrophies()==0)
						str.append("None");
					else
					{
						for(Trophy t : Trophy.values())
							if(CMath.bset(C.getTrophies(),t.flagNum()))
								str.append(t.description+", ");
					}
				}
				if(parms.containsKey("TROPHIESHORT"))
				{
					if(C.getTrophies()==0)
						str.append("None");
					else
					{
						for(Trophy t : Trophy.values())
							if(CMath.bset(C.getTrophies(),t.flagNum()))
								str.append(t.codeString+", ");
					}
				}
				if(parms.containsKey("DONATIONID"))
				{
					String old=httpReq.getUrlParameter("DONATIONID");
					if(old==null) old=C.getDonation();
					str.append(old+", ");
				}
				if(parms.containsKey("DONATION"))
				{
					Room R=CMLib.map().getRoom(C.getDonation());
					if(R!=null)	str.append(R.displayText()+", ");
					else str.append("None, ");
				}
				if(parms.containsKey("TAX"))
				{
					String old=httpReq.getUrlParameter("TAX");
					if(old==null) 
						old=((int)Math.round(C.getTaxes()*100.0))+"%";
					else
						old=((int)Math.round(CMath.s_pct(old)*100.0))+"%";
					str.append(old+", ");
				}
				if(parms.containsKey("CCLASSID"))
				{
					String old=httpReq.getUrlParameter("CCLASSID");
					if(old==null) old=C.getClanClass();
					str.append("<OPTION VALUE=\"\" "+((old.length()==0)?"SELECTED":"")+">None");
					CharClass CC=null;
					for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
					{
						CC=(CharClass)e.nextElement();
						str.append("<OPTION VALUE=\""+CC.ID()+"\" "+((old.equalsIgnoreCase(CC.ID()))?"SELECTED":"")+">"+CC.name());
					}
				}
				if(parms.containsKey("CCLASS"))
				{
					CharClass CC=CMClass.getCharClass(C.getClanClass());
					if(CC==null)CC=CMClass.findCharClass(C.getClanClass());
					if(CC!=null) str.append(CC.name()+", "); else str.append("");
				}
				if(parms.containsKey("EXP"))
				{
					String old=httpReq.getUrlParameter("EXP");
					if(old==null) old=C.getExp()+"";
					str.append(old+", ");
				}
				if(parms.containsKey("LEVEL"))
				{
					String old=httpReq.getUrlParameter("LEVEL");
					if(old==null) old=C.getClanLevel()+"";
					str.append(old+", ");
				}
				if(parms.containsKey("STATUS"))
					str.append(CMStrings.capitalizeAndLower(Clan.CLANSTATUS_DESC[C.getStatus()].toLowerCase())+", ");
				if(parms.containsKey("STATUSID"))
				{
					String old=httpReq.getUrlParameter("STATUSID");
					if(old==null) old=C.getStatus()+"";
					for(int i=0;i<Clan.CLANSTATUS_DESC.length;i++)
						str.append("<OPTION VALUE="+i+" "+((old.equals(""+i))?"SELECTED":"")+">"+CMStrings.capitalizeAndLower(Clan.CLANSTATUS_DESC[i]));
				}
				if(parms.containsKey("ACCEPTANCE"))
					str.append(CMLib.masking().maskDesc(C.getAcceptanceSettings())+", ");
				if(parms.containsKey("ACCEPTANCEID"))
				{
					String old=httpReq.getUrlParameter("ACCEPTANCEID");
					if(old==null) old=C.getAcceptanceSettings()+"";
					str.append(old+", ");
				}
				if(parms.containsKey("TYPE"))
					str.append(C.getGovernmentName()+", ");
				if(parms.containsKey("TYPEID"))
				{
					String old=httpReq.getUrlParameter("TYPEID");
					if(old==null) old=C.getGovernmentID()+"";
					ClanGovernment[] gvts=CMLib.clans().getStockGovernments();
					for(ClanGovernment gvt : gvts)
						str.append("<OPTION VALUE="+gvt.getID()+" "+((old.equals(""+gvt.getID()))?"SELECTED":"")+">"+CMStrings.capitalizeAndLower(gvt.getName()));
				}
				if(parms.containsKey("CLANIDRELATIONS"))
					str.append(CMStrings.capitalizeAndLower(Clan.REL_DESCS[C.getClanRelations(httpReq.getUrlParameter("CLANID"))].toLowerCase())+", ");
				if(parms.containsKey("POINTS"))
					str.append(""+C.calculateMapPoints()+", ");
				if(parms.containsKey("MEMBERSTART"))
				{
					if(httpReq.getUrlParameter("CLANMEMBER")!=null)
						httpReq.removeUrlParameter("CLANMEMBER");
					return "";
				}
				if(parms.containsKey("NUMMEMBERS"))
					str.append(""+C.getMemberList().size()+", ");
				if(parms.containsKey("MEMBERNEXT"))
				{
					final String cmember=httpReq.getUrlParameter("CLANMEMBER");
					String lastID="";
					String posFilter=httpReq.getUrlParameter("CLANFUNCFILTER");
					if(posFilter==null) posFilter=parms.get("CLANFUNCFILTER");
					if(posFilter==null) posFilter="";
					final Clan.Function reqFunction = (Clan.Function)CMath.s_valueOf(Clan.Function.values(), posFilter);
					final List<MemberRecord> members=C.getMemberList();
					for(MemberRecord member : members)
					{
						final String name=member.name;
						if((reqFunction!=null)&&(C.getAuthority(member.role,reqFunction)==Clan.Authority.CAN_NOT_DO))
							continue;
						if((cmember==null)
						||((cmember.length()>0)&&(cmember.equals(lastID))&&(!name.equals(lastID))))
						{
							httpReq.addFakeUrlParameter("CLANMEMBER",name);
							return "";
						}
						lastID=name;
					}
					httpReq.addFakeUrlParameter("CLANMEMBER","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					return " @break@";
				}
				if(parms.containsKey("MEMBERNAME"))
				{
					String member=httpReq.getUrlParameter("CLANMEMBER");
					str.append(member+", ");
				}
				if(parms.containsKey("MEMBERPOS"))
				{
					String cmember=httpReq.getUrlParameter("CLANMEMBER");
					if(cmember!=null)
					{
						List<MemberRecord> members=C.getMemberList();
						for(MemberRecord member : members)
						{
							String name=member.name;
							if(name.equals(cmember))
							{
								int i = member.role;
								str.append(CMStrings.capitalizeAndLower(C.getRoleName(i,true,false))+", ");
								break;
							}
						}
					}
				}
				if(parms.containsKey("OTHERCLANSTART"))
				{
					if(httpReq.getUrlParameter("CLANID")!=null)
						httpReq.removeUrlParameter("CLANID");
					return "";
				}
				if(parms.containsKey("OTHERCLANNEXT"))
				{
					String member=httpReq.getUrlParameter("CLANID");
					String lastID="";
					Clan CC=null;
					for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
					{
						CC=(Clan)e.nextElement();
						if(CC==C) continue;
						String name=CC.clanID();
						if((member==null)||((member.length()>0)&&(member.equals(lastID))&&(!name.equals(lastID))))
						{
							httpReq.addFakeUrlParameter("CLANID",name);
							return "";
						}
						lastID=name;
					}
					httpReq.addFakeUrlParameter("CLANID","");
					if(parms.containsKey("EMPTYOK"))
						return "<!--EMPTY-->";
					return " @break@";
				}
				if(parms.containsKey("OTHERCLANNAME"))
				{
					String member=httpReq.getUrlParameter("CLANID");
					Clan CC=CMLib.clans().getClan(member);
					if(CC!=null) str.append(CC.getName()+", ");
				}
				str.append(ClanData.members(C,httpReq,parms,0));
				str.append(ClanData.relations(C,httpReq,parms,0));
				if(str.toString().endsWith(", "))
					str=new StringBuffer(str.substring(0,str.length()-2));
				if(webify)
					str=colorwebifyOnly(str);
				if(decolor)
					str=new StringBuffer(CMStrings.removeColors(str.toString()));
				return clearWebMacros(str.toString());
			}
		}
		return "";
	}
}
