package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Util;
public class Impassable extends GenExit
{
	public String ID(){	return "Impassable";}
	public Environmental newInstance()
	{
		return new Impassable();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		MOB mob=affect.source();
		if((!affect.amITarget(this))&&(affect.tool()!=this))
			return true;
		else
		if(Util.bset(affect.targetMajor(),Affect.MASK_MOVE))
		{
			mob.tell("You can't go that way.");
			return false;
		}
		return true;
	}
}
