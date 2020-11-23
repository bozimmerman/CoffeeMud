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
public class Prayer_TransferBoon extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_TransferBoon";
	}

	private final static String localizedName = CMLib.lang().L("Transfer Boon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	protected List<Ability> getBoons(final MOB mob, final MOB target)
	{
		final List<Ability> blessingV=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(A.canBeUninvoked())
			&&((A.castingQuality(mob,target)==Ability.QUALITY_BENEFICIAL_OTHERS)
				||(A.castingQuality(mob,target)==Ability.QUALITY_BENEFICIAL_SELF)))
				blessingV.add(A);
		}
		return blessingV;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			final List<Ability> boonsV=getBoons(mob, (MOB)target);
			if(boonsV.size()==0)
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

		final List<Ability> blessingV=getBoons(mob,target);
		if(blessingV.size()==0)
		{
			mob.tell(L("You lack any boons to transfer."));
			return false;
		}
		final Ability blessA = blessingV.get(CMLib.dice().roll(1, blessingV.size(), -1));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> <T-IS-ARE> blessed!"):L("^S<S-NAME> bless(es) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final MOB blesserM=blessA.invoker()!=null ? blessA.invoker() : mob;
					final Ability A=CMClass.getAbility(blessA.ID());
					A.invoke(blesserM, target, true, asLevel);
					final Ability effA=target.fetchEffect(blessA.ID());
					if(effA!=null)
					{
						effA.setInvoker(blessA.invoker());
						effA.setExpirationDate(blessA.expirationDate());
						target.recoverPhyStats();
						blessA.unInvoke();
					}
					else
						return maliciousFizzle(mob,target,L("<S-YOUPOSS> attempt to bless <T-NAMESELF> fails."));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to bless <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
