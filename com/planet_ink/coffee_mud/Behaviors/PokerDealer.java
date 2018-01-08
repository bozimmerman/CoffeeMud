package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Items.interfaces.PlayingCard;
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
	Copyright (c) 2005-2018 Bo Zimmerman
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
	following conditions are met:

	* Redistributions of source code must retain the above copyright notice, this list of conditions and the following
	disclaimer.

	* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
	disclaimer in the documentation and/or other materials provided with the distribution.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public class PokerDealer extends StdBehavior
{
	@Override
	public String ID()
	{
		return "PokerDealer";
	}

	// the poker dealer operates as a state machine
	// each state tells this behavior what to expect, and
	// how to react to player actions.
	private static final int STATE_MASK=15;
	private static final int STATE_WAITING_FOR_ANTIS=0;
	private static final int STATE_DEALING=1;
	private static final int STATE_FIRST_BETTER=2;
	private static final int STATE_NEXT_BETTER=3;
	private static final int STATE_DONE_BETTING=4;
	private static final int STATE_FIRST_DRAW=5;
	private static final int STATE_NEXT_DRAW=6;
	private static final int STATE_DONE_DRAWING=7;

	// this mask is added to the gameState when the state
	// changes, and the dealer needs to notify the
	// player or players of the change in state.
	private static final int STATEMASK_NEED_ANOUNCEMENT=256;

	// for draw or stud games, this lets us know our
	// progress in the game.  The deal is round 0, after
	// a draw or the next card is round 1, and so forth.
	private int roundOfPlay=0;

	// the following constants are used to determine
	// how long the dealer will wait before starting
	// a game once antis are dropped, or how long
	// a player is allowed to bet.  Since timers are
	// checked every tick (3-4 secs), these are approx.
	private static final int TIME_SECONDSTOSTART=20;
	private static final int TIME_SECONDSTOFIRSTBET=10;
	private static final int TIME_SECONDSTOBET=30;
	private static final int TIME_SECONDSTODRAW=30;

	// the count down to begin a game starts when the first
	// player puts down an anti.  This holds the time, in
	// miliseconds, when the event expires.  Players also
	// have a time limit on betting and drawing.  This
	// number is used for that purpose as well.
	private long timer=0;

	// the current state of our game, whether we are
	// doing any of the events described by the constants
	// above
	private int gameState=STATE_WAITING_FOR_ANTIS|STATEMASK_NEED_ANOUNCEMENT;

	// whose turn it is to play, or bet, or whatever
	// -- depends on gameState also.
	// NULL means noone has been determined.
	// otherwise, its the mob object whose turn it is.
	private MOB whoseTurn=null;

	// our deck, which will handle hands and other
	// conveniences.
	private DeckOfCards myDeck=null;
	private synchronized DeckOfCards theDeck()
	{
		if(myDeck==null)
			myDeck=((DeckOfCards)CMClass.getMiscMagic("StdDeckOfCards")).createDeck(null);
		return myDeck;
	}

	// equates for the gameRules variable below
	public static final int GAME_STRAIGHTPOKER=0;
	public static final int GAME_5CARDSTUD=1;
	public static final int GAME_7CARDSTUD=2;
	public static final int GAME_DRAWPOKER=3;
	public static final String[] GAME_DESCS={
		"Straight Poker",
		"Five Card Stud",
		"Seven Card Stud",
		"Draw Poker",
	};

	@Override
	public String accountForYourself()
	{
		return "poker dealing";
	}

	// this method allows permanent parameters
	// to be set on the behavior.  It will handle
	// external specification of the game
	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		final String str=CMParms.getParmStr(newParms,"GAME","0");
		if(CMath.isInteger(str)&&(CMath.s_int(str)>=0)&&(CMath.s_int(str)<GAME_DESCS.length))
			gameRules=CMath.s_int(str);
		else
		for(int i=0;i<GAME_DESCS.length;i++)
		{
			if(str.equalsIgnoreCase(GAME_DESCS[i]))
				gameRules=i;
		}
		anti=CMParms.getParmDouble(newParms,"ANTI",1.0);
		minPlayers=CMParms.getParmInt(newParms,"MINPLAYERS",2);
	}

	// the game that is being played.  Depending on behavior parameters,
	// this variable may change between games, or remain static. The
	// default value is 0 (straight poker).
	private int gameRules=0;

	// the amount of currency required to "anti-up" and join the game.
	private double anti=1.0;

	// the minumum number of players to start a game.  Set to 1 to allow
	// the dealer to play.
	private int minPlayers=2;

	// this hashtable will be keyed by the players, and will hold the
	// amount of money the player has antied, or put down in bet, for
	// the current hand.
	private DVector pot=new DVector(2);

	// the actions a player may take, and the
	// words they should say outloud to declare
	// their intentions.
	private static final int PLAYER_BET_ACT_PASS=0;
	private static final int PLAYER_BET_ACT_CALL=1;
	private static final int PLAYER_BET_ACT_FOLD=2;
	private static final int PLAYER_BET_ACT_RAISE=3;
	private static final Object[][] PLAYER_BET_ACTIONS={
		{"PASS",Integer.valueOf(PLAYER_BET_ACT_PASS)},
		{"PAS",Integer.valueOf(PLAYER_BET_ACT_PASS)},
		{"CALL",Integer.valueOf(PLAYER_BET_ACT_CALL)},
		{"CAL",Integer.valueOf(PLAYER_BET_ACT_CALL)},
		{"FOLD",Integer.valueOf(PLAYER_BET_ACT_FOLD)},
		{"RAISE",Integer.valueOf(PLAYER_BET_ACT_RAISE)},
		{"RAIS",Integer.valueOf(PLAYER_BET_ACT_RAISE)},
		{"RASE",Integer.valueOf(PLAYER_BET_ACT_RAISE)}
	};

	// this is the list of possible hands
	// 5ofakind is not REALLY possible, since
	// wild cards are not yet supported, but
	// I still wanted a place for it.
	private static final int HAND_5OFAKIND=11<<20;
	private static final int HAND_ROYALFLUSH=10<<20;
	private static final int HAND_STRAIGHTFLUSH=9<<20;
	private static final int HAND_4OFAKIND=8<<20;
	private static final int HAND_FULLHOUSE=7<<20;
	private static final int HAND_FLUSH=6<<20;
	private static final int HAND_STRAIGHT=5<<20;
	private static final int HAND_3OFAKIND=4<<20;
	private static final int HAND_2PAIR=3<<20;
	private static final int HAND_1PAIR=2<<20;
	private static final int HAND_HIGHCARD=1<<20;
	private static final int HAND_MASK=15<<20;

	// this method just returns the amount of
	// anti as a displayable string.
	private String antiAmount(Environmental host)
	{
		final String currency=CMLib.beanCounter().getCurrency(host);
		return CMLib.beanCounter().abbreviatedPrice(currency,anti);
	}

	// since we don't know whether the host of this
	// behavior is a room or a mob, the way a message
	// is constructed differs slightly.  This method will
	// account for that.
	private CMMsg makeMessage(Environmental host, MOB target, int code, String message)
	{
		if(host instanceof MOB)
			return CMClass.getMsg((MOB)host,target,null,code,message);
		return CMClass.getMsg(CMLib.map().deity(),target,null,code,message);
	}

	// this method will communicate something to a
	// player or players.  If the host of this
	// behavior is a room or item, it will emote.  If
	// the host of this behavior is a mob, it will speak.
	private void communicate(Environmental host, MOB target, String message, CMMsg msg)
	{
		if(msg!=null)
		{
			if(host instanceof MOB)
				msg.addTrailerMsg(makeMessage(host,target,CMMsg.MSG_SPEAK,message));
			else
				msg.addTrailerMsg(makeMessage(host,target,CMMsg.MSG_OK_ACTION,message));
		}
		else
		if(host instanceof MOB)
			CMLib.commands().postSay((MOB)host,target,message,false,false);
		else
		{
			final Room R=CMLib.map().roomLocation(host);
			if(R!=null)
				R.showHappens(CMMsg.MSG_OK_ACTION,message);
		}
	}

	// checks the pot for the highest bid and returns it
	private double amountToCall()
	{
		double amountToCall=-1;
		for(int p=0;p<pot.size();p++)
		{
			final Double potAmount=(Double)pot.elementAt(p,2);
			if(potAmount.doubleValue()>amountToCall)
				amountToCall=potAmount.doubleValue();
		}
		return amountToCall;
	}

	// checks whether the pot is currently in balance.
	private boolean isThePotRight()
	{
		if(pot.size()<2)
			return true;
		double amountToCall=-1;
		for(int p=0;p<pot.size();p++)
		{
			final Double potAmount=(Double)pot.elementAt(p,2);
			if(amountToCall<0.0)
				amountToCall=potAmount.doubleValue();
			if(potAmount.doubleValue()!=amountToCall)
				return false;
		}
		return true;
	}

	// returns 0 if the player called a bet
	// return 1 if the player raised the bet
	// returns -1 if the player is below the bet
	public int getCalled0Raised1OrFolded(MOB player)
	{
		if(!pot.contains(player))
			return -1;
		final Double inPot=(Double)pot.elementAt(pot.indexOf(player),2);
		int numHigherThanPlayer=0;
		int numEqualToPlayer=0;
		for(int p=0;p<pot.size();p++)
		{
			if(pot.elementAt(p,1)!=player)
			{
				final Double potAmount=(Double)pot.elementAt(p,2);
				if(potAmount.doubleValue()==inPot.doubleValue())
					numEqualToPlayer++;
				else
				if(potAmount.doubleValue()>inPot.doubleValue())
					numHigherThanPlayer++;
			}
		}
		if(numHigherThanPlayer>0)
			return -1;
		if(numEqualToPlayer>0)
			return 0;
		return 1;
	}

	// general system event previewer.
	// when events in the same room as this behavior occur,
	// the messages are sent here before they happen.  the
	// "host" will be the object that is hosting this behavior.
	// The "msg" will be the details of the event which has occurred.
	// This method will return true if its ok to proceed, and false otherwise.
	@Override
	public boolean  okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;

		// if someone tries to pick money up off the ground here
		// they need to be stopped COLD.
		if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.target() instanceof Coins)
		&&(!msg.targetMajor(CMMsg.MASK_INTERMSG))
		&&(msg.source().location().isContent((Coins)msg.target())))
		{
			msg.source().tell(L("No touching the pot!"));
			return false;
		}

		// if someone drops some money on the ground, it
		// may be a bet, or an anti, or an invalid action
		// we should check for that first.
		if((msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.target() instanceof Coins)
		&&(!msg.targetMajor(CMMsg.MASK_INTERMSG)))
		{
			final Double inPot=pot.contains(msg.source())?(Double)pot.elementAt(pot.indexOf(msg.source()),2):null;
			final String currency=CMLib.beanCounter().getCurrency(host);
			final MOB playerDroppingMoney=msg.source();
			final Coins theMoneyDropped=(Coins)msg.target();

			// if they put down foreign currency, tell them its
			// wrong, then abort their attempt to drop it into
			// the pot.
			if(!currency.equals(theMoneyDropped.getCurrency()))
			{
				playerDroppingMoney.tell(L("That is not the proper currency.  This table is only dealing in @x1.",CMLib.beanCounter().getDenominationName(currency)));
				return false;
			}

			// game hasn't started yet, so only antis allowed
			// in this case.
			if((gameState&STATE_MASK)==STATE_WAITING_FOR_ANTIS)
			{
				if(timer<=0)
					timer=System.currentTimeMillis()+(TIME_SECONDSTOSTART*1000);
				// if they havn't antied yet
				if(inPot==null)
				{
					// if they havn't antied enough, abort their drop attempt.
					if(theMoneyDropped.getTotalValue()<anti)
					{
						msg.source().tell(L("Thats not enough.  The anti is @x1.",antiAmount(host)));
						return false;
					}

					// if they give too much, queue up a change making message.
					if(theMoneyDropped.getTotalValue()>anti)
					{
						final double change=theMoneyDropped.getTotalValue()-anti;
						Coins C=CMLib.beanCounter().makeBestCurrency(currency,change);
						final CMMsg changeMsg=CMClass.getMsg(playerDroppingMoney,C,null,
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,L("You anti up, picking up @x1 in change.",CMLib.beanCounter().abbreviatedPrice(currency,change)),
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null,
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,L("<S-NAME> antis up."));
						msg.addTrailerMsg(changeMsg);
						// now we want to clear the drop message,
						// and modify the amount actually being dropped.
						C=CMLib.beanCounter().makeBestCurrency(currency,anti);
						theMoneyDropped.setNumberOfCoins(C.getNumberOfCoins());
						theMoneyDropped.setDenomination(C.getDenomination());
						msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
					}
				}
				else
				{
					// if they've already antied, we abort this.
					playerDroppingMoney.tell(L("You have already antied, and can not anti again."));
					return false;
				}
			}
			else
			if(((gameState&STATE_MASK)!=STATE_FIRST_BETTER)
			&&((gameState&STATE_MASK)!=STATE_NEXT_BETTER))
			{
				// now, if the game is already started, but its not a betting round,
				// we reject their attempt to bet.
				playerDroppingMoney.tell(L("You can not bet at this time."));
				// abort the message
				return false;
			}
			else
			if(whoseTurn!=playerDroppingMoney)
			{
				// now, if the game is already started, we allow them only to bet on their turn.
				// if it is not their turn to bet, we abort.
				if(whoseTurn==null)
					playerDroppingMoney.tell(L("You can not bet at this time.  Please wait your turn."));
				else
					playerDroppingMoney.tell(L("It is @x1's turn to bet right now.  Please wait for the dealer to call on you.",whoseTurn.name()));
				// abort the message
				return false;
			}
			else
			{
				// first we determine the amount to call.
				final double amountToCall=amountToCall();
				// now, we check if they have called yet
				if((inPot==null)||(inPot.doubleValue()<amountToCall))
				{
					final double amountInPot=(inPot==null)?0.0:inPot.doubleValue();
					final double amountNeededToCall=amountToCall-amountInPot;
					final String instructions="  You may now raise by dropping more money, or end your betting round by saying 'call'.";

					// if they havn't given enough to call, allow it, but let them know.
					if(theMoneyDropped.getTotalValue()<amountNeededToCall)
						msg.source().tell(L("That won't be enough.  You'll still need to drop @x1 more to call.",CMLib.beanCounter().abbreviatedPrice(currency,amountNeededToCall-theMoneyDropped.getTotalValue())));
					else
					// if they give too much, make change
					if(theMoneyDropped.getTotalValue()>amountNeededToCall)
					{
						final double change=theMoneyDropped.getTotalValue()-amountNeededToCall;
						Coins C=CMLib.beanCounter().makeBestCurrency(currency,change);
						final CMMsg changeMsg=CMClass.getMsg(playerDroppingMoney,C,null,
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,L("You see the bet, picking up @x1 in change.@x2",CMLib.beanCounter().abbreviatedPrice(currency,change),instructions),
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null,
													  CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,L("<S-NAME> see(s) the bet."));
						msg.addTrailerMsg(changeMsg);
						// now we want to clear the drop message,
						// and modify the amount actually being dropped.
						C=CMLib.beanCounter().makeBestCurrency(currency,anti);
						theMoneyDropped.setNumberOfCoins(C.getNumberOfCoins());
						theMoneyDropped.setDenomination(C.getDenomination());
						msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
					}
					else
					// if they gave the perfect amount
					{
						//  we just want to change the drop message to our gambler prose.
						final String newMessage="<S-NAME> see(s) the bet.";
						msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),newMessage+instructions,msg.targetCode(),newMessage,msg.othersCode(),newMessage);
					}
				}
				else
				{
					// we enforce table stakes, so make the this player is not
					// betting the other players out of the game.
					final double totalDown=amountToCall+theMoneyDropped.getTotalValue();
					for(int p=0;p<pot.size();p++)
					{
						if(pot.elementAt(p,1)!=playerDroppingMoney)
						{
							final MOB mob =(MOB)pot.elementAt(p,1);
							if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<totalDown)
							{
								msg.source().tell(L("You may only bet up to @x1 due to the table-stakes rule.",CMLib.beanCounter().abbreviatedPrice(currency,CMLib.beanCounter().getTotalAbsoluteValue(mob,currency))));
								return false;
							}
						}

					}
					// if they are raising, we just change the drop message
					// to our fancy gamblers prose.
					final String newMessage="<S-NAME> raise(s) <T-NAMESELF>.";
					final String instructions="  You may continue raising by dropping more money.  Or say 'raise' to end your betting.";
					msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),newMessage+instructions,msg.targetCode(),newMessage,msg.othersCode(),newMessage);
				}
			}
		}
		return true;
	}

	// this method will handle the betting and drawing rounds
	// by changing whose turn it is and, if necessary,
	// changing the game state.
	private void nextPlayerNextState(Environmental host)
	{
		if(pot.size()<=0)
		{
			whoseTurn=null;
			endTheGame(host);
			gameState=STATE_WAITING_FOR_ANTIS|STATEMASK_NEED_ANOUNCEMENT;
			return;
		}
		MOB nextPlayer=(MOB)pot.elementAt(0,1);
		if(whoseTurn!=null)
		for(int p=0;p<pot.size()-1;p++)
		{
			if(pot.elementAt(p,1)==whoseTurn)
			{
				nextPlayer=(MOB)pot.elementAt(p+1,1);
				break;
			}
		}
		whoseTurn=nextPlayer;

		// if we still have more players to consider...
		switch(gameState)
		{
		case STATE_DEALING:
		case STATE_DONE_DRAWING:
			gameState=STATE_FIRST_BETTER|STATEMASK_NEED_ANOUNCEMENT;
			break;
		case STATE_FIRST_BETTER:
			if((isThePotRight())&&(whoseTurn==pot.elementAt(0,1)))
				gameState=STATE_DONE_BETTING|STATEMASK_NEED_ANOUNCEMENT;
			else
			if(isThePotRight())
				gameState=STATE_FIRST_BETTER|STATEMASK_NEED_ANOUNCEMENT;
			else
				gameState=STATE_NEXT_BETTER|STATEMASK_NEED_ANOUNCEMENT;
			break;
		case STATE_NEXT_BETTER:
			if(isThePotRight())
				gameState=STATE_DONE_BETTING|STATEMASK_NEED_ANOUNCEMENT;
			else
				gameState=STATE_NEXT_BETTER|STATEMASK_NEED_ANOUNCEMENT;
			break;
		case STATE_FIRST_DRAW:
			gameState=STATE_NEXT_DRAW|STATEMASK_NEED_ANOUNCEMENT;
			break;
		case STATE_NEXT_DRAW:
			if(whoseTurn==pot.elementAt(0,1))
				gameState=STATE_DONE_DRAWING|STATEMASK_NEED_ANOUNCEMENT;
			else
				gameState=STATE_NEXT_DRAW|STATEMASK_NEED_ANOUNCEMENT;
			break;
		}
	}

	// general system event handler.
	// when events in the same room as this behavior occur,
	// the messages are sent here.  the "host" will be the
	// object that is hosting this behavior.  The "msg" will
	// be the details of the event which has occurred.
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		// if someone enters the same room where this
		// behavior is operating, and a game is NOT
		// in progress, they should get a greeting!
		if((msg.targetMinor()==CMMsg.TYP_LOOK)
		&&(msg.othersMessage()==null)
		&&(msg.target() instanceof Room)
		&&(msg.target()==CMLib.map().roomLocation(host))
		&&((gameState&STATE_MASK)==STATE_WAITING_FOR_ANTIS))
			communicate(host,msg.source(),"Greetings "+msg.source().name()+"! The game we are playing here is "+GAME_DESCS[gameRules]+".  The anti is "+antiAmount(host)+".  Just drop the proper money here to play.",msg);

		// if someone drops some money on the ground, we
		// know that its a legal act.  we just need to
		// process it.
		if((msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.target() instanceof Coins)
		&&(!msg.targetMajor(CMMsg.MASK_INTERMSG)))
		{
			final double value=((Coins)msg.target()).getTotalValue();
			final MOB playerDroppingMoney=msg.source();
			final Double inPot=pot.contains(playerDroppingMoney)?(Double)pot.elementAt(pot.indexOf(playerDroppingMoney),2):null;

			// if they havn't antied yet
			if(inPot!=null)
				pot.setElementAt(pot.indexOf(msg.source()),2,Double.valueOf(value+inPot.doubleValue()));
			else
				pot.addElement(playerDroppingMoney,Double.valueOf(value));
		}

		// if an archon/sysop speaks, they might be requesting
		// an internal test, or a change of game type.
		// We should check for that.
		if((msg.othersMinor()==CMMsg.TYP_SPEAK)
		&&(whoseTurn!=msg.source())
		&&(CMSecurity.isASysOp(msg.source()))
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0))
		{
			final String textOfSay=CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase());
			if(textOfSay!=null)
			{
				// check for a request of a new game type
				for(int i=0;i<GAME_DESCS.length;i++)
				   if(textOfSay.indexOf(GAME_DESCS[i].toUpperCase())>=0)
				   {
					   communicate(host,null,"Ok, we'll now play "+GAME_DESCS[i]+".",msg);
					   gameRules=i;
					   return;
				   }

				// now check for internal test request
				// this will generate lots of hands, and output
				// their analysis to the log
				if(textOfSay.indexOf("DETERMINEHANDTEST")>=0)
				{
					for(int numCards=5;numCards<=7;numCards+=2)
					{
						final DVector scores=new DVector(2);
						for(int i=0;i<10000;i++)
						{
							final DeckOfCards deck2=((DeckOfCards)CMClass.getMiscMagic("StdDeckOfCards")).createDeck(null);
							deck2.shuffleDeck();
							deck2.shuffleDeck();
							final HandOfCards hand=((HandOfCards)CMClass.getMiscMagic("StdHandOfCards")).createEmptyHand(null);
							for(int ii=0;ii<numCards;ii++)
								hand.addCard(deck2.getTopCardFromDeck());
							final int score=determineHand(hand);
							int insertHere=-1;
							for(int ii=0;ii<scores.size();ii++)
							{
								if(((Integer)scores.elementAt(ii,1)).intValue()>=score)
								{
									insertHere=ii;
									break;
								}
							}
							if(insertHere<0)
								scores.addElement(Integer.valueOf(score),hand);
							else
								scores.insertElementAt(insertHere,Integer.valueOf(score),hand);
						}
						for(int i=0;i<scores.size();i++)
						{
							final HandOfCards hand=(HandOfCards)scores.elementAt(i,2);
							final Integer score=(Integer)scores.elementAt(i,1);
							final StringBuffer str=new StringBuffer("");
							final List<Item> handContents=hand.getContents();
							for(int ii=0;ii<handContents.size();ii++)
								str.append(((PlayingCard)handContents.get(ii)).getStringEncodedSuit()+((PlayingCard)handContents.get(ii)).getStringEncodedValue()+" ");
							Log.sysOut("TEST",str.toString()+": "+score.intValue()+": "+describeHand(score.intValue()));
						}
					}
				}
			}
		}

		// if a player speaks, they might be doing as the dealer
		// commands -- saying pass, call, fold, or raise
		// when a player speaks, we listen very carefully
		// this section only handles betting speech
		if((msg.othersMinor()==CMMsg.TYP_SPEAK)
		&&(whoseTurn==msg.source())
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0)
		&&(((gameState&STATE_MASK)==STATE_FIRST_BETTER)||((gameState&STATE_MASK)==STATE_NEXT_BETTER)))
		{
			// the proper person SPEAKS!
			// we should parse out their words..
			final String textOfSay=CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase());
			if(textOfSay!=null)
			{
				// now determine if they've said one
				// of the magic words (pass, fold, call, etc..
				// If they do, we can stop looping through
				// the word list and react to their statement.
				for (final Object[] element : PLAYER_BET_ACTIONS)
					if(textOfSay.equals(element[0]))
					{
						final MOB speaker=msg.source();
						final Double inPot=pot.contains(speaker)?(Double)pot.elementAt(pot.indexOf(speaker),2):null;
						final double amountToCall=amountToCall();
						final double amountInPot=(inPot==null)?0.0:inPot.doubleValue();
						final double amountNeededToCall=amountToCall-amountInPot;
						final String currency=CMLib.beanCounter().getCurrency(host);

						// now we check their words against the pot
						// and whatever they say, we move on
						switch(((Integer)element[1]).intValue())
						{
						case PLAYER_BET_ACT_FOLD:
						{
							msg.addTrailerMsg(makeMessage(host,speaker,CMMsg.MSG_OK_ACTION,"The dealer gathers up <T-YOUPOSS> cards."));
							nextPlayerNextState(host);
							theDeck().removePlayerHand(speaker);
							pot.removeElement(speaker);
							break;
						}
						case PLAYER_BET_ACT_PASS:
						{
							final int whatHappened=getCalled0Raised1OrFolded(speaker);
							if(whatHappened<0)
								communicate(host,speaker,"You can not pass.  You can either drop "+CMLib.beanCounter().abbreviatedPrice(currency,amountNeededToCall)+", or fold.",msg);
							else
							{
								if(isThePotRight())
									communicate(host,speaker,"<T-NAME> pass(es).",msg);
								else
								if(whatHappened>0)
									communicate(host,speaker,"<T-NAME> raise(s).",msg);
								else
									communicate(host,speaker,"<T-NAME> call(s).",msg);
								nextPlayerNextState(host);
							}
							break;
						}
						case PLAYER_BET_ACT_CALL:
						{
							final int whatHappened=getCalled0Raised1OrFolded(speaker);
							if(whatHappened<0)
								communicate(host,speaker,"You can not call until you have dropped "+CMLib.beanCounter().abbreviatedPrice(currency,amountNeededToCall)+".  You must drop or fold.",msg);
							else
							{
								if(whatHappened>0)
									communicate(host,speaker,"<T-NAME> raise(s).",msg);
								nextPlayerNextState(host);
							}
							break;
						}
						case PLAYER_BET_ACT_RAISE:
						{
							final int whatHappened=getCalled0Raised1OrFolded(speaker);
							if(whatHappened<0)
								communicate(host,speaker,"You can not raise until you have at least dropped "+CMLib.beanCounter().abbreviatedPrice(currency,amountNeededToCall)+" and then dropped more.  You must drop or fold.",msg);
							else
							{
								if(whatHappened==0)
									communicate(host,speaker,"<T-NAME> call(s).",msg);
								nextPlayerNextState(host);
							}
							break;
						}
						}
						break;
					}
			}
		}

		// if a player speaks, they might be doing as the dealer
		// commands -- saying pass, stand, or which cards to draw.
		// when a player speaks, we listen very carefully
		// this section only handles betting draws in draw poker
		if((msg.othersMinor()==CMMsg.TYP_SPEAK)
		&&(gameRules==GAME_DRAWPOKER)
		&&(whoseTurn==msg.source())
		&&(msg.source().location()==CMLib.map().roomLocation(host))
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0)
		&&(((gameState&STATE_MASK)==STATE_FIRST_DRAW)||((gameState&STATE_MASK)==STATE_NEXT_DRAW)))
		{
			// the proper person SPEAKS!
			// we should parse out their words..
			final String textOfSay=CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase());
			if(textOfSay!=null)
			{
				// now determine if they've said one
				// of the magic words (pass, stand, numbers, etc..
				// If they do, we can stop looping through
				// the word list and react to their statement.
				if(textOfSay.equalsIgnoreCase("PASS")||textOfSay.equalsIgnoreCase("STAND")||textOfSay.equalsIgnoreCase("STAND PAT"))
					nextPlayerNextState(host);
				else
				{
					// now we parse their words, and see if every word is a number,
					// and whether every number is between 1-5
					final Vector<String> parsed=CMParms.parse(textOfSay);
					final HandOfCards hand=theDeck().getPlayerHand(msg.source());
					boolean numbersOK=(hand!=null)&&(parsed.size()>0);
					if(hand!=null)
					for(int i=0;i<parsed.size();i++)
						if((!CMath.isInteger(parsed.elementAt(i)))
						||(CMath.s_int(parsed.elementAt(i))<=0)
						||(CMath.s_int(parsed.elementAt(i))>hand.numberOfCards()))
						{
							numbersOK=false;
							break;
						}

					// if it checks out, we remove the specified cards,
					// and deal new ones.
					if((numbersOK)&&(hand!=null))
					{
						final List<Item> cards=new Vector<Item>();
						cards.addAll(hand.getContents());
						final Vector<Item> removed=new Vector<Item>();
						// make a list of cards to remove
						for(int i=0;i<parsed.size();i++)
						{
							if(!removed.contains(cards.get(CMath.s_int(parsed.elementAt(i))-1)))
								removed.addElement(cards.get(CMath.s_int(parsed.elementAt(i))-1));
						}
						// remove them from our cards list
						for(int i=0;i<removed.size();i++)
							cards.remove(removed.elementAt(i));
						// if nothign is left, problem!
						if(cards.size()==0)
							communicate(host,whoseTurn,"You may not draw all of your cards.  Try asking for fewer.",msg);
						else
						if((cards.size()<2)&&(((PlayingCard)cards.get(0)).getBitEncodedValue()!=14))
							communicate(host,whoseTurn,"You may not draw all but one of your cards unless the remaining card is an Ace.  Try again.",msg);
						else
						{
							// everything is still good, so lets officially get those
							// cards out of the players hand and back in the deck
							for(int i=0;i<removed.size();i++)
								theDeck().addCard((PlayingCard)removed.elementAt(i));
							// now we deal the player new ones.
							dealToPlayer(host,whoseTurn,removed.size(),0);
							nextPlayerNextState(host);
						}
					}
				}
			}
		}
	}

	// The heart of poker.
	// it determines the best hand held.
	// it returns a numeric score for the hand
	// the better the hand, the higher the score
	// It can handle poker of 1 or more cards.
	private int determineHand(HandOfCards hand)
	{
		List<Item> cards=hand.getContents();
		if(cards.size()==0)
			return -1;

		// first check for flushes
		int flushSuit=-1;
		for (final int suit : PlayingCard.suits)
		{
			int tempSuitCount=0;
			int tempFlushSuit=0;
			for(int c=0;c<cards.size();c++)
			{
				if(((PlayingCard)cards.get(c)).getBitEncodedSuit()==suit)
				{
					tempSuitCount++;
					tempFlushSuit=((PlayingCard)cards.get(c)).getBitEncodedSuit();
				}
			}
			if(tempSuitCount>=5)
			{
				flushSuit=tempFlushSuit;
				break;
			}
		}

		// then for straights with ace high
		int highStraightCard=-1;
		if(cards.size()>=5)
		for(int window=cards.size()-5;window>=0;window--)
		{
			highStraightCard=((PlayingCard)cards.get(window+4)).getBitEncodedValue();
			if(highStraightCard==1)
				highStraightCard=14;
			for(int c=window+1;c<window+5;c++)
			{
				int cardBitValue=((PlayingCard)cards.get(c)).getBitEncodedValue();
				if(cardBitValue==1)
					cardBitValue=14;
				if(cardBitValue!=(((PlayingCard)cards.get(c-1)).getBitEncodedValue()+1))
					highStraightCard=-1;
			}
			if(highStraightCard>=0)
				break;
		}
		// then for straights with ace low
		if((highStraightCard<0)&&(cards.size()>=5))
		{
			hand.sortByValueAceLow();
			cards=hand.getContents();
			for(int window=cards.size()-5;window>=0;window--)
			{
				highStraightCard=((PlayingCard)cards.get(window+4)).getBitEncodedValue();
				for(int c=window+1;c<window+5;c++)
				{
					if(((PlayingCard)cards.get(c)).getBitEncodedValue()!=(((PlayingCard)cards.get(c-1)).getBitEncodedValue()+1))
						highStraightCard=-1;
				}
				if(highStraightCard>=0)
					break;
			}
			if(highStraightCard<0)
			{
				hand.sortByValueAceHigh();
				cards=hand.getContents();
			}
		}

		// now build a list of matches (pairs, etc)
		// put them in a set keyed by the face value of the
		// card
		DVector matches=new DVector(2);
		for(int c=1;c<cards.size();c++)
		{
			final Integer value=Integer.valueOf(((PlayingCard)cards.get(c)).getBitEncodedValue());
			if(value.intValue()==(((PlayingCard)cards.get(c-1)).getBitEncodedValue()))
			{
				final int index=matches.indexOf(value);
				if(index>=0)
					matches.setElementAt(index,2,Integer.valueOf(1+((Integer)matches.elementAt(index,2)).intValue()));
				else
					matches.addElement(value,Integer.valueOf(2));
			}
		}

		// then sort our matches so the MOST matches are highest
		final DVector sortedMatches=new DVector(2);
		while(matches.size()>0)
		{
			int num=0;
			for(int i=1;i<matches.size();i++)
			{
				if(((Integer)matches.elementAt(i,2)).intValue()<=((Integer)matches.elementAt(num,2)).intValue())
					num=i;
			}
			sortedMatches.addElement(matches.elementAt(num,1),matches.elementAt(num,2));
			matches.removeElementAt(num);
		}
		matches=sortedMatches;

		// a match of 5 cards means 5 of a kind!
		if((matches.size()>0)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()==5))
			return HAND_5OFAKIND+((Integer)matches.elementAt(matches.size()-1,1)).intValue();

		// check for straight flush
		if((highStraightCard==14)
		&&(flushSuit>0)
		&&hand.containsCard(flushSuit|10)
		&&hand.containsCard(flushSuit|11)
		&&hand.containsCard(flushSuit|12)
		&&hand.containsCard(flushSuit|13)
		&&hand.containsCard(flushSuit|1))
			return HAND_ROYALFLUSH+flushSuit;

		// check for straight flush
		if((highStraightCard>=0)
		&&(flushSuit>0)
		&&(hand.containsCard(flushSuit|highStraightCard))
		&&(hand.containsCard(flushSuit|highStraightCard-1))
		&&(hand.containsCard(flushSuit|highStraightCard-2))
		&&(hand.containsCard(flushSuit|highStraightCard-3))
		&&(hand.containsCard(flushSuit|highStraightCard-4)))
			return HAND_STRAIGHTFLUSH+(highStraightCard|flushSuit);

		// check for 4 of a kind
		if((matches.size()>0)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()==4))
		{
			for(int c=cards.size()-1;c>=0;c--)
			{
				if(((PlayingCard)cards.get(c)).getBitEncodedValue()!=((Integer)matches.elementAt(matches.size()-1,1)).intValue())
					return HAND_4OFAKIND+(((Integer)matches.elementAt(matches.size()-1,1)).intValue()<<15)+((PlayingCard)cards.get(c)).getBitEncodedValue();
			}
			return HAND_4OFAKIND+(((Integer)matches.elementAt(matches.size()-1,1)).intValue()<<15);
		}

		// check for a full house
		if((matches.size()>1)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()>2)
		&&(((Integer)matches.elementAt(matches.size()-2,2)).intValue()>1))
			return HAND_FULLHOUSE+(((Integer)matches.elementAt(matches.size()-1,1)).intValue()<<15)+((Integer)matches.elementAt(matches.size()-2,1)).intValue();

		// check for a flush
		if(flushSuit>0)
		{
			int addValue=0;
			int addTimes=0;
			for(int c=cards.size()-1;c>=0;c--)
			{
				if(((PlayingCard)cards.get(c)).getBitEncodedSuit()==flushSuit)
				{
					if((++addTimes)<4)
						addValue=(addValue<<4)+((PlayingCard)cards.get(c)).getBitEncodedValue();
				}
			}
			return HAND_FLUSH+(flushSuit<<13)+addValue;
		}

		// check for a straight
		if(highStraightCard>=0)
			return HAND_STRAIGHT+highStraightCard;

		// check for 3 of a kind
		if((matches.size()>0)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()==3))
		{
			final int value=((Integer)matches.elementAt(matches.size()-1,1)).intValue();
			int addValue=0;
			int addTimes=0;
			for(int c=cards.size()-1;c>=0;c--)
			{
				if(((PlayingCard)cards.get(c)).getBitEncodedValue()!=value)
				{
					if((++addTimes)<4)
						addValue=(addValue<<4)+((PlayingCard)cards.get(c)).getBitEncodedValue();
				}
			}
			return HAND_3OFAKIND+(value<<15)+addValue;
		}

		// check for 2 pair
		if((matches.size()>1)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()>1)
		&&(((Integer)matches.elementAt(matches.size()-2,2)).intValue()>1))
		{
			final int value=((Integer)matches.elementAt(matches.size()-1,1)).intValue();
			final int value1=((Integer)matches.elementAt(matches.size()-2,1)).intValue();
			int addValue=0;
			int addTimes=0;
			for(int c=cards.size()-1;c>=0;c--)
				if((((PlayingCard)cards.get(c)).getBitEncodedValue()!=value)
				&&(((PlayingCard)cards.get(c)).getBitEncodedValue()!=value1))
				{
					if((++addTimes)<2)
						addValue=(addValue<<4)+((PlayingCard)cards.get(c)).getBitEncodedValue();
				}
			return HAND_2PAIR+(value<<15)+(value1<<11)+addValue;
		}

		// check for a pair
		if((matches.size()>0)
		&&(((Integer)matches.elementAt(matches.size()-1,2)).intValue()>1))
		{
			final int value=((Integer)matches.elementAt(matches.size()-1,1)).intValue();
			int addValue=0;
			int addTimes=0;
			for(int c=cards.size()-1;c>=0;c--)
			{
				if(((PlayingCard)cards.get(c)).getBitEncodedValue()!=value)
				{
					if((++addTimes)<4)
						addValue=(addValue<<4)+((PlayingCard)cards.get(c)).getBitEncodedValue();
				}
			}
			return HAND_1PAIR+(value<<15)+addValue;
		}

		// no good hands, so build a high-card score
		int addValue=0;
		int addTimes=0;
		for(int c=cards.size()-1;c>=0;c--)
		{
			if((++addTimes)<5)
				addValue=(addValue<<4)+((PlayingCard)cards.get(c)).getBitEncodedValue();
		}
		return HAND_HIGHCARD+addValue;
	}

	// this method calls the determineHand
	// method repeatedly for all players,
	// determining the score for their hands.
	// It then passes back a 2 dimensional
	// DVector with the player mob object and
	// the score.  It is sorted by score.
	// since stud starts betting with high SHOWN cards,
	// we need a way to specify that only the face up
	// cards count for scoring purposes.
	private DVector determineAllSortedScores(boolean faceUpOnly)
	{
		final DVector unsortedScores=new DVector(2);
		for(int p=0;p<pot.size();p++)
		{
			final MOB mob=(MOB)pot.elementAt(p,1);
			HandOfCards hand=theDeck().getPlayerHand(mob);
			if(hand==null)
				continue;

			// if only the face cards count, we will pull them out,
			// put them in their own hand, generate a score for them
			// and use their score
			if(faceUpOnly)
			{
				final HandOfCards faceUpHand=((HandOfCards)CMClass.getMiscMagic("StdHandOfCards")).createEmptyHand(null);
				final List<Item> handContents=hand.getContents();
				for(int h=0;h<handContents.size();h++)
				{
					PlayingCard card=(PlayingCard)handContents.get(h);
					if(card.isFaceUp())
					{
						card=(PlayingCard)card.copyOf();
						faceUpHand.addCard(card);
					}
				}
				hand=faceUpHand;
			}
			final int score=determineHand(hand);
			if(score<0)
				continue;
			unsortedScores.addElement(mob,Integer.valueOf(score));
		}

		// now we have the scores, so sort them
		final DVector sortedScores=new DVector(2);
		while(unsortedScores.size()>0)
		{
			int topNum=0;
			for(int i=1;i<unsortedScores.size();i++)
			{
				if(((Integer)unsortedScores.elementAt(i,2)).intValue()<=((Integer)unsortedScores.elementAt(topNum,2)).intValue())
					topNum=i;
			}
			sortedScores.addElement(unsortedScores.elementAt(topNum,1),unsortedScores.elementAt(topNum,2));
			unsortedScores.removeElementAt(topNum);
		}
		return sortedScores;
	}

	// rips the score integer apart to
	// determine all the goodness about
	// the given hand.
	private String describeHand(int score)
	{
		final PlayingCard PC=(PlayingCard)CMClass.getItem("StdPlayingCard");
		if((score&HAND_MASK)==HAND_5OFAKIND)
		{
			score-=HAND_5OFAKIND;
			return "Five of a Kind of "+PC.getCardValueLongDescription(score)+"s";
		}
		if((score&HAND_MASK)==HAND_ROYALFLUSH)
		{
			score-=HAND_ROYALFLUSH;
			return "a Royal Flush of "+PC.getSuitDescription(score);
		}
		if((score&HAND_MASK)==HAND_STRAIGHTFLUSH)
		{
			score-=HAND_STRAIGHTFLUSH;
			return "a Straight Flush of "+PC.getSuitDescription(score)+", "+PC.getCardValueLongDescription(score)+" high";
		}
		if((score&HAND_MASK)==HAND_4OFAKIND)
		{
			score-=HAND_4OFAKIND;
			int base=(score&((1+2+4+8)<<15));
			score=score-base;
			base=base>>15;
			return "Four of a Kind of "+PC.getCardValueLongDescription(base)+"s, with a "+PC.getCardValueLongDescription(score)+" high card";
		}
		if((score&HAND_MASK)==HAND_FULLHOUSE)
		{
			score-=HAND_FULLHOUSE;
			int base=(score&((1+2+4+8)<<15));
			score=score-base;
			base=base>>15;
			return "a Full House: "+PC.getCardValueLongDescription(base)+"s over "+PC.getCardValueLongDescription(score)+"s";
		}
		if((score&HAND_MASK)==HAND_FLUSH)
		{
			score-=HAND_FLUSH;
			int suit=(score&((16+32)<<13));
			score=score-suit;
			suit=suit>>13;
			final StringBuffer str=new StringBuffer("a "+PC.getSuitDescription(suit)+" Flush: ");
			for(int i=0;i<4;i++)
			{
				final int value=(score&(1+2+4+8));
				score-=value;
				score=score>>4;
				if(value>0)
					str.append(PC.getCardValueLongDescription(value)+" ");
			}
			return str.toString().trim()+" high";
		}
		if((score&HAND_MASK)==HAND_STRAIGHT)
		{
			score-=HAND_STRAIGHT;
			return "a Straight, "+PC.getCardValueLongDescription(score)+" high";
		}
		if((score&HAND_MASK)==HAND_3OFAKIND)
		{
			score-=HAND_3OFAKIND;
			int combo=(score&((1+2+4+8)<<15));
			score=score-combo;
			combo=combo>>15;
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<4;i++)
			{
				final int value=(score&(1+2+4+8));
				score-=value;
				score=score>>4;
				if(value>0)
					str.append(PC.getCardValueLongDescription(value)+" ");
			}
			final String hand="Three of a Kind of "+PC.getCardValueLongDescription(combo)+"s";
			if(str.length()>0)
				return  hand+", with "+str.toString().trim()+" high";
			return hand;
		}
		if((score&HAND_MASK)==HAND_2PAIR)
		{
			score-=HAND_2PAIR;
			int combo1=(score&((1+2+4+8)<<15));
			score=score-combo1;
			combo1=combo1>>15;
			int combo2=(score&((1+2+4+8)<<11));
			score=score-combo2;
			combo2=combo2>>11;
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<2;i++)
			{
				final int value=(score&(1+2+4+8));
				score-=value;
				score=score>>4;
				if(value>0)
					str.append(PC.getCardValueLongDescription(value)+" ");
			}
			final String hand="Two Pair, "+PC.getCardValueLongDescription(combo1)+"s and "+PC.getCardValueLongDescription(combo2)+"s";
			if(str.length()>0)
				return  hand+", with "+str.toString().trim()+" high";
			return hand;
		}
		if((score&HAND_MASK)==HAND_1PAIR)
		{
			score-=HAND_1PAIR;
			int combo=(score&((1+2+4+8)<<15));
			score=score-combo;
			combo=combo>>15;
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<4;i++)
			{
				final int value=(score&(1+2+4+8));
				score-=value;
				score=score>>4;
				if(value>0)
					str.append(PC.getCardValueLongDescription(value)+" ");
			}
			final String hand="a Pair of "+PC.getCardValueLongDescription(combo)+"s";
			if(str.length()>0)
				return  hand+", with "+str.toString().trim()+" high";
			return hand;
		}
		if((score&HAND_MASK)==HAND_HIGHCARD)
		{
			score-=HAND_HIGHCARD;
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<5;i++)
			{
				final int value=(score&(1+2+4+8));
				score-=value;
				score=score>>4;
				if(value>0)
					str.append(PC.getCardValueLongDescription(value)+" ");
			}
			return  str.toString().trim()+" high";
		}
		return "";
	}

	// Resets all the internal variables so that
	// a new game can be played, and new anties taken.
	private void endTheGame(Environmental host)
	{
		timer=0;
		theDeck().resetDeckBackTo52Cards();
		pot.clear();
		gameState=STATE_WAITING_FOR_ANTIS|STATEMASK_NEED_ANOUNCEMENT;
		roundOfPlay=0;
	}

	// announces the winner of the game, if any
	// it forces the winner to get all the pot winnings.
	// it also announces what everyone had in their hands.
	private void announceWinners(Environmental host)
	{
		final DVector scores=determineAllSortedScores(false);
		if(scores.size()>0)
		{
			final MOB winner=(MOB)scores.elementAt(scores.size()-1,1);
			communicate(host,null,"The winner is "+winner.name()+", who has "+describeHand(((Integer)scores.elementAt(scores.size()-1,2)).intValue())+".",null);
			for(int d=scores.size()-1;d>=0;d--)
			{
				if(scores.elementAt(d,1)!=winner)
					communicate(host,null,((MOB)scores.elementAt(d,1)).name()+" had "+describeHand(((Integer)scores.elementAt(d,2)).intValue())+".",null);
			}

			// if the winner is here, give them all the gold on
			// the ground
			if((CMLib.map().roomLocation(host)==winner.location())
			&&(CMLib.flags().isInTheGame(winner,true)))
			{
				final Room R=winner.location();
				Item I=null;
				double totalValue=0.0;
				final Vector<Item> winnings=new Vector<Item>();
				for(int i=R.numItems()-1;i>=0;i--)
				{
					I=R.getItem(i);
					if(I instanceof Coins)
						winnings.addElement(I);
				}
				for(int i=0;i<winnings.size();i++)
				{
					I=winnings.elementAt(i);
					I.setContainer(null);
					totalValue+=((Coins)I).getTotalValue();
					I.destroy();
				}
				if(totalValue>0.0)
				{
					CMLib.beanCounter().giveSomeoneMoney(winner,CMLib.beanCounter().getCurrency(host),totalValue);
					R.show(winner,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> collect(s) @x1 in winnings.",CMLib.beanCounter().abbreviatedPrice(CMLib.beanCounter().getCurrency(host),totalValue)));
				}
			}
		}
		else
			communicate(host,null,"There was no winner in this game, and so the pot carries over to the next game.",null);
	}

	// deal cards to the given player
	// this method deals the number
	// of face down and/or face-up cards
	// specified.  It will give an appropriate
	// Message to the room.
	private void dealToPlayer(Environmental host,
							  MOB player,
							  int numberOfCards,
							  int numberFaceUp)
	{
		if(player==null)
			return;
		final Room R=CMLib.map().roomLocation(host);
		final HandOfCards hand=theDeck().getPlayerHand(player);
		if(hand!=null)
		{
			// first deal any "down cards"
			int actuallyDealtDown=0;
			for(int i=0;i<numberOfCards-numberFaceUp;i++)
			{
				final PlayingCard card=theDeck().getTopCardFromDeck();
				final int handSize=hand.numberOfCards();
				if(card!=null)
				{
					hand.addCard(card);
					card.turnFaceDown();
					actuallyDealtDown+=hand.numberOfCards()-handSize;
				}
			}
			// then deal any "up" cards
			int actuallyDealtUp=0;
			final StringBuffer dealtUp=new StringBuffer("");
			for(int i=0;i<numberFaceUp;i++)
			{
				final PlayingCard card=theDeck().getTopCardFromDeck();
				final int handSize=hand.numberOfCards();
				if(card!=null)
				{
					hand.addCard(card);
					card.turnFaceUp();
					dealtUp.append(card.name()+", ");
					actuallyDealtUp+=hand.numberOfCards()-handSize;
				}
			}

			// now, depending on what was done, give an appropriate
			// dealing message to the room so everyone knows what
			// happened.
			if((R!=null)&&((actuallyDealtDown+actuallyDealtUp)>0))
			{
				if(actuallyDealtUp==0)
				{
					if(actuallyDealtDown>0)
					{
						if(host instanceof MOB)
							R.show((MOB)host,player,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> deal(s) @x1 cards to <T-NAMESELF>.",""+actuallyDealtDown));
						else
							R.showHappens(CMMsg.MSG_OK_ACTION,L("The dealer deal(s) @x1 cards to @x2.",""+actuallyDealtDown,player.name()));
					}
				}
				else
				if(actuallyDealtDown==0)
				{
					if(dealtUp.length()>2)
					{
						if(host instanceof MOB)
							R.show((MOB)host,player,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> deal(s) to <T-NAMESELF>: @x1.",dealtUp.toString().substring(0,dealtUp.length()-2)));
						else
							R.showHappens(CMMsg.MSG_OK_ACTION,L("The dealer deal(s) to @x1: @x2.",player.name(),dealtUp.toString().substring(0,dealtUp.length()-2)));
					}
				}
				else
				if(dealtUp.length()>2)
				{
					if(host instanceof MOB)
						R.show((MOB)host,player,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> deal(s) @x1 cards to <T-NAMESELF> and turns up: @x2.",""+actuallyDealtDown,dealtUp.toString().substring(0,dealtUp.length()-2)));
					else
						R.showHappens(CMMsg.MSG_OK_ACTION,L("The dealer deal(s) @x1 cards to @x2 and turns up: @x3.",""+actuallyDealtDown,player.name(),dealtUp.toString().substring(0,dealtUp.length()-2)));
				}
			}
		}
		if(hand!=null)
			hand.sortByValueAceHigh();
	}

	// deal cards to all players
	// this method deals the number
	// of cards specified.  It will
	// give an appropriate Message by
	// called dealToPlayer repeatedly.
	private void dealToAll(Environmental host,
						   int numberOfCards,
						   int numberFaceUp)
	{
		for(int p=0;p<pot.size();p++)
		{
			final MOB player=(MOB)pot.elementAt(p,1);
			dealToPlayer(host,player,numberOfCards,numberFaceUp);
		}
	}

	// this method is called by the service engine
	// every 3-4 seconds.  It is used to initiate
	// timing events, and to initiate non-reactionary
	// events such as the dealers play.  What the
	// method returns doesn't really matter.  The
	// "ticking" variable refers to the object hosting
	// this behavior, and tickID is seldom important
	// but refers to the reason for the timed event.
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final Environmental host=(Environmental)ticking;
		final Room R=CMLib.map().roomLocation(host);

		// first see if we lost any players
		// due to leaving the room or the game.
		for(int p=pot.size()-1;p>=0;p--)
		{
			final MOB mob=(MOB)pot.elementAt(p,1);
			if(!R.isInhabitant(mob))
			{
				communicate(host,null,"Oops.. we lost "+mob.name()+".",null);
				theDeck().removePlayerHand(mob);
				pot.removeElement(mob);
			}
		}

		// now, if we are in a waiting state,
		// but the number of players has dropped below
		// minimum, we end the game
		switch((gameState)&STATE_MASK)
		{
		case STATE_FIRST_DRAW:
		case STATE_NEXT_DRAW:
		case STATE_FIRST_BETTER:
		case STATE_NEXT_BETTER:
		case STATE_DEALING:
		case STATE_DONE_DRAWING:
		case STATE_DONE_BETTING:
		{
			if(pot.size()==0)
			{
				communicate(host,null,"Looks like everyone's out.  Time to start over.",null);
				endTheGame(host);
				return true;
			}
			if(pot.size()==1)
			{
				announceWinners(host);
				endTheGame(host);
				return true;
			}
			break;
		}
		}

		// next lets check for announcements
		// that are flagged to occur.  This often
		// causes a timer to be set giving the player
		// a short time to respond.
		if((gameState&STATEMASK_NEED_ANOUNCEMENT)>0)
		{
			gameState=gameState-STATEMASK_NEED_ANOUNCEMENT;
			switch((gameState)&STATE_MASK)
			{
			case STATE_DEALING:
			case STATE_DONE_DRAWING:
				communicate(host,null,"Ok, everyone has their cards.  Take a few seconds to look them over, and then we'll start betting.",null);
				timer=System.currentTimeMillis()+(TIME_SECONDSTOFIRSTBET*1000);
				break;
			case STATE_FIRST_BETTER:
				if(whoseTurn!=null)
					communicate(host,whoseTurn,"Ok, "+whoseTurn.name()+", you may bet by dropping money, or you may say 'pass'.",null);
				communicate(host,whoseTurn,"You have "+TIME_SECONDSTOBET+" seconds.",null);
				timer=System.currentTimeMillis()+(TIME_SECONDSTOBET*1000);
				break;
			case STATE_FIRST_DRAW:
			case STATE_NEXT_DRAW:
				communicate(host,whoseTurn,"Ok, "+whoseTurn.name()+", you may request a draw by saying which cards you want dropped.  For example, say '1 3 5' to drop your first, third, and fifth card for a re-draw.  Say 'stand' if you do not want to draw.",null);
				communicate(host,whoseTurn,"You have "+TIME_SECONDSTODRAW+" seconds.",null);
				timer=System.currentTimeMillis()+(TIME_SECONDSTODRAW*1000);
				break;
			case STATE_NEXT_BETTER:
			{
				final Double inPot=pot.contains(whoseTurn)?(Double)pot.elementAt(pot.indexOf(whoseTurn),2):null;
				final double amountToCall=amountToCall();
				final double amountInPot=(inPot==null)?0.0:inPot.doubleValue();
				final double amountNeededToCall=amountToCall-amountInPot;
				final String currency=CMLib.beanCounter().getCurrency(host);
				if(amountNeededToCall==0.0)
					communicate(host,whoseTurn,"Ok, "+whoseTurn.name()+", you may open betting by dropping money, or you may say 'pass'.",null);
				else
					communicate(host,whoseTurn,"Ok, "+whoseTurn.name()+", you may call the bet of "+CMLib.beanCounter().abbreviatedPrice(currency,amountNeededToCall)+" by dropping money, or you may say 'fold'.",null);
				communicate(host,whoseTurn,"You have "+TIME_SECONDSTOBET+" seconds.",null);
				timer=System.currentTimeMillis()+(TIME_SECONDSTOBET*1000);
				break;
			}
			case STATE_DONE_BETTING:
			{
				// after betting, the next step
				// depends on the game being played and
				// what round of play it is.  Sometimes
				// the game is over after betting, sometimes
				// more cards are dealt, and sometimes cards
				// are discarded and drawn.
				timer=0;
				whoseTurn=null;
				switch(gameRules)
				{
				case PokerDealer.GAME_STRAIGHTPOKER:
					announceWinners((Environmental)ticking);
					endTheGame((Environmental)ticking);
					break;
				case PokerDealer.GAME_DRAWPOKER:
					switch(roundOfPlay)
					{
					case 0:
						roundOfPlay++;
						gameState=STATE_FIRST_DRAW|STATEMASK_NEED_ANOUNCEMENT;
						timer=0;
						nextPlayerNextState(host);
						break;
					case 1:
						announceWinners((Environmental)ticking);
						endTheGame((Environmental)ticking);
						break;
					}
					break;
				case PokerDealer.GAME_5CARDSTUD:
					switch(roundOfPlay)
					{
					case 0:
					case 1:
						communicate(host,null,"The next card is up.",null);
						dealToAll(host,1,1);
						roundOfPlay++;
						gameState=STATE_DEALING|STATEMASK_NEED_ANOUNCEMENT;
						timer=0;
						break;
					case 2:
						communicate(host,null,"The last card is down and dirty.",null);
						dealToAll(host,1,0);
						roundOfPlay++;
						gameState=STATE_DEALING|STATEMASK_NEED_ANOUNCEMENT;
						timer=0;
						break;
					default:
						announceWinners((Environmental)ticking);
						endTheGame((Environmental)ticking);
						break;
					}
					break;
				case PokerDealer.GAME_7CARDSTUD:
					switch(roundOfPlay)
					{
					case 0:
					case 1:
					case 2:
						communicate(host,null,"The next card is up.",null);
						dealToAll(host,1,1);
						roundOfPlay++;
						gameState=STATE_DEALING|STATEMASK_NEED_ANOUNCEMENT;
						timer=0;
						break;
					case 3:
						communicate(host,null,"The last card is down and dirty.",null);
						dealToAll(host,1,0);
						roundOfPlay++;
						gameState=STATE_DEALING|STATEMASK_NEED_ANOUNCEMENT;
						timer=0;
						break;
					default:
						announceWinners((Environmental)ticking);
						endTheGame((Environmental)ticking);
						break;
					}
					break;
				}
				break;
			}
			}
		}
		else
		// now, if the player was asked to do something, such as draw or bet,
		// and they fail to do so in the amount of time given, we must perform
		// a default action FOR them.
		if((timer>0)&&(System.currentTimeMillis()>timer))
		{
			switch((gameState)&STATE_MASK)
			{
			case STATE_DEALING:
			case STATE_DONE_DRAWING:
				// these events are always initiated by the dealer, so they are
				// always very short.  Therefore, unlike a typical "time-out",
				// they always occur.
				timer=0;
				// in stud games, after a deal, the highest shown
				// hand always starts betting.  Therefore, we
				// will reshuffle the pot to put the highest
				// shown hand first.
				if((gameRules==GAME_5CARDSTUD)||(gameRules==GAME_7CARDSTUD))
				{
					final DVector newPot=new DVector(2);
					boolean startAdding=false;
					final DVector scores=determineAllSortedScores(true);
					if(scores.size()>0)
					{
						final MOB winner=(MOB)scores.elementAt(scores.size()-1,1);
						final DVector theRest=new DVector(2);
						for(int p=0;p<pot.size();p++)
						{
							if(startAdding||pot.elementAt(p,1)==winner)
							{
								startAdding=true;
								newPot.addElement(pot.elementAt(p,1),pot.elementAt(p,2));
							}
							else
								theRest.addElement(pot.elementAt(p,1),pot.elementAt(p,2));
						}
						for(int p=0;p<theRest.size();p++)
							newPot.addElement(theRest.elementAt(p,1),theRest.elementAt(p,2));
						pot=newPot;
						communicate(host,null,winner.name()+" is showing the best hand with "+describeHand(((Integer)scores.elementAt(scores.size()-1,2)).intValue())+".",null);
					}
				}
				// begin the first betting state and announce it.
				whoseTurn=null;
				nextPlayerNextState(host);
				if(whoseTurn!=null)
					communicate(host,null,"Betting will start with "+whoseTurn.name()+".",null);
				break;
			case STATE_WAITING_FOR_ANTIS:
			{
				// the wait for players to anti is over.  Now we see if there are enough
				// players, shuffle the deck, announce the start of the game, and deal
				// the first hand.
				if(pot.size()<minPlayers)
				{
					communicate(host,null,"Well, I'm afraid we don't have enough players to start.  You will therefore have to re-anti and start over.",null);
					endTheGame((Environmental)ticking);
				}
				else
				{
					communicate(host,null,"OK! Antis are all in! Lets play some "+GAME_DESCS[gameRules]+"!",null);

					// lets shuffle a few times first.
					theDeck().shuffleDeck();
					theDeck().shuffleDeck();

					// now we create all the hands
					for(int p=0;p<pot.size();p++)
					{
						final MOB mob=(MOB)pot.elementAt(p,1);
						theDeck().addPlayerHand(mob,null);
					}

					// and then we deal.
					switch(gameRules)
					{
					case PokerDealer.GAME_STRAIGHTPOKER:
					case PokerDealer.GAME_DRAWPOKER:
						dealToAll(host,5,0);
						break;
					case PokerDealer.GAME_5CARDSTUD:
						dealToAll(host,2,1);
						break;
					case PokerDealer.GAME_7CARDSTUD:
						dealToAll(host,3,1);
						break;
					}
					gameState=STATE_DEALING|STATEMASK_NEED_ANOUNCEMENT;
					timer=0;
				}
				break;
			}
			case STATE_FIRST_DRAW:
			case STATE_NEXT_DRAW:
				// if a player doesn't draw in time, they stand
				if(whoseTurn!=null)
					communicate(host,whoseTurn,whoseTurn.name()+"'s  "+TIME_SECONDSTODRAW+" seconds have expired.  "+whoseTurn.charStats().HeShe()+" stands pat.",null);
				nextPlayerNextState(host);
				break;
			case STATE_FIRST_BETTER:
			case STATE_NEXT_BETTER:
			{
				// if a player fails to bet in time, it could mean a pass, call, or fold, depending
				// on what they managed to do in the time allotted.
				timer=0;
				if(whoseTurn!=null)
				{
					final int whatHappened=getCalled0Raised1OrFolded(whoseTurn);
					final String msg=whoseTurn.name()+"'s  "+TIME_SECONDSTOBET+" seconds have expired.";
					if(whatHappened<0)
					{
						communicate(host,whoseTurn,msg+"  "+whoseTurn.name()+" folds.",null);
						nextPlayerNextState(host);
						theDeck().removePlayerHand(whoseTurn);
						pot.removeElement(whoseTurn);
					}
					else
					{
						if(isThePotRight())
							communicate(host,whoseTurn,msg+"  "+whoseTurn.name()+" passes.",null);
						else
						if(whatHappened==0)
							communicate(host,whoseTurn,msg+"  "+whoseTurn.name()+" calls.",null);
						else
							communicate(host,whoseTurn,msg+"  "+whoseTurn.name()+" raises.",null);
						nextPlayerNextState(host);
					}
				}
				else
					nextPlayerNextState(host);
				break;
			}
			}
		}
		else
		// now, the last timed check is for events that are on a timer, but
		// the timer has NOT gone off yet.  In those cases, we might want
		// to give a time-out warning, such as we do for anti waits.
		if(timer>0)
		{
			switch(((gameState)&STATE_MASK))
			{
			case STATE_WAITING_FOR_ANTIS:
			{
				final long started=timer-(TIME_SECONDSTOSTART*1000);
				final long time=System.currentTimeMillis();
				final long tenseconds=timer-(1000*10);
				final long remaining=((timer-System.currentTimeMillis())/1000);
				if((time>started)&&(time<(started+CMProps.getTickMillis())))
					communicate(host,null,"We've received our first anti for "+GAME_DESCS[gameRules]+". We will start the game in "+remaining+" seconds.",null);
				else
				if((time>tenseconds)&&(time<(tenseconds+CMProps.getTickMillis())))
					communicate(host,null,"The "+GAME_DESCS[gameRules]+" game starts in "+remaining+" seconds.",null);
				break;
			}
			}
		}
		// tick methods in behaviors ALWAYS return true
		// the return is meaningless, but its as good a value as any.
		return true;
	}
}
