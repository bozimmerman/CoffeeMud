package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID(){	return "Tumbleweed"; }
	public String name(){ return "Tumbleweed"; }
	public int shortestMale(){return 14;}
	public int shortestFemale(){return 10;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 2;}
	public int weightVariance(){return 5;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Vegetation";}
	public boolean uncharmable(){return true;}
	public boolean fertile(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK|EnvStats.CAN_NOT_TASTE);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affected.envStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()/4));
		if(affected instanceof MOB)
		{
			Room R=((MOB)affected).location();
			if(R!=null)
			{
				Area A=R.getArea();
				switch(A.getClimateObj().weatherType(R))
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_DUSTSTORM:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_WINDY:
					affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
					break;
				default:
					break;
				}
			}
		}
	}
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHunger(999999);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectableState.setMovement(affectableState.getMovement()*2);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.GENDER,'N');
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+100);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+100);
		affectableStats.setStat(CharStats.SAVE_PARALYSIS,affectableStats.getStat(CharStats.SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.SAVE_UNDEAD,affectableStats.getStat(CharStats.SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)+100);
	}
	public String arriveStr()
	{
		return "rolls in";
	}
	public String leaveStr()
	{
		return "rolls";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a rolling slam");
			naturalWeapon.setRanges(0,2);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is massively shredded and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is extremeley shredded and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is very shredded and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is shredded and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is shredded and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has lost numerous strands.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has lost some strands.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has lost a few strands.^N";
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
					("some tumbleweed strands",EnvResource.RESOURCE_HEMP));
			}
		}
		return resources;
	}
}
