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
   Copyright 2000-2014 Bo Zimmerman

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
public class Mold extends StdRace
{
	@Override public String ID(){	return "Mold"; }
	@Override public String name(){ return "Mold"; }
	@Override public int shortestMale(){return 1;}
	@Override public int shortestFemale(){return 1;}
	@Override public int heightVariance(){return 1;}
	@Override public int lightestWeight(){return 5;}
	@Override public int weightVariance(){return 1;}
	@Override public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	@Override public String racialCategory(){return "Vegetation";}
	@Override public boolean fertile(){return false;}
	@Override public boolean uncharmable(){return true;}
	@Override public int[] getBreathables() { return breatheAnythingArray; }

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK|PhyStats.CAN_NOT_TASTE);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(2*affected.phyStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.phyStats().level()/2));
	}
	@Override
	public void affectCharState(MOB affectedMOB, CharState affectableState)
	{
		affectableState.setHitPoints(affectableState.getHitPoints()*4);
		affectableState.setHunger((Integer.MAX_VALUE/2)+10);
		affectedMOB.curState().setHunger(affectableState.getHunger());
		affectedMOB.curState().setMana(0);
	}
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_GENDER,'N');
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_WISDOM,1);
		affectableStats.setRacialStat(CharStats.STAT_CHARISMA,1);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,1);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-100);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_GAS,affectableStats.getStat(CharStats.STAT_SAVE_GAS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
	}
	@Override
	public String arriveStr()
	{
		return "creeps in";
	}
	@Override
	public String leaveStr()
	{
		return "creeps";
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a moldy surface");
			naturalWeapon.setRanges(0,5);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BARLEY);
			naturalWeapon.setWeaponType(Weapon.TYPE_MELTING);
		}
		return naturalWeapon;
	}
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((myHost!=null)
		&&(myHost instanceof MOB))
		{
			if(msg.amISource((MOB)myHost))
			{
				if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
					||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
					||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
				{
					msg.source().tell(_("You can't really go anywhere -- you are a mold!"));
					return false;
				}
			}
			else
			if(msg.amITarget(myHost)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_FIRE:
					{
						((MOB)myHost).curState().setHitPoints(((MOB)myHost).curState().getHitPoints()+msg.value());
						msg.setValue(1);
					}
					break;
				case CMMsg.TYP_WEAPONATTACK:
					if((msg.tool()!=null)
				   &&(msg.tool() instanceof Weapon)
				   &&((((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_SLASHING)
					||(((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_PIERCING)
					||(((Weapon)msg.tool()).weaponClassification()==Weapon.TYPE_BASHING)))
						msg.setValue(1);
					break;
				case CMMsg.TYP_COLD:
					msg.setValue(msg.value()*2);
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		return makeMobName('N',Race.AGE_MATURE);
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is near destruction!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is massively scrapped and damaged.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is extremely scrapped and damaged.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y is very scrapped and damaged.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y is scrapped and damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p is scrapped and slightly damaged.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is showing numerous scrapes.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g is showing some scrapes.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g is showing small scrapes.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect condition.^N";
	}
}

