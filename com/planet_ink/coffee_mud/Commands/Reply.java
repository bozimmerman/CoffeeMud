package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Reply extends StdCommand
{
	public Reply(){}

	private String[] access={"REPLY","REP","RE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob==null) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		if(pstats.replyTo()==null)
		{
			mob.tell("No one has told you anything yet!");
			return false;
		}
		if((pstats.replyTo().Name().indexOf("@")<0)
		&&(!pstats.replyTo().isMonster())
		&&(CMMap.getPlayer(pstats.replyTo().Name())==null))
		{
			mob.tell(pstats.replyTo().Name()+" is no longer logged in.");
			return false;
		}
		if(Util.combine(commands,1).length()==0)
		{
			mob.tell("Tell '"+pstats.replyTo().Name()+" what?");
			return false;
		}
		CommonMsgs.say(mob,pstats.replyTo(),Util.combine(commands,1),true,!mob.location().isInhabitant(pstats.replyTo()));
		if((pstats.replyTo().session()!=null)
		&&(pstats.replyTo().session().afkFlag()))
			mob.tell("Note: "+pstats.replyTo().name()+" is AFK at the moment.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
