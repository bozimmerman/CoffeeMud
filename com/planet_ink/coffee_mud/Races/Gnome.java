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
	public String standardMobCondition(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is curiously close to death.^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in excessive bloody wounds.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from a plethora of small wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and unexpected gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some alarming wounds and small gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has some small unwanted bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised in strange places.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some small cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and interesting scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small curious bruises.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
}
