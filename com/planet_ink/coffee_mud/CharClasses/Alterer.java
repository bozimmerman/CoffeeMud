package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

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
}
