 package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class CharGen extends StdCommand
{
	public CharGen()
	{
	}

	private final String[]	access	= I(new String[] { "CHARGEN" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected void equipPlayer(final MOB M)
	{
		final int level = M.basePhyStats().level();
		final CharClass C=M.baseCharStats().getCurrentClass();
		for(final long wornCode : Wearable.CODES.ALL())
		{
			if((wornCode == Wearable.IN_INVENTORY)
			|| (wornCode == Wearable.WORN_HELD)
			|| (wornCode == Wearable.WORN_MOUTH))
				 continue;
			if(wornCode==Wearable.WORN_WIELD)
			{
				final Weapon W=CMClass.getWeapon("GenWeapon");
				W.setWeaponClassification(Weapon.CLASS_SWORD);
				W.setWeaponDamageType(Weapon.TYPE_SLASHING);
				W.setMaterial(RawMaterial.RESOURCE_STEEL);
				W.setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				W.setRawLogicalAnd(true);
				switch(C.allowedWeaponLevel())
				{
				case CharClass.WEAPONS_EVILCLERIC:
					CMLib.factions().setAlignment(M,Faction.Align.EVIL);
				//$FALL-THROUGH$
				case CharClass.WEAPONS_THIEFLIKE:
				case CharClass.WEAPONS_BURGLAR:
				case CharClass.WEAPONS_ANY:
				case CharClass.WEAPONS_NEUTRALCLERIC:
					break;
				case CharClass.WEAPONS_DAGGERONLY:
					W.setWeaponClassification(Weapon.CLASS_DAGGER);
					W.setWeaponDamageType(Weapon.TYPE_PIERCING);
					break;
				case CharClass.WEAPONS_NATURAL:
					W.setMaterial(RawMaterial.RESOURCE_OAK);
					break;
				case CharClass.WEAPONS_ROCKY:
					W.setMaterial(RawMaterial.RESOURCE_STONE);
					break;
				case CharClass.WEAPONS_GOODCLERIC:
					CMLib.factions().setAlignment(M,Faction.Align.GOOD);
				//$FALL-THROUGH$
				case CharClass.WEAPONS_MAGELIKE:
					W.setMaterial(RawMaterial.RESOURCE_OAK);
					W.setWeaponClassification(Weapon.CLASS_STAFF);
					W.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case CharClass.WEAPONS_ALLCLERIC:
					if(CMLib.flags().isGood(M))
					{
						W.setMaterial(RawMaterial.RESOURCE_OAK);
						W.setWeaponClassification(Weapon.CLASS_STAFF);
						W.setWeaponDamageType(Weapon.TYPE_BASHING);
					}
					break;
				case CharClass.WEAPONS_FLAILONLY:
					W.setWeaponClassification(Weapon.CLASS_FLAILED);
					W.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				}
				W.basePhyStats().setLevel(level);
				W.basePhyStats().setWeight(8);
				W.recoverPhyStats();
				CMLib.itemBuilder().balanceItemByLevel(W);
				M.addItem(W);
				W.wearIfPossible(M);
			}
			else
			if(wornCode != Wearable.WORN_FLOATING_NEARBY)
			{
				final Armor A=CMClass.getArmor("GenArmor");
				A.setRawProperLocationBitmap(wornCode);
				A.setMaterial(RawMaterial.RESOURCE_STEEL);
				if((CharClass.ARMOR_WEARMASK & wornCode) > 0)
				switch(C.allowedArmorLevel())
				{
				case CharClass.ARMOR_ANY:
				case CharClass.ARMOR_METALONLY:
					break;
				case CharClass.ARMOR_CLOTH:
					A.setMaterial(RawMaterial.RESOURCE_COTTON);
					break;
				case CharClass.ARMOR_LEATHER:
					A.setMaterial(RawMaterial.RESOURCE_LEATHER);
					break;
				case CharClass.ARMOR_NONMETAL:
				case CharClass.ARMOR_VEGAN:
				case CharClass.ARMOR_OREONLY:
					A.setMaterial(RawMaterial.RESOURCE_OAK);
					break;
				}
				A.basePhyStats().setLevel(level);
				A.basePhyStats().setWeight(8);
				A.recoverPhyStats();
				CMLib.itemBuilder().balanceItemByLevel(A);
				M.addItem(A);
				A.wearIfPossible(M);
			}
		}
	}

	protected MOB levelMOBup(final int level, final CharClass C, final boolean player)
	{
		final MOB mob=CMClass.getFactoryMOB();
		CMLib.factions().setAlignment(mob,Faction.Align.NEUTRAL);
		mob.setName(L("Average Joe"));
		if(player)
			mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		for(final int i : CharStats.CODES.BASECODES())
			mob.baseCharStats().setStat(i,10);
		mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,11);
		mob.baseCharStats().setStat(CharStats.STAT_WISDOM,11);
		mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,11);
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,1);
		mob.basePhyStats().setArmor(100);
		mob.basePhyStats().setLevel(1);
		mob.basePhyStats().setSensesMask(0);
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.baseCharStats().getMyRace().startRacing(mob,false);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);

		final int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		for(int lvl=1;lvl<level;lvl++)
		{
			if((lvl % 3)==0)
			{
				int stat=-1;
				int bestDiff=0;
				for(final int i: CharStats.CODES.BASECODES())
				{
					final int base = max + mob.charStats().getStat(CharStats.CODES.toMAXBASE(i));
					final int diff = base - mob.baseCharStats().getStat(i);
					if(diff >= bestDiff)
					{
						stat=i;
						bestDiff = diff;
					}
				}
				if(stat>=0)
				if(mob.baseCharStats().getStat(stat)<(max+mob.charStats().getStat(CharStats.CODES.toMAXBASE(stat))))
					mob.baseCharStats().setStat(stat,mob.baseCharStats().getStat(stat)+1);
			}
			if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
				CMLib.leveler().level(mob);
			else
				CMLib.leveler().postExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
			mob.recoverPhyStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
		equipPlayer(mob);
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			a.nextElement().setProficiency(100);
		for(int a=0;a<mob.numEffects();a++) // personal
			mob.fetchEffect(a).setProficiency(100);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		return mob;
	}

	public void averageout(final MOB avgMob, final int tries)
	{
		for(final int i : CharStats.CODES.BASECODES())
			avgMob.baseCharStats().setStat(i,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(i),tries)));
		avgMob.basePhyStats().setArmor((int)Math.round(CMath.div(avgMob.basePhyStats().armor(),tries)));
		avgMob.baseState().setHitPoints((int)Math.round(CMath.div(avgMob.baseState().getHitPoints(),tries)));
		avgMob.baseState().setMovement((int)Math.round(CMath.div(avgMob.baseState().getMovement(),tries)));
		avgMob.baseState().setMana((int)Math.round(CMath.div(avgMob.baseState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverPhyStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
		avgMob.setTrains(0);
	}

	public void addHimIn(final MOB avgMob, final MOB mob2)
	{
		for(final int i : CharStats.CODES.BASECODES())
			avgMob.baseCharStats().setStat(i,avgMob.baseCharStats().getStat(i)+mob2.baseCharStats().getStat(i));
		avgMob.basePhyStats().setArmor(avgMob.basePhyStats().armor()+mob2.basePhyStats().armor());
		avgMob.baseState().setHitPoints(avgMob.baseState().getHitPoints()+mob2.baseState().getHitPoints());
		avgMob.baseState().setMovement(avgMob.baseState().getMovement()+mob2.baseState().getMovement());
		avgMob.baseState().setMana(avgMob.baseState().getMana()+mob2.baseState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverPhyStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
	}

	public MOB AverageClassMOB(final MOB mob, final int level, final CharClass C, final int numTries, final boolean player)
	{
		final MOB avgMob=levelMOBup(level,C, player);
		int tries=1;
		final Session sess=(mob!=null)?mob.session():null;
		for(;tries<numTries;tries++)
		{
			if(((tries % 20)==0)&&(sess!=null))
			{
				if(sess.isStopped())
					return avgMob;
				sess.print(".");
			}
			final MOB mob2=levelMOBup(level,C, player);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries);
		return avgMob;
	}

	public MOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB, boolean player)
	{
		MOB avgMob=null;
		int tries=0;
		int numClasses=0;
		for(;tries<numTriesClass;tries++)
		{
			for(final Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=(CharClass)c.nextElement();
				if(C.availabilityCode()!=0)
				{
					numClasses++;
					final MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB,player);
					if(avgMob==null)
					{
						avgMob=mob2;
						numClasses--;
					}
					else
						addHimIn(avgMob,mob2);
					if(avgMob!=mob2)
						mob2.destroy();
				}
			}
		}
		averageout(avgMob,numClasses);
		return avgMob;
	}

	private class CombatStats
	{
		int levelStart=1;
		int levelEnd=91;
		int TOTAL_ITERATIONS=100;
		int skipLevels=0;
		int[][][] allData=null;//new int[classSet.size()][levelEnd-levelStart+1][17];
		String[][][] allSkills=null;//new String[classSet.size()][levelEnd-levelStart+1][4];
		Area A=null;
		MOB mob=null;
		DVector classSet=new DVector(2);
		Hashtable<String,int[]> failSkillCheck=null;
	}

	public void combatRun(MOB mob, List<String> commands)
	{
		final CombatStats c=new CombatStats();
		final Room room=mob.location();
		c.mob=mob;
		if(commands.size()==0)
		{
			mob.tell(L("USAGE: CHARGEN COMBAT ([CHARCLASS(S)]...) (EXPORT=FILENAME) (FAILCHECK) (MOB=[MOB NAME]) (ITERATIONS=[X]) (SKIPLEVELS=[X]) ([START LEVEL]) ([END LEVEL])"));
			return;
		}
		final String[][] CAMATCH={
			{"Commoner","CombatAbilities"},
			{"Bard","Bardness"},
			{"Cleric","Clericness"},
			{"Druid","Druidness"},
			{"Mage","Mageness"},
			{"Thief","Thiefness"},
			{"Fighter","Fighterness"},
		};

		for(final Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			final CharClass C=(CharClass)e.nextElement();
			if(CMLib.login().isAvailableCharClass(C))
			{
				String behav="CombatAbilities";
				for (final String[] element : CAMATCH)
				{
					if(C.baseClass().equalsIgnoreCase(element[0]))
						behav=element[1];
				}
				c.classSet.add(C,behav);
			}
		}

		// set the parameters
		boolean classCleared=false;
		boolean nextLevel=false;
		String fileExp=null;
		MOB tempBadGuyM=null;
		for(int i=0;i<commands.size();i++)
		{
			String s=commands.get(i);
			if(CMath.isInteger(s))
			{
				final int x=CMath.s_int(s);
				if(x>=0)
				{
					if((nextLevel)&&(x>=c.levelStart))
						c.levelEnd=x;
					else
					if(!nextLevel)
					{
						c.levelStart=x;
						c.levelEnd=x;
						nextLevel=true;
					}
				}
			}
			else
			if(s.toUpperCase().startsWith("ITERATIONS="))
			{
				s=s.substring("ITERATIONS=".length());
				if(CMath.isInteger(s))
					c.TOTAL_ITERATIONS=CMath.s_int(s);
			}
			else
			if(s.toUpperCase().startsWith("EXPORT="))
				fileExp=s.substring("EXPORT=".length());
			else
			if(s.toUpperCase().startsWith("MOB=") && (room!=null))
			{
				String mobName=s.substring("MOB=".length());
				MOB M=room.fetchInhabitant(mobName);
				if(M==null)
				{
					M=CMLib.catalog().getCatalogMob(mobName);
					if(M!=null)
					{
						M=(MOB)M.copyOf();
						CMLib.catalog().changeCatalogUsage(M,true);
					}
				}
				if((M==null)&&(room.getArea()!=null))
					M=CMLib.map().findFirstInhabitant(room.getArea().getMetroMap(), mob, mobName, 10);
				if(M==null)
					M=CMLib.map().findFirstInhabitant(CMLib.map().rooms(), mob, mobName, 10);
				if(M==null)
				{
					mob.tell(L("Unknown mob '@x1'",mobName));
					return;
				}
				tempBadGuyM=M;
			}
			else
			if(s.toUpperCase().startsWith("SKIPLEVELS="))
			{
				c.skipLevels=CMath.s_int(s.substring("SKIPLEVELS=".length()));
				if(c.skipLevels<1)
					c.skipLevels=1;
			}
			else
			if(CMClass.findCharClass(s)!=null)
			{
				final CharClass C=CMClass.findCharClass(s);
				if(!classCleared)
				{
					classCleared=true;
					c.classSet=new DVector(2);
				}
				String behav="CombatAbilities";
				for (final String[] element : CAMATCH)
				{
					if(C.baseClass().equalsIgnoreCase(element[0]))
						behav=element[1];
				}
				c.classSet.add(C,behav);
			}
			else
			if(s.endsWith("s"))
			{
				s=s.substring(0,s.length()-1);
				for(final Enumeration e=CMClass.charClasses();e.hasMoreElements();)
				{
					final CharClass C=(CharClass)e.nextElement();
					if(CMLib.login().isAvailableCharClass(C)
					&&(C.baseClass().equalsIgnoreCase(s)||(s.equalsIgnoreCase("charclasse"))))
					{
						if(!classCleared)
						{
							classCleared=true;
							c.classSet=new DVector(2);
						}
						String behav="CombatAbilities";
						for (final String[] element : CAMATCH)
						{
							if(C.baseClass().equalsIgnoreCase(element[0]))
								behav=element[1];
						}
						c.classSet.add(C,behav);
					}
				}
			}
			else
			if(s.equalsIgnoreCase("FAILCHECK"))
				c.failSkillCheck=new Hashtable<String,int[]>();
		}

		final MOB badGuyM=tempBadGuyM;
		
		if(c.skipLevels<=0)
		{
			c.skipLevels=1;
			if((c.levelStart==1)&&(c.levelEnd==91))
				c.skipLevels=15;
		}

		c.A=CMClass.getAreaType("StdArea");
		c.A.setName(L("UNKNOWNAREA"));
		CMLib.map().addArea(c.A);
		c.allData=new int[c.classSet.size()][c.levelEnd-c.levelStart+1][17];
		c.allSkills=new String[c.classSet.size()][c.levelEnd-c.levelStart+1][4];

		final String[] allDataHeader={
			"BestIterScore",//0
			"BestHitScore",//1
			"BestSingleHitScore",//2
			"BestSingleHitPhys",//3
			"Losses",//4
			"MedScore",//5
			"MedHitPct",//6
			"LossIters",//7
			"MedWinIters",//8
			"MedPhysDone",//9
			"MedPhysTaken",//10
			"MedIsHitPct",//11
			"LostRounds",//12
			"PlayerArmor",//13
			"PlayerAttack",//15
			"PlayerDamPct",//16
			"PlayerManaPct",//17
			"BestIterSkill",//18
			"BestHitSkill",//19
			"BestSingleHitSkill",//20
		};

		final boolean[] aborted = new boolean[1];
		final Session sess=mob.session();
		aborted[0]=false;
		final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(c.classSet.size());
		final java.util.concurrent.atomic.AtomicInteger IDGEN=new java.util.concurrent.atomic.AtomicInteger(1);
		final Race humanR=CMClass.getRace("Human");
		for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
		{
			new Thread(Thread.currentThread().getThreadGroup(),"CharGen"+Thread.currentThread().getThreadGroup().getName().charAt(0))
			{
				CombatStats	c;
				int			charClassDex;
				String		fileExp;
				boolean[]	aborted;
				java.util.concurrent.CountDownLatch latch = null;

				public void start(CombatStats c, int charClassDex, String fileExp, java.util.concurrent.CountDownLatch latch, boolean[] aborted)
				{
					this.c=c;
					this.charClassDex=charClassDex;
					this.fileExp=fileExp;
					this.latch=latch;
					this.aborted=aborted;
					this.start();
				}

				@Override
				public void run()
				{
					final MOB mob=c.mob;
					final int[][][] allData = c.allData;
					final String[][][] allSkills=c.allSkills;
					final DVector classSet=c.classSet;
					final int levelStart=c.levelStart;
					for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
					{
						final CharClass C=(CharClass)c.classSet.get(charClassDex,1);
						mob.tell(C.ID()+": "+level);
						int roomRobin=0;
						Room R=null;

						final int[] bestSingleHitScore = new int[] { 0 };
						final String[] bestSingleHitSkill = new String[] { "" };
						final int[] bestSingleHitPhys = new int[] { 0 };
						final int[] bestHitScore = new int[] { 0 };
						final String[] bestHitSkill = new String[] { "" };
						final int[] bestIterScore = new int[] { Integer.MAX_VALUE };
						final String[] bestIterSkill = new String[] { "" };

						final int[] losses=new int[]{0};

						final XVector<Integer> medScore=new XVector<Integer>();
						final XVector<Integer> medWinIters=new XVector<Integer>();
						final XVector<Integer> medPhysDone=new XVector<Integer>();
						final XVector<Double> medHitPct=new XVector<Double>();
						final XVector<Double> medIsHitPct=new XVector<Double>();
						final XVector<Integer> medPhysTaken=new XVector<Integer>();
						final XVector<Integer> medLossIters=new XVector<Integer>();
						final XVector<Double> medPlayerDamPct = new XVector<Double>();
						final XVector<Double> medPlayerManaPct = new XVector<Double>();

						int H1=0;
						int H2=0;
						boolean playerExampleShown=false;
						int lastPct=0;
						int playerArmor=0;
						int playerAttack=0;
						for(int tries=0;tries<c.TOTAL_ITERATIONS;tries++)
						{
							if((CMath.div(tries,c.TOTAL_ITERATIONS)*100.0)>=lastPct+5)
							{
								lastPct+=5;
								if(mob.session()!=null)
									mob.session().print(".");
							}
							final Behavior B1=CMClass.getBehavior((String)classSet.get(charClassDex,2));
							B1.setParms(C.ID()+" NOSTAT NOCOMBATSTAT");
							switch(roomRobin)
							{
							case 0:
								R = CMClass.getLocale("Woods");
								break;
							case 1:
								R = CMClass.getLocale("CaveRoom");
								break;
							case 2:
								R = CMClass.getLocale("CityStreet");
								break;
							}
							if((++roomRobin)>2)
								roomRobin=0;
							if(R!=null)
							{
								R.addNonUninvokableEffect(CMClass.getAbility("Spell_Light"));
								R.setRoomID(c.A.name()+"#"+IDGEN.addAndGet(1));
								R.setArea(c.A);
								R.recoverPhyStats();
							}
							c.A.getTimeObj().setHourOfDay(CMLib.dice().roll(1,c.A.getTimeObj().getHoursInDay(),-1));

							//Session S=(Session)CMClass.getCommon("FakeSession");
							//S.initializeSession(null,"MEMORY");
							MOB M1=null;
							if(C.ID().equalsIgnoreCase("StdCharClass"))
							{
								M1=CMClass.getFactoryMOB();  // MOB stat
								final Behavior B2=CMClass.getBehavior("CombatAbilities");
								M1.baseCharStats().setMyRace(humanR);
								M1.basePhyStats().setLevel(level);
								M1.setName(L("GOODGUY"));
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.setLocation(R);
								M1.baseCharStats().getMyRace().setHeightWeight(M1.basePhyStats(),(char)M1.baseCharStats().getStat(CharStats.STAT_GENDER));
								M1.basePhyStats().setAbility(CMProps.getMobHPBase());
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.recoverMaxState();
								M1.resetToMaxState();
								M1.addBehavior(B2);
								M1.bringToLife(R,true);
								CMLib.threads().deleteTick(M1,Tickable.TICKID_MOB);
								CMLib.leveler().fillOutMOB(M1,level);
								//int hp=M1CMLib.leveler().getLevelMOBPlayerHP(M1);
								M1.setWimpHitPoint(0);
								M1.recoverMaxState();
								M1.recoverCharStats();
								M1.recoverPhyStats();
							}
							else
							{
								M1=AverageClassMOB(null,level,C,1,false);
								M1.baseCharStats().setMyRace(humanR);
								M1.setName(L("GOODGUY"));
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.setLocation(R);
								M1.baseCharStats().getMyRace().setHeightWeight(M1.basePhyStats(),(char)M1.baseCharStats().getStat(CharStats.STAT_GENDER));
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.recoverMaxState();
								M1.resetToMaxState();
								M1.bringToLife(M1.location(),true);
								CMLib.threads().deleteTick(M1,Tickable.TICKID_MOB);
								M1.setWimpHitPoint(0);
								M1.recoverMaxState();
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.resetToMaxState();
								B1.setStat("PRECAST","1");
								M1.addBehavior(B1);
								equipPlayer(M1);
								M1.recoverMaxState();
								M1.recoverCharStats();
								M1.recoverPhyStats();
								B1.setStat("RECORD"," ");
								B1.setStat("PROF","true");
								B1.setStat("LASTSPELL","");
								B1.setStat("PRECAST","1");
								for(int i=0;i<20;i++) // give some pre-cast ticks
									M1.tick(M1,Tickable.TICKID_MOB);
							}
							M1.resetToMaxState();
							playerArmor=CMLib.combat().adjustedArmor(M1);
							playerAttack=CMLib.combat().adjustedAttackBonus(M1,null);

							final MOB M2;
							final Behavior B2;
							if(badGuyM!=null)
							{
								M2=(MOB)badGuyM.copyOf();
								Behavior B=M2.fetchBehavior("CombatAbilities");
								if(B!=null)
									B2=B;
								else
								{
									B2=CMClass.getBehavior("CombatAbilities");
									M2.addBehavior(B2);
								}
								M2.setLocation(R);
								M2.bringToLife(M2.location(),true);
								CMLib.threads().deleteTick(M2,Tickable.TICKID_MOB);
							}
							else
							{
								M2=CMClass.getFactoryMOB();  // MOB stat
								B2=CMClass.getBehavior("CombatAbilities");
								M2.baseCharStats().setMyRace(humanR);
								M2.basePhyStats().setLevel(level);
								M2.setName(L("BADGUY"));
								M2.recoverCharStats();
								M2.recoverPhyStats();
								M2.setLocation(R);
								M2.baseCharStats().getMyRace().setHeightWeight(M2.basePhyStats(),(char)M2.baseCharStats().getStat(CharStats.STAT_GENDER));
								M2.basePhyStats().setAbility(CMProps.getMobHPBase());
								M2.recoverCharStats();
								M2.recoverPhyStats();
								M2.recoverMaxState();
								M2.resetToMaxState();
								M2.addBehavior(B2);
								M2.bringToLife(M2.location(),true);
								CMLib.threads().deleteTick(M2,Tickable.TICKID_MOB);
								CMLib.leveler().fillOutMOB(M2,level);
								final int hp=CMLib.leveler().getPlayerHitPoints(M2);
								if(hp>M2.baseState().getHitPoints())
									M2.baseState().setHitPoints(hp);
							}
							M2.setWimpHitPoint(0);
							M2.recoverMaxState();
							M2.recoverCharStats();
							M2.recoverPhyStats();
							M2.resetToMaxState();

							M1.setVictim(M2);
							M2.setVictim(M1);

							if(!playerExampleShown)
							{
								playerExampleShown=true;
								/*
								StringBuffer msg=CMLib.commands().getScore(M1);
								if(!mob.isMonster())
									mob.session().wraplessPrintln(msg.toString());
								if(mob.session()!=null)
								{
									mob.session().println(L("@x1 has @x2 behavior for @x3 abilities.",M1.name(),B1.ID(),M1.numAbilities()));
									mob.session().print(L("Working.."));
								}
								*/
							}

							H1=M1.curState().getHitPoints();
							H2=M2.curState().getHitPoints();

							int iterations=0;
							int cumScore=0;
							int hits = 0;
							int ishits = 0;
							B1.setStat("PHYSDAMTAKEN","0");
							B2.setStat("PHYSDAMTAKEN","0");
							int zeroCheck=0;
							//MOB[] ZEROMOBS=null;
							String ZEROSKILL1=null;
							String ZEROSKILL2=null;
							String ALMOSTZEROSKILL=null;
							int l1=0, l2=0;

							if((sess!=null)&&(sess.isStopped() || sess.hotkey(1)=='x'))
								aborted[0]=true;
							
							//chargen combat charclasses export=test.tab iterations=100 skiplevels=20 1 91
							while((M1.getVictim()==M2)
								&&(M2.getVictim()==M1)
								&&(!M1.amDead())
								&&(!M2.amDead())
								&&(!M1.amDestroyed())
								&&(!M2.amDestroyed())
								&&(M1.location()==M2.location())
								&&(iterations<1000))
							{
								if(aborted[0])
								{
									latch.countDown();
									return;
								}
								iterations++;
								ALMOSTZEROSKILL=B1.getStat("LASTSPELL");
								final int h1=M1.curState().getHitPoints();
								final int h2=M2.curState().getHitPoints();
								final int L1=l1;
								final int L2=l2;
								l1=CMath.s_int(B2.getStat("PHYSDAMTAKEN"));
								l2=CMath.s_int(B1.getStat("PHYSDAMTAKEN"));
								if(l1>L1)
									hits++;
								if(l2>L2)
									ishits++;
								try
								{
									CMLib.commands().postStand(M1,true);
									CMLib.commands().postStand(M2,true);
									M1.tick(M1,Tickable.TICKID_MOB);
									M2.tick(M2,Tickable.TICKID_MOB);
								}
								catch(final Exception t)
								{
									Log.errOut("CharGen",t);
								}

								int h=h2-(M2.amDead()?0:M2.curState().getHitPoints());
								h=h-(h1-(M1.amDead()?0:M1.curState().getHitPoints()));
								if((h==0)&&((!M1.amDead())&&(!M2.amDead())))
								{
									zeroCheck++;
									if(zeroCheck==1)
									{
										//ZEROMOBS=new MOB[]{(MOB)M1.copyOf(),(MOB)M2.copyOf()};
										ZEROSKILL1=ALMOSTZEROSKILL;
										ZEROSKILL2=B1.getStat("LASTSPELL");
									}
									else
									if(zeroCheck==20)
										Log.errOut("CharGen","Stale combat warning: "+ZEROSKILL1+"/"+ZEROSKILL2);
									else
									if(zeroCheck==100)
									{

										Log.errOut("CharGen","Stale Combat Abort: "+level+"/"+tries+"/"+iterations+"/"+ZEROSKILL1+"/"+ZEROSKILL2);
										//Log.errOut("CharGen",S.afkMessage());
										break;
									}
								}
								else
									zeroCheck=0;
								if(h>bestSingleHitScore[0])
								{
									bestSingleHitScore[0]=h;
									bestSingleHitPhys[0]=CMath.s_int(B2.getStat("PHYSDAMTAKEN"))-l1;
									bestSingleHitSkill[0]=B1.getStat("LASTSPELL");
								}
								cumScore+=h;
							}
							if(iterations>=1000)
								Log.errOut("CharGen","Stale Combat Abort: "+level+"/"+tries+"/"+ZEROSKILL1+"/"+ZEROSKILL2);
							else
							if(M1.amDead()||M2.amDead()||M1.amDestroyed()||M2.amDestroyed())
							{
								if(M1.amDead()||M1.amDestroyed())
									losses[0]++;
								medScore.add(Integer.valueOf(cumScore));
								medPhysDone.add(Integer.valueOf(CMath.s_int(B2.getStat("PHYSDAMTAKEN"))));
								medPhysTaken.add(Integer.valueOf(CMath.s_int(B1.getStat("PHYSDAMTAKEN"))));
								medHitPct.add(Double.valueOf((CMath.div(hits,iterations)*100)));
								medIsHitPct.add(Double.valueOf((CMath.div(ishits,iterations)*100)));
								medPlayerDamPct.add(Double.valueOf(100-(CMath.div(M1.curState().getHitPoints(),H1)*100.0)));
								medPlayerManaPct.add(Double.valueOf(100-(CMath.div(M1.curState().getMana(),M1.maxState().getMana())*100.0)));
								if(M1.amDead())
									medLossIters.add(Integer.valueOf(iterations));
								else
									medWinIters.add(Integer.valueOf(iterations));
								if(cumScore>bestHitScore[0])
								{
									bestHitScore[0]=cumScore;
									bestHitSkill[0]=B1.getStat("RECORD");
								}
								if(M2.amDead())
								{
									if(!M1.amDead())
									if(iterations<bestIterScore[0])
									{
										bestIterScore[0]=iterations;
										bestIterSkill[0]=B1.getStat("RECORD");
									}
								}
								if(c.failSkillCheck!=null)
								{
									final List<String> V=CMParms.parseSemicolons(B1.getStat("RECORD"),true);
									for(int v=0;v<V.size();v++)
									{
										String s=V.get(v).trim();
										boolean failed=false;
										if(s.startsWith("!"))
										{
											failed=true;
											s=s.substring(1);
										}
										int[] times=c.failSkillCheck.get(s);
										if(times==null)
										{
											times=new int[2];
											c.failSkillCheck.put(s,times);
										}
										times[0]++;
										if(failed)
											times[1]++;
									}
								}
							}
							M1.destroy();
							M2.destroy();
							if(R!=null)
							{
								c.A.delProperRoom(R);
								R.destroy();
							}
						}
						medScore.sort();
						medHitPct.sort();
						medLossIters.sort();
						medWinIters.sort();
						medPhysDone.sort();
						medPhysTaken.sort();
						medIsHitPct.sort();
						medPlayerDamPct.sort();
						medPlayerManaPct.sort();
						allData[charClassDex][level-levelStart][0]=bestIterScore[0];
						allData[charClassDex][level-levelStart][1]=bestHitScore[0];
						allData[charClassDex][level-levelStart][2]=bestSingleHitScore[0];
						allData[charClassDex][level-levelStart][3]=bestSingleHitPhys[0];
						allData[charClassDex][level-levelStart][4]=losses[0];
						if(medScore.size()>0)
							allData[charClassDex][level-levelStart][5]=medScore.get((int)Math.round(Math.floor(CMath.mul(0.5,medScore.size())))).intValue();
						if(medHitPct.size()>0)
							allData[charClassDex][level-levelStart][6]=medHitPct.get((int)Math.round(Math.floor(CMath.mul(0.5,medHitPct.size())))).intValue();
						if(medLossIters.size()>0)
							allData[charClassDex][level-levelStart][7]=medLossIters.get((int)Math.round(Math.floor(CMath.mul(0.75,medLossIters.size())))).intValue();
						if(medWinIters.size()>0)
							allData[charClassDex][level-levelStart][8]=medWinIters.get((int)Math.round(Math.floor(CMath.mul(0.25,medWinIters.size())))).intValue();
						if(medPhysDone.size()>0)
							allData[charClassDex][level-levelStart][9]=medPhysDone.get((int)Math.round(Math.floor(CMath.mul(0.5,medPhysDone.size())))).intValue();
						if(medPhysTaken.size()>0)
							allData[charClassDex][level-levelStart][10]=medPhysTaken.get((int)Math.round(Math.floor(CMath.mul(0.5,medPhysTaken.size())))).intValue();
						if(medIsHitPct.size()>0)
							allData[charClassDex][level-levelStart][11]=medIsHitPct.get((int)Math.round(Math.floor(CMath.mul(0.5,medIsHitPct.size())))).intValue();
						allData[charClassDex][level-levelStart][12]=losses[0];
						allData[charClassDex][level-levelStart][13]=playerArmor;
						allData[charClassDex][level-levelStart][14]=playerAttack;
						if(medPlayerDamPct.size()>0)
							allData[charClassDex][level-levelStart][15]=medPlayerDamPct.get((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerDamPct.size())))).intValue();
						if(medPlayerManaPct.size()>0)
							allData[charClassDex][level-levelStart][16]=medPlayerManaPct.get((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerManaPct.size())))).intValue();

						allSkills[charClassDex][level-levelStart][0]=bestIterSkill[0];
						allSkills[charClassDex][level-levelStart][1]=bestHitSkill[0];
						allSkills[charClassDex][level-levelStart][2]=bestSingleHitSkill[0];
						if(mob.session()!=null)
							mob.session().println("!");
						if(fileExp==null)
						{
							mob.tell(L("HITPOINTS: @x1 vs @x2",""+H1,""+H2));
							mob.tell(L("QUICKEST : @x1: @x2",""+bestIterScore[0],bestIterSkill[0]));
							mob.tell(L("MOST DAM : @x1: @x2",""+bestHitScore[0],bestHitSkill[0]));
							mob.tell(L("BEST HIT : @x1, Phys: @x2, Skill: @x3",""+bestSingleHitScore[0],""+bestSingleHitPhys[0],bestSingleHitSkill[0]));
							mob.tell(L("MEDIANS  : HITS: @x1 (@x2%), LOSS ITERS: @x3, WIN ITERS: @x4",
									""+allData[charClassDex][level-levelStart][5],""+allData[charClassDex][level-levelStart][6],""+allData[charClassDex][level-levelStart][7],""+allData[charClassDex][level-levelStart][8]));
							mob.tell(L("MEDIANS  : PHYS DONE: @x1, PHYS TAKEN: @x2 (@x3%)",
									""+allData[charClassDex][level-levelStart][9],""+allData[charClassDex][level-levelStart][10],""+allData[charClassDex][level-levelStart][11]));
							mob.tell(L("LOSSES   : @x1",""+losses[0]));
							if((c.failSkillCheck!=null)&&(c.failSkillCheck.size()>0))
							{
								final StringBuffer fails=new StringBuffer("SKILLFAILS: ");
								for(final Enumeration i=c.failSkillCheck.keys();i.hasMoreElements();)
								{
									final String s=(String)i.nextElement();
									final int[] times=c.failSkillCheck.get(s);
									if(times[1]>0)
									{
										final int pct=(int)Math.round(100.0*CMath.div(times[1],times[0]));
										if(pct>20)
											fails.append(s+"("+pct+"%) ");
									}
								}
								if(fileExp==null)
									mob.tell(fails.toString());
								c.failSkillCheck.clear();
							}
						}
					}
					latch.countDown();
				}
			}.start(c,charClassDex,fileExp,latch,aborted);
		}
		try
		{
			latch.await();
		}
		catch(final Exception e)
		{
			aborted[0]=true;
			return;
		}
		mob.tell(L(""));
		if(fileExp!=null)
		{
			final CMFile file=new CMFile(fileExp,mob);
			if(file.canWrite())
			{
				final StringBuffer buf=new StringBuffer("");
				final Vector<String> baseClasses=new Vector<String>();
				for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
				{
					final CharClass C=(CharClass)c.classSet.get(charClassDex,1);
					if(!baseClasses.contains(C.baseClass()))
						baseClasses.add(C.baseClass());
				}
				for(int d=0;d<c.allData[0][0].length;d++)
				{
					buf.append(allDataHeader[d]).append("\t\t");
					for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
						buf.append(level).append('\t');
					buf.append("\n\r");
					for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
					{
						final CharClass C=(CharClass)c.classSet.get(charClassDex,1);
						buf.append(C.ID()).append("\t").append(C.baseClass()).append("\t");
						for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
						{
							buf.append(c.allData[charClassDex][level-c.levelStart][d]).append('\t');
							//for(int i=0;i<allSkills[charClassDex][level-levelStart].length;i++)
							//    buf.append(allSkills[charClassDex][level-levelStart][i]).append('\t');
						}
						buf.append("\n\r");
					}
					buf.append("\n\r");
					buf.append(allDataHeader[d]).append("\t*****\t");
					for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
						buf.append(level).append('\t');
					buf.append("\n\r");
					for(int b=0;b<baseClasses.size();b++)
					{
						final String baseClass=baseClasses.get(b);
						final int[] levels=new int[c.levelEnd+1];
						double ct=0;
						for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
						{
							if(((CharClass)c.classSet.get(charClassDex,1)).baseClass().equalsIgnoreCase(baseClass))
							{
								ct+=1.0;
								for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
									levels[level]+=c.allData[charClassDex][level-c.levelStart][d];
							}
						}
						if(ct>0)
						{
							buf.append(baseClass).append("\t").append(baseClass).append("\t");
							for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
								buf.append(Math.round(Integer.valueOf(levels[level]).doubleValue()/ct*100.0)/100).append('\t');
							buf.append("\n\r");
						}
					}
					buf.append("\n\r");
				}
				file.saveText(buf);
			}
		}
		CMLib.map().delArea(c.A);
		c.A.destroy();
	}

	
	protected boolean setWeaponFields(final MOB mob, final List<String> commands, Map<String,String> baseItem, String fieldName, String suffix)
	{
		boolean foundOne=false;
		final String notValue="(*DY(*HHK#H(*H";
		for(String cmd : commands)
		{
			if(cmd.toLowerCase().trim().startsWith(suffix+fieldName))
			{
				String val=CMParms.getParmStr(cmd, suffix+fieldName, notValue);
				if(!val.equals(notValue))
				{
					baseItem.put(fieldName, val);
					foundOne=true;
				}
			}
		}
		return foundOne;
	}
	
	protected boolean confirmField(final MOB mob, int fieldIndex, String val)
	{
		switch(fieldIndex)
		{
		case 0:
			if(!CMParms.contains(Weapon.CLASS_DESCS, val.toUpperCase().trim()))
			{
				mob.tell(L("Illegal weapon class '@x1', values are: "+CMParms.toListString(Weapon.CLASS_DESCS),val));
				return false;
			}
			break;
		case 1:
			if(!CMParms.contains(Weapon.TYPE_DESCS, val.toUpperCase().trim()))
			{
				mob.tell(L("Illegal weapon damage types '@x1', values are: "+CMParms.toListString(Weapon.TYPE_DESCS),val));
				return false;
			}
			break;
		case 2:
		{
			if(!CMath.isInteger(val))
			{
				mob.tell(L("Not a level '@x1'.",val));
				return false;
			}
			int v=CMath.s_int(val);
			if(v<0)
			{
				mob.tell(L("Not a valid level '@x1'.",val));
				return false;
			}
			break;
		}
		case 3:
		{
			if(!CMath.isInteger(val))
			{
				mob.tell(L("Not a number of hands '@x1'.",val));
				return false;
			}
			int v=CMath.s_int(val);
			if((v<=0)||(v>2))
			{
				mob.tell(L("Not a valid number of hands '@x1'.",val));
				return false;
			}
			break;
		}
		case 4:
		{
			if(!CMath.isInteger(val))
			{
				mob.tell(L("Not a reach '@x1'.",val));
				return false;
			}
			int v=CMath.s_int(val);
			if((v<0)||(v>10))
			{
				mob.tell(L("Not a valid reach '@x1'.",val));
				return false;
			}
			break;
		}
		case 5:
		{
			if(!CMath.isInteger(val))
			{
				mob.tell(L("Not a weight '@x1'.",val));
				return false;
			}
			int v=CMath.s_int(val);
			if(v<=0)
			{
				mob.tell(L("Not a valid weight '@x1'.",val));
				return false;
			}
			break;
		}
		case 6:
			if(!CMParms.contains(RawMaterial.CODES.NAMES(), val.toUpperCase().trim()))
			{
				mob.tell(L("Illegal material/resource type '@x1', values are: "+CMParms.toListString(RawMaterial.CODES.NAMES()),val));
				return false;
			}
			break;
		}
		return true;
	}
	
	protected void weaponRun(final MOB mob, List<String> commands)
	{
		String[] fields=new String[]{"class","type","level","hands","reach","weight","material"};
		String[] defaults=new String[]{"BLUNT","BASHING","1","1","0","6","IRON"};
		StringBuilder str=new StringBuilder("\n\rInputs:\n\r");
		str.append("\n\r");
		for(int x=0;x<fields.length;x++)
			str.append("\"").append(fields[x]).append("=").append(defaults[x]).append("\"\n\r");
		str.append("\n\r")
		   .append("Put numbers BEFORE field name to add more items like the base item.\n\r")
		   .append("\n\r");
		if(commands.size()==0)
		{
			mob.tell(L(str.toString()));
			return;
		}
		final List<Map<String,String>> map=new ArrayList<Map<String,String>>();
		final Hashtable<String,String> template=new Hashtable<String,String>();
		for(int x=0;x<fields.length;x++)
			template.put(fields[x], defaults[x]);
		map.add(template);
		if(commands.size()>0)
		{
			for(int x=0;x<fields.length;x++)
			{
				setWeaponFields(mob, commands, template, fields[x], "");
				if(!confirmField(mob, x, template.get(fields[x])))
					return;
			}
			for(int i=0;i<100;i++)
			{
				boolean setOne=false;
				Hashtable<String,String> newItem=new XHashtable<String,String>(template);
				for(int x=0;x<fields.length;x++)
				{
					boolean newSetOne=setWeaponFields(mob, commands, newItem, fields[x], ""+i);
					setOne = newSetOne || setOne;
					if(newSetOne && (!confirmField(mob, x, newItem.get(fields[x]))))
						return;
				}
				if(setOne)
					map.add(newItem);
				else
				if(i>9)
					break;
			}
		}

		final Session viewerS=mob.session();
		StringBuilder headStr=new StringBuilder("");
		final int[] cols=new int[]
		{
			CMLib.lister().fixColWidth(12.0,viewerS),
			CMLib.lister().fixColWidth(12.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS),
			CMLib.lister().fixColWidth(12.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS),
			CMLib.lister().fixColWidth(6.0,viewerS)
		};
		headStr.append(CMStrings.padRight(L("WClass"), cols[0]));
		headStr.append(CMStrings.padRight(L("WType"), cols[1]));
		headStr.append(CMStrings.padRight(L("Lvl"), cols[2]));
		headStr.append(CMStrings.padRight(L("Hands"), cols[3]));
		headStr.append(CMStrings.padRight(L("Reach"), cols[4]));
		headStr.append(CMStrings.padRight(L("Weit#"), cols[5]));
		headStr.append(CMStrings.padRight(L("Mat."), cols[6]));
		headStr.append(CMStrings.padRight(L("Att"), cols[7]));
		headStr.append(CMStrings.padRight(L("Dmg"), cols[8]));
		headStr.append("\n\r");
		StringBuilder chart=new StringBuilder("");
		chart.append(headStr.toString());
		for(final Map<String,String> item : map)
		{
			Weapon W=CMClass.getWeapon("GenWeapon");
			int level=1;
			int material=RawMaterial.RESOURCE_IRON;
			int hands=1;
			int wclass=Weapon.CLASS_BLUNT;
			int reach=0;
			long wornData=Wearable.WORN_WIELD|Wearable.WORN_HELD;
			for(int f=0;f<fields.length;f++)
			{
				String val=item.get(fields[f]);
				switch(f)
				{
				case 0:
					wclass=CMParms.indexOf(Weapon.CLASS_DESCS, val.toUpperCase().trim());
					break;
				case 1:
					W.setWeaponDamageType(CMParms.indexOf(Weapon.TYPE_DESCS, val.toUpperCase().trim()));
					break;
				case 2:
					level=CMath.s_int(val.trim());
					break;
				case 3:
					hands=CMath.s_int(val.trim());
					break;
				case 4:
					reach=CMath.s_int(val.trim());
					break;
				case 5:
					W.basePhyStats().setWeight(CMath.s_int(val.trim()));
					W.phyStats().setWeight(CMath.s_int(val.trim()));
					break;
				case 6:
					material=RawMaterial.CODES.FIND_IgnoreCase(val.toUpperCase().trim());
					break;
				}
			}
			W.setWeaponClassification(wclass);
			W.basePhyStats().setLevel(level);
			W.phyStats().setLevel(level);
			W.setRawLogicalAnd(hands>1?true:false);
			W.setRanges(0, reach);
			W.setMaterial(material);
			final Map<String,String> H=CMLib.itemBuilder().timsItemAdjustments(W, level, material, hands, wclass, reach, wornData);
			W.basePhyStats().setDamage(CMath.s_int(H.get("DAMAGE")));
			W.phyStats().setDamage(CMath.s_int(H.get("DAMAGE")));
			W.basePhyStats().setAttackAdjustment(CMath.s_int(H.get("ATTACK")));
			W.phyStats().setAttackAdjustment(CMath.s_int(H.get("ATTACK")));
			W.recoverPhyStats();
			for(int i=0;i<cols.length;i++)
			{
				if(i<fields.length)
				{
					switch(i)
					{
					case 0:
						chart.append(CMStrings.padRight(Weapon.CLASS_DESCS[W.weaponClassification()], cols[i]));
						break;
					case 1:
						chart.append(CMStrings.padRight(Weapon.TYPE_DESCS[W.weaponDamageType()], cols[i]));
						break;
					case 2:
						chart.append(CMStrings.padRight(""+W.basePhyStats().level(), cols[i]));
						break;
					case 3:
						chart.append(CMStrings.padRight(""+(W.rawLogicalAnd()?2:1), cols[i]));
						break;
					case 4:
						chart.append(CMStrings.padRight(""+(W.maxRange()), cols[i]));
						break;
					case 5:
						chart.append(CMStrings.padRight(""+(W.basePhyStats().weight()), cols[i]));
						break;
					case 6:
						chart.append(CMStrings.padRight(RawMaterial.CODES.NAME(W.material()), cols[i]));
						break;
					default:
						chart.append(CMStrings.padRight(item.get(fields[i]), cols[i]));
						break;
					}
				}
				else
				if(i==fields.length)
					chart.append(CMStrings.padRight(""+W.basePhyStats().attackAdjustment(), cols[i]));
				else
				if(i==fields.length+1)
					chart.append(CMStrings.padRight(""+W.basePhyStats().damage(), cols[i]));
			}
			chart.append("\n\r");
		}
		mob.tell(chart.toString());
	}
	
	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster())
			return false;
		commands.remove(0);
		boolean createNewOnly=false;
		boolean createPlayer=false;
		if(commands.size()>0)
		{
			if(commands.get(0).equalsIgnoreCase("COMBAT"))
			{
				commands.remove(0);
				combatRun(mob,commands);
				return true;
			}

			if(commands.get(0).equalsIgnoreCase("WEAPON"))
			{
				commands.remove(0);
				weaponRun(mob,commands);
				return true;
			}
			
			if(commands.get(0).equalsIgnoreCase("NEW"))
			{
				commands.remove(0);
				createNewOnly=true;
			}

			if(commands.get(0).equalsIgnoreCase("PLAYER"))
			{
				commands.remove(0);
				createPlayer=true;
			}
			
			if(commands.get(0).equalsIgnoreCase("EQUIP") && (commands.size()>1))
			{
				MOB M=CMLib.players().getLoadPlayer(CMParms.combine(commands,1));
				if(M==null)
				{
					mob.tell(L("Equip Whom?"));
					return false;
				}
				this.equipPlayer(M);
				mob.tell(L("Done."));
				return true;
			}
		}
		CharClass C=null;
		int level=-1;
		String ClassName="";
		if(commands.size()>0)
		{
			ClassName=commands.get(0);
			C=CMClass.findCharClass(ClassName);
			level=CMath.s_int(CMParms.combine(commands,1));
		}

		if((C==null)&&(createNewOnly||(ClassName.toUpperCase().indexOf("ALL")<0)))
		{
			mob.tell(L("Enter 'ALL' for all classes."));
			try
			{
				ClassName=mob.session().prompt(L("Enter a class name:"));
			}
			catch (final Exception e)
			{
				return false;
			}

			C=CMClass.findCharClass(ClassName);
			if((C==null)&&(createNewOnly||(ClassName.toUpperCase().indexOf("ALL")<0)))
				return false;
		}

		if(level<=0)
		{
			try
			{
				level=CMath.s_int(mob.session().prompt(L("Enter a level (1-@x1): ",""+CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL))));
			}
			catch (final Exception e)
			{
				return false;
			}
			if(level<=0)
				return false;
		}

		if(C!=null)
			mob.session().print(L("\n\rAverage @x1...",C.name()));
		else
			mob.session().print(L("\n\rAverage MOB stats, across all classes..."));

		MOB avgMob=null;
		if(C!=null)
			avgMob=AverageClassMOB(mob, level,C, createNewOnly?1:100, createPlayer);
		else
			avgMob=AverageAllClassMOB(mob,level, 20, 40, createPlayer);

		mob.session().println("\n\r");

		if(avgMob!=null)
		{
			if(createNewOnly)
				avgMob.bringToLife(mob.location(),true);
			else
			{
				final StringBuilder msg=CMLib.commands().getScore(avgMob);
				if(!mob.isMonster())
					mob.session().wraplessPrintln(msg.toString());
				avgMob.destroy();
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CHARGEN);
	}
}
