package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Tumbleweed extends StdRace
{
	public String ID(){	return "Tumbleweed"; }
	public String name(){ return "Tumbleweed"; }
	protected int shortestMale(){return 14;}
	protected int shortestFemale(){return 10;}
	protected int heightVariance(){return 5;}
	protected int lightestWeight(){return 2;}
	protected int weightVariance(){return 5;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Vegetation";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK|EnvStats.CAN_NOT_TASTE);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affected.envStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()/4));
		if(affected instanceof MOB)
		{
			Room R=((MOB)affected).location();
			if(R!=null)
			{
				Area A=R.getArea();
				switch(A.weatherType(R))
				{
				case Area.WEATHER_BLIZZARD:
				case Area.WEATHER_DUSTSTORM:
				case Area.WEATHER_THUNDERSTORM:
				case Area.WEATHER_WINDY:
					affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
					break;
				default:
					break;
				}
			}
		}
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setMovement(affectableState.getMovement()*2);
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
	public String arriveStr()
	{
		return "rolls in";
	}
	public String leaveStr()
	{
		return "rolls";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a rolling slam");
			naturalWeapon.setRanges(0,2);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName() + "^r is massively shredded and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName() + "^r is extremeley shredded and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName() + "^y is very shredded and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName() + "^y is shredded and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName() + "^p is shredded and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName() + "^p has lost numerous strands.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName() + "^g has lost some strands.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName() + "^g has lost a few strands.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.displayName() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("some tumbleweed strands",EnvResource.RESOURCE_HEMP));
			}
		}
		return resources;
	}
}
