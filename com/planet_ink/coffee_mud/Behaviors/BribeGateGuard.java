package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class BribeGateGuard extends StdBehavior
{
	protected Exit e;
	protected int dir = -1;
	protected int tickTock = 0;
	protected Vector<MOB> paidPlayers = new Vector<MOB>();
	protected Hashtable<String,Boolean> toldAlready = new Hashtable<String,Boolean>();
	protected static boolean debug = false; // debuggin
	protected static boolean surviveReboot=false; // survive reboot
	protected static Map<String,Map<String,Double>> notTheJournal=new Hashtable<String,Map<String,Double>>();

	@Override
	public String ID()
	{
		return "BribeGateGuard";
	}

	@Override
	public CMObject copyOf()
	{
		final BribeGateGuard obj=(BribeGateGuard)super.copyOf();
		obj.paidPlayers=new Vector<MOB>();
		obj.paidPlayers.addAll(paidPlayers);
		obj.toldAlready=new Hashtable<String,Boolean>();
		obj.toldAlready.putAll(toldAlready);
		return obj;
	}

	@Override
	public String accountForYourself()
	{
		return "corruptable gate guarding";
	}

	protected double price()
	{
		return getVal(getParms(), "price", 5);
	}

	protected String gates()
	{
		return ID() + getVal(getParms(), "gates", "General");
	}

	protected int findGate(MOB mob)
	{
		if (!CMLib.flags().isInTheGame(mob,false))
			return -1;
		final Room R=mob.location();
		if(R!=null)
		for (int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
		{
			if (R.getRoomInDir(d) != null)
			{
				final Exit e = R.getExitInDir(d);
				if((e!=null)&&(e.hasADoor()))
				{
					return d;
				}
			}
		}
		return -1;
	}

	protected DoorKey getMyKeyTo(MOB mob, Exit e)
	{
		DoorKey key = null;
		final String keyCode = e.keyName();
		for (int i = 0; i < mob.numItems(); i++)
		{
			final Item item = mob.getItem(i);
			if ( (item instanceof DoorKey) && ( ( (DoorKey) item).getKey().equals(keyCode)))
			{
				key = (DoorKey) item;
				break;
			}
		}
		if (key == null)
		{
			key = (DoorKey) CMClass.getItem("StdKey");
			key.setKey(keyCode);
			mob.addItem(key);
		}
		return key;
	}

	protected void payment(Coins given, MOB gateGuard, MOB mob)
	{
		// make a note in the journal
		double newNum = given.getTotalValue();
		newNum += getBalance(mob);
		final Coins item = CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(gateGuard),newNum);
		delBalance(mob);
		if(item!=null)
			writeBalance(item, mob);
	}

	protected boolean checkBalance(double charge, MOB mob)
	{
		// Does this MOB have the cash for the charge?
		if (getBalance(mob) > charge)
		{
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
			final List<JournalEntry> V =CMLib.database().DBReadJournalMsgsByUpdateDate("BRIBEGATE_"+gates(), true);
			final Vector<JournalEntry> mine = new Vector<JournalEntry>();
			for (int v = 0; v < V.size(); v++)
			{
				final JournalEntry V2 =V.get(v);
				if ( ( V2.from().equalsIgnoreCase(mob.Name())))
				{
					mine.addElement(V2);
				}
			}
			for (int v = 0; v < mine.size(); v++)
			{
				final JournalEntry V2 = mine.elementAt(v);
				final String fullName = V2.subj();
				if (fullName.equals("COINS"))
				{
					final Coins item = (Coins) CMClass.getItem("StdCoins");
					if (item != null)
					{
						CMLib.coffeeMaker().setPropertiesStr(item,V2.msg(), true);
						item.recoverPhyStats();
						item.text();
						balance += item.getTotalValue();
					}
				}
			}
		}
		else
		{
			Map<String,Double> H=notTheJournal.get(gates());
			if(H==null)
			{
				H=new Hashtable<String,Double>();
				notTheJournal.put(gates(),H);
			}
			Double D=H.get(mob.Name());
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
		final Coins item = (Coins) CMClass.getItem("StdCoins");
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
			final List<JournalEntry> V = CMLib.database().DBReadJournalMsgsByUpdateDate("BRIBEGATE_"+gates(), true);
			for (int v = 0; v < V.size(); v++)
			{
				final JournalEntry V2 = V.get(v);
				if ( ( V2.from()).equalsIgnoreCase(mob.Name()))
				{
					final String fullName = V2.subj();
					if (fullName.equals("COINS"))
					{
						CMLib.database().DBDeleteJournal("BRIBEGATE_"+gates(), V2.key());
					}
				}
			}
		}
		else
		{
			final Map<String,Double> H=notTheJournal.get(gates());
			if(H==null)
				return;
			final Double D=H.get(mob.Name());
			if(D==null)
				return;
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
			Map<String,Double> H=notTheJournal.get(gates());
			if(H==null)
			{
				H=new Hashtable<String,Double>();
				notTheJournal.put(gates(),H);
			}
			final Double D=H.get(mob.Name());
			if(D!=null)
				H.remove(mob.Name());
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
			if ( (x == 0) || (!Character.isLetter(text.charAt(x - 1))))
			{
				while ( (x < text.length()) && (text.charAt(x) != '=') && (!Character.isDigit(text.charAt(x))))
				{
					x++;
				}
				if ( (x < text.length()) && (text.charAt(x) == '='))
				{
					while ( (x < text.length()) && (!Character.isDigit(text.charAt(x))))
					{
						x++;
					}
					if (x < text.length())
					{
						text = text.substring(x);
						x = 0;
						while ( (x < text.length()) && (Character.isDigit(text.charAt(x))))
						{
							x++;
						}
						return CMath.s_int(text.substring(0, x));
					}
				}
				x = -1;
			}
			else
			{
				x = text.toUpperCase().indexOf(key.toUpperCase(), x + 1);
			}
		}
		return defaultValue;
	}

	public static String getVal(String text, String key, String defaultValue)
	{
		text = text.toUpperCase();
		key = key.toUpperCase();
		final int x = text.indexOf(key);
		while (x >= 0)
		{
			final int y = text.indexOf('=', x);
			final int z = text.indexOf(' ', y);
			if (z < 0)
			{
				return text.substring(y + 1);
			}
			if ( (y > 0) && (z > y + 1))
			{
				return text.substring(y + 1, z - 1);
			}
			return defaultValue;
		}
		return defaultValue;
	}

	@Override
	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if (!super.okMessage(oking, msg))
		{
			if (debug)
			{
				//CMLib.commands().postSay( (MOB) oking, msg.source(), L("super FALSE"), true, true);
			}
			return false;
		}
		final MOB mob = msg.source();
		if (!canFreelyBehaveNormal(oking))
		{
			return true;
		}
		final MOB monster = (MOB) oking;
		final String currency=CMLib.beanCounter().getCurrency(monster);
		if (msg.amITarget(monster)
		&& (!msg.amISource(monster))
		&& (msg.targetMinor() == CMMsg.TYP_GIVE)
		&& (msg.tool()instanceof Coins)
		&& (!((Coins)msg.tool()).getCurrency().equals(currency)))
		{
			final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
			CMLib.commands().postSay(monster,mob,L("I only accept @x1.",CMLib.beanCounter().getDenominationName(currency,denomination)),false,false);
			return false;
		}
		if (msg.target() == null)
		{
			if (debug)
			{
				//CMLib.commands().postSay( (MOB) oking, msg.source(), L("Effect Target null"), true, true);
			}
			return true;
		}
		if (!CMLib.flags().canBeSeenBy(msg.source(), monster))
		{
			if (debug)
			{
				CMLib.commands().postSay( (MOB) oking, msg.source(), L("can't be seen"), true, true);
			}
			return true;
		}
		if (mob.location() == monster.location())
		{
			if (msg.target()instanceof Exit)
			{
				if (debug)
				{
					CMLib.commands().postSay( (MOB) oking, msg.source(),
										L("okAffect triggered.	Not Charging @x1 from balance @x2.",""+price(),""+getBalance(msg.source())), true, true);
				}
				if (!msg.source().isMonster())
				{
					if ( (msg.targetMinor() != CMMsg.TYP_CLOSE))
					{
						//|| (msg.target() instanceof Room) )
						if (debug)
						{
							CMLib.commands().postSay( (MOB) oking, msg.source(),
												L("Close or Leave"), true, true);
						}
						if (checkBalance(price(), mob))
						{
							return true;
						}
						final CMMsg msgs = CMClass.getMsg(monster, mob, CMMsg.MSG_NOISYMOVEMENT,
							L("<S-NAME> won't let <T-NAME> through there."));
						if (monster.location().okMessage(monster, msgs))
						{
							monster.location().send(monster, msgs);
							final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
							final String thePrice=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(price()/denomination));
							CMLib.commands().postSay(monster, mob, L("I'll let you through here if you pay the fee of @x1.",thePrice), true, false);
							if (debug) // debugging
								CMLib.commands().postSay(monster, mob, L("I'm telling you this from okAffect"), true, false);
							return false;
						}
						if (debug) // debugging
							CMLib.commands().postSay(monster, mob, L("I'm telling you this from okAffect (2)"), true, false);
						return false;
					}
					if (msg.target() instanceof Room)
					{
						if (debug) // debugging
							CMLib.commands().postSay(monster, mob, L("I'm telling you this from okAffect (3)"), true, false);
					}
					if (debug)
					{
						CMLib.commands().postSay( (MOB) oking, msg.source(),L("tarMin @x1 ? @x2",""+msg.targetMinor(),""+CMMsg.TYP_CLOSE), true, true);
						CMLib.commands().postSay( (MOB) oking, msg.source(),L("srcMin @x1 ? @x2",""+msg.sourceMinor(),""+CMMsg.TYP_LEAVE), true, true);
						CMLib.commands().postSay( (MOB) oking, msg.source(),L("source Monster? @x1",""+msg.source().isMonster()), true, true);
					}
					return true;
				}
				// And for leaving...
				return true;
			}
		}
		if (debug)
		{
			//CMLib.commands().postSay((MOB)oking,msg.source(),L("okAffect triggered.	WRONG LOCATION TO FIRE."), true,true);
		}
		if ( (mob.location() == monster.location())
		&& (mob != monster)
		&& (msg.target() != null)
		&& (!BrotherHelper.isBrother(mob, monster,false))
		&& (CMLib.flags().canSenseEnteringLeaving(mob, monster))
		&& (!CMLib.masking().maskCheck(getParms(), mob,false)))
		{
			if ((msg.target() instanceof Room)
			&& (msg.tool() instanceof Exit))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		final MOB source = msg.source();
		if (!canActAtAll(affecting))
		{
			return;
		}

		final MOB observer = (MOB) affecting;
		if ( (msg.sourceMinor() == CMMsg.TYP_ENTER)
		&& (!msg.amISource(observer))
		&& (CMLib.flags().canSenseEnteringLeaving(msg.source(), observer))
		&& (!msg.source().isMonster()))
		{
			// check if the msg.source() has paid enough.	if so, time to react
			if (checkBalance(price(), source))
			{
				paidPlayers.addElement(source);
				toldAlready.put(source.Name(),Boolean.FALSE);
			}
		}
		else
		if ( (msg.sourceMinor() == CMMsg.TYP_LEAVE)
		&& (!msg.amISource(observer))
		&& (!msg.source().isMonster()))
		{
			toldAlready.remove(source.Name());
			if (paidPlayers.contains(source))
			{ // the player that the guard acknowledged as paid has now left
				paidPlayers.remove(source);
				if (msg.tool()instanceof Exit)
				{
					final Exit exit = (Exit) msg.tool();
					if (exit.Name().equals(e.Name()))
					{ // the player is walking through the gate.	NOW we charge their balance
						charge(price(), observer, source);
						if(debug)
						{
							CMLib.commands().postSay(observer,source,L("Charging @x1, balance @x2.",""+price(),""+getBalance(source)), true,true);
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
		&& (msg.tool() instanceof Coins))
		{
			payment( (Coins) msg.tool(), observer, msg.source());
			CMLib.commands().postSay(observer, source, L("Thank you very much."), true, false);
			if(getBalance(source) > price())
			{
				final String currency=CMLib.beanCounter().getCurrency(observer);
				final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
				final long diff=Math.round((getBalance(source) - price())/denomination);
				final String difference=CMLib.beanCounter().getDenominationName(currency,denomination,diff);
				CMLib.commands().postSay(observer, source, L("I'll hang on to the additional @x1 for you",difference), true, false);
				paidPlayers.addElement(source);
				toldAlready.put(source.Name(),Boolean.FALSE);
				if (debug)	// debugging
					CMLib.commands().postSay(observer, source,
										L("I'm telling you this from execute"), true, false);
				try
				{
					if (dir >= 0)
						observer.doCommand(CMParms.parse("OPEN " + CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
					observer.doCommand(CMParms.parse("BOW " + source.Name()),MUDCmdProcessor.METAFLAG_FORCED);
				}
				catch (final Exception e1)
				{
				}
			}
			else
			if(getBalance(source) == price())
			{
				paidPlayers.addElement(source);
				toldAlready.put(source.Name(),Boolean.FALSE);
				try
				{
					if (dir >= 0)
						observer.doCommand(CMParms.parse("OPEN " +CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
					observer.doCommand(CMParms.parse("BOW " + source.Name()),MUDCmdProcessor.METAFLAG_FORCED);
				}
				catch (final Exception e1)
				{
				}
			}
			else
			if(getBalance(source) < price())
			{
				CMMsg msg2=CMClass.getMsg(observer,null,msg.tool(),CMMsg.MSG_EMOTE,L("^E<S-NAME> look(s) carefully at <O-NAME>.^?"));
				msg.addTrailerMsg(msg2);
				msg2=CMClass.getMsg(observer,null,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'I'm afraid that this is insufficient.'^?."));
				msg.addTrailerMsg(msg2);
				msg2=CMClass.getMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
				msg.addTrailerMsg(msg2);
				charge(( (Coins) msg.tool()).value(), observer, msg.source());
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking, tickID);

		if (tickID != Tickable.TICKID_MOB)
		{
			return true;
		}
		if (!canFreelyBehaveNormal(ticking))
		{
			return true;
		}
		final MOB mob = (MOB) ticking;
		dir = findGate(mob);
		if (dir < 0)
		{
			CMLib.commands().postSay(mob, null, L("I'd shut the gate, but there isn't one..."), false, false);
			return true;
		}
		e = mob.location().getExitInDir(dir);
		if (!e.isOpen())
		{
			for (int j = 0; j < mob.location().numInhabitants(); j++)
			{
				final MOB M = mob.location().fetchInhabitant(j);
				if(M.playerStats()!=null)
				{
					if ( (paidPlayers.contains(M)) && (toldAlready.containsKey(M.Name())))
					{
						final Boolean B = toldAlready.get(M.Name());
						if (!B.booleanValue())
						{
							final String currency=CMLib.beanCounter().getCurrency(mob);
							final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
							final String balanceStr=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(getBalance(M)/denomination));
							CMLib.commands().postSay(mob, M,L("We still have record that you gave us @x1 before if you're heading through",balanceStr), true, false);
						}
						toldAlready.put(M.Name(), Boolean.TRUE);
						if (dir >= 0)
							mob.doCommand(CMParms.parse("OPEN " +CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
					}
					else
					{
						if(toldAlready.containsKey(M.Name()))
							continue;
						final String currency=CMLib.beanCounter().getCurrency(mob);
						final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
						final String priceStr=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(price()/denomination));
						CMLib.commands().postSay(mob, M,L("I'll let you through here if you pay the fee of @x1.",priceStr), true, false);
						toldAlready.put(M.Name(), Boolean.TRUE);
						if (debug)	// debugging
							CMLib.commands().postSay(mob, M,L("I'm telling you this from tick"), true, false);
					}
				}
			}
		}
		final boolean nightTime = (mob.location().getArea().getTimeObj().getTODCode() == TimeClock.TimeOfDay.NIGHT);
		if (nightTime)
		{
			if ( (!e.isLocked()) && (e.hasALock()))
			{
				if (getMyKeyTo(mob, e) != null)
				{
					final CMMsg msg = CMClass.getMsg(mob, e, CMMsg.MSG_LOCK, L("<S-NAME> lock(s) <T-NAME>."));
					if (mob.location().okMessage(mob, msg))
					{
						CMLib.utensils().roomAffectFully(msg, mob.location(), dir);
					}
				}
			}
		}
		else
		if (e.isLocked())
		{
			if (getMyKeyTo(mob, e) != null)
			{
				final CMMsg msg = CMClass.getMsg(mob, e, CMMsg.MSG_UNLOCK, L("<S-NAME> unlock(s) <T-NAME>."));
				if (mob.location().okMessage(mob, msg))
				{
					CMLib.utensils().roomAffectFully(msg, mob.location(), dir);
				}
			}
		}
		tickTock++;
		if (tickTock > 2)
		{
			tickTock = 0;
			if ( (e.isOpen()) && (paidPlayers.isEmpty()))
			{
				mob.doCommand(CMParms.parse("CLOSE " +CMLib.directions().getDirectionName(dir)),MUDCmdProcessor.METAFLAG_FORCED);
			}
		}
		return true;
	}
}
