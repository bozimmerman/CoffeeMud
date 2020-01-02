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
   Copyright 2008-2020 Bo Zimmerman

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
public class Prayer_Cleanliness extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Cleanliness";
	}

	private final static String localizedName = CMLib.lang().L("Cleanliness");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target instanceof MOB))
			return Ability.QUALITY_INDIFFERENT;
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=super.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(target instanceof Room)
		{
		}
		else
		if(target instanceof Item)
		{
		}
		else
		if(target instanceof MOB)
		{
		}
		else
		{
			mob.tell(L("@x1 doesn't look very dirty.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("A bright white glow surrounds <T-NAME>."):L("^S<S-NAME> @x1, delivering a strong touch of divine cleanliness to <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Room)
				{
					Ability A=target.fetchEffect("Soiled");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					A=target.fetchEffect("Dusty");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					for(final Enumeration<Item> i=((Room)target).items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I!=null)
						&&(I.container()==null))
						{
							A=I.fetchEffect("Soiled");
							if(A!=null)
							{
								A.unInvoke();
								I.delEffect(A);
							}
							A=I.fetchEffect("Dusty");
							if(A!=null)
							{
								A.unInvoke();
								I.delEffect(A);
							}
						}
					}
					mob.tell(L("@x1 seems cleaner!",target.name(mob)));
				}
				else
				if(target instanceof Item)
				{
					Ability A=target.fetchEffect("Soiled");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					A=target.fetchEffect("Dusty");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					mob.tell(L("@x1 seems cleaner!",target.name(mob)));
				}
				else
				if(target instanceof MOB)
				{
					Ability A=target.fetchEffect("Soiled");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					A=target.fetchEffect("Dusty");
					if(A!=null)
					{
						A.unInvoke();
						target.delEffect(A);
					}
					final MOB targetM=(MOB)target;
					if((targetM.playerStats()!=null)&&(targetM.playerStats().getHygiene()>0))
						targetM.playerStats().setHygiene(0);
					targetM.tell(L("You feel clean!"));
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> @x1 for <T-NAMESELF>, but nothing happens.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
