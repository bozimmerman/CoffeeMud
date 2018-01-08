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

public class Spell_SummonCompanion extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SummonCompanion";
	}

	private final static String localizedName = CMLib.lang().L("Summon Companion");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING|Ability.FLAG_SUMMONING;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room oldRoom=null;
		MOB target=null;
		final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
		if((H.size()==0)||((H.size()==1)&&(H.contains(mob))))
		{
			mob.tell(L("You don't have any companions!"));
			return false;
		}

		boolean allHere=true;
		for (final Object element : H)
		{
			final MOB M=(MOB)element;
			if((M!=mob)&&(M.location()!=mob.location())&&(M.location()!=null))
			{
				allHere=false;
				if((CMLib.flags().canAccess(mob,M.location())))
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
			mob.tell(L("You can't seem to fixate on your companions."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=(target.phyStats().level()-(mob.phyStats().level()+(getXLEVELLevel(mob)+(2*getX1Level(mob)))))*3;
		final boolean success=proficiencyCheck(mob,-adjustment,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> summon(s) <S-HIS-HER> companion in a mighty cry!^?"));
			if((mob.location().okMessage(mob,msg))&&(oldRoom!=null)&&(oldRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);

				final MOB follower=target;
				final Room newRoom=mob.location();
				final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CMLib.protocol().msp("appear.wav",10));
				final CMMsg leaveMsg=CMClass.getMsg(follower,oldRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a great summoning swirl created by @x1.",mob.name()));
				if(oldRoom.okMessage(follower,leaveMsg))
				{
					if(newRoom.okMessage(follower,enterMsg))
					{
						follower.makePeace(true);
						oldRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,true);
						newRoom.send(follower,enterMsg);
						follower.tell(L("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
					}
					else
						mob.tell(L("Some powerful magic stifles the spell."));
				}
				else
					mob.tell(L("Some powerful magic stifles the spell."));
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to summon <S-HIS-HER> companion, but fail(s)."));

		// return whether it worked
		return success;
	}
}
