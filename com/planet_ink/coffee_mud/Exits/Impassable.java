package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Util;
public class Impassable extends GenExit
{
	public Impassable()
	{
		super();
		name="a blocked way";
		description="It doesn't look like you can go that way.";
	}
	public String ID(){	return "Impassable";}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if((!msg.amITarget(this))&&(msg.tool()!=this))
			return true;
		else
		if(Util.bset(msg.targetMajor(),CMMsg.MASK_MOVE))
		{
			mob.tell("You can't go that way.");
			return false;
		}
		return true;
	}
}
