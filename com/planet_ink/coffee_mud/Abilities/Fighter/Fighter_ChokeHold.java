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
   Copyright 2022-2024 Bo Zimmerman

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
public class Fighter_ChokeHold extends FighterGrappleSkill
{
	@Override
	public String ID()
	{
		return "Fighter_ChokeHold";
	}

	private final static String localizedName = CMLib.lang().L("Choke Hold");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		if(affected==invoker)
			return "(Choke-Holding)";
		return "(Choke Held)";
	}

	private static final String[] triggerStrings =I(new String[] {"CHOKEHOLD"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected != invoker())&&(pairedWith==invoker()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		// choke is more restrictive, can't talk
		if(msg.source()==affected)
		{
			if(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			{
				if(msg.sourceMajor(CMMsg.MASK_MOUTH))
				{
					if(msg.sourceMessage()!=null)
						msg.source().tell(L("You are in a(n) "+name().toLowerCase()+"!"));
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!CMLib.flags().isStanding(mob))&&(CMLib.flags().isStanding(mob)))
		{
			mob.tell(L("You need to stand up first!"));
			return false;
		}

		if(target.charStats().getBodyPart(Race.BODY_NECK)==0)
		{
			mob.tell(L("@x1 has no neck!",target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;

		final boolean hit=(auto)||(super.getGrappleA(target)!=null)
				||(CMLib.combat().rollToHit(mob,target));
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
					return maliciousFizzle(mob,target,L("<T-NAME> fight(s) off <S-YOUPOSS> choking move."));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to put <T-NAME> in a "+name().toLowerCase()+", but fail(s)."));

		// return whether it worked
		return success;
	}
}
