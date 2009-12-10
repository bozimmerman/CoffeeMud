package com.planet_ink.coffee_mud.Items.interfaces;

import java.util.Vector;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
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
public interface DeckOfCards extends HandOfCards
{
    public DeckOfCards createDeck(Environmental owner);
    // resetDeckBackTo52Cards()
    // resets the deck back to 52 cards.  It will
    // grab cards from all external sources first
    // and return them to the deck container owner.
    // If this fails to produce 52 cards, it will
    // create a set of cards for the deck.
    // this method also destroys any hands being
    // managed.
    public boolean resetDeckBackTo52Cards();
    // getPlayerHand(MOB player)
    // If a hand of cards has previously been added to this
    // deck for internal management, this method will return
    // that hand given the player object.
    public HandOfCards getPlayerHand(MOB player);

    // addPlayerHand(MOB player, HandOfCards cards)
    // adds and possibly creates a hand for the given player
    // if no hand is passed in, a new empty one is created
    // the hand is then added to our table, keyed by the player
    // object
    public HandOfCards addPlayerHand(MOB player, HandOfCards cards);
    // removePlayerHand(MOB player)
    // if the given player object has a hand of cards currently
    // being managed by this deck, this method will remove all
    // of the cards from the hand, return them to the deck,
    // then remove the hand from management, and destroy the hand.
    public void removePlayerHand(MOB player);
    // addCard(PlayingCard card)
    // this method adds to the base functionality found
    // in HandOfCards.java by ensuring that all cards
    // added to the deck are added face down.
    public boolean addCard(PlayingCard card);
}
