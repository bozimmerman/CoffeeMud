package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.*;
import java.util.*;

public class SocialProcessor
{
	private SocialProcessor(){}

	public static Item possibleGold(MOB mob, String itemID)
	{
		if(itemID.toUpperCase().trim().endsWith(" COINS"))
			itemID=itemID.substring(0,itemID.length()-6);
		if(itemID.toUpperCase().trim().endsWith(" GOLD"))
			itemID=itemID.substring(0,itemID.length()-5);
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		int gold=Util.s_int(itemID);
		if(gold>0)
		{
			if(mob.getMoney()>=gold)
			{
				mob.setMoney(mob.getMoney()-gold);
				Item C=(Item)CMClass.getItem("StdCoins");
				C.baseEnvStats().setAbility(gold);
				C.recoverEnvStats();
				mob.addInventory(C);
				return C;
			}
			else
				mob.tell("You don't have that much gold.");
		}
		return null;
	}

	public static void vassals(MOB mob, Vector commands)
	{
		mob.tell("The following players are in your service:");
		ExternalPlay.vassals(mob,mob.Name());
	}

	public static void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		Room location=mob.location();
		if(target!=null)
			location=target.location();
		if(location==null) return;
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				String targetName=target.name();
				if(targetName.indexOf("@")>=0)
				{
					String mudName=targetName.substring(targetName.indexOf("@")+1);
					targetName=targetName.substring(0,targetName.indexOf("@"));
					if(!(ExternalPlay.i3().i3online()))
						mob.tell("I3 is unavailable.");
					else
						ExternalPlay.i3().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_TELL,"^TYou tell "+target.name()+" '"+text+"'^?^.",Affect.MSG_TELL,"^T"+mob.name()+" tell(s) you '"+text+"'^?^.",Affect.NO_EFFECT,null);
					if((mob.location().okAffect(mob,msg))
					&&(target.okAffect(target,msg)))
					{
						mob.affect(mob,msg);
						if(mob!=target)
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
							target.setReplyTo(mob);
						}
					}
				}
			}
			else
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),Affect.NO_EFFECT,null);
				if(location.okAffect(mob,msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"));
			if(location.okAffect(mob,msg))
				location.send(mob,msg);
		}
	}

	public static void hire(MOB mob, String rest)
	{
		Environmental target=mob.location().fetchFromRoomFavorMOBs(null,rest,Item.WORN_REQ_ANY);
		if((target!=null)&&(!target.name().equalsIgnoreCase(rest))&&(rest.length()<4))
		   target=null;
		if((target!=null)&&(!Sense.canBeSeenBy(target,mob)))
			target=null;
		FullMsg msg=null;
		if(target==null)
			msg=new FullMsg(mob,null,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) 'I'm looking to hire some help.'^?");
		else
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) to <T-NAMESELF> 'Are you for hire?'^?");
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void fire(MOB mob, String rest)
	{
		Environmental target=mob.location().fetchFromRoomFavorMOBs(null,rest,Item.WORN_REQ_ANY);
		if((target!=null)&&(!target.name().equalsIgnoreCase(rest))&&(rest.length()<4))
		   target=null;
		if((target!=null)&&(!Sense.canBeSeenBy(target,mob)))
			target=null;
		if(target==null)
			mob.tell("Fire whom?");
		else
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) to <T-NAMESELF> 'You are fired!'^?");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
	}

	public static void say(MOB mob, Vector commands)
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
			return;
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
			return;
		}

		FullMsg msg=null;
		if(target==null)
			msg=new FullMsg(mob,null,null,Affect.MSG_SPEAK,"^T<S-NAME> "+theWord.toLowerCase()+"(s) '"+combinedCommands+"'^?");
		else
		if(theWord.equalsIgnoreCase("ask"))
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> ask(s) <T-NAMESELF> '"+combinedCommands+"'^?");
		else
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> "+theWord.toLowerCase()+"(s) to <T-NAMESELF> '"+combinedCommands+"'^?");
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
		if(theWord.equalsIgnoreCase("Yell"))
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R=mob.location().getRoomInDir(d);
				Exit E=mob.location().getExitInDir(d);
				if((R!=null)&&(E!=null)&&(E.isOpen()))
				{
					msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^TYou hear someone yell '"+combinedCommands+"' "+Directions.getInDirectionName(Directions.getOpDirectionCode(d))+"^?");
					if(R.okAffect(mob,msg))
						R.sendOthers(mob,msg);
				}
			}
	}

	public static void whisper(MOB mob, Vector commands)
	{
		if(commands.size()==1)
		{
			mob.tell("Whisper what?");
			return;
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
			return;
		}

		FullMsg msg=null;
		if(target==null)
		{
			Rideable R=mob.riding();
			if(R==null)
			{
				msg=new FullMsg(mob,null,null,Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) to himself '"+combinedCommands+"'^?",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
			}
			else
			{
				msg=new FullMsg(mob,R,null,Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'^?",
								Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) around <T-NAMESELF> '"+combinedCommands+"'^?",
								Affect.NO_EFFECT,null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					for(int r=0;r<R.numRiders();r++)
					{
						Rider M=R.fetchRider(r);
						if(M!=null)
						{
							msg=new FullMsg(mob,M,null,Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+R.name()+" '"+combinedCommands+"'^?",
											Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) around "+R.name()+" '"+combinedCommands+"'^?",
											Affect.NO_EFFECT,null);
							if(mob.location().okAffect(mob,msg))
								mob.location().sendOthers(mob,msg);
						}
					}
				}
			}
		}
		else
		{
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'^?"
										   ,Affect.MSG_SPEAK,"^T<S-NAME> whisper(s) to <T-NAMESELF> '"+combinedCommands+"'^?"
										   ,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
	}

	public static void yell(MOB mob, Vector commands)
	{
		Vector newCommands=Util.parse(Util.combine(commands,0).toUpperCase());
		say(mob,newCommands);
	}

	public static void report(MOB mob)
	{
		say(mob,Util.parse("say \"I have "+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+" hit points, "+mob.curState().getMana()+"/"+mob.maxState().getMana()+" mana, "+mob.curState().getMovement()+"/"+mob.maxState().getMovement()+" move, and I've scored "+mob.getExperience()+" exp.\""));
	}

	public static void reply(MOB mob, Vector commands)
	{
		if(mob==null) return;
		if(mob.replyTo()==null)
		{
			mob.tell("No one has told you anything yet!");
			return;
		}
		if((mob.replyTo().Name().indexOf("@")<0)
		&&(!mob.replyTo().isMonster())
		&&(CMMap.getPlayer(mob.replyTo().Name())==null))
		{
			mob.tell(mob.replyTo().Name()+" is no longer logged in.");
			return;
		}
		quickSay(mob,mob.replyTo(),Util.combine(commands,1),true,!mob.location().isInhabitant(mob.replyTo()));
		if((mob.replyTo().session()!=null)
		&&(mob.replyTo().session().afkFlag()))
			mob.tell(mob.replyTo().name()+" is AFK at the moment.");
	}

	public static void tell(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Tell whom what?");
			return;
		}
		commands.removeElementAt(0);
		MOB target=null;
		String targetName=((String)commands.elementAt(0)).toUpperCase();
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			if((thisSession.mob()!=null)&&(!thisSession.killFlag())&&(thisSession.mob().name().equalsIgnoreCase(targetName)))
			{
				target=thisSession.mob();
				break;
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
			mob.tell("Tell them what?");
			return;
		}
		if(target==null)
		{
			if(targetName.indexOf("@")>=0)
			{
				String mudName=targetName.substring(targetName.indexOf("@")+1);
				targetName=targetName.substring(0,targetName.indexOf("@"));
				if(!(ExternalPlay.i3().i3online()))
					mob.tell("I3 is unavailable.");
				else
					ExternalPlay.i3().i3tell(mob,targetName,mudName,combinedCommands);
				return;
			}
			else
			{
				mob.tell("That person doesn't appear to be online.");
				return;
			}
		}
		quickSay(mob,target,combinedCommands,true,true);
		if((target.session()!=null)
		&&(target.session().afkFlag()))
			mob.tell(target.name()+" is AFK at the moment.");
	}

	public static void give(MOB mob, Vector commands, boolean involuntarily)
	{
		if(commands.size()<2)
		{
			mob.tell("Give what to whom?");
			return;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("To whom should I give that?");
			return;
		}

		MOB recipient=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see "+(String)commands.elementAt(commands.size()-1)+" here.");
			return;
		}
		commands.removeElementAt(commands.size()-1);
		if((commands.size()>0)&&(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("to")))
			commands.removeElementAt(commands.size()-1);

		String thingToGive=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		do
		{
			Environmental giveThis=new SocialProcessor().possibleGold(mob,thingToGive);
			if(giveThis!=null)
				allFlag=false;
			else
				giveThis=mob.fetchCarried(null,thingToGive+addendumStr);
			if((giveThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				giveThis=mob.fetchWornItem(thingToGive);
				if(giveThis!=null)
				{
					if((!((Item)giveThis).amWearingAt(Item.HELD))&&(!((Item)giveThis).amWearingAt(Item.WIELD)))
					{
						mob.tell("You must remove that first.");
						return;
					}
					else
					if(!ItemUsage.remove(mob,((Item)giveThis),true))
						return;
				}
			}
			if(giveThis==null) break;
			if(Sense.canBeSeenBy(giveThis,mob))
				V.addElement(giveThis);
			addendumStr="."+(++addendum);
		}
		while(allFlag);

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental giveThis=(Environmental)V.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,recipient,giveThis,Affect.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			if(mob.location().okAffect(mob,newMsg))
				mob.location().send(mob,newMsg);
			else
			if(giveThis instanceof Coins)
				((Coins)giveThis).putCoinsBack();
		}
	}

	public static int relativeLevelDiff(MOB mob1, MOB mob2)
	{
		if((mob1==null)||(mob2==null)) return 0;
		int mob2Armor=(int)mob2.adjustedArmor();
		int mob1Armor=(int)mob1.adjustedArmor();
		int mob2Attack=(int)mob2.adjustedAttackBonus();
		int mob1Attack=(int)mob1.adjustedAttackBonus();
		int mob2Dmg=(int)mob2.envStats().damage();
		int mob1Dmg=(int)mob1.envStats().damage();
		int mob2Hp=(int)mob2.baseState().getHitPoints();
		int mob1Hp=(int)mob1.baseState().getHitPoints();


		double mob2HitRound=(((Util.div(CoffeeUtensils.normalizeBy5((mob2Attack+mob1Armor)),100.0))*Util.div(mob2Dmg,2.0))+1.0)*Util.mul(mob2.envStats().speed(),1.0);
		double mob1HitRound=(((Util.div(CoffeeUtensils.normalizeBy5((mob1Attack+mob2Armor)),100.0))*Util.div(mob1Dmg,2.0))+1.0)*Util.mul(mob1.envStats().speed(),1.0);
		double mob2SurvivalRounds=Util.div(mob2Hp,mob1HitRound);
		double mob1SurvivalRounds=Util.div(mob1Hp,mob2HitRound);

		//int levelDiff=(int)Math.round(Util.div((mob1SurvivalRounds-mob2SurvivalRounds),1));
		double levelDiff=mob1SurvivalRounds-mob2SurvivalRounds;
		int levelDiffed=(int)Math.round(Math.sqrt(Math.abs(levelDiff)));

		return levelDiffed*(levelDiff<0.0?-1:1);
	}

	public static void consider(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Consider whom?");
			return;
		}
		commands.removeElementAt(0);
		String targetName=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(targetName);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see '"+targetName+"' here.");
			return;
		}

		int relDiff=relativeLevelDiff(target,mob);
		int lvlDiff=(target.envStats().level()-mob.envStats().level());
		int realDiff=(relDiff+lvlDiff)/2;

		int levelDiff=Math.abs(realDiff);
		if(levelDiff<2)
		{
			mob.tell("The perfect match!");
			return;
		}
		else
		if(realDiff<0)
		{
			if(realDiff>-4)
			{
				mob.tell(target.charStats().HeShe()+" might give you a fight.");
				return;
			}
			else
			if(realDiff>-6)
			{
				mob.tell(target.charStats().HeShe()+" is hardly worth your while.");
				return;
			}
			else
			if(realDiff>-8)
			{
				mob.tell(target.charStats().HeShe()+" is a pushover.");
				return;
			}
			else
			{
				mob.tell(target.charStats().HeShe()+" is not worth the effort.");
				return;
			}

		}
		else
		if(realDiff<4)
		{
			mob.tell(target.charStats().HeShe()+" looks a little tough.");
			return;
		}
		else
		if(realDiff<6)
		{
			mob.tell(target.charStats().HeShe()+" is a serious threat.");
			return;
		}
		else
		if(realDiff<8)
		{
			mob.tell(target.charStats().HeShe()+" will clean your clock.");
			return;
		}
		else
		{
			mob.tell(target.charStats().HeShe()+" WILL KILL YOU DEAD!");
			return;
		}
	}

	public static void rebuke(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Rebuke whom?");
			return;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if(target==null)
		{
			if(mob.getWorshipCharID().length()>0)
				target=(MOB)CMMap.getDeity(Util.combine(commands,1));
			if((target==null)
			&&(!Util.combine(commands,1).equalsIgnoreCase(mob.getLeigeID()))
			&&(!Util.combine(commands,1).equalsIgnoreCase(mob.getWorshipCharID())))
			{
				mob.tell("There's nobody here called '"+Util.combine(commands,1)+"' and you aren't serving '"+Util.combine(commands,1)+"'.");
				return;
			}
		}

		FullMsg msg=null;
		if(target!=null)
			msg=new FullMsg(mob,target,null,Affect.MSG_REBUKE,"<S-NAME> rebuke(s) <T-NAMESELF>.");
		else
			msg=new FullMsg(mob,target,null,Affect.MSG_REBUKE,"<S-NAME> rebuke(s) "+mob.getLeigeID()+".");
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}
	public static void serve(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Serve whom?");
			return;
		}
		commands.removeElementAt(0);
		MOB recipient=mob.location().fetchInhabitant(Util.combine(commands,0));
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see "+Util.combine(commands,0)+" here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,recipient,null,Affect.MSG_SERVE,"<S-NAME> swear(s) fealty to <T-NAMESELF>.");
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}
}
