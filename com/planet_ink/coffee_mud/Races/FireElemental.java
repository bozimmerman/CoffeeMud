package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FireElemental extends StdRace
{
	public String ID(){	return "FireElemental"; }
	public String name(){ return "Fire Elemental"; }
	protected int shortestMale(){return 64;}
	protected int shortestFemale(){return 60;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 400;}
	protected int weightVariance(){return 100;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Fire Elemental";}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}
	
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+100);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("an arm of flame");
			naturalWeapon.setWeaponType(Weapon.TYPE_BURNING);
		}
		return naturalWeapon;
	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost put out!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is flickering alot and is almost smoked out.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is flickering alot and smoking massively.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is flickering alot and smoking a lot.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is flickering and smoking.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is flickering and smoking somewhat.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is showing large flickers.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some flickers.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small flickers.^N";
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
					("a pile of coal",EnvResource.RESOURCE_COAL));
			}
		}
		return resources;
	}
}
