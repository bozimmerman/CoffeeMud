package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Whisper extends StdCommand
{
	public Whisper(){}

	private String[] access={"WHISPER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()==1)
		{
			mob.tell("Whisper what?");
			return false;
		}
		Environmental target=null;
		if(commands.size()>2)
		{
			String possibleTarget=(String)commands.elementAt(1);
			target=mob.location().fetchFromRoomFavorMOBs(null,possibleTarget,Item.WORN_REQ_ANY);
			if((target!=null)&&(!target.name().equalsIgnoreCase(possibleTarget))&&(possibleTarget.length()<4))
			   target=null;
			if((target!=null)
			&&(Sense.canBeSeenBy(target,mob))
			&&((!(target instanceof Rider))
			   ||(((Rider)target).riding()==mob.riding())))
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
			mob.tell("Whisper what?");
			return false;
		}

		FullMsg msg=null;
		if(target==null)
		{
			Rideable R=mob.riding();
			if(R==null)
			{
				msg=new FullMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) to <S-HIM-HERSELF> '"+combinedCommands+"'^?",CMMsg.NO_EFFECT,null,CMMsg.MSG_QUIETMOVEMENT,"^T<S-NAME> whisper(s) to himself^?");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
			{
				msg=new FullMsg(mob,R,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'.^?",
								CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'.^?",
								CMMsg.NO_EFFECT,null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					for(int i=0;i<mob.location().numInhabitants();i++)
					{
						MOB M=mob.location().fetchInhabitant(i);
						if(M!=null)
						{
							if(R.amRiding(M))
								msg=new FullMsg(mob,M,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+R.name()+" '"+combinedCommands+"'.^?",
												CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+R.name()+" '"+combinedCommands+"'.^?",
												CMMsg.NO_EFFECT,null);
							else
								msg=new FullMsg(mob,M,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+R.name()+" '"+combinedCommands+"'.^?",
												CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) something around "+R.name()+".^?",
												CMMsg.NO_EFFECT,null);
							if(mob.location().okMessage(mob,msg))
								mob.location().sendOthers(mob,msg);
						}
					}
				}
			}
		}
		else
		{
			msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'.^?"
										   ,CMMsg.MSG_SPEAK,"^T<S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'.^?"
										   ,CMMsg.MSG_QUIETMOVEMENT,"^T<S-NAME> whisper(s) something to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
