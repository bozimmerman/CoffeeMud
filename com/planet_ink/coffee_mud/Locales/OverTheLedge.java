package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;


public class OverTheLedge extends InTheAir
{
	public String ID(){return "OverTheLedge";}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(Sense.isSleeping(this))
			return true;

		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.amITarget(this))
		&&((getRoomInDir(Directions.DOWN)!=msg.source().location())))
			return true;
		return super.okMessage(myHost,msg);
	}

}
