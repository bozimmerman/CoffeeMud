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
public class Slime extends StdRace
{
	@Override public String ID(){	return "Slime"; }
	@Override public String name(){ return "Slime"; }
	@Override public int shortestMale(){return 24;}
	@Override public int shortestFemale(){return 24;}
	@Override public int heightVariance(){return 12;}
	@Override public int lightestWeight(){return 80;}
	@Override public int weightVariance(){return 80;}
	@Override public long forbiddenWornBits(){return 0;}
	@Override public String racialCategory(){return "Slime";}
	@Override public boolean fertile(){return false;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,0,0,0,0,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER,YEARS_AGE_LIVES_FOREVER};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_WORK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SMELL);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_GENDER,'N');
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_WISDOM,1);
		affectableStats.setRacialStat(CharStats.STAT_CHARISMA,1);
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
			naturalWeapon.setName(L("a slimy protrusion"));
			naturalWeapon.setRanges(0,5);
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_SLIME);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_MELTING);
		}
		return naturalWeapon;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is unstable and almost disintegrated!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is nearing disintegration.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is noticeably disintegrating.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y is very damaged and slightly disintegrated.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y is very damaged.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p is starting to show major damage.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is definitely damaged.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g is disheveled and mildly damaged.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g is noticeably disheveled.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g is slightly disheveled.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect condition.^N";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(myHost instanceof MOB)
		{
			if((msg.amITarget(myHost))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool() instanceof Weapon)
			&&(msg.source()!=myHost)
			&&(msg.source().rangeToTarget()==0)
			&&(!((MOB)myHost).amDead()))
			{
				if(((((Weapon)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				&&(msg.source().getVictim()==myHost)
				&&(((Weapon)msg.tool()).subjectToWearAndTear())
				&&(CMLib.dice().rollPercentage()<20))
					CMLib.combat().postItemDamage(msg.source(), (Item)msg.tool(), null, 10, CMMsg.TYP_ACID,"<T-NAME> sizzle(s)!");
				if(((((Weapon)msg.tool()).weaponType()==Weapon.TYPE_PIERCING)||(((Weapon)msg.tool()).weaponType()==Weapon.TYPE_SHOOT))
				&&(msg.value()>0))
					msg.setValue((int)Math.round((msg.value())*.85));
			}
		}
	}

	@Override 
	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		final DeadBody body = super.getCorpseContainer(mob, room);
		if(body != null)
		{
			body.setMaterial(RawMaterial.RESOURCE_SLIME);
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
				("a "+name().toLowerCase()+" bit",RawMaterial.RESOURCE_SLIME));
			}
		}
		return resources;
	}
}
