package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Axe extends Specialization_Weapon
{
	public String ID() { return "Specialization_Axe"; }
	public String name(){ return "Axe Specialization";}
	public Specialization_Axe()
	{
		super();
		weaponType=Weapon.CLASS_AXE;
	}

}
