package com.planet_ink.coffee_mud.Abilities.SuperPowers;
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
   Copyright 2005-2018 Bo Zimmerman

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

public class Power_WebSpinning extends SuperPower
{
	@Override
	public String ID()
	{
		return "Power_WebSpinning";
	}

	private final static String localizedName = CMLib.lang().L("Web Spinning");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Webbed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(5);
	}

	@Override
	public int minRange()
	{
		return 1;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS|CAN_ITEMS|CAN_EXITS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS|CAN_ITEMS|CAN_EXITS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_BINDING;
	}

	public int amountRemaining=0;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BOUND);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if(msg.amISource(mob))
			{
				if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&((msg.sourceMajor(CMMsg.MASK_HANDS))
				||(msg.sourceMajor(CMMsg.MASK_MOVE))))
				{
					if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against the web.")))
					{
						amountRemaining-=(mob.charStats().getStat(CharStats.STAT_STRENGTH)+mob.phyStats().level());
						if(amountRemaining<0)
							unInvoke();
					}
					return false;
				}
			}
		}
		else
		if(affected instanceof Item)
		{
			if(msg.target()==affected)
			{
				if(msg.targetMinor()==CMMsg.TYP_GET)
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> is covered in sticky webbing!"),null,null));
				else
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(((Item)affected).owner()==msg.source()))
				{
					msg.source().tell(msg.source(),affected,null,L("<T-NAME> is too sticky to let go of!"));
					return false;
				}
			}
		}
		else
		if(affected instanceof Exit)
		{
			if(msg.target()==affected)
			{
				if(msg.targetMinor()==CMMsg.TYP_OPEN)
				{
					msg.source().tell(msg.source(),affected,null,L("<T-NAME> is held fast by gobs of webbing!"));
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
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
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> manage(s) to break <S-HIS-HER> way free of the web."));
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,L(auto?"":"^S<S-NAME> shoot(s) and spin(s) a web at <T-NAMESELF>!^?")+CMLib.protocol().msp("web.wav",40));
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					amountRemaining=160;
					if(CMLib.map().roomLocation(target)==mob.location())
					{
						success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1)!=null;
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,L("<T-NAME> become(s) stuck in a mass of web!"));
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> spin(s) a web towards <T-NAMESELF>, but miss(es)."));

		// return whether it worked
		return success;
	}
}
