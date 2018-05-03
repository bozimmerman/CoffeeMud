package com.planet_ink.coffee_mud.Abilities.Thief;
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

public class Thief_Autosneak extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Autosneak";
	}

	@Override
	public String displayText()
	{
		return L("(AutoSneak)");
	}

	private final static String	localizedName	= CMLib.lang().L("AutoSneak");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "AUTOSNEAK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
	}

	protected boolean	noRepeat	= false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected instanceof MOB)
		&&(!noRepeat)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(((MOB)affected).location()!=null))
		{
			int dir=-1;
			final MOB mob=(MOB)affected;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((mob.location().getRoomInDir(d)==msg.target())
				||(mob.location().getReverseExit(d)==msg.tool())
				||(mob.location().getExitInDir(d)==msg.tool()))
				{
					dir = d;
					break;
				}
			}
			if(dir>=0)
			{
				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A==null)
					A=mob.fetchAbility("Ranger_Sneak");
				if(A!=null)
				{
					noRepeat=true;
					if(A.invoke(mob,CMParms.parse(CMLib.directions().getDirectionName(dir)),null,false,0))
					{
						final int[] usage=A.usageCost(mob,false);
						if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS)&&(usage[USAGEINDEX_HITPOINTS]>0))
							mob.curState().adjHitPoints(usage[USAGEINDEX_HITPOINTS]/2,mob.maxState());
						if(CMath.bset(A.usageType(),Ability.USAGE_MANA)&&(usage[USAGEINDEX_MANA]>0))
							mob.curState().adjMana(usage[USAGEINDEX_MANA]/2,mob.maxState());
						if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT)&&(usage[USAGEINDEX_MOVEMENT]>0))
							mob.curState().adjMovement(usage[USAGEINDEX_MOVEMENT]/2,mob.maxState());
					}
					if(CMLib.dice().rollPercentage()<10)
						helpProficiency(mob, 0);
					noRepeat=false;
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically sneaking around."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically sneak around while you move."));
			beneficialAffect(mob,mob,asLevel,adjustedLevel(mob,asLevel));
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to get into <S-HIS-HER> sneaking stance, but fail(s)."));
		return success;
	}
}
