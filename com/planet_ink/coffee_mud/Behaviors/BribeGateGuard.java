package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class BribeGateGuard extends StdBehavior
{
	protected Exit e;
	protected int dir = -1;
	int tickTock = 0;
	Vector paidPlayers = new Vector();
	Hashtable toldAlready = new Hashtable();
	protected static boolean debug = false; // debuggin
	protected static boolean surviveReboot=false; // survive reboot
	protected static Hashtable notTheJournal=new Hashtable();

	public String ID()
	{
		return "BribeGateGuard";
	}



	protected double price()
	{
		return (double)getVal(getParms(), "price", 5);
	}

	protected String gates()
	{
		return ID() + getVal(getParms(), "gates", "General");
	}

	protected int findGate(MOB mob)
	{
		if (!CMLib.flags().isInTheGame(mob,false))
			return -1;
        Room R=mob.location();
        if(R!=null)
		for (int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
		{
			if (R.getRoomInDir(d) != null)
			{
				Exit e = R.getExitInDir(d);
				if((e!=null)&&(e.hasADoor()))
				{
					return d;
				}
			}
		}
		return -1;
	}

	protected Key getMyKeyTo(MOB mob, Exit e)
	{
		Key key = null;
		String keyCode = e.keyName();
		for (int i = 0; i < mob.inventorySize(); i++)
		{
			Item item = mob.fetchInventory(i);
			if ( (item instanceof Key) && ( ( (Key) item).getKey().equals(keyCode)))
			{
				key = (Key) item;
				break;
			}
		}
		if (key == null)
		{
			key = (Key) CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addInventory(key);
		}
		return key;
	}

	protected void payment(Coins given, MOB gateGuard, MOB mob)
	{
		// make a note in the journal
		double newNum = given.getTotalValue();
		newNum += getBalance(mob);
		Coins item = CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(gateGuard),newNum);
		delBalance(mob);
		if(item!=null) writeBalance(item, mob);
	}

	protected boolean checkBalance(double charge, MOB mob)
	{
		// Does this MOB have the cash for the charge?
		if (getBalance(mob) > charge) {
			return true;
		}
		return false;
	}

	protected double getBalance(MOB mob)
	{
		double balance = 0;
		// return the balance in int form
		if(surviveReboot)
		{
			Vector V =CMLib.database().DBReadJournalMsgs("BRIBEGATE_"+gates());
			Vector mine = new Vector();
			for (int v = 0; v < V.size(); v++)
			{
				JournalsLibrary.JournalEntry V2 =(JournalsLibrary.JournalEntry)V.elementAt(v);
				if ( ( V2.from.equalsIgnoreCase(mob.Name())))
				{
					mine.addElement(V2);
				}
			}
			for (int v = 0; v < mine.size(); v++)
			{
				JournalsLibrary.JournalEntry V2 = (JournalsLibrary.JournalEntry)mine.elementAt(v);
				String fullName = V2.subj;
				if (fullName.equals("COINS"))
				{
					Coins item = (Coins) CMClass.getItem("StdCoins");
					if (item != null)
					{
						CMLib.coffeeMaker().setPropertiesStr(item,V2.msg, true);
						item.recoverEnvStats();
						item.text();
						balance += item.getTotalValue();
					}
				}
			}
		}
		else
		{
			Hashtable H=(Hashtable)notTheJournal.get(gates());
			if(H==null)
			{
				H=new Hashtable();
				notTheJournal.put(gates(),H);
			}
			Double D=(Double)H.get(mob.Name());
			if(D==null)
			{
				D=Double.valueOf(0.0);
				H.put(mob.Name(),D);
			}
			balance=D.doubleValue();
		}
		return balance;
	}

	protected void charge(double charge, MOB gateGuard, MOB mob)
	{
		// update the balance in the journal
		Coins item = (Coins) CMClass.getItem("StdCoins");
		double newNum = getBalance(mob);
		newNum -= charge;
		if (newNum > 0)
		{
		    item.setCurrency(CMLib.beanCounter().getCurrency(gateGuard));
		    item.setDenomination(CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(gateGuard)));
			item.setNumberOfCoins(Math.round(newNum/item.getDenomination()));
			delBalance(mob);
			writeBalance(item, mob);
		}
		else
		{
			delBalance(mob);
		}
	}

	protected void delBalance(MOB mob)
	{
		// kill the journal entries for that mob
		if(surviveReboot)
		{
			Vector V = CMLib.database().DBReadJournalMsgs("BRIBEGATE_"+gates());
			Vector mine = new Vector();
			for (int v = 0; v < V.size(); v++)
			{
				JournalsLibrary.JournalEntry V2 = (JournalsLibrary.JournalEntry)V.elementAt(v);
				if ( ( V2.from).equalsIgnoreCase(mob.Name())) {
				  mine.addElement(V2);
				}
			}
			for (int v = 0; v < mine.size(); v++)
			{
				JournalsLibrary.JournalEntry V2 = (JournalsLibrary.JournalEntry)mine.elementAt(v);
				String fullName = V2.subj;
				if (fullName.equals("COINS")) {
				  CMLib.database().DBDeleteJournal("BRIBEGATE_"+gates(), V2.key);
				}
			}
		}
		else
		{
			Hashtable H=(Hashtable)notTheJournal.get(gates());
			if(H==null) return;
			Double D=(Double)H.get(mob.Name());
			if(D==null) return;
			H.remove(mob.Name());
		}
	}

	protected void writeBalance(Coins balance, MOB mob)
	{
		// write an entry for that mob
		if(surviveReboot)
		{
			CMLib.database().DBWriteJournal("BRIBEGATE_"+gates(), mob.Name(), CMClass.classID(balance),
			                            "COINS", CMLib.coffeeMaker().getPropertiesStr(balance, true));
		}
		else
		{
			Hashtable H=(Hashtable)notTheJournal.get(gates());
			if(H==null)
			{
				H=new Hashtable();
				notTheJournal.put(gates(),H);
			}
			Double D=(Double)H.get(mob.Name());
			if(D!=null)	H.remove(mob.Name());
			H.put(mob.Name(),Double.valueOf(balance.getTotalValue()));
		}
	}

	public static int getVal(String text, String key, int defaultValue)
	{
		text = text.toUpperCase();
		key = key.toUpperCase();
		int x = text.indexOf(key);
		while (x >= 0)
		{
			if ( (x == 0) || (!Character.isLetter(text.charAt(x - 1)))) {
			  while ( (x < text.length()) && (text.charAt(x) != '=') &&
			         (!Character.isDigit(text.charAt(x)))) {
			    x++;
			  }
			  if ( (x < text.length()) && (text.charAt(x) == '=')) {
			    while ( (x < text.length()) && (!Character.isDigit(text.charAt(x)))) {
			      x++;
			    }
			    if (x < text.length()) {
			      text = text.substring(x);
			      x = 0;
			      while ( (x < text.length()) && (Character.isDigit(text.charAt(x)))) {
			        x++;
			      }
			      return CMath.s_int(text.substring(0, x));
			    }
			  }
			  x = -1;
			}
			else {
			  x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
			}
		}
		return defaultValue;
	}

	public static String getVal(String text, String key, String defaultValue)
	{
		text = text.toUpperCase();
		key = key.toUpperCase();
		int x = text.indexOf(key);
		while (x >= 0) {
		  int y = text.indexOf("=", x);
		  int z = text.indexOf(" ", y);
		  if (z < 0) {
		    return text.substring(y + 1);
		  }
		  if ( (y > 0) && (z > y + 1)) {
		    return text.substring(y + 1, z - 1);
		  }
		  return defaultValue;
		}
		return defaultValue;
	}

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if (!super.okMessage(oking, msg)) {
		  if (debug) {
		    //CMLib.commands().postSay( (MOB) oking, msg.source(),
		    //                      "super FALSE", true, true);
		  }
		  return false;
		}
		MOB mob = msg.source();
		if (!canFreelyBehaveNormal(oking)) {
		  return true;
		}
		MOB monster = (MOB) oking;
	    String currency=CMLib.beanCounter().getCurrency(monster);
		if (msg.amITarget(monster)
	    && (!msg.amISource(monster))
	    && (msg.targetMinor() == CMMsg.TYP_GIVE)
	    && (msg.tool() != null)
	    && (msg.tool()instanceof Coins)
	    && (!((Coins)msg.tool()).getCurrency().equals(currency)))
		{
		    double denomination=CMLib.beanCounter().getLowestDenomination(currency);
		    CMLib.commands().postSay(monster,mob,"I only accept "+CMLib.beanCounter().getDenominationName(currency,denomination)+".",false,false);
		    return false;
		}
		if (msg.target() == null) {
		  if (debug) {
		    //CMLib.commands().postSay( (MOB) oking, msg.source(),
		    //                      "Effect Target null", true, true);
		  }
		  return true;
		}
		if (!CMLib.flags().canBeSeenBy(msg.source(), monster)) {
		  if (debug) {
		    CMLib.commands().postSay( (MOB) oking, msg.source(),
		                          "can't be seen", true, true);
		  }
		  return true;
		}
		if (mob.location() == monster.location()) {
		  if (msg.target()instanceof Exit) {
		    if (debug) {
		      CMLib.commands().postSay( (MOB) oking, msg.source(),
		                            "okAffect triggered.  Not Charging " + price() +
		                            " from balance " + getBalance(msg.source()) +
		                            ".", true, true);
		    }
		    if (!msg.source().isMonster()) {
		      if ( (msg.targetMinor() != CMMsg.TYP_CLOSE)){
		          //|| (msg.target() instanceof Room) ) {
		        if (debug) {
		          CMLib.commands().postSay( (MOB) oking, msg.source(),
		                                "Close or Leave", true, true);
		        }
		        if (checkBalance(price(), mob)) {
		          return true;
		        }
    	          CMMsg msgs = CMClass.getMsg(monster, mob, CMMsg.MSG_NOISYMOVEMENT,
    	              "<S-NAME> won't let <T-NAME> through there.");
    	          if (monster.location().okMessage(monster, msgs)) {
    	            monster.location().send(monster, msgs);
    	            double denomination=CMLib.beanCounter().getLowestDenomination(currency);
    	            String thePrice=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(price()/denomination));
    	            CMLib.commands().postSay(monster, mob,
    	                "I'll let you through here if you pay the fee of "+thePrice+".", true, false);
    	            if (debug) // debugging
    	              CMLib.commands().postSay(monster, mob,
    	                                    "I'm telling you this from okAffect", true, false);
    	            return false;
    	          }
    	          if (debug) // debugging
    	            CMLib.commands().postSay(monster, mob,
    	                                  "I'm telling you this from okAffect (2)", true, false);
    	        return false;
		      }
		      if (msg.target() instanceof Room) {
		        if (debug) // debugging
		          CMLib.commands().postSay(monster, mob,
		                                "I'm telling you this from okAffect (3)", true, false);
		      }
		      if (debug) {
		        CMLib.commands().postSay( (MOB) oking, msg.source(),
		                              "tarMin " + msg.targetMinor() + " ? " +
		                              CMMsg.TYP_CLOSE, true, true);
		        CMLib.commands().postSay( (MOB) oking, msg.source(),
		                              "srcMin " + msg.sourceMinor() + " ? " +
		                              CMMsg.TYP_LEAVE, true, true);
		        CMLib.commands().postSay( (MOB) oking, msg.source(),
		                              "source Monster? " +
		                              msg.source().isMonster(), true, true);
		      }
		      return true;
		    }
		    // And for leaving...
		    return true;
		  }
		}
		if (debug) {
		  //CMLib.commands().postSay((MOB)oking,msg.source(),"okAffect triggered.  WRONG LOCATION TO FIRE.", true,true);
		}
		if ( (mob.location() == monster.location())
		    && (mob != monster)
		    && (msg.target() != null)
		    && (!BrotherHelper.isBrother(mob, monster,false))
		    && (CMLib.flags().canSenseMoving(mob, monster))
		    && (!CMLib.masking().maskCheck(getParms(), mob,false))) {
		  if ( (msg.tool() != null)
		     && (msg.target()instanceof Room)
		     && (msg.tool()instanceof Exit)) {
		    return false;
		  }
		}
		return true;
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		MOB source = msg.source();
		if (!canActAtAll(affecting)) {
		  return;
		}

		MOB observer = (MOB) affecting;
		if ( (msg.sourceMinor() == CMMsg.TYP_ENTER)
		    && (!msg.amISource(observer))
		    && (CMLib.flags().canSenseMoving(msg.source(), observer))
		    && (!msg.source().isMonster())) {
		  // check if the msg.source() has paid enough.  if so, time to react
		  if (checkBalance(price(), source)) {
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),Boolean.FALSE);
		  }
		}
		else
		if ( (msg.sourceMinor() == CMMsg.TYP_LEAVE)
		    && (!msg.amISource(observer))
		    && (!msg.source().isMonster())) {
		  toldAlready.remove(source.Name());
		  if (paidPlayers.contains(source)) { // the player that the guard acknowledged as paid has now left
		    paidPlayers.remove(source);
		    if ( (msg.tool() != null) && (msg.tool()instanceof Exit)) {
		      Exit exit = (Exit) msg.tool();
		      if (exit.Name().equals(e.Name())) { // the player is walking through the gate.  NOW we charge their balance
		        charge(price(), observer, source);
		        if(debug)
		        {
		          CMLib.commands().postSay(observer,source,"Charging " + price() + ", balance " + getBalance(source) + ".", true,true);
		        }
		      }
		    }
		  }
		  else
		  {
		    // unpaid!

		  }
		}
		else
		if (msg.amITarget(observer)
		    && (!msg.amISource(observer))
		    && (msg.targetMinor() == CMMsg.TYP_GIVE)
		    && (msg.tool() != null)
		    && (msg.tool()instanceof Coins))
		{
		  payment( (Coins) msg.tool(), observer, msg.source());
		  CMLib.commands().postSay(observer, source, "Thank you very much.", true, false);
		  if(getBalance(source) > price())
		  {
		      String currency=CMLib.beanCounter().getCurrency(observer);
		      double denomination=CMLib.beanCounter().getLowestDenomination(currency);
		      long diff=Math.round((getBalance(source) - price())/denomination);
		      String difference=CMLib.beanCounter().getDenominationName(currency,denomination,diff);
		    CMLib.commands().postSay(observer, source,
		                          "I'll hang on to the additional "+difference+" for you", true, false);
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),Boolean.FALSE);
		    if (debug)  // debugging
		      CMLib.commands().postSay(observer, source,
		                            "I'm telling you this from execute", true, false);
		    try {
		      if (dir >= 0)
		        observer.doCommand(CMParms.parse("OPEN " +
		                                          Directions.getDirectionName(dir)),Command.METAFLAG_FORCED);
		      observer.doCommand(CMParms.parse("BOW " + source.Name()),Command.METAFLAG_FORCED);
		    }
		    catch (Exception e1) {}
		  }
		  else
		  if(getBalance(source) == price())
		  {
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),Boolean.FALSE);
		    try {
		      if (dir >= 0)
		        observer.doCommand(CMParms.parse("OPEN " +
		                                          Directions.getDirectionName(dir)),Command.METAFLAG_FORCED);
		      observer.doCommand(CMParms.parse("BOW " + source.Name()),Command.METAFLAG_FORCED);
		    }
		    catch (Exception e1) {}
		  }
		  else
		  if(getBalance(source) < price())
		  {
		    CMMsg msg2=CMClass.getMsg(observer,null,msg.tool(),CMMsg.MSG_EMOTE,"^E<S-NAME> look(s) carefully at <O-NAME>.^?");
		    msg.addTrailerMsg(msg2);
		    msg2=CMClass.getMsg(observer,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'I'm afraid that this is insufficient.'^?.");
		    msg.addTrailerMsg(msg2);
		    msg2=CMClass.getMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
		    msg.addTrailerMsg(msg2);
		    charge(( (Coins) msg.tool()).value(), observer, msg.source());
		  }
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking, tickID);

		if (tickID != Tickable.TICKID_MOB) {
		  return true;
		}
		if (!canFreelyBehaveNormal(ticking)) {
		  return true;
		}
		MOB mob = (MOB) ticking;
		dir = findGate(mob);
		if (dir < 0) {
		  CMLib.commands().postSay(mob, null,
		                        "I'd shut the gate, but there isn't one...", false, false);
		  return true;
		}
		e = mob.location().getExitInDir(dir);
		if (!e.isOpen()) {
		  for (int j = 0; j < mob.location().numPCInhabitants(); j++) {
		    MOB M = mob.location().fetchPCInhabitant(j);
		    if ( (paidPlayers.contains(M)) && (toldAlready.containsKey(M.Name()))) {
		      Boolean B = (Boolean) toldAlready.get(M.Name());
		      if (!B.booleanValue())
		      {
		          String currency=CMLib.beanCounter().getCurrency(mob);
		          double denomination=CMLib.beanCounter().getLowestDenomination(currency);
		          String balanceStr=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(getBalance(M)/denomination));
		        CMLib.commands().postSay(mob, M,
		                              "We still have record that you gave us " +
		                              balanceStr +
		                              " before if you're heading through", true, false);
		      }
		      toldAlready.put(M.Name(), Boolean.TRUE);
		    if (dir >= 0)
				mob.doCommand(CMParms.parse("OPEN " +
				                Directions.
				                getDirectionName(dir)),Command.METAFLAG_FORCED);
		    }
		    else {
		      if(toldAlready.containsKey(M.Name()))
		         continue;
	          String currency=CMLib.beanCounter().getCurrency(mob);
	          double denomination=CMLib.beanCounter().getLowestDenomination(currency);
	          String priceStr=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(price()/denomination));
		      CMLib.commands().postSay(mob, M,
		          "I'll let you through here if you pay the fee of " + priceStr +
		          ".", true, false);
		      toldAlready.put(M.Name(), Boolean.TRUE);
		      if (debug)  // debugging
		        CMLib.commands().postSay(mob, M,
		                              "I'm telling you this from tick", true, false);
		    }
		  }
		}
		boolean nightTime = (mob.location().getArea().getTimeObj().getTODCode() ==
		                     TimeClock.TIME_NIGHT);
		if (nightTime) {
		  if ( (!e.isLocked()) && (e.hasALock())) {
		    if (getMyKeyTo(mob, e) != null) {
		      CMMsg msg = CMClass.getMsg(mob, e, CMMsg.MSG_LOCK,
		                                "<S-NAME> lock(s) <T-NAME>.");
		      if (mob.location().okMessage(mob, msg)) {
		        CMLib.utensils().roomAffectFully(msg, mob.location(), dir);
		      }
		    }
		  }
		}
		else
		if (e.isLocked()) {
		  if (getMyKeyTo(mob, e) != null) {
		    CMMsg msg = CMClass.getMsg(mob, e, CMMsg.MSG_UNLOCK,
		                              "<S-NAME> unlock(s) <T-NAME>.");
		    if (mob.location().okMessage(mob, msg)) {
		      CMLib.utensils().roomAffectFully(msg, mob.location(), dir);
		    }
		  }
		}
		tickTock++;
		if (tickTock > 2) {
		  tickTock = 0;
		  if ( (e.isOpen()) && (paidPlayers.isEmpty())) {
		      mob.doCommand(CMParms.parse("CLOSE " +
		                                        Directions.getDirectionName(dir)),Command.METAFLAG_FORCED);
		  }
		}
		return true;
	}
}
