package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Sword extends Specialization_Weapon
{
	public String ID() { return "Specialization_Sword"; }
	public String name(){ return "Sword Specialization";}
	public Specialization_Sword()
	{
		super();
		weaponType=Weapon.CLASS_SWORD;
	}

	public Environmental newInstance(){	return new Specialization_Sword();	}
}
