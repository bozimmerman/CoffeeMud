package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_SafePet extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_SafePet"; }
	public String name(){ return "Unattackable Pets";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_SafePet();}

	public String accountForYourself()
	{ return "Unattackable";	}

	public boolean okAffect(Affect affect)
	{
		if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)&&(affect.amITarget(affected))&&(affected!=null)&&(!disabled)))
		{
			affect.source().tell("Ah, leave "+affected.name()+" alone.");
			if(affected instanceof MOB)
				((MOB)affected).makePeace();
			return false;
		}
		else
		if((affected!=null)&&(affected instanceof MOB)&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))&&(affect.amISource((MOB)affected)))
			disabled=true;
		return super.okAffect(affect);
	}
}
