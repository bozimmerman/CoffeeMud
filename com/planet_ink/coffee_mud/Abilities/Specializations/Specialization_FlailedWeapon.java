package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_FlailedWeapon extends Specialization_Weapon
{
	public String ID() { return "Specialization_FlailedWeapon"; }
	public String name(){ return "Flailing Weapon Specialization";}
	public Specialization_FlailedWeapon()
	{
		super();
		weaponType=Weapon.CLASS_FLAILED;
	}

	public Environmental newInstance(){	return new Specialization_FlailedWeapon();}
}
