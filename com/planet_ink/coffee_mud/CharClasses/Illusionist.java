package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Illusionist extends SpecialistMage
{
	public String ID(){return "Illusionist";}
	public String name(){return "Illusionist";}
	public static int domain(){return Ability.DOMAIN_ILLUSION;}
	public static int opposed(){return Ability.DOMAIN_DIVINATION;}
	public boolean playerSelectable(){	return true;}
}
