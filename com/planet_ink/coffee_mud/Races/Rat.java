package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;

public class Rat extends Rodent
{
	public Rat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rat";
		// inches
		shortestMale=6;
		shortestFemale=6;
		heightVariance=6;
		// pounds
		lightestWeight=10;
		weightVariance=10;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,4);
	}
}
