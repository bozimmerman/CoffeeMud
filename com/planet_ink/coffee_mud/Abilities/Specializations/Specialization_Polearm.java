package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Polearm extends Specialization_Weapon
{
	public String ID() { return "Specialization_Polearm"; }
	public String name(){ return "Polearm Specialization";}
	public Specialization_Polearm()
	{
		super();
		weaponType=Weapon.CLASS_POLEARM;
	}

	public Environmental newInstance(){	return new Specialization_Polearm();	}
}
