package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Ranged extends Specialization_Weapon
{
	public Specialization_Ranged()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Ranged Weapon Specialization";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);
		weaponType=Weapon.CLASS_RANGED;
		secondWeaponType=Weapon.CLASS_THROWN;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Ranged();
	}
}
