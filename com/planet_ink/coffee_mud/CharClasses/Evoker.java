package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Evoker extends SpecialistMage
{
	public String ID(){return "Evoker";}
	public String name(){return "Evoker";}
	public int domain(){return Ability.DOMAIN_EVOCATION;}
	public int opposed(){return Ability.DOMAIN_ALTERATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Evoker()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Spellcraft",false);
			CMAble.addCharAbilityMapping(ID(),9,"Spell_ContinualLight",25,true);
			CMAble.addCharAbilityMapping(ID(),11,"Spell_Shockshield",25,true);
			CMAble.addCharAbilityMapping(ID(),13,"Spell_IceLance",25,true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_Ignite",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_ForkedLightning",25,true);
			CMAble.addCharAbilityMapping(ID(),19,"Spell_Levitate",25,true);
			CMAble.addCharAbilityMapping(ID(),21,"Spell_IceStorm",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_Shove",25,true);
			CMAble.addCharAbilityMapping(ID(),30,"Spell_MassDisintegrate",25,true);
		}
	}
}
