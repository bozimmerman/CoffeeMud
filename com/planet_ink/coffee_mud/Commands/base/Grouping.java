package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Grouping
{
	private Grouping(){}

	public static void whois(MOB mob, String mobName)
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
			head.append("] Character name^N^.\n\r");
			mob.tell(head.toString()+msg.toString());
		}
	}
	public static void who(MOB mob, String mobName)
	{
		if((mobName!=null)&&(mobName.startsWith("@")))
		{
			if(!(ExternalPlay.i3().i3online()))
				mob.tell("I3 is unavailable.");
			else
				ExternalPlay.i3().i3who(mob,mobName.substring(1));
			return;
		}

		Hashtable friends=null;
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
			&&((Sense.isSeen(mob2)||(mob.isASysOp(null))))
			&&((friends==null)||(friends.containsKey(mob2.Name())||(friends.containsKey("All"))))
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
			head.append("] Character name^N^.\n\r");
			mob.tell(head.toString()+msg.toString());
		}
	}


	public static void makePeaceInGroup(MOB mob)
	{
		Hashtable myGroup=mob.getGroupMembers(new Hashtable());
		for(Enumeration e=myGroup.elements();e.hasMoreElements();)
		{
			MOB mob2=(MOB)e.nextElement();
			if(mob2.isInCombat()&&(myGroup.contains(mob2.getVictim())))
				mob2.makePeace();
		}
	}

	public static StringBuffer showWhoLong(MOB who)
	{

		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().raceName(),7)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		msg.append(Util.padRight(who.charStats().displayClassName(),7)+" ");
		msg.append(Util.padRight(levelStr,5));
		msg.append("] "+Util.padRight(who.name(),13)+" ");
		msg.append(Util.padRightPreserve("hp("+Util.padRightPreserve(""+who.curState().getHitPoints(),3)+"/"+Util.padRightPreserve(""+who.maxState().getHitPoints(),3)+")",12));
		msg.append(Util.padRightPreserve("mn("+Util.padRightPreserve(""+who.curState().getMana(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMana(),3)+")",12));
		msg.append(Util.padRightPreserve("mv("+Util.padRightPreserve(""+who.curState().getMovement(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMovement(),3)+")",12));
		msg.append("\n\r");
		return msg;
	}
	public static StringBuffer showWhoShort(MOB who)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(Util.padRight(who.charStats().raceName(),12)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		msg.append(Util.padRight(who.charStats().displayClassName(),12)+" ");
		msg.append(Util.padRight(levelStr,7));
		String name=who.name();
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


	public static void group(MOB mob)
	{
		mob.tell(mob.name()+"'s group:\n\r");
		Hashtable group=mob.getGroupMembers(new Hashtable());
		StringBuffer msg=new StringBuffer("");
		for(Enumeration e=group.elements();e.hasMoreElements();)
		{
			MOB follower=(MOB)e.nextElement();
			msg.append(showWhoLong(follower));
		}
		mob.tell(msg.toString());
	}

	public static void gtell(MOB mob, String text)
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
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_TELL,"^T<S-NAME> tell(s) the group '"+text+"'^?^.",Affect.MSG_TELL,"^T<S-NAME> tell(s) the group '"+text+"'^?^.",Affect.NO_EFFECT,null);
			if((mob.location().okAffect(mob,msg))
			&&(target.okAffect(target,msg)))
			{
				target.affect(target,msg);
				if(msg.trailerMsgs()!=null)
				{
					for(int i=0;i<msg.trailerMsgs().size();i++)
					{
						Affect affect=(Affect)msg.trailerMsgs().elementAt(i);
						if((affect!=msg)&&(target.okAffect(target,affect)))
							target.affect(target,affect);
					}
					msg.trailerMsgs().clear();
				}
			}
		}
	}

	public static void unfollow(MOB mob, boolean quiet)
	{
		nofollow(mob,false,quiet);
		Vector V=new Vector();
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB F=mob.fetchFollower(f);
			if(F!=null) V.addElement(F);
		}
		for(int v=0;v<V.size();v++)
		{
			MOB F=(MOB)V.elementAt(v);
			nofollow(F,false,quiet);
		}
	}

	public static void nofollow(MOB mob, boolean errorsOk, boolean quiet)
	{
		if(mob.amFollowing()!=null)
		{
			FullMsg msg=new FullMsg(mob,mob.amFollowing(),null,Affect.MSG_NOFOLLOW,quiet?null:"<S-NAME> stop(s) following <T-NAMESELF>.");
			// no room OKaffects, since the damn leader may not be here.
			if(mob.okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		if(errorsOk)
		{
			mob.tell("You aren't following anyone!");
			return;
		}
	}

	public static void processFollow(MOB mob, MOB tofollow, boolean quiet)
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
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
			nofollow(mob,!quiet,quiet);
	}

	public static void follow(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Follow whom?");
			return;
		}
		String whomToFollow=Util.combine(commands,1);
		if((whomToFollow.equalsIgnoreCase("self"))
		   ||(mob.name().toUpperCase().startsWith(whomToFollow)))
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
		if((target.isMonster())&&(!mob.isMonster()))
		{
			mob.tell("You cannot follow '"+target.name()+"'.");
			return;
		}
		if(Util.bset(target.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.tell(target.name()+" is not accepting followers.");
			return;
		}
		processFollow(mob,target,false);
	}

	public static void togglenofollow(MOB mob)
	{
		if(!Util.bset(mob.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			unfollow(mob,false);
			mob.tell("You are no longer accepting followers.");
		}
		else
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_NOFOLLOW));
			mob.tell("You are now accepting followers.");
		}
	}

	public static void order(MOB mob, Vector commands)
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
		Vector V=new Vector();
		boolean allFlag=whomToOrder.equalsIgnoreCase("all");
		if(whomToOrder.toUpperCase().startsWith("ALL.")){ allFlag=true; whomToOrder="ALL "+whomToOrder.substring(4);}
		if(whomToOrder.toUpperCase().endsWith(".ALL")){ allFlag=true; whomToOrder="ALL "+whomToOrder.substring(0,whomToOrder.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			MOB target=target=mob.location().fetchInhabitant(whomToOrder+addendumStr);
			if(target==null) break;
			if((Sense.canBeSeenBy(target,mob))
			&&(target!=mob)
			&&(!V.contains(target)))
				V.addElement(target);
			addendumStr="."+(++addendum);
		}
		while(allFlag);

		MOB target=null;
		if(V.size()==1)
		{
			target=(MOB)V.firstElement();
			if((!Sense.canBeSeenBy(target,mob))
			||(!Sense.canBeHeardBy(mob,target))
			||(target.location()!=mob.location()))
			{
				mob.tell("'"+whomToOrder+"' doesn't seem to be listening.");
				return;
			}
			if(!target.willFollowOrdersOf(mob))
			{
				mob.tell("You can't order '"+target.name()+"' around.");
				return;
			}
		}

		commands.removeElementAt(0);
		Vector doV=new Vector();
		for(int v=0;v<V.size();v++)
		{
			target=(MOB)V.elementAt(v);
			if(target.willFollowOrdersOf(mob))
			{
				Integer commandCodeObj=(Integer)(CommandSet.getInstance()).get((String)commands.elementAt(0));
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> order(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"'^?.");
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					doV.addElement(target);
				}
			}
		}
		for(int v=0;v<doV.size();v++)
		{
			target=(MOB)doV.elementAt(v);
			ExternalPlay.doCommand(target,commands);
		}
	}

	public static void split(MOB mob, Vector commands)
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
				FullMsg newMsg=new FullMsg(mob,recipient,C,Affect.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
				if(mob.location().okAffect(mob,newMsg))
					mob.location().send(mob,newMsg);
				eligible=true;
			}
		}
		if(!eligible) mob.tell("No one appears to be eligible to receive any of your gold.");
	}

	public static void dress(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Dress whom in what?");
			return;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see "+whom+" here.");
			return;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=mob.fetchInventory(what);
			if((item==null)||(!Sense.canBeSeenBy(item,mob)))
			{
				mob.tell("I don't see "+what+" here.");
				return;
			}
			if(!item.amWearingAt(Item.INVENTORY))
			{
				mob.tell("You might want to remove that first.");
				return;
			}
			if(target.isInCombat())
			{
				mob.tell("Not while "+target.name()+" is in combat!");
				return;
			}
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_QUIETMOVEMENT,null);
			if(mob.location().okAffect(mob,msg))
			{
				if(ExternalPlay.drop(mob,item,true))
				{
					msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_GET,Affect.MSG_GET,Affect.MSG_GET,null);
					if(mob.location().okAffect(mob,msg))
					{
						mob.location().send(mob,msg);
						msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_WEAR,Affect.MSG_WEAR,Affect.MSG_WEAR,null);
						if(mob.location().okAffect(mob,msg))
						{
							mob.location().send(mob,msg);
							mob.location().show(mob,target,item,Affect.MSG_QUIETMOVEMENT,"<S-NAME> put(s) <O-NAME> on <T-NAMESELF>.");
						}
						else
							mob.tell("You cannot seem to get "+item.name()+" on "+target.name()+".");
					}
					else
						mob.tell("You cannot seem to get "+item.name()+" to "+target.name()+".");
				}
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
	}

	public static void undress(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Undress whom? What would you like to remove?");
			return;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see "+whom+" here.");
			return;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=target.fetchInventory(what);
			if((item==null)
			   ||(!Sense.canBeSeenBy(item,mob))
			   ||(item.amWearingAt(Item.INVENTORY)))
			{
				mob.tell(target.name()+" doesn't seem to be equipped with '"+what+"'.");
				return;
			}
			if(target.isInCombat())
			{
				mob.tell("Not while "+target.name()+" is in combat!");
				return;
			}
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_QUIETMOVEMENT,null);
			if(mob.location().okAffect(mob,msg))
			{
				msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_GET,Affect.MSG_GET,Affect.MSG_GET,null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_DROP,Affect.MSG_DROP,Affect.MSG_DROP,null);
					if(mob.location().okAffect(mob,msg))
					{
						mob.location().send(mob,msg);
						if(ExternalPlay.get(mob,null,item,true))
							mob.location().show(mob,target,item,Affect.MSG_QUIETMOVEMENT,"<S-NAME> take(s) <O-NAME> off <T-NAMESELF>.");
					}
					else
						mob.tell("You cannot seem to get "+item.name()+" off "+target.name()+".");
				}
				else
					mob.tell("You cannot seem to get "+item.name()+" off of "+target.name()+".");
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
	}

	public static void feed(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Feed who what?");
			return;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see "+whom+" here.");
			return;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=mob.fetchInventory(what);
			if((item==null)||(!Sense.canBeSeenBy(item,mob)))
			{
				mob.tell("I don't see "+what+" here.");
				return;
			}
			if(!item.amWearingAt(Item.INVENTORY))
			{
				mob.tell("You might want to remove that first.");
				return;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				mob.tell("You might want to try feeding them something edibile or drinkable.");
				return;
			}
			if(target.isInCombat())
			{
				mob.tell("Not while "+target.name()+" is in combat!");
				return;
			}
			FullMsg msg=new FullMsg(mob,target,item,Affect.MSG_NOISYMOVEMENT,"<S-NAME> feed(s) "+item.name()+" to <T-NAMESELF>.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if((ExternalPlay.drop(mob,item,true))
				   &&(mob.location().isContent(item)))
				{
					msg=new FullMsg(target,item,Affect.MASK_GENERAL|Affect.MSG_GET,null);
					target.location().send(target,msg);
					if(target.isMine(item))
					{
						if(item instanceof Food)
							msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_EAT,Affect.MSG_EAT,Affect.MSG_EAT,null);
						else
							msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_DRINK,Affect.MSG_DRINK,Affect.MSG_DRINK,null);
						if(target.location().okAffect(target,msg))
							target.location().send(target,msg);
						if(target.isMine(item))
						{
							msg=new FullMsg(target,item,null,Affect.MASK_GENERAL|Affect.MSG_DROP,Affect.MSG_DROP,Affect.MSG_DROP,null);
							if(mob.location().okAffect(mob,msg))
							{
								mob.location().send(mob,msg);
								ExternalPlay.get(mob,null,item,true);
							}
						}
					}
				}
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
	}
}
