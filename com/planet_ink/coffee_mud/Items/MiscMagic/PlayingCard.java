package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
   
   The Playing Card
   This item represents a single card in a deck of 52 without wild cards.
   The value of the card is set by changing the baseEnvStats().ability()
   value to the numeric representation of the suit and card value from 2-14.
   Methods then exist to parse the ability score into usable values and 
   encodings.  The card uses bits 0-3 to represent value 2-14, bits 4,5 to
   represent the suit, and bit 6 to represent whether the card is face-up
   or face-down.
   
   The card with automatically set its own name and display text based on
   the encoding.  The card has no weight, but is ungettable to prevent
   cheating.
*/
public class PlayingCard extends StdItem implements MiscMagic
{
	public String ID(){	return "PlayingCard";}
	private int oldAbility=0;
    
    public static int[] suits={0,16,32,48};
    public static int[] cards={2,3,4,5,6,7,8,9,10,11,12,13,14};
    
	public PlayingCard()
	{
		super();
		name="A card";
		displayText="A card lies here.";
		secretIdentity="";
		baseEnvStats().setWeight(0);
		setBaseValue(0);
		recoverEnvStats();
	}

    // the encoded suit
    public int getBitEncodedSuit(){return envStats().ability()&(16+32);}
    // the encoded value from 2-14
    public int getBitEncodedValue(){return envStats().ability()&(1+2+4+8);}
    // whether the card is face up
    public boolean isFaceUp(){return (envStats().ability()&64)==64;}
    // set the card face up by turning on bit 64
    public void turnFaceUp(){ baseEnvStats().setAbility(baseEnvStats().ability()|64); recoverEnvStats();}
    // set the card face down by turning off bits 64 and up.
    public void turnFaceDown(){ baseEnvStats().setAbility(baseEnvStats().ability()&(63)); recoverEnvStats();}

    // return the suit of this card as a single letter string
    public String getStringEncodedSuit()
    {
        switch(getBitEncodedSuit())
        {
        case 0: return "S";
        case 16: return "C";
        case 32: return "H";
        case 48: return "D";
        }
        return " ";
    }
    
    // return the value of this card as a short string
    // face cards are only a single letter
    public String getStringEncodedValue()
    {
        switch(getBitEncodedValue())
        {
            case 1: case 14: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:
                return ""+getBitEncodedValue();
        }
        return "0";
    }
    
    // return the english-word representation of the value
    // passed to this method.  Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public static String getCardValueLongDescription(int value)
    {
        value=value&(1+2+4+8);
        switch(value)
        {
        case 1: return "ace";
        case 2: return "two";
        case 3: return "three";
        case 4: return "four";
        case 5: return "five";
        case 6: return "six";
        case 7: return "seven";
        case 8: return "eight";
        case 9: return "nine";
        case 10: return "ten";
        case 11: return "jack";
        case 12: return "queen";
        case 13: return "king";
        case 14: return "ace";
        }
        return "Unknown";
    }
    
    // return partial english-word representation of the value
    // passed to this method.  By partial I mean numeric for 
    // number cards and words otherwise. Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public static String getCardValueShortDescription(int value)
    {
        value=value&(1+2+4+8);
        switch(value)
        {
        case 1: return "ace";
        case 11: return "jack";
        case 12: return "queen";
        case 13: return "king";
        case 14: return "ace";
        default:
            return ""+value;
        }
    }

    // return an english-word, color-coded representation
    // of the suit passed to this method. Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public static String getSuitDescription(int suit)
    {
        suit=suit&(16+32);
        switch(suit)
        {
        case 0: return "^pspades^?";
        case 16: return "^pclubs^p";
        case 32: return "^rhearts^?";
        case 48: return "^rdiamonds^?";
        }
        return "";
    }
    
    // recoverEnvStats() is a kind of event handler
    // that is called whenever something changes in
    // the environment of this object.  This method
    // normally causes the object to reevaluate its
    // state.
    // In this case, we compare the current card
    // value with a cached and saved one to determine
    // if the NAME and DISPLAY TEXT of the card should
    // be updated.
	public void recoverEnvStats()
	{
	    super.recoverEnvStats();
	    if(oldAbility!=envStats.ability())
	    {
	        oldAbility=envStats().ability();
	        String suitStr=getSuitDescription(envStats().ability());
	        String cardStr=getCardValueShortDescription(envStats().ability());
	        if((suitStr.length()==0)||(cardStr.length()==0))
	        {
	    		name="A mangled card";
	    		displayText="A mangled playing card lies here.";
	        }
	        else
	        {
	            name="the "+cardStr+" of "+suitStr;
	            displayText="a playing card, "+name+", lies here";
	        }
            //Sense.setGettable(this,false);
	    }
	}
}
