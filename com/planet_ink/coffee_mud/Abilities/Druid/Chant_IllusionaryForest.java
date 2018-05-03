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
   Copyright 2002-2018 Bo Zimmerman

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

public class Chant_IllusionaryForest extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_IllusionaryForest";
	}

	private final static String localizedName = CMLib.lang().L("Illusionary Forest");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Illusionary Fores)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}
	Room newRoom=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, L("The appearance of this place changes..."));
		super.unInvoke();
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Room)
		&&(msg.amITarget(affected))
		&&(newRoom().fetchEffect(ID())==null)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			final CMMsg msg2=CMClass.getMsg(msg.source(),newRoom(),msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),msg.targetMessage(),
						  msg.othersCode(),msg.othersMessage());
			if(newRoom().okMessage(myHost,msg2))
			{
				newRoom().executeMsg(myHost,msg2);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public Room newRoom()
	{
		if(newRoom!=null)
			return newRoom;
		newRoom=CMClass.getLocale("Woods");
		switch(CMLib.dice().roll(1,10,0))
		{
		case 1:
			newRoom.setDisplayText(L("Forest glade"));
			newRoom.setDescription(L("This quaint forest glade is surrounded by tall oak trees.  A gentle breeze tosses leaves up into the air."));
			break;
		case 2:
			newRoom.setDisplayText(L("Dark Forest"));
			newRoom.setDescription(L("The forest is dark and thick here.  Ominous looking trees seem to block every path, and the air is perfectly still."));
			break;
		case 3:
			newRoom.setDisplayText(L("Light Forest"));
			newRoom.setDescription(L("A light growth of tall evergreens surrounds you on all sides.  There are no apparent paths, but you can still see the sky through the leaves."));
			break;
		case 4:
			newRoom.setDisplayText(L("Forest by the stream"));
			newRoom.setDescription(L("A light growth of tall evergreens surrounds you on all sides.  You can hear the sound of a running brook, but can't tell which direction its coming from."));
			break;
		case 5:
			newRoom.setDisplayText(L("Dark Forest"));
			newRoom.setDescription(L("The trees around you are dark and old, their branches seeming to reach towards you.  In the distance, a wolfs howl can be heard."));
			break;
		case 6:
			newRoom.setDisplayText(L("End of the path"));
			newRoom.setDescription(L("The forest path seems to end at the base of a copse of tall evergreens.  Behind you, the path has mysteriously vanished."));
			break;
		case 7:
			newRoom.setDisplayText(L("Forest"));
			newRoom.setDescription(L("You are standing in the middle of a light forest.  How you got here, you can't really say."));
			break;
		case 8:
			newRoom.setDisplayText(L("Dark Forest"));
			newRoom.setDescription(L("You are standing in the middle of a thick dark forest.  You wish you knew how you got here."));
			break;
		case 9:
			newRoom.setDisplayText(L("Dark Forest"));
			newRoom.setDescription(L("The trees here seem to tower endlessly into the sky.  Their branches blocking out all but the smallest glimpses of the sky."));
			break;
		case 10:
			newRoom.setDisplayText(L("Druidic Forest"));
			newRoom.setDescription(L("A forest seems to have grown up all around you.  The strange magical nature of the mushroom like trees makes you think you've entered a druidic grove."));
			break;
		}
		return newRoom;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			newRoom();
			final CMMsg msg = CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto), auto?"":L("^S<S-NAME> chant(s) dramatically!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The appearance of this place changes..."));
				if(CMLib.law().doesOwnThisLand(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMLib.database().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) dramatically, but the magic fades."));

		// return whether it worked
		return success;
	}
}
