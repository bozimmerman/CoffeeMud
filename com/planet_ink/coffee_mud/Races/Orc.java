package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Orc extends StdRace
{
	protected static Vector resources=new Vector();
	public Orc()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=60;
		shortestFemale=56;
		heightVariance=12;
		// pounds
		lightestWeight=150;
		weightVariance=100;
		forbiddenWornBits=0;
	}
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,12);
		affectableStats.setStat(CharStats.DEXTERITY,10);
		affectableStats.setStat(CharStats.INTELLIGENCE,7);
	}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		super.startRacing(mob,verifyOnly);
		Ability A=CMClass.getAbility("Orcish");
		if(A!=null)
		{
			A=(Ability)A.newInstance();
			mob.addAbility(A);
			A.autoInvocation(mob);
			if(mob.isMonster())
				A.invoke(mob,mob,true);
		}
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is nearly defeated.^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("ape fur",EnvResource.RESOURCE_FUR));
			}
		}
		return resources;
	}
}
