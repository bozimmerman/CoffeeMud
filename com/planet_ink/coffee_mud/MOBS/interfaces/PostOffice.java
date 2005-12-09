package com.planet_ink.coffee_mud.core.interfaces;
import java.util.*;

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
public interface PostOffice extends ShopKeeper
{
    public final static int DATA_USERID=0;
    public final static int DATA_CHAIN=1;
    public final static int DATA_KEY=2;
    public final static int DATA_DATA=3;
    
    public final static int PIECE_FROM=0;
    public final static int PIECE_TO=1;
    public final static int PIECE_TIME=2;
    public final static int PIECE_COD=3;
    public final static int PIECE_CLASSID=4;
    public final static int PIECE_MISCDATA=5;
    public final static int NUM_PIECES=6;
    
    public void addToBox(String boxName, Item thisThang, String from, String to, long holdTime, double COD);
    public void addToBox(MOB mob, Item thisThang, String from, String to, long holdTime, double COD);
    public boolean delFromBox(String mob, Item thisThang);
    public boolean delFromBox(MOB mob, Item thisThang);
    public void emptyBox(String mob);
    public Hashtable getOurOpenBoxes(String mob);
    public void createBoxHere(String mob, String forward);
    public void deleteBoxHere(String mob);
    public Vector getAllLocalBoxVectors(String mob);
    public Item findBoxContents(String mob, String likeThis);
    public Item findBoxContents(MOB mob, String likeThis);
    public String postalChain();
    public void setPostalChain(String name);
    public String postalBranch(); // based on individual shopkeeper
    public String findProperBranch(String name);
    public Vector parsePostalItemData(String data);
    
    public double minimumPostage();
    public void setMinimumPostage(double d);
    public double postagePerPound();
    public void setPostagePerPound(double d);
    public double holdFeePerPound();
    public void setHoldFeePerPound(double d);
    public double feeForNewBox();
    public void setFeeForNewBox(double d);
    public int maxMudMonthsHeld();
    public void setMaxMudMonthsHeld(int months);
}