package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class PlayingCard extends StdItem implements MiscMagic
{
	public String ID(){	return "PlayingCard";}
	private int oldAbility=0;
	
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

	public void recoverEnvStats()
	{
	    super.recoverEnvStats();
	    if(oldAbility!=envStats.ability())
	    {
	        oldAbility=envStats().ability();
	        int suit=envStats.ability()&(16+32);
	        int card=envStats.ability()&(1+2+4+8);
	        String suitStr=null;
	        switch(suit)
	        {
        	case 0: suitStr="spades"; break;
        	case 16: suitStr="clubs"; break;
        	case 32: suitStr="hearts"; break;
        	case 48: suitStr="diamonds"; break;
	        }
	        String cardStr=null;
	        switch(card)
	        {
	        	case 1: cardStr="ace"; break;
	        	case 11: cardStr="jack"; break;
	        	case 12: cardStr="queen"; break;
	        	case 13: cardStr="king"; break;
	        	case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:
	        	    cardStr=""+card; break;
	        }
	        if((suitStr==null)||(cardStr==null))
	        {
	    		name="A mangled card";
	    		displayText="A mangled playing card lies here.";
	        }
	        else
	        {
	            name="the "+cardStr+" of "+suitStr;
	            displayText="a playing card, "+name+", lies here";
	        }
	    }
	}
}
