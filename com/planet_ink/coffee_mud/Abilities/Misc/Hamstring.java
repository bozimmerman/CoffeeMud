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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
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
public class Hamstring extends StdAbility
{
	@Override
	public String ID()
	{
		return "Hamstring";
	}

	private final static String localizedName = CMLib.lang().L("Hamstring");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Hamstrung)");

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

	private static final String[] triggerStrings =I(new String[] {"HAMSTRING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_RACIALABILITY;
	}

	protected int lostMovement=0;

	@Override
	public void affectCharState(final MOB affected, final CharState affectableMaxState)
	{
		super.affectCharState(affected,affectableMaxState);
		affectableMaxState.setMovement(affectableMaxState.getMovement()-lostMovement);
		if(affectableMaxState.getMovement()<0)
			affectableMaxState.setMovement(0);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final int curDex=affectableStats.getStat(CharStats.STAT_DEXTERITY);
		if(curDex<4)
			affectableStats.setStat(CharStats.STAT_DEXTERITY,0);
		else
			affectableStats.setStat(CharStats.STAT_DEXTERITY,curDex-4);
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((msg.source()==affected)
		&&(!msg.isSource(CMMsg.MASK_ALWAYS)))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_FLEE:
				msg.source().tell(L("You are in too much pain to flee."));
				return false;
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
				if((msg.target() instanceof Room)
				&&(msg.source().isAttributeSet(Attrib.AUTORUN)))
				{
					msg.source().tell(L("You are in too much pain to run."));
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host, msg);
		if(affected instanceof MOB)
		{
			if(msg.target()==affected)
			{
				if((msg.targetMinor()==CMMsg.TYP_HEALING)
				&&(msg.value()>0))
					unInvoke();
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=super.getTarget(mob, commands, givenTarget);
		if(target == null)
			return false;

		if(target.charStats().getBodyPart(Race.BODY_LEG)<1)
		{
			mob.tell(L("@x1 has no legs to hamstring!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,auto?"":L("F^<FIGHT^><S-NAME> attempt(s) to hamstring <T-NAME>.^</FIGHT^>^N"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final int duration = (2*super.adjustedLevel(mob, asLevel)) + (10 * super.getXTIMELevel(mob));
					this.lostMovement=CMLib.dice().roll(adjustedLevel(mob,asLevel), 6, 0) + (5*super.getXLEVELLevel(mob));
					if(this.lostMovement > target.maxState().getMovement())
						this.lostMovement=target.maxState().getMovement();
					if(super.maliciousAffect(mob, target, asLevel, duration, -1)!=null)
					{
						target.recoverCharStats();
						target.recoverMaxState();
						target.recoverPhyStats();
						if(target.curState().getMovement()>target.maxState().getMovement())
							target.curState().setMovement(target.maxState().getMovement());
						target.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> <T-IS-ARE> hamstrung!"));
					}
				}
			}
		}
		else
			return super.maliciousFizzle(mob,null,auto?"":L("<S-NAME> attempt(s) to tag this place, but can't get it out."));
		return success;
	}
}
