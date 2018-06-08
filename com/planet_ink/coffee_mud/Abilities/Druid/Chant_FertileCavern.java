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
   Copyright 2004-2018 Bo Zimmerman

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
		return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;
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
	
	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if(R!=null)
				R.setResource(RawMaterial.RESOURCE_DIRT);
		}
		return true;
	}
	

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null)
			return false;

		if(target.domainType()!=Room.DOMAIN_INDOORS_CAVE)
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAME>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					beneficialAffect(mob, target, asLevel,0);
					final Chant_FertileCavern A=(Chant_FertileCavern)target.fetchEffect(ID());
					if(A!=null)
					{
						target.showHappens(CMMsg.MSG_OK_VISUAL,L("The rock and stone of @x1 begins to soften and grow dark and rich!",target.name()));
						A.previousResource=target.myResource();
						target.setResource(RawMaterial.RESOURCE_DIRT);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAME>, but the magic fades."));
		// return whether it worked
		return success;
	}
}
