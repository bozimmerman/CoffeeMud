package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2004-2020 Bo Zimmerman

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
public class Chant_FertileCavern extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FertileCavern";
	}

	private final static String localizedName = CMLib.lang().L("Fertile Cavern");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	protected int previousResource=-1;

	@Override
	public void unInvoke()
	{
		if((affected instanceof Room)
		&&(this.canBeUninvoked()))
		{
			((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,L("The soil begins to revert to rock!"));
			((Room)affected).setResource(previousResource);
		}
	}

	protected boolean hasTicked = false;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof Room)
		{
			hasTicked = true;
			final Room R=(Room)affected;
			if(R!=null)
				R.setResource(RawMaterial.RESOURCE_DIRT);
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(!canBeUninvoked()
		&&(!hasTicked))
		{
			if((msg.source() != null)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() == affected)
			&&(affected instanceof Room))
			{
				final Room R=(Room)affected;
				if((R!=null)
				&&(!hasTicked))
				{
					if((!CMLib.threads().isTicking(this, -1))
					&&(!CMLib.threads().isTicking(R, -1)))
						CMLib.threads().startTickDown(this, Tickable.TICKID_SPELL_AFFECT, 3);
				}
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(!canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				final Room R=(Room)affected;
				if(R!=null)
					R.setResource(RawMaterial.RESOURCE_DIRT);
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;

		if(R.fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already fertile."));
			return false;
		}

		if(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell(L("This chant cannot be used here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,R,this,verbalCastCode(mob,R,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAME>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if((R instanceof Room)
					&&(CMLib.law().doesOwnThisProperty(mob,(R))))
					{
						R.addNonUninvokableEffect((Ability)this.copyOf());
						CMLib.database().DBUpdateRoom(R);
					}
					else
						beneficialAffect(mob, R, asLevel,0);
					final Chant_FertileCavern A=(Chant_FertileCavern)R.fetchEffect(ID());
					if(A!=null)
					{
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("The rock and stone of @x1 begins to soften and grow dark and rich!",R.name()));
						A.previousResource=R.myResource();
						R.setResource(RawMaterial.RESOURCE_DIRT);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,R,L("<S-NAME> chant(s) to <T-NAME>, but the magic fades."));
		// return whether it worked
		return success;
	}
}
