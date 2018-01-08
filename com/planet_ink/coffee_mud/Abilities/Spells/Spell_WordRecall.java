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

public class Spell_WordRecall extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WordRecall";
	}

	private final static String localizedName = CMLib.lang().L("Word of Recall");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL-90;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	protected int verbalCastCode(MOB mob, Physical target, boolean auto)
	{
		int affectType=CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_RECALL;
		if(auto)
			affectType=affectType|CMMsg.MASK_ALWAYS;
		return affectType;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=(!mob.isInCombat())||proficiencyCheck(mob,0,auto);
		if(success)
		{
			final int AUTO=auto?CMMsg.MASK_ALWAYS:0;
			final Room recalledRoom=mob.location();
			final Room recallRoom=mob.getStartRoom();
			CMMsg msg=CMClass.getMsg(mob,recalledRoom,this,verbalCastCode(mob,recalledRoom,auto),CMMsg.MASK_MAGIC|AUTO|CMMsg.MSG_LEAVE,verbalCastCode(mob,recalledRoom,auto),auto?L("<S-NAME> disappear(s) into the Java Plane!"):L("<S-NAME> recall(s) body and spirit to the Java Plane!"));
			CMMsg msg2=CMClass.getMsg(mob,recallRoom,this,verbalCastCode(mob,recallRoom,auto),CMMsg.MASK_MAGIC|AUTO|CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,verbalCastCode(mob,recallRoom,auto),null);
			if((recalledRoom.okMessage(mob,msg))&&(recallRoom.okMessage(mob,msg2)))
			{
				recalledRoom.send(mob,msg);
				recallRoom.send(mob,msg2);
				if(recalledRoom.isInhabitant(mob))
					recallRoom.bringMobHere(mob,false);
				for(int f=0;f<mob.numFollowers();f++)
				{
					final MOB follower=mob.fetchFollower(f);

					msg=CMClass.getMsg(follower,recalledRoom,this,verbalCastCode(mob,recalledRoom,auto),CMMsg.MASK_MAGIC|AUTO|CMMsg.MSG_LEAVE,verbalCastCode(mob,recalledRoom,auto),auto?L("<S-NAME> disappear(s) into the Java Plane!"):L("<S-NAME> <S-IS-ARE> sucked into the vortex created by @x1s recall.",mob.name()));
					if((follower!=null)
					&&(follower.isMonster())
					&&(!follower.isPossessing())
					&&(follower.location()==recalledRoom)
					&&(recalledRoom.isInhabitant(follower))
					&&(recalledRoom.okMessage(follower,msg)))
					{
						msg2=CMClass.getMsg(follower,recallRoom,this,verbalCastCode(mob,recallRoom,auto),CMMsg.MASK_MAGIC|AUTO|CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,verbalCastCode(mob,recallRoom,auto),null);
						if(recallRoom.okMessage(follower,msg2))
						{
							recallRoom.send(follower,msg2);
							if(recalledRoom.isInhabitant(follower))
								recallRoom.bringMobHere(follower,false);
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to recall, but <S-HIS-HER> plea goes unheard."));

		// return whether it worked
		return success;
	}

}
