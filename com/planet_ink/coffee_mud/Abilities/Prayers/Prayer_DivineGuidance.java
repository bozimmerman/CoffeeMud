package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2011-2018 Bo Zimmerman

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

public class Prayer_DivineGuidance extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_DivineGuidance";
	}

	private final static String localizedName = CMLib.lang().L("Divine Guidance");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Awaiting Divine Guidance)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB))
			return;
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOOD);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10+(2*getXLEVELLevel(invoker())));
		affectableStats.setDamage(affectableStats.damage()+5+getXLEVELLevel(invoker()));
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(canBeUninvoked())
				mob.tell(L("You have received your divine guidance."));
		}
		super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((invoker==null)||(!(affected instanceof MOB)))
			return;
		if(msg.amISource((MOB)affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(!msg.amITarget(affected))
		&&(msg.tool() instanceof Weapon)
		&&(msg.value()>0))
		{
			unInvoke();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L(auto?"<T-NAME> await(s) divine guidance!":"^S<S-NAME> "+prayForWord(mob)+" to give <T-NAME> divine guidance.^?")+CMLib.protocol().msp("bless.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				target.recoverPhyStats();
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for divine guidance, but <S-IS-ARE> not heard.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
