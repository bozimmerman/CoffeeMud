package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Transmuter extends SpecialistMage
{
	public String ID(){return "Transmuter";}
	public String name(){return "Transmuter";}
	public int domain(){return Ability.DOMAIN_TRANSMUTATION;}
	public int opposed(){return Ability.DOMAIN_CONJURATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Transmuter()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),3,"Spell_CauseStink",25,true);
			CMAble.addCharAbilityMapping(ID(),3,"Spell_CauseStink",25,true);
		}
	}
}
