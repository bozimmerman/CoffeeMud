package com.planet_ink.coffee_mud.commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.sysop.CreateEdit;
import com.planet_ink.coffee_mud.commands.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Grouping
{
	public void who(MOB mob, String mobName)
	{
		if((mobName!=null)&&(mobName.length()==0))
		{
			mob.tell("whois whom?");
			return;
		}

		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			if((thisSession.mob()!=null)&&(!thisSession.killFlag())&&((mobName==null)||(thisSession.mob().name().toUpperCase().startsWith(mobName.toUpperCase()))))
			{
				msg.append(showWho(thisSession.mob(),true));
			}
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("[");
			head.append(Util.padRight("Race",8)+" ");
			head.append(Util.padRight("Class",8)+" ");
			head.append(Util.padRight("Lvl",4));
			head.append("] Character name\n\r");
			mob.tell(head.toString()+msg.toString());
		}
	}

	public StringBuffer showWho(MOB who, boolean shortForm)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().getMyRace().name(),8)+" ");
		msg.append(Util.padRight(who.charStats().getMyClass().name(),8)+" ");
		msg.append(Util.padRight(Integer.toString(who.envStats().level()),4));
		msg.append("] "+Util.padRight(who.name(),15));
		if(!shortForm)
		{
			msg.append(Util.padRight("hp("+Util.padRight(""+who.curState().getHitPoints(),3)+"/"+Util.padRight(""+who.maxState().getHitPoints(),3)+")",12));
			msg.append(Util.padRight("mn("+Util.padRight(""+who.curState().getMana(),3)+"/"+Util.padRight(""+who.maxState().getMana(),3)+")",12));
			msg.append(Util.padRight("mv("+Util.padRight(""+who.curState().getMovement(),3)+"/"+Util.padRight(""+who.maxState().getMovement(),3)+")",12));
		}
		msg.append("\n\r");
		return msg;
	}


	private void addFollowers(MOB mob, Hashtable toThis)
	{
		if(toThis.get(mob)==null)
		   	toThis.put(mob,mob);

		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if(toThis.get(follower)==null)
			{
				toThis.put(follower,follower);
				addFollowers(follower,toThis);
			}
		}
	}

	public Hashtable getGroupMembers(MOB mob)
	{
		Hashtable followers=new Hashtable();
		addFollowers(mob,followers);
		if(mob.amFollowing()!=null)
			addFollowers(mob.amFollowing(),followers);
		return followers;
	}

	public void group(MOB mob)
	{
		mob.tell(mob.name()+"'s group:\n\r");
		Hashtable group=getGroupMembers(mob);
		StringBuffer msg=new StringBuffer("");
		for(Enumeration e=group.elements();e.hasMoreElements();)
		{
			MOB follower=(MOB)e.nextElement();
			msg.append(showWho(follower,false));
		}
		mob.tell(msg.toString());
	}

	public void gtell(MOB mob, String text)
	{
		if(text.length()==0)
		{
			mob.tell("Tell the group what?");
			return;
		}

		Hashtable group=getGroupMembers(mob);
		mob.tell("Ok.");
		for(Enumeration e=group.elements();e.hasMoreElements();)
		{
			MOB target=(MOB)e.nextElement();
			target.tell(mob.name()+" tell(s) the group '"+text+"'.");
		}
	}

	public void nofollow(MOB mob, boolean errorsOk)
	{
		if(mob.amFollowing()!=null)
		{
			FullMsg msg=new FullMsg(mob,mob.amFollowing(),null,Affect.MSG_OK_ACTION,"<S-NAME> stop(s) following <T-NAMESELF>.");
			mob.setFollowing(null);
			// no OKaffects, since the damn leader may not be here.
			mob.location().send(mob,msg);
		}
		else
		if(errorsOk)
		{
			mob.tell("You aren't following anyone!");
			return;
		}
	}

	public void follow(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Follow whom?");
			return;
		}
		String whomToFollow=Util.combine(commands,1);
		MOB target=mob.location().fetchInhabitant(whomToFollow);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see them here.");
			return;
		}
		if(mob.amFollowing()==target)
		{
			mob.tell("You are already following "+target.name()+"!");
			return;
		}
		nofollow(mob,false);
		FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_ACTION,"<S-NAME> follow(s) <T-NAMESELF>.");
		if(mob.location().okAffect(msg))
		{
			mob.setFollowing(target);
			mob.location().send(mob,msg);
		}
	}

	public void order(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()<3)
		{
			mob.tell("Order who do to what?");
			return;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("Order them to do what?");
			return;
		}
		String whomToFollow=(String)commands.elementAt(0);
		MOB target=mob.fetchFollower(whomToFollow);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("They don't seem to be following you.");
			return;
		}
		else
		if(target.location()!=mob.location())
		{
			mob.tell("They are not here.");
			return;
		}
		commands.removeElementAt(0);
		FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"<S-NAME> order(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"'.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			ExternalPlay.doCommand(target,commands);
		}
	}

	public void split(MOB mob, Vector commands)
	{
		if(mob.numFollowers()==0)
		{
			mob.tell("You have no followers!");
			return;
		}
		if(commands.size()<2)
		{
			mob.tell("Split how much gold?");
			return;
		}
		int gold=Util.s_int((String)commands.elementAt(1));
		if(gold<0)
		{
			mob.tell("Split how much gold?!?");
			return;
		}

		int num=0;
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB recipient=mob.fetchFollower(f);
			if((recipient.amFollowing()==mob)&&(!recipient.isMonster()))
				num++;
		}

		gold=(int)Math.floor(Util.div(gold,num+1));

		if((gold*num)>mob.getMoney())
		{
			mob.tell("You don't have that much gold.");
			return;
		}
		boolean eligible=false;
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB recipient=mob.fetchFollower(f);
			if((recipient.amFollowing()==mob)&&(!recipient.isMonster()))
			{
				mob.setMoney(mob.getMoney()-gold);
				Item C=(Item)CMClass.getItem("Coins");
				C.baseEnvStats().setAbility(gold);
				C.recoverEnvStats();
				mob.addInventory(C);
				FullMsg newMsg=new FullMsg(mob,recipient,C,Affect.MSG_GIVE,"<S-NAME> give(s) "+C.name()+" to <T-NAMESELF>.");
				if(mob.location().okAffect(newMsg))
					mob.location().send(mob,newMsg);
				eligible=true;
			}
		}
		if(!eligible) mob.tell("Noone appears to be eligible to receive any of your gold.");
	}
}
