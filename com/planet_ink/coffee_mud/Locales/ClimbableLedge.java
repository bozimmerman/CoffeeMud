package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;


public class ClimbableLedge extends ClimbableSurface
{
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(Sense.isSleeping(this))
			return super.okAffect(myHost,affect);

		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.amITarget(this)))
		{
			Rideable ladder=findALadder(affect.source(),this);
			if(ladder!=null)
			{
				affect.source().setRiding(ladder);
				affect.source().recoverEnvStats();
			}
			if((getRoomInDir(Directions.DOWN)!=affect.source().location()))
				return true;
		}
		return super.okAffect(myHost,affect);
	}
	public Environmental newInstance()
	{
		return new ClimbableLedge();
	}
}
