package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Griffon extends GreatBird
{
	public Griffon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Griffon";
		// inches
		shortestMale=56;
		shortestFemale=59;
		heightVariance=12;
		// pounds
		lightestWeight=160;
		weightVariance=80;
		forbiddenWornBits=Item.HELD|Item.WIELD;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)	{}
}
