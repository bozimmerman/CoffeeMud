package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.CreateEdit;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Grouping
{
	public void whois(MOB mob, String mobName)
	{
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("whois whom?");
			return;
		}
		
		if(mobName.startsWith("@"))
		{
			if(!(ExternalPlay.i3().i3online()))
				mob.tell("I3 is unavailable.");
			else
				ExternalPlay.i3().i3who(mob,mobName.substring(1));
			return;
		}
		
		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			MOB mob2=thisSession.mob();
			if((mob2!=null)
			&&(!thisSession.killFlag())
			&&((Sense.isSeen(mob2)||(mob.isASysOp(null))))
			&&(mob2.envStats().level()>0)
			&&(mob2.name().toUpperCase().startsWith(mobName.toUpperCase())))
				msg.append(showWho(mob2,true));
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(Util.padRight("Race",8)+" ");
			head.append(Util.padRight("Class",12)+" ");
			head.append(Util.padRight("Lvl",5));
			head.append("] Character name^N^.\n\r");
			mob.tell(head.toString()+msg.toString());
		}
	}
	public void who(MOB mob, String mobName)
	{
		if((mobName!=null)&&(mobName.startsWith("@")))
		{
			if(!(ExternalPlay.i3().i3online()))
				mob.tell("I3 is unavailable.");
			else
				ExternalPlay.i3().i3who(mob,mobName.substring(1));
			return;
		}

		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			MOB mob2=thisSession.mob();
			if((mob2!=null)
			&&(!thisSession.killFlag())
			&&((Sense.isSeen(mob2)||(mob.isASysOp(null))))
			&&(mob2.envStats().level()>0))
				msg.append(showWho(mob2,true));
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(Util.padRight("Race",8)+" ");
			head.append(Util.padRight("Class",12)+" ");
			head.append(Util.padRight("Lvl",5));
			head.append("] Character name^N^.\n\r");
			mob.tell(head.toString()+msg.toString());
		}
	}

	
	public void makePeaceInGroup(MOB mob)
	{
		Hashtable myGroup=mob.getGroupMembers(new Hashtable());
		for(Enumeration e=myGroup.elements();e.hasMoreElements();)
		{
			MOB mob2=(MOB)e.nextElement();
			if(mob2.isInCombat()&&(myGroup.contains(mob2.getVictim())))
				mob2.makePeace();
		}
	}
	
	public StringBuffer showWho(MOB who, boolean shortForm)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().getMyRace().name(),8)+" ");
		int classLevel=who.charStats().getClassLevel(who.charStats().getCurrentClass());
		String levelStr=null;
		if(classLevel>=who.envStats().level())
			levelStr=""+who.envStats().level();
		else
			levelStr=classLevel+"/"+who.envStats().level();
		msg.append(Util.padRight(who.charStats().getCurrentClass().name(),12)+" ");
		msg.append(Util.padRight(levelStr,5));
		msg.append("] "+Util.padRight(who.name(),15));
		if(!shortForm)
		{
			msg.append(Util.padRightPreserve("hp("+Util.padRightPreserve(""+who.curState().getHitPoints(),3)+"/"+Util.padRightPreserve(""+who.maxState().getHitPoints(),3)+")",12));
			msg.append(Util.padRightPreserve("mn("+Util.padRightPreserve(""+who.curState().getMana(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMana(),3)+")",12));
			msg.append(Util.padRightPreserve("mv("+Util.padRightPreserve(""+who.curState().getMovement(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMovement(),3)+")",12));
		}
		msg.append("\n\r");
		return msg;
	}


	public void group(MOB mob)
	{
		mob.tell(mob.name()+"'s group:\n\r");
		Hashtable group=mob.getGroupMembers(new Hashtable());
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

		Hashtable group=mob.getGroupMembers(new Hashtable());
		for(Enumeration e=group.elements();e.hasMoreElements();)
		{
			MOB target=(MOB)e.nextElement();
			target.tell(mob.name()+" tell(s) the group '"+text+"'.");
		}
	}

	public void nofollow(MOB mob, boolean errorsOk, boolean quiet)
	{
		if(mob.amFollowing()!=null)
		{
			FullMsg msg=new FullMsg(mob,mob.amFollowing(),null,Affect.MSG_NOFOLLOW,quiet?null:"<S-NAME> stop(s) following <T-NAMESELF>.");
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

	public void processFollow(MOB mob, MOB tofollow, boolean quiet)
	{
		if(tofollow!=null)
		{
			if(tofollow==mob)
			{
				nofollow(mob,true,false);
				return;
			}
			if(mob.getGroupMembers(new Hashtable()).contains(tofollow))
			{
				if(!quiet)
					mob.tell("You are already a member of "+tofollow.name()+"'s group!");
				return;
			}
			nofollow(mob,false,false);
			FullMsg msg=new FullMsg(mob,tofollow,null,Affect.MSG_FOLLOW,quiet?null:"<S-NAME> follow(s) <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
			nofollow(mob,!quiet,quiet);
	}

	public void follow(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Follow whom?");
			return;
		}
		String whomToFollow=Util.combine(commands,1);
		if(whomToFollow.equalsIgnoreCase("self"))
		{
			nofollow(mob,true,false);
			return;
		}
		MOB target=mob.location().fetchInhabitant(whomToFollow);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see them here.");
			return;
		}
		if(target.isMonster())
		{
			mob.tell("You cannot follow '"+target.name()+"'.");
			return;
		}
		if((target.getBitmap()&MOB.ATT_NOFOLLOW)>0)
		{
			mob.tell(target.name()+" is not accepting followers.");
			return;
		}
		processFollow(mob,target,false);
	}

	public void togglenofollow(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_NOFOLLOW)==0)
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_NOFOLLOW);
			for(int i=0;i<mob.numFollowers();i++)
			{
				MOB follower=mob.fetchFollower(i);
				if((follower!=null)&&(follower.amFollowing()!=null)&&(follower.amFollowing()==mob))
					nofollow(follower,false,false);
			}
			mob.tell("You are no longer accepting followers.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_NOFOLLOW);
			mob.tell("You are now accepting followers.");
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
		String whomToOrder=(String)commands.elementAt(0);
		MOB target=target=mob.location().fetchInhabitant(whomToOrder);
		boolean canFollow=false;
		if((target!=null)
		&&((mob.isASysOp(mob.location()))
			||(target.amFollowing()==mob)
			||(target.getLeigeID().equals(mob.name()))))
		   canFollow=true;
		
		if((target==null)
		&&(Sense.canBeSeenBy(target,mob))
		&&(Sense.canBeHeardBy(mob,target))
		&&(target.location()==mob.location()))
		{
			mob.tell("'"+whomToOrder+"' doesn't seem to be here.");
			return;
		}
		else
		if(!canFollow)
		{
			mob.tell("You can't order '"+whomToOrder+"' around.");
			return;
		}
		commands.removeElementAt(0);
		Integer commandCodeObj=(Integer)(new CommandSet()).get((String)commands.elementAt(0));
		if((!mob.isASysOp(mob.location()))
		   &&(commandCodeObj!=null)
		   &&(commandCodeObj.intValue()==CommandSet.ORDER))
		{
			mob.tell("You cannot order someone to follow.");
			return;
		}
		FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> order(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"'^?.");
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
			if((recipient!=null)
			   &&(recipient.amFollowing()==mob)
			   &&(!recipient.isMonster()))
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
			if((recipient!=null)
			   &&(recipient.amFollowing()==mob)
			   &&(!recipient.isMonster()))
			{
				mob.setMoney(mob.getMoney()-gold);
				Item C=(Item)CMClass.getItem("StdCoins");
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
