package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_SparringRoom extends Property
{
	@Override
	public String ID()
	{
		return "Prop_SparringRoom";
	}

	@Override
	public String name()
	{
		return "Player Death Neutralizing";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.tool() instanceof LimbDamage)
			return false;
		if(msg.sourceMinor()==CMMsg.TYP_FLEE)
			return false;
		if(msg.sourceMinor()==CMMsg.TYP_RECALL)
		{
			msg.source().tell(L("Noone hears your plea."));
			return false;
		}
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(!msg.source().isMonster())
		&&(msg.source().isInCombat()))
		{
			MOB source=null;
			if((msg.tool() instanceof MOB))
				source=(MOB)msg.tool();
			final MOB target=msg.source();
			final Room deathRoom=target.location();
			deathRoom.show(source,source,CMMsg.MSG_OK_VISUAL,msg.sourceMessage());
			if(source!=null)
			{
				final CharClass combatCharClass=CMLib.combat().getCombatDominantClass(source,target);
				final Set<MOB> beneficiaries=CMLib.combat().getCombatBeneficiaries(source,target,combatCharClass);
				final Set<MOB> dividers=CMLib.combat().getCombatDividers(source,target,combatCharClass);
				CMLib.combat().dispenseExperience(beneficiaries,dividers,target);
			}
			target.makePeace(true);
			target.setRiding(null);
			for(int a=target.numEffects()-1;a>=0;a--) // personal effects
			{
				final Ability A=target.fetchEffect(a);
				if(A!=null)
					A.unInvoke();
			}
			target.setLocation(null);
			while(target.numFollowers()>0)
			{
				final MOB follower=target.fetchFollower(0);
				if(follower!=null)
				{
					follower.setFollowing(null);
					target.delFollower(follower);
				}
			}
			target.setFollowing(null);
			Room R=null;
			if(text().trim().length()>0)
				R=CMLib.map().getRoom(text().trim());
			if(R==null)
				R=target.getStartRoom();
			R.bringMobHere(target,false);
			target.bringToLife(R,true);
			target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
			deathRoom.recoverRoomStats();
			return false;
		}
		return true;
	}
}
