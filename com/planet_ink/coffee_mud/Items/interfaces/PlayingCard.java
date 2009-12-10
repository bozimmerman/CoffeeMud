package com.planet_ink.coffee_mud.Items.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;

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
public interface PlayingCard extends Item
{
    public static int[] suits={0,16,32,48};
    public static int[] cards={2,3,4,5,6,7,8,9,10,11,12,13,14};
    public int getBitEncodedSuit();
    // the encoded value from 2-14
    public int getBitEncodedValue();
    // whether the card is face up
    public boolean isFaceUp();
    // set the card face up by turning on bit 64
    public void turnFaceUp();
    // set the card face down by turning off bits 64 and up.
    public void turnFaceDown();
    // return the suit of this card as a single letter string
    public String getStringEncodedSuit();
    // return the value of this card as a short string
    // face cards are only a single letter
    public String getStringEncodedValue();
    
    // return the english-word representation of the value
    // passed to this method.  Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public String getCardValueLongDescription(int value);
    // return partial english-word representation of the value
    // passed to this method.  By partial I mean numeric for 
    // number cards and words otherwise. Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public String getCardValueShortDescription(int value);
    // return an english-word, color-coded representation
    // of the suit passed to this method. Since this method is static,
    // it may be called as a utility function and does not
    // necessarily represent THIS card object.
    public String getSuitDescription(int suit);
}
