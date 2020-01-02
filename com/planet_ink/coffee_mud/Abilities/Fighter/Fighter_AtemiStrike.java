package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2003-2020 Bo Zimmerman

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
public class Fighter_AtemiStrike extends MonkSkill
{
	@Override
	public String ID()
	{
		return "Fighter_AtemiStrike";
	}

	private final static String localizedName = CMLib.lang().L("Atemi Strike");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Atemi Strike)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"ATEMI"});
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
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_PUNCHING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected volatile int tickUp = 0;
	protected volatile int tickTarget = 0;

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
			if((!mob.amDead())
			&&((tickTarget<=0)||(tickUp==0)||(tickUp>=tickTarget)))
				CMLib.combat().postDeath(invoker,mob,null);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		tickUp++;
		return true;
	}
	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(mob.baseWeight()<(((MOB)target).baseWeight()/2)))
				return Ability.QUALITY_INDIFFERENT;
			if(anyWeapons(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isGolem(target))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_HAND)<=0)
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away from your target to strike!"));
			return false;
		}

		if((!auto)&&(mob.baseWeight()<(target.baseWeight()/2)))
		{
			mob.tell(L("@x1 is too big to strike!",target.name(mob)));
			return false;
		}

		if((!auto)&&(anyWeapons(mob)))
		{
			mob.tell(L("You must be unarmed to perform the strike."));
			return false;
		}

		if(CMLib.flags().isGolem(target))
		{
			mob.tell(target,null,null,L("You can't hurt <S-NAMESELF> with Atemi Strike."));
			return false;
		}

		if(mob.charStats().getBodyPart(Race.BODY_HAND)<=0)
		{
			mob.tell(L("You need hands to do this."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*20;
		else
			levelDiff=0;
		// now see if it worked
		final boolean hit=(auto)||(CMLib.combat().rollToHit(mob,target));
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_STRENGTH)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto)&&(hit);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("<T-NAME> hit(s) the floor!"):L("^F^<FIGHT^><S-NAME> deliver(s) a deadly Atemi strike to <T-NAMESELF>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> do(es) not look well."));
					final int ticks = 20 - (getXLEVELLevel(mob)/2);
					final Fighter_AtemiStrike strike = (Fighter_AtemiStrike)maliciousAffect(mob,target,asLevel,ticks,-1);
					strike.tickUp=0;
					strike.tickTarget = ticks/2;
					success=strike!=null;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) the deadly Atemi strike on <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
