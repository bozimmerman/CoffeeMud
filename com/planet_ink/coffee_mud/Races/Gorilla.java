package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gorilla extends Monkey
{
	public Gorilla()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gorilla";
		// inches
		shortestMale=62;
		shortestFemale=60;
		heightVariance=12;
		// pounds
		lightestWeight=220;
		weightVariance=80;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,18);
		affectableStats.setStat(CharStats.DEXTERITY,16);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
}
