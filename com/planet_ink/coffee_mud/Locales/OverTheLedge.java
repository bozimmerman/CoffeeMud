package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;


public class OverTheLedge extends InTheAir
{
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		
		if(Sense.isSleeping(this)) return true;
		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.amITarget(this))
		&&(getRoomInDir(Directions.DOWN)!=affect.source().location()))
			return true;
		return isOkAffect(this,affect);
	}
	public Environmental newInstance()
	{
		return new OverTheLedge();
	}
}
