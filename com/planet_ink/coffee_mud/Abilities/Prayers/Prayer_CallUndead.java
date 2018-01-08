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

public class Prayer_CallUndead extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_CallUndead";
	}

	private final static String	localizedName	= CMLib.lang().L("Call Undead");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY | Ability.FLAG_TRANSPORTING | Ability.FLAG_SUMMONING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room oldRoom=null;
		MOB target=null;
		final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
		for(Iterator<MOB> m=H.iterator();m.hasNext();)
		{
			final MOB M=m.next();
			if(!CMLib.flags().isUndead(M))
				m.remove();
		}
		if((H.size()==0)||((H.size()==1)&&(H.contains(mob))))
		{
			mob.tell(L("You don't have any controlled undead!"));
			return false;
		}

		boolean allHere=true;
		for (final Object element : H)
		{
			final MOB M=(MOB)element;
			if((M!=mob)&&(M.location()!=mob.location())&&(M.location()!=null))
			{
				allHere=false;
				if((CMLib.flags().canAccess(mob,M.location()))
				&&(!CMLib.flags().isTracking(M)))
				{
					target=M;
					oldRoom=M.location();
					break;
				}
			}
		}
		if((target==null)&&(allHere))
		{
			mob.tell(L("Better look around first."));
			return false;
		}

		if(target==null)
		{
			mob.tell(L("Either they are all en route, or you can not fixate on your undead."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=(target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob))))*3;
		final boolean success=proficiencyCheck(mob,-adjustment,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> call(s) <S-HIS-HER> undead to come to <S-HIM-HER>!^?"));
			if((mob.location().okMessage(mob,msg))&&(oldRoom != null)&&(oldRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				oldRoom.sendOthers(mob,msg);
				final MOB follower=target;
				final Room newRoom=mob.location();
				final Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)
				{
					A.invoke(follower,CMParms.parse("\""+CMLib.map().getExtendedRoomID(newRoom)+"\""),newRoom,true,0);
					return true;
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to call <S-HIS-HER> undead, but fail(s)."));

		// return whether it worked
		return success;
	}
}
