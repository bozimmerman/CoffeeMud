package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Conjurer extends SpecialistMage
{
	public String ID(){return "Conjurer";}
	public String name(){return "Conjurer";}
	public static int domain(){return Ability.DOMAIN_CONJURATION;}
	public static int opposed(){return Ability.DOMAIN_TRANSMUTATION;}
	public boolean playerSelectable(){	return true;}
}
