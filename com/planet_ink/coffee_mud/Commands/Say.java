package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Say extends StdCommand
{
	public Say(){}

	private String[] access={"SAY","ASK","`","SA","SAYTO"};
	public String[] getAccessWords(){return access;}
	
	private static final String[] impossibleTargets={
		"HERE",
		"THERE",
		"IS",
		"JUST",
		"A",
		"AN",
		"TO",
		"THE",
		"SOME",
		"SITS",
		"RESTS",
		"LEFT",
		"HAS",
		"BEEN"
	};
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String theWord="Say";
		boolean toFlag=false;
		if(((String)commands.elementAt(0)).equalsIgnoreCase("ask"))
			theWord="Ask";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("yell"))
			theWord="Yell";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("sayto")
		||((String)commands.elementAt(0)).equalsIgnoreCase("sayt"))
		{
			theWord="Say";
			toFlag=true;
		}
		
		if(commands.size()==1)
		{
			mob.tell(theWord+" what?");
			return false;
		}
		
		String whom="";
		Environmental target=null;
		if(commands.size()>2)
		{
			whom=((String)commands.elementAt(1)).toUpperCase();
			if(!toFlag)
				for(int i=0;i<impossibleTargets.length;i++)
					if(impossibleTargets[i].startsWith(whom))
					{ whom=""; break;}
			if(whom.length()>0)
			{
				target=mob.location().fetchFromRoomFavorMOBs(null,whom,Item.WORN_REQ_ANY);
				
				if((!toFlag)&&(target!=null))
				{
					if(!(target instanceof MOB))
						target=null;
					else
					if(target.name().toUpperCase().indexOf(whom.toUpperCase())<0)
						target=null;
					else
					if((!target.name().equalsIgnoreCase(whom))&&(whom.length()<4))
						target=null;
				}
				
				if((target!=null)&&(Sense.canBeSeenBy(target,mob)))
					commands.removeElementAt(1);
				else
					target=null;
			}
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
		if(toFlag&&((target==null)||(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+whom+"' here to speak to.");
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
