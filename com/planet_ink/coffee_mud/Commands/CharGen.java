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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class CharGen extends StdCommand
{
	public CharGen(){}

	private final String[] access={"CHARGEN"};
	public String[] getAccessWords(){return access;}

	protected void equipPlayer(MOB M)
	{
		int level = M.basePhyStats().level();
		CharClass C=M.baseCharStats().getCurrentClass();
		for(long wornCode : Wearable.CODES.ALL())
		{
			if((wornCode == Wearable.IN_INVENTORY) 
			|| (wornCode == Wearable.WORN_HELD)
			|| (wornCode == Wearable.WORN_MOUTH)) 
				 continue;
			if(wornCode==Wearable.WORN_WIELD)
			{
				Weapon W=CMClass.getWeapon("GenWeapon");
				W.setWeaponClassification(Weapon.CLASS_SWORD);
				W.setWeaponType(Weapon.TYPE_SLASHING);
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
					W.setWeaponType(Weapon.TYPE_PIERCING);
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
					W.setWeaponType(Weapon.TYPE_BASHING);
					break;
				case CharClass.WEAPONS_ALLCLERIC:
					if(CMLib.flags().isGood(M))
					{
						W.setMaterial(RawMaterial.RESOURCE_OAK);
						W.setWeaponClassification(Weapon.CLASS_STAFF);
						W.setWeaponType(Weapon.TYPE_BASHING);
					}
					break;
				case CharClass.WEAPONS_FLAILONLY:
					W.setWeaponClassification(Weapon.CLASS_FLAILED);
					W.setWeaponType(Weapon.TYPE_BASHING);
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
				Armor A=CMClass.getArmor("GenArmor");
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
	
	
	protected MOB levelMOBup(int level, CharClass C, boolean player)
	{
		MOB mob=CMClass.getFactoryMOB();
		CMLib.factions().setAlignment(mob,Faction.Align.NEUTRAL);
		mob.setName("Average Joe");
		if(player) mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		for(int i : CharStats.CODES.BASE())
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

		int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		for(int lvl=1;lvl<level;lvl++)
		{
			if((lvl % 3)==0)
			{
				int stat=-1;
				int bestDiff=0;
				for(int i: CharStats.CODES.BASE())
				{
					int base = max + mob.charStats().getStat(CharStats.CODES.toMAXBASE(i));
					int diff = base - mob.baseCharStats().getStat(i);
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
		for(Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			a.nextElement().setProficiency(100);
		for(int a=0;a<mob.numEffects();a++) // personal
			mob.fetchEffect(a).setProficiency(100);
		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		return mob;
	}

	public void averageout(MOB avgMob, int tries)
	{
		for(int i : CharStats.CODES.BASE())
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

	public void addHimIn(MOB avgMob, MOB mob2)
	{
		for(int i : CharStats.CODES.BASE())
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

	public MOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries, boolean player)
	{
		MOB avgMob=levelMOBup(level,C, player);
		int tries=1;
		for(;tries<numTries;tries++)
		{
			if(((tries % 20)==0)&&(mob!=null))
				mob.session().print(".");
			MOB mob2=levelMOBup(level,C, player);
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
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.availabilityCode()!=0)
				{
					numClasses++;
					MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB,player);
					if(avgMob==null)
					{
						avgMob=mob2;
						numClasses--;
					}
					else
						addHimIn(avgMob,mob2);
					if(avgMob!=mob2) mob2.destroy();
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
		Hashtable failSkillCheck=null;
	}
	
	public void combatRun(MOB mob, Vector commands)
	{
		CombatStats c=new CombatStats();
		c.mob=mob;
		if(commands.size()==0)
		{
			mob.tell("USAGE: CHARGEN COMBAT ([CHARCLASS(S)]...) (EXPORT=FILENAME) (FAILCHECK) (ITERATIONS=[X]) (SKIPLEVELS=[X]) ([START LEVEL]) ([END LEVEL])");
			return;
		}
		String[][] CAMATCH={
				{"Commoner","CombatAbilities"},
				{"Bard","Bardness"},
				{"Cleric","Clericness"},
				{"Druid","Druidness"},
				{"Mage","Mageness"},
				{"Thief","Thiefness"},
				{"Fighter","Fighterness"},
		};
		
		for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
		{
			CharClass C=(CharClass)e.nextElement();
			if((CMath.bset(C.availabilityCode(),Area.THEME_FANTASY)
				||CMath.bset(C.availabilityCode(),Area.THEME_HEROIC)
				||CMath.bset(C.availabilityCode(),Area.THEME_TECHNOLOGY))
			&&(!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK)))
			{
				String behav="CombatAbilities";
				for(int x=0;x<CAMATCH.length;x++)
					if(C.baseClass().equalsIgnoreCase(CAMATCH[x][0]))
						behav=CAMATCH[x][1];
				c.classSet.addElement(C,behav);
			}
		}
		
		// set the parameters
		boolean classCleared=false;
		boolean nextLevel=false;
		String fileExp=null;
		for(int i=0;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(CMath.isInteger(s))
			{
				int x=CMath.s_int(s);
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
			if(s.toUpperCase().startsWith("SKIPLEVELS="))
			{
				c.skipLevels=CMath.s_int(s.substring("SKIPLEVELS=".length()));
				if(c.skipLevels<1) c.skipLevels=1;
			}
			else
			if(CMClass.findCharClass(s)!=null)
			{
				CharClass C=CMClass.findCharClass(s);
				if(!classCleared)
				{
					classCleared=true;
					c.classSet=new DVector(2);
				}
				String behav="CombatAbilities";
				for(int x=0;x<CAMATCH.length;x++)
					if(C.baseClass().equalsIgnoreCase(CAMATCH[x][0]))
						behav=CAMATCH[x][1];
				c.classSet.addElement(C,behav);
			}
			else
			if(s.endsWith("s"))
			{
				s=s.substring(0,s.length()-1);
				for(Enumeration e=CMClass.charClasses();e.hasMoreElements();)
				{
					CharClass C=(CharClass)e.nextElement();
					if((CMath.bset(C.availabilityCode(),Area.THEME_FANTASY)
						||CMath.bset(C.availabilityCode(),Area.THEME_HEROIC)
						||CMath.bset(C.availabilityCode(),Area.THEME_TECHNOLOGY))
					&&(!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK))
					&&(C.baseClass().equalsIgnoreCase(s)||(s.equalsIgnoreCase("charclasse"))))
					{
						if(!classCleared)
						{
							classCleared=true;
							c.classSet=new DVector(2);
						}
						String behav="CombatAbilities";
						for(int x=0;x<CAMATCH.length;x++)
							if(C.baseClass().equalsIgnoreCase(CAMATCH[x][0]))
								behav=CAMATCH[x][1];
						c.classSet.addElement(C,behav);
					}
				}
			}
			else
			if(s.equalsIgnoreCase("FAILCHECK"))
				c.failSkillCheck=new Hashtable();
		}
		
		if(c.skipLevels<=0)
		{
			c.skipLevels=1;
			if((c.levelStart==1)&&(c.levelEnd==91))
				c.skipLevels=15;
		}
		
		
			
		
		c.A=CMClass.getAreaType("StdArea");
		c.A.setName("UNKNOWNAREA");
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
		
		
		boolean[] aborted = new boolean[1];
		aborted[0]=false;
		final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(c.classSet.size());
		final java.util.concurrent.atomic.AtomicInteger IDGEN=new java.util.concurrent.atomic.AtomicInteger(1);
		final Race humanR=CMClass.getRace("Human");
		for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
		{
			new Thread(Thread.currentThread().getThreadGroup(),"CharGen"+Thread.currentThread().getThreadGroup().getName().charAt(0))
			{
				CombatStats c;
				int charClassDex;
				String fileExp;
				java.util.concurrent.CountDownLatch latch = null;
				boolean[] aborted;
				public void start(CombatStats c, int charClassDex, String fileExp, java.util.concurrent.CountDownLatch latch, boolean[] aborted)
				{
					this.c=c;
					this.charClassDex=charClassDex;
					this.fileExp=fileExp;
					this.latch=latch;
					this.aborted=aborted;
					this.start();
				}
				public void run() 
				{
					MOB mob=c.mob;
					int[][][] allData = c.allData;
					String[][][] allSkills=c.allSkills;
					DVector classSet=c.classSet;
					int levelStart=c.levelStart;
					for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
					{
						CharClass C=(CharClass)c.classSet.elementAt(charClassDex,1);
						mob.tell(C.ID()+": "+level);
						int roomRobin=0;
						Room R=null;
						
						int[] bestSingleHitScore=new int[]{0};
						String[] bestSingleHitSkill=new String[]{""};
						int[] bestSingleHitPhys=new int[]{0};
						int[] bestHitScore=new int[]{0};
						String[] bestHitSkill=new String[]{""};
						int[] bestIterScore=new int[]{Integer.MAX_VALUE};
						String[] bestIterSkill=new String[]{""};
						
						int[] losses=new int[]{0};
						
						XVector<Integer> medScore=new XVector<Integer>();
						XVector<Integer> medWinIters=new XVector<Integer>();
						XVector<Integer> medPhysDone=new XVector<Integer>();
						XVector<Double> medHitPct=new XVector<Double>();
						XVector<Double> medIsHitPct=new XVector<Double>();
						XVector<Integer> medPhysTaken=new XVector<Integer>();
						XVector<Integer> medLossIters=new XVector<Integer>();
						XVector<Double> medPlayerDamPct = new XVector();
						XVector<Double> medPlayerManaPct = new XVector();
						
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
								if(mob.session()!=null) mob.session().print(".");
							}
							Behavior B1=CMClass.getBehavior((String)classSet.elementAt(charClassDex,2));
							B1.setParms(C.ID());
							switch(roomRobin)
							{
							case 0: R=CMClass.getLocale("Woods"); break; 
							case 1: R=CMClass.getLocale("CaveRoom"); break; 
							case 2: R=CMClass.getLocale("CityStreet"); break; 
							}
							if((++roomRobin)>2) roomRobin=0;
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
								Behavior B2=CMClass.getBehavior("CombatAbilities");
								M1.baseCharStats().setMyRace(humanR);
								M1.basePhyStats().setLevel(level);
								M1.setName("GOODGUY");
								M1.recoverCharStats();
								M1.recoverPhyStats();
								M1.setLocation(R);
								M1.baseCharStats().getMyRace().setHeightWeight(M1.basePhyStats(),(char)M1.baseCharStats().getStat(CharStats.STAT_GENDER));
								M1.basePhyStats().setAbility(11);
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
								M1.setName("GOODGUY");
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
							
							MOB M2=CMClass.getFactoryMOB();  // MOB stat
							Behavior B2=CMClass.getBehavior("CombatAbilities");
							M2.baseCharStats().setMyRace(humanR);
							M2.basePhyStats().setLevel(level);
							M2.setName("BADGUY");
							M2.recoverCharStats();
							M2.recoverPhyStats();
							M2.setLocation(R);
							M2.baseCharStats().getMyRace().setHeightWeight(M2.basePhyStats(),(char)M2.baseCharStats().getStat(CharStats.STAT_GENDER));
							M2.basePhyStats().setAbility(11);
							M2.recoverCharStats();
							M2.recoverPhyStats();
							M2.recoverMaxState();
							M2.resetToMaxState();
							M2.addBehavior(B2);
							M2.bringToLife(M2.location(),true);
							CMLib.threads().deleteTick(M2,Tickable.TICKID_MOB);
							CMLib.leveler().fillOutMOB(M2,level);
							int hp=CMLib.leveler().getPlayerHitPoints(M2);
							if(hp>M2.baseState().getHitPoints())
								M2.baseState().setHitPoints(hp);
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
									mob.session().println(M1.name()+" has "+B1.ID()+" behavior for "+M1.numAbilities()+" abilities.");
									mob.session().print("Working..");
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
								int h1=M1.curState().getHitPoints();
								int h2=M2.curState().getHitPoints();
								int L1=l1;
								int L2=l2;
								l1=CMath.s_int(B2.getStat("PHYSDAMTAKEN"));
								l2=CMath.s_int(B1.getStat("PHYSDAMTAKEN"));
								if(l1>L1) hits++;
								if(l2>L2) ishits++;
								try
								{
									CMLib.commands().postStand(M1,true);
									CMLib.commands().postStand(M2,true);
									M1.tick(M1,Tickable.TICKID_MOB);
									M2.tick(M2,Tickable.TICKID_MOB);
								}
								catch(Exception t)
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
								medScore.addElement(Integer.valueOf(cumScore));
								medPhysDone.addElement(Integer.valueOf(CMath.s_int(B2.getStat("PHYSDAMTAKEN"))));
								medPhysTaken.addElement(Integer.valueOf(CMath.s_int(B1.getStat("PHYSDAMTAKEN"))));
								medHitPct.addElement(Double.valueOf((CMath.div(hits,iterations)*100)));
								medIsHitPct.addElement(Double.valueOf((CMath.div(ishits,iterations)*100)));
								medPlayerDamPct.addElement(Double.valueOf(100-(CMath.div(M1.curState().getHitPoints(),H1)*100.0)));
								medPlayerManaPct.addElement(Double.valueOf(100-(CMath.div(M1.curState().getMana(),M1.maxState().getMana())*100.0)));
								if(M1.amDead())
									medLossIters.addElement(Integer.valueOf(iterations));
								else
									medWinIters.addElement(Integer.valueOf(iterations));
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
									List<String> V=CMParms.parseSemicolons(B1.getStat("RECORD"),true);
									for(int v=0;v<V.size();v++)
									{
										String s=V.get(v).trim();
										boolean failed=false;
										if(s.startsWith("!"))
										{
											failed=true;
											s=s.substring(1);
										}
										int[] times=(int[])c.failSkillCheck.get(s);
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
							allData[charClassDex][level-levelStart][5]=medScore.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medScore.size())))).intValue();
						if(medHitPct.size()>0)
							allData[charClassDex][level-levelStart][6]=medHitPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medHitPct.size())))).intValue();
						if(medLossIters.size()>0)
							allData[charClassDex][level-levelStart][7]=medLossIters.elementAt((int)Math.round(Math.floor(CMath.mul(0.75,medLossIters.size())))).intValue();
						if(medWinIters.size()>0)
							allData[charClassDex][level-levelStart][8]=medWinIters.elementAt((int)Math.round(Math.floor(CMath.mul(0.25,medWinIters.size())))).intValue();
						if(medPhysDone.size()>0)
							allData[charClassDex][level-levelStart][9]=medPhysDone.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPhysDone.size())))).intValue();
						if(medPhysTaken.size()>0)
							allData[charClassDex][level-levelStart][10]=medPhysTaken.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPhysTaken.size())))).intValue();
						if(medIsHitPct.size()>0)
							allData[charClassDex][level-levelStart][11]=medIsHitPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medIsHitPct.size())))).intValue();
						allData[charClassDex][level-levelStart][12]=losses[0];
						allData[charClassDex][level-levelStart][13]=playerArmor;
						allData[charClassDex][level-levelStart][14]=playerAttack;
						if(medPlayerDamPct.size()>0)
							allData[charClassDex][level-levelStart][15]=medPlayerDamPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerDamPct.size())))).intValue();
						if(medPlayerManaPct.size()>0)
							allData[charClassDex][level-levelStart][16]=medPlayerManaPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerManaPct.size())))).intValue();
						
						allSkills[charClassDex][level-levelStart][0]=bestIterSkill[0];
						allSkills[charClassDex][level-levelStart][1]=bestHitSkill[0];
						allSkills[charClassDex][level-levelStart][2]=bestSingleHitSkill[0];
						if(mob.session()!=null) mob.session().println("!");
						if(fileExp==null)
						{
							mob.tell("HITPOINTS: "+H1+" vs "+H2);
							mob.tell("QUICKEST : "+bestIterScore[0]+": "+bestIterSkill[0]);
							mob.tell("MOST DAM : "+bestHitScore[0]+": "+bestHitSkill[0]);
							mob.tell("BEST HIT : "+bestSingleHitScore[0]+", Phys: "+bestSingleHitPhys[0]+", Skill: "+bestSingleHitSkill[0]);
							mob.tell("MEDIANS  : HITS: "+allData[charClassDex][level-levelStart][5]+" ("+allData[charClassDex][level-levelStart][6]+"%), LOSS ITERS: "+allData[charClassDex][level-levelStart][7]+", WIN ITERS: "+allData[charClassDex][level-levelStart][8]);
							mob.tell("MEDIANS  : PHYS DONE: "+allData[charClassDex][level-levelStart][9]+", PHYS TAKEN: "+allData[charClassDex][level-levelStart][10]+" ("+allData[charClassDex][level-levelStart][11]+"%)");
							mob.tell("LOSSES   : "+losses[0]);
							if((c.failSkillCheck!=null)&&(c.failSkillCheck.size()>0))
							{
								StringBuffer fails=new StringBuffer("SKILLFAILS: ");
								for(Enumeration i=c.failSkillCheck.keys();i.hasMoreElements();)
								{
									String s=(String)i.nextElement();
									int[] times=(int[])c.failSkillCheck.get(s);
									if(times[1]>0)
									{
										int pct=(int)Math.round(100.0*CMath.div(times[1],times[0]));
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
		catch(Exception e)
		{
			aborted[0]=true;
			return;
		}
		mob.tell("");
		if(fileExp!=null)
		{
			CMFile file=new CMFile(fileExp,mob);
			if(file.canWrite())
			{
				StringBuffer buf=new StringBuffer("");
				Vector baseClasses=new Vector();
				for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
				{
					CharClass C=(CharClass)c.classSet.elementAt(charClassDex,1);
					if(!baseClasses.contains(C.baseClass()))
						baseClasses.addElement(C.baseClass());
				}
				for(int d=0;d<c.allData[0][0].length;d++)
				{
					buf.append(allDataHeader[d]).append("\t\t");
					for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
						buf.append(level).append('\t');
					buf.append("\n\r");
					for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
					{
						CharClass C=(CharClass)c.classSet.elementAt(charClassDex,1);
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
						String baseClass=(String)baseClasses.elementAt(b);
						int[] levels=new int[c.levelEnd+1];
						double ct=0;
						for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
							if(((CharClass)c.classSet.elementAt(charClassDex,1)).baseClass().equalsIgnoreCase(baseClass))
							{
								ct+=1.0;
								for(int level=c.levelStart;level<=c.levelEnd;level+=c.skipLevels)
									levels[level]+=c.allData[charClassDex][level-c.levelStart][d];
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

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster())
			return false;
		commands.removeElementAt(0);
		boolean createNewOnly=false;
		boolean createPlayer=false;
		if(commands.size()>0)
		{
			if(((String)commands.firstElement()).equalsIgnoreCase("COMBAT"))
			{
				commands.removeElementAt(0);
				combatRun(mob,commands);
				return true;
			}
			
			if(((String)commands.firstElement()).equalsIgnoreCase("NEW"))
			{
				commands.removeElementAt(0);
				createNewOnly=true;
			}
			
			if(((String)commands.firstElement()).equalsIgnoreCase("PLAYER"))
			{
				commands.removeElementAt(0);
				createPlayer=true;
			}
		}
		CharClass C=null;
		int level=-1;
		String ClassName="";
		if(commands.size()>0)
		{
			ClassName=(String)commands.elementAt(0);
			C=CMClass.findCharClass(ClassName);
			level=CMath.s_int(CMParms.combine(commands,1));
		}

		if((C==null)&&(createNewOnly||(ClassName.toUpperCase().indexOf("ALL")<0)))
		{
			mob.tell("Enter 'ALL' for all classes.");
			try
			{
				ClassName=mob.session().prompt("Enter a class name:");
			}
			catch(Exception e){return false;}

			C=CMClass.findCharClass(ClassName);
			if((C==null)&&(createNewOnly||(ClassName.toUpperCase().indexOf("ALL")<0)))
				return false;
		}

		if(level<=0)
		{
			try
			{
				level=CMath.s_int(mob.session().prompt("Enter a level (1-"+CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)+"): "));
			}
			catch(Exception e){return false;}
			if(level<=0)
				return false;
		}

		if(C!=null)
			mob.session().print("\n\rAverage "+C.name()+"...");
		else
			mob.session().print("\n\rAverage MOB stats, across all classes...");

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
				StringBuilder msg=CMLib.commands().getScore(avgMob);
				if(!mob.isMonster())
					mob.session().wraplessPrintln(msg.toString());
				avgMob.destroy();
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CHARGEN);}

	
}
