package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Doll extends StdRace
{
	public String ID(){	return "Doll"; }
	public String name(){ return "Doll"; }
	protected int shortestMale(){return 6;}
	protected int shortestFemale(){return 6;}
	protected int heightVariance(){return 3;}
	protected int lightestWeight(){return 10;}
	protected int weightVariance(){return 20;}
	protected long forbiddenWornBits(){return 0;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,5);
		affectableStats.setStat(CharStats.DEXTERITY,5);
		affectableStats.setStat(CharStats.INTELLIGENCE,13);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is nearly disassembled!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in tears and cracks.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is broken badly with lots of tears.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous tears and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some tears and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few cracks.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is scratched heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor scratches.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is a bit disheveled.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" clothes",EnvResource.RESOURCE_COTTON));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" parts",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
