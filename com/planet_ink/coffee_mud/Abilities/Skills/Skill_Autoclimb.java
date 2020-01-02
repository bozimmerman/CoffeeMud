package com.planet_ink.coffee_mud.Abilities.Skills;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_Autoclimb extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Autoclimb";
	}

	@Override
	public String displayText()
	{
		return L("(Autoclimb)");
	}

	private final static String	localizedName	= CMLib.lang().L("AutoClimb");

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
	public int usageType()
	{
		return USAGE_MOVEMENT;
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

	private static final String[]	triggerStrings	= I(new String[] { "AUTOCLIMB" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
	}

	protected volatile boolean	noRepeat	= false;


	protected int fixPts(final MOB mob, final int pts)
	{
		int halfPts=pts/2;
		halfPts+=super.getXLOWCOSTLevel(mob)+(super.getXLEVELLevel(mob)/2);
		if(halfPts > pts)
			return pts;
		return halfPts;
	}

	protected boolean doAutoClimb(final MOB mob, final int dir, final boolean flee)
	{
		final Room R=mob.location();
		if((dir >=0)
		&& (R.getRoomInDir(dir)!=null)
		&&(mob.curState().getMovement()>0)
		&&(!CMLib.flags().isFalling(mob)))
		{
			final Ability A=mob.fetchAbility("Skill_Climb");
			if((A!=null)
			&&(proficiencyCheck(mob, 0, false)))
			{
				noRepeat=true;
				if(flee)
					CMLib.commands().postFlee((MOB)affected, "NOWHERE");
				if(A.invoke(mob,CMParms.parse(CMLib.directions().getDirectionName(dir)),null,false,0))
				{
					final int[] usage=A.usageCost(mob,false);
					if(CMath.bset(A.usageType(),Ability.USAGE_HITPOINTS)&&(usage[USAGEINDEX_HITPOINTS]>0))
						mob.curState().adjHitPoints(fixPts(mob,usage[USAGEINDEX_HITPOINTS]),mob.maxState());
					if(CMath.bset(A.usageType(),Ability.USAGE_MANA)&&(usage[USAGEINDEX_MANA]>0))
						mob.curState().adjMana(fixPts(mob,usage[USAGEINDEX_MANA]),mob.maxState());
					if(CMath.bset(A.usageType(),Ability.USAGE_MOVEMENT)&&(usage[USAGEINDEX_MOVEMENT]>0))
						mob.curState().adjMovement(fixPts(mob,usage[USAGEINDEX_MOVEMENT]),mob.maxState());
				}
				if(CMLib.dice().rollPercentage()<10)
					helpProficiency(mob, 0);
				noRepeat=false;
			}
			else
			if(A==null)
				mob.tell(L("You don't seem to know how to climb?!"));
			else
				mob.tell(L("You forgot to automatically climb."));
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.source()==affected)
		&&(!noRepeat))
		{
			if((msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL)
			&&(msg.targetMessage()!=null))
			{
				final int x=msg.targetMessage().indexOf(' ');
				final String cmd;
				if(x>0)
					cmd=msg.targetMessage().substring(0, x).toUpperCase().trim();
				else
					cmd=msg.targetMessage().toUpperCase().trim();
				final int dir=CMLib.directions().getGoodDirectionCode(cmd);
				if(dir >= 0)
					return doAutoClimb(msg.source(),dir, false);
			}
			if(((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_FLEE))
			&&(msg.target() instanceof Room)
			&&(msg.tool() instanceof Exit)
			&&(msg.source().riding()==null)
			&&(((MOB)affected).location()!=null))
			{
				final int dir=msg.value()-1;
				return doAutoClimb((MOB)affected,dir,(msg.targetMinor()==CMMsg.TYP_FLEE));
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically climbing around."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically climb around while you move."));
			beneficialAffect(mob,mob,asLevel,adjustedLevel(mob,asLevel));
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to get into <S-HIS-HER> climbing position, but fail(s)."));
		return success;
	}
}
