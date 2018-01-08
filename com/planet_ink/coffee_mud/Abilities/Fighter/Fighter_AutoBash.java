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
   Copyright 2004-2018 Bo Zimmerman

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

public class Fighter_AutoBash extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_AutoBash";
	}

	private final static String localizedName = CMLib.lang().L("AutoBash");

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

	private static final String[] triggerStrings =I(new String[] {"AUTOBASH"});
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_SHIELDUSE;
	}

	protected volatile int numberOfShields=-1;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;

		if((numberOfShields<0)&&(tickID==Tickable.TICKID_MOB))
		{
			numberOfShields=0;
			for(final Enumeration<Item> i=mob.items(); i.hasMoreElements(); )
			{
				final Item I=i.nextElement();
				if((I instanceof Shield)
				&&(I.amWearingAt(Wearable.WORN_HELD)||I.amWearingAt(Wearable.WORN_WIELD))
				&&(I.owner()==ticking)
				&&(I.container() == null))
					numberOfShields++;
			}
			mob.recoverPhyStats();
		}

		for(int i=0;i<numberOfShields;i++)
		{
			if(mob.isInCombat()
			&&(mob.rangeToTarget()==0)
			&&(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
			&&(proficiencyCheck(null,0,false)))
			{
				final Ability A=mob.fetchAbility("Skill_Bash");
				if(A!=null)
					A.invoke(mob,mob.getVictim(),false,adjustedLevel(mob,0));
				if(CMLib.dice().rollPercentage()<(10/numberOfShields))
					helpProficiency(mob, 0);
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if(msg.amISource(mob)&&(msg.target() instanceof Shield))
			numberOfShields=-1;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically bashing opponents."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically bash opponents when you fight."));
			beneficialAffect(mob,mob,asLevel,0);
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to get into <S-HIS-HER> bashing mood, but fail(s)."));
		return success;
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		numberOfShields=-1;
		return super.autoInvocation(mob, force);
	}
}
