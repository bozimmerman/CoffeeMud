package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;

public class IndoorInTheAir extends StdRoom
{
	public String ID(){return "IndoorInTheAir";}
	public IndoorInTheAir()
	{
		super();
		name="the space";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		return InTheAir.isOkAirAffect(this,msg);
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		InTheAir.airAffects(this,msg);
	}

}
