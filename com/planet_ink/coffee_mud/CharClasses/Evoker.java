package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

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
}
