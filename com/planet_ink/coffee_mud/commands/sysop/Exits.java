package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;

public class Exits
{
	public static void Create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=MUD.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}
		else
			thisExit=(Exit)thisExit.newInstance();

		Exit opExit=mob.location().getExit(direction);
		Room opRoom=mob.location().getRoom(direction);
		Exit reverseExit=null;
		if(opRoom!=null)
			reverseExit=opRoom.getExit(Directions.getOpDirectionCode(direction));

		if(opExit!=null)
			mob.location().exits()[direction]=null;

		mob.location().exits()[direction]=thisExit;

		if((reverseExit!=null)
		   &&((reverseExit==opExit)
			  ||((thisExit.ID().equals(reverseExit.ID()))
				 &&(thisExit.text().equals(reverseExit.text())))))
		{
			mob.tell("Opposite room exits this way. This exit will apply to both rooms.");
			opRoom.exits()[Directions.getOpDirectionCode(direction)]=null;
			opRoom.exits()[Directions.getOpDirectionCode(direction)]=thisExit;
			RoomLoader.DBUpdateExits(opRoom);
		}

		if(thisExit instanceof GenExit)
			Generic.modifyGenExit(mob,(GenExit)thisExit);
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"Suddenly a portal opens up "+Directions.getInDirectionName(direction)+".\n\r");
		RoomLoader.DBUpdateExits(mob.location());
		Log.sysOut("Exits",mob.location().ID()+" exits changed by "+mob.ID()+".");
	}


	public static void Modify(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] [NEW MISC TEXT]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		Exit thisExit=mob.location().getExit(direction);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=CommandProcessor.combine(commands,3);

		if(true)//command.equals("LEVEL"))
		{
			if(thisExit instanceof GenExit)
				Generic.modifyGenExit(mob,(GenExit)thisExit);
			else
				thisExit.setMiscText(restStr);


			for(int m=0;m<MUD.map.size();m++)
			{
				Room room=(Room)MUD.map.elementAt(m);
				for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
				{
					Exit exit=room.getExit(e2);
					if((exit!=null)&&(exit==thisExit))
					{
						RoomLoader.DBUpdateExits(room);
						break;
					}
				}
			}
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,thisExit.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try TEXT, or TEXT.");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
		}
		Log.sysOut("Exits",mob.location().ID()+" exits changed by "+mob.ID()+".");
	}

	public static void Destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY EXIT [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;

		}
		mob.location().exits()[direction]=null;
		RoomLoader.DBUpdateExits(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
		Log.sysOut("Exits",mob.location().ID()+" exits destroyed by "+mob.ID()+".");
	}
}
