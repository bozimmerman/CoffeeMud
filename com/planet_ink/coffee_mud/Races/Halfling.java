package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Halfling extends StdRace
{
	public Halfling()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
	public boolean playerSelectable(){return true;}

	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		mob.baseCharStats().setDexterity(mob.baseCharStats().getDexterity()+1);
		mob.baseCharStats().setStrength(mob.baseCharStats().getStrength()-1);
		mob.baseEnvStats().setSensesMask(Sense.CAN_SEE_INFRARED);

		if(!mob.isMonster())
			outfit(mob);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setDexterity(affectableStats.getDexterity()+1);
		affectableStats.setStrength(affectableStats.getStrength()-1);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(52+weightModifier);
 		else
			mob.baseEnvStats().setWeight(48+weightModifier);
	}
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		s1.setName("a small tunic");
		s1.setDisplayText("a small tunic is folded neatly here.");
		s1.setDescription("It is a small but nicely made button-up tunic.");
		Armor p1=CMClass.getArmor("GenPants");
		p1.setName("some small pants");
		p1.setDisplayText("some small pants lie here.");
		p1.setDescription("They appear to be for a dimunitive person, and extend barely past the knee at that.");
		wearOutfit(mob,s1,null,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
}
