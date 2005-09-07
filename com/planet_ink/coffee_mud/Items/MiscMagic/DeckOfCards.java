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
public class DeckOfCards extends StdContainer implements MiscMagic
{
	public String ID(){	return "DeckOfCards";}
	boolean alreadyFilled=false;
    private Vector cardsCache=null;
    private Vector hands=new Vector();
    
	public DeckOfCards()
	{
		super();
		name="A deck of cards";
		displayText="A deck of cards has been left here.";
		secretIdentity="A magical deck of cards.  Say \"Shuffle\" to me.";
		recoverEnvStats();
	}

	protected Item makePlayingCard(int abilityCode)
	{
		Item I=CMClass.getItem("PlayingCard");
		I.baseEnvStats().setAbility(abilityCode);
		I.recoverEnvStats();
		I.setContainer(this);
		if(owner() instanceof Room)
			((Room)owner()).addItem(I);
		else
		if(owner() instanceof MOB)
			((MOB)owner()).addInventory(I);
		return I;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((!alreadyFilled)&&(owner()!=null))
		{
			alreadyFilled=true;
			if(getContents().size()==0)
			{
			    int[] suits={0,16,32,48};
			    int[] cards={1,2,3,4,5,6,7,8,9,10,11,12,13};
			    for(int i=0;i<suits.length;i++)
			        for(int ii=0;ii<cards.length;ii++)
                        makePlayingCard(suits[i]+cards[ii]);
			}
            cardsCache=getContents();
            hands=new Vector();
		}
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().toUpperCase().indexOf("SHUFFLE")>0))
		{
		    Room R=CoffeeUtensils.roomLocation(this);
	        Vector V=getContents();
	        Environmental own=owner();
	        if(V.size()==0)
	            msg.source().tell("There are no cards left in the deck");
	        else
		    if(R!=null)
		    {
		        for(int i=0;i<V.size()*5;i++)
		        {
		            Item I=(Item)V.elementAt(Dice.roll(1,V.size(),-1));
		            I.removeFromOwnerContainer();
		            I.setContainer(this);
		            if(own instanceof MOB)
		                ((MOB)own).addInventory(I);
		            else
		            if(own instanceof Room)
		                R.addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
		        }
                R.show(msg.source(),null,this,CMMsg.MASK_GENERAL|CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-HAS-HAVE> thoroughly shuffled <O-NAMESELF>.");
		    }
		    return false;
		}
		return super.okMessage(myHost,msg);
	}
    
    public static DeckOfCards createDeck(Environmental owner)
    {
        DeckOfCards deck=new DeckOfCards();
        if(owner==null)
            owner=CMClass.getLocale("StdRoom");
        if(owner instanceof MOB)
            ((MOB)owner).giveItem(deck);
        else
        if(owner instanceof Room)
            ((Room)owner).bringItemHere(deck,Item.REFUSE_PLAYER_DROP);
        else
            return null;
        Room R=CoffeeUtensils.roomLocation(deck.owner());
        if(R==null) return null;
        FullMsg msg=new FullMsg(CMMap.god(R),deck,null,CMMsg.MSG_SPEAK," DONOTHING ",CMMsg.MSG_SPEAK," DONOTHING ",CMMsg.MSG_SPEAK," DONOTHING ");
        deck.okMessage(CMMap.god(R),msg);
        return deck;
    }
    
    public boolean shuffleDeck()
    {
        Vector V=getContents();
        Environmental own=owner();
        if(V.size()==0)
            return false;
        for(int i=0;i<V.size()*5;i++)
        {
            Item I=(Item)V.elementAt(Dice.roll(1,V.size(),-1));
            I.removeFromOwnerContainer();
            I.setContainer(this);
            if(own instanceof MOB)
                ((MOB)own).addInventory(I);
            else
            if(own instanceof Room)
                ((Room)own).addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
        }
        return true;
    }
    
    public Item topCardFromDeck()
    {
        Vector deckContents=getContents();
        while(deckContents.size()>0)
        {
            Item card=(Item)deckContents.firstElement();
            if(cardsCache.contains(card))
            {
                card.removeFromOwnerContainer();
                return card;
            }
            card.destroy();
            deckContents.removeElement(card);
        }
        return null;
    }
    
    public boolean returnCardToDeck(Item card)
    {
        if(card==null) return false;
        if(!cardsCache.contains(card)) return false;
        Item handToDestroy=null;
        try{
            for(int h=0;h<hands.size();h++)
            {
                Container hand=(Container)hands.elementAt(h);
                if((hand.owner()==card.owner())
                &&(hand.getContents().contains(card))
                &&(hand.getContents().size()==1))
                {
                    handToDestroy=hand;
                    break;
                }
            }
        }catch(ArrayIndexOutOfBoundsException e){}
        if(owner() instanceof MOB)
            ((MOB)owner()).giveItem(card);
        else
        if(owner() instanceof Room)
            ((Room)owner()).bringItemHere(card,Item.REFUSE_PLAYER_DROP);
        else
            return false;
        card.setContainer(this);
        if(handToDestroy!=null)
            handToDestroy.destroy();
        if(owner() instanceof MOB)
            return ((MOB)owner()).isMine(card);
        else
        if(owner() instanceof Room)
            return ((Room)owner()).isContent(card);
        return true;
    }
    
    public int numberOfCardsInTheDeck()
    {
        Vector deckContents=getContents();
        return deckContents.size();
    }
    
    public boolean resetDeckBackTo52Cards()
    {
        if((cardsCache==null)||(cardsCache.size()==0))
            return false;
        for(int i=0;i<cardsCache.size();i++)
        {
            Item card=(Item)cardsCache.elementAt(i);
            if(card.owner()!=owner())
                returnCardToDeck(card);
        }
        return numberOfCardsInTheDeck()==52;
    }
    
    public Vector returnDeckContents()
    {
        return getContents();
    }

    private String convertAbilityCodeToCardCode(int abilityCode)
    {
        int suit=abilityCode&(16+32);
        int card=abilityCode&(1+2+4+8);
        String suitStr=" ";
        switch(suit)
        {
        case 0: suitStr="S"; break;
        case 16: suitStr="C"; break;
        case 32: suitStr="H"; break;
        case 48: suitStr="D"; break;
        }
        String cardStr=" ";
        switch(card)
        {
            case 1: cardStr="A"; break;
            case 11: cardStr="J"; break;
            case 12: cardStr="Q"; break;
            case 13: cardStr="K"; break;
            case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:
                cardStr=""+card; break;
        }
        return suitStr+cardStr;
    }
    
    public String[] returnDeckContentsEncoded()
    {
        Vector contents=getContents();
        String[] encodedDeck=new String[contents.size()];
        for(int i=0;i<contents.size();i++)
        {
            Item card=(Item)contents.elementAt(i);
            encodedDeck[i]=convertAbilityCodeToCardCode(card.envStats().ability());
        }
        return encodedDeck;
    }
    
    
    private Item getPlayersHand(MOB player)
    {
        if((player==null)||(hands==null))
            return null;
        try{
            for(int i=0;i<hands.size();i++)
                if(((Item)hands.elementAt(i)).owner()==player)
                    return (Item)hands.elementAt(i);
        }catch(ArrayIndexOutOfBoundsException e){}
        return null;
    }
    
    private Item addEmptyHand(MOB player)
    {
        // calling this method without the intention
        // of putting a card inside is counter-productive.
        // the other methods should automatically create and
        // destroy the hands as cards are dealt and returned
        // to the deck respectively!
        Item hand=getPlayersHand(player);
        if(hand!=null) return hand;
        hand=CMClass.getItem("GenContainer");
        hand.setName("Your hand");
        hand.setDisplayText("Somehow a hand of cards has fallen, and can't get up!");
        hand.setDescription("");
        Sense.setGettable(hand,false);
        Sense.setDroppable(hand,false);
        Sense.setRemovable(hand,false);
        hand.baseEnvStats().setWeight(1);
        hand.recoverEnvStats();
        ((Container)hand).setCapacity(0);
        ((Container)hand).setContainTypes(Container.CONTAIN_SSCOMPONENTS);
        player.giveItem(hand);
        if(player.isMine(hand))
        {
            hands.add(hand);
            return hand;
        }
        return null;
    }

    
    
    public boolean addCardToPlayersHand(MOB player, Item card)
    {
        if((player==null)||(card==null)) return false;
        if(!cardsCache.contains(card)) return false;
        Item hand=addEmptyHand(player);
        if(hand==null) return false;
        player.giveItem(card);
        card.setContainer(hand);
        return player.isMine(card);
    }
    
    public int numberOfCardsInPlayersHand(MOB player)
    {
        Item I=getPlayersHand(player);
        if(I instanceof Container) 
            return ((Container)I).getContents().size();
        return 0;
    }
    
    public boolean removeCardFromHand(MOB player, Item card)
    {
        if((player==null)||(card==null)) return false;
        if(!cardsCache.contains(card)) return false;
        Item hand=getPlayersHand(player);
        Item handToDestroy=null;
        if((hand!=null)
        &&(((Container)hand).getContents().contains(card))
        &&(((Container)hand).getContents().size()==1))
            handToDestroy=hand;
        card.removeFromOwnerContainer();
        card.setContainer(null);
        if(handToDestroy!=null)
            handToDestroy.destroy();
        return true;
    }
    
    private int translateCardCodeToSuitAbilityCode(String cardCode)
    {
        if((cardCode==null)||(cardCode.length()==0)) return -1;
        switch(cardCode.toUpperCase().charAt(0))
        {
        case 'S': return 0;
        case 'C': return 16;
        case 'H': return 32;
        case 'D': return 48;
        }
        return -1;
    }
    
    private int translateCardCodeToValueAbilityCode(String cardCode)
    {
        if((cardCode==null)||(cardCode.length()<2)) return -1;
        switch(cardCode.toUpperCase().charAt(1))
        {
        case 'K': return 13;
        case 'J': return 11;
        case 'Q': return 12;
        case 'A': return 1;
        default:
            if(Util.isInteger(""+cardCode.charAt(1)))
                return Util.s_int(""+cardCode.charAt(1));
            break;
        }
        return -1;
    }
    
    public boolean removeCardFromHand(MOB player, String cardCode)
    {
        Item hand=getPlayersHand(player);
        if(hand==null) return false;
        Vector handContents=((Container)hand).getContents();
        if(handContents.size()==0) return false;
        int suitCode=translateCardCodeToSuitAbilityCode(cardCode);
        int valueCode=translateCardCodeToValueAbilityCode(cardCode);
        if((suitCode<0)||(valueCode<0)) return false;
        int totalAbilityCode=suitCode+valueCode;
        for(int i=0;i<handContents.size();i++)
        {
            Item card=(Item)handContents.elementAt(i);
            if(card.envStats().ability()==totalAbilityCode)
                return removeCardFromHand(player,card);
        }
        return false;
    }

    
    public boolean doesHandContainAtLeastOneOfValue(MOB player, String cardCode)
    {
        Item hand=getPlayersHand(player);
        if(hand==null) return false;
        Vector handContents=((Container)hand).getContents();
        if(handContents.size()==0) return false;
        if(cardCode.length()==0) return false;
        if(cardCode.length()==1) cardCode=" "+cardCode;
        int valueCode=translateCardCodeToValueAbilityCode(cardCode);
        if(valueCode<0) return false;
        for(int i=0;i<handContents.size();i++)
        {
            Item card=(Item)handContents.elementAt(i);
            if((card.envStats().ability()&(1+2+4+8))==valueCode)
                return true;
        }
        return false;
    }
    
    public boolean doesHandContainAtLeastOneOfSuit(MOB player, String cardCode)
    {
        Item hand=getPlayersHand(player);
        if(hand==null) return false;
        Vector handContents=((Container)hand).getContents();
        if(handContents.size()==0) return false;
        if(cardCode.length()==0) return false;
        if(cardCode.length()==1) cardCode=cardCode+" ";
        int suitCode=translateCardCodeToSuitAbilityCode(cardCode);
        if(suitCode<0) return false;
        for(int i=0;i<handContents.size();i++)
        {
            Item card=(Item)handContents.elementAt(i);
            if((card.envStats().ability()&(16+32))==suitCode)
                return true;
        }
        return false;
    }
    
    public String[] returnHandContentsEncoded(MOB player)
    {
        Item hand=getPlayersHand(player);
        if(hand==null) return new String[0];
        Vector contents=((Container)hand).getContents();
        if(contents.size()==0) return new String[0];
        String[] encodedHand=new String[contents.size()];
        for(int i=0;i<contents.size();i++)
        {
            Item card=(Item)contents.elementAt(i);
            encodedHand[i]=convertAbilityCodeToCardCode(card.envStats().ability());
        }
        return encodedHand;
    }
}