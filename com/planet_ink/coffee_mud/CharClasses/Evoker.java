package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Evoker extends SpecialistMage
{
	public String ID(){return "Evoker";}
	public String name(){return "Evoker";}
	public static int domain(){return Ability.DOMAIN_EVOCATION;}
	public static int opposed(){return Ability.DOMAIN_ALTERATION;}
	public boolean playerSelectable(){	return true;}
}
