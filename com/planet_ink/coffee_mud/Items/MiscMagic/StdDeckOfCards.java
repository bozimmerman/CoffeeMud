package com.planet_ink.coffee_mud.Items.MiscMagic;
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

	Deck Of Cards
	This class represents a deck of 52 cards without jokers
	It inherets from the HandOfCards class for most of its
	important functionality such as retreiving and adding cards and
	shuffling and sorting.

	The Deck has added functionality to make the "reigning in" of cards
	once a game is over easier.  It also has methods to keep track of
	one or more Hands, each of which must be attributed to a player.
*/

public class StdDeckOfCards extends StdHandOfCards implements DeckOfCards
{
	@Override
	public String ID()
	{
		return "StdDeckOfCards";
	}

	// a flag to tell us whether the deck instance
	// has already been filled with cards.
	boolean alreadyFilled=false;

	// a vector of all the cards in the deck.
	// this is our private cache, since, as soon
	// as cards start getting dealt, we would lose
	// track of them otherwise.
	protected List<Item> cardsCache=null;

	// this object can manage one or more hands
	// keyed by the mob/player object.
	// This functionality is optional.
	DVector hands=new DVector(2);

	// the constructor for this class doesn't do much except set
	// some of the display properties of the deck container
	public StdDeckOfCards()
	{
		super();
		name="A deck of cards";
		displayText=L("A deck of cards has been left here.");
		secretIdentity="A magical deck of cards.  Say \"Shuffle\" to me.";
		recoverPhyStats();
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	// makePlayingCard(int cardBitCode)
	// this method creates a playing card object for
	// population in the deck.  The card created is
	// determined by the cardCode, which is a bit masked
	// value where bits 4-5 determine suit, and lower bits
	// the value.
	protected PlayingCard makePlayingCard(int cardBitCode)
	{
		final Item I=CMClass.getItem("StdPlayingCard");
		I.basePhyStats().setAbility(cardBitCode);
		I.recoverPhyStats();
		I.setContainer(this);
		return (PlayingCard)I;
	}

	// makeAllCards()
	// this method creates all 52 cards in the deck
	// and adds them to a vector which is returned.
	private Vector<PlayingCard> makeAllCards()
	{
		final Vector<PlayingCard> allCards=new Vector<PlayingCard>();
		for (final int suit : PlayingCard.suits)
		{
			for (final int card : PlayingCard.cards)
				allCards.addElement(makePlayingCard(suit+card));
		}
		return allCards;
	}

	// fillInTheDeck()
	// this method creates all 52 cards in the deck
	// and adds them to a deck owner
	private List<Item> fillInTheDeck()
	{
		alreadyFilled=true;
		if(!hasContent())
		{
			final Vector<PlayingCard> allCards=makeAllCards();
			for(int i=0;i<allCards.size();i++)
				addCard(allCards.elementAt(i));
		}
		cardsCache=getContents();
		hands.clear();
		return cardsCache;
	}

	// createDeck(Environmental owner)
	// This static method creates a new deck of cards container,
	// gives it to the given owner object (mob or room), and
	// then populates the deck container with all appropriate cards.
	@Override
	public DeckOfCards createDeck(Environmental owner)
	{
		final StdDeckOfCards deck=new StdDeckOfCards();
		if(owner instanceof MOB)
		{
			if(deck.owner==null)
				((MOB)owner).addItem(deck);
			else
				((MOB)owner).moveItemTo(deck);
		}
		else
		if(owner instanceof Room)
		{
			if(deck.owner==null)
				((Room)owner).addItem(deck);
			else
				((Room)owner).moveItemTo(deck,ItemPossessor.Expire.Player_Drop);
		}
		deck.fillInTheDeck();
		return deck;
	}

	// resetDeckBackTo52Cards()
	// resets the deck back to 52 cards.  It will
	// grab cards from all external sources first
	// and return them to the deck container owner.
	// If this fails to produce 52 cards, it will
	// create a set of cards for the deck.
	// this method also destroys any hands being
	// managed.
	@Override
	public boolean resetDeckBackTo52Cards()
	{
		if((cardsCache==null)||(cardsCache.size()==0))
			return false;

		// first retrieve all our cards by looping
		// through our cached list.  If we already
		// have a card, make sure its faced-down
		for(int i=0;i<cardsCache.size();i++)
		{
			final PlayingCard card=(PlayingCard)cardsCache.get(i);
			if(card.owner()!=owner())
				addCard(card);
			else
				card.turnFaceDown();
		}
		// next destroy and clear any hands we may
		// be managing.
		for(int h=hands.size()-1;h>=0;h--)
			((Item)hands.elementAt(h,2)).destroy();
		hands.clear();

		// if something went wrong (because a player cast
		// disintegrate on their cards or something) we
		// just ditch the entire deck and rebuild it.
		if(numberOfCards()<52)
		{
			for(int i=0;i<cardsCache.size();i++)
				cardsCache.get(i).destroy();
			cardsCache.clear();
			final Vector<PlayingCard> allCards=makeAllCards();
			for(int i=0;i<allCards.size();i++)
				addCard(allCards.elementAt(i));
			cardsCache=getContents();
		}
		return numberOfCards()==52;
	}

	// getPlayerHand(MOB player)
	// If a hand of cards has previously been added to this
	// deck for internal management, this method will return
	// that hand given the player object.
	@Override
	public HandOfCards getPlayerHand(MOB player)
	{
		if(player!=null)
		for(int i=0;i<hands.size();i++)
		{
			if(hands.elementAt(i,1)==player)
				return (HandOfCards)hands.elementAt(i,2);
		}
		return null;
	}

	// addPlayerHand(MOB player, HandOfCards cards)
	// adds and possibly creates a hand for the given player
	// if no hand is passed in, a new empty one is created
	// the hand is then added to our table, keyed by the player
	// object
	@Override
	public HandOfCards addPlayerHand(MOB player, HandOfCards cards)
	{
		if(player==null)
			return null;
		if(hands.contains(player))
			return (HandOfCards)hands.elementAt(hands.indexOf(player),2);
		if(cards==null)
		{
			cards=((HandOfCards)CMClass.getMiscMagic("StdHandOfCards")).createEmptyHand(player);
		}
		hands.addElement(player,cards);
		return cards;
	}

	// removePlayerHand(MOB player)
	// if the given player object has a hand of cards currently
	// being managed by this deck, this method will remove all
	// of the cards from the hand, return them to the deck,
	// then remove the hand from management, and destroy the hand.
	@Override
	public void removePlayerHand(MOB player)
	{
		final HandOfCards cards=getPlayerHand(player);
		if(cards==null)
			return;
		final List<Item> cardSet=cards.getContents();
		for(int c=0;c<cardSet.size();c++)
			addCard((PlayingCard)cardSet.get(c));
		hands.removeElement(player);
		cards.destroy();
	}

	// addCard(PlayingCard card)
	// this method adds to the base functionality found
	// in HandOfCards.java by ensuring that all cards
	// added to the deck are added face down.
	@Override
	public boolean addCard(PlayingCard card)
	{
		if(card!=null)
			card.turnFaceDown();
		return super.addCard(card);
	}

	// this is a system event previewer method
	// its purpose is normally to preview any events occurring
	// in the same room, see if they are relevant to this object,
	// and if so, whether this object should modify or cancel
	// the event before it takes place.
	// There are two things our deck needs to handle here.
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// In this case, we use the instance of an event to trigger
		// the initial filling of the deck with cards, since, once
		// this object is capable of handling events, it must already
		// be "in the world" and ready to receive cards.
		if((!alreadyFilled)&&(owner()!=null))
			fillInTheDeck();

		// This handler also checks to see if anyone is saying "shuffle"
		// directly to the deck item.  If so, we cancel the message by
		// returning false (since it would make them look silly to be
		// talking to a deck of cards), and instead
		// display a message showing the owner of this deck shuffling it.
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().toUpperCase().indexOf("SHUFFLE")>0))
		{
			if(!shuffleDeck())
				msg.source().tell(L("There are no cards left in the deck"));
			else
			{
				final Room R=CMLib.map().roomLocation(this);
				if(R!=null)
					R.show(msg.source(),null,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,
							L("<S-NAME> <S-HAS-HAVE> thoroughly shuffled <O-NAMESELF>."));
			}
			return false;
		}
		return super.okMessage(myHost,msg);
	}

}
