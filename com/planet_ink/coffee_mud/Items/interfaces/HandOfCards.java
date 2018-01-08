package com.planet_ink.coffee_mud.Items.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.Environmental;

/*
   Copyright 2005-2018 Bo Zimmerman

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
/**
 * A hand of cards is a partial group of cards container, containing
 * individual cards.  It is typically managed by the DeckOfCards object
 * that created it.
 * @author Bo Zimmerman
 */
public interface HandOfCards extends Container
{
	/**
	 * Shuffles the hand of cards, randomizing them. 
	 * @return the hand of cards, randomizing them.
	 */
	public boolean shuffleDeck();

	/**
	 * Returns the top card item object from the deck
	 * @return the top card item object from the deck
	 */
	public PlayingCard getTopCardFromDeck();
	
	/**
	 * Returns the given card item object to
	 * the deck by removing it from its current
	 * owner and adding it back to the decks owner
	 * and container.  If doing this causes a players
	 * hand to be devoid of cards, the hand container
	 * is destroyed.
	 * @param card the card to remove
	 * @return true if the card was moved, false if pigs fly
	 */
	public boolean addCard(PlayingCard card);
	
	/**
	 * Returns the current number of cards in the deck.
	 * @return number of cards in the deck
	 */
	public int numberOfCards();
	
	/**
	 * Removes the given card from the
	 * deck and places it in limbo.  Calls
	 * to this method should be followed
	 * by an addCard method on another deck.
	 * @param card the card to remove
	 * @return true if the card was there to remove, false otherwise
	 */
	public boolean removeCard(PlayingCard card);
	
	/**
	 * Removes all cards from the deck and
	 * places them in limbo.  Calls to this
	 * method should be followed by either
	 * a destroy method on the cards themselves
	 * or an addCard method on another deck.
	 * @return true if there were any cards removed, false otherwise
	 */
	public boolean removeAllCards();
	
	/**
	 * This method builds a string array equal in size to the deck.
	 * It then returns the contents of the deck encoded in
	 * cardStringCode format.  See convertCardBitCodeToCardStringCode
	 * @return string encoded list of the cards
	 */
	public String[] getContentsEncoded();
	
	/**
	 * This method is a sort of anti-shuffle.  It puts the cards in
	 * order, first by value, then by suit, with ace considered high.
	 */
	public void sortByValueAceHigh();

	/**
	 * This method is a sort of anti-shuffle.  It puts the cards in
	 * order, first by value, then by suit, with ace low.
	 */
	public void sortByValueAceLow();
	
	/**
	 * Creates an empty HandOfCards object
	 * if the player passed in is not null, it will
	 * add the new hand to the inventory of the given
	 * hand-holder.  Either way, it will return the
	 * empty hand object.
	 * @param player the holder can be a mob or a room
	 * @return the new empty hand of cards created.
	 */
	public HandOfCards createEmptyHand(Environmental player);

	/**
	 * Returns whether this hand contains a card of
	 * the given string code value.
	 * A string code is a single letter suit followed
	 * by a single letter for face cards and the ace,
	 * or a number for other cards.
	 * @param cardStringCode the encoded card string
	 * @return true if that card is here, false otherwise.
	 */
	public boolean containsCard(String cardStringCode);

	/**
	 * Gets the PlayingCard from this deck or hand if
	 * it is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * a string code is a single letter suit followed
	 * by a single letter for face cards and the ace,
	 * or a number for other cards.
	 * @param cardStringCode the encoded card string
	 * @return the card object, or null.
	 */
	public PlayingCard getCard(String cardStringCode);

	/**
	 * Returns the first PlayingCard from this deck or hand
	 * of the given value is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * a string code is a single letter for face cards
	 * and the ace, or a number for other cards.
	 * @param cardStringCode the encoded card string
	 * @return the card object, or null.
	 */
	public PlayingCard getFirstCardOfValue(String cardStringCode);

	/**
	 * Returns whether a PlayingCard in this deck or hand
	 * of the given value is to be found herein.
	 * A string code is a single letter for face cards
	 * and the ace, or a number for other cards.
	 * @param cardStringCode the encoded card string
	 * @return true if one was found, false otherwise
	 */
	public boolean containsAtLeastOneOfValue(String cardStringCode);

	/**
	 * Returns whether a PlayingCard in this deck or hand
	 * of the given suit is to be found herein.
	 * A string code is a single letter suit.
	 * @param cardStringCode the encoded card string
	 * @return true if one was found, false otherwise
	 */
	public boolean containsAtLeastOneOfSuit(String cardStringCode);

	/**
	 * Returns the first PlayingCard from this deck or hand
	 * of the given suit is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * A string code is a single letter suit.
	 * @param cardStringCode the encoded card string
	 * @return the card object, or null.
	 */
	public PlayingCard getFirstCardOfSuit(String cardStringCode);

	/**
	 * Returns whether this hand contains a card of
	 * the given bit code value.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return true if one was found, false otherwise
	 */
	public boolean containsCard(int cardBitCode);

	/**
	 * Returns the PlayingCard from this deck or hand if
	 * it is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return the card object, or null.
	 */
	public PlayingCard getCard(int cardBitCode);

	/**
	 * Returns the first PlayingCard from this deck or hand
	 * of the given value is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return the card object, or null.
	 */
	public PlayingCard getFirstCardOfValue(int cardBitCode);
	
	/**
	 * Returns whether a PlayingCard in this deck or hand
	 * of the given value is to be found herein.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return true if one was found, false otherwise
	 */
	public boolean containsAtLeastOneOfValue(int cardBitCode);

	/**
	 * Returns whether a PlayingCard in this deck or hand
	 * of the given suit is to be found herein.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return true if one was found, false otherwise
	 */
	public boolean containsAtLeastOneOfSuit(int cardBitCode);

	/**
	 * Returns the first PlayingCard from this deck or hand
	 * of the given suit is to be found herein.  DOES NOT REMOVE!
	 * removeCard should be called next to do that.
	 * A bit code is as described in PlayingCard.java
	 * @see PlayingCard#getBitEncodedValue()
	 * @param cardBitCode the card bit code
	 * @return the card object, or null.
	 */
	public PlayingCard getFirstCardOfSuit(int cardBitCode);
}
