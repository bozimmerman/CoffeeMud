package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead extends StdRace
{
	public String ID(){	return "Undead"; }
	public String name(){ return "Undead"; }
	protected int shortestMale(){return 64;}
	protected int shortestFemale(){return 60;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 100;}
	protected int weightVariance(){return 100;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
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
			return "^r" + mob.name() + "^r is massively broken and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has grown weak and slightly damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has lost stability and is very weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is in an somewhat unbalanced.^N";
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
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

