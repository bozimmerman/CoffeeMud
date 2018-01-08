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
   Copyright 2006-2018 Bo Zimmerman

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

public class Skill_Befriend extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Befriend";
	}

	private final static String	localizedName	= CMLib.lang().L("Befriend");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BEFRIEND" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (msg.amITarget(affected))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_LIST:
				if(affected instanceof MOB)
				{
					MOB M=(MOB)affected;
					final MOB srcM=msg.source();
					if(srcM==M.amFollowing())
					{
						srcM.recoverCharStats();
						srcM.charStats().setStat(CharStats.STAT_CHARISMA, srcM.charStats().getStat(CharStats.STAT_CHARISMA) + 10);
						msg.addTrailerRunnable(new Runnable()
						{
							@Override
							public void run()
							{
								srcM.recoverCharStats();
							}
							
						});
					}
					else
					if((text().length()>0)&&(text().startsWith(srcM.Name())))
					{
						String name = text();
						int amt=10;
						int x=text().indexOf('=');
						if(x>0)
						{
							name = text().substring(0,x).trim();
							amt=CMath.s_int(text().substring(x+1).trim());
						}
						if(name.equals(srcM.Name()))
						{
							final MOB theMob = srcM;
							final int oldCha = srcM.baseCharStats().getStat(CharStats.STAT_CHARISMA);
							srcM.baseCharStats().setStat(CharStats.STAT_CHARISMA, oldCha + amt);
							srcM.recoverCharStats();
							msg.addTrailerRunnable(new Runnable()
							{
								final MOB mob=theMob;
								final int cha=oldCha;
								@Override
								public void run()
								{
									mob.baseCharStats().setStat(CharStats.STAT_CHARISMA, cha);
									mob.recoverCharStats();
								}
							});
						}
					}
				}
				break;
			case CMMsg.TYP_NOFOLLOW:
				if(affected instanceof MOB)
				{
					MOB M=(MOB)affected;
					final MOB srcM=msg.source();
					if(srcM==M.amFollowing())
						unInvoke();
				}
				break;
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}
	
	@Override 
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking instanceof MOB)
		&&((text().length()==0)||(text().indexOf('=')<0)))
		{
			final MOB mob=(MOB)ticking;
			if((mob.amFollowing()==null)
			||(mob.amFollowing().isMonster())
			||(!CMLib.flags().isInTheGame(mob.amFollowing(), true)))
			{
				if(mob.getStartRoom()==null)
					mob.destroy();
				else
				if(mob.getStartRoom() != mob.location())
					CMLib.tracking().wanderAway(mob, false, true);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("You must specify someone to befriend!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target==mob)
		{
			mob.tell(L("You are already your own friend."));
			return false;
		}
		if(target.phyStats().level()>mob.phyStats().level()+(mob.phyStats().level()/10))
		{
			mob.tell(L("@x1 is a bit too powerful to befriend.",target.charStats().HeShe()));
			return false;
		}
		if(!CMLib.flags().isMobile(target))
		{
			mob.tell(L("You can only befriend fellow travellers."));
			return false;
		}

		if(CMLib.coffeeShops().getShopKeeper(target)!=null)
		{
			mob.tell(L("You cann't befriend a merchant."));
			return false;
		}

		if(!target.isMonster())
		{
			mob.tell(L("You need to ask @x1",target.charStats().himher()));
			return false;
		}

		if(target.amFollowing()!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> is already someone elses friend."));
			return false;
		}

		if(!target.charStats().getMyRace().racialCategory().equals(mob.charStats().getMyRace().racialCategory()))
		{
			mob.tell(target,null,null,L("<S-NAME> is not a fellow @x1.",mob.charStats().getMyRace().racialCategory()));
			return false;
		}

		final Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
		if(F!=null)
		{
			final int his=target.fetchFaction(F.factionID());
			final int mine=target.fetchFaction(F.factionID());
			if(F.fetchRange(his)!=F.fetchRange(mine))
			{
				mob.tell(target,null,null,L("<S-NAME> is not @x1, like yourself.",F.fetchRangeName(mine)));
				return false;
			}
		}

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell(L("You can't speak!"));
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!CMLib.flags().canBeHeardSpeakingBy(mob,target)))
		{
			mob.tell(L("@x1 can't hear your words.",target.charStats().HeShe()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=mob.phyStats().level()-target.phyStats().level();
		if(levelDiff>0)
			levelDiff=(-(levelDiff*levelDiff))/(1+super.getXLEVELLevel(mob));
		else
			levelDiff=(levelDiff*(-levelDiff))/(1+super.getXLEVELLevel(mob));

		final boolean success=proficiencyCheck(mob,levelDiff,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> befriend(s) <T-NAME>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.commands().postFollow(target,mob,false);
				CMLib.combat().makePeaceInGroup(mob);
				if(target.amFollowing()!=mob)
					mob.tell(L("@x1 seems unwilling to be your friend.",target.name(mob)));
				else
				{
					Ability A=super.beneficialAffect(mob, target, asLevel, Ability.TICKS_FOREVER);
					if(A!=null)
					{
						A.makeNonUninvokable();
						A.makeLongLasting();
						A.setSavable(true);
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to befriend <T-NAMESELF>, but fail(s)."));

		return success;
	}

}
