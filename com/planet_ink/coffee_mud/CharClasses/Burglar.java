package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Burglar extends Thief
{
	public String ID(){return "Burglar";}
	public String name(){return "Burglar";}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	
	public Burglar()
	{
		super();
		maxStat[CharStats.DEXTERITY]=22;
		maxStat[CharStats.CHARISMA]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Edged",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			CMAble.addCharAbilityMapping(ID(),1,"Taxidermy",50,true);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Caltrops",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",true);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_SneakAttack",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Appraise",false);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",true);
			
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",true);
			
			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dirt",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",true);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Thief_Peek",true);
CMAble.addCharAbilityMapping(ID(),7,"Thief_UsePoison",true);
		
			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Observation",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",true);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_Haggle",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Thief_Steal",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Thief_Listen",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_Search",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Bind",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Surrender",true);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Thief_Snatch",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);
			
			CMAble.addCharAbilityMapping(ID(),16,"Thief_SilentGold",false);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",true);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
			
			CMAble.addCharAbilityMapping(ID(),18,"Thief_SilentLoot",false);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_ComprehendLangs",false);
		
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",true);
			
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Lore",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Sap",true);
CMAble.addCharAbilityMapping(ID(),21,"Thief_Caltrops",true);
			
CMAble.addCharAbilityMapping(ID(),22,"Thief_Flank",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Charm",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Thief_Bribe",true);
			
CMAble.addCharAbilityMapping(ID(),25,"Thief_Ambush",false);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Ventrilloquate",false);
CMAble.addCharAbilityMapping(ID(),30,"Thief_Nondetection",false);
		}
	}
	public String statQualifications(){return "Dexterity 9+ Charisma 9+";}
	protected static int[] allowedWeapons={ 
				Weapon.CLASS_SWORD,
				Weapon.CLASS_BLUNT,
				Weapon.CLASS_FLAILED,
				Weapon.CLASS_NATURAL,
				Weapon.CLASS_DAGGER};
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, flailed, blunt, natural, or dagger-like weapon.";}
	protected boolean isAllowedWeapon(int wclass){
		for(int i=0;i<allowedWeapons.length;i++)
			if(wclass==allowedWeapons[i]) return true;
		return false;
	}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Burglar.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Burglar.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
}
