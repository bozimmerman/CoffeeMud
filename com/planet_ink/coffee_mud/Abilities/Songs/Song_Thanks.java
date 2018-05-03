package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Song_Thanks extends Song
{
	@Override
	public String ID()
	{
		return "Song_Thanks";
	}

	private final static String localizedName = CMLib.lang().L("Thanks");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected boolean skipStandardSongInvoke()
	{
		return true;
	}

	@Override
	protected boolean maliciousButNotAggressiveFlag()
	{
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat()&&(mob.isMonster()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return true;
		if(mob==invoker)
			return true;
		if(invoker==null)
			return true;
		if(mob.location()!=invoker.location())
			return true;
		//if(!mob.isMonster()) return true;
		if((CMLib.dice().rollPercentage()<6)
		   &&(CMLib.dice().rollPercentage()>mob.charStats().getSave(CharStats.STAT_SAVE_MIND))
		   &&(CMLib.dice().rollPercentage()>mob.charStats().getSave(CharStats.STAT_SAVE_MAGIC))
		   &&(CMLib.flags().canMove(mob))
		   &&(CMLib.flags().canBeSeenBy(invoker,mob))
		   &&(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)>(1.0+super.getXLEVELLevel(invoker()))))
		{
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
				CMLib.commands().postSay(mob,invoker,L("Thank you @x1!",invoker.name()),false,false);
				break;
			case 2:
				CMLib.commands().postSay(mob,invoker,L("Thanks for being you, @x1!",invoker.name()),false,false);
				break;
			case 3:
				CMLib.commands().postSay(mob,invoker,L("Thanks @x1!",invoker.name()),false,false);
				break;
			case 4:
				CMLib.commands().postSay(mob,invoker,L("You are great, @x1!  Thanks!",invoker.name()),false,false);
				break;
			case 5:
				CMLib.commands().postSay(mob,invoker,L("I appreciate you, @x1!",invoker.name()),false,false);
				break;
			case 6:
				CMLib.commands().postSay(mob,invoker,L("Keep it up, @x1! Thanks!",invoker.name()),false,false);
				break;
			case 7:
				CMLib.commands().postSay(mob,invoker,L("Thanks a lot, @x1!",invoker.name()),false,false);
				break;
			case 8:
				CMLib.commands().postSay(mob,invoker,L("Thank you dearly, @x1!",invoker.name()),false,false);
				break;
			case 9:
				CMLib.commands().postSay(mob,invoker,L("Thank you always, @x1!",invoker.name()),false,false);
				break;
			case 10:
				CMLib.commands().postSay(mob,invoker,L("You're the best, @x1! Thanks!",invoker.name()),false,false);
				break;
			}
			final Coins C=CMLib.beanCounter().makeBestCurrency(mob,CMath.mul(1.0,super.getXLEVELLevel(invoker())));
			if(C!=null)
			{
				CMLib.beanCounter().subtractMoney(mob,CMath.mul(1.0,super.getXLEVELLevel(invoker())));
				mob.addItem(C);
				mob.doCommand(CMParms.parse("GIVE \""+C.name()+"\" \""+invoker.name()+"\""),MUDCmdProcessor.METAFLAG_FORCED);
				if(!C.amDestroyed())
					C.putCoinsBack();
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell(L("You can't sing!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);
		unsingAllByThis(mob,mob);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?L("^SThe @x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to sing the @x1.^?",songOf());
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str=L("^S<S-NAME> start(s) the @x1 over again.^?",songOf());

			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.get(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(mob.location().okMessage(mob,msg))
				{
					final Set<MOB> h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null)
						continue;
					final Song newOne=(Song)this.copyOf();

					for (final Object element : h)
					{
						final MOB follower=(MOB)element;

						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if(auto)
							affectType=affectType|CMMsg.MASK_ALWAYS;

						if((CMLib.flags().canBeHeardSpeakingBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
						{
							final CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							final CMMsg msg3=msg2;
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								follower.location().send(follower,msg2);
								if(msg2.value()<=0)
								{
									follower.location().send(follower,msg3);
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
									{
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
								}
							}
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> hit(s) a foul note."));

		return success;
	}
}
