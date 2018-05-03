package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
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
import com.planet_ink.coffee_mud.WebMacros.MUDGrinder;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class GrinderClans
{
	public String name()
	{
		return "GrinderClans";
	}

	public static String membersList(Clan C, HTTPRequest httpReq)
	{
		final Vector<String> newMembersNames=new Vector<String>();
		final List<MemberRecord> DV=C.getMemberList();
		if(httpReq.isUrlParameter("MEMB1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("MEMB"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final MOB M=CMLib.players().getLoadPlayer(aff);
					if(M==null)
						return "Unknown player '"+aff+"'.";
					newMembersNames.addElement(M.Name());
					final int newRole=CMath.s_int(httpReq.getUrlParameter("ROLE"+num));
					C.addMember(M,newRole);
				}
				num++;
				aff=httpReq.getUrlParameter("MEMB"+num);
			}
			for(final MemberRecord member : DV)
			{
				if(!newMembersNames.contains(member.name))
				{
					final MOB M=CMLib.players().getLoadPlayer(member.name);
					if(M!=null)
						C.delMember(M);
					else
						CMLib.database().DBUpdateClanMembership(member.name, C.clanID(), -1);
				}
			}
		}
		return "";
	}

	public static String relationsList(Clan C, HTTPRequest httpReq)
	{
		if(httpReq.isUrlParameter("RELATION1"))
		{
			int relat=0;
			Clan CC=null;
			for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
			{
				CC=e.nextElement();
				if(CC==C)
					continue;
				relat++;
				final String aff=httpReq.getUrlParameter("RELATION"+relat);
				if((aff!=null)&&(aff.length()>0))
				{
					if(C.getClanRelations(CC.clanID())!=CMath.s_int(aff))
						C.setClanRelations(CC.clanID(),CMath.s_int(aff),System.currentTimeMillis());
				}
				else
					return "No relation for clan "+CC.clanID();
			}
		}
		return "";
	}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		final String last=httpReq.getUrlParameter("CLAN");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			final Clan C=CMLib.clans().getClan(last);
			if(C!=null)
			{
				String str=null;
				str=httpReq.getUrlParameter("PREMISE");
				if(str!=null)
					C.setPremise(str);
				str=httpReq.getUrlParameter("RECALLID");
				if(str!=null)
				{
					final Room R=MUDGrinder.getRoomObject(httpReq, str);
					if(R!=null)
						C.setRecall(CMLib.map().getExtendedRoomID(R));
				}
				str=httpReq.getUrlParameter("MORGUEID");
				if(str!=null)
				{
					final Room R=MUDGrinder.getRoomObject(httpReq, str);
					if(R!=null)
						C.setMorgue(CMLib.map().getExtendedRoomID(R));
				}
				str=httpReq.getUrlParameter("AUTOPOSITIONID");
				if(str!=null)
					C.setAutoPosition(CMath.s_int(str));
				str=httpReq.getUrlParameter("DONATIONID");
				if(str!=null)
				{
					final Room R=MUDGrinder.getRoomObject(httpReq, str);
					if(R!=null)
						C.setDonation(CMLib.map().getExtendedRoomID(R));
				}
				str=httpReq.getUrlParameter("TAX");
				if(str!=null)
					C.setTaxes(CMath.s_pct(str));
				str=httpReq.getUrlParameter("CCLASSID");
				if(str!=null)
				{
					CharClass CC=CMClass.getCharClass(str);
					if(CC==null)
						CC=CMClass.findCharClass(str);
					if(CC!=null)
						C.setClanClass(CC.ID());
				}
				str=httpReq.getUrlParameter("EXP");
				if(str!=null)
					C.setExp(CMath.s_int(str));
				str=httpReq.getUrlParameter("CATEGORY");
				if(str!=null)
					C.setCategory(str);
				str=httpReq.getUrlParameter("MINMEMBERS");
				if(str!=null)
					C.setMinClanMembers(CMath.s_int(str));
				str=httpReq.getUrlParameter("ISRIVALROUS");
				if(str!=null)
					C.setRivalrous(str.equalsIgnoreCase("on"));
				str=httpReq.getUrlParameter("STATUSID");
				if(str!=null)
					C.setStatus(CMath.s_int(str));
				str=httpReq.getUrlParameter("ACCEPTANCEID");
				if(str!=null)
					C.setAcceptanceSettings(str);
				str=httpReq.getUrlParameter("TYPEID");
				if(str!=null)
					C.setGovernmentID(CMath.s_int(str));
				String err=GrinderClans.membersList(C,httpReq);
				if(err.length()>0)
					return err;
				err=GrinderClans.relationsList(C,httpReq);
				if(err.length()>0)
					return err;
			}
		}
		return "";
	}
}
