package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Mold extends StdRace
{
	public String ID(){	return "Mold"; }
	public String name(){ return "Mold"; }
	protected int shortestMale(){return 1;}
	protected int shortestFemale(){return 1;}
	protected int heightVariance(){return 1;}
	protected int lightestWeight(){return 5;}
	protected int weightVariance(){return 1;}
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
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(2*affected.envStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()/2));
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHitPoints(affectableState.getHitPoints()*4);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.GENDER,(int)'N');
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)-100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}
	public String arriveStr()
	{
		return "creeps in";
	}
	public String leaveStr()
	{
		return "creeps";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a moldy surface");
			naturalWeapon.setRanges(0,5);
			naturalWeapon.setWeaponType(Weapon.TYPE_MELTING);
		}
		return naturalWeapon;
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB))
		{
			if(affect.amISource((MOB)myHost))
			{
				if(((affect.targetMinor()==Affect.TYP_LEAVE)
					||(affect.sourceMinor()==Affect.TYP_ADVANCE)
					||(affect.sourceMinor()==Affect.TYP_RETREAT)))
				{
					affect.source().tell("You can't really go anywhere -- you are a mold!");
					return false;
				}
			}
			else
			if(affect.amITarget(myHost)&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
			{
				int dmg=affect.targetCode()-Affect.MASK_HURT;
				if(affect.sourceMinor()==Affect.TYP_FIRE)
				{
					affect.modify(affect.source(),affect.target(),affect.tool(),
								  affect.sourceCode(),affect.sourceMessage(),
								  Affect.MASK_HURT+1,affect.targetMessage(),
								  affect.othersCode(),affect.othersMessage());
					((MOB)myHost).curState().setHitPoints(((MOB)myHost).curState().getHitPoints()+dmg);
				}
				else
				if(affect.sourceMinor()==Affect.TYP_COLD)
				{
					dmg=dmg*2;
					if(dmg>1024) dmg=1000;
					affect.modify(affect.source(),affect.target(),affect.tool(),
								  affect.sourceCode(),affect.sourceMessage(),
								  Affect.MASK_HURT+dmg,affect.targetMessage(),
								  affect.othersCode(),affect.othersMessage());
				}
			}
		}
		return super.okAffect(myHost,affect);
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively scrapped and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley scrapped and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very scrapped and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is scrapped and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is scrapped and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is showing numerous scrapes.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some scrapes.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small scrapes.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
}

