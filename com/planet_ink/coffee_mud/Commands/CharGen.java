 package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class CharGen extends StdCommand
{
	public CharGen(){}

	private String[] access={"CHARGEN"};
	public String[] getAccessWords(){return access;}

	protected void equipPlayer(MOB M)
	{
		int level = M.baseEnvStats().level();
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
					CMLib.factions().setAlignment(M,Faction.ALIGN_EVIL);
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
					CMLib.factions().setAlignment(M,Faction.ALIGN_GOOD);
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
				W.baseEnvStats().setLevel(level);
				W.baseEnvStats().setWeight(8);
				W.recoverEnvStats();
				CMLib.itemBuilder().balanceItemByLevel(W);
				M.addInventory(W);
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
				A.baseEnvStats().setLevel(level);
				A.baseEnvStats().setWeight(8);
				A.recoverEnvStats();
				CMLib.itemBuilder().balanceItemByLevel(A);
				M.addInventory(A);
				A.wearIfPossible(M);
			}
		}
	}
	
	
	protected MOB levelMOBup(int level, CharClass C)
	{
		MOB mob=CMClass.getMOB("StdMOB");
		CMLib.factions().setAlignment(mob,Faction.ALIGN_NEUTRAL);
		mob.setName("Average Joe");
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		for(int i : CharStats.CODES.BASE())
			mob.baseCharStats().setStat(i,10);
		mob.baseCharStats().setStat(CharStats.STAT_STRENGTH,11);
		mob.baseCharStats().setStat(CharStats.STAT_WISDOM,11);
		mob.baseCharStats().setStat(CharStats.STAT_DEXTERITY,11);
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,1);
		mob.baseEnvStats().setArmor(100);
		mob.baseEnvStats().setLevel(1);
		mob.baseEnvStats().setSensesMask(0);
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.baseCharStats().getMyRace().startRacing(mob,false);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);

		int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
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
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
        equipPlayer(mob);
        for(int a=0;a<mob.numAbilities();a++)
        	mob.fetchAbility(a).setProficiency(100);
        for(int a=0;a<mob.numEffects();a++)
        	mob.fetchEffect(a).setProficiency(100);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		return mob;
	}

	public void averageout(MOB avgMob, int tries)
	{
		for(int i : CharStats.CODES.BASE())
			avgMob.baseCharStats().setStat(i,(int)Math.round(CMath.div(avgMob.baseCharStats().getStat(i),tries)));
		avgMob.baseEnvStats().setArmor((int)Math.round(CMath.div(avgMob.baseEnvStats().armor(),tries)));
		avgMob.baseState().setHitPoints((int)Math.round(CMath.div(avgMob.baseState().getHitPoints(),tries)));
		avgMob.baseState().setMovement((int)Math.round(CMath.div(avgMob.baseState().getMovement(),tries)));
		avgMob.baseState().setMana((int)Math.round(CMath.div(avgMob.baseState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
		avgMob.setTrains(0);
	}

	public void addHimIn(MOB avgMob, MOB mob2)
	{
		for(int i : CharStats.CODES.BASE())
			avgMob.baseCharStats().setStat(i,avgMob.baseCharStats().getStat(i)+mob2.baseCharStats().getStat(i));
		avgMob.baseEnvStats().setArmor(avgMob.baseEnvStats().armor()+mob2.baseEnvStats().armor());
		avgMob.baseState().setHitPoints(avgMob.baseState().getHitPoints()+mob2.baseState().getHitPoints());
		avgMob.baseState().setMovement(avgMob.baseState().getMovement()+mob2.baseState().getMovement());
		avgMob.baseState().setMana(avgMob.baseState().getMana()+mob2.baseState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
	}

	public MOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries)
	{
		MOB avgMob=levelMOBup(level,C);
		int tries=0;
		for(;tries<numTries;tries++)
		{
			if(((tries % 20)==0)&&(mob!=null))
				mob.session().print(".");
			MOB mob2=levelMOBup(level,C);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries+1);
		return avgMob;
	}

	public MOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB)
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
					MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB);
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
	
	public void combatRun(MOB mob, Vector commands) {
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
    	final Random r = new Random(System.currentTimeMillis());
        for(int charClassDex=0;charClassDex<c.classSet.size();charClassDex++)
        {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            new Thread() {
            	CombatStats c;
            	int charClassDex;
            	String fileExp;
            	java.util.concurrent.CountDownLatch latch = null;
            	boolean[] aborted;
            	public void start(CombatStats c, int charClassDex, String fileExp, java.util.concurrent.CountDownLatch latch, boolean[] aborted){
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
		                
		                Vector medScore=new Vector();
		                Vector medWinIters=new Vector();
		                Vector medPhysDone=new Vector();
		                Vector medHitPct=new Vector();
		                Vector medIsHitPct=new Vector();
		                Vector medPhysTaken=new Vector();
		                Vector medLossIters=new Vector();
		                Vector medPlayerDamPct = new Vector();
		                Vector medPlayerManaPct = new Vector();
		                
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
				                R.setRoomID(c.A.name()+"#"+r.nextInt(Integer.MAX_VALUE));
				                R.setArea(c.A);
				                R.recoverEnvStats();
			                }
			                c.A.getTimeObj().setTimeOfDay(CMLib.dice().roll(1,c.A.getTimeObj().getHoursInDay(),-1));
			                
			                //Session S=(Session)CMClass.getCommon("FakeSession");
			                //S.initializeSession(null,"MEMORY");
			                MOB M1=null;
			                if(C.ID().equalsIgnoreCase("StdCharClass"))
			                {
			                    M1=CMClass.getMOB("StdMOB");  // MOB stat
			                    Behavior B2=CMClass.getBehavior("CombatAbilities");
			                    M1.baseCharStats().setMyRace(CMClass.getRace("Human"));
			                    M1.baseEnvStats().setLevel(level);
			                    M1.setName("GOODGUY");
			                    M1.recoverCharStats();
			                    M1.recoverEnvStats();
			                    M1.setLocation(R);
			                    M1.baseCharStats().getMyRace().setHeightWeight(M1.baseEnvStats(),(char)M1.baseCharStats().getStat(CharStats.STAT_GENDER));
			                    M1.baseEnvStats().setAbility(11);
			                    M1.recoverCharStats();
			                    M1.recoverEnvStats();
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
			                    M1.recoverEnvStats();
			                }
			                else
			                {
				                M1=AverageClassMOB(null,level,C,1);
			                    M1.baseCharStats().setMyRace(CMClass.getRace("Human"));
				                M1.setName("GOODGUY");
				                M1.recoverCharStats();
			                    M1.recoverEnvStats();
			                    M1.setLocation(R);
				                M1.baseCharStats().getMyRace().setHeightWeight(M1.baseEnvStats(),(char)M1.baseCharStats().getStat(CharStats.STAT_GENDER));
				                M1.recoverCharStats();
				                M1.recoverEnvStats();
				                M1.recoverMaxState();
				                M1.resetToMaxState();
				                M1.bringToLife(M1.location(),true);
				                CMLib.threads().deleteTick(M1,Tickable.TICKID_MOB);
				                M1.setWimpHitPoint(0);
				                M1.recoverMaxState();
				                M1.recoverCharStats();
				                M1.recoverEnvStats();
				                M1.resetToMaxState();
			                    B1.setStat("PRECAST","1");
				                M1.addBehavior(B1);
				                equipPlayer(M1);
				                M1.recoverMaxState();
				                M1.recoverCharStats();
				                M1.recoverEnvStats();
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
			                
		                    MOB M2=CMClass.getMOB("StdMOB");  // MOB stat
		                    Behavior B2=CMClass.getBehavior("CombatAbilities");
		                    M2.baseCharStats().setMyRace(CMClass.getRace("Human"));
		                    M2.baseEnvStats().setLevel(level);
		                    M2.setName("BADGUY");
		                    M2.recoverCharStats();
		                    M2.recoverEnvStats();
		                    M2.setLocation(R);
		                    M2.baseCharStats().getMyRace().setHeightWeight(M2.baseEnvStats(),(char)M2.baseCharStats().getStat(CharStats.STAT_GENDER));
		                    M2.baseEnvStats().setAbility(11);
		                    M2.recoverCharStats();
		                    M2.recoverEnvStats();
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
		                    M2.recoverEnvStats();
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
		                        try {
			                    	CMLib.commands().postStand(M1,true);
			                    	CMLib.commands().postStand(M2,true);
			                        M1.tick(M1,Tickable.TICKID_MOB);
			                        M2.tick(M2,Tickable.TICKID_MOB);
		                        } catch(Throwable t) {
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
		                    if(M1.amDead()||M2.amDead())
		                    {
		                    	if(M1.amDead())
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
		                            Vector V=CMParms.parseSemicolons(B1.getStat("RECORD"),true);
		                            for(int v=0;v<V.size();v++)
		                            {
		                                String s=((String)V.elementAt(v)).trim();
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
			            CMParms.sortVector(medScore);
			            CMParms.sortVector(medHitPct);
			            CMParms.sortVector(medLossIters);
			            CMParms.sortVector(medWinIters);
			            CMParms.sortVector(medPhysDone);
			            CMParms.sortVector(medPhysTaken);
			            CMParms.sortVector(medIsHitPct);
			            CMParms.sortVector(medPlayerDamPct);
			            CMParms.sortVector(medPlayerManaPct);
		                allData[charClassDex][level-levelStart][0]=bestIterScore[0];
		                allData[charClassDex][level-levelStart][1]=bestHitScore[0];
		                allData[charClassDex][level-levelStart][2]=bestSingleHitScore[0];
		                allData[charClassDex][level-levelStart][3]=bestSingleHitPhys[0];
		                allData[charClassDex][level-levelStart][4]=losses[0];
		                if(medScore.size()>0)
			                allData[charClassDex][level-levelStart][5]=((Integer)medScore.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medScore.size()))))).intValue();
		                if(medHitPct.size()>0)
			                allData[charClassDex][level-levelStart][6]=((Double)medHitPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medHitPct.size()))))).intValue();
		                if(medLossIters.size()>0)
			                allData[charClassDex][level-levelStart][7]=((Integer)medLossIters.elementAt((int)Math.round(Math.floor(CMath.mul(0.75,medLossIters.size()))))).intValue();
		                if(medWinIters.size()>0)
			                allData[charClassDex][level-levelStart][8]=((Integer)medWinIters.elementAt((int)Math.round(Math.floor(CMath.mul(0.25,medWinIters.size()))))).intValue();
		                if(medPhysDone.size()>0)
			                allData[charClassDex][level-levelStart][9]=((Integer)medPhysDone.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPhysDone.size()))))).intValue();
		                if(medPhysTaken.size()>0)
			                allData[charClassDex][level-levelStart][10]=((Integer)medPhysTaken.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPhysTaken.size()))))).intValue();
		                if(medIsHitPct.size()>0)
			                allData[charClassDex][level-levelStart][11]=((Double)medIsHitPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medIsHitPct.size()))))).intValue();
		                allData[charClassDex][level-levelStart][12]=losses[0];
		                allData[charClassDex][level-levelStart][13]=playerArmor;
		                allData[charClassDex][level-levelStart][14]=playerAttack;
		                if(medPlayerDamPct.size()>0)
			                allData[charClassDex][level-levelStart][15]=((Double)medPlayerDamPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerDamPct.size()))))).intValue();
		                if(medPlayerManaPct.size()>0)
			                allData[charClassDex][level-levelStart][16]=((Double)medPlayerManaPct.elementAt((int)Math.round(Math.floor(CMath.mul(0.5,medPlayerManaPct.size()))))).intValue();
		                
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
			                        if(times[1]>0) {
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
            try {
            	latch.await();
            } catch(Exception e) {
            	aborted[0]=true;
            	return;
            }
	    }
        mob.tell("");
        if(fileExp!=null)
        {
        	CMFile file=new CMFile(fileExp,mob,false);
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
	                	    //	buf.append(allSkills[charClassDex][level-levelStart][i]).append('\t');
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
				level=CMath.s_int(mob.session().prompt("Enter a level (1-"+CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)+"): "));
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
			avgMob=AverageClassMOB(mob, level,C, createNewOnly?1:100);
		else
			avgMob=AverageAllClassMOB(mob,level, 20, 40);

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
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CHARGEN");}

	
}
