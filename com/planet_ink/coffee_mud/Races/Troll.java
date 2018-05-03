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
   Copyright 2001-2018 Bo Zimmerman

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
public class Troll extends StdRace
{
	@Override
	public String ID()
	{
		return "Troll";
	}

	private final static String localizedStaticName = CMLib.lang().L("Troll");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 74;
	}

	@Override
	public int shortestFemale()
	{
		return 70;
	}

	@Override
	public int heightVariance()
	{
		return 14;
	}

	@Override
	public int lightestWeight()
	{
		return 200;
	}

	@Override
	public int weightVariance()
	{
		return 200;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Giant-kin");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	culturalAbilityNames			= { "Draconic" };
	private final int[]		culturalAbilityProficiencies	= { 50 };

	@Override
	public String[] culturalAbilityNames()
	{
		return culturalAbilityNames;
	}

	@Override
	public int[] culturalAbilityProficiencies()
	{
		return culturalAbilityProficiencies;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 1, 5, 40, 100, 150, 200, 230, 260 };

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
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,16);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,12);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,8);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)-100);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(myHost instanceof MOB)
		{
			final MOB mob=(MOB)myHost;
			if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			   &&(msg.sourceMinor()==CMMsg.TYP_FIRE))
			{
				final int recovery=(int)Math.round(CMath.mul((msg.value()),1.5));
				msg.setValue(msg.value()+recovery);
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_MOB)
			return false;
		if(ticking instanceof MOB)
		{
			final MOB M=(MOB)ticking;
			final Room room=M.location();
			if((room!=null)&&(!M.amDead()))
			{
				if(M.curState().adjHitPoints((int)Math.round(CMath.div(M.phyStats().level(),2.0)),M.maxState()))
					M.location().show(M,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> regenerate(s)."));
				final Area A=room.getArea();
				if(A!=null)
				{
					switch(A.getClimateObj().weatherType(room))
					{
					case Climate.WEATHER_HEAT_WAVE:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,8,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The scorching heat <DAMAGE> <T-NAME>!"));
						}
						break;
					case Climate.WEATHER_DUSTSTORM:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,16,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The burning hot dust <DAMAGE> <T-NAME>!"));
						}
						break;
					case Climate.WEATHER_DROUGHT:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,8,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The burning dry heat <DAMAGE> <T-NAME>!"));
						}
						break;
					}
				}
			}
		}
		return true;
	}

	@Override
	public String arriveStr()
	{
		return "thunders in";
	}

	@Override
	public String leaveStr()
	{
		return "leaves";
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("huge clawed hands"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_SLASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is near to heartless death!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in torn slabs of flesh.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is gored badly with lots of tears.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y has numerous gory tears and gashes.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some gory tears and gashes.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a few gory wounds.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut and bruised heavily.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g has some minor cuts and bruises.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and scratches.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<4;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 hide",name().toLowerCase()),RawMaterial.RESOURCE_HIDE));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
