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
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2003-2016 Bo Zimmerman

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

public class PlayerData extends StdWebMacro
{
	@Override public String name() { return "PlayerData"; }

	public final static String[] BASICS={
		"NAME",
		"DESCRIPTION",
		"LASTDATETIME",
		"EMAIL",
		"RACENAME",
		"CHARCLASS",
		"LEVEL",
		"LEVELSTR",
		"CLASSLEVEL",
		"CLASSES",
		"MAXCARRY",
		"ATTACKNAME",
		"ARMORNAME",
		"DAMAGENAME",
		"HOURS",
		"PRACTICES",
		"EXPERIENCE",
		"EXPERIENCELEVEL",
		"TRAINS",
		"MONEY",
		"DEITYNAME",
		"LIEGE",
		"CLANNAMES",
		"CLANROLE", // deprecated
		"ALIGNMENTNAME",
		"ALIGNMENTSTRING",
		"WIMP",
		"STARTROOM",
		"LOCATION",
		"STARTROOMID",
		"LOCATIONID",
		"INVENTORY",
		"WEIGHT",
		"ENCUMBRANCE",
		"GENDERNAME",
		"LASTDATETIMEMILLIS",
		"HITPOINTS",
		"MANA",
		"MOVEMENT",
		"RIDING",
		"HEIGHT",
		"LASTIP",
		"QUESTPOINTS",
		"BASEHITPOINTS",
		"BASEMANA",
		"BASEMOVEMENT",
		"IMAGE",
		"MAXITEMS",
		"IMGURL",
		"HASIMG",
		"NOTES",
		"LEVELS",
		"ATTACK",
		"DAMAGE",
		"ARMOR",
		"SPEEDNAME",
		"SPEED",
		"EXPERTISE",
		"TATTOOS",
		"SECURITY",
		"TITLES",
		"FACTIONNAMES",
		"ACCTEXPUSED",
		"ACCTEXP",
		"FOLLOWERNAMES",
		"ACCOUNT"
	};

	public static int getBasicCode(String val)
	{
		for(int i=0;i<BASICS.length;i++)
			if(val.equalsIgnoreCase(BASICS[i]))
				return i;
		return -1;
	}

	public static String getBasic(MOB M, int i)
	{
		final StringBuffer str=new StringBuffer("");
		switch(i)
		{
		case 0:
			str.append(M.Name() + ", ");
			break;
		case 1:
			str.append(M.description() + ", ");
			break;
		case 2:
			if (M.playerStats() != null)
				str.append(CMLib.time().date2String(M.playerStats().getLastDateTime()) + ", ");
			break;
		case 3:
			if (M.playerStats() != null)
				str.append(M.playerStats().getEmail() + ", ");
			break;
		case 4:
			str.append(M.baseCharStats().getMyRace().name() + ", ");
			break;
		case 5:
			str.append(M.baseCharStats().getCurrentClass().name(M.baseCharStats().getCurrentClassLevel()) + ", ");
			break;
		case 6:
			str.append(M.basePhyStats().level() + ", ");
			break;
		case 7:
			str.append(M.baseCharStats().displayClassLevel(M, true) + ", ");
			break;
		case 8:
			str.append(M.baseCharStats().getClassLevel(M.baseCharStats().getCurrentClass()) + ", ");
			break;
		case 9:
		{
			for(int c=M.charStats().numClasses()-1;c>=0;c--)
			{
				final CharClass C=M.charStats().getMyClass(c);
				str.append(C.name(M.baseCharStats().getCurrentClassLevel())+" ("+M.charStats().getClassLevel(C)+") ");
			}
			str.append(", ");
			break;
		}
		case 10:
			if (M.maxCarry() > (Integer.MAX_VALUE / 3))
				str.append("NA, ");
			else
				str.append(M.maxCarry() + ", ");
			break;
		case 11:
			str.append(CMStrings.capitalizeAndLower(CMStrings.removeColors(CMLib.combat().fightingProwessStr(M))) + ", ");
			break;
		case 12:
			str.append(CMStrings.capitalizeAndLower(CMStrings.removeColors(CMLib.combat().armorStr(M))) + ", ");
			break;
		case 13:
			str.append(CMLib.combat().adjustedDamage(M, null, null, 0, false) + ", ");
			break;
		case 14:
			str.append(Math.round(CMath.div(M.getAgeMinutes(), 60.0)) + ", ");
			break;
		case 15:
			str.append(M.getPractices() + ", ");
			break;
		case 16:
			str.append(M.getExperience() + ", ");
			break;
		case 17: 
			if(M.getExpNeededLevel()==Integer.MAX_VALUE)
				str.append("N/A, ");
			else
				str.append(M.getExpNextLevel()+", ");
			break;
		case 18:
			str.append(M.getTrains() + ", ");
			break;
		case 19:
			str.append(CMLib.beanCounter().getMoney(M) + ", ");
			break;
		case 20:
			str.append(M.getWorshipCharID() + ", ");
			break;
		case 21:
			str.append(M.getLiegeID() + ", ");
			break;
		case 22:
		case 23: 
		{
			final StringBuilder buf = new StringBuilder("");
			for (final Pair<Clan, Integer> p : M.clans())
				buf.append(p.first.getName()).append(", ");
			if (buf.length() > 2)
				str.append(buf.substring(0, buf.length() - 2));
			break;
		}
		case 24:
			str.append(M.fetchFaction(CMLib.factions().AlignID()) + ", ");
			break;
		case 25:
		{
			final Faction.FRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),M.fetchFaction(CMLib.factions().AlignID()));
			if(FR!=null)
				str.append(FR.name()+", ");
			else
				str.append(M.fetchFaction(CMLib.factions().AlignID()));
			break;
		}
		case 26:
			str.append(M.getWimpHitPoint() + ", ");
			break;
		case 27: 
			if(M.getStartRoom()!=null)
				str.append(M.getStartRoom().displayText()+", ");
			break;
		case 28: 
			if(M.location()!=null)
				str.append(M.location().displayText()+", ");
			break;
		case 29: 
			if(M.getStartRoom()!=null)
				str.append(M.getStartRoom().roomID()+", ");
			break;
		case 30: 
			if(M.location()!=null)
				str.append(M.location().roomID()+", ");
			break;
		case 31:
		{
			for(int inv=0;inv<M.numItems();inv++)
			{
				final Item I=M.getItem(inv);
				if((I!=null)&&(I.container()==null))
					  str.append(I.name()+", ");
			}
			break;
		}
		case 32:
			str.append(M.basePhyStats().weight() + ", ");
			break;
		case 33:
			str.append(M.phyStats().weight() + ", ");
			break;
		case 34:
			str.append(CMStrings.capitalizeAndLower(M.baseCharStats().genderName()) + ", ");
			break;
		case 35: 
			if(M.playerStats()!=null)
				 str.append(M.playerStats().getLastDateTime()+", ");
			 break;
		case 36:
			str.append(M.curState().getHitPoints() + ", ");
			break;
		case 37:
			str.append(M.curState().getMana() + ", ");
			break;
		case 38:
			str.append(M.curState().getMovement() + ", ");
			break;
		case 39: 
			if(M.riding()!=null)
				 str.append(M.riding().name()+", ");
			 break;
		case 40:
			str.append(M.basePhyStats().height() + ", ");
			break;
		case 41:
		{
			if(!M.isMonster())
				str.append(M.session().getAddress()+", ");
			else
			if(M.playerStats()!=null)
				str.append(M.playerStats().getLastIP()+", ");
			break;
		}
		case 42:
			str.append(M.getQuestPoint() + ", ");
			break;
		case 43:
			str.append(M.maxState().getHitPoints() + ", ");
			break;
		case 44:
			str.append(M.maxState().getMana() + ", ");
			break;
		case 45:
			str.append(M.maxState().getMovement() + ", ");
			break;
		case 46:
			str.append(M.rawImage() + ", ");
			break;
		case 47:
			str.append(M.maxItems() + ", ");
			break;
		case 48:
		{
			final String[] paths=CMLib.protocol().mxpImagePath(M.image());
			if(paths[0].length()>0)
				str.append(paths[0]+paths[1]+", ");
			break;
		}
		case 49: 
		{
			if(CMLib.protocol().mxpImagePath(M.image())[0].length()>0)
				str.append("true, ");
			 else
				 str.append("false, ");
			 break;
		}
		case 50: if(M.playerStats()!=null)
				 	str.append(M.playerStats().getNotes()+", ");
				 break;
		case 51:
			if(M.playerStats()!=null)
			 {
				long lastDateTime=-1;
				for(int level=0;level<=M.phyStats().level();level++)
				{
					final long dateTime=M.playerStats().leveledDateTime(level);
					final long ageMinutes=M.playerStats().leveledMinutesPlayed(level);
					if((dateTime>1529122205)&&(dateTime!=lastDateTime))
					{
						lastDateTime = dateTime;
						str.append("<TR>");
						if(level==0)
						 	str.append("<TD><FONT COLOR=WHITE>Created</FONT></TD>");
						else
						 	str.append("<TD><FONT COLOR=WHITE>"+level+"</FONT></TD>");
						str.append("<TD><FONT COLOR=WHITE>"+CMLib.time().date2String(dateTime)+"</FONT></TD>");
						str.append("<TD><FONT COLOR=WHITE>"+CMLib.time().date2EllapsedTime(ageMinutes * 60000L,TimeUnit.MINUTES,true)+"</FONT></TD>");
						str.append("</TR>");
					}
				}
				str.append(", ");
			 }
			 break;
		case 52:
			str.append(M.basePhyStats().attackAdjustment() + ", ");
			break;
		case 53:
			str.append(M.basePhyStats().damage() + ", ");
			break;
		case 54:
			str.append(M.basePhyStats().armor() + ", ");
			break;
		case 55:
			str.append(M.phyStats().speed() + ", ");
			break;
		case 56:
			str.append(M.basePhyStats().speed() + ", ");
			break;
		case 57:
		{
			for(final Enumeration<String> x=M.expertises();x.hasMoreElements();)
			{
				final String E=x.nextElement();
				final ExpertiseLibrary.ExpertiseDefinition X=CMLib.expertises().getDefinition(E);
				if(X==null)
					str.append(E+", ");
				else
					str.append(X.name()+", ");
			}
			break;
		}
		case 58:
		{
			for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
			  str.append(e.nextElement().toString()+", ");
			break;
		}
		case 59:
		{
			if(M.playerStats()!=null)
			{
				final List<String> flags=CMParms.parseSemicolons(M.playerStats().getSetSecurityFlags(null), true);
				for(int b=0;b<flags.size();b++)
				{
					final String B=flags.get(b);
					if(B!=null)
						str.append(B+", ");
				}
			}
			break;
		}
		case 60:
		{
			if(M.playerStats()!=null)
				for(int b=0;b<M.playerStats().getTitles().size();b++)
				{
					final String B=M.playerStats().getTitles().get(b);
					if(B!=null)
						str.append(B+", ");
				}
				break;
		}
		case 61:
		{
			for(final Enumeration<String> e=M.factions();e.hasMoreElements();)
			{
				final String FID=e.nextElement();
				final Faction F=CMLib.factions().getFaction(FID);
				final int value=M.fetchFaction(FID);
				if(F!=null)
					str.append(F.name()+" ("+value+"), ");
			}
			break;
		}
		case 62:
			str.append(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION) ? "true" : "false");
			break;
		case 63:
		{
			if(M.playerStats()!=null)
			{
				if(CMSecurity.isAllowedEverywhere(M, CMSecurity.SecFlag.NOEXPIRE))
					str.append("Never");
				else
					str.append(CMLib.time().date2String(M.playerStats().getAccountExpiration()));
			}
			 break;
		}
		case 64: 
		{
			for(int f=0;f<M.numFollowers();f++)
				str.append(M.fetchFollower(f).name()).append(", ");
			//Vector<MOB> V=CMLib.database().DBScanFollowers(M);
			//for(int v=0;v<V.size();v++)
			//    str.append(((MOB)V.elementAt(v)).name()).append(", ");
			break;
		}
		case 65:
			if((M.playerStats()!=null)&&(M.playerStats().getAccount()!=null))
				str.append(M.playerStats().getAccount().getAccountName());
			break;
		}
		return str.toString();
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("PLAYER");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			MOB M=CMLib.players().getLoadPlayer(last);
			if(M==null)
			{
				final MOB authM=Authenticate.getAuthenticatedMob(httpReq);
				if((authM!=null)&&(authM.Name().equalsIgnoreCase(last)))
					M=authM;
				else
					return " @break@";
			}

			final boolean firstTime=(!httpReq.isUrlParameter("ACTION"))
							||(httpReq.getUrlParameter("ACTION")).equals("FIRSTTIME");
			final StringBuffer str=new StringBuffer("");
			for(MOB.Attrib a : MOB.Attrib.values())
			{
				if(parms.containsKey(a.getName()))
				{
					boolean set=M.isAttributeSet(a);
					if(a.isAutoReversed()) 
						set=!set;
					str.append((set?"ON":"OFF")+",");
				}
			}
			for(final int i : CharStats.CODES.ALLCODES())
			{
				final String stat=CharStats.CODES.NAME(i);
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					final CharStats C=M.charStats();
					if(parms.containsKey(stat))
					{
						String old=httpReq.getUrlParameter(stat);
						if((firstTime)||(old.length()==0))
						{
							if((!CharStats.CODES.isBASE(i))&&(i!=CharStats.STAT_GENDER))
								old=""+C.getSave(i);
							else
								old=""+C.getStat(i);
						}
						str.append(old+", ");
					}
				}
			}
			for(final int i : CharStats.CODES.ALLCODES())
			{
				final String stat=CharStats.CODES.NAME(i);
				if(!stat.equalsIgnoreCase("GENDER"))
				{
					final CharStats C=M.baseCharStats();
					if(parms.containsKey("BASE"+stat))
					{
						String old=httpReq.getUrlParameter("BASE"+stat);
						if((firstTime)||(old.length()==0))
							old=""+C.getStat(i);
						str.append(old+", ");
					}
				}
			}
			for(int i=0;i<BASICS.length;i++)
			{
				if(parms.containsKey(BASICS[i]))
				{
					if(httpReq.isUrlParameter(BASICS[i]))
						str.append(httpReq.getUrlParameter(BASICS[i])+", ");
					else
						str.append(getBasic(M,i));
				}
			}
			if(parms.containsKey("RACE"))
			{
				String old=httpReq.getUrlParameter("RACE");
				if((firstTime)||(old.length()==0))
					old=""+M.baseCharStats().getMyRace().ID();
				for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R2=r.nextElement();
					str.append("<OPTION VALUE=\""+R2.ID()+"\"");
					if(R2.ID().equals(old))
						str.append(" SELECTED");
					str.append(">"+R2.name());
				}
			}
			if(parms.containsKey("DEITY"))
			{
				String old=httpReq.getUrlParameter("DEITY");
				if(firstTime)
					old=M.getWorshipCharID();
				str.append("<OPTION "+((old.length()==0)?"SELECTED":"")+" VALUE=\"\">Godless");
				for(final Enumeration<Deity> e=CMLib.map().deities();e.hasMoreElements();)
				{
					final Deity E=e.nextElement();
					str.append("<OPTION VALUE=\""+E.Name()+"\"");
					if(E.Name().equalsIgnoreCase(old))
						str.append(" SELECTED");
					str.append(">"+E.Name());
				}
			}
			if(parms.containsKey("TITLELIST"))
			{
				if(M.playerStats()!=null)
				{
					int b=0;
					final Vector<String> titles=new Vector<String>();
					if(firstTime)
						titles.addAll(M.playerStats().getTitles());
					else
					while(httpReq.isUrlParameter("TITLE"+b))
					{
						final String B=httpReq.getUrlParameter("TITLE"+b);
						if((B!=null)&&(B.trim().length()>0))
							titles.addElement(B);
						b++;
					}
					for(b=0;b<titles.size();b++)
					{
						final String B=titles.elementAt(b);
						if(B!=null)
							str.append("<INPUT TYPE=TEXT NAME=TITLE"+b+" SIZE="+B.length()+" VALUE=\""+CMStrings.replaceAll(B,"\"","&quot;")+"\"><BR>");
					}
					str.append("<INPUT TYPE=TEXT NAME=TITLE"+titles.size()+" SIZE=60 VALUE=\"\">");
				}
			}
			if(parms.containsKey("ALIGNMENT"))
			{
				String old=httpReq.getUrlParameter("ALIGNMENT");
				if((firstTime)||(old.length()==0))
					old=""+M.fetchFaction(CMLib.factions().AlignID());
				if(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null)
				{
					for(final Faction.Align v : Faction.Align.values())
						if(v!=Faction.Align.INDIFF)
						{
							str.append("<OPTION VALUE="+v.toString());
							if(old.equalsIgnoreCase(v.toString()))
								str.append(" SELECTED");
							str.append(">"+CMStrings.capitalizeAndLower(v.toString().toLowerCase()));
						}
				}
			}
			if(parms.containsKey("BASEGENDER"))
			{
				String old=httpReq.getUrlParameter("BASEGENDER");
				if(firstTime)
					old=""+(char)M.baseCharStats().getStat(CharStats.STAT_GENDER);
				str.append("<OPTION VALUE=M "+((old.equalsIgnoreCase("M"))?"SELECTED":"")+">M");
				str.append("<OPTION VALUE=F "+((old.equalsIgnoreCase("F"))?"SELECTED":"")+">F");
				str.append("<OPTION VALUE=N "+((old.equalsIgnoreCase("N"))?"SELECTED":"")+">N");
			}
			if(parms.containsKey("PLAYERCLANRESET"))
				httpReq.removeUrlParameter("CLAN");
			else
			if(parms.containsKey("PLAYERCLANNEXT"))
			{
				final String prevClan=httpReq.getUrlParameter("CLAN");
				boolean found=false;
				boolean next=false;
				for(final Pair<Clan, Integer> p : M.clans())
				{
					if((prevClan == null)||(next))
					{
						httpReq.addFakeUrlParameter("CLAN", p.first.clanID());
						found=true;
						break;
					}
					else
					if(p.first.clanID().equals(prevClan))
						next=true;
				}
				if(!found)
				{
					httpReq.addFakeUrlParameter("CLAN","");
					if(parms.containsKey("EMPTYOK"))
						str.append("<!--EMPTY-->, ");
					else
						str.append(" @break@, ");
				}
			}
			str.append(MobData.expertiseList(M,httpReq,parms));
			str.append(MobData.classList(M,httpReq,parms));
			str.append(MobData.itemList(null,M,M,httpReq,parms,0));
			str.append(MobData.abilities(M,httpReq,parms,0));
			str.append(MobData.factions(M,httpReq,parms,0));
			str.append(AreaData.affects(M,httpReq,parms,0));
			str.append(AreaData.behaves(M,httpReq,parms,0));
			str.append(ExitData.dispositions(M,firstTime,httpReq,parms));
			str.append(MobData.senses(M,firstTime,httpReq,parms));
			str.append(MobData.clans(M,httpReq,parms,0));
			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
			return clearWebMacros(strstr);
		}
		return "";
	}
}
