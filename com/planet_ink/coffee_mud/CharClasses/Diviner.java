package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Diviner extends SpecialistMage
{
	public String ID(){return "Diviner";}
	public String name(){return "Diviner";}
	public static int domain(){return Ability.DOMAIN_DIVINATION;}
	public static int opposed(){return Ability.DOMAIN_ILLUSION;}
	public boolean playerSelectable(){	return true;}
}
