package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Hammer extends Specialization_Weapon
{
	public String ID() { return "Specialization_Hammer"; }
	public String name(){ return "Hammer Specialization";}
	public Specialization_Hammer()
	{
		super();
		weaponType=Weapon.CLASS_HAMMER;
	}

}
