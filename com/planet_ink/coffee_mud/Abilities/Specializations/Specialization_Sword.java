package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Sword extends Specialization_Weapon
{
	public Specialization_Sword()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sword Specialization";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);
		weaponType=Weapon.CLASS_SWORD;

		addQualifyingClass("Fighter",1);
		addQualifyingClass("Ranger",1);
		addQualifyingClass("Paladin",1);
		addQualifyingClass("Thief",1);
		addQualifyingClass("Bard",1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Sword();
	}
}
