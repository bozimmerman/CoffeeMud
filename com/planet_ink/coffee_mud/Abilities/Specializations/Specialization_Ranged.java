package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Ranged extends Specialization_Weapon
{
	public String ID() { return "Specialization_Ranged"; }
	public String name(){ return "Ranged Weapon Specialization";}
	public Specialization_Ranged()
	{
		super();
		weaponType=Weapon.CLASS_RANGED;
		secondWeaponType=Weapon.CLASS_THROWN;
	}

	public Environmental newInstance(){	return new Specialization_Ranged();	}
}
