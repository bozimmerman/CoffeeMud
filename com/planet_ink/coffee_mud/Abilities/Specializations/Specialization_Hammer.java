package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Hammer extends Specialization_Weapon
{
	public Specialization_Hammer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hammer Specialization";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		weaponType=Weapon.CLASS_HAMMER;

		baseEnvStats().setLevel(1);

		addQualifyingClass("Fighter",1);
		addQualifyingClass("Ranger",1);
		addQualifyingClass("Paladin",1);
		addQualifyingClass("Cleric",1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Hammer();
	}
}
