package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;

public class Enchanter extends SpecialistMage
{
	public String ID(){return "Enchanter";}
	public String name(){return "Enchanter";}
	public static int domain(){return Ability.DOMAIN_ENCHANTMENT;}
	public static int opposed(){return Ability.DOMAIN_ABJURATION;}
	public boolean playerSelectable(){	return true;}
}
