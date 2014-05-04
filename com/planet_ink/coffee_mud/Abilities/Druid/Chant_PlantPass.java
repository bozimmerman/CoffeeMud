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
public class Chant_PlantPass extends Chant
{
	@Override public String ID() { return "Chant_PlantPass"; }
	@Override public String name(){ return "Plant Pass";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_SHAPE_SHIFTING;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return 0;}
	@Override public long flags(){return Ability.FLAG_TRANSPORTING;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(_("You must specify the name of the location of one of your plants.  Use your 'My Plants' skill if necessary."));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();

		final Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(myPlant==null)
		{
			mob.tell(_("There doesn't appear to be any of your plants here to travel through."));
			return false;
		}

		final Vector candidates=Druid_MyPlants.myPlantRooms(mob);
		Room newRoom=null;
		for(int m=0;m<candidates.size();m++)
		{
			final Room room=(Room)candidates.elementAt(m);
			if(CMLib.english().containsString(room.displayText(mob),areaName))
			{
			   newRoom=room;
			   break;
			}
		}
		if(newRoom==null)
		{
			mob.tell(_("You can't seem to fixate on a place called '@x1', perhaps you have nothing growing there?",CMParms.combine(commands,0)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,myPlant,this,CMMsg.MASK_MOVE|verbalCastCode(mob,myPlant,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF> and <S-IS-ARE> drawn into it!^?");
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				final Set<MOB> h=properTargets(mob,givenTarget,false);
				if(h==null) return false;

				final Room thisRoom=mob.location();
				for (final Object element : h)
				{
					final MOB follower=(MOB)element;
					final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,_("<S-NAME> emerge(s) from the ground."));
					final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,_("<S-NAME> <S-IS-ARE> sucked into @x1.",myPlant.name()));
					if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						if(follower.isInCombat())
						{
							CMLib.commands().postFlee(follower,("NOWHERE"));
							follower.makePeace();
						}
						thisRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell(_("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,_("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));


		// return whether it worked
		return success;
	}
}
