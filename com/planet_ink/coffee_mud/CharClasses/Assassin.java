package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Assassin extends Thief
{
	public String ID(){return "Assassin";}
	public String name(){return "Assassin";}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded2=false;
	public boolean loaded(){return abilitiesLoaded2;}
	public void setLoaded(boolean truefalse){abilitiesLoaded2=truefalse;};
	
	public String weaponLimitations(){return "";}
	protected boolean isAllowedWeapon(int wclass){ return true;}
	
	public String statQualifications(){return "Dexterity 9+ Wisdom 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become an Assassin.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become an Assassin.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherBonuses()
	{
		return "Strong resistance to all poisons at 21st level.";
	}
	public Assassin()
	{
		super();
		maxStat[CharStats.DEXTERITY]=25;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_KillLog",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",true);
			
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Mark",true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_SneakAttack",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",true);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_IdentifyPoison",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",false);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Thief_MarkInvisibility",true);
			CMAble.addCharAbilityMapping(ID(),7,"Specialization_Natural",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_Shadow",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),9,"Specialization_FlailedWeapon",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Fighter_CritStrike",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",false);
			
			CMAble.addCharAbilityMapping(ID(),12,"Thief_UsePoison",true);
			CMAble.addCharAbilityMapping(ID(),12,"Skill_TwoWeaponFighting",false);
			CMAble.addCharAbilityMapping(ID(),12,"Specialization_BluntWeapon",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_AnalyzeMark",true);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Observation",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Assassinate",true);
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),15,"Fighter_TrueShot",false);
			CMAble.addCharAbilityMapping(ID(),15,"Specialization_Axe",false);
			
			CMAble.addCharAbilityMapping(ID(),16,"Fighter_DualParry",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_CriticalShot",true);
			CMAble.addCharAbilityMapping(ID(),17,"Specialization_Hammer",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Peek",false);
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Sap",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",true);
			CMAble.addCharAbilityMapping(ID(),19,"Specialization_Polearm",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Trap",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Fighter_AxKick",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Thief_Ambush",true);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_Flank",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Thief_FrameMark",true);
			CMAble.addCharAbilityMapping(ID(),23,"Fighter_Cleave",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Fighter_Tumble",false);
			
			CMAble.addCharAbilityMapping(ID(),25,"Skill_AttackHalf",true);
			CMAble.addCharAbilityMapping(ID(),25,"Fighter_CalledShot",false);
			
			CMAble.addCharAbilityMapping(ID(),30,"Thief_Shadowstrike",true);
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affectableStats.getClassLevel(this)>=21)
			affectableStats.setStat(CharStats.SAVE_POISON,200);
	}
}
