package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Flower extends Vine
{
	public String ID(){	return "Flower"; }
	public String name(){ return "Flower"; }
	protected int shortestMale(){return 4;}
	protected int shortestFemale(){return 4;}
	protected int heightVariance(){return 5;}
	protected int lightestWeight(){return 1;}
	protected int weightVariance(){return 1;}
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
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK|EnvStats.CAN_NOT_TASTE|EnvStats.CAN_NOT_MOVE);
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
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
		return "floats in";
	}
	public String leaveStr()
	{
		return "floats";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a nasty bloom");
			naturalWeapon.setRanges(0,3);
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
			if(((affect.targetMinor()==Affect.TYP_LEAVE)
				||(affect.sourceMinor()==Affect.TYP_ADVANCE)
				||(affect.sourceMinor()==Affect.TYP_RETREAT)))
			{
				affect.source().tell("You can't really go anywhere -- you are rooted!");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((tickID==Host.MOB_TICK)&&(ticking instanceof MOB))
			((MOB)ticking).curState().recoverTick(((MOB)ticking),((MOB)ticking).maxState());
		return true;
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
			return "^p" + mob.displayName() + "^p has lost numerous petals.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName() + "^g has lost some petals.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName() + "^g has lost a few petals.^N";
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
					("some petals",EnvResource.RESOURCE_HERBS));
			}
		}
		return resources;
	}
}
