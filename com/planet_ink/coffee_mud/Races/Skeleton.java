package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skeleton extends StdRace
{
	public String ID(){	return "Skeleton"; }
	public String name(){ return "Skeleton"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Undead";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!(myHost instanceof MOB)) return super.okAffect(myHost,affect);
		MOB myChar=(MOB)myHost;
		MOB mob=(MOB)myChar;
		if((affect.amITarget(mob))
	    &&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&((((Weapon)affect.tool()).weaponType()==Weapon.TYPE_PIERCING)
			||(((Weapon)affect.tool()).weaponType()==Weapon.TYPE_SLASHING))
		&&(!mob.amDead()))
		{
			int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),2.0));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return super.okAffect(myChar,affect);
	}


	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		super.affectCharState(affectedMOB, affectableState);
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setThirst(999999);
		affectedMOB.curState().setThirst(affectableState.getThirst());
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
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("knuckle bone",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
