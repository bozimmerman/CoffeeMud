package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2001-2018 Bo Zimmerman

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

public class Fighter_Whomp extends FighterSkill implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Fighter_Whomp";
	}

	private final static String localizedName = CMLib.lang().L("Whomp");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(knocked out)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"WHOMP"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Unconscious";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if((msg.sourceMajor(CMMsg.MASK_EYES))
			||(msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
					mob.tell(L("You are way too drowsy."));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> seem(s) less drowsy."));
				else
					mob.tell(L("You feel less drowsy."));
				CMLib.commands().postStand(mob,true);
			}
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getStat(CharStats.STAT_STRENGTH)<CMProps.getIntVar(CMProps.Int.BASEMAXSTAT))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(mob.baseWeight()<(((MOB)target).baseWeight()-250)))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away from your target to whomp!"));
			return false;
		}
		if((!auto)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)<CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)))
		{
			mob.tell(L("You need at least an @x1 strength to do that.",""+CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)));
			return false;
		}

		if((!auto)&&(mob.baseWeight()<(target.baseWeight()-250)))
		{
			mob.tell(L("@x1 is way too big to knock out!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;
		// now see if it worked
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto)&&(hit);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),L(auto?"<T-NAME> hit(s) the floor!":"^F<S-NAME> knock(s) <T-NAMESELF> to the floor!^?"+CMLib.protocol().msp("bashed2.wav",30)));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,asLevel,2,-1)!=null;
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to knock <T-NAMESELF> out, but fail(s)."));

		// return whether it worked
		return success;
	}
}
