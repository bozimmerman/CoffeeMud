package com.planet_ink.coffee_mud.commands.base.sysop;

import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Exits
{
	public void create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		else
			thisExit=(Exit)thisExit.newInstance();

		Exit opExit=mob.location().exits()[direction];
		Room opRoom=mob.location().doors()[direction];

		Exit reverseExit=mob.location().getReverseExit(direction);
		if(reverseExit!=null)
		{
			if((thisExit.isGeneric())&&(reverseExit.isGeneric()))
			{
				thisExit=(Exit)reverseExit.copyOf();
				new Generic().modifyGenExit(mob,thisExit);
			}
		}


		mob.location().exits()[direction]=thisExit;
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly a portal opens up "+Directions.getInDirectionName(direction)+".\n\r");
		ExternalPlay.DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.exits()[revDirCode]==reverseExit)
			{
				opRoom.exits()[revDirCode]=(Exit)thisExit.copyOf();
				ExternalPlay.DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.exits()[revDirCode]==null)&&(opRoom.doors()[revDirCode]==mob.location()))
			{
				opRoom.exits()[revDirCode]=(Exit)thisExit.copyOf();
				ExternalPlay.DBUpdateExits(opRoom);
			}
		}
		Log.sysOut("Exits",mob.location().ID()+" exits changed by "+mob.ID()+".");
	}


	public void modify(MOB mob, Vector commands)
		throws IOException
	{
		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		Exit thisExit=mob.location().exits()[direction];
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=Util.combine(commands,3);

		if(thisExit.isGeneric())
			new Generic().modifyGenExit(mob,thisExit);
		else
		if(restStr.length()>0)
			thisExit.setMiscText(restStr);
		else
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		for(int m=0;m<CMMap.map.size();m++)
		{
			Room room=(Room)CMMap.map.elementAt(m);
			for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
			{
				Exit exit=room.exits()[e2];
				if((exit!=null)&&(exit==thisExit))
				{
					ExternalPlay.DBUpdateExits(room);
					break;
				}
			}
		}
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,thisExit.name()+" shake(s) under the transforming power.");
		Log.sysOut("Exits",mob.location().ID()+" exits changed by "+mob.ID()+".");
	}

	public void destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY EXIT [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U or D.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;

		}
		mob.location().exits()[direction]=null;
		ExternalPlay.DBUpdateExits(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
		Log.sysOut("Exits",mob.location().ID()+" exits destroyed by "+mob.ID()+".");
	}
}
