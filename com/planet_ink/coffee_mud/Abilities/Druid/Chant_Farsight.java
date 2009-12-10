package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant_Farsight extends Chant
{
	public String ID() { return "Chant_Farsight"; }
	public String name(){ return "Eaglesight";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for a far off vision, but the magic fades.");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),"^S<S-NAME> chant(s) for a far off vision.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				int limit=(mob.envStats().level()+(2*super.getXLEVELLevel(mob)))/3;
				if(limit<0) limit=1;
				if(commands.size()==0)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit exit=thatRoom.getExitInDir(d);
						Room room=thatRoom.getRoomInDir(d);

						if((exit!=null)&&(room!=null)&&(CMLib.flags().canBeSeenBy(exit,mob)&&(exit.isOpen())))
						{
							mob.tell("^D" + CMStrings.padRight(Directions.getDirectionName(d),5)+":^.^N ^d"+exit.viewableText(mob, room)+"^N");
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
					String whatToOpen=(String)commands.elementAt(0);
					int dirCode=Directions.getGoodDirectionCode(whatToOpen);
					if(limit<=0)
					{
						mob.tell("Your sight has reached its limit.");
						success=true;
						break;
					}
					else
					if(dirCode<0)
					{
						mob.tell("\n\r'"+whatToOpen+"' is not a valid direction.");
						commands.removeAllElements();
						success=false;
					}
					else
					{
						Exit exit=thatRoom.getExitInDir(dirCode);
						Room room=thatRoom.getRoomInDir(dirCode);

						if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob))||(!exit.isOpen()))
						{
							mob.tell("\n\rSomething has obstructed your vision.");
							success=false;
							commands.removeAllElements();
						}
						else
						{
							commands.removeElementAt(0);
							thatRoom=room;
							limit--;
							mob.tell("\n\r");
							CMMsg msg2=CMClass.getMsg(mob,thatRoom,CMMsg.MSG_LOOK,null);
							thatRoom.executeMsg(mob,msg2);
						}
					}
				}
			}
		}

		return success;
	}
}
