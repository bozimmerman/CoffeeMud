package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class StdCharClass implements CharClass, Cloneable
{
	public String ID(){return "StdCharClass";}
	public String name(){return "mob";}
	public String baseClass(){return ID();}
	public int getMinHitPointsLevel(){return 2;}
	public int getMaxHitPointsLevel(){return 12;}
	public int getBonusPracLevel(){return 0;}
	public int getBonusManaLevel(){return 15;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getPracsFirstLevel(){return 5;}
	public int getTrainsFirstLevel(){return 3;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getMovementMultiplier(){return 5;}
	protected int maxStat[]={18,18,18,18,18,18};
	private static long wearMask=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;

	public boolean playerSelectable()
	{
		return false;
	}

	public void cloneFix(CharClass C)
	{
	}

	public CharClass copyOf()
	{
		try
		{
			StdCharClass E=(StdCharClass)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public boolean loaded(){return true;}
	public void setLoaded(boolean truefalse){};

	public int classDurationModifier(MOB myChar, Ability skill, int duration)
	{ return duration;}

	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable myChar, int tickID){
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if((!mob.isMonster())&&(mob.baseEnvStats().level()>0))
		{
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
			{
				if(!quiet)
					mob.tell("But you are already a "+name()+"!");
				return false;
			}
			if(CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
			{
				if(!quiet)
					mob.tell("You should be happy to be a "+name()+"!");
				return false;
			}
			else
			if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI"))
			&&(!mob.charStats().getCurrentClass().baseClass().equals(baseClass())))
			{
				if(!quiet)
					mob.tell("You must be a "+baseClass()+" type to become a "+name()+".");
				return false;
			}
			if(mob.getTrains()<1)
			{
				if(!quiet)
					mob.tell("You need a training session to become a "+name()+".");
				return false;
			}
		}
		return true;
	}
	public String weaponLimitations(){return "";}
	public String armorLimitations(){return "";}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public String statQualifications(){return "";}
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}

	public boolean armorCheck(MOB mob)
	{
		if(allowedArmorLevel()==CharClass.ARMOR_ANY) return true;

		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if(I==null) break;
			if((!I.amWearingAt(Item.INVENTORY))
			&&((I instanceof Armor)||(I instanceof Shield)))
			{
				boolean ok=true;
				switch(I.material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_LEATHER:
					if(allowedArmorLevel()==CharClass.ARMOR_CLOTH)
						ok=false;
					break;
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_MITHRIL:
					ok=false;
					break;
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_ROCK:
					if((allowedArmorLevel()==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel()==CharClass.ARMOR_LEATHER))
						ok=false;
					break;
				default:
					break;
				}
				if((!ok)&&((I.rawProperLocationBitmap()&wearMask)>0))
					return false;
			}
		}
		return true;
	}

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

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),A.ID())>0)
			&&(CMAble.getQualifyingLevel(ID(),A.ID())<=mob.baseCharStats().getClassLevel(this))
			&&(CMAble.getDefaultGain(ID(),A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),A.ID()),CMAble.getDefaultParm(ID(),A.ID()),isBorrowedClass);
		}
	}
	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			mob.setPractices(mob.getPractices()+getPracsFirstLevel());
			mob.setTrains(mob.getTrains()+getTrainsFirstLevel());
			grantAbilities(mob,isBorrowedClass);
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

	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
	}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void gainExperience(MOB mob,
							   MOB victim,
							   String homage,
							   int amount,
							   boolean quiet)
	{
		if(victim!=null)
		{
			double theAmount=new Integer(amount).doubleValue();
			int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
			int levelDiff=victim.envStats().level()-mob.envStats().level();

			if(levelDiff<(-levelLimit) )
				theAmount=0.0;
			else
			if(levelLimit>0)
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
			MOB sire=CMMap.getPlayer(mob.getLeigeID());
			if(sire!=null)
			{
				int sireShare=(int)Math.round(Util.div(amount,10.0));
				if(sireShare<=0) sireShare=1;
				amount-=sireShare;
				if(sire.charStats()!=null)
					sire.charStats().getCurrentClass().gainExperience(sire,null," from "+mob.name(),sireShare,quiet);
			}
		}

		mob.setExperience(mob.getExperience()+amount);
		if(homage==null) homage="";
		if(!quiet)
		{
			if(amount>1)
				mob.tell("^N^!You gain ^H"+amount+"^N^! experience points"+homage+".^N");
			else
			if(amount>0)
				mob.tell("^N^!You gain ^H"+amount+"^N^! experience point"+homage+".^N");
		}

		while(mob.getExperience()>=mob.getExpNextLevel())
			level(mob);
	}

	public void unLevel(MOB mob)
	{
		if(mob.baseEnvStats().level()<2) return;
		mob.tell("^ZYou have ****LOST A LEVEL****^.^N\n\r\n\r"+CommonStrings.msp("doh.wav",60));

		levelAdjuster(mob,-1);
		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.WISDOM),4.0))+getBonusPracLevel();
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()-practiceGain);
		int trainGain=0;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()-trainGain);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
		mob.tell("^HYou are now a level "+mob.charStats().getClassLevel(mob.charStats().getCurrentClass())+" "+mob.charStats().getCurrentClass().name()+"^N.\n\r");
	}

	private static final int breakLevel=25;
	protected int neededToBeLevel(int level)
	{
		int neededLevel=1000;

		if(level==0)
			neededLevel=0;
		else
		for(int i=1;i<level;i++)
			if(i<breakLevel)
				neededLevel+=1000+(100*i);
			else
				neededLevel+=1000+(100*(breakLevel-1))+(25*(i-(breakLevel-1)));
		return neededLevel;
	}

	protected StringBuffer levelAdjuster(MOB mob, int adjuster)
	{
		mob.baseEnvStats().setLevel(mob.baseEnvStats().level()+adjuster);
		CharClass curClass=mob.baseCharStats().getCurrentClass();
		mob.baseCharStats().setClassLevel(curClass,mob.baseCharStats().getClassLevel(curClass)+adjuster);
		int classLevel=mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass());
		int gained=mob.getExperience()-mob.getExpNextLevel();
		if(gained<50) gained=50;

		int neededNext=neededToBeLevel(mob.baseEnvStats().level());
		int neededLower=neededToBeLevel(mob.baseEnvStats().level()-1);
		mob.setExpNextLevel(neededNext);
		if(mob.getExperience()>mob.getExpNextLevel())
			mob.setExperience(neededLower+gained);

		StringBuffer theNews=new StringBuffer("");

		mob.recoverCharStats();
		mob.recoverEnvStats();
		theNews.append("^HYou are now a "+mob.charStats().displayClassLevel(mob,false)+".^N\n\r");

		int newHitPointGain=getMinHitPointsLevel()+(int)Math.floor(Math.random()*(getMaxHitPointsLevel()-getMinHitPointsLevel()));
		newHitPointGain+=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.CONSTITUTION),2.0))-4;
		if(newHitPointGain<=0) newHitPointGain=1;
		newHitPointGain=newHitPointGain*adjuster;
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		if(mob.baseState().getHitPoints()<20) mob.baseState().setHitPoints(20);
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("^NYou have gained ^H"+newHitPointGain+"^? hit " +
			(newHitPointGain!=1?"points":"point") + ", ^H");

		int mvGain=(int)Math.round(Util.mul(Util.div(mob.charStats().getStat(CharStats.STRENGTH),9.0),getMovementMultiplier()));
		mvGain=mvGain*adjuster;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+"^N move " + (mvGain!=1?"points":"point") + ", ^H");

		int attGain=(int)Math.round(Util.div(mob.charStats().getStat(getAttackAttribute()),6.0))+getBonusAttackLevel();
		attGain=attGain*adjuster;
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+attGain);
		mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+attGain);
		theNews.append(attGain+"^N attack " + (attGain!=1?"points":"point") + ", ^H");

		int manaGain=(int)Math.round(Util.mul(Util.div(mob.charStats().getStat(CharStats.INTELLIGENCE),18.0),getBonusManaLevel()));
		manaGain=manaGain*adjuster;
		mob.baseState().setMana(mob.baseState().getMana()+manaGain);
		theNews.append(manaGain+"^N " + (manaGain!=1?"points":"point") + " of mana,");
		if((adjuster<0)&&(((classLevel+1)%getLevelsPerBonusDamage())==0))
			mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()-1);
		else
		if((adjuster>0)&&((classLevel%getLevelsPerBonusDamage())==0))
			mob.baseEnvStats().setDamage(mob.baseEnvStats().damage()+1);
		mob.recoverMaxState();
		return theNews;
	}

	public void buildMOB(MOB mob, int level, int alignment, int weight, int wimp, char gender)
	{
		if(!mob.isMonster()) return;

		mob.setAlignment(500);
		mob.baseCharStats().setStat(CharStats.GENDER,(int)gender);
		mob.baseCharStats().setStat(CharStats.STRENGTH,10);
		mob.baseCharStats().setStat(CharStats.WISDOM,10);
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,10);
		mob.baseCharStats().setStat(CharStats.DEXTERITY,13);
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,10);
		mob.baseCharStats().setStat(CharStats.CHARISMA,10);
		mob.baseCharStats().setStat(getAttackAttribute(),21);
		mob.baseCharStats().setCurrentClass(this);
		mob.baseCharStats().setClassLevel(this,1);
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
		mob.baseCharStats().getCurrentClass().startCharacter(mob,true,false);

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
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
			mob.charStats().getCurrentClass().gainExperience(mob,null,null,mob.getExpNeededLevel()+1,true);
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
			unLevel(mob);
	}
	public void level(MOB mob)
	{
		StringBuffer theNews=new StringBuffer("^xYou have L E V E L E D ! ! ! ! ! ^.^N\n\r\n\r"+CommonStrings.msp("level_gain.wav",60));
		theNews.append(levelAdjuster(mob,1));

		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.WISDOM),4.0))+getBonusPracLevel();
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append("^H" + practiceGain+"^N practice " +
			( practiceGain != 1? "points" : "point" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and ^H"+trainGain+"^N training sessions.\n\r^N");

		mob.tell(theNews.toString());

		grantAbilities(mob,false);

		// check for autoinvoking abilities
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(CMAble.qualifiesByLevel(mob,A)))
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
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getStat(CharStats.STRENGTH),9.0)*getMovementMultiplier())));
	}

	public boolean canAdvance(MOB mob, int abilityCode)
	{
		if((abilityCode<0)||(abilityCode>5)) return false;
		if(mob.baseCharStats().getStat(abilityCode)>=getMaxStat(abilityCode)) return false;
		return true;
	}

	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   Hashtable followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((mob.getVictim()==killed)
		 ||(followers.get(mob)!=null)
		 ||(mob==killer)))
			return true;
		return false;
	}

	public Hashtable dispenseExperience(MOB killer, MOB killed)
	{
		if((killer==null)||(killed==null)) return new Hashtable();
		Room deathRoom=killed.location();
		Hashtable beneficiaries=new Hashtable();
		Hashtable followers=(killer!=null)?killer.getGroupMembers(new Hashtable()):(new Hashtable());

		int totalLevels=0;
		int expAmount=100;
		for(int m=0;m<deathRoom.numInhabitants();m++)
		{
			MOB mob=deathRoom.fetchInhabitant(m);
			if((isValidBeneficiary(killer,killed,mob,followers))
			&&(beneficiaries.get(mob)==null))
			{
				beneficiaries.put(mob,mob);
				totalLevels+=mob.envStats().level();
				expAmount+=10;
			}
		}
		if(beneficiaries.size()>0)
			for(Enumeration e=beneficiaries.elements();e.hasMoreElements();)
			{
				MOB mob=(MOB)e.nextElement();
				int myAmount=(int)Math.round(Util.mul(expAmount,Util.div(mob.envStats().level(),totalLevels)));
				if(myAmount>100) myAmount=100;
				mob.charStats().getCurrentClass().gainExperience(mob,killed,"",myAmount,false);
			}
		return beneficiaries;
	}
}
