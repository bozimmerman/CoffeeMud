package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Diviner extends SpecialistMage
{
	public String ID(){return "Diviner";}
	public String name(){return "Diviner";}
	public int domain(){return Ability.DOMAIN_DIVINATION;}
	public int opposed(){return Ability.DOMAIN_ILLUSION;}
	public boolean playerSelectable(){	return true;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
}
