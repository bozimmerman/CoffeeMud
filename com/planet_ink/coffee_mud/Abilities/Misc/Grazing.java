package com.planet_ink.coffee_mud.Abilities.Misc;
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
public class Grazing extends StdAbility
{
	@Override
	public String ID()
	{
		return "Grazing";
	}

	private final static String localizedName = CMLib.lang().L("Grazing");

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

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[] triggerStrings = I(new String[] { "GRAZE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected volatile boolean	isGrazing	= false;
	protected volatile int		tickCount	= 0;
	protected volatile int		cuds		= 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			final Room R=mob.location();
			if(R==null)
				return super.tick(ticking, tickID);
			if(isGrazing)
			{
				if((R.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
				||(mob.isInCombat())
				||(!CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				||(isBusy(mob)))
				{
					mob.tell(L("You stop grazing."));
					isGrazing=false;
				}
				else
				if(tickCount >= 6)
				{
					tickCount=0;
					if(mob.curState().getHunger()<1)
					{
						mob.tell(L("You are no longer hungry."));
					}
					else
					{
						cuds++;
						if(cuds >= getMaxCuds(mob))
						{
							mob.tell(L("You are full."));
							mob.tell(L("You stop grazing."));
							isGrazing=false;
						}
						else
							mob.tell(L("You have filled a stomach (@x1/@x2).",""+cuds,""+getMaxCuds(mob)));
					}
				}
				else
				if(!R.show(mob, null, CMMsg.TYP_NOISYMOVEMENT, L("<S-NAME> continue(s) grazing.")))
				{
					mob.tell(L("You stop grazing."));
					isGrazing=false;
				}
			}
			else
			{
				tickCount=0;
				if(mob.curState().getHunger()<1)
				{
					if(cuds>0)
					{
						final String msgStr=(cuds==1)?
								L("<S-NAME> start(s) chewing the last of <S-HIS-HER> cud."):
								L("<S-NAME> start(s) chewing <S-HIS-HER> cud.");
						if(R.show(mob, null, CMMsg.MSG_QUIETMOVEMENT, msgStr))
						{
							cuds--;
							mob.curState().setHunger(mob.maxState().maxHunger(mob.baseWeight()));
							mob.tell(L("You are no longer hungry."));
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	protected int getMaxCuds(final MOB mob)
	{
		final int maxCuds = 3+super.getXLEVELLevel(mob);
		return maxCuds;
	}

	protected boolean isBusy(final MOB mob)
	{
		for(final Enumeration<Ability> a=mob.personalEffects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)||(A.ID().equalsIgnoreCase("AstroEngineering")))
			&&(!A.isNowAnAutoEffect()))
				return true;
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=mob;
		if(target==null)
			return false;
		Grazing A=(Grazing)target.fetchEffect(ID());
		if((A!=null)&&(A.cuds >= getMaxCuds(mob)))
		{
			mob.tell(L("You are too full to graze."));
			return false;
		}

		if(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		{
			mob.tell(L("This doesn't look like good grazing land."));
			return false;
		}

		if((!auto)&&(mob.isInCombat()))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		if((!auto)&&(isBusy(mob)))
		{
			mob.tell(L("You are too busy right now."));
			return false;
		}

		if((!auto)&&(!CMLib.flags().isAliveAwakeMobileUnbound(mob, false)))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> start(s) grazing here."));
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(A==null)
					A=(Grazing)beneficialAffect(mob,target,asLevel,9999);
				if(A!=null)
				{
					A.isGrazing=true;
					A.makeLongLasting();
				}
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<T-NAME> fumble(s) trying to graze."));

		// return whether it worked
		return success;
	}
}
