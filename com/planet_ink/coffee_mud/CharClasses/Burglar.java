package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Burglar extends Thief
{
	public String ID(){return "Burglar";}
	public String name(){return "Burglar";}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded2=false;
	public boolean loaded(){return abilitiesLoaded2;}
	public void setLoaded(boolean truefalse){abilitiesLoaded2=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_BURGLAR;}
	
	public Burglar()
	{
		super();
		maxStatAdj[CharStats.DEXTERITY]=4;
		maxStatAdj[CharStats.CHARISMA]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"ThievesCant",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Appraise",true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Palm",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),4,"Fighter_Intimidate",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"Thief_TagTurf",false);
			CMAble.addCharAbilityMapping(ID(),5,"Thief_DetectTraps",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);
			
			CMAble.addCharAbilityMapping(ID(),7,"Thief_Peek",true);
			CMAble.addCharAbilityMapping(ID(),7,"Thief_Observation",false);
			
			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Forgery",false);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Listen",true);
			
			CMAble.addCharAbilityMapping(ID(),10,"Thief_ImprovedHiding",false);
			CMAble.addCharAbilityMapping(ID(),10,"Thief_BackStab",false);
			
			CMAble.addCharAbilityMapping(ID(),11,"Thief_Steal",true);
			
			CMAble.addCharAbilityMapping(ID(),12,"Thief_SlipItem",false);
			CMAble.addCharAbilityMapping(ID(),12,"Thief_ImprovedPeek",false);
			
			CMAble.addCharAbilityMapping(ID(),13,"Thief_PlantItem",false);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Detection",true);
			
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Bribe",false);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_ImprovedSwipe",false);
			
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);
			CMAble.addCharAbilityMapping(ID(),15,"Thief_SilentGold",true);
			
			CMAble.addCharAbilityMapping(ID(),16,"Thief_Safecracking",false);
			CMAble.addCharAbilityMapping(ID(),16,"Thief_SilentLoot",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),17,"Fighter_BlindFighting",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Robbery",false);
			CMAble.addCharAbilityMapping(ID(),18,"Skill_Map",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Thief_SenseLaw",true);
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Mug",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Thief_Lore",false);
			
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Racketeer",false);
			CMAble.addCharAbilityMapping(ID(),21,"Thief_StripItem",true);
			
			CMAble.addCharAbilityMapping(ID(),22,"Thief_UsePoison",false);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_ImprovedSteal",false);
			
			CMAble.addCharAbilityMapping(ID(),23,"Spell_AnalyzeDweomer",false);
			CMAble.addCharAbilityMapping(ID(),23,"Fighter_Tumble",true);
			
			CMAble.addCharAbilityMapping(ID(),24,"Thief_Con",false);
			CMAble.addCharAbilityMapping(ID(),24,"Thief_Comprehension",false);

			CMAble.addCharAbilityMapping(ID(),25,"Thief_Embezzle",true);
			
			CMAble.addCharAbilityMapping(ID(),30,"Thief_ContractHit",true);
		}
	}
	public String statQualifications(){return "Dexterity 9+ Charisma 9+";}
	public String weaponLimitations(){return "To avoid fumble chance, must be sword, flailed, blunt, natural, or dagger-like weapon.";}
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
