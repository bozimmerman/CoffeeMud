package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_ClanHome extends Spell
{
	@Override public String ID() { return "Spell_ClanHome"; }
	@Override public String name(){return "Clan Home";}
	@Override protected int canTargetCode(){return 0;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	@Override public long flags(){return Ability.FLAG_TRANSPORTING|Ability.FLAG_CLANMAGIC;}
	@Override protected boolean disregardsArmorCheck(MOB mob){return true;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!mob.clans().iterator().hasNext())
		{
			mob.tell(_("You aren't even a member of a clan."));
			return false;
		}
		final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, Clan.Function.CLAN_BENEFITS);
		if(clanPair==null)
		{
			mob.tell(_("You are not authorized to draw from the power of your clan."));
			return false;
		}
		final Clan C=clanPair.first;
		Room clanHomeRoom=null;
		clanHomeRoom=CMLib.map().getRoom(C.getRecall());
		if(clanHomeRoom==null)
		{
			mob.tell(_("Your clan does not have a clan home."));
			return false;
		}
		if(!CMLib.flags().canAccess(mob,clanHomeRoom))
		{
			mob.tell(_("You can't use this magic to get there from here."));
			return false;
		}
		if(!CMLib.law().doesOwnThisProperty(C.clanID(),clanHomeRoom))
		{
			mob.tell(_("Your clan no longer owns that room."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,mob,auto),_("^S<S-NAME> invoke(s) a teleportation spell.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Set<MOB> h=properTargets(mob,givenTarget,false);
				if(h==null) return false;

				final Room thisRoom=mob.location();
				for (final Object element : h)
				{
					final MOB follower=(MOB)element;
					final CMMsg enterMsg=CMClass.getMsg(follower,clanHomeRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,_("<S-NAME> appears in a puff of red smoke."));
					final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,_("<S-NAME> disappear(s) in a puff of red smoke."));
					if(thisRoom.okMessage(follower,leaveMsg)&&clanHomeRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						clanHomeRoom.bringMobHere(follower,false);
						clanHomeRoom.send(follower,enterMsg);
						follower.tell(_("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
					}
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,_("<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell."));


		// return whether it worked
		return success;
	}
}
