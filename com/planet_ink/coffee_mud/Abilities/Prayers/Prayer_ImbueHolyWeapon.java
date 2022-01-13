package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2022 Bo Zimmerman

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
public class Prayer_ImbueHolyWeapon extends Prayer_ImbueShield
{

	@Override
	public String ID()
	{
		return "Prayer_ImbueHolyWeapon";
	}

	private final static String	localizedName	= CMLib.lang().L("Imbue Holy Weapon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING|Ability.FLAG_HOLY;
	}

	@Override
	protected int maxPrayerLevel()
	{
		return 20;
	}

	@Override
	protected boolean isOkPrayer(final Ability imbuePrayerA)
	{
		if((imbuePrayerA.ID().equals("Spell_Stoneskin"))
		||(imbuePrayerA.ID().equals("Spell_MirrorImage"))
		||(CMath.bset(imbuePrayerA.flags(), FLAG_SUMMONING))
		||(imbuePrayerA.abstractQuality()!=Ability.QUALITY_MALICIOUS)
		||(!imbuePrayerA.canTarget(CAN_MOBS)))
			return false;
		return true;
	}
	
	@Override
	protected void doImbue(final MOB mob, final Item targetI, final Ability imbuePrayerA)
	{
		mob.location().show(mob,targetI,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) divinely!"));
		if(CMath.bset(flags(), Ability.FLAG_UNHOLY))
		targetI.basePhyStats().setDisposition(targetI.basePhyStats().disposition()|PhyStats.IS_BONUS);
		targetI.basePhyStats().setLevel(targetI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())/2));
		//final Ability A=CMClass.getAbility("Prop_WearSpellCast");
		//A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
		final Ability A=CMClass.getAbility("Prop_FightSpellCast");
		A.setMiscText("25%;MAXTICKS=12;"+imbuePrayerA.ID()+";");
		targetI.addNonUninvokableEffect(A);
		targetI.recoverPhyStats();
	}

	@Override
	protected int getXPCost(final Ability imbuePrayerA)
	{
		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		return experienceToLose;
	}

	@Override
	protected boolean isAppropriateItem(final Physical target)
	{
		return (target instanceof Weapon);
	}

	@Override
	protected boolean checkAlignment(final MOB mob, final Physical target, final boolean quiet)
	{
		if(CMLib.flags().isEvil(target))
		{
			mob.tell(L("You can not imbue that repulsive weapon."));
			return false;
		}
		return true;
	}

}
