package com.planet_ink.coffee_mud.MOBS.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Hashtable;
import java.util.Vector;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface PostOffice extends ShopKeeper
{
    public void addToBox(String boxName, Item thisThang, String from, String to, long holdTime, double COD);
    public void addToBox(MOB mob, Item thisThang, String from, String to, long holdTime, double COD);
    public boolean delFromBox(String mob, Item thisThang);
    public boolean delFromBox(MOB mob, Item thisThang);
    public void emptyBox(String mob);
    public Hashtable getOurOpenBoxes(String mob);
    public void createBoxHere(String mob, String forward);
    public void deleteBoxHere(String mob);
    public MailPiece parsePostalItemData(String data);
    public Item findBoxContents(String mob, String likeThis);
    public Item findBoxContents(MOB mob, String likeThis);
    public String postalChain();
    public void setPostalChain(String name);
    public String postalBranch(); // based on individual shopkeeper
    public String findProperBranch(String name);
    
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
    
    public static class MailPiece
    {
    	public String from="";
    	public String to="";
    	public String time="";
    	public String cod="";
    	public String classID="";
    	public String xml="";
    }

}