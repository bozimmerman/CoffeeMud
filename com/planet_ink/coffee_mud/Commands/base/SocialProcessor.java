package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.*;
import java.util.*;

public class SocialProcessor
{

	public Item possibleGold(MOB mob, String itemID)
	{
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

	public void vassals(MOB mob, Vector commands)
	{
		mob.tell("The following players are in your service:");
		ExternalPlay.vassals(mob,mob.name());
	}
	
	public void quickSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		Room location=mob.location();
		if(target!=null)
			location=target.location();
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				mob.tell(mob,target,"You tell "+target.name()+" '"+text+"'.");
				target.tell(mob,target,mob.name()+" tell(s) you '"+text+"'.");
				target.setReplyTo(mob);
			}
			else
			{
				FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>."));
				if(mob.okAffect(msg)&&target.okAffect(msg))
				{
					mob.affect(msg);
					target.affect(msg);
				}
			}
		}
		else
		if(!isPrivate)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>."));
			if(location.okAffect(msg))
				location.send(mob,msg);
		}
	}


	public void cmdSay(MOB mob, Vector commands)
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
			target=mob.location().fetchFromRoomFavorMOBs(null,possibleTarget);
			if((target!=null)&&(!target.name().equalsIgnoreCase(possibleTarget))&&(possibleTarget.length()<4))
			   target=null;
			if((target!=null)&&(Sense.canBeSeenBy(target,mob)))
				commands.removeElementAt(1);
			else
				target=null;
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+" what?");
			return;
		}

		FullMsg msg=null;
		if(target==null)
			msg=new FullMsg(mob,null,null,Affect.MSG_SPEAK,"<S-NAME> "+theWord.toLowerCase()+"(s) '"+combinedCommands+"'.");
		else
		if(theWord.equalsIgnoreCase("ask"))
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"<S-NAME> ask(s) <T-NAMESELF> '"+combinedCommands+"'.");
		else
			msg=new FullMsg(mob,target,null,Affect.MSG_SPEAK,"<S-NAME> "+theWord.toLowerCase()+"(s) to <T-NAMESELF> '"+combinedCommands+"'.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void yell(MOB mob, Vector commands)
	{
		Vector newCommands=Util.parse(Util.combine(commands,0).toUpperCase());
		cmdSay(mob,newCommands);
	}

	public void report(MOB mob)
	{
		cmdSay(mob,Util.parse("say \"I have "+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+" hit points, "+mob.curState().getMana()+"/"+mob.maxState().getMana()+" mana, "+mob.curState().getMovement()+"/"+mob.maxState().getMovement()+" move, and I've scored "+mob.getExperience()+" exp.\""));
	}
	public void tell(MOB mob, Vector commands)
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
		if(target==null)
		{
			mob.tell("That person doesn't appear to be online.");
			return;
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Tell them what?");
			return;
		}
		mob.tell("You tell "+target.name()+" '"+combinedCommands+"'");
		// deafness does not matter!
		target.tell(mob.name()+" tells you '"+combinedCommands+"'");
		target.setReplyTo(mob);
	}

	public void doSocial(Social social, MOB mob, Vector commands)
	{
		String targetStr="";
		if((commands.size()>1)&&(!((String)commands.elementAt(1)).equalsIgnoreCase("SELF")))
			targetStr=(String)commands.elementAt(1);

		Environmental Target=mob.location().fetchFromRoomFavorMOBs(null,targetStr);
		if((Target!=null)&&(!Sense.canBeSeenBy(Target,mob)))
		   Target=null;

		if((Target==null)&&(targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,null,social.sourceCode,social.You_see,Affect.NO_EFFECT,null,social.othersCode,social.Third_party_sees);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		if((Target==null)&&(!targetStr.equals("")))
		{
			FullMsg msg=new FullMsg(mob,null,null,social.sourceCode,social.See_when_no_target,Affect.NO_EFFECT,null,social.othersCode,social.See_when_no_target);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,Target,null,social.sourceCode,social.You_see,social.targetCode,social.Target_sees,social.othersCode,social.Third_party_sees);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}

	public void give(MOB mob, Vector commands, boolean involuntarily)
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

		String itemID=Util.combine(commands,0);


		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		int addendum=1;
		String addendumStr="";
		Environmental last=null;
		do
		{
			Item giveThis=possibleGold(mob,itemID+addendumStr);
			if(giveThis!=null)
				allFlag=false;
			else
			{
				giveThis=mob.fetchCarried(null,itemID+addendumStr);
				if((giveThis!=null)&&(mob.isMine(giveThis)))
				{
					((Item)giveThis).setLocation(null);
					((Item)giveThis).remove();
				}
			}
			if((giveThis==null)||(!Sense.canBeSeenBy(giveThis,mob)))
			{
				if((!doneSomething)&&(Util.s_int(itemID)<=0))
					mob.tell("You aren't carrying that.");
				return;
			}
			else
			if(last==giveThis)
			{
				addendumStr="."+(++addendum);
				continue;
			}

			FullMsg newMsg=new FullMsg(mob,recipient,giveThis,Affect.MSG_GIVE,"<S-NAME> give(s) "+giveThis.name()+" to <T-NAMESELF>.");
			if(mob.location().okAffect(newMsg))
				mob.location().send(mob,newMsg);
			else
				addendumStr="."+(++addendum);

			last=giveThis;
			doneSomething=true;
		}while(allFlag);
	}

	private ShopKeeper shopkeeper(Room here)
	{
		MOB thisOne=null;
		for(int i=0;i<here.numInhabitants();i++)
		{
			MOB thisMOB=here.fetchInhabitant(i);
			if((thisMOB!=null)&&(thisMOB instanceof ShopKeeper))
			{
				if(thisOne==null)
					thisOne=thisMOB;
				else
					return null;
			}
		}
		return (ShopKeeper)thisOne;
	}

	public void sell(MOB mob, Vector commands)
	{
		ShopKeeper shopkeeper=shopkeeper(mob.location());
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Sell what to whom?");
				return;
			}
			commands.removeElementAt(0);
			MOB possibleShopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if(shopkeeper!=null)
			{
				shopkeeper=(ShopKeeper)possibleShopkeeper;
				commands.removeElementAt(commands.size()-1);
			}
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' buying anything.");
				return;
			}
			commands.removeElementAt(commands.size()-1);
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Sell what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental thisThang=null;
			thisThang=mob.fetchCarried(null,thisName);
			if(thisThang==null)
				thisThang=mob.fetchFollower(thisName);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't see '"+thisName+"' here.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_SELL,"<S-NAME> sell(s) "+thisThang.name()+" to "+shopkeeper.name());
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}


	public void value(MOB mob, Vector commands)
	{
		ShopKeeper shopkeeper=shopkeeper(mob.location());
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Value what with whom?");
				return;
			}
			commands.removeElementAt(0);
			MOB possibleShopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if(shopkeeper!=null)
			{
				shopkeeper=(ShopKeeper)possibleShopkeeper;
				commands.removeElementAt(commands.size()-1);
			}
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' buying anything.");
				return;
			}
			commands.removeElementAt(commands.size()-1);
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Value what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental thisThang=null;
			thisThang=mob.fetchInventory(thisName);
			if(thisThang==null)
				thisThang=mob.fetchFollower(thisName);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't see '"+thisName+"' here.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_VALUE,null);
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public void buy(MOB mob, Vector commands)
	{
		ShopKeeper shopkeeper=shopkeeper(mob.location());
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Buy what from whom?");
				return;
			}
			commands.removeElementAt(0);
			MOB possibleShopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if(shopkeeper!=null)
			{
				shopkeeper=(ShopKeeper)possibleShopkeeper;
				commands.removeElementAt(commands.size()-1);
			}
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' selling anything.");
				return;
			}
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Buy what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental thisThang=shopkeeper.getStock(thisName);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("There doesn't appear to be any for sale.  Try LIST.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_BUY,"<S-NAME> buy(s) "+thisThang.name()+" from "+shopkeeper.name());
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public void list(MOB mob, Vector commands)
	{
		ShopKeeper shopkeeper=shopkeeper(mob.location());
		if(shopkeeper==null)
		{
			if(commands.size()<2)
			{
				if(mob.isASysOp(mob.location()))
					mob.tell("List what or from whom?");
				else
					mob.tell("List from whom?");
				return;
			}
			commands.removeElementAt(0);
			MOB possibleShopkeeper=mob.location().fetchInhabitant(Util.combine(commands,0));
			if(shopkeeper!=null)
				shopkeeper=(ShopKeeper)possibleShopkeeper;
			else
			{
				if(mob.isASysOp(mob.location()))
					new Lister().list(mob,commands);
				else
					mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' selling anything.");
				return;
			}
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,null,Affect.MSG_LIST,null);
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	public void consider(MOB mob, Vector commands)
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

		int realDiff=//relativeLevelDiff(target,mob);
			target.envStats().level()-mob.envStats().level();

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

	public int relativeLevelDiff(MOB mob, MOB mob2)
	{
		if((mob==null)||(mob2==null)) return -1;
		TheFight theFight=new TheFight();

		int mob2armor=(int)theFight.adjustedArmor(mob2);
		int mobArmor=(int)theFight.adjustedArmor(mob);
		int mob2attack=(int)theFight.adjustedAttackBonus(mob2);
		int mobAttack=(int)theFight.adjustedAttackBonus(mob);
		int mob2dmg=(int)mob2.envStats().damage();
		int mobDmg=(int)mob.envStats().damage();
		int mob2hp=(int)mob2.baseState().getHitPoints();
		int mobHp=(int)mob.baseState().getHitPoints();

		double mob2hitRound=((Util.div((mobArmor+mob2attack),100.0))*Util.div(mob2dmg,2.0))*Util.mul(mob2.envStats().speed(),1.0);
		if(mob2hitRound<0) mob2hitRound=0.01;
		double mobHitRound=((Util.div((mob2armor+mobAttack),100.0))*Util.div(mobDmg,2.0))*Util.mul(mob.envStats().speed(),1.0);
		if(mobHitRound<0) mobHitRound=0.01;

		double mob2survivalRounds=Util.div(mob2hp,mobHitRound);

		double mobSurvivalRounds=Util.div(mobHp,mob2hitRound);

		int factorRoundsLevel=3;

		int levelDiff=(int)Math.round(Util.div((mobSurvivalRounds-mob2survivalRounds),factorRoundsLevel));

		return levelDiff;

	}
	public void rebuke(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Rebuke whom?");
			return;
		}
		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if(target==null)
		{
			if(!Util.combine(commands,1).equalsIgnoreCase(mob.getLeigeID()))
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
	public void serve(MOB mob, Vector commands)
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
