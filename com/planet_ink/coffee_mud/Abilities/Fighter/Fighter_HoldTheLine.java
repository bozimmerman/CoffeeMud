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
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_HoldTheLine extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_HoldTheLine";
	}

	private final static String localizedName = CMLib.lang().L("Hold The Line");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HOLDTHELINE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(5);
	}

	protected volatile boolean alreadyAttacked = false;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
		{
			if(msg.source()==affected)
			{
				unInvoke();
				return true;
			}
			final Room R = msg.source().location();
			if(R == null)
				return true;
			if((msg.source().getVictim()==affected)
			&&(msg.source().isInCombat())
			&&(proficiencyCheck(msg.source(),0,false)))
			{
				if(CMLib.dice().rollPercentage()<20)
					super.helpProficiency(msg.source(), 0);
				R.show((MOB)affected, msg.source(), this, CMMsg.MSG_NOISYMOVEMENT, L("^F^<FIGHT^><S-NAME> hold(s) the line against <T-NAME>.^</FIGHT^>^?"));
				return false;
			}
		}
		else
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK))
		{
			if(alreadyAttacked)
				return false;
			alreadyAttacked=true;
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		alreadyAttacked=false;
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
				((MOB)affected).tell(L("Your ability to hold the line wavers."));
		}
		super.unInvoke();
	}

	@Override
	public int castingQuality(final MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			target=mob.getVictim();
			if(target == null)
				return Ability.QUALITY_INDIFFERENT;
			final Item I = mob.fetchWieldedItem();
			if(!(I instanceof Weapon))
				return Ability.QUALITY_INDIFFERENT;
			final Weapon W = (Weapon)I;
			if((!W.rawLogicalAnd())
			|| (W.maxRange()<1)
			|| (!W.amBeingWornProperly())
			|| (W.weaponClassification()!=Weapon.CLASS_POLEARM))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target = mob.getVictim();
		if((target == null)
		||((target.location()!=mob.location())))
		{
			mob.tell(L("You must be in combat to do that."));
			return false;
		}
		final Item I = mob.fetchWieldedItem();
		if(!(I instanceof Weapon))
		{
			mob.tell(L("You can not do this with @x1.",I.name(mob)));
			return false;
		}
		if(mob.riding() != null)
		{
			mob.tell(L("You can't do that while @x1 @x2.",mob.riding().getMountString(),mob.riding().name(mob)));
			return false;
		}
		final Weapon W = (Weapon)I;
		if((!W.rawLogicalAnd())
		|| (W.maxRange()<1)
		|| (!W.amBeingWornProperly())
		|| (W.weaponClassification()!=Weapon.CLASS_POLEARM))
		{
			mob.tell(L("You can not do that with @x1.  Hold The Line requires a long two-handed polearm-class weapon."));
			return false;
		}

		if(mob.rangeToTarget() < W.maxRange())
		{
			mob.tell(L("Your target is too close."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,
											L("^F^<FIGHT^><S-NAME> hold(s) the line with @x1!^</FIGHT^>^?",W.name(mob)));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,2+(super.getXLEVELLevel(mob)/3));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to hold the line, but fail(s)."));

		// return whether it worked
		return success;
	}
}
