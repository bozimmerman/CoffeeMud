package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_Farsight extends Spell
{
	public String ID() { return "Spell_Farsight"; }
	public String name(){return "Farsight";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> get(s) a far off look, but the spell fizzles.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType(auto),"^S<S-NAME> get(s) a far off look in <S-HIS-HER> eyes.^?");
			int limit=mob.envStats().level()/5;
			if(limit<0) limit=1;
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				if(commands.size()==0)
				{
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						Exit exit=thatRoom.getExitInDir(d);
						Room room=thatRoom.getRoomInDir(d);

						if((exit!=null)&&(room!=null)&&(Sense.canBeSeenBy(exit,mob)&&(exit.isOpen())))
						{
							mob.tell("^D" + Util.padRight(Directions.getDirectionName(d),5)+":^.^N ^d"+exit.viewableText(mob, room)+"^N");
							exit=room.getExitInDir(d);
							room=room.getRoomInDir(d);
							if((exit!=null)&&(room!=null)&&(Sense.canBeSeenBy(exit,mob)&&(exit.isOpen())))
							{
								mob.tell(Util.padRight("",5)+":^N ^d"+exit.viewableText(mob, room)+"^N");
								exit=room.getExitInDir(d);
								room=room.getRoomInDir(d);
								if((exit!=null)&&(room!=null)&&(Sense.canBeSeenBy(exit,mob)&&(exit.isOpen())))
								{
									mob.tell(Util.padRight("",5)+":^N ^d"+exit.viewableText(mob, room)+"^N");
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

						if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob)))||((exit!=null)&&(!exit.isOpen())))
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
							FullMsg msg2=new FullMsg(mob,thatRoom,CMMsg.MSG_LOOK,null);
							thatRoom.executeMsg(mob,msg2);
						}
					}
				}
			}
		}

		return success;
	}
}
