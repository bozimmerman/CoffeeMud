package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GreatCat extends StdRace
{
	public GreatCat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Great Cat";
	}
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStrength(16);
		affectableStats.setDexterity(16);
		affectableStats.setIntelligence(6);
	}
	public String arriveStr()
	{
		return "struts in";
	}
	public String leaveStr()
	{
		return "struts";
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_DARK);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		mob.baseEnvStats().setWeight(300+weightModifier);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("huge sharp claws");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
}
