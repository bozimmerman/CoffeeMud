package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Monk extends StdCharClass
{
	public String ID(){return "Monk";}
	public String name(){return "Monk";}
	public String baseClass(){return "Fighter";}
	public int getMaxHitPointsLevel(){return 24;}
	public int getBonusPracLevel(){return -1;}
	public int getBonusManaLevel(){return 8;}
	public int getBonusAttackLevel(){return 2;}
	public int getAttackAttribute(){return CharStats.STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 1;}
	public int getPracsFirstLevel(){return 3;}
	public int getTrainsFirstLevel(){return 4;}
	private static boolean abilitiesLoaded=false;
	
	public Monk()
	{
		super();
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.DEXTERITY]=22;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Fighter_Kick",true);
			CMAble.addCharAbilityMapping(ID(),1,"Monk_MonkeyPunch",false);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),2,"Skill_Climb",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),5,"Fighter_Rescue",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),7,"Monk_DeflectProjectile",true);
			CMAble.addCharAbilityMapping(ID(),7,"Monk_KnifeHand",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),8,"Monk_AxeKick",false);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true); 
			CMAble.addCharAbilityMapping(ID(),10,"Monk_BodyFlip",false);
			CMAble.addCharAbilityMapping(ID(),11,"Fighter_BlindFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Monk_CatchProjectile",true);
			CMAble.addCharAbilityMapping(ID(),12,"Monk_FlyingKick",false);
			CMAble.addCharAbilityMapping(ID(),13,"Fighter_WeaponBreak",false);
			CMAble.addCharAbilityMapping(ID(),13,"Monk_Pin",false);
			CMAble.addCharAbilityMapping(ID(),14,"Skill_Dirt",false);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Search",false);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_Sweep",false);
			CMAble.addCharAbilityMapping(ID(),15,"Monk_Cartwheel",true);
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
			CMAble.addCharAbilityMapping(ID(),17,"Monk_CircleParry",true);
			CMAble.addCharAbilityMapping(ID(),17,"Monk_KiStrike",false);
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Snatch",false);
			CMAble.addCharAbilityMapping(ID(),19,"Fighter_Tumble",true);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_AttackHalf",true);
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_Endurance",false);
			CMAble.addCharAbilityMapping(ID(),22,"Monk_CircleTrip",true);
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Listen",false);
			CMAble.addCharAbilityMapping(ID(),24,"Monk_LightningStrike",false);
			CMAble.addCharAbilityMapping(ID(),25,"Monk_ReturnProjectile",false);
			CMAble.addCharAbilityMapping(ID(),30,"Monk_AtemiStrike",false);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public String statQualifications(){return "Strength 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STRENGTH)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Strength to become a Monk.");
			return false;
		}

		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Monk.");
			return false;
		}

		return super.qualifiesForThisClass(mob,quiet);
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

	public boolean anyWeapons(MOB mob)
	{
		if(mob.fetchWieldedItem()!=null) return false;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(I.amWearingAt(Item.HELD)))
				return false;
		}
		return true;
	}
	
	public String otherBonuses(){return "Receives (Dexterity/9)+1 bonus to defense every level.  Receives 2%/lvl unarmed attack bonus.  Receives bonus attack when unarmed.  Has Slow Fall ability.  Receives 2%/level trap avoidance.";}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isSleeping(affected)||Sense.isSitting(affected))
			affectableStats.setArmor(affectableStats.armor()+(100-affected.baseEnvStats().armor()));
		else
		if((affected instanceof MOB)&&(!anyWeapons((MOB)affected)))
		{
			affectableStats.setSpeed(affectableStats.speed()+1.0);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((MOB)affected).charStats().getClassLevel(this));
			if(affected.fetchAffect("Falling")!=null)
				affectableStats.setWeight(0);
		}
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,
			affectableStats.getStat(CharStats.SAVE_MIND)
			+(affectableStats.getClassLevel(this)*2));
	}
	public void level(MOB mob)
	{
		super.level(mob);
		int attArmor=((int)Math.round(Util.div(mob.charStats().getStat(CharStats.DEXTERITY),9.0)))+1;
		mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-attArmor);
		mob.envStats().setArmor(mob.envStats().armor()-attArmor);
		mob.tell("^NYour stealthiness grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	}
	
	public void outfit(MOB mob)
	{
	}
	
	public String weaponLimitations(){return "To avoid fumble chance, must use dagger, staff, or natural weapon.";}
	public String armorLimitations(){return "Must wear cloth, vegetation, or paper based armor to avoid spell failure.";}
	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(affect.amITarget(myChar)
		   &&((affect.targetMajor()&Affect.MASK_MALICIOUS)>0)
		   &&(affect.tool()!=null)
		   &&(affect.tool() instanceof Trap))
		{
			if(Dice.rollPercentage()<(2*myChar.charStats().getClassLevel(this)))
			{
				myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> avoid(s) the trap!");
				return false;
			}
		}
		else
		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if((affect.tool()!=null)
			&&(affect.tool() instanceof Ability)
			&&(myChar.isMine(affect.tool())))
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if(I==null) break;
					if((!I.amWearingAt(Item.INVENTORY))
					&&((I instanceof Armor)||(I instanceof Shield)))
					{
						switch(I.material()&EnvResource.MATERIAL_MASK)
						{
						case EnvResource.MATERIAL_CLOTH:
						case EnvResource.MATERIAL_UNKNOWN:
						case EnvResource.MATERIAL_VEGETATION:
						case EnvResource.MATERIAL_PAPER:
							break;
						default:
							if((Dice.rollPercentage()>myChar.charStats().getStat(CharStats.DEXTERITY)*2)
							&&(I.rawProperLocationBitmap()!=(Item.ON_RIGHT_FINGER|Item.ON_LEFT_FINGER))
							&&(I.rawProperLocationBitmap()!=Item.ON_EARS)
							&&(I.rawProperLocationBitmap()!=(Item.ON_EARS|Item.HELD))
							&&(I.rawProperLocationBitmap()!=Item.ON_EYES)
							&&(I.rawProperLocationBitmap()!=(Item.ON_EYES|Item.HELD))
							&&(I.rawProperLocationBitmap()!=Item.ON_NECK)
							&&(I.rawProperLocationBitmap()!=(Item.ON_NECK|Item.HELD))
							&&(I.rawProperLocationBitmap()!=(Item.ON_RIGHT_FINGER|Item.ON_LEFT_FINGER|Item.HELD)))
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> fumble(s) <S-HIS-HER> "+affect.tool().name()+" attempt due to <S-HIS-HER> armor!");
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
			}
		}
		return super.okAffect(myChar,affect);
	}
}
