package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Elf extends StdRace
{
	public Elf()
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
		affectableStats.setDexterity(affectableStats.getDexterity()+1);
		affectableStats.setConstitution(affectableStats.getConstitution()-1);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(90+weightModifier);
 		else
			mob.baseEnvStats().setWeight(70+weightModifier);
	}
	public void outfit(MOB mob)
	{
		// Have to, since it requires use of special constructor
		Armor s1=CMClass.getArmor("GenShirt");
		s1.setName("a delicate green shirt");
		s1.setDisplayText("a delicate green shirt sits gracefully here.");
		s1.setDescription("Obviously fine craftmenship, with sharp folds and intricate designs.");
		
		Armor s2=CMClass.getArmor("GenShoes");
		s2.setName("a pair of sandals");
		s2.setDisplayText("a pair of sandals lie here.");
		s2.setDescription("Obviously fine craftmenship, these light leather sandals have tiny woodland drawings in them.");
			
		Armor p1=CMClass.getArmor("GenPants");
		p1.setName("some delicate leggings");
		p1.setDisplayText("a pair delicate brown leggings sit here.");
		p1.setDescription("Obviously fine craftmenship, with sharp folds and intricate designs.  They look perfect for dancing in!");
		wearOutfit(mob,s1,s2,p1);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
}
