package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Who extends StdCommand
{
	public Who(){}

	private String[] access={"WHO","WH"};
	public String[] getAccessWords(){return access;}
	
	protected static final String shortHead=
		 "^x["
		+Util.padRight("Race",12)+" "
		+Util.padRight("Class",12)+" "
		+Util.padRight("Level",7)
		+"] Character name^.^N\n\r";
		 
	
	public StringBuffer showWhoShort(MOB who)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().raceName(),12)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		msg.append(Util.padRight(who.charStats().displayClassName(),12)+" ");
		msg.append(Util.padRight(levelStr,7));
		String name=null;
		if(Util.bset(who.envStats().disposition(),EnvStats.IS_NOT_SEEN))
			name="("+who.name()+")";
		else
			name=who.name();
		if((who.session()!=null)&&(who.session().afkFlag()))
		{
			long t=(who.session().getIdleMillis()/1000);
			String s=t+"s";
			if(t>600)
			{
				t=t/60;
				s=t+"m";
				if(t>120)
				{
					t=t/60;
					s=t+"h";
					if(t>48)
					{
						t=t/24;
						s=t+"d";
					}
				}
			}
			name=name+(" (idle: "+s+")");
		}
		msg.append("] "+Util.padRight(name,35));
		msg.append("\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String mobName=Util.combine(commands,1);
		if((mobName!=null)&&(mobName.startsWith("@")))
		{
			if(!(CMClass.I3Interface().i3online()))
				mob.tell("I3 is unavailable.");
			else
				CMClass.I3Interface().i3who(mob,mobName.substring(1));
			return false;
		}

		HashSet friends=null;
		if((mobName!=null)
		&&(mobName.equalsIgnoreCase("friends"))
		&&(mob.playerStats()!=null))
		{
			friends=mob.playerStats().getFriends();
			mobName=null;
		}

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
				||(CMSecurity.isAllowedAnywhere(mob,"WIZINV"))))
			&&((friends==null)||(friends.contains(mob2.Name())||(friends.contains("All"))))
			&&(mob2.envStats().level()>0))
				msg.append(showWhoShort(mob2));
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(Util.padRight("Race",12)+" ");
			head.append(Util.padRight("Class",12)+" ");
			head.append(Util.padRight("Level",7));
			head.append("] Character name^.^N\n\r");
			mob.tell(head.toString()+msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
