package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.*;
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
		ExternalPlay.vassals(mob,mob.name());
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
					mob.tell(mob,target,"You tell "+targetName+" '"+text+"'");
					target.tell(mob,target,mob.name()+" tell(s) you '"+text+"'");
					target.setReplyTo(mob);
				}
			}
			else
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),Affect.NO_EFFECT,null);
				if(location.okAffect(msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"));
			if(location.okAffect(msg))
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
		if(mob.location().okAffect(msg))
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
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}

	public static void cmdSay(MOB mob, Vector commands)
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
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void yell(MOB mob, Vector commands)
	{
		Vector newCommands=Util.parse(Util.combine(commands,0).toUpperCase());
		cmdSay(mob,newCommands);
	}

	public static void report(MOB mob)
	{
		cmdSay(mob,Util.parse("say \"I have "+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+" hit points, "+mob.curState().getMana()+"/"+mob.maxState().getMana()+" mana, "+mob.curState().getMovement()+"/"+mob.maxState().getMovement()+" move, and I've scored "+mob.getExperience()+" exp.\""));
	}
	
	public static void reply(MOB mob, Vector commands)
	{
		if(mob==null) return;
		if(mob.replyTo()==null)
		{
			mob.tell("No one has told you anything yet!");
			return;
		}
		if((mob.replyTo().name().indexOf("@")<0)
		&&(CMMap.MOBs.get(mob.replyTo().name())==null))
		{
			mob.tell(mob.replyTo().name()+" is no longer logged in.");
			return;
		}
		quickSay(mob,mob.replyTo(),Util.combine(commands,1),true,!mob.location().isInhabitant(mob.replyTo()));
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
		mob.tell("You tell "+target.name()+" '"+combinedCommands+"'");
		// deafness does not matter!
		target.tell(mob.name()+" tells you '"+combinedCommands+"'");
		target.setReplyTo(mob);
	}

	public static void doSocial(Social social, MOB mob, Vector commands)
	{
		String targetStr="";
		if((commands.size()>1)&&(!((String)commands.elementAt(1)).equalsIgnoreCase("SELF")))
			targetStr=(String)commands.elementAt(1);

		Environmental Target=mob.location().fetchFromRoomFavorMOBs(null,targetStr,Item.WORN_REQ_ANY);
		if((Target!=null)&&(!Sense.canBeSeenBy(Target,mob)))
		   Target=null;

		String You_see=social.You_see;
		if((You_see!=null)&&(You_see.trim().length()==0)) You_see=null;
		String Third_party_sees=social.Third_party_sees;
		if((Third_party_sees!=null)&&(Third_party_sees.trim().length()==0)) Third_party_sees=null;
		String Target_sees=social.Target_sees;
		if((Target_sees!=null)&&(Target_sees.trim().length()==0)) Target_sees=null;
		String See_when_no_target=social.See_when_no_target;
		if((See_when_no_target!=null)&&(See_when_no_target.trim().length()==0)) See_when_no_target=null;
		if((Target==null)&&(targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,null,social.sourceCode,You_see,Affect.NO_EFFECT,null,social.othersCode,Third_party_sees);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		if((Target==null)&&(!targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,null,social.sourceCode,See_when_no_target,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,Target,null,social.sourceCode,You_see,social.targetCode,Target_sees,social.othersCode,Third_party_sees);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
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
			FullMsg newMsg=new FullMsg(mob,recipient,giveThis,Affect.MSG_GIVE,"<S-NAME> give(s) "+giveThis.name()+" to <T-NAMESELF>.");
			if(mob.location().okAffect(newMsg))
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
		if(mob.location().okAffect(msg))
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
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
}
