package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Enchanter extends SpecialistMage
{
	public String ID(){return "Enchanter";}
	public String name(){return "Enchanter";}
	public int domain(){return Ability.DOMAIN_ENCHANTMENT;}
	public int opposed(){return Ability.DOMAIN_ABJURATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Enchanter()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),7,"Spell_ManaBurn",25,true);
			CMAble.addCharAbilityMapping(ID(),14,"Spell_Alarm",25,true);
			CMAble.addCharAbilityMapping(ID(),16,"Spell_MindFog",25,true);
			CMAble.addCharAbilityMapping(ID(),18,"Spell_Enthrall",25,true);
			CMAble.addCharAbilityMapping(ID(),23,"Spell_LowerResists",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_Geas",25,true);
		}
	}
}
