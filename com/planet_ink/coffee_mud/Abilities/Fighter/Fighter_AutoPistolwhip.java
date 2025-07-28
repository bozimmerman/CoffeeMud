package com.planet_ink.coffee_mud.Abilities.Fighter;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class Fighter_AutoPistolwhip extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_AutoPistolwhip";
	}

	private final static String localizedName = CMLib.lang().L("AutoPistolwhip");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	private static final String[] triggerStrings =I(new String[] {"AUTOPISTOLWHIP"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	protected boolean inventoryAmmoCheck(final MOB M, final String ammoType)
	{
		if(M==null)
			return false;
		for(int i=0;i<M.numItems();i++)
		{
			final Item I=M.getItem(i);
			if((I instanceof Ammunition)
			&&(((Ammunition)I).ammunitionType().equalsIgnoreCase(ammoType)))
				return true;
		}
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_NEEDRELOAD)
		&&(msg.source()==affected)
		&&(Fighter_Pistolwhip.isPistolWeapon(msg.target())))
		{
			final AmmunitionWeapon W = (AmmunitionWeapon)msg.target();
			if((W.ammunitionRemaining()<=0)
			&&(!inventoryAmmoCheck(msg.source(),W.ammunitionType()))
			&&(super.proficiencyCheck(msg.source(), 0, false)))
			{
				final Ability A = msg.source().fetchAbility("Fighter_Pistolwhip");
				if(A!=null)
				{
					this.helpProficiency(msg.source(), 0);
					A.invoke(msg.source(), msg.source().getVictim(), false, 0);
					if(CMLib.dice().rollPercentage() < super.getXLEVELLevel(msg.source()))
						A.invoke(msg.source(), msg.source().getVictim(), false, 0);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically pistolwhipping opponents."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically pistolwhipping opponents when you run out of ammo."));
			beneficialAffect(mob,mob,asLevel,0);
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to get into <S-HIS-HER> pistolwhipping mood, but fail(s)."));
		return success;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
