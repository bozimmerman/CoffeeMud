package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2002-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Chicken extends StdRace
{
	@Override public String ID(){	return "Chicken"; }
	@Override public String name(){ return "Chicken"; }
	@Override public int shortestMale(){return 13;}
	@Override public int shortestFemale(){return 13;}
	@Override public int heightVariance(){return 6;}
	@Override public int lightestWeight(){return 20;}
	@Override public int weightVariance(){return 5;}
	@Override public long forbiddenWornBits(){return ~(Wearable.WORN_HEAD|Wearable.WORN_EYES);}
	@Override public String racialCategory(){return "Avian";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,0 ,0 ,1 ,1 ,0 ,2 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,1,2,4,7,15,20,21,22};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)

	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,3);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,4);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}
	@Override
	public String arriveStr()
	{
		return "walks in";
	}
	@Override
	public String leaveStr()
	{
		return "walks";
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a menacing beak"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				switch(gender)
				{
				case 'M': case 'm': return "cockrell";
				case 'F': case 'f': return "chick";
				default: return super.makeMobName(gender, age);
				}
			case Race.AGE_YOUNGADULT:
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
				switch(gender)
				{
				case 'M': case 'm': return "rooster";
				case 'F': case 'f': return "hen";
				default: return super.makeMobName(gender, age);
				}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
				switch(gender)
				{
				case 'M': case 'm': return "old rooster";
				case 'F': case 'f': return "old hen";
				default: return super.makeMobName(gender, age);
				}
			default :
				return super.makeMobName(gender, age);
		}
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in blood and matted feathers.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has numerous bloody matted feathers.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some bloody matted feathers.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a lot of missing feathers.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p has a few missing feathers.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has a missing feather.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has a few feathers out of place.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g has a some ruffled feathers.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect health.^N";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			if((CMLib.dice().rollPercentage()>99)&&(((MOB)ticking).numItems()<9))
			{
				final Item I=CMClass.getItem("GenFoodResource");
				I.setName(L("an egg"));
				I.setDisplayText(L("an egg has been left here."));
				I.setMaterial(RawMaterial.RESOURCE_EGGS);
				I.setDescription(L("It looks like a chicken egg!"));
				I.basePhyStats().setWeight(1);
				CMLib.materials().addEffectsToResource(I);
				((MOB)ticking).addItem((Item)I.copyOf());
			}
			if((((MOB)ticking).numItems()>5)
			&&(((MOB)ticking).location()!=null)
			&&(((MOB)ticking).location().findItem(null,"an egg")==null))
			{
				final Item I=((MOB)ticking).findItem("an egg");
				if(I!=null)
				{
					((MOB)ticking).location().show(((MOB)ticking),null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> lay(s) an egg."));
					I.removeFromOwnerContainer();
					I.executeMsg((MOB)ticking,CMClass.getMsg((MOB)ticking,I,null,CMMsg.TYP_ROOMRESET,null));
					((MOB)ticking).location().addItem(I,ItemPossessor.Expire.Resource);
					((MOB)ticking).location().recoverRoomStats();
				}
			}
		}
		return true;
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" lips",RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" feathers",RawMaterial.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
