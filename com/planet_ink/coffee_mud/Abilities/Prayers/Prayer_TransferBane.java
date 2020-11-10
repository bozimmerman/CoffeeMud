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
   Copyright 2020-2020 Bo Zimmerman

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
public class Prayer_TransferBane extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_TransferBane";
	}

	private final static String localizedName = CMLib.lang().L("Transfer Bane");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	protected List<Ability> getBanes(final MOB mob, final MOB target)
	{
		final List<Ability> cursesV=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof Prayer)
			&&(A.canBeUninvoked())
			&&(A.castingQuality(mob,target)==Ability.QUALITY_MALICIOUS))
				cursesV.add(A);
		}
		return cursesV;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			final List<Ability> cursesV=getBanes(mob, (MOB)target);
			if(cursesV.size()==0)
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

		final List<Ability> cursesV=getBanes(mob,target);
		if(cursesV.size()==0)
		{
			mob.tell(L("You lack any banes to transfer."));
			return false;
		}
		final Ability curseA = cursesV.get(CMLib.dice().roll(1, cursesV.size(), -1));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?L("<T-NAME> <T-IS-ARE> cursed!"):L("^S<S-NAME> curse(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final MOB curserM;
					if((curseA.invoker()!=null)
					&&(curseA.invoker()!=target)
					&&(curseA.invoker().mayIFight(target)))
						curserM=curseA.invoker();
					else
						curserM=mob;
					final Ability A=CMClass.getAbility(curseA.ID());
					A.invoke(curserM, target, true, asLevel);
					final Ability effA=target.fetchEffect(curseA.ID());
					if(effA!=null)
					{
						effA.setInvoker(curseA.invoker());
						effA.setExpirationDate(curseA.expirationDate());
						target.recoverPhyStats();
						curseA.unInvoke();
					}
					else
						return maliciousFizzle(mob,target,L("<S-YOUPOSS> attempt to curse <T-NAMESELF> fails."));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to curse <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
