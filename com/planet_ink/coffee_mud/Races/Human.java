package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
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
			mob.baseEnvStats().setWeight(140+weightModifier);
 		else
			mob.baseEnvStats().setWeight(100+weightModifier);
	}
}
