package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
		affectableStats.setIntelligence(affectableStats.getIntelligence()+1);
		affectableStats.setWisdom(affectableStats.getWisdom()-1);
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
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		s1.setName("a small patchy tunic");
		s1.setDisplayText("a small patchy tunic has been left here.");
		s1.setDescription("This small tunic is made of bits and pieces of many other shirts, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools.");
		
		Armor s2=CMClass.getArmor("GenShoes");
		s2.setName("a pair of small shoes");
		s2.setDisplayText("a pair of small shoes lie here.");
		s2.setDescription("This pair of small shoes appears to be a hodgepodge of materials and workmanship.");
			
		Armor p1=CMClass.getArmor("GenPants");
		p1.setName("a pair of small patchy pants");
		p1.setDisplayText("a pair of small patchy pants lie here.");
		p1.setDescription("This pair of small pants is made of bits and pieces of many other pants, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools.");
		wearOutfit(mob,s1,s2,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
}
