package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_BluntWeapon extends Specialization_Weapon
{
	public String ID() { return "Specialization_BluntWeapon"; }
	public String name(){ return "Blunt Weapon Specialization";}
	public Specialization_BluntWeapon()
	{
		super();
		weaponType=Weapon.CLASS_BLUNT;
		secondWeaponType=Weapon.CLASS_STAFF;
	}

	public Environmental newInstance(){	return new Specialization_BluntWeapon();}
}
