package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Gnome extends StdRace
{
	public Gnome()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
	public boolean playerSelectable(){return true;}
	
	public void newCharacter(MOB mob)
	{
		super.newCharacter(mob);
		mob.baseCharStats().setIntelligence(mob.baseCharStats().getIntelligence()+1);
		mob.baseCharStats().setWisdom(mob.baseCharStats().getWisdom()-1);
		mob.baseEnvStats().setSensesMask(Sense.CAN_SEE_INFRARED);

		if(!mob.isMonster())
		{
			Shirt s1=new Shirt();
			s1.wear(Item.ON_TORSO);
			mob.addInventory(s1);
			Pants p1=new Pants();
			p1.wear(Item.ON_LEGS);
			mob.addInventory(p1);
		}
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4; 		
		if (gender == 'M')
			mob.baseEnvStats().setWeight(72+weightModifier);
 		else
			mob.baseEnvStats().setWeight(68+weightModifier);
	}
}
