package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;

public class IndoorInTheAir extends StdRoom
{
	public IndoorInTheAir()
	{
		super();
		name="the space";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new IndoorInTheAir();
	}
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		return InTheAir.isOkAffect(this,affect);
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		InTheAir.affect(this,affect);
	}
	
}
