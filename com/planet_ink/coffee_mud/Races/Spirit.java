package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spirit extends Undead
{
	public String ID(){	return "Spirit"; }
	public String name(){ return "Spirit"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	protected boolean destroyBodyAfterUse(){return true;}

	protected static Vector resources=new Vector();

	public Weapon funHumanoidWeapon()
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
					naturalWeapon.setName("an invisible punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 4:
					naturalWeapon.setName("an incorporal bite");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 5:
					naturalWeapon.setName("a fading elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 6:
					naturalWeapon.setName("a translucent backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 7:
					naturalWeapon.setName("a strong ghostly jab");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 8:
					naturalWeapon.setName("a ghostly punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
					case 9:
					naturalWeapon.setName("a translucent knee");
					naturalWeapon.setWeaponType(Weapon.TYPE_BURSTING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		return (Weapon)naturalWeaponChoices.elementAt(Dice.roll(1,naturalWeaponChoices.size(),-1));
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near banishment!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively weak and faded.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is very faded.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is somewhat faded.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is very weak and slightly faded.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has lost stability and is weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is in somewhat unbalanced.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" essence",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

