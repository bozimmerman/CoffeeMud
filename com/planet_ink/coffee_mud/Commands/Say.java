package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Say extends StdCommand
{
	public Say(){}

	private String[] access={"SAY","ASK","'"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String theWord="Say";
		if(((String)commands.elementAt(0)).equalsIgnoreCase("ask"))
			theWord="Ask";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("yell"))
			theWord="Yell";
		if(commands.size()==1)
		{
			mob.tell(theWord+" what?");
			return false;
		}
		Environmental target=null;
		if(commands.size()>2)
		{
			String possibleTarget=(String)commands.elementAt(1);
			target=mob.location().fetchFromRoomFavorMOBs(null,possibleTarget,Item.WORN_REQ_ANY);
			if((target!=null)&&(!target.name().equalsIgnoreCase(possibleTarget))&&(possibleTarget.length()<4))
			   target=null;
			if((target!=null)&&(Sense.canBeSeenBy(target,mob)))
				commands.removeElementAt(1);
			else
				target=null;
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+" what?");
			return false;
		}

		FullMsg msg=null;
		if(target==null)
			msg=new FullMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> "+theWord.toLowerCase()+"(s) '"+combinedCommands+"'^?");
		else
		if(theWord.equalsIgnoreCase("ask"))
			msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> ask(s) <T-NAMESELF> '"+combinedCommands+"'^?");
		else
			msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> "+theWord.toLowerCase()+"(s) to <T-NAMESELF> '"+combinedCommands+"'^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(theWord.equalsIgnoreCase("Yell"))
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().getRoomInDir(d);
					Exit E=mob.location().getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^TYou hear someone yell '"+combinedCommands+"' "+Directions.getInDirectionName(Directions.getOpDirectionCode(d))+"^?");
						if(R.okMessage(mob,msg))
							R.sendOthers(mob,msg);
					}
				}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
