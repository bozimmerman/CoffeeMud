package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Alterer extends SpecialistMage
{
	public String ID(){return "Alterer";}
	public String name(){return "Alterer";}
	public int domain(){return Ability.DOMAIN_ALTERATION;}
	public int opposed(){return Ability.DOMAIN_EVOCATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Alterer()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),13,"Spell_IncreaseGravity",25,true);
			CMAble.addCharAbilityMapping(ID(),17,"Spell_SlowProjectiles",25,true);
			CMAble.addCharAbilityMapping(ID(),20,"Spell_Timeport",25,true);
			CMAble.addCharAbilityMapping(ID(),22,"Spell_GravitySlam",25,true);
			CMAble.addCharAbilityMapping(ID(),24,"Spell_AlterSubstance",25,true);
			CMAble.addCharAbilityMapping(ID(),25,"Spell_Duplicate",25,true);
		}
	}
}
