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
public class Tumbleweed extends StdRace
{
	@Override
	public String ID()
	{
		return "Tumbleweed";
	}

	public Tumbleweed()
	{
		super();
		super.naturalAbilImmunities.add("Disease_PoisonIvy");
	}
	
	private final static String localizedStaticName = CMLib.lang().L("Tumbleweed");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 14;
	}

	@Override
	public int shortestFemale()
	{
		return 10;
	}

	@Override
	public int heightVariance()
	{
		return 5;
	}

	@Override
	public int lightestWeight()
	{
		return 2;
	}

	@Override
	public int weightVariance()
	{
		return 5;
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
	public boolean fertile()
	{
		return false;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 0, 0, 0, 0, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER, YEARS_AGE_LIVES_FOREVER };

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
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_TASTE);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affected.phyStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.phyStats().level()/4));
		if(affected instanceof MOB)
		{
			final Room R=((MOB)affected).location();
			if(R!=null)
			{
				final Area A=R.getArea();
				switch(A.getClimateObj().weatherType(R))
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_DUSTSTORM:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_WINDY:
					affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger((Integer.MAX_VALUE/2)+10);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setMovement(affectableState.getMovement()*2);
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
	public String arriveStr()
	{
		return "rolls in";
	}

	@Override
	public String leaveStr()
	{
		return "rolls";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a rolling slam"));
			naturalWeapon.setRanges(0,2);
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_OAK);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		return super.makeMobName('N', Race.AGE_MATURE);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is near destruction!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is massively shredded and damaged.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is extremely shredded and damaged.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is very shredded and damaged.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is shredded and damaged.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p is shredded and slightly damaged.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p has lost numerous strands.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has lost some strands.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has lost a few strands.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g is no longer in perfect condition.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect condition.^N",mob.name(viewer));
	}
	
	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_HEMP);
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
					(L("some tumbleweed strands"),RawMaterial.RESOURCE_HEMP));
			}
		}
		return resources;
	}
}
