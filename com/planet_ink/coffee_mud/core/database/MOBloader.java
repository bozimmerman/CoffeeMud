package com.planet_ink.coffee_mud.core.database;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.CMSecurity.SecGroup;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnector.DBPreparedBatchEntry;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PlayerCode;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.PrideCat;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinnerPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class MOBloader
{
	protected DBConnector DB=null;

	public MOBloader(final DBConnector newDB)
	{
		DB=newDB;
	}

	protected Room emptyRoom=null;

	protected Comparator<String[]> itemComparator = new Comparator<String[]>() {
		@Override
		public int compare(final String[] o1, final String[] o2)
		{
			if(o2.length>o1.length)
				return -1;
			if(o1.length>o2.length)
				return 1;
			for(int i=0;i<o1.length;i++)
			{
				final int c=o1[i].compareTo(o2[i]);
				if(c!=0)
					return c;
			}
			return 0;
		}

	};

	protected final Filterer<String> normalInventoryFilter = new Filterer<String>()
	{

		@Override
		public boolean passesFilter(final String obj)
		{
			return (obj != null) && !(obj.startsWith("<ROOM") && (obj.indexOf("/>")>=0));
		}

	};

	protected final Filterer<Pair<String,String>> normalMoneyFilter = new Filterer<Pair<String,String>>()
	{
		@Override
		public boolean passesFilter(final Pair<String, String> obj)
		{
			if (obj!=null && (obj.second==null || obj.second.trim().length()==0))
			{
				final Item I=CMClass.getBasicItem(obj.first);
				if((I!=null)&&(I instanceof Coins))
					return true;
			}
			return false;
		}
	};

	public MOB DBReadUserOnly(String name, final String[] locationID)
	{
		if((name==null)||(name.length()==0))
			return null;
		DBConnection D=null;
		final DatabaseEngine dbE = CMLib.database();
		MOB mob=null;
		name = DB.injectionClean(name);
		int oldDisposition=0;
		try
		{
			D=DB.DBFetch();

			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R.next())
			{
				mob=CMClass.getMOB(DBConnections.getRes(R,"CMCHID"));
				if(mob == null)
				{
					mob=CMClass.getMOB("StdMOB");
					if(mob == null)
						return null;
				}
				oldDisposition=mob.basePhyStats().disposition();
				mob.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
				mob.phyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
				mob.setDisplayText(""); // for centaur

				final CharStats stats=mob.baseCharStats();
				final CharState state=mob.baseState();
				final PlayerStats pstats=(PlayerStats)CMClass.getCommon("DefaultPlayerStats");
				mob.setPlayerStats(pstats);
				final String username=DBConnections.getRes(R,"CMUSERID");
				final String password=DBConnections.getRes(R,"CMPASS");
				mob.setName(username);
				pstats.setPassword(password);
				stats.setMyClasses(DBConnections.getRes(R,"CMCLAS"));
				stats.setStat(CharStats.STAT_STRENGTH,CMath.s_int(DBConnections.getRes(R,"CMSTRE")));
				final Race raceR=CMClass.getRace(DBConnections.getRes(R,"CMRACE"));
				dbE.registerRaceUsed(raceR);
				stats.setMyRace(raceR);
				stats.setStat(CharStats.STAT_DEXTERITY,CMath.s_int(DBConnections.getRes(R,"CMDEXT")));
				stats.setStat(CharStats.STAT_CONSTITUTION,CMath.s_int(DBConnections.getRes(R,"CMCONS")));
				stats.setStat(CharStats.STAT_GENDER,DBConnections.getRes(R,"CMGEND").charAt(0));
				stats.setStat(CharStats.STAT_WISDOM,CMath.s_int(DBConnections.getRes(R,"CMWISD")));
				stats.setStat(CharStats.STAT_INTELLIGENCE,CMath.s_int(DBConnections.getRes(R,"CMINTE")));
				stats.setStat(CharStats.STAT_CHARISMA,CMath.s_int(DBConnections.getRes(R,"CMCHAR")));
				state.setHitPoints(CMath.s_int(DBConnections.getRes(R,"CMHITP")));
				stats.setMyLevels(DBConnections.getRes(R,"CMLEVL"));
				int level=0;
				for(int i=0;i<mob.baseCharStats().numClasses();i++)
					level+=stats.getClassLevel(mob.baseCharStats().getMyClass(i));
				mob.basePhyStats().setLevel(level);
				state.setMana(CMath.s_int(DBConnections.getRes(R,"CMMANA")));
				state.setMovement(CMath.s_int(DBConnections.getRes(R,"CMMOVE")));
				mob.setDescription(DBConnections.getRes(R,"CMDESC"));
				final int align=(CMath.s_int(DBConnections.getRes(R,"CMALIG")));
				if((CMLib.factions().getFaction(CMLib.factions().getAlignmentID())!=null)&&(align>=0))
					CMLib.factions().setAlignmentOldRange(mob,align);
				mob.setExperience(CMath.s_int(DBConnections.getRes(R,"CMEXPE")));
				//mob.setExpNextLevel(CMath.s_int(DBConnections.getRes(R,"CMEXLV")));
				stats.setWorshipCharID(DBConnections.getRes(R,"CMWORS"));
				mob.setPractices(CMath.s_int(DBConnections.getRes(R,"CMPRAC")));
				mob.setTrains(CMath.s_int(DBConnections.getRes(R,"CMTRAI")));
				mob.setAgeMinutes(CMath.s_long(DBConnections.getRes(R,"CMAGEH")));
				mob.setMoney(CMath.s_int(DBConnections.getRes(R,"CMGOLD")));
				mob.setWimpHitPoint(CMath.s_int(DBConnections.getRes(R,"CMWIMP")));
				mob.setQuestPoint(CMath.s_int(DBConnections.getRes(R,"CMQUES")));
				String roomID=DBConnections.getRes(R,"CMROID");
				if(roomID==null)
					roomID="";
				final int x=roomID.indexOf("||");
				if(x>=0)
				{
					locationID[0]=roomID.substring(x+2);
					mob.setLocation(CMLib.map().getRoom(locationID[0]));
					roomID=roomID.substring(0,x);
				}
				mob.setStartRoom(CMLib.map().getRoom(roomID));
				pstats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
				pstats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				mob.basePhyStats().setAttackAdjustment(CMath.s_int(DBConnections.getRes(R,"CMATTA")));
				mob.basePhyStats().setArmor(CMath.s_int(DBConnections.getRes(R,"CMAMOR")));
				mob.basePhyStats().setDamage(CMath.s_int(DBConnections.getRes(R,"CMDAMG")));
				mob.setAttributesBitmap((DBConnections.getLongRes(R,"CMBTMP")));
				mob.setLiegeID(DBConnections.getRes(R,"CMLEIG"));
				mob.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
				mob.basePhyStats().setWeight((int)DBConnections.getLongRes(R,"CMWEIT"));
				pstats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				final String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
					pstats.setColorStr(colorStr);
				pstats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				mob.setClan("", Integer.MIN_VALUE); // delete all sequence
				pstats.setEmail(DBConnections.getRes(R,"CMEMAL"));
				final String buf=DBConnections.getRes(R,"CMPFIL");
				pstats.setXML(buf);
				stats.setNonBaseStatsFromString(DBConnections.getRes(R,"CMSAVE"));
				List<String> V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
				for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
					mob.delTattoo(e.nextElement());
				for(final String tatt : V9)
					mob.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
				V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
				mob.delAllExpertises();
				for(int v=0;v<V9.size();v++)
					mob.addExpertise(V9.get(v));

				// check for a non-existant birthday and fix it
				if(pstats.getBirthday()==null)
				{
					stats.setStat(CharStats.STAT_AGE,
						pstats.initializeBirthday(CMLib.time().localClock(mob.getStartRoom()),(int)Math.round(CMath.div(mob.getAgeMinutes(),60.0)),stats.getMyRace()));
				}

				final TimeClock C=CMLib.time().localClock(mob.getStartRoom());
				// check for a messed up/reset birthday and fix it
				if((pstats.getBirthday()[PlayerStats.BIRTHDEX_YEAR]==1)
				||(pstats.getBirthday()[PlayerStats.BIRTHDEX_YEAR]>C.getYear()))
				{
					final int age = mob.baseCharStats().getStat(CharStats.STAT_AGE);
					if((pstats.getBirthday()[PlayerStats.BIRTHDEX_MONTH]==1)
					&&(pstats.getBirthday()[PlayerStats.BIRTHDEX_DAY]==1))
					{
						if((age > 1)&&(C.getYear() > age))
							pstats.initializeBirthday(C,(int)Math.round(CMath.div(mob.getAgeMinutes(),60.0)),stats.getMyRace());
						Log.warnOut("MOBloader","Reset the birthday of player '"+mob.Name()+"' (Might have been holdover from being first-year player)");
					}
					if((age > 1)&&(C.getYear() > age))
					{
						pstats.getBirthday()[PlayerStats.BIRTHDEX_YEAR]=C.getYear()-age;
						pstats.getBirthday()[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED]=C.getYear();
					}
				}
				mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));
				final List<XMLLibrary.XMLTag> CleanXML=CMLib.xml().parseAllXML(DBConnections.getRes(R,"CMMXML"));
				R.close();
				if(pstats.getSavedPose().length()>0)
					mob.setDisplayText(pstats.getSavedPose());
				CMLib.coffeeMaker().unpackFactionFromXML(mob,CleanXML);
				if((CMProps.isUsingAccountSystem())&&(pstats.getAccount()==null))
				{
					// yes, this can happen when you wiggle in and out of the account system.
					for(final Enumeration<PlayerAccount> a = CMLib.players().accounts(); a.hasMoreElements();)
					{
						final PlayerAccount pA=a.nextElement();
						if(pA.findPlayer(mob.Name())!=null)
						{
							pstats.setAccount(pA);
							break;
						}
					}
				}
				CMLib.achievements().loadPlayerSkillAwards(mob, pstats);
			}
			R.close();
			R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+name+"'");
			while(R.next() && (mob!=null))
			{
				final String clanID=DBConnections.getRes(R,"CMCLAN");
				final int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
				final Clan C=CMLib.clans().getClan(clanID);
				if(C!=null)
					mob.setClan(C.clanID(), clanRole);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if(mob != null)
		{
			mob.basePhyStats().setDisposition(oldDisposition);
			mob.recoverPhyStats();
		}
		return mob;
	}

	public String queryCMCHARStr(final String name, final String fieldName)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();

			final ResultSet R=D.query("SELECT "+fieldName+" FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R.next())
				return DBConnections.getRes(R, fieldName);
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String queryCMACCTStr(final String name, final String fieldName)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();

			final ResultSet R=D.query("SELECT "+fieldName+" FROM CMACCT WHERE CMANAM='"+name+"'");
			if(R.next())
				return DBConnections.getRes(R, fieldName);
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public Long queryCMCHARLong(final String name, final String fieldName)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();

			final ResultSet R=D.query("SELECT "+fieldName+" FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R.next())
				return Long.valueOf(DBConnections.getLongRes(R,fieldName));
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public Object DBReadPlayerValue(final String name, final PlayerCode code)
	{
		switch(code)
		{
		case ABLES:
		{
			final XVector<Ability> V=new XVector<Ability>();
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				final ResultSet R2=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+name+"'");
				while(R2.next())
				{
					final String abilityID=DBConnections.getRes(R2,"CMABID");
					int proficiency=(int)DBConnections.getLongRes(R2,"CMABPF");
					if((proficiency>Integer.MIN_VALUE+1)
					&&(proficiency!=Integer.MAX_VALUE))
					{
						if(proficiency<0)
							proficiency+=200;
						final Ability A=CMClass.getRawAbility(abilityID);
						if(A!=null)
						{
							A.setProficiency(proficiency);
							A.setMiscText(DBConnections.getRes(R2,"CMABTX"));
							V.add(A);
						}
					}
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("MOB",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
			return V;
		}
		case AFFBEHAV:
		{
			final XVector<CMObject> V=new XVector<CMObject>();
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				final ResultSet R2=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+name+"'");
				while(R2.next())
				{
					final String abilityID=DBConnections.getRes(R2,"CMABID");
					final int proficiency=(int)DBConnections.getLongRes(R2,"CMABPF");
					if(proficiency<=Integer.MIN_VALUE+1)
					{
						final Behavior newBehavior=CMClass.getBehavior(abilityID);
						if(newBehavior!=null)
						{
							newBehavior.setParms(DBConnections.getRes(R2,"CMABTX"));
							V.add(newBehavior);
						}
					}
					else
					{
						final Ability A=CMClass.getRawAbility(abilityID);
						if(A!=null)
						{
							A.setMiscText(DBConnections.getRes(R2,"CMABTX"));
							if(proficiency==Integer.MAX_VALUE)
								V.add(A);
							else
							if(proficiency<0)
							{
								A.setProficiency(proficiency+200);
								V.add(A);
							}
						}
					}
				}
			}
			catch(final Exception sqle)
			{
				Log.errOut("MOB",sqle);
			}
			finally
			{
				DB.DBDone(D);
			}
			return V;
		}
		case ALIGNMENT:
		{
			final String alignmentID=CMLib.factions().getAlignmentID();
			final List<XMLLibrary.XMLTag> CleanXML=CMLib.xml().parseAllXML(queryCMCHARStr(name, "CMMXML"));
			for(final Pair<String,Integer> p : CMLib.coffeeMaker().unpackFactionFromXML(null,CleanXML))
				if(p.first.equalsIgnoreCase(alignmentID))
					return p.second;
			return Integer.valueOf(Integer.MAX_VALUE);
		}
		case ARMOR:
			return Integer.valueOf((queryCMCHARLong(name, "CMAMOR")).intValue());
		case ATTACK:
			return Integer.valueOf((queryCMCHARLong(name, "CMATTA")).intValue());
		case CHARCLASS:
		{
			final CharStats stats = (CharStats)CMClass.getCommon("DefaultCharStats");
			stats.setMyClasses(queryCMCHARStr(name, "CMCLAS"));
			return stats.getCurrentClass();
		}
		case DAMAGE:
			return Integer.valueOf((queryCMCHARLong(name, "CMDAMG")).intValue());
		case DESCRIPTION:
			return queryCMCHARStr(name, "CMDESC");
		case EXPERS:
		{
			final String buf=queryCMCHARStr(name, "CMPFIL");
			return CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
		}
		case FACTIONS:
		{
			final List<XMLLibrary.XMLTag> CleanXML=CMLib.xml().parseAllXML(queryCMCHARStr(name, "CMMXML"));
			return CMLib.coffeeMaker().unpackFactionFromXML(null,CleanXML);
		}
		case INVENTORY:
			return new XVector<String[]>(DBReadPlayerItemData(name, null, normalInventoryFilter).iterator());
		case LEVEL:
		{
			final CharStats stats = (CharStats)CMClass.getCommon("DefaultCharStats");
			stats.setMyClasses(queryCMCHARStr(name, "CMCLAS"));
			stats.setMyLevels(queryCMCHARStr(name, "CMLEVL"));
			int level=0;
			for(int i=0;i<stats.numClasses();i++)
				level+=stats.getClassLevel(stats.getMyClass(i));
			return Integer.valueOf(level);
		}
		case MONEY:
		{
			final List<String[]> items = DBReadPlayerItemData(name, normalMoneyFilter, null);
			final List<Coins> coins = new Vector<Coins>();
			for(final String[] i : items)
			{
				Item I=CMClass.getItemPrototype(i[1]);
				if(I instanceof Coins)
				{
					I=DBBuildItemFromData(i);
					coins.add((Coins)I);
				}
			}
			final int oldmoney = queryCMCHARLong(name, "CMGOLD").intValue();
			if(oldmoney != 0)
			{
				String roomID=queryCMCHARStr(name, "CMROID");
				if(roomID==null)
					roomID="";
				final int x=roomID.indexOf("||");
				if(x>=0)
					roomID = roomID.substring(0,x).trim();
				String currency="";
				if(roomID.length()>0)
				{
					final Room R=CMLib.map().getRoom(roomID);
					currency = CMLib.beanCounter().getCurrency(R);
				}
				coins.addAll(CMLib.beanCounter().makeAllCurrency(currency, oldmoney));
			}
			return coins;
		}
		case NAME:
			return name;
		case RACE:
			return CMClass.getRace(queryCMCHARStr(name, "CMRACE"));
		case TATTS:
		{
			final List<Tattoo> T=new ArrayList<Tattoo>();
			final String buf=queryCMCHARStr(name, "CMPFIL");
			final List<String> V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
			for(final String tatt : V9)
				T.add(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
			return T;
		}
		case ACCOUNT:
		{
			if(CMProps.isUsingAccountSystem())
			{
				for(final Enumeration<PlayerAccount> a = CMLib.players().accounts(); a.hasMoreElements();)
				{
					final PlayerAccount pA=a.nextElement();
					if(pA.findPlayer(name)!=null)
						return pA.getAccountName();
				}
			}
			return "";
		}
		case AGE:
			return queryCMCHARLong(name, "CMAGEH");
		case CHANNELMASK:
			return Integer.valueOf((queryCMCHARLong(name, "CMCHAN")).intValue());
		case CLANS:
		{
			final XVector<Pair<Clan,Integer>> V=new XVector<Pair<Clan,Integer>>();
			for(final Pair<String,Integer> p : DBReadPlayerClans(name))
			{
				final Clan C=CMLib.clans().getClan(p.first);
				if(C!=null)
					V.add(new Pair<Clan,Integer>(C,p.second));
			}
			return V;
		}
		case COLOR:
			return queryCMCHARStr(name, "CMCOLR");
		case DEITY:
			return queryCMCHARStr(name, "CMWORS");
		case EMAIL:
			return queryCMCHARStr(name, "CMEMAL");
		case EXPERIENCE:
			return Integer.valueOf((queryCMCHARLong(name, "CMEXPE")).intValue());
		case HEIGHT:
			return Integer.valueOf((queryCMCHARLong(name, "CMHEIT")).intValue());
		case HITPOINTS:
			return Integer.valueOf((queryCMCHARLong(name, "CMHITP")).intValue());
		case LASTDATE:
			return queryCMCHARLong(name, "CMDATE");
		case LASTIP:
			return queryCMCHARStr(name, "CMLSIP");
		case LEIGE:
			return queryCMCHARStr(name, "CMLEIG");
		case LOCATION:
		{
			String roomID=queryCMCHARStr(name, "CMROID");
			if(roomID==null)
				roomID="";
			final int x=roomID.indexOf("||");
			if(x>=0)
				return roomID.substring(x+2);
			return roomID;
		}
		case MANA:
			return Integer.valueOf((queryCMCHARLong(name, "CMMANA")).intValue());
		case MATTRIB:
			return Long.valueOf((queryCMCHARLong(name, "CMBTMP")).longValue());
		case MOVES:
			return Integer.valueOf((queryCMCHARLong(name, "CMMOVE")).intValue());
		case PASSWORD:
		{
			if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
			{
				final String accountName = DBGetAccountNameFromPlayer(name);
				if(accountName != null)
					return queryCMACCTStr(accountName, "CMPASS");
				return "";
			}
			else
				return queryCMCHARStr(name, "CMPASS");
		}
		case PRACTICES:
			return Integer.valueOf((queryCMCHARLong(name, "CMPRAC")).intValue());
		case QUESTPOINTS:
			return Integer.valueOf((queryCMCHARLong(name, "CMQUES")).intValue());
		case STARTROOM:
		{
			String roomID=queryCMCHARStr(name, "CMROID");
			if(roomID==null)
				roomID="";
			final int x=roomID.indexOf("||");
			if(x>=0)
				return roomID.substring(0,x);
			return roomID;
		}
		case TRAINS:
			return Integer.valueOf((queryCMCHARLong(name, "CMTRAI")).intValue());
		case WEIGHT:
			return Integer.valueOf((queryCMCHARLong(name, "CMWEIT")).intValue());
		case WIMP:
			return Integer.valueOf((queryCMCHARLong(name, "CMWIMP")).intValue());
		}
		return null;
	}

	public void updatePlayerStartRooms(final String oldID, final String newID)
	{
		DB.update(
		"UPDATE CMCHAR SET "
		+"CMROID='"+newID+"' "
		+"WHERE CMROID='"+oldID+"'");
	}

	public void updateCMCHARString(final String name, final String fieldName, final Object value)
	{
		DB.updateWithClobs("UPDATE CMCHAR SET "+fieldName+"=? WHERE CMUSERID=?", value.toString(), name);
	}

	public void updateCMCHARLong(final String name, final String fieldName, Object value)
	{
		value = "" + CMath.s_long(value.toString()); // poor man's sql injection fix
		DB.updateWithClobs("UPDATE CMCHAR SET "+fieldName+"="+value.toString()+" WHERE CMUSERID=?", name);
	}

	public void DBSetPlayerValue(final String name, final PlayerCode code, final Object value)
	{
		switch(code)
		{
		case ABLES:
		{
			@SuppressWarnings("unchecked")
			final XVector<Ability> newAbles=new XVector<Ability>((List<Ability>)value);
			@SuppressWarnings("unchecked")
			final XVector<Ability> oldAbles=new XVector<Ability>((List<Ability>)this.DBReadPlayerValue(name, code));
			final List<Ability>[] deltas = newAbles.makeDeltas(oldAbles, new Comparator<Ability>() {
				@Override
				public int compare(final Ability o1, final Ability o2)
				{
					return o1.ID().compareTo(o2.ID());
				}
			});
			for(final Ability p : deltas[0])
			{
				DB.updateWithClobs("INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX) "
								+ "values (?,?,"+p.proficiency()+",?)",name,p.ID(),p.text());
			}
			for(final Ability p : deltas[1])
				DB.updateWithClobs("DELETE FROM CMCHAB WHERE CMUSERID=? AND CMABID=?",name,p.ID());
			for(final Ability np : newAbles)
			{
				for(final Ability op : oldAbles)
				{
					if(np.ID().equalsIgnoreCase(op.ID())
					&&(!np.text().equalsIgnoreCase(op.text())))
						DB.updateWithClobs("UPDATE CMCHAB SET CMABTX=? WHERE CMUSERID=? AND CMABID=?",np.text(),name,np.ID());
				}
			}
			break;
		}
		case AFFBEHAV:
		{
			@SuppressWarnings("unchecked")
			final XVector<CMObject> newAffBs=new XVector<CMObject>((List<CMObject>)value);
			@SuppressWarnings("unchecked")
			final XVector<CMObject> oldAffBs=new XVector<CMObject>((List<CMObject>)this.DBReadPlayerValue(name, code));
			final List<CMObject>[] deltas = newAffBs.makeDeltas(oldAffBs, new Comparator<CMObject>() {
				@Override
				public int compare(final CMObject o1, final CMObject o2)
				{
					final String o1id = (o1 instanceof Behavior) ? ("B"+o1.ID()) : ("A"+o1.ID());
					final String o2id = (o2 instanceof Behavior) ? ("B"+o2.ID()) : ("A"+o2.ID());
					return o1id.compareTo(o2id);
				}
			});
			for(final CMObject p : deltas[0])
			{
				if(p instanceof Behavior)
					DB.updateWithClobs("INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX) "
									+ "values (?,?,"+(Integer.MIN_VALUE+1)+",?)",name,p.ID(),((Behavior)p).getParms());
				else
					DB.updateWithClobs("INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX) "
							+ "values (?,?,"+Integer.MAX_VALUE+",?)",name,p.ID(),((Ability)p).text());

			}
			for(final CMObject p : deltas[1])
				DB.updateWithClobs("DELETE FROM CMCHAB WHERE CMUSERID=? AND CMABID=?",name,p.ID());
			for(final CMObject np : newAffBs)
			{
				for(final CMObject op : oldAffBs)
				{
					if(np.ID().equalsIgnoreCase(op.ID())
					&&(np instanceof Behavior)
					&&(op instanceof Behavior)
					&&(!((Behavior)np).getParms().equalsIgnoreCase(((Behavior)op).getParms())))
						DB.updateWithClobs("UPDATE CMCHAB SET CMABTX=? WHERE CMUSERID=? AND CMABID=?",((Behavior)np).getParms(),name,np.ID());
					else
					if(np.ID().equalsIgnoreCase(op.ID())
					&&(np instanceof Ability)
					&&(op instanceof Ability)
					&&(!((Ability)np).text().equalsIgnoreCase(((Ability)op).text())))
						DB.updateWithClobs("UPDATE CMCHAB SET CMABTX=? WHERE CMUSERID=? AND CMABID=?",((Ability)np).text(),name,np.ID());
				}
			}
			break;
		}
		case ALIGNMENT:
		{
			final String alignmentID=CMLib.factions().getAlignmentID();
			final List<XMLLibrary.XMLTag> xmlTags=CMLib.xml().parseAllXML(queryCMCHARStr(name,"CMMXML"));
			final List<Pair<String,Integer>> oldPack = CMLib.coffeeMaker().unpackFactionFromXML(null,xmlTags);
			for(final Pair<String,Integer> p : oldPack)
			{
				if(p.first.equalsIgnoreCase(alignmentID))
					p.second = Integer.valueOf(value.toString());
			}
			final String newXML = CMLib.coffeeMaker().getFactionXML(null, oldPack);
			final XMLLibrary.XMLTag newFactionsTag=CMLib.xml().parseAllXML(newXML).get(0);
			final StringBuilder newXMLStr = new StringBuilder("");
			for(final XMLLibrary.XMLTag tag : xmlTags)
			{
				if(tag.tag().equalsIgnoreCase("FACTIONS"))
					newXMLStr.append(newFactionsTag.toString());
				else
					newXMLStr.append(tag.toString());
			}
			updateCMCHARString(name, "CMMXML", newXMLStr.toString());
			break;
		}
		case ARMOR:
			updateCMCHARLong(name, "CMAMOR", value);
			break;
		case ATTACK:
			updateCMCHARLong(name, "CMATTA", value);
			break;
		case CHARCLASS:
		{
			final CharStats stats = (CharStats)CMClass.getCommon("DefaultCharStats");
			stats.setMyClasses(queryCMCHARStr(name,"CMCLAS"));
			stats.setMyLevels(queryCMCHARStr(name,"CMLEVL"));
			stats.setCurrentClass((CharClass)value);
			updateCMCHARString(name, "CMCLAS", stats.getMyClassesStr());
			break;
		}
		case DAMAGE:
			updateCMCHARLong(name, "CMDAMG", value);
			break;
		case DESCRIPTION:
			updateCMCHARString(name, "CMDESC", value);
			break;
		case EXPERS:
		{
			final String buf=queryCMCHARStr(name, "CMPFIL");
			final List<XMLLibrary.XMLTag> tags=CMLib.xml().parseAllXML(buf);
			@SuppressWarnings("unchecked")
			final List<String> Ts=(List<String>)value;
			final StringBuilder tbuf=new StringBuilder("");
			for(final String T : Ts)
			{
				if(tbuf.length()>0)
					tbuf.append(";");
				tbuf.append(T.toString());
			}
			final StringBuilder str = new StringBuilder("");
			for(final XMLLibrary.XMLTag tag : tags)
			{
				if(tag.tag().equalsIgnoreCase("EDUS"))
					tag.setValue(tbuf.toString());
				str.append(tag.toString());
			}
			updateCMCHARString(name, "CMPFIL", str.toString());
			break;
		}
		case FACTIONS:
		{
			final List<XMLLibrary.XMLTag> xmlTags=CMLib.xml().parseAllXML(queryCMCHARStr(name,"CMMXML"));
			@SuppressWarnings("unchecked")
			final XVector<Pair<String,Integer>> newPack = new XVector<Pair<String,Integer>>((List<Pair<String,Integer>>)value);
			final String newXML = CMLib.coffeeMaker().getFactionXML(null, newPack);
			final XMLLibrary.XMLTag newFactionsTag=CMLib.xml().parseAllXML(newXML).get(0);
			final StringBuilder newXMLStr = new StringBuilder("");
			for(final XMLLibrary.XMLTag tag : xmlTags)
			{
				if(tag.tag().equalsIgnoreCase("FACTIONS"))
					newXMLStr.append(newFactionsTag.toString());
				else
					newXMLStr.append(tag.toString());
			}
			updateCMCHARString(name, "CMMXML", newXMLStr.toString());
			break;
		}
		case INVENTORY:
		{
			final XVector<String[]> oldInv = new XVector<String[]>(DBReadPlayerItemData(name, null, normalInventoryFilter).iterator());
			@SuppressWarnings("unchecked")
			final XVector<String[]> newInv = new XVector<String[]>((List<String[]>)value);
			final List<String[]>[] deltas = newInv.makeDeltas(oldInv, itemComparator);
			for(final String[] p : deltas[1]) // must be done first, because deleted might just be changed
			{
				if((p[0]!=null)&&(p[0].length()>0))
					DB.updateWithClobs("DELETE FROM CMCHIT WHERE CMUSERID=? AND CMITID=? AND CMITNM=?",name,p[1],p[0]);
			}
			for(final String[] p : deltas[0])
			{
				final Item I=CMClass.getItem(p[1]);
				if(I!=null)
				{
					final String itemID=((p[0]!=null)&&(p[0].length()>0))?p[0]:getShortID(I);
					 // String[] (dbid, item class, item txt, loID, worn, uses, lvl, abilty, heit)
					DB.updateWithClobs("INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
									  +"CMITUR, CMITLV, CMITAB, CMHEIT"
									  +") values (?,?,?,?,?,"
									  + Integer.valueOf(CMath.s_int(p[5]))+","
									  + Integer.valueOf(CMath.s_int(p[6]))+","
									  + Integer.valueOf(CMath.s_int(p[7]))+","
									  + Integer.valueOf(CMath.s_int(p[8]))+","
									  + Integer.valueOf(CMath.s_int(p[9]))+")",name,itemID,p[1],p[2],p[3]);
				}
			}
			return ;
		}
		case LEVEL:
		{
			final CharStats stats = (CharStats)CMClass.getCommon("DefaultCharStats");
			stats.setMyClasses(queryCMCHARStr(name,"CMCLAS"));
			stats.setMyLevels(queryCMCHARStr(name,"CMLEVL"));
			stats.setClassLevel(stats.getCurrentClass(), CMath.s_int(value.toString()) - stats.combinedSubLevels());
			updateCMCHARString(name, "CMLEVL", stats.getMyLevelsStr());
			break;
		}
		case MONEY:
		{
			final XVector<String[]> oldInv = new XVector<String[]>(DBReadPlayerItemData(name, normalMoneyFilter, null).iterator());
			final XVector<Coins> oldCoins = new XVector<Coins>();
			for(final Iterator<String[]> i=oldInv.iterator();i.hasNext();)
			{
				final String[] t=i.next();
				Item I=CMClass.getItemPrototype(t[1]);
				if(I instanceof Coins)
				{
					I=DBBuildItemFromData(t);
					oldCoins.add((Coins)I);
				}
			}
			oldInv.clear();
			@SuppressWarnings("unchecked")
			final XVector<Coins> newInv = new XVector<Coins>((List<Coins>)value);
			final List<Coins>[] deltas = newInv.makeDeltas(oldCoins, new Comparator<Coins>() {
				@Override
				public int compare(final Coins o1, final Coins o2)
				{
					int cv = o1.ID().compareTo(o2.ID());
					if(cv == 0)
					{
						cv = o1.text().compareTo(o2.text());
						if(cv == 0)
						{
							cv = o1.getCurrency().compareTo(o2.getCurrency());
							if(cv == 0)
							{
								cv = Long.valueOf(o1.getNumberOfCoins()).compareTo(Long.valueOf(o2.getNumberOfCoins()));
								if(cv == 0)
									cv = Double.valueOf(o1.getDenomination()).compareTo(Double.valueOf(o2.getDenomination()));
							}
						}
					}
					return cv;
				}
			});
			for(final Coins p : deltas[0])
			{
				final Item I=p;
				final String itemID=((I.databaseID()!=null)&&(I.databaseID().length()>0))?I.databaseID():getShortID(I);
				DB.updateWithClobs("INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
								  +"CMITUR, CMITLV, CMITAB, CMHEIT"
								  +") values (?,?,?,?,'',"
								  + I.rawProperLocationBitmap()+","
								  + I.usesRemaining()+","
								  + I.basePhyStats().level()+","
								  + I.basePhyStats().ability()+","
								  + I.basePhyStats().height()+")",name,itemID,p.ID(),p.text());
			}
			for(final Coins p : deltas[1])
			{
				if((p.databaseID()!=null)&&(p.databaseID().length()>0))
					DB.updateWithClobs("DELETE FROM CMCHIT WHERE CMUSERID=? AND CMITID=? AND CMITNM=?",name,p.ID(),p.databaseID());
			}
			break;
		}
		case NAME:
			break; // just no
		case RACE:
			updateCMCHARString(name, "CMRACE", ((Race)value).ID());
			break;
		case TATTS:
		{
			final String buf=queryCMCHARStr(name, "CMPFIL");
			final List<XMLLibrary.XMLTag> tags=CMLib.xml().parseAllXML(buf);
			@SuppressWarnings("unchecked")
			final List<Tattoo> Ts=(List<Tattoo>)value;
			final StringBuilder tbuf=new StringBuilder("");
			for(final Tattoo T : Ts)
			{
				if(tbuf.length()>0)
					tbuf.append(";");
				tbuf.append(T.toString());
			}
			final StringBuilder str = new StringBuilder("");
			for(final XMLLibrary.XMLTag tag : tags)
			{
				if(tag.tag().equalsIgnoreCase("TATTS"))
					tag.setValue(tbuf.toString());
				str.append(tag.toString());
			}
			updateCMCHARString(name, "CMPFIL", str.toString());
			break;
		}
		case ACCOUNT:
			break; // lets not go here
		case AGE:
			updateCMCHARLong(name, "CMAGEH", value);
			break;
		case CHANNELMASK:
			updateCMCHARLong(name, "CMCHAN", value);
			break;
		case CLANS:
		{
			final XVector<Pair<Clan,Integer>> oldClans = new XVector<Pair<Clan,Integer>>();
			for(final Pair<String,Integer> p : DBReadPlayerClans(name))
			{
				final Clan C=CMLib.clans().getClan(p.first);
				oldClans.add(new Pair<Clan,Integer>(C,p.second));
			}
			@SuppressWarnings("unchecked")
			final XVector<Pair<Clan,Integer>> newClans = new XVector<Pair<Clan,Integer>>((List<Pair<Clan,Integer>>)value);
			final List<Pair<Clan,Integer>>[] deltas = newClans.makeDeltas(oldClans, new Comparator<Pair<Clan, Integer>>(){
				@Override
				public int compare(final Pair<Clan, Integer> o1, final Pair<Clan, Integer> o2)
				{
					return o1.first.clanID().compareTo(o2.first.clanID());
				}
			});
			for(final Pair<Clan,Integer> p : deltas[0])
			{
				DB.updateWithClobs("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO, CMCLSTS) "
								+ "values (?,?,?,?)",name,p.first.clanID(),p.second.toString(),"");
			}
			for(final Pair<Clan,Integer> p : deltas[1])
				DB.updateWithClobs("DELETE FROM CMCHCL WHERE CMUSERID=? AND CMCLAN=?",name,p.first.clanID());
			for(final Pair<Clan,Integer> np : newClans)
			{
				for(final Pair<Clan,Integer> op : oldClans)
				{
					if(np.first.clanID().equalsIgnoreCase(op.first.clanID())
					&&(np.second.intValue()!=op.second.intValue()))
						DB.updateWithClobs("UPDATE CMCHCL SET CMCLRO=? WHERE CMUSERID=? AND CMCLAN=?",np.second.toString(),name,np.first.clanID());
				}
			}
			break;
		}
		case COLOR:
			updateCMCHARString(name, "CMCOLR", value);
			break;
		case DEITY:
			updateCMCHARString(name, "CMWORS", value);
			break;
		case EMAIL:
			updateCMCHARString(name, "CMEMAL", value);
			break;
		case EXPERIENCE:
			updateCMCHARLong(name, "CMEXPE", value);
			break;
		case HEIGHT:
			updateCMCHARLong(name, "CMHEIT", value);
			break;
		case HITPOINTS:
			updateCMCHARLong(name, "CMHITP", value);
			break;
		case LASTDATE:
			updateCMCHARLong(name, "CMDATE", value);
			break;
		case LASTIP:
			updateCMCHARString(name, "CMLSIP", value);
			break;
		case LEIGE:
			updateCMCHARString(name, "CMLEIG", value);
			break;
		case LOCATION:
		{
			String roomID=queryCMCHARStr(name,"CMROID");
			if(roomID==null)
				roomID="";
			final int x=roomID.indexOf("||");
			if(x>=0)
				roomID = roomID.substring(0,x)+"||"+(String)value;
			else
				roomID=(String)value;
			updateCMCHARString(name, "CMROID", roomID);
			break;
		}
		case MANA:
			updateCMCHARLong(name, "CMMANA", value);
			break;
		case MATTRIB:
			updateCMCHARLong(name, "CMBTMP", value);
			break;
		case MOVES:
			updateCMCHARLong(name, "CMMOVE", value);
			break;
		case PASSWORD:
			break; // nu uh
		case PRACTICES:
			updateCMCHARLong(name, "CMPRAC", value);
			break;
		case QUESTPOINTS:
			updateCMCHARLong(name, "CMQUES", value);
			break;
		case STARTROOM:
		{
			String roomID=queryCMCHARStr(name,"CMROID");
			if(roomID==null)
				roomID="";
			final int x=roomID.indexOf("||");
			if(x>=0)
				roomID = ((String)value)+"||"+roomID.substring(x+2);
			else
				roomID=(String)value;
			updateCMCHARString(name, "CMROID", roomID);
			break;
		}
		case TRAINS:
			updateCMCHARLong(name, "CMTRAI", value);
			break;
		case WEIGHT:
			updateCMCHARLong(name, "CMWEIT", value);
			break;
		case WIMP:
			updateCMCHARLong(name, "CMWIMP", value);
			break;
		}
	}

	public PairList<String,Integer> DBReadPlayerClans(String name)
	{
		DBConnection D=null;
		name = DB.injectionClean(name);
		final PairList<String,Integer> list = new PairVector<String,Integer>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+name+"'");
			while(R.next())
			{
				final String clanID=DBConnections.getRes(R,"CMCLAN");
				final int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
				final Clan C=CMLib.clans().getClan(clanID);
				if(C!=null)
					list.add(C.clanID(), Integer.valueOf(clanRole));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;
	}


	public int DBReadPlayerBitmap(String name)
	{
		if((name==null)||(name.length()==0))
			return -1;
		DBConnection D=null;
		name = DB.injectionClean(name);
		int bitmap=-1;
		try
		{
			D=DB.DBFetch();

			final ResultSet R=D.query("SELECT CMBTMP FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R.next())
				bitmap = CMath.s_int(DBConnections.getRes(R,"CMBTMP"));
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return bitmap;
	}

	protected Item DBBuildItemFromData(final String[] data)
	{
		 //String[] (dbid, item class, item txt, loID, worn, uses, lvl, abilty, heit)
		final Item I=CMClass.getItem(data[1]);
		I.setDatabaseID(data[0]);
		I.setMiscText(data[2]);
		I.wearAt(CMath.s_int(data[4]));
		I.setUsesRemaining(CMath.s_int(data[5]));
		I.basePhyStats().setLevel(CMath.s_int(data[6]));
		I.basePhyStats().setAbility(CMath.s_int(data[7]));
		I.basePhyStats().setHeight(CMath.s_int(data[8]));
		I.recoverPhyStats();
		return I;
	}

	public List<String[]> DBReadPlayerItemData(String name, final Filterer<Pair<String,String>> classLocFilter, final Filterer<String> textFilter)
	{
		final List<String[]> items=new Vector<String[]>();
		if((name==null)||(name.length()==0))
			return items;
		name=CMStrings.capitalizeAndLower(DB.injectionClean(name));
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+name+"'");
			while(R.next())
			{
				final String loID=DBConnections.getRes(R,"CMITLO");
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItemPrototype(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				if((classLocFilter==null)||(classLocFilter.passesFilter(new Pair<String,String>(itemID,loID))))
				{
					final String dbID=DBConnections.getRes(R,"CMITNM");
					final String text=DBConnections.getResQuietly(R,"CMITTX");
					if((text != null)
					&&((textFilter==null)||(textFilter.passesFilter(text))))
					{
						items.add(new String[]
							{dbID, itemID, text, loID,
							""+DBConnections.getLongRes(R, "CMITWO"),
							""+DBConnections.getLongRes(R, "CMITUR"),
							""+DBConnections.getLongRes(R, "CMITLV"),
							""+DBConnections.getLongRes(R, "CMITAB"),
							""+DBConnections.getLongRes(R, "CMHEIT")});
					}
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return items;
	}

	public MOB DBRead(final String name)
	{
		if((name==null)||(name.length()==0))
			return null;
		if(emptyRoom==null)
			emptyRoom=CMClass.getLocale("StdRoom");
		final String[] oldLocID=new String[1];
		if(CMLib.players().getPlayer(name)!=null) // super important to stay this t-group
			return CMLib.players().getPlayer(name);
		final MOB mob=DBReadUserOnly(name,oldLocID);
		if(mob == null)
			return null;
		final int oldDisposition=mob.basePhyStats().disposition();
		mob.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		mob.phyStats().setDisposition(PhyStats.IS_NOT_SEEN|PhyStats.IS_SNEAKING);
		CMLib.players().addPlayer(mob);
		mob.recoverPhyStats();
		mob.recoverCharStats();
		Room prevRoom=mob.location();
		boolean inhab=false;
		if(prevRoom!=null)
			inhab=prevRoom.isInhabitant(mob);
		mob.setLocation(mob.getStartRoom());
		DBConnection D=null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHIT WHERE CMUSERID='"+mob.Name()+"'");
			final Map<String,Item> itemNums=new HashMap<String,Item>();
			final Map<Item,String> itemLocs=new HashMap<Item,String>();
			Room extraContentR=null;
			while(R.next())
			{
				final String itemNum=DBConnections.getRes(R,"CMITNM");
				final String itemID=DBConnections.getRes(R,"CMITID");
				final Item newItem=CMClass.getItem(itemID);
				if(newItem==null)
					Log.errOut("MOB","Couldn't find item '"+itemID+"'");
				else
				{
					itemNums.put(itemNum,newItem);
					boolean addToMOB=true;
					String text=DBConnections.getResQuietly(R,"CMITTX");
					int roomX;
					if(text.startsWith("<ROOM") && ((roomX=text.indexOf("/>"))>=0))
					{
						final String roomXML=text.substring(0,roomX+2);
						text=text.substring(roomX+2);
						int roomY;
						if(text.startsWith("<AROOM")
						&&((roomY=text.indexOf("</AROOM>"))>=0))
						{
							final String addOnXml=text.substring(0,roomY+8);
							text=text.substring(roomY+8);
							extraContentR=(Room)CMLib.coffeeMaker().unpackUnknownFromXML(addOnXml);
						}
						newItem.setMiscText(text);
						final List<XMLLibrary.XMLTag> xml=CMLib.xml().parseAllXML(roomXML);
						if((xml!=null)&&(xml.size()>0))
						{
							final String roomID=xml.get(0).parms().get("ID");
							final long expirationDate=CMath.s_long(xml.get(0).parms().get("EXPIRE"));
							if(roomID.startsWith("SPACE.") && (newItem instanceof SpaceObject))
							{
								CMLib.space().addObjectToSpace((SpaceObject)newItem,CMParms.toLongArray(CMParms.parseCommas(roomID.substring(6), true)));
								addToMOB=false;
							}
							else
							{
								final Room itemR=CMLib.map().getRoom(roomID);
								if(itemR!=null)
								{
									if(newItem instanceof Boardable)
										((Boardable)newItem).dockHere(itemR);
									else
										itemR.addItem(newItem);
									newItem.setExpirationDate(expirationDate);
									addToMOB=false;
								}
								else
								if(newItem instanceof Boardable)
								{
									Log.errOut("Destroying "+newItem.name()+" on "+name+" because it has an invalid location '"+roomID+"'.");
									newItem.destroy();
									continue;
								}
							}
						}
					}
					else
					{
						newItem.setMiscText(text);
					}
					if((prevRoom==null)
					&&(newItem instanceof Boardable))
					{
						final Area area=((Boardable)newItem).getArea();
						if(area != null)
							prevRoom=area.getRoom(oldLocID[0]);
					}
					final String loc=DBConnections.getResQuietly(R,"CMITLO");
					if(loc.length()>0)
					{
						final Item container=itemNums.get(loc);
						if(container instanceof Container)
							newItem.setContainer((Container)container);
						else
							itemLocs.put(newItem,loc);
					}
					newItem.wearAt((int)DBConnections.getLongRes(R,"CMITWO"));
					newItem.setUsesRemaining((int)DBConnections.getLongRes(R,"CMITUR"));
					newItem.basePhyStats().setLevel((int)DBConnections.getLongRes(R,"CMITLV"));
					newItem.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMITAB"));
					newItem.basePhyStats().setHeight((int)DBConnections.getLongRes(R,"CMHEIT"));
					newItem.recoverPhyStats();
					if(addToMOB)
					{
						mob.addItem(newItem);
						if(newItem instanceof Boardable)
							CMLib.map().registerWorldObjectLoaded(null, null, newItem);
					}
					else
					{
						CMLib.map().registerWorldObjectLoaded(null, null, newItem);
						mob.playerStats().getExtItems().addItem(newItem);
						if((newItem instanceof DeadBody) // legacy fix
						&&(((DeadBody)newItem).isPlayerCorpse())
						&&(newItem.isSavable()))
							newItem.setSavable(false); // so the rooms dont save it.
					}
					if(extraContentR!=null)
					{
						final Room itemR=CMLib.map().roomLocation(newItem);
						if(itemR!=null)
						{
							Rideable leadR=null;
							for(final Enumeration<MOB> m=extraContentR.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M instanceof Rideable)&&(leadR==null))
									leadR=(Rideable)M;
								M.setSavable(false);
								if(M.location()!=itemR)
									itemR.bringMobHere(M, true);
							}
							for(final Enumeration<Item> i=extraContentR.items();i.hasMoreElements();)
							{
								final Item I=i.nextElement();
								if((I instanceof Rideable)&&(leadR==null))
									leadR=(Rideable)I;
								I.setSavable(false);
								if(I.owner()!=itemR)
									itemR.moveItemTo(I, Expire.Never, Move.Followers);
							}
							if((leadR!=null)&&(newItem instanceof Rider))
								((Rider)newItem).setRiding(leadR);
						}
						extraContentR.destroy();
						extraContentR=null;
					}
				}
			}
			for(final Item keyItem : itemLocs.keySet())
			{
				final String location=itemLocs.get(keyItem);
				final Item container=itemNums.get(location);
				if(container instanceof Container)
				{
					keyItem.setContainer((Container)container);
					keyItem.recoverPhyStats();
					container.recoverPhyStats();
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		if(prevRoom!=null)
		{
			mob.setLocation(prevRoom);
			if(inhab&&(!prevRoom.isInhabitant(mob)))
				prevRoom.addInhabitant(mob);
		}
		else
		if((mob.location()!=null)
		&&((inhab&&(!mob.location().isInhabitant(mob)))))
			mob.location().addInhabitant(mob);
		// now grab the abilities
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'");
			while(R.next())
			{
				final String abilityID=DBConnections.getRes(R,"CMABID");
				int proficiency=(int)DBConnections.getLongRes(R,"CMABPF");
				if((proficiency==Integer.MIN_VALUE)||(proficiency==Integer.MIN_VALUE+1))
				{
					if(abilityID.equalsIgnoreCase("ScriptingEngine"))
					{
						if(CMClass.getCommon("DefaultScriptingEngine")==null)
							Log.errOut("MOB","Couldn't find scripting engine!");
						else
						{

							final String xml=DBConnections.getRes(R,"CMABTX");
							if(xml.length()>0)
								CMLib.coffeeMaker().unpackGenScriptsXML(mob,CMLib.xml().parseAllXML(xml),true);
						}
					}
					else
					{
						final Behavior newBehavior=CMClass.getBehavior(abilityID);
						if(newBehavior==null)
							Log.errOut("MOB","Couldn't find behavior '"+abilityID+"'");
						else
						{
							newBehavior.setParms(DBConnections.getRes(R,"CMABTX"));
							mob.addBehavior(newBehavior);
						}
					}
				}
				else
				{
					final Ability newAbility=CMClass.getRawAbility(abilityID);
					if(newAbility==null)
						Log.errOut("MOB","Couldn't find ability '"+abilityID+"'");
					else
					{
						if((proficiency<0)||(proficiency==Integer.MAX_VALUE))
						{
							if(proficiency==Integer.MAX_VALUE)
							{
								newAbility.setProficiency(CMLib.ableMapper().getMaxProficiency(newAbility.ID()));
								mob.addNonUninvokableEffect(newAbility);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							}
							else
							{
								proficiency=proficiency+200;
								newAbility.setProficiency(proficiency);
								newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
								final Ability newAbility2=(Ability)newAbility.copyOf();
								mob.addNonUninvokableEffect(newAbility);
								mob.addAbility(newAbility2);
							}
						}
						else
						{
							newAbility.setProficiency(proficiency);
							newAbility.setMiscText(DBConnections.getRes(R,"CMABTX"));
							mob.addAbility(newAbility);
						}
					}
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		D=null;
		mob.basePhyStats().setDisposition(oldDisposition);
		mob.recoverPhyStats();
		if(mob.baseCharStats()!=null)
		{
			mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
			final int oldWeight=mob.basePhyStats().weight();
			final int oldHeight=mob.basePhyStats().height();
			mob.baseCharStats().getMyRace().startRacing(mob,true);
			if(oldWeight>0)
				mob.basePhyStats().setWeight(oldWeight);
			if(oldHeight>0)
				mob.basePhyStats().setHeight(oldHeight);
		}
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		CMLib.threads().suspendResumeRecurse(mob, false, true);
		return mob;
	}

	public List<String> getUserList()
	{
		DBConnection D=null;
		final Vector<String> V=new Vector<String>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final String username=DBConnections.getRes(R,"CMUSERID");
				V.addElement(username);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}

	protected PlayerLibrary.ThinPlayer parseThinUser(final ResultSet R)
	{
		try
		{
			final String name=DBConnections.getRes(R,"CMUSERID");
			String cclass=DBConnections.getRes(R,"CMCLAS");
			final int x=cclass.lastIndexOf(';');
			CharClass C=null;
			if((x>0)&&(x<cclass.length()-2))
			{
				C=CMClass.getCharClass(cclass.substring(x+1));
				if(C!=null)
				{
					if(C.nameSet().length>1)
						cclass=C.ID();
					else
						cclass=C.name();
				}
			}
			final String charClass=(cclass);
			final String rrace=DBConnections.getRes(R,"CMRACE");
			final Race R2=CMClass.getRace(rrace);
			final String race;
			if(R2!=null)
				race=(R2.name());
			else
				race=rrace;
			final List<String> lvls=CMParms.parseSemicolons(DBConnections.getRes(R,"CMLEVL"), true);
			int calcLevel=0;
			for(final String lvl : lvls)
				calcLevel+=CMath.s_int(lvl);
			final int level = calcLevel;
			final int age=(int)DBConnections.getLongRes(R,"CMAGEH");
			final MOB M=CMLib.players().getPlayer(name);
			final long last;
			if((M!=null)&&(M.lastTickedDateTime()>0))
				last=M.lastTickedDateTime();
			else
				last=DBConnections.getLongRes(R,"CMDATE");
			final String lsIP=DBConnections.getRes(R,"CMLSIP");
			final String email=DBConnections.getRes(R,"CMEMAL");
			final String ip=lsIP;
			final int exp=CMath.s_int(DBConnections.getRes(R,"CMEXPE"));
			final int expLvl=CMath.s_int(DBConnections.getRes(R,"CMEXLV"));
			final String leigeID=DBConnections.getRes(R,"CMLEIG");
			final String worshipID=DBConnections.getRes(R,"CMWORS");
			final char gendChar=DBConnections.getRes(R, "CMGEND").charAt(0);
			return new PlayerLibrary.ThinPlayer()
			{
				XVector<String> clans = null;
				@Override
				public String name()
				{
					return name;
				}

				@Override
				public String charClass()
				{
					return charClass;
				}

				@Override
				public String race()
				{
					return race;
				}

				@Override
				public int level()
				{
					return level;
				}

				@Override
				public int age()
				{
					return age;
				}

				@Override
				public long last()
				{
					return last;
				}

				@Override
				public String email()
				{
					return email;
				}

				@Override
				public String ip()
				{
					return ip;
				}

				@Override
				public int exp()
				{
					return exp;
				}

				@Override
				public int expLvl()
				{
					return expLvl;
				}

				@Override
				public String liege()
				{
					return leigeID;
				}

				@Override
				public String worship()
				{
					return worshipID;
				}

				@Override
				public String gender()
				{
					final String[] set = CMProps.getGenderDef(gendChar);
					return set[1]; // GEND_NOUN
				}

				@Override
				public Enumeration<String> clans()
				{
					if(clans==null)
						clans = new XVector<String>(CMLib.database().DBReadMemberClans(name));
					return clans.elements();
				}
			};
		}
		catch(final Exception e)
		{
			Log.errOut("MOBloader",e);
		}
		return null;
	}

	public PlayerLibrary.ThinPlayer getThinUser(String name)
	{
		DBConnection D=null;
		PlayerLibrary.ThinPlayer thisUser=null;
		name=DB.injectionClean(name);
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
				thisUser=parseThinUser(R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return thisUser;
	}

	public List<PlayerLibrary.ThinPlayer> getExtendedUserList()
	{
		DBConnection D=null;
		final Vector<PlayerLibrary.ThinPlayer> allUsers=new Vector<PlayerLibrary.ThinPlayer>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final PlayerLibrary.ThinPlayer thisUser=parseThinUser(R);
				if(thisUser != null)
					allUsers.addElement(thisUser);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return allUsers;
	}

	public List<PlayerLibrary.ThinPlayer> vassals(String liegeName)
	{
		DBConnection D=null;
		final List<PlayerLibrary.ThinPlayer> list=new ArrayList<PlayerLibrary.ThinPlayer>();
		liegeName=DB.injectionClean(liegeName);
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMLEIG='"+liegeName+"'");
			if(R!=null)
			{
				while(R.next())
					list.add(this.parseThinUser(R));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return list;
	}

	public List<PlayerLibrary.ThinPlayer> worshippers(String deityID)
	{
		DBConnection D=null;
		final List<PlayerLibrary.ThinPlayer> DV=new Vector<PlayerLibrary.ThinPlayer>();
		try
		{
			D=DB.DBFetch();
			deityID=DB.injectionClean(deityID);
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMWORS='"+deityID+"'");
			if(R!=null) while(R.next())
			{
				DV.add(parseThinUser(R));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return DV;
	}

	public List<MOB> DBScanFollowers(String name)
	{
		DBConnection D=null;
		final Vector<MOB> V=new Vector<MOB>();
		// now grab the followers
		try
		{
			D=DB.DBFetch();
			final ResultSet R;
			if(name == null)
				R=D.query("SELECT * FROM CMCHFO");
			else
			{
				name=DB.injectionClean(name);
				R=D.query("SELECT * FROM CMCHFO WHERE CMUSERID='"+name+"'");
			}
			while(R.next())
			{
				final String MOBID=DBConnections.getRes(R,"CMFOID");
				final MOB newMOB=CMClass.getMOB(MOBID);
				if(newMOB==null)
					Log.errOut("MOB","Couldn't find MOB '"+MOBID+"'");
				else
				{
					newMOB.setMiscText(DBConnections.getResQuietly(R,"CMFOTX"));
					newMOB.basePhyStats().setLevel(((int)DBConnections.getLongRes(R,"CMFOLV")));
					newMOB.basePhyStats().setAbility((int)DBConnections.getLongRes(R,"CMFOAB"));
					newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					newMOB.recoverPhyStats();
					newMOB.recoverCharStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					V.addElement(newMOB);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return V;
	}

	public void DBReadFollowers(final MOB mob, final boolean bringToLife)
	{
		Room location=mob.location();
		if(location==null)
			location=mob.getStartRoom();
		final List<MOB> V=DBScanFollowers(mob.Name());
		for(int v=0;v<V.size();v++)
		{
			final MOB newMOB=V.get(v);
			final Room room=(location==null)?newMOB.getStartRoom():location;
			newMOB.setStartRoom(room);
			newMOB.setLocation(room);
			newMOB.setFollowing(mob);
			if((newMOB.getStartRoom()!=null)
			&&(CMLib.law().doesHavePriviledgesHere(mob,newMOB.getStartRoom()))
			&&((newMOB.location()==null)
				||(!CMLib.law().doesHavePriviledgesHere(mob,newMOB.location()))))
			{
				newMOB.setLocation(newMOB.getStartRoom());
			}
			if(bringToLife)
			{
				newMOB.bringToLife(mob.location(),true);
				mob.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,CMLib.lang().L("<S-NAME> appears!"));
			}
		}
	}

	public void DBUpdateEmail(final MOB mob)
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return;
		final String name=DB.injectionClean(mob.Name());
		final String email=DB.injectionClean(pstats.getEmail());
		DB.update("UPDATE CMCHAR SET CMEMAL='"+email+"' WHERE CMUSERID='"+name+"'");
	}

	private MemberRecord BuildClanMemberRecord(final ResultSet R)
	{
		final String username=DB.getRes(R,"CMUSERID");
		final int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
		int mobpvps=0;
		int playerpvps=0;
		long donatedXP=0;
		long joinDate=0;
		double donatedGold=0;
		double dues=0;
		final String stats=DB.getRes(R,"CMCLSTS");
		if(stats!=null)
		{
			final String[] splitstats=stats.split(";");
			if(splitstats.length>0)
				mobpvps=CMath.s_int(splitstats[0]);
			if(splitstats.length>1)
				playerpvps=CMath.s_int(splitstats[1]);
			if(splitstats.length>2)
				donatedGold=CMath.s_double(splitstats[2]);
			if(splitstats.length>3)
				donatedXP=CMath.s_long(splitstats[3]);
			if(splitstats.length>4)
				joinDate=CMath.s_long(splitstats[4]);
			if(splitstats.length>5)
				dues=CMath.s_double(splitstats[5]);
		}
		final Clan.MemberRecord mR=new Clan.MemberRecord(username,clanRole);
		mR.mobpvps=mobpvps;
		mR.playerpvps=playerpvps;
		mR.donatedGold=donatedGold;
		mR.donatedXP=donatedXP;
		mR.joinDate=joinDate;
		mR.dues=dues;
		return mR;
	}

	public MemberRecord DBGetClanMember(String clan, String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			name=DB.injectionClean(name);
			clan=DB.injectionClean(clan);
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
			{
				return BuildClanMemberRecord(R);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public List<Clan.MemberRecord> DBClanMembers(String clan)
	{
		final List<Clan.MemberRecord> members = new Vector<Clan.MemberRecord>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			clan=DB.injectionClean(clan);
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"'");
			if(R!=null) while(R.next())
			{
				members.add(BuildClanMemberRecord(R));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return members;
	}


	public List<String> DBMemberClans(String userID)
	{
		final List<String> clans = new Vector<String>(1);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			userID=DB.injectionClean(userID);
			final ResultSet R=D.query("SELECT CMCLAN FROM CMCHCL where CMUSERID='"+userID+"'");
			if(R!=null) while(R.next())
			{
				clans.add(DB.getRes(R, "CMCLAN"));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return clans;
	}

	public Clan.MemberRecord DBClanMember(String clan, String memberName)
	{
		Clan.MemberRecord member = null;
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			clan=DB.injectionClean(clan);
			memberName=DB.injectionClean(CMStrings.capitalizeAndLower(memberName));
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' AND CMUSERID='"+memberName+"'");
			if(R!=null)
			{
				member = BuildClanMemberRecord(R);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return member;
	}

	public void DBUpdateClanMembership(String name, String clan, final int role)
	{
		final MOB M=CMLib.players().getPlayer(name);
		if(M!=null)
		{
			M.setClan(clan, role);
		}
		DBConnection D=null;
		try
		{
			name=DB.injectionClean(name);
			clan=DB.injectionClean(clan);
			D=DB.DBFetch();
			if(role<0)
			{
				DB.update("DELETE FROM CMCHCL WHERE CMUSERID='"+name+"' AND CMCLAN='"+clan+"'");
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
					Log.debugOut("User '"+name+"' was deleted from clan '"+clan+"'");
			}
			else
			{
				final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
				if((R!=null) && (R.next()))
				{
					final int clanRole = (int)DBConnections.getLongRes(R,"CMCLRO");
					R.close();
					if(clanRole == role)
						return;
					D.update("UPDATE CMCHCL SET CMCLRO="+role+" where CMCLAN='"+clan+"' and CMUSERID='"+name+"'", 0);
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
						Log.debugOut("User '"+name+"' had role in clan '"+clan+"' changed from "+clanRole+" to "+role);
				}
				else
				{
					final String newStats="0;0;0;0;"+System.currentTimeMillis()+";0";
					D.update("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO, CMCLSTS) values ('"+name+"','"+clan+"',"+role+",'"+newStats+"')",0);
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
						Log.debugOut("User '"+name+"' was inserted into clan '"+clan+"' as role "+role);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdateClanKills(String clan, String name, final int adjMobKills, final int adjPlayerKills)
	{
		if(((adjMobKills==0)&&(adjPlayerKills==0))
		||(clan==null)
		||(name==null))
			return;

		name=DB.injectionClean(name);
		clan=DB.injectionClean(clan);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
			MemberRecord M=null;
			if(R!=null)
			{
				if(R.next())
				{
					M=BuildClanMemberRecord(R);
					R.close();
					M.mobpvps+=adjMobKills;
					M.playerpvps+=adjPlayerKills;
					final String newStats=M.mobpvps+";"+M.playerpvps+";"+M.donatedGold+";"+M.donatedXP+";"+M.joinDate+";"+M.dues;
					D.update("UPDATE CMCHCL SET CMCLSTS='"+newStats+"' where CMCLAN='"+clan+"' and CMUSERID='"+name+"'", 0);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdateClanDonates(String clan, String name, final double adjGold, final int adjXP, final double adjDues)
	{
		if(((adjGold==0)&&(adjXP==0)&&(adjDues==0))
		||(clan==null)
		||(name==null))
			return;

		name=DB.injectionClean(name);
		clan=DB.injectionClean(clan);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL where CMCLAN='"+clan+"' and CMUSERID='"+name+"'");
			MemberRecord M=null;
			if(R!=null)
			{
				if(R.next())
				{
					M=BuildClanMemberRecord(R);
					R.close();
					M.donatedGold+=adjGold;
					M.donatedXP+=adjXP;
					M.dues+=adjDues;
					final String newStats=M.mobpvps+";"+M.playerpvps+";"+M.donatedGold+";"+M.donatedXP+";"+M.joinDate+";"+M.dues;
					D.update("UPDATE CMCHCL SET CMCLSTS='"+newStats+"' where CMCLAN='"+clan+"' and CMUSERID='"+name+"'", 0);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBUpdate(final MOB mob)
	{
		final PlayerStats pStats = mob.playerStats();
		if((pStats==null)||(!pStats.isSavable()))
			return;
		DBUpdateJustMOB(mob);
		if(mob.Name().length()==0)
			return;
		DBUpdateItems(mob);
		DBUpdateAbilities(mob);
		pStats.setLastUpdated(System.currentTimeMillis());
		final PlayerAccount account = pStats.getAccount();
		if(account != null)
		{
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdatePassword(String name, String password)
	{
		name=CMStrings.capitalizeAndLower(DB.injectionClean(name));
		password=DB.injectionClean(password);
		DB.update("UPDATE CMCHAR SET CMPASS='"+password+"' WHERE CMUSERID='"+name+"'");
	}

	private String getPlayerStatsXML(final MOB mob)
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return "";
		final StringBuilder pfxml=new StringBuilder(pstats.getXML());
		if(mob.tattoos().hasMoreElements())
		{
			pfxml.append("<TATTS>");
			for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
				pfxml.append(e.nextElement().toString()+";");
			pfxml.append("</TATTS>");
		}
		if(mob.expertises().hasMoreElements())
		{
			pfxml.append("<EDUS>");
			for(final Enumeration<String> x=mob.expertises();x.hasMoreElements();)
				pfxml.append(x.nextElement()).append(';');
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));
		return pfxml.toString();
	}

	public void DBUpdateJustPlayerStats(final MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}
		final PlayerStats pstats=mob.playerStats();
		if((pstats==null)||(!pstats.isSavable()))
			return;
		final String pfxml=getPlayerStatsXML(mob);
		final String name=DB.injectionClean(mob.Name());
		DB.updateWithClobs("UPDATE CMCHAR SET CMPFIL=? WHERE CMUSERID='"+name+"'", pfxml.toString());
	}

	public void DBUpdateJustMOB(final MOB mob)
	{
		if(mob.Name().length()==0)
		{
			DBCreateCharacter(mob);
			return;
		}
		final PlayerStats pstats=mob.playerStats();
		if((pstats==null)||(!pstats.isSavable()))
			return;
		final String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";

		if((mob.location()!=null)
		&&(mob.location().getArea()!=null)
		&&(CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_PARENT)
			||CMath.bset(mob.location().getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
			strOtherRoomID=strStartRoomID;

		final String playerStatsXML=getPlayerStatsXML(mob);
		final String factionDataXML=CMLib.coffeeMaker().getFactionXML(mob, null).toString();
		DB.updateWithClobs(
				 "UPDATE CMCHAR SET  CMPASS='"+pstats.getPasswordStr()+"'"
				+", CMCHID='"+mob.ID()+"'"
				+", CMCLAS='"+mob.baseCharStats().getMyClassesStr()+"'"
				+", CMSTRE="+mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)
				+", CMRACE='"+mob.baseCharStats().getMyRace().ID()+"'"
				+", CMDEXT="+mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)
				+", CMCONS="+mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)
				+", CMGEND='"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))+"'"
				+", CMWISD="+mob.baseCharStats().getStat(CharStats.STAT_WISDOM)
				+", CMINTE="+mob.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE)
				+", CMCHAR="+mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)
				+", CMHITP="+mob.baseState().getHitPoints()
				+", CMLEVL='"+mob.baseCharStats().getMyLevelsStr()+"'"
				+", CMMANA="+mob.baseState().getMana()
				+", CMMOVE="+mob.baseState().getMovement()
				+", CMALIG=-1"
				+", CMEXPE="+mob.getExperience()
				+", CMEXLV="+mob.getExpNextLevel()
				+", CMWORS='"+mob.baseCharStats().getWorshipCharID()+"'"
				+", CMPRAC="+mob.getPractices()
				+", CMTRAI="+mob.getTrains()
				+", CMAGEH="+mob.getAgeMinutes()
				+", CMGOLD="+mob.getMoney()
				+", CMWIMP="+mob.getWimpHitPoint()
				+", CMQUES="+mob.getQuestPoint()
				+", CMROID='"+strStartRoomID+"||"+strOtherRoomID+"'"
				+", CMDATE='"+pstats.getLastDateTime()+"'"
				+", CMCHAN="+pstats.getChannelMask()
				+", CMATTA="+mob.basePhyStats().attackAdjustment()
				+", CMAMOR="+mob.basePhyStats().armor()
				+", CMDAMG="+mob.basePhyStats().damage()
				+", CMBTMP="+mob.getAttributesBitmap()
				+", CMLEIG='"+mob.getLiegeID()+"'"
				+", CMHEIT="+mob.basePhyStats().height()
				+", CMWEIT="+mob.basePhyStats().weight()
				+", CMPRPT=?"
				+", CMCOLR=?"
				+", CMLSIP='"+pstats.getLastIP()+"'"
				+", CMEMAL=?"
				+", CMPFIL=?"
				+", CMSAVE=?"
				+", CMMXML=?"
				+", CMDESC=?"
				+"  WHERE CMUSERID='"+mob.Name()+"'"
				,new String[][]{{
					pstats.getPrompt(),
					pstats.getColorStr(),
					pstats.getEmail(),
					playerStatsXML,
					mob.baseCharStats().getNonBaseStatsAsString(),
					factionDataXML,
					mob.description()+" "
				}}
				);
		final List<String> clanStatements=new LinkedList<String>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"'");
			final Set<String> savedClans=new HashSet<String>();
			if(R!=null)
			{
				while(R.next())
				{
					final String clanID=DB.getRes(R,"CMCLAN");
					final Pair<Clan,Integer> role=mob.getClanRole(clanID);
					if(role==null)
					{
						clanStatements.add("DELETE FROM CMCHCL WHERE CMUSERID='"+mob.Name()+"' AND CMCLAN='"+clanID+"'");
						if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
							Log.debugOut("User '"+mob.Name()+"' was deleted from clan '"+clanID+"'");
					}
					else
					{
						final MemberRecord M=BuildClanMemberRecord(R);
						if(role.second.intValue()!=M.role)
						{
							clanStatements.add("UPDATE CMCHCL SET CMCLRO="+role.second.intValue()+" where CMCLAN='"+clanID+"' and CMUSERID='"+mob.Name()+"'");
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
								Log.debugOut("User '"+mob.Name()+"' had role in clan '"+clanID+"' changed from "+M.role+" to role "+role.second.intValue());
						}
					}
					savedClans.add(clanID.toUpperCase());
				}
				R.close();
			}
			for(final Pair<Clan,Integer> p : mob.clans())
			{
				if(!savedClans.contains(p.first.clanID().toUpperCase()))
				{
					final String newStats="0;0;0;0;"+System.currentTimeMillis()+";0";
					clanStatements.add("INSERT INTO CMCHCL (CMUSERID, CMCLAN, CMCLRO, CMCLSTS) "
							+ "values ('"+mob.Name()+"','"+p.first.clanID()+"',"+p.second.intValue()+",'"+newStats+"')");
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.CLANMEMBERS))
						Log.debugOut("User '"+mob.Name()+"' was added to clan '"+p.first.clanID()+"' as role "+p.second.intValue());
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if(clanStatements.size()>0)
			DB.update(clanStatements.toArray(new String[0]));
	}

	protected String getShortID(final Environmental E)
	{
		final String classID = ""+E;
		final int x=classID.indexOf('@');
		if(x<0)
			return E.ID()+"@"+classID.hashCode()+Math.random();
		else
			return E.ID()+classID.substring(0, x).hashCode()+classID.substring(x);
	}

	protected String getDBItemUpdateString(final MOB mob, final Item thisItem)
	{
		CMLib.catalog().updateCatalogIntegrity(thisItem);
		final String container=((thisItem.container()!=null)?(""+getShortID(thisItem.container())):"");
		final String name=DB.injectionClean(mob.Name());
		final String itemID=getShortID(thisItem);
		thisItem.setDatabaseID(itemID);
		return "INSERT INTO CMCHIT (CMUSERID, CMITNM, CMITID, CMITTX, CMITLO, CMITWO, "
			+"CMITUR, CMITLV, CMITAB, CMHEIT"
			+") values ('"+name+"','"+(itemID)+"','"+thisItem.ID()+"',?,'"+container+"',"+thisItem.rawWornCode()+","
		+thisItem.usesRemaining()+","+thisItem.basePhyStats().level()+","+thisItem.basePhyStats().ability()+","
		+thisItem.basePhyStats().height()+")";
	}

	protected DBPreparedBatchEntry doBulkInsert(final StringBuilder str, final List<String> clobs, final String sql, final String clob)
	{
		if(str.length()==0)
		{
			str.append(sql);
			clobs.add(clob+" ");
		}
		else
		if(str.length()<512000)
		{
			final int x=sql.indexOf(" values ");
			str.append(", ").append(sql.substring(x+7));
			clobs.add(clob+" ");
		}
		else
		{
			final DBPreparedBatchEntry entry = new DBPreparedBatchEntry(str.toString(),clobs.toArray(new String[0]));
			str.setLength(0);
			return entry;
		}
		return null;
	}

	private List<DBPreparedBatchEntry> getDBItemUpdateStrings(final MOB mob)
	{
		final HashSet<Item> done=new HashSet<Item>();
		final List<DBPreparedBatchEntry> strings=new LinkedList<DBPreparedBatchEntry>();
		final StringBuilder bulkSQL=new StringBuilder("");
		final List<String> bulkClobs=new ArrayList<String>();
		final boolean useBulkInserts = DB.useBulkInserts();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item thisItem=mob.getItem(i);
			if((thisItem!=null)
			&&(!done.contains(thisItem))
			&&(thisItem.isSavable()))
			{
				CMLib.catalog().updateCatalogIntegrity(thisItem);
				final String sql=getDBItemUpdateString(mob,thisItem);
				if(!useBulkInserts)
					strings.add(new DBPreparedBatchEntry(sql,thisItem.text()+" "));
				else
				{
					final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,thisItem.text()+" ");
					if(entry != null)
						strings.add(entry);
				}
				done.add(thisItem);
			}
		}
		final PlayerStats pStats=mob.playerStats();
		if(pStats !=null)
		{
			final ItemCollection coll=pStats.getExtItems();
			final List<Item> finalCollection=new LinkedList<Item>();
			final List<Item> extraItems=new LinkedList<Item>();
			for(int i=coll.numItems()-1;i>=0;i--)
			{
				final Item thisItem=coll.getItem(i);
				if(thisItem!=null)
				{
					if(!thisItem.amDestroyed())
					{
						final Item cont=thisItem.ultimateContainer(null);
						if(cont.owner() instanceof Room)
							finalCollection.add(thisItem);
						else
						if((thisItem instanceof SpaceObject)
						&&(CMLib.space().isObjectInSpace((SpaceObject)thisItem)))
							finalCollection.add(thisItem);
					}
				}
			}
			for(int i=finalCollection.size()-1;i>=0;i--)
			{
				final Item thisItem = finalCollection.get(i);
				if(thisItem instanceof Container)
				{
					final List<Item> contents=((Container)thisItem).getDeepContents();
					if(thisItem instanceof DeadBody)
					{
						if((contents.size()==0)
						&&(thisItem.container()==null)
						&&(((DeadBody)thisItem).getMobName().equalsIgnoreCase(mob.Name())))
						{
							finalCollection.remove(i);
							continue;
						}
					}
					for(final Item I : contents)
					{
						if(!finalCollection.contains(I))
							extraItems.add(I);
					}
				}
			}
			finalCollection.addAll(extraItems);
			for(final Item thisItem : finalCollection)
			{
				if(!done.contains(thisItem))
				{
					CMLib.catalog().updateCatalogIntegrity(thisItem);
					final Item cont=thisItem.ultimateContainer(null);
					final String sql=getDBItemUpdateString(mob,thisItem);
					final String roomID;
					if((cont.owner()==null)
					&&(thisItem instanceof SpaceObject)
					&&(CMLib.space().isObjectInSpace((SpaceObject)thisItem)))
						roomID="SPACE."+CMParms.toListString(((SpaceObject)thisItem).coordinates());
					else
					if(cont.owner() instanceof Room)
						roomID=CMLib.map().getApproximateExtendedRoomID((Room)cont.owner());
					else
					if((thisItem instanceof SpaceObject)
					&&(CMLib.space().isObjectInSpace((SpaceObject)thisItem)))
						roomID="SPACE."+CMParms.toListString(((SpaceObject)thisItem).coordinates());
					else
						roomID="";
					String insert="";
					if((thisItem instanceof Rider)
					&&(thisItem instanceof NavigableItem)
					&&(((Rider)thisItem).riding()!=null))
					{
						final Room R=CMLib.map().roomLocation(thisItem);
						final Room fakeR=CMClass.getLocale("StoneRoom");
						Rideable leadR=((Rider)thisItem).riding();
						while(leadR != null)
						{
							if(leadR instanceof MOB)
							{
								final MOB rideM=(MOB)leadR;
								if((rideM.isMonster())
								&&((rideM.amUltimatelyFollowing()==null)||(!rideM.amUltimatelyFollowing().isPlayer()))
								&&(rideM.location()==R))
									fakeR.addInhabitant(rideM); // will not affect location
								leadR=rideM.riding();
							}
							else
							if(leadR instanceof Item)
							{
								final Item rideI=(Item)leadR;
								if((!finalCollection.contains(rideI))
								&&(rideI.owner()==R))
								{
									fakeR.addItem(rideI);
									leadR=rideI.riding();
								}
							}
							else
								break;
						}
						insert = CMLib.coffeeMaker().getUnknownXML(fakeR).toString();
						fakeR.delAllInhabitants(false);
						fakeR.delAllItems(false);
					}
					final String text="<ROOM ID=\""+roomID+"\" EXPIRE="+thisItem.expirationDate()+" />"+insert+thisItem.text();
					if(!useBulkInserts)
						strings.add(new DBPreparedBatchEntry(sql,text+" "));
					else
					{
						final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,text+" ");
						if(entry != null)
							strings.add(entry);
					}
					done.add(thisItem);
				}
			}
		}
		if((bulkSQL.length()>0) && useBulkInserts)
			strings.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		return strings;
	}

	public void DBUpdateItems(final MOB mob)
	{
		if(mob.Name().length()==0)
			return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		final String name=DB.injectionClean(mob.Name());
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHIT WHERE CMUSERID='"+name+"'"));
		statements.addAll(getDBItemUpdateStrings(mob));
		DB.updateWithClobs(statements);
	}

	public void DBScanPridePlayerWinners(final CMCallback<Pair<ThinPlayer,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		DBConnection D=null;
		final long msWait=Math.round(1000.0 * CMath.div(scanCPUPercent, 100));
		final long sleepAmount=1000 - msWait;
		long nextWaitAfter=System.currentTimeMillis() + msWait;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCHAR");
			while((R!=null)&&(R.next()))
			{
				final String userID=DB.getRes(R, "CMUSERID");
				final String pxml;
				final ThinPlayer whom;
				final MOB M=CMLib.players().getPlayer(userID);
				if((M!=null)&&(M.playerStats()!=null))
				{
					pxml=M.playerStats().getXML();
					whom=CMLib.players().getThinPlayer(userID);
				}
				else
				{
					pxml=DB.getRes(R, "CMPFIL");
					whom=this.parseThinUser(R);
				}
				final String[] pridePeriods=CMLib.xml().returnXMLValue(pxml, "NEXTPRIDEPERIODS").split(",");
				final String[] prideStats=CMLib.xml().returnXMLValue(pxml, "PRIDESTATS").split(";");
				final String[] prideExceptions=CMLib.xml().returnXMLValue(pxml, "FLAGS").split(",");
				if(CMParms.contains(prideExceptions, PlayerFlag.NOSTATS.toString())
					||CMParms.contains(prideExceptions, PlayerFlag.NOTOP.toString()))
					continue;
				final Pair<Long,int[]>[] allData = CMLib.players().parsePrideStats(pridePeriods, prideStats);
				callBack.callback(new Pair<ThinPlayer,Pair<Long,int[]>[]>(whom,allData));
				if((sleepAmount>0)&&(System.currentTimeMillis() > nextWaitAfter))
				{
					CMLib.s_sleep(sleepAmount);
					nextWaitAfter=System.currentTimeMillis() + msWait;
				}
			}
			if(R!=null)
				R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		final List<PlayerData> pdV = CMLib.database().DBReadPlayerSectionData("SYSTEM_PRIDE_PARCHIVE");
		final XMLLibrary xLib = CMLib.xml();
		final Long now = Long.valueOf(System.currentTimeMillis());
		for(final PlayerData pd : pdV)
		{
			final String fakeName = pd.who();
			final List<XMLLibrary.XMLTag> pieces = xLib.parseAllXML(pd.xml());
			final PrideCat cat = PrideCat.valueOf(xLib.getValFromPieces(pieces,"PCAT"));
			final String unit = xLib.getValFromPieces(pieces, "PUNIT");
			final PrideStat stat = PrideStat.valueOf(xLib.getValFromPieces(pieces, "PSTAT"));
			final int pval = xLib.getIntFromPieces(pieces, "PVALUE");
			@SuppressWarnings("unchecked")
			final Pair<Long,int[]>[] allData = new Pair[TimeClock.TimePeriod.values().length];
			final int[] data = new int[PrideStat.values().length];
			allData[TimeClock.TimePeriod.ALLTIME.ordinal()] = new Pair<Long,int[]>(now,data);
			data[stat.ordinal()] = pval;
			final ThinPlayer whom = new ThinPlayer()
			{
				@Override
				public String name()
				{
					return fakeName;
				}

				@Override
				public String charClass()
				{
					if(cat == PrideCat.CLASS)
						return unit;
					else
					if(cat == PrideCat.BASECLASS)
					{
						CharClass C = CMClass.getCharClass(unit);
						if((C!=null)&&(C.baseClass().equalsIgnoreCase(unit)))
							return C.ID();
						for(final Enumeration<CharClass> c = CMClass.charClasses();c.hasMoreElements();)
						{
							C = c.nextElement();
							if(C.baseClass().equalsIgnoreCase(unit))
								return C.ID();
						}
					}
					return null;
				}

				@Override
				public String race()
				{
					if(cat == PrideCat.RACE)
						return unit;
					else
					if(cat == PrideCat.RACECAT)
					{
						Race R = CMClass.getRace(unit);
						if((R!=null)&&(R.racialCategory().equalsIgnoreCase(unit)))
							return R.ID();
						for(final Enumeration<Race> r = CMClass.races();r.hasMoreElements();)
						{
							R = r.nextElement();
							if(R.racialCategory().equalsIgnoreCase(unit))
								return R.ID();
						}
					}
					return null;
				}

				@Override
				public int level()
				{
					if(cat == PrideCat.LEVEL)
						return CMath.s_int(unit);
					else
						return -1;
				}

				@Override
				public int age()
				{
					return 0;
				}

				@Override
				public long last()
				{
					return 0;
				}

				@Override
				public String email()
				{
					return null;
				}

				@Override
				public String ip()
				{
					return null;
				}

				@Override
				public int exp()
				{
					return 0;
				}

				@Override
				public int expLvl()
				{
					return 0;
				}

				@Override
				public String liege()
				{
					return null;
				}

				@Override
				public String worship()
				{
					return null;
				}

				@Override
				public String gender()
				{
					if(cat == PrideCat.GENDER)
						return unit;
					return null;
				}

				@Override
				public Enumeration<String> clans()
				{
					if(cat == PrideCat.CLAN)
						return new XVector<String>(unit).elements();
					return new EmptyEnumeration<String>();
				}
			};
			final Pair<ThinPlayer,Pair<Long,int[]>[]> cbData = new Pair<ThinPlayer,Pair<Long,int[]>[]>(whom,allData);
			callBack.callback(cbData);
			if((sleepAmount>0)&&(System.currentTimeMillis() > nextWaitAfter))
			{
				CMLib.s_sleep(sleepAmount);
				nextWaitAfter=System.currentTimeMillis() + msWait;
			}
		}
	}

	public void DBScanPrideAccountWinners(final CMCallback<Pair<String,Pair<Long,int[]>[]>> callBack, final short scanCPUPercent)
	{
		DBConnection D=null;
		final long msWait=Math.round(1000.0 * CMath.div(scanCPUPercent, 100));
		final long sleepAmount=1000 - msWait;
		try
		{
			long nextWaitAfter=System.currentTimeMillis() + msWait;
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMANAM,CMAXML FROM CMACCT");
			while((R!=null)&&(R.next()))
			{
				final String userID=DB.getRes(R, "CMANAM");
				final PlayerAccount A = CMLib.players().getAccount(userID);
				final String pxml;
				if(A!=null)
					pxml=A.getXML();
				else
					pxml=DB.getRes(R, "CMAXML");
				final String[] pridePeriods=CMLib.xml().returnXMLValue(pxml, "NEXTPRIDEPERIODS").split(",");
				final String[] prideStats=CMLib.xml().returnXMLValue(pxml, "PRIDESTATS").split(";");
				final String[] prideExceptions=CMLib.xml().returnXMLValue(pxml, "FLAGS").split(",");
				if(CMParms.contains(prideExceptions, PlayerFlag.NOSTATS.toString())
					||CMParms.contains(prideExceptions, PlayerFlag.NOTOP.toString()))
					continue;
				final Pair<Long,int[]>[] allData = CMLib.players().parsePrideStats(pridePeriods, prideStats);
				callBack.callback(new Pair<String,Pair<Long,int[]>[]>(userID,allData));
				if((sleepAmount>0)&&(System.currentTimeMillis() > nextWaitAfter))
				{
					CMLib.s_sleep(sleepAmount);
					nextWaitAfter=System.currentTimeMillis() + msWait;
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}


	// this method is unused, but is a good idea of how to collect riders, followers, carts, etc.
	protected void addFollowerDependent(final PhysicalAgent P, final PairList<PhysicalAgent,String> list, final String parent)
	{
		if(P==null)
			return;
		if(list.containsFirst(P))
			return;
		if((P instanceof MOB)
		&&((!((MOB)P).isMonster())||(((MOB)P).isPossessing())))
			return;
		CMLib.catalog().updateCatalogIntegrity(P);
		final String myCode=""+(list.size()-1);
		list.add(P,CMClass.classID(P)+"#"+myCode+parent);
		if(P instanceof Rideable)
		{
			final Rideable R=(Rideable)P;
			for(int r=0;r<R.numRiders();r++)
				addFollowerDependent(R.fetchRider(r),list,"@"+myCode+"R");
		}
		if(P instanceof Container)
		{
			final Container C=(Container)P;
			final List<Item> contents=C.getDeepContents();
			for(int c=0;c<contents.size();c++)
				addFollowerDependent(contents.get(c),list,"@"+myCode+"C");
		}
	}

	public int DBRaceCheck(String raceID)
	{
		if((raceID==null)||(raceID.trim().length()==0))
			return 0;
		raceID = DB.injectionClean(raceID);
		int ct = DB.queryRows("SELECT RACEID FROM CMCHAR WHERE CMRACE='"+raceID+"'");
		ct += DB.queryRows("SELECT RACEID FROM CMROCH WHERE CMROID='"+raceID+"' OR CMROTX LIKE '%RACE>"+raceID+"</%");
		ct += DB.queryRows("SELECT RACEID FROM CMCHFO WHERE CMFOID='"+raceID+"' OR CMFOTX LIKE '%RACE>"+raceID+"</%");
		return ct;
	}

	public void DBUpdateFollowers(final MOB mob)
	{
		if((mob==null)||(mob.Name().length()==0))
			return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		final String name=DB.injectionClean(mob.Name());
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHFO WHERE CMUSERID='"+name+"'"));

		if(mob.numFollowers()==0)
		{
			DB.updateWithClobs(statements);
			return;
		}

		final boolean useBulkInserts = DB.useBulkInserts();
		final StringBuilder bulkSQL = new StringBuilder("");
		final List<String> bulkClobs = new ArrayList<String>();

		// make a list of the valid savable followers
		final List<MOB> followers=new ArrayList<MOB>(mob.numFollowers());
		for(int f=0;f<mob.numFollowers();f++)
		{
			final MOB followM=mob.fetchFollower(f);
			if((followM!=null)
			&&(followM.isMonster())
			&&(!followM.isPossessing()))
			{
				if(CMLib.flags().isSavable(followM))
					followers.add(followM);
				else
				{
					// check if the room's to blame
					final Room R=followM.location();
					if((R!=null)
					&&(!CMLib.flags().isSavable(R))
					&&(!CMLib.flags().isSavable(R.getArea())))
						followers.add(followM);
				}
			}
		}

		for(int f=0;f<followers.size();f++)
		{
			MOB followM = followers.get(f);
			// prevent rejuving map mobs from getting text() called and wiping
			// out their original specs
			if((followM.basePhyStats().rejuv()>0)
			&&(followM.basePhyStats().rejuv()!=PhyStats.NO_REJUV))
			{
				final Room R=followM.location();
				final Integer order = Integer.valueOf(mob.fetchFollowerOrder(followM));
				followM.setFollowing(null);
				mob.delFollower(followM);
				final MOB newFol = (MOB) followM.copyOf();
				newFol.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				newFol.phyStats().setRejuv(PhyStats.NO_REJUV);
				newFol.text();
				followM.killMeDead(false);
				mob.addFollower(newFol, order.intValue());
				if(newFol.amFollowing()!=mob)
					newFol.setFollowing(mob);
				if(CMLib.flags().isInTheGame(mob, true)
				&&(!CMLib.flags().isInTheGame(newFol, true))
				&&(R!=null))
					newFol.bringToLife(R, false);
				if(!mob.isFollowedBy(newFol))
					mob.addFollower(newFol, order.intValue());
				if(newFol.amFollowing()!=mob)
					newFol.setFollowing(mob);
				followM=newFol;
			}

			CMLib.catalog().updateCatalogIntegrity(followM);
			final String sql="INSERT INTO CMCHFO (CMUSERID, CMFONM, CMFOID, CMFOTX, CMFOLV, CMFOAB"
							+") values ('"+name+"',"+f+",'"+CMClass.classID(followM)+"',?,"
							+followM.basePhyStats().level()+","+followM.basePhyStats().ability()+")";
			if(!useBulkInserts)
				statements.add(new DBPreparedBatchEntry(sql,followM.text()+" "));
			else
			{
				final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,followM.text()+" ");
				if(entry != null)
					statements.add(entry);
			}
		}

		if((bulkSQL.length()>0) && useBulkInserts)
			statements.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		DB.updateWithClobs(statements);
	}

	public void DBNameChange(String oldName, String newName)
	{
		if((oldName==null)
		||(oldName.trim().length()==0)
		||(newName==null)
		||(newName.trim().length()==0))
			return;
		oldName=DB.injectionClean(oldName);
		newName=DB.injectionClean(newName);
		DB.update("UPDATE CMCHAB SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMWORS='"+newName+"' WHERE CMWORS='"+oldName+"'");
		DB.update("UPDATE CMCHAR SET CMLEIG='"+newName+"' WHERE CMLEIG='"+oldName+"'");
		DB.update("UPDATE CMCHCL SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");

		DB.update("UPDATE CMCHFO SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMCHIT SET CMUSERID='"+newName+"' WHERE CMUSERID='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMFROM='"+newName+"' WHERE CMFROM='"+oldName+"'");
		DB.update("UPDATE CMJRNL SET CMTONM='"+newName+"' WHERE CMTONM='"+oldName+"'");
		DB.update("UPDATE CMPDAT SET CMPLID='"+newName+"' WHERE CMPLID='"+oldName+"'");
	}

	public void DBDeleteCharOnly(String mobName)
	{
		mobName=DB.injectionClean(mobName);
		DB.update("DELETE FROM CMCHAR WHERE CMUSERID='"+mobName+"'");
		DB.update("DELETE FROM CMCHCL WHERE CMUSERID='"+mobName+"'");
	}

	public void DBUpdateAbilities(final MOB mob)
	{
		if(mob.Name().length()==0)
			return;
		final List<DBPreparedBatchEntry> statements=new LinkedList<DBPreparedBatchEntry>();
		statements.add(new DBPreparedBatchEntry("DELETE FROM CMCHAB WHERE CMUSERID='"+mob.Name()+"'"));
		final HashSet<String> H=new HashSet<String>();
		final boolean useBulkInserts = DB.useBulkInserts();
		final StringBuilder bulkSQL = new StringBuilder("");
		final List<String> bulkClobs = new ArrayList<String>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)&&(thisAbility.isSavable()))
			{
				int proficiency=thisAbility.proficiency();
				final Ability effectA=mob.fetchEffect(thisAbility.ID());
				if(effectA!=null)
				{
					if((effectA.isSavable())&&(!effectA.canBeUninvoked())&&(!effectA.isAutoInvoked()))
						proficiency=proficiency-200;
				}
				H.add(thisAbility.ID());
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+thisAbility.ID()+"',"+proficiency+",?)";
				if(!useBulkInserts)
					statements.add(new DBPreparedBatchEntry(sql,thisAbility.text()));
				else
				{
					final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,thisAbility.text());
					if(entry != null)
						statements.add(entry);
				}
			}
		}
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!H.contains(A.ID()))&&(A.isSavable())&&(!A.canBeUninvoked()))
			{
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+A.ID()+"',"+Integer.MAX_VALUE+",?)";
				if(!useBulkInserts)
					statements.add(new DBPreparedBatchEntry(sql,A.text()));
				else
				{
					final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,A.text());
					if(entry != null)
						statements.add(entry);
				}
			}
		}
		for(final Enumeration<Behavior> e=mob.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if((B!=null)&&(B.isSavable()))
			{
				final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
				+") values ('"+mob.Name()+"','"+B.ID()+"',"+(Integer.MIN_VALUE+1)+",?"
				+")";
				if(!useBulkInserts)
					statements.add(new DBPreparedBatchEntry(sql,B.getParms()));
				else
				{
					final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,B.getParms());
					if(entry != null)
						statements.add(entry);
				}
			}
		}
		final String scriptStuff = CMLib.coffeeMaker().getGenScriptsXML(mob,true);
		if(scriptStuff.length()>0)
		{
			final String sql="INSERT INTO CMCHAB (CMUSERID, CMABID, CMABPF,CMABTX"
			+") values ('"+mob.Name()+"','ScriptingEngine',"+(Integer.MIN_VALUE+1)+",?"
			+")";
			if(!useBulkInserts)
				statements.add(new DBPreparedBatchEntry(sql,scriptStuff));
			else
			{
				final DBPreparedBatchEntry entry = doBulkInsert(bulkSQL,bulkClobs,sql,scriptStuff);
				if(entry != null)
					statements.add(entry);
			}
		}
		if((bulkSQL.length()>0) && useBulkInserts)
			statements.add(new DBPreparedBatchEntry(bulkSQL.toString(),bulkClobs.toArray(new String[0])));
		DB.updateWithClobs(statements);
	}

	public void DBCreateCharacter(final MOB mob)
	{
		if(mob.Name().length()==0)
			return;
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return;
		DB.update("INSERT INTO CMCHAR (CMCHID, CMUSERID, CMPASS, CMCLAS, CMRACE, CMGEND "
				+") VALUES ('"+mob.ID()+"','"+mob.Name()+"','"+pstats.getPasswordStr()+"','"+mob.baseCharStats().getMyClassesStr()
				+"','"+mob.baseCharStats().getMyRace().ID()+"','"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))
				+"')");
		final PlayerAccount account = pstats.getAccount();
		if(account != null)
		{
			account.addNewPlayer(mob);
			DBUpdateAccount(account);
			account.setLastUpdated(System.currentTimeMillis());
		}
	}

	public void DBUpdateAccount(final PlayerAccount account)
	{
		if(account == null)
			return;
		final String characters = CMParms.toSemicolonListString(account.getPlayers());
		DB.updateWithClobs("UPDATE CMACCT SET CMPASS='"+account.getPasswordStr()+"',  CMCHRS=?,  CMAXML=?  WHERE CMANAM='"+account.getAccountName()+"'",
				new String[][]{{characters,account.getXML()}});
	}

	public void DBDeleteAccount(final PlayerAccount account)
	{
		if(account == null)
			return;
		final String login=DB.injectionClean(account.getAccountName());
		DB.update("DELETE FROM CMACCT WHERE CMANAM='"+login+"'");
	}

	public void DBCreateAccount(final PlayerAccount account)
	{
		if(account == null)
			return;
		account.setAccountName(CMStrings.capitalizeAndLower(account.getAccountName()));
		final String characters = CMParms.toSemicolonListString(account.getPlayers());
		final String login=DB.injectionClean(account.getAccountName());
		DB.updateWithClobs("INSERT INTO CMACCT (CMANAM, CMPASS, CMCHRS, CMAXML) "
				+"VALUES ('"+login+"','"+account.getPasswordStr()+"',?,?)",new String[][]{{characters,account.getXML()}});
	}

	public PlayerAccount MakeAccount(final String username, final ResultSet R) throws SQLException
	{
		PlayerAccount account = null;
		account = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		final String password=DB.getRes(R,"CMPASS");
		final String chrs=DB.getRes(R,"CMCHRS");
		final String xml=DB.getRes(R,"CMAXML");
		final Vector<String> names = new Vector<String>();
		if(chrs!=null)
			names.addAll(CMParms.parseSemicolons(chrs,true));
		account.setAccountName(CMStrings.capitalizeAndLower(username));
		account.setPassword(password);
		account.setPlayerNames(names);
		account.setXML(xml);
		return account;
	}

	public PlayerAccount DBReadAccount(String login)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			login=CMStrings.capitalizeAndLower(DB.injectionClean(login));
			final ResultSet R=D.query("SELECT * FROM CMACCT WHERE CMANAM='"+login+"'");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMANAM");
				if(login.equalsIgnoreCase(username))
					account = MakeAccount(username,R);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return account;
	}

	public List<PlayerAccount> DBListAccounts(String mask)
	{
		DBConnection D=null;
		PlayerAccount account = null;
		final Vector<PlayerAccount> accounts = new Vector<PlayerAccount>();
		if(mask!=null)
			mask=mask.toLowerCase();
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMACCT");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMANAM");
				if((mask==null)||(mask.length()==0)||(username.toLowerCase().indexOf(mask)>=0))
				{
					account = MakeAccount(username,R);
					accounts.add(account);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return accounts;
	}

	public List<String> DBExpiredCharNameSearch(final Set<String> skipNames)
	{
		DBConnection D=null;
		String buf=null;
		final List<String> expiredPlayers = new ArrayList<String>();
		final long now=System.currentTimeMillis();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMUSERID,CMPFIL FROM CMCHAR");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMUSERID");
				if((skipNames!=null)&&(skipNames.contains(username)))
					continue;
				buf=DBConnections.getRes(R,"CMPFIL");
				final String secGrps=CMLib.xml().restoreAngleBrackets(CMLib.xml().returnXMLValue(buf,"SECGRPS"));
				final SecGroup g=CMSecurity.instance().createGroup("", CMParms.parseSemicolons(secGrps,true));
				if(g.contains(SecFlag.NOEXPIRE, false))
					continue;
				long expiration;
				if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
					expiration=CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP"));
				else
				{
					final Calendar C=Calendar.getInstance();
					C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
					expiration=C.getTimeInMillis();
				}
				if(now>=expiration)
					expiredPlayers.add(username);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return expiredPlayers;
	}

	public PlayerLibrary.ThinnerPlayer DBUserSearch(String login)
	{
		DBConnection D=null;
		String buf=null;
		PlayerLibrary.ThinnerPlayer thinPlayer = null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			login=CMStrings.capitalizeAndLower(DB.injectionClean(login));
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+login+"'");
			if(R!=null) while(R.next())
			{
				final String username=DB.getRes(R,"CMUSERID");
				thinPlayer = CMLib.players().newThinnerPlayer();
				final String password=DB.getRes(R,"CMPASS");
				final String email=DB.getRes(R,"CMEMAL");
				thinPlayer.name(username);
				thinPlayer.password(password);
				thinPlayer.email(email);
				// Acct Exp Code
				buf=DBConnections.getRes(R,"CMPFIL");
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		if((buf!=null)&&(thinPlayer!=null))
		{
			PlayerAccount acct = null;
			thinPlayer.accountName(CMLib.xml().returnXMLValue(buf,"ACCOUNT"));
			if((thinPlayer.accountName()!=null)&&(thinPlayer.accountName().length()>0))
				acct = CMLib.players().getLoadAccount(thinPlayer.accountName());
			if((acct != null)&&(CMProps.isUsingAccountSystem()))
				thinPlayer.expiration(acct.getAccountExpiration());
			else
			if(CMLib.xml().returnXMLValue(buf,"ACCTEXP").length()>0)
				thinPlayer.expiration(CMath.s_long(CMLib.xml().returnXMLValue(buf,"ACCTEXP")));
			else
			{
				final Calendar C=Calendar.getInstance();
				C.add(Calendar.DATE,CMProps.getIntVar(CMProps.Int.TRIALDAYS));
				thinPlayer.expiration(C.getTimeInMillis());
			}
		}
		return thinPlayer;
	}

	public String DBLeigeSearch(String login)
	{
		DBConnection D=null;
		try
		{
			// why in the hell is this a memory scan?
			// case insensitivity from databases configured almost
			// certainly by amateurs is the answer. That, and fakedb
			// doesn't understand 'LIKE'
			D=DB.DBFetch();
			login=CMStrings.capitalizeAndLower(DB.injectionClean(login));
			final ResultSet R=D.query("SELECT CMLEIG FROM CMCHAR WHERE CMUSERID='"+login+"'");
			if(R!=null) while(R.next())
				return DB.getRes(R, "CMLEIG");
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public Pair<String, Boolean> DBFetchEmailData(String name)
	{
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if((M.Name().equalsIgnoreCase(name))&&(M.playerStats()!=null))
			{
				final boolean canReceiveRealEmail =
					(M.playerStats()!=null)
					&&(M.isAttributeSet(MOB.Attrib.AUTOFORWARD))
					&&((M.playerStats().getAccount()==null)
						||(!M.playerStats().getAccount().isSet(AccountFlag.NOAUTOFORWARD)));
				return new Pair<String,Boolean>(M.playerStats().getEmail(),Boolean.valueOf(canReceiveRealEmail));
			}
		}
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			name=DB.injectionClean(name);
			final ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R!=null) while(R.next())
			{
				// String username=DB.getRes(R,"CMUSERID");
				final int btmp=CMath.s_int(DB.getRes(R,"CMBTMP"));
				final String temail=DB.getRes(R,"CMEMAL");
				return new Pair<String,Boolean>(temail,Boolean.valueOf((btmp&MOB.Attrib.AUTOFORWARD.getBitCode())!=0));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String DBGetAccountNameFromPlayer(String player)
	{
		if(player==null)
			return null;
		DBConnection D=null;
		player=CMStrings.capitalizeAndLower(player);
		final MOB M = CMLib.players().getPlayer(player);
		if((M!=null)
		&&(M.Name().equalsIgnoreCase(player))
		&&(M.playerStats()!=null)
		&&(M.playerStats().getAccount()!=null))
			return M.playerStats().getAccount().getAccountName();
		for(final Enumeration<PlayerAccount> a=CMLib.players().accounts();a.hasMoreElements();)
		{
			final PlayerAccount A = a.nextElement();
			if(A.findPlayer(player)!=null)
				return A.getAccountName();
		}
		try
		{
			D=DB.DBFetch();
			player=DB.injectionClean(player.trim());
			final ResultSet R=D.query("SELECT CMCHRS, CMANAM FROM CMACCT WHERE CMCHRS LIKE '%"+player+"%'");
			if(R!=null)
			{
				while(R.next())
				{
					final String charStrs=DB.getRes(R,"CMUSERID");
					final String aname=DB.getRes(R,"CMANAM");
					final List<String> chars = CMParms.parseSemicolons(charStrs, true);
					for(final String c : chars)
					{
						if(c.equalsIgnoreCase(player))
							return aname;
					}
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public String DBPlayerEmailSearch(String email)
	{
		DBConnection D=null;
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if((M.playerStats()!=null)&&(M.playerStats().getEmail().equalsIgnoreCase(email)))
				return M.Name();
		}
		try
		{
			D=DB.DBFetch();
			email=DB.injectionClean(email.trim());
			ResultSet R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL='"+email+"'");
			if(((R==null)||(!R.next()))&&(!CMStrings.isLowerCase(email)))
				R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL='"+email.toLowerCase()+"'");
			if((R==null)||(!R.next()))
				R=D.query("SELECT * FROM CMCHAR WHERE CMEMAL LIKE '"+email+"'");
			if((R==null)||(!R.next()))
				R=D.query("SELECT * FROM CMCHAR");
			if(R!=null)
			{
				while(R.next())
				{
					final String username=DB.getRes(R,"CMUSERID");
					final String temail=DB.getRes(R,"CMEMAL");
					if(temail.equalsIgnoreCase(email))
					{
						return username;
					}
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return null;
	}

	public PairList<String, Long> DBSearchPFIL(String match)
	{
		final PairList<String,Long> names = new PairVector<String,Long>();
		DBConnection D=null;
		match=DB.injectionClean(match);
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMUSERID, CMDATE FROM CMCHAR WHERE CMPFIL like '%"+match+"%'");
			while(R.next())
				names.add(DB.getRes(R, "CMUSERID"), Long.valueOf(DB.getLongRes(R, "CMDATE")));
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return names;
	}

	public PlayerStats DBLoadPlayerStats(String name)
	{
		if((name==null)||(name.length()==0))
			return null;
		name=CMStrings.capitalizeAndLower(DB.injectionClean(name));
		DBConnection D=null;
		PlayerStats pStats = null;
		// now grab the items
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMDATE, CMCHAN, CMPRPT, CMCOLR, CMLSIP, CMPFIL FROM CMCHAR WHERE CMUSERID='"+name+"'");
			if(R.next())
			{
				pStats = (PlayerStats)CMClass.getCommon("DefaultPlayerStats");
				pStats.setLastDateTime(CMath.s_long(DBConnections.getRes(R,"CMDATE")));
				pStats.setChannelMask((int)DBConnections.getLongRes(R,"CMCHAN"));
				pStats.setPrompt(DBConnections.getRes(R,"CMPRPT"));
				final String colorStr=DBConnections.getRes(R,"CMCOLR");
				if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
					pStats.setColorStr(colorStr);
				pStats.setLastIP(DBConnections.getRes(R,"CMLSIP"));
				final String buf=DBConnections.getRes(R,"CMPFIL");
				pStats.setXML(buf);
			}
			R.close();
		}
		catch(final Exception sqle)
		{
			Log.errOut("MOB",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		return pStats;
	}
}
