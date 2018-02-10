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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class TreeGolem extends StdRace
{
	@Override
	public String ID()
	{
		return "TreeGolem";
	}

	public TreeGolem()
	{
		super();
		super.naturalAbilImmunities.add("Disease_PoisonIvy");
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Tree Golem");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 64;
	}

	@Override
	public int shortestFemale()
	{
		return 60;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 400;
	}

	@Override
	public int weightVariance()
	{
		return 100;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Integer.MAX_VALUE;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Vegetation");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	@Override
	public boolean uncharmable()
	{
		return true;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,8 ,8 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 10, 20, 50, 65, 75, 100, 150, 160 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public String arriveStr()
	{
		return "slides in";
	}

	@Override
	public String leaveStr()
	{
		return "slides";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a jagged limb"));
			naturalWeapon.setRanges(0,2);
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_OAK);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB)
		&&(msg.amISource((MOB)myHost)))
		{
			if(msg.targetMinor()==CMMsg.TYP_LEAVE)
			{
				msg.source().tell(L("You can't really go anywhere -- you are rooted!"));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_GENDER,'N');
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_GAS,affectableStats.getStat(CharStats.STAT_SAVE_GAS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is near destruction!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is massively splintered and broken.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is extremely splintered and broken.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is very splintered and broken.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is splintered and broken.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p is splintered and slightly broken.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p has lost lots of leaves.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has lost some more leaves.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has lost a few leaves.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is no longer in perfect condition.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
				return name().toLowerCase()+" seedling";
			case Race.AGE_TODDLER:
			case Race.AGE_CHILD:
				return name().toLowerCase()+" sapling";
			default :
				return super.makeMobName('N', age);
		}
	}

	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_WOOD);
		}
		return body;
	}
	
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					(L("a pound of wood"),RawMaterial.RESOURCE_WOOD));
				resources.addElement(makeResource
					(L("a pound of leaves"),RawMaterial.RESOURCE_GREENS));
			}
		}
		return resources;
	}
}
