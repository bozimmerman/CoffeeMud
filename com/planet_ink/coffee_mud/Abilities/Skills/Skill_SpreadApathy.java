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
   Copyright 2022-2025 Bo Zimmerman

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
public class Skill_SpreadApathy extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_SpreadApathy";
	}

	private final static String	localizedName	= CMLib.lang().L("Spread Apathy");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(Spreading Apathy)";
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SPREADAPATHY", "SAPATHY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected Ability apathyA = null;
	protected Ability getDisease()
	{
		if(apathyA == null)
		{
			apathyA = CMClass.getAbility("Disease_Apathy");
			apathyA.makeLongLasting();
		}
		apathyA.setAffectedOne(affected);
		return apathyA;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if ((msg.target() == affected)
		&& (msg.tool() instanceof Ability )
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&&(msg.tool().ID().equals("Disease_Apathy")))
			return false;
		if(!getDisease().okMessage(myHost, msg))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(CMLib.flags().canBeHeardSpeakingBy(msg.source(), (MOB)msg.target()))
		&&(!msg.source().mayIFight((MOB)msg.target())))
			return;
		getDisease().executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		getDisease().tick(ticking, tickID);
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats stats)
	{
		super.affectPhyStats(affected,stats);
		stats.addAmbiance("^kapathetic^?");
	}

	@Override
	public void unInvoke()
	{
		final MOB invoker=this.invoker();
		super.unInvoke();
		if((invoker!=null)&&(this.unInvoked))
			invoker.tell(L("You are no longer aggressively apathetic."));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;

		final Ability oldA = mob.fetchEffect(ID());
		if(oldA != null)
		{
			oldA.unInvoke();
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("<S-NAME> become(s) aggressively apathetic.");
			final CMMsg msg=CMClass.getMsg(mob,mob,this,CMMsg.MSG_QUIETMOVEMENT,str,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_QUIETMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Ability A=super.beneficialAffect(mob, mob, asLevel, 0);
				if(A != null)
					A.makeLongLasting();
			}
		}
		else
			return beneficialWordsFizzle(mob,mob,L("<S-NAME> attempt(s) to go aggressively apathetic, but fail(s)."));

		// return whether it worked
		return success;
	}
}
