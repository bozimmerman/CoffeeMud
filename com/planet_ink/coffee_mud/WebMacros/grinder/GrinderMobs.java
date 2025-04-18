package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.RoomData;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataSpawn;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class GrinderMobs
{
	public enum MOBDataField
	{
		NAME,CLASSES,DISPLAYTEXT,DESCRIPTION,
		LEVEL(false),ABILITY(false),REJUV(false),MISCTEXT(false),
		RACE,GENDER,HEIGHT,WEIGHT,
		SPEED,ATTACK,DAMAGE,ARMOR,
		ALIGNMENT,MONEY,ISRIDEABLE,RIDEABLETYPE,
		MOBSHELD,ISSHOPKEEPER,SHOPKEEPERTYPE,ISGENERIC,
		ISBANKER,COININT,ITEMINT,BANKNAME,SHOPPREJ,
		ISDEITY,CLEREQ,CLERIT,WORREQ,WORRIT,
		CLESIN,WORSIN,CLEPOW,CURSES,POWERS,
		CLANID,TATTOOS,EXPERTISES,
		BUDGET,DEVALRATE,INVRESETRATE,IMAGE,
		ISPOSTMAN,POSTCHAIN,POSTMIN,POSTLBS,
		POSTHOLD,POSTNEW,POSTHELD,IGNOREMASK,
		LOANINT,SVCRIT,AUCCHAIN,LIVELIST,TIMELIST,
		TIMELISTPCT,LIVECUT,TIMECUT,MAXDAYS,
		MINDAYS,ISAUCTION,DEITYID,VARMONEY,
		CATACAT,SELLIMASK,LIBRCHAIN,LIBROVERCHG,
		LIBRDAYCHG,LIBROVERPCT,LIBDAYPCT,LIBMINDAYS,
		LIBMAXDAYS,LIBMAXBORROW,ISLIBRARIAN,LIBCMASK,
		STATESTR,STATESUBJSTR,RIDERSTR,MOUNTSTR,DISMOUNTSTR,
		ISDRINK, LIQUIDHELD, QUENCHED, LIQUIDTYPES, SIVIEWTYPES,
		CURRENCIES, CURRENCY,CATARATE,CATALIVE,CATAMASK,CATACAP,
		ISBROKER, BROCHAIN, MAXLISTINGS, COMMISSIONPCT
		;

		public boolean isGenField;
		private MOBDataField(final boolean isGeneric)
		{
			this.isGenField=isGeneric;
		}

		private MOBDataField()
		{
			isGenField = true;
		}
	}

	public static String senses(final Physical P, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		P.basePhyStats().setSensesMask(0);
		for(int d=0;d<PhyStats.CAN_SEE_CODES.length;d++)
		{
			final String parm=httpReq.getUrlParameter(PhyStats.CAN_SEE_CODES[d]);
			if((parm!=null)&&(parm.equals("on")))
				P.basePhyStats().setSensesMask(P.basePhyStats().sensesMask()|(1<<d));
		}
		return "";
	}

	public static void happilyAddItem(final Item I, final MOB M)
	{
		if(I.subjectToWearAndTear() && ((I.usesRemaining()<1)||(I.usesRemaining()>100)))
			I.setUsesRemaining(100);
		I.recoverPhyStats();
		M.addItem(I);
		M.recoverPhyStats();
		M.recoverCharStats();
		M.recoverMaxState();
	}

	public static String abilities(final MOB M, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		final boolean player=M.playerStats()!=null;
		final LinkedList<Ability> onesToDel=new LinkedList<Ability>();
		for(int a=0;a<M.numAbilities();a++)
		{
			final Ability A=M.fetchAbility(a);
			if((A!=null)&&((!player)||(A.isSavable())))
				onesToDel.add(A);
		}
		for (final Ability A : onesToDel)
		{
			if(M.fetchEffect(A.ID())!=null)
				M.delEffect(M.fetchEffect(A.ID()));
			M.delAbility(A);
		}
		if(httpReq.isUrlParameter("ABLES1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("ABLES"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final Ability A=CMClass.getAbility(aff);
					if(A==null)
					{
						if((!CMSecurity.isDisabled(DisFlag.LANGUAGES))
						||(!CMClass.isLanguage(aff)))
							return "Unknown Ability '"+aff+"'.";
						else
							break;
					}
					else
					{
						if(player)
						{
							String prof=httpReq.getUrlParameter("ABPOF"+num);
							if(prof==null)
								prof="0";
							String txt=httpReq.getUrlParameter("ABTXT"+num);
							if(txt==null)
								txt="";
							A.setProficiency(CMath.s_int(prof));
							A.setMiscText(txt);
						}
						else
							A.setProficiency(75);
						M.addAbility(A);
						A.autoInvocation(M, false);
					}
				}
				num++;
				aff=httpReq.getUrlParameter("ABLES"+num);
			}
		}
		return "";
	}

	public static String factions(final MOB E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		for(final Enumeration<String> e=E.factions();e.hasMoreElements();)
		{
			final String strip=e.nextElement();
			E.removeFaction(strip);
		}
		if(httpReq.isUrlParameter("FACTION1"))
		{
			int num=1;
			String whichFaction=httpReq.getUrlParameter("FACTION"+num);
			String howMuch=httpReq.getUrlParameter("FACTDATA"+num);
			while((whichFaction!=null)&&(howMuch!=null))
			{
				if(whichFaction.length()>0)
				{
					final Faction F=CMLib.factions().getFaction(whichFaction);
					if(F!=null)
					{
						int amt=Integer.valueOf(howMuch).intValue();
						if(amt<F.minimum())
							amt=F.minimum();
						if(amt>F.maximum())
							amt=F.maximum();
						E.addFaction(F.factionID(),amt);
					}
				}
				num++;
				whichFaction=httpReq.getUrlParameter("FACTION"+num);
				howMuch=httpReq.getUrlParameter("FACTDATA"+num);
			}
		}
		return "";
	}

	public static String blessings(final Deity E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		while(E.numBlessings()>0)
		{
			final Ability A=E.fetchBlessing(0);
			if(A!=null)
				E.delBlessing(A);
		}
		if(httpReq.isUrlParameter("BLESS1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("BLESS"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final boolean clericOnly=(httpReq.isUrlParameter("BLONLY"+num))&&(httpReq.getUrlParameter("BLONLY"+num)).equalsIgnoreCase("on");
					final Ability B=CMClass.getAbility(aff);
					if(B==null)
						return "Unknown Blessing '"+aff+"'.";
					else
					{
						final String atext=httpReq.getUrlParameter("BLESSTEXT"+num);
						if((atext!=null)&&(atext.length()>0))
							B.setMiscText(atext);
						E.addBlessing(B,clericOnly);
					}
				}
				num++;
				aff=httpReq.getUrlParameter("BLESS"+num);
			}
		}
		return "";
	}

	public static String clans(final MOB E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		final List<String> clans=new ArrayList<String>();
		for(final Pair<Clan,Integer> p : E.clans())
			clans.add(p.first.clanID());
		for(final String clanID : clans)
			E.setClan(clanID, -1);
		if(httpReq.isUrlParameter("CLAN1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("CLAN"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final int role=CMath.s_int(httpReq.getUrlParameter("CLANROLE"+num));
					final Clan C=CMLib.clans().getClan(aff);
					if(C==null)
						return "Unknown Clan '"+aff+"'.";
					else
						E.setClan(C.clanID(), role);
				}
				num++;
				aff=httpReq.getUrlParameter("CLAN"+num);
			}
		}
		return "";
	}

	public static String curses(final Deity E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		while(E.numCurses()>0)
		{
			final Ability A=E.fetchCurse(0);
			if(A!=null)
				E.delCurse(A);
		}
		if(httpReq.isUrlParameter("CURSE1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("CURSE"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final Ability B=CMClass.getAbility(aff);
					final boolean clericOnly=(httpReq.isUrlParameter("CUONLY"+num))&&(httpReq.getUrlParameter("CUONLY"+num)).equalsIgnoreCase("on");
					if(B==null)
						return "Unknown Curse '"+aff+"'.";
					else
					{
						final String atext=httpReq.getUrlParameter("CURSETEXT"+num);
						if((atext!=null)&&(atext.length()>0))
							B.setMiscText(atext);
						E.addCurse(B,clericOnly);
					}
				}
				num++;
				aff=httpReq.getUrlParameter("CURSE"+num);
			}
		}
		return "";
	}

	public static String expertiseList(final MOB E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		E.delAllExpertises();
		if(httpReq.isUrlParameter("EXPER1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("EXPER"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(aff);
					if(def==null)
						return "Unknown Expertise '"+aff+"'.";
					else
						E.addExpertise(def.ID());
				}
				num++;
				aff=httpReq.getUrlParameter("EXPER"+num);
			}
		}
		return "";
	}

	public static String items(final MOB M, final List<Item> allitems, final HTTPRequest httpReq)
	{
		if(httpReq.isUrlParameter("ITEM1"))
		{
			final List<Item> items=new ArrayList<Item>();
			final List<String> cstrings=new ArrayList<String>();
			for(int i=1;;i++)
			{
				final String MATCHING=httpReq.getUrlParameter("ITEM"+i);
				final String WORN=httpReq.getUrlParameter("ITEMWORN"+i);
				if(MATCHING==null)
					break;
				Item I2=CMLib.webMacroFilter().findItemInAnything(allitems,MATCHING);
				if(I2!=null)
				{
					if(!CMath.isNumber(MATCHING))
						I2=(Item)I2.copyOf();
					if(I2!=null)
					{
						CMLib.flags().setSavable(I2, true);
						final boolean worn=((WORN!=null)&&(WORN.equalsIgnoreCase("on")));
						I2.setContainer(null);
						I2.unWear();
						if(worn)
							I2.wearEvenIfImpossible(M);
						happilyAddItem(I2,M);
						items.add(I2);
						I2.setContainer(null);
						final String CONTAINER=httpReq.getUrlParameter("ITEMCONT"+i);
						cstrings.add((CONTAINER==null)?"":CONTAINER);
					}
				}
			}
			for(int i=0;i<cstrings.size();i++)
			{
				final String CONTAINER=cstrings.get(i);
				if(CONTAINER.length()==0)
					continue;
				final Item I2=items.get(i);
				final Item C2=(Item)CMLib.english().fetchEnvironmental(items,CONTAINER,true);
				if(C2 instanceof Container)
					I2.setContainer((Container)C2);
			}
			for(int i=0;i<allitems.size();i++)
			{
				final Item I=allitems.get(i);
				if(!M.isMine(I))
				{
					I.setOwner(M);
					I.destroy();
				}
			}
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if((I.container()!=null)&&(!M.isMine(I.container())))
					I.setContainer(null);
			}
			return "";
		}
		return "No Item Data!";
	}

	public static String powers(final Deity E, final HTTPRequest httpReq, final java.util.Map<String,String> parms)
	{
		while(E.numPowers()>0)
		{
			final Ability A=E.fetchPower(0);
			if(A!=null)
				E.delPower(A);
		}
		if(httpReq.isUrlParameter("POWER1"))
		{
			int num=1;
			String aff=httpReq.getUrlParameter("POWER"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					final Ability B=CMClass.getAbility(aff);
					if(B==null)
						return "Unknown Power '"+aff+"'.";
					else
					{
						final String atext=httpReq.getUrlParameter("POWERTEXT"+num);
						if((atext!=null)&&(atext.length()>0))
							B.setMiscText(atext);
						E.addPower(B);
					}
				}
				num++;
				aff=httpReq.getUrlParameter("POWER"+num);
			}
		}
		return "";
	}

	public static String editMob(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final MOB whom, Room R)
	{
		final String mobCode=httpReq.getUrlParameter("MOB");
		if(mobCode==null)
			return "@break@";

		final String newClassID=httpReq.getUrlParameter("CLASSES");

		String shopMobCode=httpReq.getUrlParameter("SHOPMOB");
		if(shopMobCode==null)
			shopMobCode="";

		CatalogLibrary.CataData cataData=null;
		synchronized(CMClass.getSync(("SYNC"+((R!=null)?R.roomID():"null"))))
		{
			if(R!=null)
			{
				R=CMLib.map().getRoom(R);
				CMLib.map().resetRoom(R);
			}

			MOB M=null;
			if(mobCode.equals("NEW")||mobCode.equals("NEWDEITY")||mobCode.startsWith("NEWCATA-"))
				M=CMClass.getMOB(newClassID);
			else
				M=CMLib.webMacroFilter().getMOBFromWebCache(R,mobCode);

			MOB shopM=null;

			if((shopMobCode != null)
			&&(shopMobCode.length()>0)
			&&(M instanceof ShopKeeper))
			{
				shopM=M;
				if(shopMobCode.equals("NEW")||shopMobCode.equals("NEWDEITY")||shopMobCode.startsWith("NEWCATA-"))
					M=CMClass.getMOB(newClassID);
				else
				{
					final MOB chkM=CMLib.webMacroFilter().getMOBFromWebCache((Room)null,shopMobCode);
					if(chkM != null)
						M=(MOB)((ShopKeeper)M).getShop().getStock(chkM.Name(), null);
				}
			}

			if(M==null)
			{
				final StringBuffer str=new StringBuffer("No MOB?!");
				str.append(" Got: "+mobCode);
				str.append(", Includes: ");
				if(R!=null)
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M2=R.fetchInhabitant(m);
						if((M2!=null)&&(M2.isSavable()))
							str.append(M2.Name()+"="+CMLib.webMacroFilter().findMOBWebCacheCode(R,M2)+"<BR>\n\r");
					}
				}
				return str.toString();
			}
			final MOB oldM=M;
			if((newClassID!=null)&&(!newClassID.equals(CMClass.classID(M))))
				M=CMClass.getMOB(newClassID);
			if((shopMobCode == null)||(shopMobCode.length()==0))
				M.setStartRoom(R);

			final List<Item> allitems=new ArrayList<Item>();
			while(oldM.numItems()>0)
			{
				final Item I=oldM.getItem(0);
				allitems.add(I);
				oldM.delItem(I);
			}
			MOB copyMOB=(MOB)M.copyOf();
			if(R!=null)
				R.delInhabitant(copyMOB);

			for(final MOBDataField o : MOBDataField.values())
			{
				final String parmName=o.name();
				final boolean generic=o.isGenField;
				String old=httpReq.getUrlParameter(parmName);
				if(old==null)
					old="";
				if((M.isGeneric()||(!generic)))
				switch(o)
				{
				case NAME: // name
					M.setName(old);
					break;
				case CLASSES: // classes
					break;
				case DISPLAYTEXT: // displaytext
					M.setDisplayText(old);
					break;
				case DESCRIPTION: // description
					M.setDescription(CMStrings.fixMudCRLF(old));
					break;
				case LEVEL: // level
					M.basePhyStats().setLevel(CMath.s_int(old));
					break;
				case ABILITY: // ability;
					M.basePhyStats().setAbility(CMath.s_int(old));
					break;
				case REJUV: // rejuv;
					M.basePhyStats().setRejuv(CMath.s_int(old));
					break;
				case MISCTEXT: // misctext
					if(!M.isGeneric())
						M.setMiscText(old);
					break;
				case RACE: // race
					M.baseCharStats().setMyRace(CMClass.getRace(old));
					break;
				case GENDER: // gender
					if(old.length()>0)
						M.baseCharStats().setStat(CharStats.STAT_GENDER,old.charAt(0));
					break;
				case HEIGHT: // height
					M.basePhyStats().setHeight(CMath.s_int(old));
					break;
				case WEIGHT: // weight;
					M.basePhyStats().setWeight(CMath.s_int(old));
					break;
				case SPEED: // speed
					double d=CMath.s_double(old);
					if(d<0.0)
						d=1.0;
					M.basePhyStats().setSpeed(d);
					break;
				case ATTACK: // attack
					M.basePhyStats().setAttackAdjustment(CMath.s_int(old));
					break;
				case DAMAGE: // damage
					M.basePhyStats().setDamage(CMath.s_int(old));
					break;
				case ARMOR: // armor
					M.basePhyStats().setArmor(CMath.s_int(old));
					break;
				case ALIGNMENT: // alignment
					for(final Faction.Align v : Faction.Align.values())
					{
						if(old.equalsIgnoreCase(v.toString()))
							CMLib.factions().setAlignment(M,v);
					}
					break;
				case MONEY: // money
					CMLib.beanCounter().setMoney(M,CMath.s_int(old));
					break;
				case ISRIDEABLE: // is rideable
					break;
				case RIDEABLETYPE: // rideable type
					if(M instanceof Rideable)
						((Rideable)M).setRideBasis(Rideable.Basis.values()[CMath.s_int(old)]);
					break;
				case MOBSHELD: // mobs held
					if(M instanceof Rideable)
						((Rideable)M).setRiderCapacity(CMath.s_int(old));
					break;
				case ISSHOPKEEPER: // is shopkeeper
					break;
				case SELLIMASK: // sell item mask
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setWhatIsSoldZappermask(old.trim());
					break;
				case CATARATE: // catarate
					if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setRate(CMath.s_pct(old));
					}
					break;
				case CATACAP:
					if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setCap(CMath.s_int(old));
					}
					break;
				case CATALIVE: // catalive
					if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setSpawn((CataSpawn)CMath.s_valueOf(CataSpawn.class,old));
					}
					break;
				case CATAMASK: // catamask
					if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setMaskStr(old);
					}
					break;
				case SHOPKEEPERTYPE: // shopkeeper type
					if(M instanceof ShopKeeper)
					{
						((ShopKeeper)M).setWhatIsSoldMask(0);
						((ShopKeeper)M).addSoldType(CMath.s_int(old));
						int x=1;
						while(httpReq.getUrlParameter(parmName+x)!=null)
						{
							((ShopKeeper)M).addSoldType(CMath.s_int(httpReq.getUrlParameter(parmName+x)));
							x++;
						}
					}
					break;
				case SIVIEWTYPES: // view types
					if(M instanceof ShopKeeper)
					{
						((ShopKeeper)M).viewFlags().clear();
						int x=0;
						String key=parmName;
						while(httpReq.getUrlParameter(key)!=null)
						{
							final ViewType V = (ViewType)CMath.s_valueOf(ViewType.class, httpReq.getUrlParameter(key));
							if(V != null)
								((ShopKeeper)M).viewFlags().add(V);
							x++;
							key=parmName+x;
						}
					}
					break;
				case ISGENERIC: // is generic
					break;
				case ISBANKER: // is banker
					break;
				case COININT: // coin interest
					if(M instanceof Banker)
						((Banker)M).setCoinInterest(CMath.s_double(old));
					break;
				case ITEMINT: // item interest
					if(M instanceof Banker)
						((Banker)M).setItemInterest(CMath.s_double(old));
					break;
				case BANKNAME: // bank name
					if(M instanceof Banker)
						((Banker)M).setBankChain(old);
					break;
				case SHOPPREJ: // shopkeeper prejudices
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setPrejudiceFactors(old);
					break;
				case CURRENCIES:
				case CURRENCY:
					if(M instanceof Economics)
						((Economics)M).setCurrency(old);
					break;
				case ISDEITY: // is deity
					break;
				case CLEREQ: // cleric requirements
					if(M instanceof Deity)
						((Deity)M).setClericRequirements(old);
					break;
				case CLERIT: // cleric ritual
					if(M instanceof Deity)
						((Deity)M).setClericRitual(old);
					break;
				case WORREQ: // worshipper requirements
					if(M instanceof Deity)
						((Deity)M).setWorshipRequirements(old);
					break;
				case WORRIT: // worshipper ritual
					if(M instanceof Deity)
						((Deity)M).setWorshipRitual(old);
					break;
				case CLESIN: // cleric sins
					if(M instanceof Deity)
						((Deity)M).setClericSin(old);
					break;
				case WORSIN: // worshipper sins
					if(M instanceof Deity)
						((Deity)M).setWorshipSin(old);
					break;
				case CLEPOW: // cleric power
					if(M instanceof Deity)
						((Deity)M).setClericPowerup(old);
					break;
				case CURSES: // curses
					break;
				case POWERS: // powers
					break;
				case CLANID: // clanid
				{
					final List<String> list=CMParms.parseCommas(old,true);
					M.setClan("", Integer.MIN_VALUE); // signal to clear the list
					for(String entry : list)
					{
						entry=entry.trim();
						String clanID=entry;
						int role=-1;
						final int x=entry.lastIndexOf('(');
						if(x>0)
						{
							clanID=entry.substring(0,x).trim();
							if(entry.endsWith(")"))
								role=CMath.s_int(entry.substring(x+1,entry.length()-1));
							else
								role=CMath.s_int(entry.substring(x+1));
						}
						Clan C=CMLib.clans().getClan(clanID);
						if(C==null)
							C=CMLib.clans().findClan(clanID);
						if(C!=null)
						{
							if(role<0)
								role=C.getGovernment().getAcceptPos();
							M.setClan(C.clanID(), role);
						}
					}
					break;
				}
				case TATTOOS: // tattoos
					{
						final List<String> V=CMParms.parseSemicolons(old,true);
						for(final Enumeration<Tattoo> e=M.tattoos();e.hasMoreElements();)
							M.delTattoo(e.nextElement());
						for(final String tatt : V)
							M.addTattoo(((Tattoo)CMClass.getCommon("DefaultTattoo")).parse(tatt));
					}
					break;
				case EXPERTISES: // expertises
					{
						final List<String> V=CMParms.parseSemicolons(old,true);
						M.delAllExpertises();
						for(int v=0;v<V.size();v++)
							M.addExpertise(V.get(v));
					}
					break;
				case BUDGET: // budget
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setBudget(old);
					break;
				case DEVALRATE: // devaluation rate
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setDevalueRate(old);
					break;
				case INVRESETRATE: // inventory reset rate
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setInvResetRate(CMath.s_int(old));
					break;
				case IMAGE: // image
					M.setImage(old);
					break;
				case ISPOSTMAN: // is postman
					break;
				case POSTCHAIN: // postal chain
					if(M instanceof PostOffice)
						((PostOffice)M).setPostalChain(old);
					break;
				case POSTMIN: // minimum postage
					if(M instanceof PostOffice)
						((PostOffice)M).setMinimumPostage(CMath.s_double(old));
					break;
				case POSTLBS: // postage per pound after first
					if(M instanceof PostOffice)
						((PostOffice)M).setPostagePerPound(CMath.s_double(old));
					break;
				case POSTHOLD: // holding fee per pound per month
					if(M instanceof PostOffice)
						((PostOffice)M).setHoldFeePerPound(CMath.s_double(old));
					break;
				case POSTNEW: // new box fee
					if(M instanceof PostOffice)
						((PostOffice)M).setFeeForNewBox(CMath.s_double(old));
					break;
				case POSTHELD: // maximum months held
					if(M instanceof PostOffice)
						((PostOffice)M).setMaxMudMonthsHeld(CMath.s_int(old));
					break;
				case ISLIBRARIAN: // is librarian
					break;
				case LIBRCHAIN: // library chain
					if(M instanceof Librarian)
						((Librarian)M).setLibraryChain(old);
					break;
				case LIBROVERCHG: // library overdue charge
					if(M instanceof Librarian)
						((Librarian)M).setOverdueCharge(CMath.s_double(old));
					break;
				case LIBRDAYCHG: // library daily overdue charge
					if(M instanceof Librarian)
						((Librarian)M).setDailyOverdueCharge(CMath.s_double(old));
					break;
				case LIBROVERPCT: // library overdue pct charge
					if(M instanceof Librarian)
						((Librarian)M).setOverdueChargePct(CMath.s_pct(old));
					break;
				case LIBDAYPCT: // library daily overdue pct charge
					if(M instanceof Librarian)
						((Librarian)M).setDailyOverdueChargePct(CMath.s_pct(old));
					break;
				case LIBMINDAYS: // library overdue days
					if(M instanceof Librarian)
						((Librarian)M).setMinOverdueDays(CMath.s_int(old));
					break;
				case LIBMAXDAYS: // library reclaim days
					if(M instanceof Librarian)
						((Librarian)M).setMaxOverdueDays(CMath.s_int(old));
					break;
				case MAXLISTINGS: // max listings
					if(M instanceof CraftBroker)
						((CraftBroker)M).setMaxListings(CMath.s_int(old));
					break;
				case COMMISSIONPCT:
					if(M instanceof CraftBroker)
						((CraftBroker)M).setCommissionPct(CMath.s_pct(old));
					break;
				case LIBMAXBORROW: // library max borrowed
					if(M instanceof Librarian)
						((Librarian)M).setMaxBorrowed(CMath.s_int(old));
					break;
				case LIBCMASK: // library contributor mask
					if(M instanceof Librarian)
						((Librarian)M).setContributorMask(old);
					break;
				case IGNOREMASK: // shopkeeper ignore mask
					if(M instanceof ShopKeeper)
						((ShopKeeper)M).setIgnoreMask(old);
					break;
				case LOANINT: // loan interest
					if((M instanceof Banker)&&(old.length()>0))
						((Banker)M).setLoanInterest(CMath.s_double(old));
					break;
				case SVCRIT: // service ritual
					if(M instanceof Deity)
						((Deity)M).setServiceRitual(old);
					break;
				case AUCCHAIN: // auction house
					if(M instanceof Auctioneer)
						((Auctioneer)M).setAuctionHouse(old);
					break;
				case BROCHAIN: // auction house
					if(M instanceof CraftBroker)
						((CraftBroker)M).setBrokerChain(old);
					break;
				case LIVELIST: // live list
					//if(M instanceof Auctioneer)
					//	if(old.length()==0)
					//		((Auctioneer)M).setLiveListingPrice(-1.0);
					//	else
					//		((Auctioneer)M).setLiveListingPrice(CMath.s_double(old));
					break;
				case TIMELIST: // timed list
					if(M instanceof Auctioneer)
					{
						if(old.length()==0)
							((Auctioneer)M).setTimedListingPrice(-1.0);
						else
							((Auctioneer)M).setTimedListingPrice(CMath.s_double(old));
					}
					break;
				case TIMELISTPCT: // timed list pct
					if(M instanceof Auctioneer)
					{
						if(old.length()==0)
							((Auctioneer)M).setTimedListingPct(-1.0);
						else
							((Auctioneer)M).setTimedListingPct(CMath.s_pct(old));
					}
					break;
				case LIVECUT: // live cut
					//if(M instanceof Auctioneer)
					//	if(old.length()==0)
					//		((Auctioneer)M).setLiveFinalCutPct(-1.0);
					//	else
					//		((Auctioneer)M).setLiveFinalCutPct(CMath.s_pct(old));
					break;
				case TIMECUT: // timed cut
					if(M instanceof Auctioneer)
					{
						if(old.length()==0)
							((Auctioneer)M).setTimedFinalCutPct(-1.0);
						else
							((Auctioneer)M).setTimedFinalCutPct(CMath.s_pct(old));
					}
					break;
				case MAXDAYS: // max days
					if(M instanceof Auctioneer)
					{
						if(old.length()==0)
							((Auctioneer)M).setMaxTimedAuctionDays(-1);
						else
							((Auctioneer)M).setMaxTimedAuctionDays(CMath.s_int(old));
					}
					else
					if(M instanceof CraftBroker)
					{
						if(old.length()==0)
							((CraftBroker)M).setMaxTimedListingDays(-1);
						else
							((CraftBroker)M).setMaxTimedListingDays(CMath.s_int(old));
					}
					break;
				case MINDAYS: // min days
					if(M instanceof Auctioneer)
					{
						if(old.length()==0)
							((Auctioneer)M).setMinTimedAuctionDays(-1);
						else
							((Auctioneer)M).setMinTimedAuctionDays(CMath.s_int(old));
					}
					break;
				case ISAUCTION: // is auction
					break;
				case ISBROKER: // is broker
					break;
				case DEITYID: // deity
					/*
					if(old.length()==0)
						M.setWorshipCharID("");
					else
					if(CMLib.map().getDeity(old)!=null)
						M.setWorshipCharID(CMLib.map().getDeity(old).Name());
					*/
					break;
				case VARMONEY: // money variation
					M.setMoneyVariation(CMath.s_double(old));
					break;
				case CATACAT: // catacat
					if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
					{
						if(cataData==null)
							cataData=CMLib.catalog().sampleCataData("");
						cataData.setCategory(old.toUpperCase().trim());
					}
					break;
				case MOUNTSTR: // mountstr
					if(M instanceof Rideable)
						((Rideable) M).setMountString(old);
					break;
				case DISMOUNTSTR: // dismountstr
					if(M instanceof Rideable)
						((Rideable) M).setDismountString(old);
					break;
				case STATESTR: // statestr
					if(M instanceof Rideable)
						((Rideable) M).setStateString(old);
					break;
				case STATESUBJSTR: // statesubjstr
					if(M instanceof Rideable)
						((Rideable) M).setStateStringSubject(old);
					break;
				case RIDERSTR: // riderstr
					if(M instanceof Rideable)
						((Rideable) M).setRideString(old);
					break;
				case LIQUIDTYPES: // liquid types
					if((M instanceof LiquidHolder)&&(!(M instanceof Potion)))
						((LiquidHolder)M).setLiquidType(CMath.s_int(old));
					break;
				case ISDRINK: // is drink
					break;
				case LIQUIDHELD: // liquid held
					if(M instanceof LiquidHolder)
					{
						((LiquidHolder)M).setLiquidHeld(CMath.s_int(old));
						((LiquidHolder)M).setLiquidRemaining(CMath.s_int(old));
					}
					break;
				case QUENCHED: // quenched
					if(M instanceof Drink)
						((Drink)M).setThirstQuenched(CMath.s_int(old));
					break;
				}
			}

			if(M.isGeneric())
			{
				String error=GrinderExits.dispositions(M,httpReq,parms);
				if(error.length()>0)
					return error;
				error=GrinderMobs.senses(M,httpReq,parms);
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
				error=GrinderAreas.doAffects(M,httpReq,parms); // after abilities because of auto-invoking crap
				if(error.length()>0)
					return error;
				error=GrinderMobs.clans(M,httpReq,parms);
				if(error.length()>0)
					return error;
				if(M instanceof Deity)
				{
					error=GrinderMobs.blessings((Deity)M,httpReq,parms);
					if(error.length()>0)
						return error;
					error=GrinderMobs.curses((Deity)M,httpReq,parms);
					if(error.length()>0)
						return error;
					error=GrinderMobs.powers((Deity)M,httpReq,parms);
					if(error.length()>0)
						return error;
				}

				error=GrinderMobs.items(M,allitems,httpReq);
				if(error.length()>0)
					return error;

				if((M instanceof ShopKeeper)
				&&(httpReq.isUrlParameter("SHP1")))
				{
					final ShopKeeper SK=(ShopKeeper)M;
					final CoffeeShop shop=(SK instanceof Librarian)?((Librarian)SK).getBaseLibrary():SK.getShop();
					final XVector<Environmental> inventory=new XVector<Environmental>(shop.getStoreInventory());
					shop.emptyAllShelves();

					int num=1;
					String MATCHING=httpReq.getUrlParameter("SHP"+num);
					String theparm=httpReq.getUrlParameter("SDATA"+num);
					String theprice=httpReq.getUrlParameter("SPRIC"+num);
					while((MATCHING!=null)&&(theparm!=null))
					{
						if(CMath.isNumber(MATCHING)&&(inventory.size()>0))
						{
							final Environmental O=inventory.elementAt(CMath.s_int(MATCHING)-1);
							if(O!=null)
								shop.addStoreInventory(O,CMath.s_int(theparm),CMath.s_int(theprice));
						}
						else
						if(MATCHING.startsWith("CATALOG-"))
						{
							Environmental O=CMLib.webMacroFilter().getMOBFromCatalog(MATCHING);
							if(O==null)
								O=CMLib.webMacroFilter().findItemInAnything(null,MATCHING);
							if(O!=null)
								shop.addStoreInventory((Environmental)O.copyOf(),CMath.s_int(theparm),CMath.s_int(theprice));
						}
						else
						if(MATCHING.indexOf('@')>0)
						{
							Environmental O=CMLib.webMacroFilter().getMOBFromAnywhere(MATCHING);
							if(O==null)
								O=CMLib.webMacroFilter().findItemInAnything(null,MATCHING);
							if(O!=null)
								shop.addStoreInventory((Environmental)O.copyOf(),CMath.s_int(theparm),CMath.s_int(theprice));
						}
						else
						{
							Environmental O=null;
							for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
							{
								final MOB M2=m.nextElement();
								if(CMClass.classID(M2).equals(MATCHING)&&(!M2.isGeneric()))
								{
									O=(MOB)M2.copyOf();
									break;
								}
							}
							if(O==null)
							for(final Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
							{
								final Ability A2=a.nextElement();
								if(CMClass.classID(A2).equals(MATCHING))
								{
									O=(Ability)A2.copyOf();
									break;
								}
							}
							if(O==null)
								O=CMLib.webMacroFilter().findItemInAnything(null,MATCHING);
							if(O!=null)
								shop.addStoreInventory((Environmental)O.copyOf(),CMath.s_int(theparm),CMath.s_int(theprice));
						}
						num++;
						MATCHING=httpReq.getUrlParameter("SHP"+num);
						theparm=httpReq.getUrlParameter("SDATA"+num);
						theprice=httpReq.getUrlParameter("SPRIC"+num);
					}
				}

				int num=1;
				if((M instanceof Economics)
				&&(httpReq.isUrlParameter("IPRIC1")))
				{
					final List<String> prics=new ArrayList<String>();
					String DOUBLE=httpReq.getUrlParameter("IPRIC"+num);
					String MASK=httpReq.getUrlParameter("IPRICM"+num);
					while((DOUBLE!=null)&&(MASK!=null))
					{
						if(CMath.isNumber(DOUBLE))
							prics.add((DOUBLE+" "+MASK).trim());
						num++;
						DOUBLE=httpReq.getUrlParameter("IPRIC"+num);
						MASK=httpReq.getUrlParameter("IPRICM"+num);
					}
					((Economics)M).setItemPricingAdjustments(CMParms.toStringArray(prics));
				}
			}

			M.recoverPhyStats();
			M.recoverCharStats();
			M.recoverMaxState();
			M.resetToMaxState();
			M.text();
			String newMobCode=null;
			String newShopMobCode=null;
			if(R==null)
			{
				if(mobCode.startsWith("CATALOG-")||mobCode.startsWith("NEWCATA-"))
				{
					final MOB M2=CMLib.catalog().getCatalogMob(mobCode.substring(8));
					if((M2!=null)&&(!M.Name().equalsIgnoreCase(M2.Name())))
						M.setName(M2.Name());
					newMobCode=mobCode;
					if(M2==null)
					{
						String category=null;
						if(cataData!=null)
							category=cataData.category();
						CMLib.catalog().addCatalog(category,M);
						Log.infoOut("GrinderItems",whom.Name()+" created catalog MOB "+M.Name());
					}
					else
					{
						if(cataData!=null)
						{
							final CatalogLibrary.CataData data=CMLib.catalog().getCatalogMobData(M.Name());
							data.build(cataData.data(null));
						}
						CMLib.catalog().updateCatalog(M);
						Log.infoOut("GrinderItems",whom.Name()+" updated catalog MOB "+M.Name());
					}
					copyMOB=M;
				}
				else
				{
					CMLib.webMacroFilter().contributeMOBsToWebCache(new XVector<MOB>(M));
					final MOB M2=CMLib.webMacroFilter().findMOBMatchInWebCache(M);
					newMobCode=CMLib.webMacroFilter().findMOBWebCacheCode(M2);
				}
			}
			else
			if((shopMobCode!=null)
			&&(shopMobCode.length()>0)
			&&(shopM instanceof ShopKeeper))
			{
				if(shopMobCode.equals("NEW")||shopMobCode.equals("NEWDEITY"))
					((ShopKeeper)shopM).getShop().addStoreInventory(M);
				CMLib.webMacroFilter().contributeMOBsToWebCache(new XVector<MOB>(M));
				final MOB M2=CMLib.webMacroFilter().findMOBMatchInWebCache(M);
				newShopMobCode=CMLib.webMacroFilter().findMOBWebCacheCode(M2);
				CMLib.database().DBUpdateMOBs(R);
				newMobCode=CMLib.webMacroFilter().findMOBWebCacheCode(R,shopM);
			}
			else
			{
				if(mobCode.equals("NEW")||mobCode.equals("NEWDEITY"))
				{
					M.bringToLife(R,true);
				}
				else
				if(M!=oldM)
				{
					oldM.destroy();
					R.delInhabitant(oldM);
					M.bringToLife(R,true);
				}
				else
				if(!M.Name().equals(copyMOB.Name()))
				{

				}
				R.recoverRoomStats();
				CMLib.database().DBUpdateMOBs(R);
				newMobCode=CMLib.webMacroFilter().findMOBWebCacheCode(R,M);
			}
			if((newShopMobCode!=null)&&(newShopMobCode.length()>0))
			{
				httpReq.addFakeUrlParameter("SHOPMOB", newShopMobCode);
			}
			if(newMobCode!=null)
				httpReq.addFakeUrlParameter("MOB",newMobCode);
			if(!copyMOB.sameAs(M))
				Log.sysOut("Grinder",whom.Name()+" modified mob "+copyMOB.Name()+((R!=null)?" in room "+R.roomID():"")+".");
		}
		return "";
	}
}
