package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WoodGolem extends StdRace
{
	protected static Vector resources=new Vector();
	public WoodGolem()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=64;
		shortestFemale=60;
		heightVariance=12;
		// pounds
		lightestWeight=400;
		weightVariance=100;
		forbiddenWornBits=0;
	}
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively splintered and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley splintered and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very splintered and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is splintered and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is splintered and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is showing large splinters.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some splinters.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small splinters.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("a pound of oak",EnvResource.RESOURCE_OAK));
			}
		}
		return resources;
	}
}

