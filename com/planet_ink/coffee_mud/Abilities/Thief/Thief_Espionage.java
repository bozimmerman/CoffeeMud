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
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
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

public class Thief_Espionage extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Espionage";
	}

	private final static String localizedName = CMLib.lang().L("Espionage");

	@Override
	public String name()
	{
		return localizedName;
	}
		// can NOT have a display text since the ability instance
		// is shared between the invoker and the target
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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"ESPIONAGE"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.othersMajor(CMMsg.MASK_CHANNEL)))
		{
			final int channelInt=msg.othersMinor()-CMMsg.TYP_CHANNEL;
			final ChannelsLibrary.CMChannel chan=CMLib.channels().getChannel(channelInt);
			final boolean areareq=chan.flags().contains(ChannelsLibrary.ChannelFlag.SAMEAREA);
			if((chan.flags().contains(ChannelsLibrary.ChannelFlag.CLANONLY)
				||chan.flags().contains(ChannelsLibrary.ChannelFlag.CLANALLYONLY))
			&&(invoker()!=null)
			&&((!CMLib.clans().isAnyCommonClan(invoker(),msg.source()))
			&&((!chan.flags().contains(ChannelFlag.CLANALLYONLY))
				||(!CMLib.clans().findAnyClanRelations(invoker(),msg.source(),Clan.REL_ALLY))))
			&&(!CMLib.channels().mayReadThisChannel(msg.source(),areareq,invoker(),channelInt)))
				invoker().executeMsg(myHost,msg);
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((invoker!=null)&&(affected!=null))
				invoker.tell(L("You are no longer committing espionage with @x1.",affected.name()));
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Commit espionage through whom?"));
			return false;
		}
		final MOB target=this.getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;
		if(target==mob)
		{
			mob.tell(L("You cannot do that with yourself?!"));
			return false;
		}
		final Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			if(A.invoker()==mob)
			{
				A.unInvoke();
				return false;
			}
			else
			{
				mob.tell(mob,target,null,L("It is too crowded to commit espionage with <T-NAME>."));
				return false;
			}
		}
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(L("@x1 is watching you too closely.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));

		final boolean success=proficiencyCheck(mob,-(levelDiff*10),auto);

		if(!success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":L("Your attempt to commit espionage using <T-NAMESELF> fails; <T-NAME> spots you!"),CMMsg.MSG_OK_VISUAL,auto?"":L("You spot <S-NAME> trying to commit espionage through you."),CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_THIEF_ACT,L("You are now committing espionage with <T-NAME>.  Enter 'espionage <targetname>' again to disengage."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		return success;
	}
}
