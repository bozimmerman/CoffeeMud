package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClanWho extends Who
{
	public ClanWho(){}

	private String[] access={"CLANWHO","CLWH"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(Clans.getClan(mob.getClanID())==null))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		else
		{
			StringBuffer msg=new StringBuffer("");
			for(int s=0;s<Sessions.size();s++)
			{
				Session thisSession=(Session)Sessions.elementAt(s);
				MOB mob2=thisSession.mob();
				if((mob2!=null)&&(mob2.soulMate()!=null))
					mob2=mob2.soulMate();

				if((mob2!=null)
				&&(!thisSession.killFlag())
				&&((((mob2.envStats().disposition()&EnvStats.IS_NOT_SEEN)==0)
					||(mob.isASysOp(null))))
				&&(mob2.getClanID().equals(mob.getClanID()))
				&&(mob2.envStats().level()>0))
					msg.append(showWhoShort(mob2));
			}
			mob.tell(shortHead+msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
