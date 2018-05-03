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

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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

public class Skill_Shush extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Shush";
	}

	private final static String	localizedName	= CMLib.lang().L("Shush");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Feeling shushed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHUSH" });

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
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA|USAGE_MOVEMENT;
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.source()==affected)
		{
			
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null)
			&&(msg.sourceMessage().indexOf(L(" whisper(s) "))<0))
			{
				final String say=CMStrings.getSayFromMessage(msg.sourceMessage());
				if(say!=null)
				{
					if(msg.target()!=null)
					{
						CMLib.commands().forceStandardCommand(msg.source(), "WHISPER", new XVector<String>(new String[]{
							"WHISPER",msg.target().Name(),say
						}));
					}
					else
					{
						CMLib.commands().forceStandardCommand(msg.source(), "WHISPER", new XVector<String>(new String[]{
							"WHISPER",say
						}));
					}
				}
				return false;
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.sourceMajor(CMMsg.MSK_CAST_VERBAL)))
			{
				msg.source().tell(L("You don't feel comfortable making loud noises right now."));
				return false;
			}
			
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected instanceof MOB)
		&&(((MOB)affected).isInCombat()))
			unInvoke();
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("You feel free to speak again."));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if((target==null)||(R==null))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int twis = target.charStats().getStat(CharStats.STAT_WISDOM);
		final int scha = mob.charStats().getStat(CharStats.STAT_CHARISMA);
		final boolean success=(!target.isInCombat()) && proficiencyCheck(mob,-((twis-scha)*2)+getXLEVELLevel(mob),auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MASK_HANDS|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("<S-NAME> shush(es) <T-NAMESELF>."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)/10)+1+((2*getXLEVELLevel(mob))/3),CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0));
				if(target.fetchEffect(ID())!=null)
					R.show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> shushed!"));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> shush(es) <T-NAMESELF>, but <T-NAME> just seem(s) annoyed."));
		return success;
	}
}
