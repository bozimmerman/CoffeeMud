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

	public String accountForYourself()
	{ return "Unattackable";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)&&(msg.amITarget(affected))&&(affected!=null)&&(!disabled)))
		{
			msg.source().tell("Ah, leave "+affected.name()+" alone.");
			if(affected instanceof MOB)
				((MOB)affected).makePeace();
			return false;
		}
		else
		if((affected!=null)&&(affected instanceof MOB)&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))&&(msg.amISource((MOB)affected)))
			disabled=true;
		return super.okMessage(myHost,msg);
	}
}
