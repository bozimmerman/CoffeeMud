package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Illusionist extends SpecialistMage
{
	public String ID(){return "Illusionist";}
	public String name(){return "Illusionist";}
	public int domain(){return Ability.DOMAIN_ILLUSION;}
	public int opposed(){return Ability.DOMAIN_DIVINATION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	private static boolean myAbilitiesLoaded=false;
	
	public Illusionist()
	{
		super();
		if(!myAbilitiesLoaded)
		{
			myAbilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"",25,true);
		}
	}
}
