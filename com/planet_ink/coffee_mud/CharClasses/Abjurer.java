package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Abjurer extends SpecialistMage
{
	public String ID(){return "Abjurer";}
	public String name(){return "Abjurer";}
	public static int domain(){return Ability.DOMAIN_ABJURATION;}
	public static int opposed(){return Ability.DOMAIN_ENCHANTMENT;}
	public boolean playerSelectable(){	return true;}
}
