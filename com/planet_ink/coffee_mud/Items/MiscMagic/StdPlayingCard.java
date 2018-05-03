package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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

	The Playing Card
	This item represents a single card in a deck of 52 without wild cards.
	The value of the card is set by changing the basePhyStats().ability()
	value to the numeric representation of the suit and card value from 2-14.
	Methods then exist to parse the ability score into usable values and
	encodings.  The card uses bits 0-3 to represent value 2-14, bits 4,5 to
	represent the suit, and bit 6 to represent whether the card is face-up
	or face-down.

	The card with automatically set its own name and display text based on
	the encoding.
*/
public class StdPlayingCard extends StdItem implements MiscMagic, PlayingCard
{
	@Override
	public String ID()
	{
		return "StdPlayingCard";
	}

	protected int oldAbility=0;

	public StdPlayingCard()
	{
		super();
		name="A card";
		displayText=L("A card lies here.");
		secretIdentity="";
		basePhyStats().setWeight(0);
		setBaseValue(0);
		recoverPhyStats();
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	// the encoded suit
	@Override
	public int getBitEncodedSuit()
	{
		return phyStats().ability() & (16 + 32);
	}

	// the encoded value from 2-14
	@Override
	public int getBitEncodedValue()
	{
		return phyStats().ability() & (1 + 2 + 4 + 8);
	}

	// whether the card is face up
	@Override
	public boolean isFaceUp()
	{
		return (phyStats().ability() & 64) == 64;
	}

	// set the card face up by turning on bit 64
	@Override
	public void turnFaceUp()
	{
		basePhyStats().setAbility(basePhyStats().ability() | 64);
		recoverPhyStats();
	}

	// set the card face down by turning off bits 64 and up.
	@Override
	public void turnFaceDown()
	{
		basePhyStats().setAbility(basePhyStats().ability() & (63));
		recoverPhyStats();
	}

	// return the suit of this card as a single letter string
	@Override
	public String getStringEncodedSuit()
	{
		switch(getBitEncodedSuit())
		{
		case 0:
			return "S";
		case 16:
			return "C";
		case 32:
			return "H";
		case 48:
			return "D";
		}
		return " ";
	}

	// return the value of this card as a short string
	// face cards are only a single letter
	@Override
	public String getStringEncodedValue()
	{
		switch(getBitEncodedValue())
		{
		case 1:
		case 14:
			return "A";
		case 11:
			return "J";
		case 12:
			return "Q";
		case 13:
			return "K";
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
				return ""+getBitEncodedValue();
		}
		return "0";
	}

	// return the english-word representation of the value
	// passed to this method.  Since this method is static,
	// it may be called as a utility function and does not
	// necessarily represent THIS card object.
	@Override
	public String getCardValueLongDescription(int value)
	{
		value=value&(1+2+4+8);
		switch(value)
		{
		case 1:
			return "ace";
		case 2:
			return "two";
		case 3:
			return "three";
		case 4:
			return "four";
		case 5:
			return "five";
		case 6:
			return "six";
		case 7:
			return "seven";
		case 8:
			return "eight";
		case 9:
			return "nine";
		case 10:
			return "ten";
		case 11:
			return "jack";
		case 12:
			return "queen";
		case 13:
			return "king";
		case 14:
			return "ace";
		}
		return "Unknown";
	}

	// return partial english-word representation of the value
	// passed to this method.  By partial I mean numeric for
	// number cards and words otherwise. Since this method is static,
	// it may be called as a utility function and does not
	// necessarily represent THIS card object.
	@Override
	public String getCardValueShortDescription(int value)
	{
		value=value&(1+2+4+8);
		switch(value)
		{
		case 1:
			return "ace";
		case 11:
			return "jack";
		case 12:
			return "queen";
		case 13:
			return "king";
		case 14:
			return "ace";
		default:
			return ""+value;
		}
	}

	// return an english-word, color-coded representation
	// of the suit passed to this method. Since this method is static,
	// it may be called as a utility function and does not
	// necessarily represent THIS card object.
	@Override
	public String getSuitDescription(int suit)
	{
		suit=suit&(16+32);
		switch(suit)
		{
		case 0:
			return "^pspades^?";
		case 16:
			return "^pclubs^p";
		case 32:
			return "^rhearts^?";
		case 48:
			return "^rdiamonds^?";
		}
		return "";
	}

	// recoverPhyStats() is a kind of event handler
	// that is called whenever something changes in
	// the environment of this object.  This method
	// normally causes the object to reevaluate its
	// state.
	// In this case, we compare the current card
	// value with a cached and saved one to determine
	// if the NAME and DISPLAY TEXT of the card should
	// be updated.
	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(oldAbility!=phyStats.ability())
		{
			oldAbility=phyStats().ability();
			final String suitStr=getSuitDescription(phyStats().ability());
			final String cardStr=getCardValueShortDescription(phyStats().ability());
			if((suitStr.length()==0)||(cardStr.length()==0))
			{
				name="A mangled card";
				displayText=L("A mangled playing card lies here.");
			}
			else
			{
				name="the "+cardStr+" of "+suitStr;
				displayText=L("a playing card, @x1, lies here",name);
			}
			//CMLib.flags().setGettable(this,false);
		}
	}
}
