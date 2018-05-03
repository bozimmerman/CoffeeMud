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

public class Spirit extends Undead
{
	@Override
	public String ID()
	{
		return "Spirit";
	}

	private final static String localizedStaticName = CMLib.lang().L("Spirit");

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
	protected boolean destroyBodyAfterUse()
	{
		return true;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAnythingArray;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	protected Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			Vector<Weapon> naturalWeaponChoices=new Vector<Weapon>();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				switch(i)
				{
					case 1:
					case 2:
					case 3:
						naturalWeapon.setName(L("an invisible punch"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 4:
						naturalWeapon.setName(L("an incorporal bite"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 5:
						naturalWeapon.setName(L("a fading elbow"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 6:
						naturalWeapon.setName(L("a translucent backhand"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 7:
						naturalWeapon.setName(L("a strong ghostly jab"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 8:
						naturalWeapon.setName(L("a ghostly punch"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 9:
						naturalWeapon.setName(L("a translucent knee"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
					case 10:
						naturalWeapon.setName(L("an otherworldly slap"));
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BURSTING);
						break;
				}
				naturalWeapon.setMaterial(RawMaterial.RESOURCE_PLASMA);
				naturalWeapon.setUsesRemaining(1000);
				naturalWeaponChoices.add(naturalWeapon);
			}
			this.naturalWeaponChoices = naturalWeaponChoices;
		}
		return naturalWeaponChoices.get(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMinor()==CMMsg.TYP_UNDEAD)
			||(msg.sourceMinor()==CMMsg.TYP_UNDEAD))
		&&((!(myHost instanceof MOB))
			||(msg.amITarget(myHost)&&(!((MOB)myHost).amDead())))
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)
			||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(msg.targetMinor()==CMMsg.TYP_LEGALWARRANT)))
		{
			String immunityName="certain";
			if(msg.tool()!=null)
				immunityName=msg.tool().name();
			if(!msg.sourceMajor(CMMsg.MASK_CNTRLMSG) && !msg.targetMajor(CMMsg.MASK_CNTRLMSG))
			{
				final Room R=CMLib.map().roomLocation(msg.target());
				if(msg.target()!=msg.source())
					R.show(msg.source(),msg.target(),CMMsg.MSG_OK_VISUAL,L("<T-NAME> seem(s) immune to @x1 attacks from <S-NAME>.",immunityName));
				else
					R.show(msg.source(),msg.target(),CMMsg.MSG_OK_VISUAL,L("<T-NAME> seem(s) immune to @x1.",immunityName));
			}
			return false;
		}
		return true;
	}

	private final String[]	racialAbilityNames			= { "Prayer_Heal", "Prayer_CureLight" };
	private final int[]		racialAbilityLevels			= { 1, 1 };
	private final int[]		racialAbilityProficiencies	= { 100, 100 };
	private final boolean[]	racialAbilityQuals			= { true, false };
	private final String[]	racialAbilityParms			= { "", "" };

	@Override
	public String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	public int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	public int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	public boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_UNDEAD,affectableStats.getStat(CharStats.STAT_SAVE_UNDEAD)+100);
	}
	
	@Override
	public Weapon myNaturalWeapon()
	{
		return funHumanoidWeapon();
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
			return L("^r@x1^r is near banishment!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is massively weak and faded.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is very faded.^N",mob.name(viewer));
		else
		if(pct<.40)
			return L("^y@x1^y is somewhat faded.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y is very weak and slightly faded.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has lost stability and is weak.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is unstable and slightly weak.^N",mob.name(viewer));
		else
		if(pct<.80)
			return L("^g@x1^g is unbalanced and unstable.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g is somewhat unbalanced.^N",mob.name(viewer));
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
			body.setMaterial(RawMaterial.RESOURCE_AIR);
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
				(L("some @x1 essence",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}

