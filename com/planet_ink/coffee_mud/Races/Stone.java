package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Stone extends StdRace
{
	public String ID(){	return "Stone"; }
	public String name(){ return "Stone"; }
	public int shortestMale(){return 2;}
	public int shortestFemale(){return 2;}
	public int heightVariance(){return 1;}
	public int lightestWeight(){return 1;}
	public int weightVariance(){return 1;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Stone Golem";}
	public boolean uncharmable(){return true;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK|EnvStats.CAN_NOT_TASTE);
		affectableStats.setArmor(affectableStats.armor()+affectableStats.armor());
		affectableStats.setAttackAdjustment(0);
		affectableStats.setDamage(0);
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHitPoints(affectableState.getHitPoints()*4);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(999999);
		affectedMOB.curState().setThirst(affectableState.getThirst());
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
			naturalWeapon.setName("its body");
			naturalWeapon.setRanges(0,3);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB)
		&&(msg.amISource((MOB)myHost)))
		{
			if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
				||(msg.sourceMinor()==CMMsg.TYP_RETREAT)
				||(msg.sourceMinor()==CMMsg.TYP_RECALL)))
			{
				msg.source().tell("You can't really go anywhere -- you're a rock!");
				return false;
			}
		}
		else
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&(myHost instanceof MOB)
		&&(msg.amITarget(myHost)))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().isMonster())
			&&(msg.source().getVictim()!=target))
			{
				msg.source().tell("Attack a rock?!");
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost broken!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively cracked and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley cracked and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very cracked and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is cracked and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is cracked and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is showing numerous cracks.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some crachs.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small cracks.^N";
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
					("some pebbles",EnvResource.RESOURCE_STONE));
			}
		}
		return resources;
	}

}
