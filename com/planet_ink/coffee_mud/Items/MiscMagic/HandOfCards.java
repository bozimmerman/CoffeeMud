package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
import java.util.*;

import com.planet_ink.coffee_mud.system.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class HandOfCards extends StdContainer implements MiscMagic
{
    public String ID(){ return "HandOfCards";}
    public static int[] suits={0,16,32,48};
    public static int[] cards={1,2,3,4,5,6,7,8,9,10,11,12,13};
    
    // if this hand or deck is owned by a mob or a room, then
    // than mob or room suffices as a container.  Otherwise,
    // we need an internal container to keep track.
    private Vector backupContents=new Vector();
    
    // the constructor for this class doesn't do much except set
    // some of the display properties of the deck container
    public HandOfCards()
    {
        super();
        setName("a hand of cards");
        setDisplayText("a pile of cards lay here");
        setDescription("");
        Sense.setGettable(this,false);
        Sense.setDroppable(this,false);
        Sense.setRemovable(this,false);
        baseEnvStats().setWeight(1);
        setCapacity(0);
        setContainTypes(Container.CONTAIN_SSCOMPONENTS);
        recoverEnvStats();
    }

    
    // this method is an override of the container object
    // method of the same name.  If is necessary because
    // if the hand or deck is owned by a mob or a room, then
    // than mob or room suffices as a container.  Otherwise,
    // we need to use the internal container to keep track.
    public Vector getContents()
    {
        if((owner instanceof MOB)||(owner instanceof Room))
            return super.getContents();
        return (Vector)backupContents.clone();
    }
    
    // public boolean shuffleDeck()
    // Shuffles the deck by removing a random card from the
    // middle of the deck and adding it to the bottom
    // 52*5 times.
    public boolean shuffleDeck()
    {
        Vector V=getContents();
        Environmental own=owner();
        if(V.size()==0)
            return false;
        for(int i=0;i<V.size()*5;i++)
        {
            Item I=(Item)V.elementAt(Dice.roll(1,V.size(),-1));
            I.setContainer(this);
            if(own instanceof MOB)
            {
                I.removeFromOwnerContainer();
                ((MOB)own).addInventory(I);
            }
            else
            if(own instanceof Room)
            {
                I.removeFromOwnerContainer();
                ((Room)own).addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
            }
            else
            {
                backupContents.removeElement(I);
                backupContents.addElement(I);
            }
        }
        return true;
    }
    
    // public Item getTopCardFromDeck()
    // returns the top card item object from the deck 
    public PlayingCard getTopCardFromDeck()
    {
        Vector deckContents=getContents();
        if(deckContents.size()==0) return null;
        PlayingCard card=(PlayingCard)deckContents.firstElement();
        if(card.owner() instanceof HandOfCards)
            ((HandOfCards)card.owner()).removeCard(card);
        return card;
    }
    
    // public boolean addCard(PlayingCard card)
    // returns the given card item object to 
    // the deck by removing it from its current
    // owner and adding it back to the decks owner
    // and container.  If doing this causes a players 
    // hand to be devoid of cards, the hand container
    // is destroyed.
    public boolean addCard(PlayingCard card)
    {
        if(card==null) return false;
        if(card.owner()==owner())
        {
            card.setContainer(this);
            return true;
        }
        if(card.owner() instanceof HandOfCards)
            ((HandOfCards)card.owner()).removeCard(card);
        if(owner() instanceof MOB)
            ((MOB)owner()).giveItem(card);
        else
        if(owner() instanceof Room)
            ((Room)owner()).bringItemHere(card,Item.REFUSE_PLAYER_DROP);
        else
            backupContents.addElement(card);
        card.setContainer(this);
        if(owner() instanceof MOB)
            return ((MOB)owner()).isMine(card);
        else
        if(owner() instanceof Room)
            return ((Room)owner()).isContent(card);
        return true;
    }

    // public int numberOfCardsInTheDeck()
    // returns the current number of cards
    // in the deck.
    public int numberOfCards()
    {
        Vector deckContents=getContents();
        return deckContents.size();
    }
    
    public boolean removeCard(PlayingCard card)
    {
        Vector handContents=getContents();
        if(handContents.contains(card))
        {
            if((card.owner() instanceof MOB)
            ||(card.owner() instanceof Room))
                card.removeFromOwnerContainer();
            else
                backupContents.remove(card);
            return true;
        }
        return false;
    }
    
    public boolean removeAllCards()
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return false;
        for(int i=0;i<handContents.size();i++)
            removeCard((PlayingCard)handContents.elementAt(i));
        return true;
    }
    
    
    // public String[] getContentsEncoded()
    // This method builds a string array equal in size to the deck.
    // It then returns the contents of the deck encoded in 
    // cardStringCode format.  See convertCardBitCodeToCardStringCode
    public String[] getContentsEncoded()
    {
        Vector contents=getContents();
        String[] encodedDeck=new String[contents.size()];
        for(int i=0;i<contents.size();i++)
        {
            PlayingCard card=(PlayingCard)contents.elementAt(i);
            encodedDeck[i]=card.getStringEncodedSuit()+card.getStringEncodedValue();
        }
        return encodedDeck;
    }
    
    // public void sortByValueAceLow()
    // This method is a sort of anti-shuffle.  It puts the cards in
    // order, first by value, then by suit, with ace low.
    public void sortByValueAceLow()
    {
        Vector unsorted=getContents();
        if(unsorted.size()==0) return;
        // first step is to get them out of the deck and
        // then re-add them in order.
        for(int u=0;u<unsorted.size();u++)
            removeCard((PlayingCard)unsorted.elementAt(u));
        
        // now we pick them out in order
        while(unsorted.size()>0)
        {
            PlayingCard card=(PlayingCard)unsorted.firstElement();
            int cardBitEncodedValue=card.getBitEncodedValue();
            for(int u=1;u<unsorted.size();u++)
            {
                PlayingCard card2=(PlayingCard)unsorted.elementAt(u);
                int card2BitEncodedValue=card2.getBitEncodedValue();
                if((card2BitEncodedValue<cardBitEncodedValue)
                ||((card2BitEncodedValue==cardBitEncodedValue)
                    &&(card2.getBitEncodedSuit()<card.getBitEncodedSuit())))
                {
                    card=card2;
                    cardBitEncodedValue=card.getBitEncodedValue();
                }
            }
            unsorted.remove(card);
            addCard(card);
        }
    }
    
    
    // public void sortByValueAceHigh()
    // This method is a sort of anti-shuffle.  It puts the cards in
    // order, first by value, then by suit, with ace high.
    public void sortByValueAceHigh()
    {
        Vector unsorted=getContents();
        if(unsorted.size()==0) return;
        // first step is to get them out of the deck and
        // then re-add them in order.
        for(int u=0;u<unsorted.size();u++)
            removeCard((PlayingCard)unsorted.elementAt(u));
        
        // now we pick them out in order
        while(unsorted.size()>0)
        {
            PlayingCard card=(PlayingCard)unsorted.firstElement();
            int cardBitEncodedValue=card.getBitEncodedValue();
            if(cardBitEncodedValue==1) cardBitEncodedValue=14;
            for(int u=1;u<unsorted.size();u++)
            {
                PlayingCard card2=(PlayingCard)unsorted.elementAt(u);
                int card2BitEncodedValue=card2.getBitEncodedValue();
                if(card2BitEncodedValue==1) card2BitEncodedValue=14;
                if((card2BitEncodedValue<cardBitEncodedValue)
                ||((card2BitEncodedValue==cardBitEncodedValue)
                    &&(card2.getBitEncodedSuit()<card.getBitEncodedSuit())))
                {
                    card=card2;
                    cardBitEncodedValue=card.getBitEncodedValue();
                    if(cardBitEncodedValue==1) cardBitEncodedValue=14;
                }
            }
            unsorted.remove(card);
            addCard(card);
        }
    }
    
    public static HandOfCards createEmptyHand(Environmental player)
    {
        // calling this method without the intention
        // of putting a card inside is counter-productive.
        // the other methods should automatically create and
        // destroy the hands as cards are dealt and returned
        // to the deck respectively!
        HandOfCards hand=(HandOfCards)CMClass.getItem("HandOfCards");
        if(player instanceof MOB)
        {
            ((MOB)player).giveItem(hand);
            if(((MOB)player).isMine(hand))
                return hand;
            return null;
        }
        else
        if(player instanceof Room)
        {
            ((Room)player).addItemRefuse(hand,0);
            if(((Room)player).isContent(hand))
                return hand;
            return null;
        }
        return hand;
    }
    
    public boolean doesContainAtLeastOneOfValue(String cardStringCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return false;
        if(cardStringCode.length()==0) return false;
        if(cardStringCode.length()==1) cardStringCode=" "+cardStringCode;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getStringEncodedValue().equals(cardStringCode.substring(1)))
                return true;
        }
        return false;
    }
    
    public boolean containsAtLeastOneOfSuit(String cardStringCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return false;
        if(cardStringCode.length()==0) return false;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getStringEncodedSuit().charAt(0)==cardStringCode.charAt(0))
                return true;
        }
        return false;
    }
    
}