package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

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
}
