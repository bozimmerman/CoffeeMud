package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Thief extends StdCharClass
{
	public String ID(){return "Thief";}
	public String name(){return "Thief";}
	public String baseClass(){return "Thief";}
	public int getMaxHitPointsLevel(){return 16;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusManaLevel(){return 12;}
	public int getBonusAttackLevel(){return 1;}
	public int getAttackAttribute(){return CharStats.DEXTERITY;}
	public int getLevelsPerBonusDamage(){ return 5;}
	public int allowedArmorLevel(){return CharClass.ARMOR_LEATHER;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	protected static int[] allowedWeapons={
				Weapon.CLASS_SWORD,
				Weapon.CLASS_RANGED,
				Weapon.CLASS_THROWN,
				Weapon.CLASS_NATURAL,
				Weapon.CLASS_DAGGER};
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};


	public Thief()
	{
		super();
		maxStatAdj[CharStats.DEXTERITY]=7;
		if(ID().equals(baseClass())&&(!loaded()))
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"ThievesCant",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,false);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",false);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_SneakAttack",false);

			CMAble.addCharAbilityMapping(ID(),3,"Thief_Countertracking",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",true);

			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Autosneak",false);

			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dirt",false);

			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);

			CMAble.addCharAbilityMapping(ID(),7,"Thief_Peek",true);
			CMAble.addCharAbilityMapping(ID(),7,"Thief_UsePoison",false);

			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);

			CMAble.addCharAbilityMapping(ID(),9,"Thief_Observation",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",false);

			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Haggle",false);

			CMAble.addCharAbilityMapping(ID(),11,"Thief_Steal",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",false);

			CMAble.addCharAbilityMapping(ID(),12,"Thief_Listen",false);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Thief_Graffiti",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_Detection",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Bind",false);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Arsonry",false);

			CMAble.addCharAbilityMapping(ID(),14,"Thief_Surrender",false);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);

			CMAble.addCharAbilityMapping(ID(),15,"Thief_Snatch",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);

			CMAble.addCharAbilityMapping(ID(),16,"Thief_SilentGold",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);

			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_CarefulStep",true);

			CMAble.addCharAbilityMapping(ID(),18,"Thief_SilentLoot",false);
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Comprehension",false);

			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",true);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Snatch",false);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Ventrilloquate",false);

			CMAble.addCharAbilityMapping(ID(),20,"Thief_Lore",false);
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Alertness",false);

			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sap",true);
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Panhandling",true);

			CMAble.addCharAbilityMapping(ID(),22,"Thief_Flank",false);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_ImprovedDistraction",false);

			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",false);
			CMAble.addCharAbilityMapping(ID(),23,"Skill_Warrants",true);

			CMAble.addCharAbilityMapping(ID(),24,"Thief_Bribe",false);
			CMAble.addCharAbilityMapping(ID(),24,"Skill_EscapeBonds",false);

			CMAble.addCharAbilityMapping(ID(),25,"Thief_Ambush",true);
			CMAble.addCharAbilityMapping(ID(),25,"Thief_Squatting",false);

			CMAble.addCharAbilityMapping(ID(),30,"Thief_Nondetection",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Thief.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear leather, cloth, or vegetation based armor to avoid skill failure.";}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(myHost instanceof MOB)
		{
			MOB myChar=(MOB)myHost;
			if(msg.amISource(myChar)
			   &&(!myChar.isMonster())
			   &&(msg.sourceCode()==CMMsg.MSG_THIEF_ACT)
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof MOB)
			   &&(msg.targetMessage()==null)
			   &&(msg.tool()!=null)
			   &&(msg.tool() instanceof Ability)
			   &&(msg.tool().ID().equals("Thief_Steal")
				  ||msg.tool().ID().equals("Thief_Robbery")
				  ||msg.tool().ID().equals("Thief_Mug")
				  ||msg.tool().ID().equals("Thief_Swipe")))
				MUDFight.postExperience(myChar,(MOB)msg.target()," for a successful "+msg.tool().name(),10,false);
		}
		super.executeMsg(myHost,msg);
	}

	protected boolean isAllowedWeapon(int wclass){
		for(int i=0;i<allowedWeapons.length;i++)
			if(wclass==allowedWeapons[i]) return true;
		return false;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)&&(!myChar.isMonster()))
		{
			boolean spellLike=((msg.tool()!=null)
							   &&((CMAble.getQualifyingLevel(ID(),true,msg.tool().ID())>0))
							   &&(myChar.isMine(msg.tool())));
			if((spellLike||((msg.sourceMajor()&CMMsg.MASK_DELICATE)>0))
			&&(!armorCheck(myChar)))
			{
				if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
				{
					String name="in <S-HIS-HER> maneuver";
					if(spellLike)
						name=msg.tool().name().toLowerCase();
					myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) "+name+"!");
					return false;
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			{
				int classification=((Weapon)msg.tool()).weaponClassification();
				if(!isAllowedWeapon(classification))
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+msg.tool().name()+".");
						return false;
					}
			}
		}
		return super.okMessage(myChar,msg);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);

		int dexStat=mob.charStats().getStat(CharStats.DEXTERITY);
		int maxDexStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.DEXTERITY));
		if(dexStat>maxDexStat) dexStat=maxDexStat;
		int attArmor=((int)Math.round(Util.div(dexStat,9.0)))+1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level after 1st.  Bonus experience for using certain skills.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isSleeping(affected)||Sense.isSitting(affected))
			affectableStats.setArmor(affectableStats.armor()+(100-affected.baseEnvStats().armor()));
	}
	public void level(MOB mob)
	{
		super.level(mob);
		int dexStat=mob.charStats().getStat(CharStats.DEXTERITY);
		int maxDexStat=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
					 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+CharStats.DEXTERITY));
		if(dexStat>maxDexStat) dexStat=maxDexStat;
		int attArmor=((int)Math.round(Util.div(dexStat,9.0)))+1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^NYour stealthiness grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}
