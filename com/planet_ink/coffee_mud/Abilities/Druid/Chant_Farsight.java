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

public class Chant_Farsight extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Farsight";
	}

	private final static String	localizedName	= CMLib.lang().L("Eaglesight");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ENDURING;
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
		return 0;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) for a far off vision, but the magic fades."));
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L("^S<S-NAME> chant(s) for a far off vision.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				int limit=(mob.phyStats().level()+(2*getXLEVELLevel(mob)))/3;
				if(limit<0)
					limit=1;
				if(commands.size()==0)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit exit=thatRoom.getExitInDir(d);
						Room room=thatRoom.getRoomInDir(d);

						if((exit!=null)&&(room!=null)&&(CMLib.flags().canBeSeenBy(exit,mob)&&(exit.isOpen())))
						{
							mob.tell("^D" + CMStrings.padRight(CMLib.directions().getDirectionName(d),5)+":^.^N ^d"+exit.viewableText(mob, room)+"^N");
							exit=room.getExitInDir(d);
							room=room.getRoomInDir(d);
							if((exit!=null)&&(room!=null)&&(CMLib.flags().canBeSeenBy(exit,mob)&&(exit.isOpen())))
							{
								mob.tell(CMStrings.padRight("",5)+":^N ^d"+exit.viewableText(mob, room)+"^N");
								exit=room.getExitInDir(d);
								room=room.getRoomInDir(d);
								if((exit!=null)&&(room!=null)&&(CMLib.flags().canBeSeenBy(exit,mob)&&(exit.isOpen())))
								{
									mob.tell(CMStrings.padRight("",5)+":^N ^d"+exit.viewableText(mob, room)+"^N");
								}
							}
						}
					}
				}
				else
				while(commands.size()>0)
				{
					final String whatToOpen=commands.get(0);
					int dirCode=CMLib.directions().getGoodDirectionCode(whatToOpen);
					final Exit exit;
					final Room room;
					if(dirCode < 0)
					{
						Item I=thatRoom.findItem(null, whatToOpen);
						if((I instanceof Exit)&&(CMLib.flags().canBeSeenBy(I, mob)))
						{
							exit=(Exit)I;
							room=((Exit)I).lastRoomUsedFrom(mob.location());
							dirCode=Directions.GATE;
						}
						else
						{
							exit=null;
							room=null;
						}
					}
					else
					{
						exit=thatRoom.getExitInDir(dirCode);
						room=thatRoom.getRoomInDir(dirCode);
					}
					if(limit<=0)
					{
						mob.tell(L("Your sight has reached its limit."));
						success=true;
						break;
					}
					else
					if(dirCode<0)
					{
						mob.tell(L("\n\r'@x1' is not a valid direction.",whatToOpen));
						commands.clear();
						success=false;
					}
					else
					{
						if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob))||(!exit.isOpen()))
						{
							mob.tell(L("\n\rSomething has obstructed your vision."));
							success=false;
							commands.clear();
						}
						else
						{
							commands.remove(0);
							thatRoom=room;
							limit--;
							mob.tell(L("\n\r"));
							final CMMsg msg2=CMClass.getMsg(mob,thatRoom,CMMsg.MSG_LOOK,null);
							thatRoom.executeMsg(mob,msg2);
						}
					}
				}
			}
		}
		return success;
	}
}
