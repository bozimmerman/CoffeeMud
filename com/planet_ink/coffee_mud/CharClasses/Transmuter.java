package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Transmuter extends SpecialistMage
{
	public String ID(){return "Transmuter";}
	public String name(){return "Transmuter";}
	public static int domain(){return Ability.DOMAIN_TRANSMUTATION;}
	public static int opposed(){return Ability.DOMAIN_CONJURATION;}
	public boolean playerSelectable(){	return true;}
}
