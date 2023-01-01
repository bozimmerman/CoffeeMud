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
   Copyright 2022-2023 Bo Zimmerman

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
public class Fighter_AnkleLock extends FighterGrappleSkill
{
	@Override
	public String ID()
	{
		return "Fighter_AnkleLock";
	}

	private final static String localizedName = CMLib.lang().L("Ankle Lock");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		if(affected==invoker)
			return "(Ankle-Lock)";
		return "(Ankle-Locked)";
	}

	private static final String[] triggerStrings =I(new String[] {"ANKLELOCK"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!CMLib.flags().isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	protected int chanceOfStrength(final MOB mob)
	{
		final MOB invoker=invoker();
		if((invoker != null)&&(mob!=null))
		{
			final int str = 10 * (invoker.charStats().getStat(CharStats.STAT_STRENGTH) - mob.charStats().getStat(CharStats.STAT_STRENGTH));
			if(str < 10)
				return 10;
			return str;
		}
		return 50;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if (affected instanceof MOB)
		{
			final MOB mob = (MOB)affected;
			if(mob != invoker())
			{
				if((tickUp >= 3)
				&&(CMLib.dice().rollPercentage()<chanceOfStrength(mob)))
				{
					LimbDamage dA = (LimbDamage)mob.fetchEffect("BrokenLimbs");
					if(dA == null)
					{
						dA=(LimbDamage)CMClass.getAbility("BrokenLimbs");
						if(dA!=null)
							dA.invoke(invoker(), new XVector<String>("FOOT"), mob, true, -1);
					}
					else
					if(!dA.isDamaged("foot"))
						dA.invoke(invoker(), new XVector<String>("FOOT"), mob, true, -1);
					unInvoke();
					return false;
				}
				if(mob.fetchWieldedItem()!=null)
					CMLib.commands().postRemove(mob, mob.fetchWieldedItem(), false);
			}
		}
		return true;
	}

	@Override
	protected boolean isHandsFree()
	{
		return true;
	}

	@Override
	protected boolean hasWeightLimit()
	{
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.source()==affected)
		{
			if(msg.target()==pairedWith)
			{
				if((msg.tool() instanceof FighterGrappleSkill)
				&&(msg.source().isMine(msg.tool())))
				{
					unInvoke();
					// uniquely OK
					return true;
				}
			}
		}
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.charStats().getBodyPart(Race.BODY_FOOT)<1)
		{
			mob.tell(L("@x1 has no feet!",target.name(mob)));
			return false;
		}

		if((!auto)&&(CMLib.flags().isStanding(target))&&(mob!=target))
		{
			mob.tell(L("Your target must be sitting!"));
			return false;
		}

		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;

		// now see if it worked
		final boolean hit=(auto)
						||(super.getGrappleA(target)!=null)
						||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,0,auto)&&(hit);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					auto?L("<T-NAME> get(s) <T-HIMHERSELF> in a(n) "+name().toLowerCase()+"!"):
						L("^F^<FIGHT^><S-NAME> put(s) <T-NAME> in a "+name().toLowerCase()+"!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success = finishGrapple(mob,4,target, asLevel);
				else
					return maliciousFizzle(mob,target,L("<T-NAME> fight(s) off <S-YOUPOSS> ankle-locking move."));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to put <T-NAME> in a "+name()+", but fail(s)."));

		// return whether it worked
		return success;
	}
}
