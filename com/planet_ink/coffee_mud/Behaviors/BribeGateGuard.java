package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BribeGateGuard extends StdBehavior 
{
	private Hashtable partials = new Hashtable();
	protected Exit e;
	protected int dir = -1;
	int tickTock = 0;
	Vector paidPlayers = new Vector();
	Hashtable toldAlready = new Hashtable();
	private static boolean debug = true; // debuggin
	private static boolean surviveReboot=false; // survive reboot
	private static Hashtable notTheJournal=new Hashtable();

	public String ID() 
	{
		return "BribeGateGuard";
	}

	public Behavior newInstance() 
	{
		return new BribeGateGuard();
	}

	private int price() 
	{
		return getVal(getParms(), "price", 5);
	}

	private String gates() 
	{
		return ID() + getVal(getParms(), "gates", "General");
	}

	private int findGate(MOB mob) 
	{
		if (mob.location() == null) 
		{
			return -1;
		}
		if (!mob.location().isInhabitant(mob)) 
		{
			return -1;
		}
		for (int d = 0; d < Directions.NUM_DIRECTIONS; d++) 
		{
			if (mob.location().getRoomInDir(d) != null) 
			{
				Exit e = mob.location().getExitInDir(d);
				if (e.hasADoor()) 
				{
					return d;
				}
			}
		}
		return -1;
	}

	private Key getMyKeyTo(MOB mob, Exit e) 
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

	private void payment(Coins given, MOB mob) 
	{
		// make a note in the journal
		Coins item = (Coins) CMClass.getItem("StdCoins");
		int newNum = given.numberOfCoins();
		newNum += getBalance(mob);
		item.setNumberOfCoins(newNum);
		delBalance(mob);
		writeBalance(item, mob);
	}

	private boolean checkBalance(int charge, MOB mob) 
	{
		// Does this MOB have the cash for the charge?
		if (getBalance(mob) > charge) {
			return true;
		}
		return false;
	}

	private int getBalance(MOB mob) 
	{
		int balance = 0;
		// return the balance in int form
		if(surviveReboot)
		{
			Vector V =ExternalPlay.DBReadJournal(gates());
			Vector mine = new Vector();
			for (int v = 0; v < V.size(); v++) 
			{
				Vector V2 = (Vector) V.elementAt(v);
				if ( ( (String) V2.elementAt(1)).equalsIgnoreCase(mob.Name())) 
				{
					mine.addElement(V2);
				}
			}
			for (int v = 0; v < mine.size(); v++) 
			{
				Vector V2 = (Vector) mine.elementAt(v);
				String fullName = ( (String) V2.elementAt(4));
				if (fullName.equals("COINS")) 
				{
					Coins item = (Coins) CMClass.getItem("StdCoins");
					if (item != null) 
					{
						Generic.setPropertiesStr(item, ( (String) V2.elementAt(5)), true);
						item.recoverEnvStats();
						item.text();
					}
					balance += item.numberOfCoins();
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
			Integer I=(Integer)H.get(mob.Name());
			if(I==null)
			{
				I=new Integer(0);
				H.put(mob.Name(),I);
			}
			balance=I.intValue();
		}
		return balance;
	}

	private void charge(int charge, MOB mob) 
	{
		// update the balance in the journal
		Coins item = (Coins) CMClass.getItem("StdCoins");
		int newNum = getBalance(mob);
		newNum -= charge;
		if (newNum > 0) 
		{
			item.setNumberOfCoins(newNum);
			delBalance(mob);
			writeBalance(item, mob);
		}
		else 
		{
			delBalance(mob);
		}
	}

	private void delBalance(MOB mob) 
	{
		// kill the journal entries for that mob
		if(surviveReboot)
		{
			Vector V = ExternalPlay.DBReadJournal(gates());
			Vector mine = new Vector();
			for (int v = 0; v < V.size(); v++) 
			{
				Vector V2 = (Vector) V.elementAt(v);
				if ( ( (String) V2.elementAt(1)).equalsIgnoreCase(mob.Name())) {
				  mine.addElement(V2);
				}
			}
			for (int v = 0; v < mine.size(); v++) 
			{
				Vector V2 = (Vector) mine.elementAt(v);
				String fullName = ( (String) V2.elementAt(4));
				if (fullName.equals("COINS")) {
				  ExternalPlay.DBDeleteJournal( ( (String) V2.elementAt(0)),
				                               Integer.MAX_VALUE);
				}
			}
		}
		else
		{
			Hashtable H=(Hashtable)notTheJournal.get(gates());
			if(H==null) return;
			Integer I=(Integer)H.get(mob.Name());
			if(I==null) return;
			H.remove(mob.Name());
		}
	}

	private void writeBalance(Coins balance, MOB mob) 
	{
		// write an entry for that mob
		if(surviveReboot)
		{
			ExternalPlay.DBWriteJournal(gates(), mob.Name(), CMClass.className(balance),
			                            "COINS", Generic.getPropertiesStr(balance, true),
			                            -1);
		}
		else
		{
			Hashtable H=(Hashtable)notTheJournal.get(gates());
			if(H==null)
			{
				H=new Hashtable();
				notTheJournal.put(gates(),H);
			}
			Integer I=(Integer)H.get(mob.Name());
			if(I!=null)	H.remove(mob.Name());
			H.put(mob.Name(),new Integer(balance.numberOfCoins()));
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
			      return Util.s_int(text.substring(0, x));
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

	public boolean okAffect(Environmental oking, Affect affect) 
	{
		if (!super.okAffect(oking, affect)) {
		  if (debug) {
		    //ExternalPlay.quickSay( (MOB) oking, affect.source(),
		    //                      "super FALSE", true, true);
		  }
		  return false;
		}
		MOB mob = affect.source();
		if (!canFreelyBehaveNormal(oking)) {
		  return true;
		}
		MOB monster = (MOB) oking;
		if (affect.target() == null) {
		  if (debug) {
		    //ExternalPlay.quickSay( (MOB) oking, affect.source(),
		    //                      "affect Target null", true, true);
		  }
		  return true;
		}
		if (!Sense.canBeSeenBy(affect.source(), oking)) {
		  if (debug) {
		    ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                          "can't be seen", true, true);
		  }
		  return true;
		}
		if (mob.location() == monster.location()) {
		  if (affect.target()instanceof Exit) {
		    if (debug) {
		      ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                            "okAffect triggered.  Not Charging " + price() +
		                            " from balance " + getBalance(affect.source()) +
		                            ".", true, true);
		    }
		    if (!affect.source().isMonster()) {
		      if ( (affect.targetMinor() != Affect.TYP_CLOSE)){
		          //|| (affect.target() instanceof Room) ) {
		        if (debug) {
		          ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                                "Close or Leave", true, true);
		        }
		        if (checkBalance(price(), mob)) {
		          return true;
		        }
		        else {
		          FullMsg msgs = new FullMsg(monster, mob, Affect.MSG_NOISYMOVEMENT,
		              "<S-NAME> won't let <T-NAME> through there.");
		          if (monster.location().okAffect(monster, msgs)) {
		            monster.location().send(monster, msgs);
		            ExternalPlay.quickSay(monster, mob,
		                "I'll let you through here if you pay the fee of " + price() +
		                " gold.", true, false);
		            if (debug) // debugging
		              ExternalPlay.quickSay(monster, mob,
		                                    "I'm telling you this from okAffect", true, false);
		            return false;
		          }
		          if (debug) // debugging
		            ExternalPlay.quickSay(monster, mob,
		                                  "I'm telling you this from okAffect (2)", true, false);
		          return false;
		        }
		      }
		      if (affect.target() instanceof Room) {
		        if (debug) // debugging
		          ExternalPlay.quickSay(monster, mob,
		                                "I'm telling you this from okAffect (3)", true, false);
		      }
		      if (debug) {
		        ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                              "tarMin " + affect.targetMinor() + " ? " +
		                              Affect.TYP_CLOSE, true, true);
		        ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                              "srcMin " + affect.sourceMinor() + " ? " +
		                              Affect.TYP_LEAVE, true, true);
		        ExternalPlay.quickSay( (MOB) oking, affect.source(),
		                              "source Monster? " +
		                              affect.source().isMonster(), true, true);
		      }
		      return true;
		    }
		    // And for leaving...
		    return true;
		  }
		}
		if (debug) {
		  //ExternalPlay.quickSay((MOB)oking,affect.source(),"okAffect triggered.  WRONG LOCATION TO FIRE.", true,true);
		}
		if ( (mob.location() == monster.location())
		    && (mob != monster)
		    && (affect.target() != null)
		    && (!BrotherHelper.isBrother(mob, monster))
		    && (Sense.canSenseMoving(mob, monster))
		    && (!SaucerSupport.zapperCheck(getParms(), mob))) {
		  if ( (affect.tool() != null)
		     && (affect.target()instanceof Room)
		     && (affect.tool()instanceof Exit)) {
		    return false;
		  }
		}
		return true;
	}

	public void affect(Environmental affecting, Affect affect) 
	{
		super.affect(affecting, affect);
		MOB source = affect.source();
		if (!canActAtAll(affecting)) {
		  return;
		}

		MOB observer = (MOB) affecting;
		if ( (affect.sourceMinor() == Affect.TYP_ENTER)
		    && (!affect.amISource(observer))
		    && (Sense.canSenseMoving(affect.source(), affecting))
		    && (!affect.source().isMonster())) {
		  // check if the affect.source() has paid enough.  if so, time to react
		  if (checkBalance(price(), source)) {
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),new Boolean(false));
		  }
		}
		else
		if ( (affect.sourceMinor() == Affect.TYP_LEAVE)
		    && (!affect.amISource(observer))
		    && (!affect.source().isMonster())) {
		  toldAlready.remove(source.Name());
		  if (paidPlayers.contains(source)) { // the player that the guard acknowledged as paid has now left
		    paidPlayers.remove(source);
		    if ( (affect.tool() != null) && (affect.tool()instanceof Exit)) {
		      Exit exit = (Exit) affect.tool();
		      if (exit.Name().equals(e.Name())) { // the player is walking through the gate.  NOW we charge their balance
		        charge(price(), source);
		        if(debug)
		        {
		          ExternalPlay.quickSay(observer,source,"Charging " + price() + ", balance " + getBalance(source) + ".", true,true);
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
		if (affect.amITarget(observer)
		    && (!affect.amISource(observer))
		    && (affect.targetMinor() == Affect.TYP_GIVE)
		    && (affect.tool() != null)
		    && (affect.tool()instanceof Coins)) {
		  payment( (Coins) affect.tool(), affect.source());
		  ExternalPlay.quickSay(observer, source, "Thank you very much.", true, false);
		  if(getBalance(source) > price())
		  {
		    ExternalPlay.quickSay(observer, source,
		                          "I'll hang on to the additional " +
		                          (getBalance(source) - price()) + " for you", true, false);
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),new Boolean(false));
		    if (debug)  // debugging
		      ExternalPlay.quickSay(observer, source,
		                            "I'm telling you this from Affect", true, false);
		    try {
		      if (dir >= 0)
		        ExternalPlay.doCommand(observer,
		                               Util.parse("OPEN " +
		                                          Directions.getDirectionName(dir)));
		      ExternalPlay.doCommand(observer, Util.parse("BOW " + source.Name()));
		    }
		    catch (Exception e1) {}
		  }
		  else
		  if(getBalance(source) == price())
		  {
		    paidPlayers.addElement(source);
		    toldAlready.put(source.Name(),new Boolean(false));
		    try {
		      if (dir >= 0)
		        ExternalPlay.doCommand(observer,
		                               Util.parse("OPEN " +
		                                          Directions.getDirectionName(dir)));
		      ExternalPlay.doCommand(observer, Util.parse("BOW " + source.Name()));
		    }
		    catch (Exception e1) {}
		  }
		  else
		  if(getBalance(source) < price())
		  {
		    FullMsg msg=new FullMsg(observer,null,affect.tool(),Affect.MSG_EMOTE,"^E<S-NAME> look(s) carefully at <O-NAME>.");
		    affect.addTrailerMsg(msg);
		    msg=new FullMsg(observer,null,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) 'I'm afraid that this is insufficient.'^?.");
		    affect.addTrailerMsg(msg);
		    msg=new FullMsg(observer,source,affect.tool(),Affect.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
		    affect.addTrailerMsg(msg);
		    charge(( (Coins) affect.tool()).value(), affect.source());
		  }
		}
	}

	public boolean tick(Tickable ticking, int tickID) 
	{
		super.tick(ticking, tickID);

		if (tickID != Host.MOB_TICK) {
		  return true;
		}
		if (!canFreelyBehaveNormal(ticking)) {
		  return true;
		}
		MOB mob = (MOB) ticking;
		dir = findGate(mob);
		if (dir < 0) {
		  ExternalPlay.quickSay(mob, null,
		                        "I'd shut the gate, but there isn't one...", false, false);
		  return true;
		}
		e = mob.location().getExitInDir(dir);
		if (!e.isOpen()) {
		  for (int j = 0; j < mob.location().numPCInhabitants(); j++) {
		    MOB M = (MOB)mob.location().fetchPCInhabitant(j);
		    if ( (paidPlayers.contains(M)) && (toldAlready.containsKey(M.Name()))) {
		      Boolean B = (Boolean) toldAlready.get(M.Name());
		      if (!B.booleanValue())
		        ExternalPlay.quickSay(mob, M,
		                              "We still have record that you gave us " +
		                              getBalance(M) +
		                              " before if you're heading through", true, false);
		      toldAlready.put(M.Name(), new Boolean(true));
		      try {
		        if (dir >= 0)
		          ExternalPlay.doCommand(mob,
		                                 Util.parse("OPEN " +
		                                            Directions.
		                                            getDirectionName(dir)));
		      }
		      catch (Exception e1) {}
		    }
		    else {
		      if(toldAlready.containsKey(M.Name()))
		         continue;
		      ExternalPlay.quickSay(mob, M,
		          "I'll let you through here if you pay the fee of " + price() +
		          " gold.", true, false);
		      toldAlready.put(M.Name(), new Boolean(true));
		      if (debug)  // debugging
		        ExternalPlay.quickSay(mob, M,
		                              "I'm telling you this from tick", true, false);
		    }
		  }
		}
		boolean nightTime = (mob.location().getArea().getTODCode() ==
		                     Area.TIME_NIGHT);
		if (nightTime) {
		  if ( (!e.isLocked()) && (e.hasALock())) {
		    if (getMyKeyTo(mob, e) != null) {
		      FullMsg msg = new FullMsg(mob, e, Affect.MSG_LOCK,
		                                "<S-NAME> lock(s) <T-NAME>.");
		      if (mob.location().okAffect(mob, msg)) {
		        ExternalPlay.roomAffectFully(msg, mob.location(), dir);
		      }
		    }
		  }
		}
		else
		if (e.isLocked()) {
		  if (getMyKeyTo(mob, e) != null) {
		    FullMsg msg = new FullMsg(mob, e, Affect.MSG_UNLOCK,
		                              "<S-NAME> unlock(s) <T-NAME>.");
		    if (mob.location().okAffect(mob, msg)) {
		      ExternalPlay.roomAffectFully(msg, mob.location(), dir);
		    }
		  }
		}
		tickTock++;
		if (tickTock > 2) {
		  tickTock = 0;
		  if ( (e.isOpen()) && (paidPlayers.isEmpty())) {
		    try {
		      ExternalPlay.doCommand(mob,
		                             Util.parse("CLOSE " +
		                                        Directions.getDirectionName(dir)));
		    }
		    catch (Exception e1) {}
		  }
		}
		return true;
	}
}