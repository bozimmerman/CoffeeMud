package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class HalfElf extends StdRace
{
	public HalfElf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Half Elf";
	}
	public boolean playerSelectable(){return true;}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		mob.baseEnvStats().setSensesMask(Sense.CAN_SEE_INFRARED);

		if(!mob.isMonster())
			outfit(mob);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_INFRARED);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(110+weightModifier);
 		else
			mob.baseEnvStats().setWeight(85+weightModifier);
	}
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		Armor s2=CMClass.getArmor("GenShoes");
		Armor p1=CMClass.getArmor("GenPants");
		wearOutfit(mob,s1,s2,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
}
