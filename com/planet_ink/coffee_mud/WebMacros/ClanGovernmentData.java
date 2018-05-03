package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2011-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "ClanGovernmentData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("GOVERNMENT");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			ClanGovernment G=null;
			if(CMath.isInteger(last))
			{
				final int lastID=CMath.s_int(last);
				G=CMLib.clans().getStockGovernment(lastID);
			}
			if(G==null)
			{
				final Set<Integer> usedTypeIDs=new HashSet<Integer>();
				for(final ClanGovernment G2 : CMLib.clans().getStockGovernments())
					usedTypeIDs.add(Integer.valueOf(G2.getID()));
				G=CMLib.clans().createSampleGovernment();
				G.setName(httpReq.getUrlParameter("NAME"));
				G.setCategory(httpReq.getUrlParameter("CATEGORY"));
				for(int i=0;i<CMLib.clans().getStockGovernments().length;i++)
				{
					if(!usedTypeIDs.contains(Integer.valueOf(i)))
					{
						G.setID(i);
						break;
					}
				}
			}
			final StringBuffer str=new StringBuffer("");

			// ******************************************************************************************
			// do govt positions FIRST!
			// ******************************************************************************************
			final List<ClanPosition> posList=new Vector<ClanPosition>();
			String posDexStr="0";
			int posDex=0;
			final Set<Integer> usedRoleIDs=new HashSet<Integer>();
			if(!httpReq.isUrlParameter("GPOSID_"+posDexStr))
			{
				for(final ClanPosition P : G.getPositions())
				{
					posList.add(P);
					usedRoleIDs.add(Integer.valueOf(P.getRoleID()));
				}
			}
			else
			while(httpReq.isUrlParameter("GPOSID_"+posDexStr) && httpReq.getUrlParameter("GPOSID_"+posDexStr).trim().length()>0)
			{
				final String oldID=httpReq.getUrlParameter("GPOSID_"+posDexStr);
				final String oldName=httpReq.getUrlParameter("GPOSNAME_"+posDexStr);
				final String oldPluralName=httpReq.getUrlParameter("GPOSPLURALNAME_"+posDexStr);
				final int oldRoleID=CMath.s_int(httpReq.getUrlParameter("GPOSROLEID_"+posDexStr));
				usedRoleIDs.add(Integer.valueOf(oldRoleID));
				final int oldRank=CMath.s_int(httpReq.getUrlParameter("GPOSRANK_"+posDexStr));
				final int oldMax=CMath.s_int(httpReq.getUrlParameter("GPOSMAX_"+posDexStr));
				final String oldMask=httpReq.getUrlParameter("GPOSINNERMASK_"+posDexStr);
				final String oldIsPublicStr=httpReq.getUrlParameter("GPOSISPUBLIC_"+posDexStr);
				final boolean oldIsPublic=oldIsPublicStr==null?false:oldIsPublicStr.equalsIgnoreCase("on");
				final Clan.Authority powerFuncs[]=new Clan.Authority[Clan.Function.values().length];
				for(int f=0;f<Clan.Function.values().length;f++)
					powerFuncs[f]=Clan.Authority.CAN_NOT_DO;
				String authDexStr="";
				int authDex=0;
				while(httpReq.getUrlParameter("GPOSPOWER_"+posDexStr+"_"+authDexStr)!=null)
				{
					final Clan.Function auth = (Clan.Function)CMath.s_valueOf(Clan.Function.values(),httpReq.getUrlParameter("GPOSPOWER_"+posDexStr+"_"+authDexStr));
					powerFuncs[auth.ordinal()]=Clan.Authority.CAN_DO;
					authDex++;
					authDexStr=Integer.toString(authDex);
				}
				final ClanPosition P=(ClanPosition)CMClass.getCommon("DefaultClanPosition");
				P.setID(oldID);
				P.setRoleID(oldRoleID);
				P.setRank(oldRank);
				P.setName(oldName);
				P.setPluralName(oldPluralName);
				P.setMax(oldMax);
				P.setInnerMaskStr(oldMask);
				P.setFunctionChart(powerFuncs);
				P.setPublic(oldIsPublic);
				posList.add(P);
				posDex++;
				posDexStr=Integer.toString(posDex);
			}

			final String cmpos=httpReq.getUrlParameter("GOVTPOSITION");
			ClanPosition gPos = null;
			if((cmpos!=null)&&(cmpos.length()>0)&&(CMath.s_int(cmpos)>=0)&&(CMath.s_int(cmpos)<posList.size()))
				gPos=posList.get(CMath.s_int(cmpos));

			if((gPos!=null)&&parms.containsKey("GPOSID_"+cmpos))
				str.append(gPos.getID()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSROLEID_"+cmpos))
				str.append(gPos.getRoleID()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSNAME_"+cmpos))
				str.append(gPos.getName()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSPLURALNAME_"+cmpos))
				str.append(gPos.getPluralName()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSRANK_"+cmpos))
				str.append(gPos.getRank()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSMAX_"+cmpos))
				str.append(gPos.getMax()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSINNERMASK_"+cmpos))
				str.append(gPos.getInnerMaskStr()+", ");
			if((gPos!=null)&&parms.containsKey("GPOSISPUBLIC_"+cmpos))
				str.append(gPos.isPublic()?"checked, ":"");
			if((gPos!=null)&&parms.containsKey("GPOSPOWER_"+cmpos+"_"))
			{
				for(final Clan.Function func : Clan.Function.values())
				{
					str.append("<OPTION VALUE=\""+func.toString()+"\"");
					if(gPos.getFunctionChart()[func.ordinal()]==Authority.CAN_DO)
						str.append(" SELECTED");
					str.append(">"+func.toString());
				}
			}
			if(parms.containsKey("GPOSPOWERLIST"))
			{
				for(final Clan.Function func : Clan.Function.values())
					str.append("<OPTION VALUE=\""+func.toString()+"\">"+func.toString());
			}

			if(parms.containsKey("NEXTPOSITIONID"))
			{
				for(int i=0;i<posList.size()+10;i++)
				{
					if(!usedRoleIDs.contains(Integer.valueOf(i)))
					{
						str.append(i+", ");
						break;
					}
				}
			}

			// iterators
			if(parms.containsKey("POSITIONSTART"))
			{
				if(httpReq.getUrlParameter("GOVTPOSITION")!=null)
					httpReq.removeUrlParameter("GOVTPOSITION");
				return "";
			}
			if(parms.containsKey("POSITIONNEXT"))
			{
				String lastPos="";
				for(int p=0;p<posList.size();p++)
				{
					if((cmpos==null)||((cmpos.length()>0)&&(cmpos.equals(lastPos))&&(!(""+p).equals(lastPos))))
					{
						httpReq.addFakeUrlParameter("GOVTPOSITION",(""+p));
						return "";
					}
					lastPos=(""+p);
				}
				httpReq.addFakeUrlParameter("LASTGOVTPOSITION",""+posList.size());
				httpReq.addFakeUrlParameter("GOVTPOSITION","");
				if(parms.containsKey("EMPTYOK"))
					return "<!--EMPTY-->";
				return " @break@";
			}

			if(parms.containsKey("NAME"))
			{
				String old=httpReq.getUrlParameter("NAME");
				if(old==null)
					old=G.getName();
				str.append(old+", ");
			}
			if(parms.containsKey("AUTOROLE"))
			{
				String old=httpReq.getUrlParameter("AUTOROLE");
				if(old==null)
					old=""+G.getAutoRole();
				final int autoPos=CMath.s_int(old);
				for(final ClanPosition pos : posList)
					str.append("<OPTION VALUE="+pos.getRoleID()+" "+((autoPos==pos.getRoleID())?"SELECTED":"")+">"+pos.getName());
			}
			if(parms.containsKey("ACCEPTPOS"))
			{
				String old=httpReq.getUrlParameter("ACCEPTPOS");
				if(old==null)
					old=""+G.getAcceptPos();
				final int autoPos=CMath.s_int(old);
				for(final ClanPosition pos : posList)
					str.append("<OPTION VALUE="+pos.getRoleID()+" "+((autoPos==pos.getRoleID())?"SELECTED":"")+">"+pos.getName());
			}
			if(parms.containsKey("SHORTDESC"))
			{
				String old=httpReq.getUrlParameter("SHORTDESC");
				if(old==null)
					old=G.getShortDesc();
				str.append(old+", ");
			}
			if(parms.containsKey("CATEGORY"))
			{
				String old=httpReq.getUrlParameter("CATEGORY");
				if(old==null)
					old=G.getCategory();
				str.append(old+", ");
			}
			if(parms.containsKey("REQUIREDMASK"))
			{
				String old=httpReq.getUrlParameter("REQUIREDMASK");
				if(old==null)
					old=G.getRequiredMaskStr();
				str.append(old+", ");
			}
			if(parms.containsKey("ENTRYSCRIPT"))
			{
				String old=httpReq.getUrlParameter("ENTRYSCRIPT");
				if(old==null)
					old=G.getEntryScript();
				str.append(old+", ");
			}
			if(parms.containsKey("EXITSCRIPT"))
			{
				String old=httpReq.getUrlParameter("EXITSCRIPT");
				if(old==null)
					old=G.getExitScript();
				str.append(old+", ");
			}
			if(parms.containsKey("ISPUBLIC"))
			{
				String old=httpReq.getUrlParameter("ISPUBLIC");
				if(old==null)
					old=G.isPublic()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("ISDEFAULT"))
			{
				String old=httpReq.getUrlParameter("ISDEFAULT");
				if(old==null)
					old=G.isDefault()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("ISFAMILYONLY"))
			{
				String old=httpReq.getUrlParameter("ISFAMILYONLY");
				if(old==null)
					old=G.isFamilyOnly()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("OVERRIDEMINMEMBERS"))
			{
				String old=httpReq.getUrlParameter("OVERRIDEMINMEMBERS");
				if(old==null)
					old=G.getOverrideMinMembers()==null?"":G.getOverrideMinMembers().toString();
				str.append(old+", ");
			}
			if(parms.containsKey("CONQUESTENABLED"))
			{
				String old=httpReq.getUrlParameter("CONQUESTENABLED");
				if(old==null)
					old=G.isConquestEnabled()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("CONQUESTITEMLOYALTY"))
			{
				String old=httpReq.getUrlParameter("CONQUESTITEMLOYALTY");
				if(old==null)
					old=G.isConquestItemLoyalty()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("CONQUESTDEITYBASIS"))
			{
				String old=httpReq.getUrlParameter("CONQUESTDEITYBASIS");
				if(old==null)
					old=G.isConquestByWorship()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("ISRIVALROUS"))
			{
				String old=httpReq.getUrlParameter("ISRIVALROUS");
				if(old==null)
					old=G.isRivalrous()?"on":"";
				str.append(old.equalsIgnoreCase("on")?"checked, ":"");
			}
			if(parms.containsKey("MAXVOTEDAYS"))
			{
				String old=httpReq.getUrlParameter("MAXVOTEDAYS");
				if(old==null)
					old=Integer.toString(G.getMaxVoteDays());
				str.append(CMath.s_int(old)+", ");
			}
			if(parms.containsKey("XPLEVELFORMULA"))
			{
				String old=httpReq.getUrlParameter("XPLEVELFORMULA");
				if(old==null)
					old=G.getXpCalculationFormulaStr();
				str.append(CMath.s_int(old)+", ");
			}
			if(parms.containsKey("VOTEQUORUMPCT"))
			{
				String old=httpReq.getUrlParameter("VOTEQUORUMPCT");
				if(old==null)
					old=Integer.toString(G.getVoteQuorumPct());
				str.append(CMath.s_int(old)+", ");
			}
			if(parms.containsKey("AUTOPROMOTEBY"))
			{
				String old=httpReq.getUrlParameter("AUTOPROMOTEBY");
				if(old==null)
					old=""+G.getAutoPromoteBy().toString();
				for(final Clan.AutoPromoteFlag flag : Clan.AutoPromoteFlag.values())
					str.append("<OPTION VALUE="+flag.toString()+" "+((old.equals(flag.toString()))?"SELECTED":"")+">"+flag.toString());
			}
			if(parms.containsKey("VOTEFUNCS"))
			{
				final String old=httpReq.getUrlParameter("VOTEFUNCS");
				final Set<String> voteFuncs=new HashSet<String>();
				if(old==null)
				{
					if(posList.size()>0)
					{
						final ClanPosition P=posList.get(0);
						for(final Clan.Function func : Clan.Function.values())
						{
							if(P.getFunctionChart()[func.ordinal()]==Clan.Authority.MUST_VOTE_ON)
								voteFuncs.add(func.toString());
						}
					}
				}
				else
				{
					voteFuncs.add(old);
					int x=1;
					while(httpReq.getUrlParameter("VOTEFUNCS"+x)!=null)
					{
						voteFuncs.add(httpReq.getUrlParameter("VOTEFUNCS"+x));
						x++;
					}
				}
				for(final Clan.Function func : Clan.Function.values())
				{
					str.append("<OPTION VALUE=\""+func.toString()+"\"");
					if(voteFuncs.contains(func.toString()))
						str.append(" SELECTED");
					str.append(">"+func.toString());
				}
			}
			if(parms.containsKey("LONGDESC"))
			{
				String old=httpReq.getUrlParameter("LONGDESC");
				if(old==null)
					old=G.getLongDesc();
				str.append(old+", ");
			}
			if(parms.containsKey("RABLE"))
			{
				final MOB mob=Authenticate.getAuthenticatedMob(httpReq);
				str.append(RaceData.dynAbilities(mob,G.getClanLevelAbilities(null,null,Integer.valueOf(Integer.MAX_VALUE)),G.getName(),G,httpReq,parms,1,parms.get("FONT"))+", ");
			}
			if(parms.containsKey("REFFS"))
				str.append(RaceData.dynEffects(G.getName(),G,httpReq,parms,1,parms.get("FONT"))+", ");

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return clearWebMacros(strstr);
		}
		return "";
	}
}
