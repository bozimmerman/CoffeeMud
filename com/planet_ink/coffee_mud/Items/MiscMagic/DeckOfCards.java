package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
import java.util.*;

import com.planet_ink.coffee_mud.system.*;

/* 
   Copyright 2005-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Deck Of Cards
   This class represents a deck of 52 cards without jokers
   It inherets from the HandOfCards class for most of its
   important functionality such as retreiving and adding cards and
   shuffling and sorting.
   
   The Deck has added functionality to make the "reigning in" of cards
   once a game is over easier.  It also has methods to keep track of
   one or more Hands, each of which must be attributed to a player.
*/
public class DeckOfCards extends HandOfCards
{
	public String ID(){	return "DeckOfCards";}
    
    // a flag to tell us whether the deck instance
    // has already been filled with cards.  
	boolean alreadyFilled=false;
    
    // a vector of all the cards in the deck. 
    // this is our private cache, since, as soon
    // as cards start getting dealt, we would lose
    // track of them otherwise.
    protected Vector cardsCache=null;
    
    // this object can manage one or more hands
    // keyed by the mob/player object.
    // This functionality is optional.
    DVector hands=new DVector(2);
    
    // the constructor for this class doesn't do much except set
    // some of the display properties of the deck container
	public DeckOfCards()
	{
		super();
		name="A deck of cards";
		displayText="A deck of cards has been left here.";
		secretIdentity="A magical deck of cards.  Say \"Shuffle\" to me.";
		recoverEnvStats();
	}

    // makePlayingCard(int cardBitCode)
    // this method creates a playing card object for
    // population in the deck.  The card created is
    // determined by the cardCode, which is a bit masked
    // value where bits 4-5 determine suit, and lower bits
    // the value.  
    protected PlayingCard makePlayingCard(int cardBitCode)
    {
        Item I=CMClass.getItem("PlayingCard");
        I.baseEnvStats().setAbility(cardBitCode);
        I.recoverEnvStats();
        I.setContainer(this);
        return (PlayingCard)I;
    }

    // makeAllCards()
    // this method creates all 52 cards in the deck
    // and adds them to a vector which is returned.
    private Vector makeAllCards()
    {
        Vector allCards=new Vector();
        for(int i=0;i<PlayingCard.suits.length;i++)
            for(int ii=0;ii<PlayingCard.cards.length;ii++)
                allCards.addElement(makePlayingCard(PlayingCard.suits[i]+PlayingCard.cards[ii]));
        return allCards;
    }
    
    // fillInTheDeck()
    // this method creates all 52 cards in the deck
    // and adds them to a deck owner
    private Vector fillInTheDeck()
    {
        alreadyFilled=true;
        if(getContents().size()==0)
        {
            Vector allCards=makeAllCards();
            for(int i=0;i<allCards.size();i++)
                addCard((PlayingCard)allCards.elementAt(i));
        }
        cardsCache=getContents();
        hands.clear();
        return cardsCache;
    }
    
    // createDeck(Environmental owner)
    // This static method creates a new deck of cards container,
    // gives it to the given owner object (mob or room), and 
    // then populates the deck container with all appropriate cards.
    public static DeckOfCards createDeck(Environmental owner)
    {
        DeckOfCards deck=new DeckOfCards();
        if(owner instanceof MOB)
        {
            if(deck.owner==null)
                ((MOB)owner).addInventory(deck);
            else
                ((MOB)owner).giveItem(deck);
        }
        else
        if(owner instanceof Room)
        {
            if(deck.owner==null)
                ((Room)owner).addItem(deck);
            else
                ((Room)owner).bringItemHere(deck,Item.REFUSE_PLAYER_DROP);
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
    public boolean resetDeckBackTo52Cards()
    {
        if((cardsCache==null)||(cardsCache.size()==0))
            return false;
        
        // first retreive all our cards by looping
        // through our cached list.  If we already 
        // have a card, make sure its faced-down
        for(int i=0;i<cardsCache.size();i++)
        {
            PlayingCard card=(PlayingCard)cardsCache.elementAt(i);
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
                ((Item)cardsCache.elementAt(i)).destroy();
            cardsCache.clear();
            Vector allCards=makeAllCards();
            for(int i=0;i<allCards.size();i++)
                if(owner() instanceof Room)
                    ((Room)owner()).addItem((Item)allCards.elementAt(i));
                else
                if(owner() instanceof MOB)
                    ((MOB)owner()).addInventory((Item)allCards.elementAt(i));
            cardsCache=getContents();
        }
        return numberOfCards()==52;
    }
    
    // getPlayerHand(MOB player)
    // If a hand of cards has previously been added to this
    // deck for internal management, this method will return
    // that hand given the player object.
    public HandOfCards getPlayerHand(MOB player)
    {
        if(player!=null)
        for(int i=0;i<hands.size();i++)
            if(hands.elementAt(i,1)==player)
                return (HandOfCards)hands.elementAt(i,2);
        return null;
    }

    // addPlayerHand(MOB player, HandOfCards cards)
    // adds and possibly creates a hand for the given player
    // if no hand is passed in, a new empty one is created
    // the hand is then added to our table, keyed by the player
    // object
    public HandOfCards addPlayerHand(MOB player, HandOfCards cards)
    {
        if(player==null) return null;
        if(hands.contains(player))
            return (HandOfCards)hands.elementAt(hands.indexOf(player),2);
        if(cards==null) cards=HandOfCards.createEmptyHand(player);
        hands.addElement(player,cards);
        return cards;
    }

    // removePlayerHand(MOB player)
    // if the given player object has a hand of cards currently
    // being managed by this deck, this method will remove all
    // of the cards from the hand, return them to the deck,
    // then remove the hand from management, and destroy the hand.
    public void removePlayerHand(MOB player)
    {
        HandOfCards cards=getPlayerHand(player);
        if(cards==null) return;
        Vector cardSet=cards.getContents();
        for(int c=0;c<cardSet.size();c++)
            addCard((PlayingCard)cardSet.elementAt(c));
        hands.removeElement(player);
        cards.destroy();
    }
    
    // addCard(PlayingCard card)
    // this method adds to the base functionality found
    // in HandOfCards.java by ensuring that all cards
    // added to the deck are added face down.
    public boolean addCard(PlayingCard card)
    {
        if(card!=null) card.turnFaceDown();
        return super.addCard(card);
    }

    // this is a system event previewer method
    // its purpose is normally to preview any events occurring
    // in the same room, see if they are relevant to this object,
    // and if so, whether this object should modify or cancel
    // the event before it takes place.
    // There are two things our deck needs to handle here.
    public boolean okMessage(Environmental myHost, CMMsg msg)
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
                msg.source().tell("There are no cards left in the deck");
            else
            {
                Room R=CoffeeUtensils.roomLocation(this);
                if(R!=null)
                    R.show(msg.source(),null,this,CMMsg.MASK_GENERAL|CMMsg.MSG_QUIETMOVEMENT,
                            "<S-NAME> <S-HAS-HAVE> thoroughly shuffled <O-NAMESELF>.");
            }
            return false;
        }
        return super.okMessage(myHost,msg);
    }
    
}