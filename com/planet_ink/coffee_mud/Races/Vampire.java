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
   Copyright 2023-2023 Bo Zimmerman

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
public class Vampire extends Undead
{
	@Override
	public String ID()
	{
		return "Vampire";
	}

	private final static String localizedStaticName = CMLib.lang().L("Vampire");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	private final String[]	racialAbilityNames			= { "Undead_EnergyDrain" };
	private final int[]		racialAbilityLevels			= { 1 };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { false };
	private final String[]	racialAbilityParms			= { "" };

	@Override
	protected String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	protected int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	protected int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	protected boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final List<Weapon> newList = new XVector<Weapon>(super.getHumanoidWeapons());
			// remove dups
			final Set<String> names = new TreeSet<String>();
			for(int i=newList.size()-1;i>=0;i--)
			{
				final Weapon W = newList.get(i);
				if(!names.contains(W.Name()))
				{
					names.add(W.Name());
					final Weapon W1 = (Weapon)W.copyOf();
					if(CMStrings.containsWordIgnoreCase(W.Name(), L("teeth"))
					||CMStrings.containsWordIgnoreCase(W.Name(), L("bite")))
					{
						final Ability A=CMClass.getAbility("Prop_FightSpellCast");
						W1.addNonUninvokableEffect(A);
						A.setMiscText("20%;Undead_EnergyDrain;NOOWN");
					}
					newList.set(i, W1);
				}
				else
					newList.remove(i);
			}
			this.naturalWeaponChoices = newList.toArray(new Weapon[newList.size()]);
		}
		return super.getNaturalWeapons();
	}

	private final String[]	racialEffectNames			= { "Prop_WeaponImmunity" , "Disease_Vampirism" };
	private final int[]		racialEffectLevels			= { 1 , 1};
	private final String[]	racialEffectParms			= { "+ALL -WOODEN -MAGICSKILLS", "" };

	@Override
	protected String[] racialEffectNames()
	{
		return racialEffectNames;
	}

	@Override
	protected int[] racialEffectLevels()
	{
		return racialEffectLevels;
	}

	@Override
	protected String[] racialEffectParms()
	{
		return racialEffectParms;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,22);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,22);
		affectableStats.setRacialStat(CharStats.STAT_CHARISMA,20);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectedMOB.baseCharStats().getStat(CharStats.STAT_CHARISMA));
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_CHARISMA_ADJ));
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK|PhyStats.CAN_SEE_INVISIBLE);
	}

}
