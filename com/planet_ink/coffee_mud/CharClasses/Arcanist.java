package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Arcanist extends Thief
{
	public String ID(){return "Arcanist";}
	public String name(){return "Arcanist";}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int getBonusManaLevel(){return 18;}
	
	public Arcanist()
	{
		super();
		maxStat[CharStats.DEXTERITY]=22;
		maxStat[CharStats.INTELLIGENCE]=22;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Edged",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Alchemy",true);
			CMAble.addCharAbilityMapping(ID(),1,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Swipe",true);
			
			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",true);
			CMAble.addCharAbilityMapping(ID(),2,"Spell_Erase",false);
			
			CMAble.addCharAbilityMapping(ID(),3,"Skill_WandUse",25,true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_Appraise",false);
			
			CMAble.addCharAbilityMapping(ID(),4,"Spell_ClarifyScroll",true);
			CMAble.addCharAbilityMapping(ID(),4,"Thief_Sneak",false);
			
			CMAble.addCharAbilityMapping(ID(),5,"PaperMaking",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_Revoke",false);
			
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),6,"Thief_Pick",false);
			
CMAble.addCharAbilityMapping(ID(),7,"Skill_Spellcraft",true);
CMAble.addCharAbilityMapping(ID(),7,"Thief_IdentifyPoison",false);
		
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),8,"Thief_UsePoison",false);
			
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_RemoveTraps",false);
			
			CMAble.addCharAbilityMapping(ID(),10,"Spell_RechargeWand",true);
			
			CMAble.addCharAbilityMapping(ID(),11,"Thief_Lore",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Trip",false);
			
CMAble.addCharAbilityMapping(ID(),12,"Thief_RecordSpell",true);
			CMAble.addCharAbilityMapping(ID(),12,"Thief_Snatch",false);
			
CMAble.addCharAbilityMapping(ID(),13,"Spell_DisenchantWand",true);
			CMAble.addCharAbilityMapping(ID(),13,"Skill_Map",false);
			
			CMAble.addCharAbilityMapping(ID(),14,"Fighter_RapidShot",false);

CMAble.addCharAbilityMapping(ID(),15,"Thief_EnchantWand",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Bash",false);
			
CMAble.addCharAbilityMapping(ID(),16,"Spell_WardArea",true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_DetectInvisible",false);
			
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_Shadow",false);
			
			CMAble.addCharAbilityMapping(ID(),18,"Thief_Search",true);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Knock",false);
			
			CMAble.addCharAbilityMapping(ID(),19,"Thief_Distract",false);
			
			CMAble.addCharAbilityMapping(ID(),20,"Spell_EnchantArmor",true);
			
			CMAble.addCharAbilityMapping(ID(),21,"Thief_Observation",false);
			
			CMAble.addCharAbilityMapping(ID(),22,"Spell_EnchantWeapon",true);
			
			CMAble.addCharAbilityMapping(ID(),23,"Thief_Trap",true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_Charm",false);
			
			CMAble.addCharAbilityMapping(ID(),24,"Spell_Disenchant",true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_ComprehendLangs",false);
		
CMAble.addCharAbilityMapping(ID(),25,"Spell_SpellStoring",true);
			
CMAble.addCharAbilityMapping(ID(),30,"Spell_EnchantedItem",true);
		}
	}
	
	
	public String statQualifications(){return "Dexterity 9+, Intelligence 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become an Arcanist.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Intelligence to become an Arcanist.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String otherBonuses()
	{
		return "Magic resistance, 1%/level";
	}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MAGIC,affectableStats.getStat(CharStats.SAVE_MAGIC)+(affectableStats.getClassLevel(this)));
	}
}
