package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Dice;
import java.util.*;

public class StdRace implements Race
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String name="MOB";
	protected int practicesAtFirstLevel=0;
	protected int trainsAtFirstLevel=0;
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	public String ID()
	{
		return myID;
	}
	public String name()
	{
		return name;
	}
	public boolean playerSelectable(){return false;}

	/** some general statistics about such an item
	 * see class "EnvStats" for more information. */
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{

	}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		return true;
	}

	public void affect(MOB myChar, Affect affect)
	{
	}
	public void wearOutfit(MOB mob, Armor s1, Armor s2, Armor p1)
	{
		if((s1!=null)&&(mob.fetchInventory(s1.ID())==null))
		{
			mob.addInventory(s1);
			if(!mob.amWearingSomethingHere(Item.ON_TORSO))
				s1.wearAt(Item.ON_TORSO);
		}
		if((p1!=null)&&(mob.fetchInventory(p1.ID())==null))
		{
			mob.addInventory(p1);
			if(!mob.amWearingSomethingHere(Item.ON_LEGS))
				p1.wearAt(Item.ON_LEGS);
		}
		if((s2!=null)&&(mob.fetchInventory(s2.ID())==null))
		{
			mob.addInventory(s2);
			if(!mob.amWearingSomethingHere(Item.ON_FEET))
				s2.wearAt(Item.ON_FEET);
		}
	}
	public String arriveStr()
	{
		return "arrives";
	}
	public String leaveStr()
	{
		return "leaves";
	}
	public void outfit(MOB mob)
	{
	}
	public void level(MOB mob)
	{
	}
	public void newCharacter(MOB mob)
	{
		mob.setPractices(mob.getPractices()+practicesAtFirstLevel);
		mob.setTrains(mob.getTrains()+trainsAtFirstLevel);

	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
			naturalWeapon=(Weapon)CMClass.getWeapon("Natural");
		return naturalWeapon;
	}
	
	public Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<7;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName("a quick punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 4:
					naturalWeapon.setName("fingernails and teeth");
					naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
					case 5:
					naturalWeapon.setName("an elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
					break;
					case 6:
					naturalWeapon.setName("a backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		return (Weapon)naturalWeaponChoices.elementAt(Dice.roll(1,naturalWeaponChoices.size(),0)-1);
	}
	public void setWeight(MOB mob)
	{
		if(mob.baseEnvStats().weight()>5) return;

		Random randomizer = new Random(System.currentTimeMillis());
		char gender = mob.baseCharStats().getGender();

		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		if (gender == 'M')
			mob.baseEnvStats().setWeight(130+weightModifier);
 		else
			mob.baseEnvStats().setWeight(105+weightModifier);
	}
}
