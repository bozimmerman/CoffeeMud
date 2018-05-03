package com.planet_ink.coffee_mud.Items.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;

/*
   Copyright 2004-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http:*www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
/**
 * A playing card is just what it sounds like - a card you can play card games
 * with. It is typically contained in both a HandOfCards, managed by a
 * DeckOfCards that created it.
 * 
 * @author Bo Zimmerman
 */
public interface PlayingCard extends Item 
{
	/** A static list of bitmap values, one for each suit.  In order, spades, clubs, hearts, diamonds.  Needs to be enumified. */
	public static int[] suits = { 0, 16, 32, 48 };
	
	/** A static list of card values, 2 through 14, with the ace as 14. */
	public static int[] cards = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	/**
	 * Returns the bitmap value that represents the suit of this card.
	 * @see PlayingCard#suits
	 * @return the bitmap value that represents the suit of this card
	 */
	public int getBitEncodedSuit();

	/**
	 * Returns the full bit-encoded value of this card, with suit | value.
	 * @see PlayingCard#suits
	 * @see PlayingCard#cards
	 * The card values are 2-14, the suit bitmasks are 0, 16, 32, and 48.
	 * Face up is bit 64.
	 * @return the full bit-encoded value of this card
	 */
	public int getBitEncodedValue();

	/**
	 * Gets whether the card is face up
	 * @see PlayingCard#turnFaceUp()
	 * @see PlayingCard#turnFaceDown()
	 * Face up is bit 64.
	 * @return true if the card is face up, false if down
	 */
	public boolean isFaceUp();

	/**
	 * Sets the card as face up
	 * @see PlayingCard#isFaceUp()
	 * @see PlayingCard#turnFaceDown()
	 * Face up is bit 64.
	 */
	public void turnFaceUp();

	/**
	 * Sets the card as face down
	 * @see PlayingCard#isFaceUp()
	 * @see PlayingCard#turnFaceDown()
	 * Face up is bit 64.
	 */
	public void turnFaceDown();

	/**
	 * Returns the suit of this card as a single letter string
	 * @return the suit of this card as a single letter string
	 */
	public String getStringEncodedSuit();

	/**
	 * Return the value of this card as a short string
	 * face cards are only a single letter
	 * @return the value of this card as a short string
	 */
	public String getStringEncodedValue();

	/**
	 * Returns the english-word representation of the value
	 * passed to this method. Since this method is static,
	 * it may be called as a utility function and does not
	 * necessarily represent THIS card object.
	 * @param value the value to return a description of
	 * @return the value passed in as a string
	 */
	public String getCardValueLongDescription(int value);

	/**
	 * Returns partial english-word representation of the value
	 * passed to this method. By partial I mean numeric for
	 * number cards and words otherwise. Since this method is static,
	 * it may be called as a utility function and does not
	 * necessarily represent THIS card object.
	 * @param value the value to return a description of
	 * @return the value passed in as a string
	 */
	public String getCardValueShortDescription(int value);

	/**
	 * Returns an english-word, color-coded representation
	 * of the suit passed to this method. Since this method is static,
	 * it may be called as a utility function and does not
	 * necessarily represent THIS card object.
	 * @param suit the suit to return a description of
	 * @return the suit passed in as a string
	 */
	public String getSuitDescription(int suit);
}
