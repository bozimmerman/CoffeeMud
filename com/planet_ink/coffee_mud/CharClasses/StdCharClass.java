package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class StdCharClass implements CharClass
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="MOB";
	protected int minHitPointsPerLevel=2;
	protected int maxHitPointsPerLevel=12;
	protected int maxStat[]={18,18,18,18,18,18};
	protected int bonusPracLevel=0;
	protected int manaMultiplier=15;
	protected int attackAttribute=CharStats.STRENGTH;
	protected int bonusAttackLevel=1;
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

	protected void giveMobAbility(MOB mob, Ability A, int profficiency, boolean isBorrowedClass)
	{
		A=(Ability)A.copyOf();
		if(mob.fetchAbility(A.ID())==null)
		{
			A.setBorrowed(mob,isBorrowedClass);
			A.setProfficiency(profficiency);
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
	}

	public void newCharacter(MOB mob, boolean isBorrowed)
	{
		mob.setPractices(mob.getPractices()+practicesAtFirstLevel);
		mob.setTrains(mob.getTrains()+trainsAtFirstLevel);
	}

	public void outfit(MOB mob)
	{
	}
	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{

	}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		return true;
	}

	public void affect(MOB myChar, Affect affect)
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

			if(levelDiff<(-levelLimit) )
				theAmount=0.0;
			else
			{
				double levelFactor=Util.div(levelDiff,levelLimit);
				theAmount=theAmount+Util.mul(levelFactor,amount);
			}
			
			double victimFactor=Util.div((500.0-new Integer(victim.getAlignment()).doubleValue()),10.0);
			double mobFactor=Util.div(Math.abs(500.0-new Integer(mob.getAlignment()).doubleValue()),1000.0)+0.5;
			mob.setAlignment(mob.getAlignment()+(int)Math.round(mobFactor*victimFactor));
			
			double alignExpFactor=Math.abs(Util.div(victim.getAlignment()-mob.getAlignment(),1000.0));
			amount=(int)Math.round((theAmount/2.0)+((theAmount/2.0)*alignExpFactor));
		}

		mob.setExperience(mob.getExperience()+amount);
		mob.tell("^BYou gain ^H"+amount+"^? experience points.^N");
		while(mob.getExperience()>mob.getExpNextLevel())
			level(mob);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		mob.tell("\n\r^ZYou've lost a level!!!^N");
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()-1);
		mob.tell("^HYou are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+"^N.\n\r");

		levelAdjuster(mob,-1);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	protected StringBuffer levelAdjuster(MOB mob, int adjuster)
	{
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+1);

		int neededNext=1000;
		for(int i=1;i<mob.baseEnvStats().level();i++)
		{
			neededNext+=1000+(125*i);
		}
		mob.setExpNextLevel(neededNext);
		if(mob.getExperience()>mob.getExpNextLevel())
			mob.setExperience(mob.getExpNextLevel()-1000);

		StringBuffer theNews=new StringBuffer("");

		theNews.append("^HYou are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+".^N\n\r");

		int newHitPointGain=minHitPointsPerLevel+(int)Math.floor(Math.random()*(maxHitPointsPerLevel-minHitPointsPerLevel))+minHitPointsPerLevel;
		newHitPointGain+=(int)Math.floor(Util.div(mob.charStats().getConstitution(),2.0))-4;
		if(newHitPointGain<=0) newHitPointGain=1;
		newHitPointGain=newHitPointGain*adjuster;
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("^BYou have gained ^H"+newHitPointGain+"^B hit " + 
			(newHitPointGain!=1?"points":"point") + ", ^H");

		int mvGain=(int)Math.round(Util.div(mob.charStats().getStrength(),9.0)*12);
		mvGain=mvGain*adjuster;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+"^B move " + (mvGain!=1?"points":"point") + ", ^H");

		int attGain=(int)Math.round(Util.div(mob.charStats().getCurStat(this.attackAttribute),6.0))+this.bonusAttackLevel;
		attGain=attGain*adjuster;
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+attGain);
		mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+attGain);
		theNews.append(attGain+"^B attack " + (attGain!=1?"points":"point") + ", ^H");

		int manaGain=(int)Math.round(Util.div(mob.charStats().getIntelligence(),18.0)*manaMultiplier);
		manaGain=manaGain*adjuster;
		mob.baseState().setMana(mob.baseState().getMana()+manaGain);
		theNews.append(manaGain+"^B " + (manaGain!=1?"points":"point") + " of mana,");

		mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+(damageBonusPerLevel*adjuster));
		mob.recoverMaxState();
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
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.baseCharStats().getMyClass().newCharacter(mob,true);

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
			mob.recoverMaxState();
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
		while(mob.inventorySize()>0)
		{
			Item I=mob.fetchInventory(0);
			if(I!=null) mob.delInventory(I);
		}
		mob.resetToMaxState();
	}

	public void level(MOB mob)
	{
		StringBuffer theNews=new StringBuffer("^XYou have L E V E L E D ! ! ! ! ! ^N\n\r\n\r");
		theNews.append(levelAdjuster(mob,1));

		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getWisdom(),4.0))+bonusPracLevel;
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append("^H" + practiceGain+"^B practice " +
			( practiceGain != 1? "sessions" : "session" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and ^H"+trainGain+"^B training point.\n\r^N");

		mob.tell(theNews.toString());
		
		// check for autoinvoking abilities
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A.envStats().level()==mob.envStats().level()))
				A.autoInvocation(mob);
		}
		
		// wrap it all up
		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public int getMaxStat(int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return -1;
		return maxStat[abilityCode];
	}
	public int getLevelMana(MOB mob)
	{
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getIntelligence(),18.0)*manaMultiplier)));
	}

	public int getLevelAttack(MOB mob)
	{
		int attGain=(int)Math.round(Util.div(mob.charStats().getCurStat(this.attackAttribute),6.0))+this.bonusAttackLevel;
		return ((mob.baseEnvStats().level()-1)*attGain);
	}

	public int getLevelArmor(MOB mob)
	{
		return 75-((mob.baseEnvStats().level()-1)*3);
	}

	public int getLevelDamage(MOB mob)
	{
		return 2+(mob.baseEnvStats().level());
	}

	public int getLevelMove(MOB mob)
	{
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getStrength(),9.0)*12)));
	}

	public boolean canAdvance(MOB mob, int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return false;
		if(mob.baseCharStats().getCurStat(abilityCode)>=getMaxStat(abilityCode)) return false;
		return true;
	}

}
