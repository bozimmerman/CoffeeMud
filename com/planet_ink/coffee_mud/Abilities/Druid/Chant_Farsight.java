package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Farsight extends Chant
{
	public Chant_Farsight()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Farsight";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(9);
		canAffectCode=0;
		canTargetCode=0;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Farsight();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for a far off vision, but the magic fades.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType,"^S<S-NAME> chant(s) for a far off vision.^?");
			if(mob.location().okAffect(msg))
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
							mob.tell("^D" + Util.padRight(Directions.getDirectionName(d),5)+":^^^N ^d"+exit.viewableText(mob, room)+"^N");
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
							mob.tell("\n\r");
							FullMsg msg2=new FullMsg(mob,thatRoom,Affect.MSG_EXAMINESOMETHING,null);
							thatRoom.affect(msg2);
						}
					}
				}
			}
		}

		return success;
	}
}