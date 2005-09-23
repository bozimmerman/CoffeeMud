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
   
   Hand Of Cards
   This object represents an arbitarily sized collection of
   PlayingCard objects.  It includes numerous methods for
   retreiving and adding cards, shuffling and sorting, and
   most expecially for returning a message of turned-up cards
   to anyone who LOOKS at a person holding a hand with such
   cars.
*/
public class HandOfCards extends StdContainer implements MiscMagic
{
    public String ID(){ return "HandOfCards";}
    
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
        // this type is arbitrary
        setContainTypes(Container.CONTAIN_SSCOMPONENTS);
        recoverEnvStats();
    }

    // getContents()
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
    
    // shuffleDeck()
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
    
    // getTopCardFromDeck()
    // returns the top card item object from the deck 
    public PlayingCard getTopCardFromDeck()
    {
        Vector deckContents=getContents();
        if(deckContents.size()==0) return null;
        PlayingCard card=(PlayingCard)deckContents.firstElement();
        if(card.container() instanceof HandOfCards)
            ((HandOfCards)card.container()).removeCard(card);
        return card;
    }
    
    // addCard(PlayingCard card)
    // returns the given card item object to 
    // the deck by removing it from its current
    // owner and adding it back to the decks owner
    // and container.  If doing this causes a players 
    // hand to be devoid of cards, the hand container
    // is destroyed.
    public boolean addCard(PlayingCard card)
    {
        if(card==null) return false;
        if(card.container() instanceof HandOfCards)
            ((HandOfCards)card.container()).removeCard(card);
        if(owner() instanceof MOB)
        {
            if(!((MOB)owner()).isMine(card))
                ((MOB)owner()).giveItem(card);
        }
        else
        if(owner() instanceof Room)
        {
            if(!((Room)owner()).isContent(card))
                ((Room)owner()).bringItemHere(card,Item.REFUSE_PLAYER_DROP);
        }
        else
        {
            card.setOwner(card);
            if(!backupContents.contains(card))
                backupContents.addElement(card);
        }
        card.setContainer(this);
        if(owner() instanceof MOB)
            return ((MOB)owner()).isMine(card);
        else
        if(owner() instanceof Room)
            return ((Room)owner()).isContent(card);
        return true;
    }

    // numberOfCards()
    // returns the current number of cards
    // in the deck.
    public int numberOfCards()
    {
        Vector deckContents=getContents();
        return deckContents.size();
    }

    // removeCard(PlayingCard card)
    // removes the given card from the 
    // deck and places it in limbo.  calls
    // to this method should be followed
    // by an addCard method on another deck.
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

    // removeAllCards()
    // removes all cards from the deck and
    // places them in limbo.  Calls to this
    // method should be followed by either
    // a destroy method on the cards themselves
    // or an addCard method on another deck.
    public boolean removeAllCards()
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return false;
        for(int i=0;i<handContents.size();i++)
            removeCard((PlayingCard)handContents.elementAt(i));
        return true;
    }
    
    
    // getContentsEncoded()
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
    
    // sortByValueAceHigh()
    // This method is a sort of anti-shuffle.  It puts the cards in
    // order, first by value, then by suit, with ace considered high.
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
    
    
    // sortByValueAceLow()
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
            if(cardBitEncodedValue==14) cardBitEncodedValue=1;
            for(int u=1;u<unsorted.size();u++)
            {
                PlayingCard card2=(PlayingCard)unsorted.elementAt(u);
                int card2BitEncodedValue=card2.getBitEncodedValue();
                if(card2BitEncodedValue==14) card2BitEncodedValue=1;
                if((card2BitEncodedValue<cardBitEncodedValue)
                ||((card2BitEncodedValue==cardBitEncodedValue)
                    &&(card2.getBitEncodedSuit()<card.getBitEncodedSuit())))
                {
                    card=card2;
                    cardBitEncodedValue=card.getBitEncodedValue();
                    if(cardBitEncodedValue==14) cardBitEncodedValue=1;
                }
            }
            unsorted.remove(card);
            addCard(card);
        }
    }

    // createEmptyHand(Environmental player)
    // creates an empty HandOfCards object
    // if the player passed in is not null, it will
    // add the new hand to the inventory of the given 
    // hand-holder.  Either way, it will return the 
    // empty hand object.
    public static HandOfCards createEmptyHand(Environmental player)
    {
        // calling this method without the intention
        // of putting a card inside is counter-productive.
        // the other methods should automatically create and
        // destroy the hands as cards are dealt and returned
        // to the deck respectively!
        HandOfCards hand=(HandOfCards)CMClass.getMiscMagic("HandOfCards");
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

    // containsCard(String cardStringCode)
    // returns whether this hand contains a card of
    // the given string code value
    // a string code is a single letter suit followed
    // by a single letter for face cards and the ace,
    // or a number for other cards.
    public boolean containsCard(String cardStringCode)
    { return getCard(cardStringCode)!=null;}
    
    // getCard(String cardStringCode)
    // returns the PlayingCard from this deck or hand if
    // it is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter suit followed
    // by a single letter for face cards and the ace,
    // or a number for other cards.
    public PlayingCard getCard(String cardStringCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        if(cardStringCode.length()<2) return null;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if((card.getStringEncodedSuit().charAt(0)==cardStringCode.charAt(0))
            &&card.getStringEncodedValue().equalsIgnoreCase(cardStringCode.substring(1)))
                return card;
        }
        return null;
    }
    
    // getFirstCardOfValue(String cardStringCode)
    // returns the first PlayingCard from this deck or hand
    // of the given value is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter for face cards 
    // and the ace, or a number for other cards.
    public PlayingCard getFirstCardOfValue(String cardStringCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        if(cardStringCode.length()==0) return null;
        if(cardStringCode.length()==1) cardStringCode=" "+cardStringCode;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getStringEncodedValue().equalsIgnoreCase(cardStringCode.substring(1)))
                return card;
        }
        return null;
    }
    
    // containsAtLeastOneOfValue(String cardStringCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given value is to be found herein.  
    // a string code is a single letter for face cards 
    // and the ace, or a number for other cards.
    public boolean containsAtLeastOneOfValue(String cardStringCode)
    { return getFirstCardOfValue(cardStringCode)!=null;}
    
    // containsAtLeastOneOfSuit(String cardStringCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given suit is to be found herein.  
    // a string code is a single letter suit 
    public boolean containsAtLeastOneOfSuit(String cardStringCode)
    { return getFirstCardOfSuit(cardStringCode)!=null;}
    
    // getFirstCardOfSuit(String cardStringCode)
    // returns the first PlayingCard from this deck or hand
    // of the given suit is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a string code is a single letter suit 
    public PlayingCard getFirstCardOfSuit(String cardStringCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        if(cardStringCode.length()==0) return null;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getStringEncodedSuit().charAt(0)==cardStringCode.charAt(0))
                return card;
        }
        return null;
    }
    
    // containsCard(int cardBitCode)
    // returns whether this hand contains a card of
    // the given bit code value
    // a bit code is as described in PlayingCard.java
    public boolean containsCard(int cardBitCode)
    { return getCard(cardBitCode)!=null;}
    
    // getCard(int cardBitCode)
    // returns the PlayingCard from this deck or hand if
    // it is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getCard(int cardBitCode)
    {
        if((cardBitCode&(1+2+4+8))==1)
            cardBitCode=(cardBitCode&(16+32))+14;
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.envStats().ability()==cardBitCode)
                return card;
        }
        return null;
    }
    
    // getFirstCardOfValue(int cardBitCode)
    // returns the first PlayingCard from this deck or hand
    // of the given value is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getFirstCardOfValue(int cardBitCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        cardBitCode=cardBitCode&(1+2+4+8);
        if(cardBitCode==1) cardBitCode=14;
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getBitEncodedValue()==cardBitCode)
                return card;
        }
        return null;
    }
    
    // containsAtLeastOneOfValue(int cardBitCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given value is to be found herein.  
    // a bit code is as described in PlayingCard.java
    public boolean containsAtLeastOneOfValue(int cardBitCode)
    { return getFirstCardOfValue(cardBitCode)!=null;}
    
    // containsAtLeastOneOfSuit(int cardBitCode)
    // returns whether a PlayingCard in this deck or hand
    // of the given suit is to be found herein.  
    // a bit code is as described in PlayingCard.java
    public boolean containsAtLeastOneOfSuit(int cardBitCode)
    { return getFirstCardOfSuit(cardBitCode)!=null;}
    
    // getFirstCardOfSuit(int cardBitCode)
    // returns the first PlayingCard from this deck or hand
    // of the given suit is to be found herein.  DOES NOT REMOVE!
    // removeCard should be called next to do that.
    // a bit code is as described in PlayingCard.java
    public PlayingCard getFirstCardOfSuit(int cardBitCode)
    {
        Vector handContents=getContents();
        if(handContents.size()==0) return null;
        cardBitCode=cardBitCode&(16+32);
        for(int i=0;i<handContents.size();i++)
        {
            PlayingCard card=(PlayingCard)handContents.elementAt(i);
            if(card.getBitEncodedSuit()==cardBitCode)
                return card;
        }
        return null;
    }
    
    // this method is a general event handler
    // to make playing stud-games easier on the players,
    // we capture the LOOK event in this method and
    // then tell the looker what the other player
    // has  "turned up"
    public void executeMsg(Environmental host, CMMsg msg)
    {
        if(owner() instanceof MOB)
        {
            // check to see if this event is a LOOK or EXAMINE event 
            // and see of the target of the action is the owner of
            // this hand of cards, and also check quickly whether
            // the player HAS any cards.
            if(((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
            &&(msg.target()==owner())
            &&(numberOfCards()>0))
            {
                // now we build a string list of the cards which
                // are marked as FACE-UP
                StringBuffer str=new StringBuffer("");
                Vector cards=getContents();
                for(int c=0;c<cards.size();c++)
                {
                    PlayingCard card=(PlayingCard)cards.elementAt(c);
                    if(card.isFaceUp())
                        str.append(card.name()+", ");
                }
                
                // now we have that list, so we generate a new OK (always occurs)
                // message to notify the looker of what our player has.  We add our
                // new message as a "trailer" to ensure it happens after the
                // primary event has taken place.  Remember, at this point, we
                // are capturing an event in mid-stride.  This event we have
                // captured has not completed executing yet.
                if(str.length()>0)
                    msg.addTrailerMsg(new FullMsg(msg.source(),owner(),null,CMMsg.MSG_OK_VISUAL,"<T-NAME> <T-HAS-HAVE> the following cards shown: "+str.toString().substring(0,str.length()-2)+".",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
            }
        }
        super.executeMsg(host,msg);
    }
}