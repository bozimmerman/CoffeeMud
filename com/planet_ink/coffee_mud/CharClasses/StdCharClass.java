package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.db.*;

public class StdCharClass implements CharClass
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="";
	protected int minHitPointsPerLevel=2;
	protected int maxHitPointsPerLevel=12;
	protected int maxStat[]={18,18,18,18,18,18};
	protected int bonusPracLevel=0;
	protected int manaMultiplier=15;
	protected int attackAttribute=CharStats.STRENGTH;
	protected int bonusAttackLevel=2;
	protected int practicesAtFirstLevel=5;
	protected int trainsAtFirstLevel=3;
	protected int damageBonusPerLevel=1;
	
	public String ID()
	{
		return myID;
	}
	public String name()
	{
		return name;
	}
	
	public boolean playerSelectable()
	{
		return false;
	}
	
	public boolean qualifiesForThisClass(MOB mob)
	{
		return true;
	}
	
	protected void giveMobAbility(MOB mob, Ability A)
	{
		A=(Ability)A.copyOf();
		if(mob.fetchAbility(A.ID())==null)
		{
			mob.addAbility(A);
			A.autoInvocation(mob);
		}
	}
	
	public int[] maxStat()
	{
		return maxStat;
	}
	
	public void logon(MOB mob)
	{
	};
	
	public void newCharacter(MOB mob)
	{
		Skill_Recall S=new Skill_Recall();
		S.setProfficiency(50);
		mob.addAbility(S);
		mob.setPractices(mob.getPractices()+practicesAtFirstLevel);
		mob.setTrains(mob.getTrains()+trainsAtFirstLevel);
	}
	
	/** some general statistics about such an item
	 * see class "Stats" for more information. */
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		
	}
	
	public boolean okAffect(Affect affect)
	{
		return true;
	}
	
	public void affect(Affect affect)
	{
	}
	
	public void gainExperience(MOB mob, MOB victim, int amount)
	{
		int levelLimit=6;
		double theAmount=new Integer(amount).doubleValue();
		
		if(victim!=null)
		{
			int levelDiff=victim.envStats().level()-mob.envStats().level();
			//SocialProcessor.relativeLevelDiff(victim,mob);
			
			if(levelDiff<-levelLimit) 
				theAmount=0.0;
			else
			{
				double levelFactor=Util.div(levelDiff,levelLimit);
				theAmount=theAmount+Util.mul(levelFactor,amount);
			}
			double alignFactor=Util.div(Math.abs(victim.getAlignment()-mob.getAlignment()),1000.0);
		
			mob.setAlignment(mob.getAlignment()+(int)Math.round(100*alignFactor));
			amount=(int)Math.round(theAmount*alignFactor);
		}
		
		mob.setExperience(mob.getExperience()+amount);
		mob.tell("You gain "+amount+" experience points.");
		if(mob.getExperience()>mob.getExpNextLevel())
			level(mob);
	}
	
	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		mob.tell("\n\rYou've lost a level!!");
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()-1);
		mob.tell("You are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+".\n\r");
		
		levelAdjuster(mob,-1);
		
		mob.recoverEnvStats();
		mob.recoverCharStats();
	}
	
	private StringBuffer levelAdjuster(MOB mob, int adjuster)
	{
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+1);
		
		int neededNext=1000;
		for(int i=1;i<mob.baseEnvStats().level();i++)
		{
			neededNext+=1000+(500*i);
		}
		mob.setExpNextLevel(neededNext);
		if(mob.getExperience()>mob.getExpNextLevel())
			mob.setExperience(mob.getExpNextLevel()-1000);
		
		StringBuffer theNews=new StringBuffer("");
		
		theNews.append("You are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+".\n\r");
		
		int newHitPointGain=minHitPointsPerLevel+(int)Math.floor(Math.random()*(maxHitPointsPerLevel-minHitPointsPerLevel))+minHitPointsPerLevel;
		newHitPointGain+=(int)Math.floor(Util.div(mob.charStats().getConstitution(),2.0))-4;
		if(newHitPointGain<=0) newHitPointGain=1;
		newHitPointGain=newHitPointGain*adjuster;
		mob.maxState().setHitPoints(mob.maxState().getHitPoints()+newHitPointGain);
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("You have gained "+newHitPointGain+" hit points, ");
		
		int mvGain=(int)Math.round(Util.div(mob.charStats().getStrength(),9.0))*8;
		mvGain=mvGain*adjuster;
		mob.maxState().setMovement(mob.maxState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+" move points, ");
		
		int attGain=(int)Math.round(Util.div(mob.charStats().getCurStat(this.attackAttribute),6.0))+this.bonusAttackLevel;
		attGain=attGain*adjuster;
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+attGain);
		mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+attGain);
		theNews.append(attGain+" attack points, ");
		
		int manaGain=(int)Math.round(Util.div(mob.charStats().getIntelligence(),18.0))*manaMultiplier;
		manaGain=manaGain*adjuster;
		mob.maxState().setMana(mob.maxState().getMana()+manaGain);
		theNews.append(manaGain+" points of mana, ");
		
		mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+(damageBonusPerLevel*adjuster));
		
		return theNews;
	}
	
	public void buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender)
	{
		if(!mob.isMonster()) return;
		
		mob.setAlignment(500);
		mob.baseEnvStats().setLevel(1);
		mob.baseCharStats().setGender(gender);
		mob.baseCharStats().setStrength(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setWisdom(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setIntelligence((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setDexterity(1+(int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setConstitution((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setCharisma((int)Math.round(CharStats.AVG_VALUE));
		mob.baseCharStats().setMyClass(this);
		mob.baseEnvStats().setArmor(50);
		mob.baseEnvStats().setLevel(1);
		mob.baseEnvStats().setSensesMask(0);
		mob.baseEnvStats().setWeight(weight);
		mob.baseEnvStats().setDamage(10);
		mob.setWimpHitPoint(wimp);
		mob.setMoney(10);
		mob.maxState().setHitPoints(20);
		mob.maxState().setMovement(100);
		mob.maxState().setMana(100);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.baseCharStats().getMyClass().newCharacter(mob);
			
		for(int lvl=1;lvl<level;lvl++)
		{
			mob.setMoney(mob.getMoney()+10);
			switch(lvl % 6)
			{
			case 0:
				mob.baseCharStats().setStrength(mob.baseCharStats().getStrength()+1);
				break;
			case 1:
				mob.baseCharStats().setDexterity(mob.baseCharStats().getDexterity()+1);
				break;
			case 2:
				mob.baseCharStats().setIntelligence(mob.baseCharStats().getIntelligence()+1);
				break;
			case 3:
				mob.baseCharStats().setConstitution(mob.baseCharStats().getConstitution()+1);
				break;
			case 4:
				mob.baseCharStats().setCharisma(mob.baseCharStats().getCharisma()+1);
				break;
			case 5:
				mob.baseCharStats().setWisdom(mob.baseCharStats().getWisdom()+1);
				break;
			}
			int oldattack=mob.baseEnvStats().attackAdjustment();
			mob.charStats().getMyClass().gainExperience(mob,null,mob.getExpNeededLevel()+1);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
		}
		while(mob.inventorySize()>0)
			mob.delInventory(mob.fetchInventory(0));
		mob.recoverMaxState();
	}
	
	public void level(MOB mob)
	{
		StringBuffer theNews=new StringBuffer("You have L E V E L E D ! ! ! ! ! \n\r\n\r");
		theNews.append(levelAdjuster(mob,1));
		
		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getWisdom(),4.0))+bonusPracLevel;
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append(practiceGain+" practices, ");
		
		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and "+trainGain+" training point.\n\r");
		
		mob.tell(theNews.toString());
		mob.recoverEnvStats();
		mob.recoverCharStats();
	}
	
	public int getMaxStat(int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return -1;
		return maxStat[abilityCode];
	}
	
	public boolean canAdvance(MOB mob, int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return false;
		if(mob.charStats().getCurStat(abilityCode)>=getMaxStat(abilityCode)) return false;
		return true;
	}
	
}
