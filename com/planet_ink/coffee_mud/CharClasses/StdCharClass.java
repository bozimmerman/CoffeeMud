package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdCharClass implements CharClass, Cloneable
{
	public String ID(){return "StdCharClass";}
	public String name(){return "mob";}
	public String baseClass(){return ID();}
	public int getBonusPracLevel(){return 0;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getPracsFirstLevel(){return 5;}
	public int getTrainsFirstLevel(){return 3;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getMovementMultiplier(){return 10;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 3;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 6;}
	protected int maxStatAdj[]={0,0,0,0,0,0};
	protected Vector outfitChoices=null;
	public int allowedArmorLevel(){return CharClass.ARMOR_ANY;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_ANY;}
	protected HashSet disallowedWeaponClasses(MOB mob){return null;}
	protected HashSet requiredWeaponMaterials(){return null;}
	protected int requiredArmorSourceMinor(){return -1;}
	protected String armorFailMessage(){return "<S-NAME> fumble(s) <S-HIS-HER> <SKILL> due to <S-HIS-HER> armor!";}
	private static boolean commonMapped=false;
	public boolean raceless(){return false;}
	public boolean leveless(){return false;}
	public boolean expless(){return false;}
	
	public StdCharClass()
	{
		if(!commonMapped)
		{
			commonMapped=true;
			CMAble.addCharAbilityMapping("All",1,"Armorsmithing",false);
			CMAble.addCharAbilityMapping("All",1,"Blacksmithing",false);
			CMAble.addCharAbilityMapping("All",1,"Butchering",false);
			CMAble.addCharAbilityMapping("All",1,"Carpentry",false);
			CMAble.addCharAbilityMapping("All",1,"Chopping",false);
			CMAble.addCharAbilityMapping("All",1,"ClanCrafting",false);
			CMAble.addCharAbilityMapping("All",1,"Cobbling",false);
			CMAble.addCharAbilityMapping("All",10,"Construction",false);
			CMAble.addCharAbilityMapping("All",1,"Cooking",false);
			CMAble.addCharAbilityMapping("All",1,"Baking",false);
			CMAble.addCharAbilityMapping("All",1,"FoodPrep",false);
			CMAble.addCharAbilityMapping("All",1,"Digging",false);
			CMAble.addCharAbilityMapping("All",1,"Distilling",false);
			CMAble.addCharAbilityMapping("All",1,"Drilling",false);
			CMAble.addCharAbilityMapping("All",1,"Dyeing",false);
			CMAble.addCharAbilityMapping("All",1,"Embroidering",false);
			CMAble.addCharAbilityMapping("All",1,"Engraving",false);
			CMAble.addCharAbilityMapping("All",10,"Farming",false);
			CMAble.addCharAbilityMapping("All",1,"Costuming",false);
			CMAble.addCharAbilityMapping("All",1,"FireBuilding",false);
			CMAble.addCharAbilityMapping("All",1,"Fishing",false);
			CMAble.addCharAbilityMapping("All",1,"Fletching",false);
			CMAble.addCharAbilityMapping("All",1,"Foraging",false);
			CMAble.addCharAbilityMapping("All",1,"GlassBlowing",false);
			CMAble.addCharAbilityMapping("All",1,"Herbology",false);
			CMAble.addCharAbilityMapping("All",1,"Hunting",false);
			CMAble.addCharAbilityMapping("All",1,"JewelMaking",false);
			CMAble.addCharAbilityMapping("All",1,"Lacquerring",false);
			CMAble.addCharAbilityMapping("All",1,"LeatherWorking",false);
			CMAble.addCharAbilityMapping("All",15,"LockSmith",false);
			CMAble.addCharAbilityMapping("All",10,"Masonry",false);
			CMAble.addCharAbilityMapping("All",30,"MasterTailoring",false);
			CMAble.addCharAbilityMapping("All",30,"MasterCostuming",false);
			CMAble.addCharAbilityMapping("All",30,"MasterLeatherWorking",false);
			CMAble.addCharAbilityMapping("All",30,"MasterWeaponsmithing",false);
			CMAble.addCharAbilityMapping("All",30,"MasterArmorsmithing",false);
			CMAble.addCharAbilityMapping("All",20,"Merchant",false);
			CMAble.addCharAbilityMapping("All",1,"Mining",false);
			CMAble.addCharAbilityMapping("All",5,"Painting",false);
			CMAble.addCharAbilityMapping("All",5,"PaperMaking",false);
			CMAble.addCharAbilityMapping("All",1,"Pottery",false);
			CMAble.addCharAbilityMapping("All",1,"ScrimShaw",false);
			CMAble.addCharAbilityMapping("All",1,"Sculpting",false);
			CMAble.addCharAbilityMapping("All",1,"Searching",false);
			CMAble.addCharAbilityMapping("All",4,"Shipwright",false);
			CMAble.addCharAbilityMapping("All",1,"Smelting",false);
			CMAble.addCharAbilityMapping("All",1,"SmokeRings",false);
			CMAble.addCharAbilityMapping("All",10,"Speculate",false);
			CMAble.addCharAbilityMapping("All",1,"Tailoring",false);
			CMAble.addCharAbilityMapping("All",20,"Taxidermy",false);
			CMAble.addCharAbilityMapping("All",4,"Wainwrighting",false);
			CMAble.addCharAbilityMapping("All",1,"Weaponsmithing",false);
			CMAble.addCharAbilityMapping("All",1,"Weaving",false);
		
		
			CMAble.addCharAbilityMapping("Mage",1,"Alchemy",false);
			CMAble.addCharAbilityMapping("Bard",10,"Alchemy",false);
			CMAble.addCharAbilityMapping("Cleric",1,"Alchemy",false);
		}
	}
	
	public boolean isGeneric(){return false;}
	public int availabilityCode(){return 0;}
	
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
			&&(!mob.charStats().getCurrentClass().baseClass().equals(baseClass()))
			&&(!mob.charStats().getCurrentClass().baseClass().equals("StdCharClass"))
			&&(!mob.charStats().getCurrentClass().baseClass().equals("Commoner")))
			{
				if(!quiet)
					mob.tell("You must be a "+baseClass()+" type to become a "+name()+".");
				return false;
			}
		}
		return true;
	}
	public String weaponLimitations()
	{ return WEAPONS_LONGDESC[allowedWeaponLevel()];}
	public String armorLimitations()
	{ return ARMOR_LONGDESC[allowedArmorLevel()];}
	public String otherLimitations(){return "";}
	public String otherBonuses(){return "";}
	public String statQualifications(){return "";}
	
	protected HashSet buildDisallowedWeaponClasses(){return buildDisallowedWeaponClasses(allowedWeaponLevel());}
	protected HashSet buildDisallowedWeaponClasses(int lvl)
	{
		if(lvl==CharClass.WEAPONS_ANY)
			return null;
		int[] set=CharClass.WEAPONS_SETS[lvl];
		HashSet H=new HashSet();
		if(set[0]>Weapon.classifictionDescription.length)
			return null;
		for(int i=0;i<Weapon.classifictionDescription.length;i++)
		{
			boolean found=false;
			for(int s=0;s<set.length;s++)
				if(set[s]==i) found=true;
			if(!found) H.add(new Integer(i));
		}
		return H;
	}
	protected HashSet buildRequiredWeaponMaterials()
	{
		if(allowedWeaponLevel()==CharClass.WEAPONS_ANY)
			return null;
		int[] set=CharClass.WEAPONS_SETS[allowedWeaponLevel()];
		if(set[0]>Weapon.classifictionDescription.length)
		{
			HashSet H=new HashSet();
			for(int s=0;s<set.length;s++)
				H.add(new Integer(set[s]));
			return H;
		}
		else
			return null;
	}


	protected boolean isQualifyingAuthority(MOB mob, Ability A)
	{
		CharClass C=null;
		int ql=0;
		for(int i=(mob.charStats().numClasses()-1);i>=0;i--) // last one is current
		{
			C=mob.charStats().getMyClass(i);
			ql=CMAble.getQualifyingLevel(C.ID(),true,A.ID());
			if((C!=null)
			&&(ql>0)
			&&(ql<=mob.charStats().getClassLevel(C)))
				return (C==this);
		}
		return false;
	}
	
	
	protected boolean armorCheck(MOB mob, int sourceCode, Environmental E)
	{   
		if(!(E instanceof Ability)) return true;
		if((allowedArmorLevel()!=CharClass.ARMOR_ANY)
		&&((requiredArmorSourceMinor()<0)||(sourceCode&CMMsg.MINOR_MASK)==requiredArmorSourceMinor())
		&&(isQualifyingAuthority(mob,(Ability)E))
		&&(mob.isMine(E))
		&&(!E.ID().equals("Skill_Recall"))
		&&(((Ability)E).classificationCode()!=Ability.COMMON_SKILL)
		&&(((Ability)E).classificationCode()!=Ability.LANGUAGE)
		&&(!CoffeeUtensils.armorCheck(mob,allowedArmorLevel()))
		&&(Dice.rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2)))
			return false;
		return true;
	}
	protected boolean weaponCheck(MOB mob, int sourceCode, Environmental E)
	{
		if((((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_WEAPONATTACK)||((sourceCode&CMMsg.MINOR_MASK)==CMMsg.TYP_THROW))
		&&(E instanceof Weapon)
		&&(mob.charStats().getCurrentClass()==this)
		&&(((requiredWeaponMaterials()!=null)&&(!requiredWeaponMaterials().contains(new Integer(((Weapon)E).material()&EnvResource.MATERIAL_MASK))))
			||((disallowedWeaponClasses(mob)!=null)&&(disallowedWeaponClasses(mob).contains(new Integer(((Weapon)E).weaponClassification())))))
		&&(Dice.rollPercentage()>(mob.charStats().getStat(getAttackAttribute())*2))
		&&(mob.fetchWieldedItem()!=null))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+E.name()+".");
			return false;
		}
		return true;
	}

	protected void giveMobAbility(MOB mob, Ability A, int profficiency, String defaultParm, boolean isBorrowedClass)
	{ giveMobAbility(mob,A,profficiency,defaultParm,isBorrowedClass,true);}
	protected void giveMobAbility(MOB mob, Ability A, int profficiency, String defaultParm, boolean isBorrowedClass, boolean autoInvoke)
	{
		if(mob.fetchAbility(A.ID())==null)
		{
			A=(Ability)A.copyOf();
			A.setBorrowed(mob,isBorrowedClass);
			A.setProfficiency(profficiency);
			A.setMiscText(defaultParm);
			mob.addAbility(A);
			if(autoInvoke)
				A.autoInvocation(mob);
		}
	}

	public int[] maxStatAdjustments()
	{
		return maxStatAdj;
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if((CMAble.getQualifyingLevel(ID(),true,A.ID())>0)
			&&(CMAble.getQualifyingLevel(ID(),true,A.ID())<=mob.baseCharStats().getClassLevel(this))
			&&(CMAble.getDefaultGain(ID(),true,A.ID())))
				giveMobAbility(mob,A,CMAble.getDefaultProfficiency(ID(),true,A.ID()),CMAble.getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
		}
	}
	public void endCharacter(MOB mob)
	{
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

	public Vector outfit(){return outfitChoices;}
	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		if(affectableStats.getCurrentClass()==this)
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			affectableStats.setStat(CharStats.MAX_STRENGTH_ADJ+i,affectableStats.getStat(CharStats.MAX_STRENGTH_ADJ+i)+maxStatAdj[i]);
	}

	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.source()==myHost)
		&&(!msg.source().isMonster()))
		{
			if(!armorCheck(msg.source(),msg.sourceCode(),msg.tool()))
			{
				if(msg.tool()==null)
				    msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,Util.replaceAll(armorFailMessage(),"<SKILL>","maneuver"));
				else
				    msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,Util.replaceAll(armorFailMessage(),"<SKILL>",msg.tool().name()+" attempt"));
				return false;
			}
			if(!weaponCheck(msg.source(),msg.sourceCode(),msg.tool()))
				return false;
		}
		return true;
	}
	

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	    if((msg.source()==myHost)
	    &&(msg.targetMinor()==CMMsg.TYP_WIELD)
	    &&(msg.target() instanceof Weapon)
		&&(msg.source().charStats().getCurrentClass()==this)
	    &&(!msg.source().isMonster())
		&&(((requiredWeaponMaterials()!=null)&&(!requiredWeaponMaterials().contains(new Integer(((Weapon)msg.target()).material()&EnvResource.MATERIAL_MASK))))
			||((disallowedWeaponClasses(msg.source())!=null)&&(disallowedWeaponClasses(msg.source()).contains(new Integer(((Weapon)msg.target()).weaponClassification()))))))
	        msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),null,CMMsg.TYP_OK_VISUAL,"<T-NAME> feel(s) a bit strange in your hands.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
	}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void gainExperience(MOB mob,
							   MOB victim,
							   String homageMessage,
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
				if(levelFactor>new Integer(levelLimit).doubleValue())
					levelFactor=new Integer(levelLimit).doubleValue();
				theAmount=theAmount+Util.mul(levelFactor,amount);
			}

			double victimFactor=Util.div((500.0-new Integer(victim.getAlignment()).doubleValue()),10.0);
			double mobFactor=Util.div(Math.abs(500.0-new Integer(mob.getAlignment()).doubleValue()),1000.0)+0.5;
			mob.setAlignment(mob.getAlignment()+(int)Math.round(mobFactor*victimFactor*0.50));

			double alignExpFactor=Math.abs(Util.div(victim.getAlignment()-mob.getAlignment(),1000.0));
			amount=(int)Math.round((theAmount/2.0)+((theAmount/2.0)*alignExpFactor));
		}
		if((mob.getLiegeID().length()>0)&&(amount>2))
		{
			MOB sire=CMMap.getPlayer(mob.getLiegeID());
			if((sire!=null)&&(Sense.isInTheGame(sire,true)))
			{
				int sireShare=(int)Math.round(Util.div(amount,10.0));
				if(sireShare<=0) sireShare=1;
				amount-=sireShare;
				MUDFight.postExperience(sire,null," from "+mob.name(),sireShare,quiet);
			}
		}
		if((mob.getClanID().length()>0)&&(amount>2))
		{
			Clan C=Clans.getClan(mob.getClanID());
			if(C!=null) amount=C.applyExpMods(amount);
		}

		mob.setExperience(mob.getExperience()+amount);
		if(homageMessage==null) homageMessage="";
		if(!quiet)
		{
			if(amount>1)
				mob.tell("^N^!You gain ^H"+amount+"^N^! experience points"+homageMessage+".^N");
			else
			if(amount>0)
				mob.tell("^N^!You gain ^H"+amount+"^N^! experience point"+homageMessage+".^N");
		}

		while((mob.getExperience()>=mob.getExpNextLevel())
		&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			level(mob);
	}

	public void unLevel(MOB mob)
	{
		if((mob.baseEnvStats().level()<2) 
		||(CMSecurity.isDisabled("LEVELS")))
		    return;
		mob.tell("^ZYou have ****LOST A LEVEL****^.^N\n\r\n\r"+CommonStrings.msp("doh.wav",60));
		if(!mob.isMonster())
			CommonMsgs.channel("WIZINFO","",mob.Name()+" has just lost a level.",true);

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
	public int getLevelExperience(int level)
	{ return neededToBeLevel(level);}

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

		int conStat=mob.charStats().getStat(CharStats.CONSTITUTION);
		int maxConStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.CONSTITUTION));
		if(conStat>maxConStat) conStat=maxConStat;
		int newHitPointGain=(int)Math.floor(Util.div(conStat,getHPDivisor())+Dice.roll(getHPDice(),getHPDie(),0));
		if(newHitPointGain<=0)
		{
			if(conStat>=1)
				newHitPointGain=adjuster;
		}
		else
			newHitPointGain=newHitPointGain*adjuster;
		mob.baseState().setHitPoints(mob.baseState().getHitPoints()+newHitPointGain);
		if(mob.baseState().getHitPoints()<20) mob.baseState().setHitPoints(20);
		mob.curState().setHitPoints(mob.curState().getHitPoints()+newHitPointGain);
		theNews.append("^NYou have gained ^H"+newHitPointGain+"^? hit " +
			(newHitPointGain!=1?"points":"point") + ", ^H");

		double lvlMul=1.0;//-Util.div(mob.envStats().level(),100.0);
		if(lvlMul<0.1) lvlMul=.1;
		int mvStat=mob.charStats().getStat(CharStats.STRENGTH);
		int maxMvStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.STRENGTH));
		if(mvStat>maxMvStat) mvStat=maxMvStat;
		int mvGain=(int)Math.round(lvlMul*Util.mul(Util.div(mvStat,18.0),getMovementMultiplier()));
		mvGain=mvGain*adjuster;
		mob.baseState().setMovement(mob.baseState().getMovement()+mvGain);
		mob.curState().setMovement(mob.curState().getMovement()+mvGain);
		theNews.append(mvGain+"^N move " + (mvGain!=1?"points":"point") + ", ^H");

		int attStat=mob.charStats().getStat(getAttackAttribute());
		int maxAttStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+getAttackAttribute()));
		if(attStat>=maxAttStat) attStat=maxAttStat;
		int attGain=(int)Math.round(Util.div(attStat,6.0))+getBonusAttackLevel();
		if(mvStat>=25)attGain+=2;
		else
		if(mvStat>=22)attGain+=1;
		attGain=attGain*adjuster;
		mob.baseEnvStats().setAttackAdjustment(mob.baseEnvStats().attackAdjustment()+attGain);
		mob.envStats().setAttackAdjustment(mob.envStats().attackAdjustment()+attGain);
		theNews.append(attGain+"^N attack " + (attGain!=1?"points":"point") + ", ^H");

		int man2Stat=mob.charStats().getStat(getAttackAttribute());
		int maxMan2Stat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+getAttackAttribute()));
		if(man2Stat>maxMan2Stat) man2Stat=maxMan2Stat;
		
		int manStat=mob.charStats().getStat(CharStats.INTELLIGENCE);
		int maxManStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.INTELLIGENCE));
		if(manStat>maxManStat) manStat=maxManStat;
		int manaGain=(int)Math.floor(Util.div(manStat,getManaDivisor())+Dice.roll(getManaDice(),getManaDie(),0));
		if(man2Stat>17) manaGain=manaGain+((man2Stat-17)/2);
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

	public MOB fillOutMOB(MOB mob, int level)
	{
		if(mob==null) mob=CMClass.getMOB("StdMOB");
		if(!mob.isMonster()) return mob;

		long rejuv=MudHost.TICKS_PER_RLMIN+MudHost.TICKS_PER_RLMIN+(level*MudHost.TICKS_PER_RLMIN/2);
		if(rejuv>(MudHost.TICKS_PER_RLMIN*20)) rejuv=(MudHost.TICKS_PER_RLMIN*20);
		mob.baseEnvStats().setLevel(level);
		mob.baseEnvStats().setRejuv((int)rejuv);
		mob.baseEnvStats().setSpeed(getLevelSpeed(mob));
		mob.baseEnvStats().setArmor(getLevelArmor(mob));
		mob.baseEnvStats().setDamage(getLevelDamage(mob));
		mob.baseEnvStats().setAttackAdjustment(getLevelAttack(mob));
		mob.setMoney(Dice.roll(1,level,0)+Dice.roll(1,10,0));
		return mob;
	}

	public void loseExperience(MOB mob, int amount)
	{
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return;
        if((mob.getLiegeID().length()>0)&&(amount>2))
        {
			MOB sire=CMMap.getPlayer(mob.getLiegeID());
			if((sire!=null)&&(Sense.isInTheGame(sire,true)))
            {
                int sireShare=(int)Math.round(Util.div(amount,10.0));
                amount-=sireShare;
				if(MUDFight.postExperience(sire,null,"",-sireShare,true))
					sire.tell("^N^!You lose ^H"+sireShare+"^N^! experience points from "+mob.Name()+".^N");
            }
        }
        if((mob.getClanID().length()>0)&&(amount>2))
        {
            Clan C=Clans.getClan(mob.getClanID());
            if((C!=null)&&(C.getTaxes()>0.0))
            {
                int clanshare=(int)Math.round(Util.mul(amount,C.getTaxes()));
                if(clanshare>0)
				{
                    amount-=clanshare; 
                    C.adjExp(clanshare*-1);
					C.update();
				}
            }
        }
		mob.setExperience(mob.getExperience()-amount);
		int neededLowest=neededToBeLevel(mob.baseEnvStats().level()-2);
		if((mob.getExperience()<neededLowest)&&(mob.baseEnvStats().level()>1))
			unLevel(mob);
	}
	public void level(MOB mob)
	{
	    if(CMSecurity.isDisabled("LEVELS")) 
	        return;
		StringBuffer theNews=new StringBuffer("^xYou have L E V E L E D ! ! ! ! ! ^.^N\n\r\n\r"+CommonStrings.msp("level_gain.wav",60));
		theNews.append(levelAdjuster(mob,1));
		if(mob.playerStats()!=null)
		{
			CommonMsgs.channel("WIZINFO","",mob.Name()+" has just gained a level.",true);
			if(mob.soulMate()==null)
				CoffeeTables.bump(mob,CoffeeTables.STAT_LEVELSGAINED);
		}

		int practiceGain=(int)Math.floor(Util.div(mob.charStats().getStat(CharStats.WISDOM),4.0))+getBonusPracLevel();
		if(practiceGain<=0)practiceGain=1;
		mob.setPractices(mob.getPractices()+practiceGain);
		theNews.append(" ^H" + practiceGain+"^N practice " +
			( practiceGain != 1? "points" : "point" ) + ", ");

		int trainGain=1;
		if(trainGain<=0)trainGain=1;
		mob.setTrains(mob.getTrains()+trainGain);
		theNews.append("and ^H"+trainGain+"^N training sessions.\n\r^N");

		mob.tell(theNews.toString());

		grantAbilities(mob,false);

		// check for autoinvoking abilities
		for(int a=0;a<mob.numLearnedAbilities();a++)
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

	public int getLevelMana(MOB mob)
	{
		return 100+((mob.baseEnvStats().level()-1)*((int)Math.round(Util.div(mob.baseCharStats().getStat(CharStats.INTELLIGENCE),getHPDivisor())))+(getHPDie()*(getHPDice()+1)/2));
	}

	public int getLevelAttack(MOB mob)
	{
		int attGain=(int)Math.round(Util.div(mob.charStats().getStat(getAttackAttribute()),6.0))+getBonusAttackLevel();
		return ((mob.baseEnvStats().level()-1)*attGain);
	}

	public int getLevelArmor(MOB mob)
	{
		return 100-(mob.baseEnvStats().level()*(4+getBonusAttackLevel()));
	}

	public int getLevelDamage(MOB mob)
	{
		return 2+(mob.baseEnvStats().level());
	}

	public double getLevelSpeed(MOB mob)
	{
		return 1.0+Math.floor(Util.div(mob.baseEnvStats().level(),25.0));
	}

	public int getLevelMove(MOB mob)
	{
		int move=100;
		double lvlMul=1.0;//-Util.div(mob.envStats().level(),100.0);
		if(lvlMul<0.1) lvlMul=.1;
		if(mob.baseEnvStats().level()>1)
			move+=((int)Math.round(Util.mul(mob.baseEnvStats().level()-1,Util.mul(Util.mul(lvlMul,Util.div(mob.baseCharStats().getStat(CharStats.STRENGTH),18.0)),getMovementMultiplier()))));
		return move;
	}

	protected boolean isValidBeneficiary(MOB killer,
									   MOB killed,
									   MOB mob,
									   HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}
	public HashSet dispenseExperience(MOB killer, MOB killed)
	{
		if((killer==null)||(killed==null)) return new HashSet();
		Room deathRoom=killed.location();
		if(deathRoom==null) deathRoom=killer.location();

		HashSet beneficiaries=new HashSet();
		HashSet followers=(killer!=null)?killer.getGroupMembers(new HashSet()):(new HashSet());

		int totalLevels=0;
		int expAmount=100;

		if(deathRoom!=null)
		{
			for(int m=0;m<deathRoom.numInhabitants();m++)
			{
				MOB mob=deathRoom.fetchInhabitant(m);
				if((isValidBeneficiary(killer,killed,mob,followers))
				&&(killer!=killed)
				&&(!beneficiaries.contains(mob)))
				{
					beneficiaries.add(mob);
					totalLevels+=(mob.envStats().level()*mob.envStats().level());
					expAmount+=25;
				}
			}
		}

		if(beneficiaries.size()>0)
			for(Iterator i=beneficiaries.iterator();i.hasNext();)
			{
				MOB mob=(MOB)i.next();
				int myAmount=(int)Math.round(Util.mul(expAmount,Util.div(mob.envStats().level()*mob.envStats().level(),totalLevels)));
				if(myAmount>100) myAmount=100;
				MUDFight.postExperience(mob,killed,"",myAmount,false);
			}
		return beneficiaries;
	}
	public String classParms(){ return "";}
	public void setClassParms(String parms){}
	protected static String[] CODES={"CLASS","PARMS"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+classParms();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClassParms(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(CharClass E)
	{
		if(!(E instanceof StdCharClass)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
