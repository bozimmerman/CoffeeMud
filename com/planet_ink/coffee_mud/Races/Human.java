package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Human extends StdRace
{
	public Human()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		trainsAtFirstLevel=2;
	}

	public boolean playerSelectable(){return true;}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);

		if(!mob.isMonster())
			outfit(mob);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(140+weightModifier);
 		else
			mob.baseEnvStats().setWeight(100+weightModifier);
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
