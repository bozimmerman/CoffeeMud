package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Possess extends StdCommand
{
	public Possess(){}

	private String[] access={"POSSESS","POSS"};
	public String[] getAccessWords(){return access;}

	public MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		return target;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}

		commands.removeElementAt(0);
		String MOBname=Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=mob.isASysOp(null)?CMMap.rooms():mob.location().getArea().getMap();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster()))
				{
					target=mob2;
					break;
				}
			}
		}
		if((target==null)||(!target.isMonster()))
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");

		Session s=mob.session();
		s.setMob(target);
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		CommonMsgs.look(target,true);
		target.tell("^HYour spirit has changed bodies...");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
