package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;


public class ClimbableLedge extends ClimbableSurface
{
	public String ID(){return "ClimbableLedge";}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(Sense.isSleeping(this))
			return super.okMessage(myHost,msg);

		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.amITarget(this)))
		{
			Rideable ladder=findALadder(msg.source(),this);
			if(ladder!=null)
			{
				msg.source().setRiding(ladder);
				msg.source().recoverEnvStats();
			}
			if((getRoomInDir(Directions.DOWN)!=msg.source().location()))
				return true;
		}
		return super.okMessage(myHost,msg);
	}
	public Environmental newInstance()
	{
		return new ClimbableLedge();
	}
}
