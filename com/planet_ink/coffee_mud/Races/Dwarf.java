package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Dwarf extends StdRace
{
	public Dwarf()
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
		affectableStats.setConstitution(affectableStats.getConstitution()+1);
		affectableStats.setCharisma(affectableStats.getCharisma()-1);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(130+weightModifier);
 		else
			mob.baseEnvStats().setWeight(105+weightModifier);
	}
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		s1.setName("a grey work tunic");
		s1.setDisplayText("a grey work tunic has been left here.");
		s1.setDescription("There are lots of little loops and folks for hanging tools about it.");
		
		Armor s2=CMClass.getArmor("GenShoes");
		s2.setName("a pair of hefty work boots");
		s2.setDisplayText("some hefty work boots have been left here.");
		s2.setDescription("Thick and well worn boots with very tough souls.");
			
		Armor p1=CMClass.getArmor("GenPants");
		p1.setName("some hefty work pants");
		p1.setDisplayText("some hefty work pants have been left here.");
		p1.setDescription("There are lots of little loops and folks for hanging tools about it.");
		
		wearOutfit(mob,s1,s2,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is nearly dead!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding from cuts and gashes.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous wounds.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some wounds.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few cuts.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is very winded.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is slightly winded.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
}
