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
@SuppressWarnings({"unchecked","rawtypes"})
public class Smurf extends StdRace
{
	@Override public String ID(){	return "Smurf"; }
	@Override public String name(){ return "Smurf"; }
	@Override public int shortestMale(){return 7;}
	@Override public int shortestFemale(){return 7;}
	@Override public int heightVariance(){return 1;}
	@Override public int lightestWeight(){return 5;}
	@Override public int weightVariance(){return 2;}
	@Override public long forbiddenWornBits(){return 0;}
	@Override public String racialCategory(){return "Fairy-kin";}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,2,20,110,175,263,350,390,430};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	protected Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName(L("a smurfy punch"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 4:
					naturalWeapon.setName(L("some smurfy teeth"));
					naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
					case 5:
					naturalWeapon.setName(L("a smurfy elbow"));
					naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
					break;
					case 6:
					naturalWeapon.setName(L("a smurfy backhand"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 7:
					naturalWeapon.setName(L("a smurfy jab"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 8:
					naturalWeapon.setName(L("a smurfy poke"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 9:
					naturalWeapon.setName(L("a smurfy knee"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 10:
					naturalWeapon.setName(L("a smurfy head butt"));
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
				naturalWeapon.setUsesRemaining(1000);
				naturalWeaponChoices.add(naturalWeapon);
			}
		}
		return naturalWeaponChoices.get(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
	}
	@Override
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is mortally wounded and will soon die.^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a few bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is cut and bruised.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has a few bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g has a few small bruises.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect health.^N";
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" head",RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
