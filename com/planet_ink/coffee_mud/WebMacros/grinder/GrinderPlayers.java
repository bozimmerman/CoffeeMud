package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.AreaData;
import com.planet_ink.coffee_mud.WebMacros.ExitData;
import com.planet_ink.coffee_mud.WebMacros.MUDGrinder;
import com.planet_ink.coffee_mud.WebMacros.MobData;
import com.planet_ink.coffee_mud.WebMacros.PlayerData.BASICS;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
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

public class GrinderPlayers extends GrinderMobs
{
	public static String titleList(MOB E, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		if(E.playerStats()==null)
			return "";
		E.playerStats().getTitles().clear();
		if(httpReq.isUrlParameter("TITLE0"))
		{
			int num=0;
			while(httpReq.isUrlParameter("TITLE"+num))
			{
				final String aff=httpReq.getUrlParameter("TITLE"+num);
				if(aff.trim().length()>0)
					E.playerStats().getTitles().add(aff.trim());
				num++;
			}
		}
		return "";
	}

	public static String setBasics(HTTPRequest httpReq, MOB M)
	{
		for(int i=0;i<BASICS.values().length;i++)
		{
			String err = setBasic(httpReq, M, BASICS.values()[i]);
			if((err != null) && (err.trim().length()>0))
				return err;
		}
		return "";
	}
	
	public static String setBasic(HTTPRequest httpReq, MOB M, BASICS basic)
	{
		if(httpReq.isUrlParameter(basic.name()))
		{
			String old=httpReq.getUrlParameter(basic.name());
			if(old==null)
				old="";
			switch(basic)
			{
			case NAME:
				break; // dont set name!
			case DESCRIPTION:
				M.setDescription(old);
				break;
			case LASTDATETIME:
				if (M.playerStats() != null)
					M.playerStats().setLastDateTime(CMLib.time().string2Millis(old));
				break;
			case EMAIL:
				if (M.playerStats() != null)
					M.playerStats().setEmail(old);
				break;
			case RACENAME:
				M.baseCharStats().setMyRace(CMClass.getRace(old));
				break;
			case CHARCLASS:
				break; // dont set class/levels list through this.
			case LEVEL:
				M.basePhyStats().setLevel(CMath.s_int(old));
				break;
			case LEVELSTR:
				break; // dont set levelstr
			case CLASSLEVEL:
				break; // dont set classlevelstr
			case CLASSES:
				break; // dont set classlist through this
			case MAXCARRY:
				break; // cant set maxcarry
			case ATTACKNAME:
				M.basePhyStats().setAttackAdjustment(CMath.s_int(old));
				break;
			case ARMORNAME:
				M.basePhyStats().setArmor(CMath.s_int(old));
				break;
			case DAMAGENAME:
				M.basePhyStats().setDamage(CMath.s_int(old));
				break;
			case HOURS:
				M.setAgeMinutes(CMath.s_long(old) * 60L);
				break;
			case PRACTICES:
				M.setPractices(CMath.s_int(old));
				break;
			case EXPERIENCE:
				M.setExperience(CMath.s_int(old));
				break;
			case EXPERIENCELEVEL:
				break; // dont set exp/level
			case TRAINS:
				M.setTrains(CMath.s_int(old));
				break;
			case MONEY:
				CMLib.beanCounter().setMoney(M, CMath.s_int(old));
				break;
			case DEITYNAME:
				if (CMLib.map().getDeity(old) != null)
					M.setWorshipCharID(old);
				break;
			case LIEGE:
				if (CMLib.players().getPlayer(old) != null)
					M.setLiegeID(old);
				break;
			case CLANNAMES:
				break; // if(CMLib.clans().getClan(old)!=null)
						// M.setClan(old); break;
			case CLANROLE: // deprecated
				break; // M.setClanRole(CMath.s_int(old)); break;
			case ALIGNMENTNAME:
			case ALIGNMENTSTRING:
			{
				if(CMath.isInteger(old))
				{
					final int a=CMath.s_int(old);
					if((a>=0)&&(a<Faction.Align.values().length))
						CMLib.factions().setAlignment(M,Faction.Align.values()[a]);
				}
				else
				{
					final Faction.Align A=(Faction.Align)CMath.s_valueOf(Faction.Align.class,old.toUpperCase().trim());
					if(A!=null)
						CMLib.factions().setAlignment(M,A);
				}
				break;
			}
			case WIMP:
				M.setWimpHitPoint(CMath.s_int(old));
				break;
			case STARTROOM:
			{
				final Room R = MUDGrinder.getRoomObject(httpReq, old);
				if (R != null)
					M.setStartRoom(R);
				break;
			}
			case LOCATION:
			{
				final Room R = MUDGrinder.getRoomObject(httpReq, old);
				if (R != null)
					M.setLocation(R);
				break;
			}
			case STARTROOMID:
			{
				final Room R = MUDGrinder.getRoomObject(httpReq, old);
				if (R != null)
					M.setStartRoom(R);
				break;
			}
			case LOCATIONID:
			{
				final Room R = MUDGrinder.getRoomObject(httpReq, old);
				if (R != null)
					M.setLocation(R);
				break;
			}
			case INVENTORY:
				break; // dont set inv list here
			case WEIGHT:
				M.basePhyStats().setWeight(CMath.s_int(old));
				break;
			case ENCUMBRANCE:
				M.phyStats().setWeight(CMath.s_int(old));
				break;
			case GENDERNAME:
				if (old.length() == 1)
					M.baseCharStats().setStat(CharStats.STAT_GENDER, old.toUpperCase().charAt(0));
				break;
			case LASTDATETIMEMILLIS:
				if (M.playerStats() != null)
					M.playerStats().setLastDateTime(CMath.s_long(old));
				break;
			case HITPOINTS:
				M.curState().setHitPoints(CMath.s_int(old));
				break;
			case MANA:
				M.curState().setMana(CMath.s_int(old));
				break;
			case MOVEMENT:
				M.curState().setMovement(CMath.s_int(old));
				break;
			case RIDING:
				break; // dont set riding here
			case HEIGHT:
				M.basePhyStats().setHeight(CMath.s_int(old));
				break;
			case LASTIP:
				if (M.playerStats() != null)
					M.playerStats().setLastIP(old);
				break;
			case QUESTPOINTS:
				M.setQuestPoint(CMath.s_int(old));
				break;
			case MAXHITPOINTS:
			case BASEHITPOINTS:
				M.baseState().setHitPoints(CMath.s_int(old));
				break;
			case MAXMANA:
			case BASEMANA:
				M.baseState().setMana(CMath.s_int(old));
				break;
			case MAXMOVEMENT:
			case BASEMOVEMENT:
				M.baseState().setMovement(CMath.s_int(old));
				break;
			case IMAGE:
				break; // dont set rawimage here
			case MAXITEMS:
				break; // dont set maxitems here?!
			case IMGURL:
				break; // dont set image here
			case HASIMG:
				break; // dont set imagepath here
			case NOTES:
				if (M.playerStats() != null)
					M.playerStats().setNotes(old);
				break;
			case LEVELS:
				break; // dont set level chart
			case ATTACK:
				M.basePhyStats().setAttackAdjustment(CMath.s_int(old));
				break;
			case DAMAGE:
				M.basePhyStats().setDamage(CMath.s_int(old));
				break;
			case ARMOR:
				M.basePhyStats().setArmor(CMath.s_int(old));
				break;
			case SPEEDNAME:
				M.phyStats().setSpeed(CMath.s_double(old));
				break;
			case SPEED:
				M.basePhyStats().setSpeed(CMath.s_double(old));
				break;
			case EXPERTISE:
			{
				final List<String> V=CMParms.parseCommas(old.toUpperCase(),true);
				M.delAllExpertises();
				for(int v=0;v<V.size();v++)
				{
					if(CMLib.expertises().getDefinition(V.get(v))!=null)
						M.addExpertise(V.get(v));
				}
				break;
			}
			case TATTOOS:
			{
				final List<String> V=CMParms.parseCommas(old.toUpperCase(),true);
				for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
					M.delTattoo(e.nextElement());
				for(final String tatt : V)
					M.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
				break;
			}
			case SECURITY:
			{
				if(M.playerStats()!=null)
				{
					final List<String> V=CMParms.parseCommas(old.toUpperCase(),true);
					M.playerStats().getSetSecurityFlags(CMParms.toSemicolonListString(V));
				}
				break;
			}
			case TITLES:
				break; // CAN'T do titles here!!
			case FACTIONNAMES:
				break; // dont do faction lists here
			case ACCTEXPUSED:
				break; // dont do accountexpiration flag here.
			case ACCTEXP:
			{
				if(M.playerStats()!=null)
				{
					if(old.equalsIgnoreCase("Never"))
					{
						final PlayerStats P=M.playerStats();
						final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
						if(!secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
						{
							secFlags.add(CMSecurity.SecFlag.NOEXPIRE.name());
							P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
						}
					}
					else
					{
						final PlayerStats P=M.playerStats();
						final List<String> secFlags=CMParms.parseSemicolons(P.getSetSecurityFlags(null),true);
						if(secFlags.contains(CMSecurity.SecFlag.NOEXPIRE.name()))
						{
							secFlags.remove(CMSecurity.SecFlag.NOEXPIRE.name());
							P.getSetSecurityFlags(CMParms.toSemicolonListString(secFlags));
						}
						if(old.equalsIgnoreCase("Now"))
							M.playerStats().setAccountExpiration(System.currentTimeMillis());
						else
							M.playerStats().setAccountExpiration(CMLib.time().string2Millis(old));
					}
				}
				break;
			}
			case FOLLOWERNAMES:
			{
				if(M.playerStats()!=null)
				{
					final String oldAccountName =(M.playerStats().getAccount()!=null) ? M.playerStats().getAccount().getAccountName() : "";
					if(!old.equals(oldAccountName))
					{
						final PlayerAccount newAccount = CMLib.players().getLoadAccount(old);
						if(newAccount != null)
						{
							M.playerStats().setAccount(newAccount);
							newAccount.addNewPlayer(M);
							final PlayerAccount oldAccount = (oldAccountName.length()>0)?CMLib.players().getLoadAccount(oldAccountName):null;
							if(oldAccount!=null)
							{
								oldAccount.delPlayer(M);
								CMLib.database().DBUpdateAccount(oldAccount);
							}
						}
					}
				}
				break;
			}
			case ACCOUNT:
				break;
			}
		}
		if(M.playerStats()!=null)
		{
			int b=0;
			M.playerStats().getTitles().clear();
			while(httpReq.isUrlParameter("TITLE"+b))
			{
				String old=httpReq.getUrlParameter("TITLE"+b);
				if(old==null)
					old="";
				M.playerStats().getTitles().add(old);
				b++;
			}
		}
		return "";
	}

	public static String classList(MOB M, HTTPRequest httpReq, java.util.Map<String,String> parms)
	{
		if(httpReq.isUrlParameter("CHARCLASS1"))
		{
			final StringBuffer classList=new StringBuffer("");
			final StringBuffer levelsList=new StringBuffer("");
			int num=1;
			String aff=httpReq.getUrlParameter("CHARCLASS"+num);
			int totalLevel=0;
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final CharClass C=CMClass.getCharClass(aff);
					if(C==null)
						return "Unknown class '"+aff+"'.";
					classList.append(C.ID()+";");
					String lvl=httpReq.getUrlParameter("CHARCLASSLVL"+num);
					if(lvl==null)
						lvl="0";
					totalLevel+=CMath.s_int(lvl);
					levelsList.append(lvl+";");
				}
				num++;
				aff=httpReq.getUrlParameter("CHARCLASS"+num);
			}
			M.baseCharStats().setMyClasses(classList.toString());
			M.baseCharStats().setMyLevels(levelsList.toString());
			M.basePhyStats().setLevel(totalLevel);
		}
		return "";
	}

	public static String editPlayer(MOB whom, HTTPRequest httpReq, java.util.Map<String,String> parms, MOB M)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final Vector<Item> allitems=new Vector<Item>();
		while(M.numItems()>0)
		{
			final Item I=M.getItem(0);
			allitems.addElement(I);
			M.delItem(I);
		}

		for(MOB.Attrib a : MOB.Attrib.values())
		{
			if(httpReq.isUrlParameter(a.getName()))
			{
				String old=httpReq.getUrlParameter(a.getName());
				if(old==null)
					old="";
				if(old.equalsIgnoreCase("on"))
					M.setAttribute(a,true);
				else
					M.setAttribute(a,false);
			}
		}
		for(final int i : CharStats.CODES.ALLCODES())
		{
			final CharStats C=M.charStats();
			final String stat=CharStats.CODES.NAME(i);
			if(httpReq.isUrlParameter(stat))
			{
				String old=httpReq.getUrlParameter(stat);
				if(old==null)
					old="";
				if(!stat.equalsIgnoreCase("GENDER"))
					C.setStat(i,CMath.s_int(old));
				else
				if(old.length()>0)
					C.setStat(i,old.charAt(0));
			}
		}
		for(final int i : CharStats.CODES.ALLCODES())
		{
			final CharStats C=M.baseCharStats();
			final String stat=CharStats.CODES.NAME(i);
			if(httpReq.isUrlParameter("BASE"+stat))
			{
				String old=httpReq.getUrlParameter("BASE"+stat);
				if(old==null)
					old="";
				if(!stat.equalsIgnoreCase("GENDER"))
					C.setStat(i,CMath.s_int(old));
				else
				if(old.length()>0)
					C.setStat(i,old.charAt(0));
			}
		}
		GrinderPlayers.setBasics(httpReq,M);
		if(httpReq.isUrlParameter("RACE"))
		{
			final String old=httpReq.getUrlParameter("RACE");
			if((old!=null)&&(CMClass.getRace(old)!=null))
				M.baseCharStats().setMyRace(CMClass.getRace(old));
		}
		if(httpReq.isUrlParameter("DEITY"))
		{
			final String old=httpReq.getUrlParameter("DEITY");
			if((old!=null)&&(CMLib.map().getDeity(old)!=null))
				M.setWorshipCharID(CMLib.map().getDeity(old).Name());
		}
		if(httpReq.isUrlParameter("ALIGNMENT"))
		{
			final String old=httpReq.getUrlParameter("ALIGNMENT");
			final Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
			if((F!=null)&&(old!=null)&&(old.length()>0))
			{
				for(final Faction.Align v : Faction.Align.values())
				{
					if((v!=Faction.Align.INDIFF)&&(v.toString().equalsIgnoreCase(old)))
						CMLib.factions().setAlignment(M,v);
				}
			}
		}
		String error=GrinderExits.dispositions(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderMobs.senses(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=titleList(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderAreas.doAffects(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderAreas.doBehavs(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderMobs.factions(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderMobs.abilities(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderMobs.items(M,allitems,httpReq);
		if(error.length()>0)
			return error;
		error=GrinderMobs.expertiseList(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=GrinderMobs.clans(M,httpReq,parms);
		if(error.length()>0)
			return error;
		error=classList(M,httpReq,parms);
		if(error.length()>0)
			return error;
		M.recoverPhyStats();
		M.recoverCharStats();
		M.recoverMaxState();
		M.recoverPhyStats();
		M.recoverCharStats();
		M.recoverMaxState();
		if(M.location()!=null)
			M.location().recoverRoomStats();
		CMLib.database().DBUpdatePlayer(M);
		Log.sysOut("Grinder",whom.Name()+" modified player "+M.Name());
		return "";
	}

}
