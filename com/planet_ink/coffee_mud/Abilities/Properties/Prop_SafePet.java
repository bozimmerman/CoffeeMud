package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_SafePet extends Property
{
	public Prop_SafePet()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Unattackable Pets";
	}

	public Environmental newInstance()
	{
		return new Prop_SafePet();
	}

	public String accountForYourself()
	{ return "Unattackable";	}

	public boolean okAffect(Affect affect)
	{
		if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)&&(affect.amITarget(affected))&&(affected!=null)))
		{
			affect.source().tell("Ah, leave "+affected.name()+" alone.");
			if(affected instanceof MOB)
				((MOB)affected).makePeace();
			return false;
		}
		return super.okAffect(affect);
	}
}
