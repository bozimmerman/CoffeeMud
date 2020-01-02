package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class FoodBegging extends StdAbility
{
	@Override
	public String ID()
	{
		return "FoodBegging";
	}

	private final static String localizedName = CMLib.lang().L("Food Begging");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BEG4FOOD","BEGFORFOOD","FOODBEGGING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}


	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		if(target.isInCombat())
		{
			mob.tell(L("Not during combat!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(!CMLib.flags().canBeSeenBy(mob, target))
			success=false;

		if(success)
		{
			final String[] emotes={
				"<S-NAME> rub(s) up against <T-YOUPOSS> leg.",
				"<S-NAME> make(s) cute little noises and stare(s) at <T-NAME> with wide eyes.",
				"<S-NAME> sit(s) pretty, staring at <T-NAME>.",
				"<S-NAME> roll(s) around on the ground, making cute little noises at <T-NAME>.",
				"<S-NAME> look(s) up at <T-NAME> with big sad eyes.",
				"<S-NAME> watch(es) <T-NAME> with sad hungry eyes.",
				"<S-YOUPOSS> tummy rumble(s) as <S-HE-SHE> spot(s) <T-NAME>.",
			};
			final String emote = emotes[CMLib.dice().roll(1, emotes.length, -1)];

			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?0:CMMsg.MASK_MALICIOUS)|CMMsg.MASK_HANDS|CMMsg.TYP_MIND,L(auto?"":emote));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.isInCombat())
					target.makePeace(true);
				if(mob.isInCombat())
					mob.makePeace(true);
				if(msg.value() <= 0)
				{
					Item tempF=null;
					Item backupF=null;
					for(final Enumeration<Item> i=target.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if(I instanceof Food)
						{
							if(I.container()!=null)
								backupF=I;
							else
							{
								tempF=I;
								break;
							}
						}
					}
					if(tempF==null)
						tempF=backupF;
					if(tempF != null)
					{
						final Item finalF=tempF;
						CMLib.threads().scheduleRunnable(new Runnable()
						{
							final MOB M=mob;
							final Room R=mob.location();
							final MOB tM=target;
							final Item food=finalF;
							@Override
							public void run()
							{
								if(R.isInhabitant(M) && R.isInhabitant(tM))
								{
									finalF.setContainer(null);
									if(!CMLib.commands().postGive(tM, mob, food, false))
									{
										final Command feedC=CMClass.getCommand("Feed");
										if(feedC != null)
										{
											try
											{
												feedC.execute(tM, new XVector<String>(
													"FEED",mob.Name(),finalF.name()), 0);
											}
											catch (final IOException e)
											{
												Log.errOut(e);
											}
										}
									}
								}
							}
						}, CMProps.getTickMillis());
					}
					else
						target.tell(L("You feel lousy for not having anything to offer @x1",mob.name(target)));

				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<T-NAME> ignore(s) <S-NAME>."));
		// return whether it worked
		return success;
	}
}
