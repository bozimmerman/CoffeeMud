package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Trashcan extends Property
{
	public String ID() { return "Prop_Trashcan"; }
	public String name(){ return "Auto purges items put into a container";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Prop_Trashcan();}
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected instanceof Item)
		&&(msg.targetMinor()==Affect.TYP_PUT)
		&&(msg.amITarget(affected))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
			((Item)msg.tool()).destroy();
		else
		if((affected instanceof Room)
		&&(msg.targetMinor()==Affect.TYP_DROP)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item))
			((Item)msg.target()).destroy();
	}
}