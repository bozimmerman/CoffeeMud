package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spirit extends Undead
{
	public String ID(){	return "Spirit"; }
	public String name(){ return "Spirit"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	protected boolean destroyBodyAfterUse(){return true;}

	protected static Vector resources=new Vector();

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
		return (Weapon)naturalWeaponChoices.elementAt(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is near banishment!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is massively weak and faded.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is very faded.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y is somewhat faded.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y is very weak and slightly faded.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p has lost stability and is weak.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is unstable and slightly weak.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g is unbalanced and unstable.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g is somewhat unbalanced.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect condition.^N";
	}
	
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" essence",RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

