package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Unique extends StdRace
{
	public String ID(){	return "Unique"; }
	public String name(){ return "Unique"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unique";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is pulsating in an unstable rage!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively and amazingly angry.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very angry.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat angry.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is very irritated.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is starting to show irritation.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is definitely serious and concerned.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is growing serious and concerned.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is definitely unamused and is starting to notice.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer amused, though still unconcerned.^N";
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
				("a "+name().toLowerCase()+" internal organ",EnvResource.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
