package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_FlailedWeapon extends Specialization_Weapon
{
	public Specialization_FlailedWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Flailing Weapon Specialization";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);
		weaponType=Weapon.CLASS_FLAILED;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_FlailedWeapon();
	}
}
