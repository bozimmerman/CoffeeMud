package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WaterElemental extends StdRace
{
	public String ID(){	return "WaterElemental"; }
	public String name(){ return "Water Elemental"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 400;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Water Elemental";}
	private String[]racialAbilityNames={"Skill_Swim"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProfficiencies={100};
	private boolean[]racialAbilityQuals={false};
	protected String[] racialAbilityNames(){return null;}
	protected int[] racialAbilityLevels(){return null;}
	protected int[] racialAbilityProfficiencies(){return null;}
	protected boolean[] racialAbilityQuals(){return null;}
	public boolean fertile(){return false;}
	public boolean uncharmable(){return true;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
		affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+100);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("an arm of ice");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost dry!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is dripping alot and is almost dried out.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is dripping alot and steaming massively.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is dripping alot and steaming a lot.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is dripping and steaming.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is dripping and starting to steam.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is dripping more.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some dripping.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small drips.^N";
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
					("a puddle of water",EnvResource.RESOURCE_FRESHWATER));
			}
		}
		return resources;
	}
}
