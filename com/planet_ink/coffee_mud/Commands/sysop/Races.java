package com.planet_ink.coffee_mud.Commands.sysop;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Races
{
	private Races(){}
	public static boolean destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be deleted.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=(MOB)room.fetchInhabitant(i);
				if(M.baseCharStats().getMyRace()==R)
				{
					mob.tell("A MOB called '"+M.Name()+" in "+room.roomID()+" is this race, and must first be deleted.");
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
			}
		}
		CMClass.delRace(R);
		mob.location().showHappens(Affect.MSG_OK_ACTION,"The diversity of the world just decreased!");
		return true;
	}
	public static boolean modify(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		return true;
	}
	public static void create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R!=null)
		{
			mob.tell("A race with the ID '"+R.ID()+"' already exists!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(raceID.indexOf(" ")>=0)
		{
			mob.tell("'"+raceID+"' is an invalid race id, because it contains a space.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Race GR=CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE ID=\""+Util.capitalize(raceID)+"\"><NAME>"+Util.capitalize(raceID)+"</NAME></RACE>");
		CMClass.addRace(GR);
		modify(mob,commands);
		mob.location().showHappens(Affect.MSG_OK_ACTION,"The diversity of the world just increased!");
	}

}
