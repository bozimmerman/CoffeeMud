package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Look extends StdCommand
{
	public Look(){}

	private String[] access={"EXAMINE","EXAM","EXA","LOOK","LOO","LO","L"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean quiet=false;
		if((commands.size()>0)&&(((String)commands.firstElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		String textMsg="<S-NAME> look(s) ";
		if(mob.location()==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("at")))
			   commands.removeElementAt(1);
			else
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("to")))
			   commands.removeElementAt(1);
			String ID=Util.combine(commands,1);

			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
			{
				mob.location().listExits(mob);
				return false;
			}
			if(ID.equalsIgnoreCase("SELF"))
				ID=mob.name();
			Environmental thisThang=null;
			int dirCode=Directions.getGoodDirectionCode(ID);
			if(dirCode>=0)
			{
				Room room=mob.location().getRoomInDir(dirCode);
				Exit exit=mob.location().getExitInDir(dirCode);
				if((room!=null)&&(exit!=null))
					thisThang=exit;
				else
				{
					mob.tell("You don't see anything that way.");
					return false;
				}
			}
			if(dirCode<0)
			{
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Item.WORN_REQ_ANY);
				if((thisThang==null)
				&&(commands.size()>2)
				&&(((String)commands.elementAt(1)).equalsIgnoreCase("in")))
				{
					commands.removeElementAt(1);
					String ID2=Util.combine(commands,1);
					thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID2,Item.WORN_REQ_ANY);
					if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
					{
						mob.tell("That's not a container.");
						return false;
					}
				}
			}
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>.";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,CMMsg.MSG_EXAMINESOMETHING,textMsg+name);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				if((thisThang instanceof Room)&&(Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS)))
					((Room)thisThang).listExits(mob);
			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
				if(((String)commands.elementAt(0)).toUpperCase().startsWith("E"))
				{
					mob.tell("Examine what?");
					return false;
				}

			FullMsg msg=new FullMsg(mob,mob.location(),null,CMMsg.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"),CMMsg.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"at you."),CMMsg.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if((Util.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
			&&(Sense.canBeSeenBy(mob.location(),mob)))
				mob.location().listExits(mob);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
