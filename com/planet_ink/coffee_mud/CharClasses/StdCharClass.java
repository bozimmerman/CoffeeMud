package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class StdCharClass implements CharClass
{
	public String ID(){return "StdCharClass";}
	public String name(){return "mob";}
	public int getMinHitPointsLevel(){return 2;}
	public int getMaxHitPointsLevel(){return 12;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getPracsFirstLevel(){return 5;}
	public int getTrainsFirstLevel(){return 3;}
	public int getLevelsPerBonusDamage(){ return 1;}
	protected int maxStat[]={18,18,18,18,18,18};

	public boolean playerSelectable()
	{
		return false;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		return true;
	}
	public String weaponLimitations(){return "";}
	public String armorLimitations(){return "";}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public String statQualifications(){return "";}

	protected void giveMobAbility(MOB mob, Ability A, int profficiency, String defaultParm, boolean isBorrowedClass)
	{ giveMobAbility(mob,A,profficiency,defaultParm,isBorrowedClass,true);}
	protected void giveMobAbility(MOB mob, Ability A, int profficiency, String defaultParm, boolean isBorrowedClass, boolean autoInvoke)
	{
		A=(Ability)A.copyOf();
		if(mob.fetchAbility(A.ID())==null)
		{
			A.setBorrowed(mob,isBorrowedClass);
			A.setProfficiency(profficiency);
			A.setMiscText(defaultParm);
			mob.addAbility(A);
			if(autoInvoke)
				A.autoInvocation(mob);
		}
	}

	public int[] maxStat()
	{
		return maxStat;
	}

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			mob.setPractices(mob.getPractices()+getPracsFirstLevel());
			mob.setTrains(mob.getTrains()+getTrainsFirstLevel());
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
				if((A.qualifyingLevel(mob)>0)&&(CMAble.getDefaultGain(ID(),A.ID())))
					giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
			}
		}
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

	public void gainExperience(MOB mob, 
							   MOB victim, 
							   String homage, 
							   int amount)
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
		if((homage!=null)&&(mob.getLeigeID().length()>0)&&(amount>2))
		{
			MOB sire=(MOB)CMMap.MOBs.get(mob.getLeigeID());
			if(sire!=null)
			{
				int sireShare=(int)Math.round(Util.div(amount,10.0));
				if(sireShare<=0) sireShare=1;
				amount-=sireShare;
				if((sire.charStats()!=null)&&(sire.charStats().getMyClass()!=null))
					sire.charStats().getMyClass().gainExperience(sire,null," from "+mob.name(),sireShare);
			}
		}

		mob.setExperience(mob.getExperience()+amount);
		if(homage==null) homage="";
		if(amount>1)
			mob.tell("^!You gain ^H"+amount+"^? experience points"+homage+".^N");
		else
		if(amount>0)
			mob.tell("^!You gain ^H"+amount+"^? experience point"+homage+".^N");
			
		while(mob.getExperience()>=mob.getExpNextLevel())
			level(mob);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		mob.tell("\n\r^ZYou've lost a level!!!^N");

		levelAdjuster(mob,-1);
		mob.tell("^HYou are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+"^N.\n\r");

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	protected int neededToBeLevel(int level)
	{
		int neededLevel=1000;
		if(level==0)
			neededLevel=0;
		else
		for(int i=1;i<level;i++)
			neededLevel+=1000+(125*i);
		return neededLevel;
	}
	
	protected StringBuffer levelAdjuster(MOB mob, int adjuster)
	{
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+adjuster);
		int gained=mob.getExperience()-mob.getExpNextLevel();
		if(gained<50) gained=50;

		int neededNext=neededToBeLevel(mob.baseEnvStats().level());
		int neededLower=neededToBeLevel(mob.baseEnvStats().level()-1);
		mob.setExpNextLevel(neededNext);
		if(mob.getExperience()>mob.getExpNextLevel())
			mob.setExperience(neededLower+gained);

		StringBuffer theNews=new StringBuffer("");

		theNews.append("^HYou are now a level "+mob.baseEnvStats().level()+" "+mob.charStats().getMyClass().name()+".^N\n\r");

		int newHitPointGain=getMinHitPointsLevel()+(int)Math.floor(Math.random()*(getMaxHitPointsLevel()-getMinHitPointsLevel()));
		newHitPointGain+=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.CONSTITUTION),2.0))-4;
		if(newHitPointGain<=0) newHitPointGain=1;
		newHitPointGain=newHitPointGain*adjuster;
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("^!You have gained ^H"+newHitPointGain+"^! hit " + 
			(newHitPointGain!=1?"points":"point") + ", ^H");

		int mvGain=(int)Math.round(Util.div(mob.charStats().getStat(CharStats.STRENGTH),9.0)*6);
		mvGain=mvGain*adjuster;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+"^! move " + (mvGain!=1?"points":"point") + ", ^H");

		int attGain=(int)Math.round(Util.div(mob.charStats().getStat(getAttackAttribute()),6.0))+getBonusAttackLevel();
		attGain=attGain*adjuster;
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+attGain);
		mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+attGain);
		theNews.append(attGain+"^! attack " + (attGain!=1?"points":"point") + ", ^H");

		int manaGain=(int)Math.round(Util.div(mob.charStats().getStat(CharStats.INTELLIGENCE),18.0)*getBonusManaLevel());
		manaGain=manaGain*adjuster;
		mob.baseState().setMana(mob.baseState().getMana()+manaGain);
		theNews.append(manaGain+"^! " + (manaGain!=1?"points":"point") + " of mana,");
		if((adjuster<0)&&(((mob.baseEnvStats().level()+1)%getLevelsPerBonusDamage())==0))
			mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()-1);
		else
		if((adjuster>0)&&((mob.baseEnvStats().level()%getLevelsPerBonusDamage())==0))
			mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+1);
		mob.recoverMaxState();
		return theNews;
	}

	public void buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender)
	{
		if(!mob.isMonster()) return;

		mob.setAlignment(500);
		mob.baseEnvStats().setLevel(1);
		mob.baseCharStats().setStat(CharStats.GENDER,(int)gender);
		mob.baseCharStats().setStat(CharStats.STRENGTH,11);
		mob.baseCharStats().setStat(CharStats.WISDOM,10);
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,10);
		mob.baseCharStats().setStat(CharStats.DEXTERITY,11);
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,10);
		mob.baseCharStats().setStat(CharStats.CHARISMA,10);
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
		mob.baseCharStats().getMyClass().startCharacter(mob,true,false);

		for(int lvl=1;lvl<level;lvl++)
		{
			mob.setMoney(mob.getMoney()+10);
			switch(lvl % 6)
			{
			case 0:
				mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+1);
				break;
			case 1:
				mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+1);
				break;
			case 2:
				mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+1);
				break;
			case 3:
				mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+1);
				break;
			case 4:
				mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+1);
				break;
			case 5:
				mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+1);
				break;
			}
			int oldattack=mob.baseEnvStats().attackAdjustment();
			mob.charStats().getMyClass().gainExperience(mob,null,null,mob.getExpNeededLevel()+1);
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

	public void loseExperience(MOB mob, int amount)
	{
		if((mob.isMonster())||(mob.soulMate()!=null)) return;
		int neededLowest=neededToBeLevel(mob.baseEnvStats().level()-2);
		mob.setExperience(mob.getExperience()-amount);
		if((mob.getExperience()<neededLowest)&&(mob.baseEnvStats().level()>1))
		{
			mob.tell("^xYou have ****LOST A LEVEL****^N\n\r\n\r");
			unLevel(mob);
		}
	}
	public void level(MOB mob)
	{
		StringBuffer theNews=new StringBuffer("^xYou have L E V E L E D ! ! ! ! ! ^N\n\r\n\r");
		theNews.append(levelAdjuster(mob,1));

		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.WISDOM),4.0))+getBonusPracLevel();
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append("^H" + practiceGain+"^! practice " +
			( practiceGain != 1? "sessions" : "session" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and ^H"+trainGain+"^! training point.\n\r^N");

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
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getStat(CharStats.INTELLIGENCE),18.0)*getBonusManaLevel())));
	}

	public int getLevelAttack(MOB mob)
	{
		int attGain=(int)Math.round(Util.div(mob.charStats().getStat(getAttackAttribute()),6.0))+getBonusAttackLevel();
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
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getStat(CharStats.STRENGTH),9.0)*6)));
	}

	public boolean canAdvance(MOB mob, int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return false;
		if(mob.baseCharStats().getStat(abilityCode)>=getMaxStat(abilityCode)) return false;
		return true;
	}
	public Hashtable dispenseExperience(MOB killer, MOB killed)
	{
		if((killer==null)||(killed==null)) return new Hashtable();
		Room deathRoom=killed.location();
		int expAmount=60;
		int totalLevels=0;
		
		Hashtable beneficiaries=new Hashtable();
		Hashtable followers=(killer!=null)?killer.getGroupMembers(new Hashtable()):(new Hashtable());

		for(int m=0;m<deathRoom.numInhabitants();m++)
		{
			MOB mob=deathRoom.fetchInhabitant(m);
			if((mob!=null)
			&&(!mob.amDead())
			&&(mob.charStats().getMyClass()!=null)
			&&(beneficiaries.get(mob)==null))
			{
				if((mob.getVictim()==killed)
				||(followers.get(mob)!=null)
				||(mob==killer))
				{
					beneficiaries.put(mob,mob);
					expAmount+=10;
					totalLevels+=mob.envStats().level();
				}
			}
		}
		if(beneficiaries.size()>0)
		{
			for(Enumeration e=beneficiaries.elements();e.hasMoreElements();)
			{
				MOB mob=(MOB)e.nextElement();
				int myAmount=(int)Math.round(Util.mul(expAmount,Util.div(mob.envStats().level(),totalLevels)));
				if(myAmount>100) myAmount=100;
				mob.charStats().getMyClass().gainExperience(mob,killed,"",myAmount);
			}
		}
		return beneficiaries;
	}

}
