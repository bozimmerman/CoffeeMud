package com.planet_ink.coffee_mud.Items.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.Environmental;

/*
Copyright 2005-2010 Bo Zimmerman

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
public interface HandOfCards extends Container
{
    public boolean shuffleDeck();
    // getTopCardFromDeck()
    // returns the top card item object from the deck 
    public PlayingCard getTopCardFromDeck();
    // addCard(PlayingCard card)
    // returns the given card item object to 
    // the deck by removing it from its current
    // owner and adding it back to the decks owner
    // and container.  If doing this causes a players 
    // hand to be devoid of cards, the hand container
    // is destroyed.
    public boolean addCard(PlayingCard card);
    // numberOfCards()
    // returns the current number of cards
    // in the deck.
    public int numberOfCards();
    // removeCard(PlayingCard card)
    // removes the given card from the 
    // deck and places it in limbo.  calls
    // to this method should be followed
    // by an addCard method on another deck.
    public boolean removeCard(PlayingCard card);
    // removeAllCards()
    // removes all cards from the deck and
    // places them in limbo.  Calls to this
    // method should be followed by either
    // a destroy method on the cards themselves
    // or an addCard method on another deck.
    public boolean removeAllCards();
    // getContentsEncoded()
    // This method builds a string array equal in size to the deck.
    // It then returns the contents of the deck encoded in 
    // cardStringCode format.  See convertCardBitCodeToCardStringCode
    public String[] getContentsEncoded();
    // sortByValueAceHigh()
    // This method is a sort of anti-shuffle.  It puts the cards in
    // order, first by value, then by suit, with ace considered high.
    public void sortByValueAceHigh();
    
    // sortByValueAceLow()
    // This method is a sort of anti-shuffle.  It puts the cards in
    // order, first by value, then by suit, with ace low.
    public void sortByValueAceLow();
    // createEmptyHand(Environmental player)
    // creates an empty HandOfCards object
    // if the player passed in is not null, it will
    // add the new hand to the inventory of the given 
    // hand-holder.  Either way, it will return the 
    // empty hand object.
    public HandOfCards createEmptyHand(Environmental player);

    // containsCard(String cardStringCode)
    // returns whether this hand contains a card of
    // the given string code value
    // a string code is a single letter suit followed
    // by a single letter for face cards and the ace,
    // or a number for other cards.
    public boolean containsCard(String cardStringCode);
    
    // getCard(String cardStringCode)
    // returns the PlayingCard from this deck or hand if
    // it is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter suit followed
    // by a single letter for face cards and the ace,
    // or a number for other cards.
    public PlayingCard getCard(String cardStringCode);
    
    // getFirstCardOfValue(String cardStringCode)
    // returns the first PlayingCard from this deck or hand
    // of the given value is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter for face cards 
    // and the ace, or a number for other cards.
    public PlayingCard getFirstCardOfValue(String cardStringCode);
    
    // containsAtLeastOneOfValue(String cardStringCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given value is to be found herein.  
    // a string code is a single letter for face cards 
    // and the ace, or a number for other cards.
    public boolean containsAtLeastOneOfValue(String cardStringCode);
    
    // containsAtLeastOneOfSuit(String cardStringCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given suit is to be found herein.  
    // a string code is a single letter suit 
    public boolean containsAtLeastOneOfSuit(String cardStringCode);
    
    // getFirstCardOfSuit(String cardStringCode)
    // returns the first PlayingCard from this deck or hand
    // of the given suit is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter suit 
    public PlayingCard getFirstCardOfSuit(String cardStringCode);
    
    // containsCard(int cardBitCode)
    // returns whether this hand contains a card of
    // the given bit code value
    // a bit code is as described in PlayingCard.java
    public boolean containsCard(int cardBitCode);
    
    // getCard(int cardBitCode)
    // returns the PlayingCard from this deck or hand if
    // it is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getCard(int cardBitCode);
    
    // getFirstCardOfValue(int cardBitCode)
    // returns the first PlayingCard from this deck or hand
    // of the given value is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getFirstCardOfValue(int cardBitCode);
    // containsAtLeastOneOfValue(int cardBitCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given value is to be found herein.  
    // a bit code is as described in PlayingCard.java
    public boolean containsAtLeastOneOfValue(int cardBitCode);
    
    // containsAtLeastOneOfSuit(int cardBitCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given suit is to be found herein.  
    // a bit code is as described in PlayingCard.java
    public boolean containsAtLeastOneOfSuit(int cardBitCode);
    
    // getFirstCardOfSuit(int cardBitCode)
    // returns the first PlayingCard from this deck or hand
    // of the given suit is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getFirstCardOfSuit(int cardBitCode);

}
