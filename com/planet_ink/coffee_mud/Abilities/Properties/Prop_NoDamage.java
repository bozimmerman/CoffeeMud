package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoDamage extends Property
{
	public String ID() { return "Prop_NoDamage"; }
	public String name(){ return "No Damage";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prop_NoDamage();}
	private boolean lastLevelChangers=true;

	public String accountForYourself()
	{ return "Harmless";	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(Util.bset(msg.targetCode(),Affect.MASK_HURT)
		&&(affected !=null)
		&&((msg.source()==affected)||(msg.tool()==affected)))
		{
			msg.modify(msg.source(),msg.target(),msg.tool(),
					   msg.sourceCode(),msg.sourceMessage(),
					   Affect.MASK_HURT,msg.targetMessage(),
					   msg.othersCode(),msg.othersMessage());
		}
		return true;
	}
}
