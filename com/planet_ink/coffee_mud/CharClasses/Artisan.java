package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Artisan extends StdCharClass
{
	public String ID(){return "Artisan";}
	public String name(){return "Artisan";}
	public String baseClass(){return "Artisan";}
	public int getMaxHitPointsLevel(){return 5;}
	public int getBonusPracLevel(){return 5;}
	public int getBonusManaLevel(){return 12;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	public int getLevelsPerBonusDamage(){ return 25;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	protected static int[] allowedWeapons={
				Weapon.CLASS_NATURAL,
				Weapon.CLASS_DAGGER};
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};


	public Artisan()
	{
		super();
		maxStatAdj[CharStats.WISDOM]=7;
		maxStatAdj[CharStats.INTELLIGENCE]=7;
		if(ID().equals(baseClass())&&(!loaded()))
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Warrants",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Skill_WandUse",false);

			CMAble.addCharAbilityMapping(ID(),15,"Thief_Appraise",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Haggle",false);

			CMAble.addCharAbilityMapping(ID(),22,"Skill_Cage",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Skill_Stability",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Thief_Lore",false);
		}
	}

	public void cloneFix(CharClass C)
	{
		super.cloneFix(C);
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			if(A!=null)
			{
				int level=CMAble.getQualifyingLevel(ID(),A.ID());
				if((!CMAble.getDefaultGain(baseClass(),A.ID()))
				&&(level>0)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
				{
					if(level>1) level=level/2;
					if(level<1) level=1;
					CMAble.addCharAbilityMapping(ID(),level,A.ID(),25,true);
				}
			}
		}
	}
	
	public boolean playerSelectable()
	{
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			int exp=0;
			for(int a=0;a<mob.numAffects();a++)
			{
				Ability A=mob.fetchAffect(a);
				if((A!=null)
				&&(!A.isAutoInvoked())
				&&(mob.isMine(A))
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
					exp++;
			}
			if(exp>0)
				gainExperience(mob,null,mob.getLeigeID(),exp,false);
		}
		return super.tick(ticking,tickID);
	}
	
	public String statQualifications(){return "Wisdom 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Artisan.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become a Artisan.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(mob.freeWearPositions(Item.WIELD)>0)
				w.wearAt(Item.WIELD);
		}
	}
	public String weaponLimitations(){return "To avoid fumble chance, must use natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear cloth, or vegetation based armor to avoid skill failure.";}
	
	protected boolean isAllowedWeapon(int wclass){
		for(int i=0;i<allowedWeapons.length;i++)
			if(wclass==allowedWeapons[i]) return true;
		return false;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			boolean spellLike=((affect.tool()!=null)&&(myChar.fetchAbility(affect.tool().ID())!=null))&&(myChar.isMine(affect.tool()));
			if((spellLike||((affect.sourceMajor()&Affect.MASK_DELICATE)>0))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.INTELLIGENCE)*2))
				{
					String name="in <S-HIS-HER> maneuver";
					if(spellLike)
						name=affect.tool().name().toLowerCase();
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) "+name+"!");
					return false;
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				if(!isAllowedWeapon(classification))
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.WISDOM)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
			}
		}
		return super.okAffect(myChar,affect);
	}
	public String otherBonuses(){return "";}
}