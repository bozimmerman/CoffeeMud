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

public class Skill_Meditation extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Meditation";
	}

	private final static String localizedName = CMLib.lang().L("Meditation");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Meditating)");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"MEDITATE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> end(s) <S-HIS-HER> meditation."));
				else
					mob.tell(L("Your meditation ends."));
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))||(CMath.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH))))
			unInvoke();
		if(CMath.bset(msg.othersMajor(),CMMsg.MASK_SOUND)
		   &&(CMLib.flags().canBeHeardMovingBy(msg.source(),mob)))
		{
			if(!msg.amISource(mob))
				msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_HANDS,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,L("Your meditation is interrupted by the noise.")));
			else
				msg.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.TYP_GENERAL|CMMsg.MASK_HANDS,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,L("Your meditation is interrupted.")));
		}
		return;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		final MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!proficiencyCheck(null,0,false))
			return true;

		if((mob.curState().getHunger()<=0)
		||(mob.curState().getThirst()<=0))
		{
			if(mob.curState().getThirst()<=0)
				mob.tell(L("Your mouth is dry!"));
			else
				mob.tell(L("Your stomach growls!"));
			unInvoke();
			return false;
		}

		if((!mob.isInCombat())
		&&(CMLib.flags().isSitting(mob)))
		{
			final double man=((mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)
							+(2*getXLEVELLevel(mob))
							+mob.charStats().getStat(CharStats.STAT_WISDOM)));
			mob.curState().adjMana( (int)Math.round( ( man * .1 ) + ( ( mob.phyStats().level() + ( 2.0 * getXLEVELLevel( mob ) ) ) / 2.0 ) ),
									mob.maxState() );
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=mob;
		if(mob.isInCombat())
		{
			mob.tell(L("You can't meditate while in combat!"));
			return false;
		}
		if(!CMLib.flags().isSitting(mob))
		{
			mob.tell(L("You must be in a sitting, restful position to meditate."));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("You are already meditating!"));
			return false;
		}
		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("<S-NAME> begin(s) to meditate..."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,Ability.TICKS_FOREVER);
				helpProficiency(mob, 0);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to meditate, but lose(s) concentration."));

		// return whether it worked
		return success;
	}
}
