package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Thief extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	private static long wearMask=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;
	
	public Thief()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=16;
		maxStat[CharStats.DEXTERITY]=25;
		bonusPracLevel=1;
		manaMultiplier=12;
		attackAttribute=CharStats.DEXTERITY;
		bonusAttackLevel=1;
		damageBonusPerLevel=0;
		name=myID;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Peek",true);
			CMAble.addCharAbilityMapping(ID(),2,"Skill_Appraise",true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Hide",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",true);
			CMAble.addCharAbilityMapping(ID(),5,"Thief_Pick",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),7,"Thief_BackStab",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Steal",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_RemoveTraps",true);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Observation",true);
			CMAble.addCharAbilityMapping(ID(),11,"Thief_Listen",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Distract",true);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Search",true);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_Surrender",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_DetectInvisible",false);
			CMAble.addCharAbilityMapping(ID(),16,"Thief_Poison",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Knock",false);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_SilentLoot",true);
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Bribe",true);
			CMAble.addCharAbilityMapping(ID(),21,"Thief_SilentGold",true);
			CMAble.addCharAbilityMapping(ID(),22,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Charm",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)>8)
			return true;
		return false;
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public static boolean thiefOk(MOB myChar, Affect affect)
	{
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(affect.sourceMinor()==Affect.TYP_DELICATE_HANDS_ACT)
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if(I==null) break;
					if((((I.rawWornCode()&wearMask)>0)&&(I instanceof Armor))
					 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield)))
					{
						switch(I.material()&EnvResource.MATERIAL_MASK)
						{
						case EnvResource.MATERIAL_CLOTH:
						case EnvResource.MATERIAL_LEATHER:
						case EnvResource.MATERIAL_FLESH:
						case EnvResource.MATERIAL_VEGETATION:
							break;
						default:
							if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> armor make(s) <S-HIM-HER> fumble(s) in <S-HIS-HER> maneuver!");
								return false;
							}
							break;
						}
					}
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon))
			{
				int classification=((Weapon)affect.tool()).weaponClassification();
				switch(classification)
				{
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_DAGGER:
					break;
				default:
					if(Dice.rollPercentage()>(myChar.charStats().getStat(CharStats.DEXTERITY)*2))
					{
						myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
						return false;
					}
					break;
				}
			}
		}
		return true;
	}
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, ranged, thrown, natural, or dagger-like weapon.";}
	public String armorLimitations(){return "Must wear leather, cloth, or vegetation based armor to avoid skill failure.";}
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!thiefOk(myChar,affect))
			return false;
		return super.okAffect(myChar,affect);
	}

	public void unLevel(MOB mob)
	{
		if(mob.envStats().level()<2)
			return;
		super.unLevel(mob);

		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		attArmor=attArmor*-1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level after 1st.";}
	public void level(MOB mob)
	{
		super.level(mob);
		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^BYour stealthiness grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
}
