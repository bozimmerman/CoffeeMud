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
public class Troll extends StdRace
{
	@Override public String ID(){	return "Troll"; }
	@Override public String name(){ return "Troll"; }
	@Override public int shortestMale(){return 74;}
	@Override public int shortestFemale(){return 70;}
	@Override public int heightVariance(){return 14;}
	@Override public int lightestWeight(){return 200;}
	@Override public int weightVariance(){return 200;}
	@Override public long forbiddenWornBits(){return 0;}
	@Override public String racialCategory(){return "Troll-kin";}
	private final String[]culturalAbilityNames={"Draconic"};
	private final int[]culturalAbilityProficiencies={50};
	@Override public String[] culturalAbilityNames(){return culturalAbilityNames;}
	@Override public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	@Override public int[] bodyMask(){return parts;}

	private final int[] agingChart={0,1,5,40,100,150,200,230,260};
	@Override public int[] getAgingChart(){return agingChart;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

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
		if(!super.tick(ticking,tickID)) return false;
		if(tickID!=Tickable.TICKID_MOB) return false;
		if(ticking instanceof MOB)
		{
			final MOB M=(MOB)ticking;
			final Room room=M.location();
			if((room!=null)&&(!M.amDead()))
			{
				if(M.curState().adjHitPoints((int)Math.round(CMath.div(M.phyStats().level(),2.0)),M.maxState()))
					M.location().show(M,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> regenerate(s).");
				final Area A=room.getArea();
				if(A!=null)
				{
					switch(A.getClimateObj().weatherType(room))
					{
					case Climate.WEATHER_HEAT_WAVE:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,8,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The scorching heat <DAMAGE> <T-NAME>!");
						}
						break;
					case Climate.WEATHER_DUSTSTORM:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,16,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The burning hot dust <DAMAGE> <T-NAME>!");
						}
						break;
					case Climate.WEATHER_DROUGHT:
						if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_FIRE))
						{
							final int damage=CMLib.dice().roll(1,8,0);
							CMLib.combat().postDamage(M,M,null,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The burning dry heat <DAMAGE> <T-NAME>!");
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
			naturalWeapon.setName("huge clawed hands");
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_SLASHING);
		}
		return naturalWeapon;
	}
	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is near to heartless death!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in torn slabs of flesh.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is gored badly with lots of tears.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has numerous gory tears and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some gory tears and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a few gory wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p is cut and bruised heavily.^N";
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
				for(int i=0;i<4;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",RawMaterial.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
