package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Abjurer extends SpecialistMage
{
	public String ID(){return "Abjurer";}
	public String name(){return "Abjurer";}
	public int domain(){return Ability.DOMAIN_ABJURATION;}
	public int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Abjurer()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_SongShield",25,true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_ResistBludgeoning",25,true);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Counterspell",25,true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ResistPiercing",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ChantShield",25,true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_ResistSlashing",25,true);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_PrayerShield",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_ResistIndignities",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_AchillesArmor",25,true);
		}
	}
}
