package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_EdgedWeapon extends Specialization_Weapon
{
	public String ID() { return "Specialization_EdgedWeapon"; }
	public String name(){ return "Edged Weapon Specialization";}
	public Specialization_EdgedWeapon()
	{
		super();
		weaponType=Weapon.CLASS_EDGED;
		secondWeaponType=Weapon.CLASS_DAGGER;
	}

}
