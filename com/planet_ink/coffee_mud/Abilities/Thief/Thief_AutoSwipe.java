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
public class Thief_AutoSwipe extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_AutoSwipe";
	}

	@Override
	public String displayText()
	{
		return L("(Autoswiping)");
	}

	private final static String localizedName = CMLib.lang().L("AutoSwipe");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"AUTOSWIPE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	protected Thief_Swipe swipeA = null;
	protected int risk = 90;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(((MOB)affected).location()!=null)
		&&(!msg.source().isInCombat()))
		{
			if(swipeA == null)
			{
				swipeA=(Thief_Swipe)msg.source().fetchAbility("Thief_Swipe");
			}
			final Room R=(Room)msg.target();
			if((R.numInhabitants()>1)
			&&(swipeA!=null))
			{
				final MOB mob=msg.source();
				final List<MOB> elligible=new ArrayList<MOB>(2);
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)
					&&(M!=mob)
					&&(swipeA.getAllowedSwipe(mob, M))
					)
					{
						elligible.add(M);
					}
				}
				if(elligible.size()>1)
				{
					for(final Iterator<MOB> i=elligible.iterator();i.hasNext();)
					{
						final MOB M=i.next();
						if((M!=null)
						&&(swipeA.getDiscoverChance(mob, M)<risk))
							i.remove();
					}
				}
				if(elligible.size()>1)
				{
					CMLib.threads().scheduleRunnable(new Runnable()
					{
						final MOB M=elligible.get(CMLib.dice().roll(1, elligible.size(), -1));
						final Room R=M.location();
						final Ability swiperA=swipeA;
						final MOB invokM=mob;
						@Override
						public void run()
						{
							if((swiperA!=null)
							&&(M!=null)
							&&(invokM!=null)
							&&(invokM.location()==M.location())
							&&(M.location()==R))
							{
								swiperA.invoke(mob, new XVector<String>("$"+M.Name()+"$"), null, false, 0);
							}
						}
					}, 500);
				}
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		int risk = 90;
		if((commands.size()>0) && CMath.isPct(CMParms.combine(commands,0)))
		{
			risk = (int)Math.round(CMath.s_pct(CMParms.combine(commands,0)) * 100.0);
			if(target.fetchEffect(ID())!=null)
			{
				final Thief_AutoSwipe swipeA=(Thief_AutoSwipe)target.fetchEffect(ID());
				swipeA.risk = risk;
				target.tell(L("Your safety factor has been changed to " + risk + "%."));
				return false;
			}
		}
		if(target.fetchEffect(ID())!=null)
		{
			target.tell(L("You are no longer automatically swiping from folk."));
			target.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if((!auto)
		&&(target.fetchAbility("Thief_Swipe")==null))
		{
			target.tell(L("You don't know how to swipe yet!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			target.tell(L("You will now automatically swipe when you enter a room."));
			beneficialAffect(mob,target,asLevel,0);
			final Thief_AutoSwipe A=(Thief_AutoSwipe)mob.fetchEffect(ID());
			if(A!=null)
			{
				A.makeLongLasting();
				A.risk=risk;
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to casually auto-swipe, but can't seem to get into the spirit of it."));
		return success;
	}
}
