package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TreeGolem extends StdRace
{
	public String ID(){	return "TreeGolem"; }
	public String name(){ return "Tree Golem"; }
	protected int shortestMale(){return 64;}
	protected int shortestFemale(){return 60;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 400;}
	protected int weightVariance(){return 100;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Vegetation";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,8 ,8 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public String arriveStr()
	{
		return "slides in";
	}
	public String leaveStr()
	{
		return "slides";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a jagged limb");
			naturalWeapon.setRanges(0,2);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB)
		&&(affect.amISource((MOB)myHost)))
		{
			if(affect.targetMinor()==Affect.TYP_LEAVE)
			{
				affect.source().tell("You can't really go anywhere -- you are rooted!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.GENDER,(int)'N');
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
			return "^r" + mob.name() + "^r is massively splintered and broken.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley splintered and broken.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very splintered and broken.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is splintered and broken.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is splintered and slightly broken.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has lost lots of leaves.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has lost some more leaves.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has lost a few leaves.^N";
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
					("a pound of wood",EnvResource.RESOURCE_WOOD));
				resources.addElement(makeResource
					("a pound of leaves",EnvResource.RESOURCE_GREENS));
			}
		}
		return resources;
	}
}
