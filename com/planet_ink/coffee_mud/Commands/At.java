package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class At extends StdCommand
{
	public At(){}

	private String[] access={"AT"};
	public String[] getAccessWords(){return access;}

	public Room findRoomLiberally(MOB mob, StringBuffer cmd)
	{
		Room room=null;
		Room curRoom=mob.location();
		int dirCode=Directions.getGoodDirectionCode(cmd.toString());
		if(dirCode>=0)
			room=mob.location().rawDoors()[dirCode];
		if(room==null)
			room = CMMap.getRoom(cmd.toString());
		if(room==null)
		{
			if((cmd.charAt(0)=='#')&&(curRoom!=null))
			{
				cmd.insert(0,curRoom.getArea().Name());
				room = CMMap.getRoom(cmd.toString());
			}
			else
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if((thisSession.mob()!=null) && (!thisSession.killFlag())
					&&(thisSession.mob().location()!=null)
					&&(thisSession.mob().name().equalsIgnoreCase(cmd.toString())))
					{
						room = thisSession.mob().location();
						break;
					}
				}
				if(room==null)
					for(int s=0;s<Sessions.size();s++)
					{
						Session thisSession=(Session)Sessions.elementAt(s);
						if((thisSession.mob()!=null)&&(!thisSession.killFlag())
						&&(thisSession.mob().location()!=null)
						&&(EnglishParser.containsString(thisSession.mob().name(),cmd.toString())))
						{
							room = thisSession.mob().location();
							break;
						}
					}
				if(room==null)
				{
					Vector candidates=new Vector();
					MOB target=null;
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						target=R.fetchInhabitant(cmd.toString());
						if(target!=null)
							candidates.addElement(target);
					}
					if(candidates.size()>0)
					{
						target=(MOB)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
						room=target.location();
					}
				}
				if(room==null)
				{
					for(Enumeration a=CMMap.areas();a.hasMoreElements();)
					{
						Area A=(Area)a.nextElement();
						if((EnglishParser.containsString(A.name(),cmd.toString()))
						&&(A.mapSize()>0))
						{
							int tries=0;
							while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
								room=(Room)A.getRandomRoom();
							break;
						}
					}
				}
				if(room==null)
				{
					String areaName=cmd.toString().toUpperCase();
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(EnglishParser.containsString(R.displayText(),areaName))
						{
						   room=R;
						   break;
						}
					}
					if(room==null)
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(EnglishParser.containsString(R.description(),areaName))
						{
						   room=R;
						   break;
						}
					}
					if(room==null)
					{
						Vector candidates=new Vector();
						Item target=null;
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							target=R.fetchItem(null,cmd.toString());
							if(target!=null)
								candidates.addElement(target);
						}
						if(candidates.size()>0)
						{
							target=(Item)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
							if(target.owner() instanceof Room)
								room=(Room)target.owner();
						}
					}
				}
			}
		}
		return room;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		Room room=findRoomLiberally(mob,new StringBuffer(cmd));
		if(room==null)
		{
			if(CMSecurity.isAllowedAnywhere(mob,"AT"))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,"AT"))
		{
			mob.tell("You aren't powerful enough to do that there.");
			return false;
		}
		Room R=mob.location();
		if(R!=room)	room.bringMobHere(mob,false);
		mob.doCommand(commands);
		if(mob.location()!=R) R.bringMobHere(mob,false);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AT");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
