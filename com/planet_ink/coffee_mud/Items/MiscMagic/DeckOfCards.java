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
	public DeckOfCards()
	{
		super();
		name="A deck of cards";
		displayText="A deck of cards has been left here.";
		secretIdentity="A magical deck of cards.  Say \"Shuffle\" to me.";
		recoverEnvStats();
	}

	protected Item makeResource(int ability)
	{
		Item I=CMClass.getItem("PlayingCard");
		I.baseEnvStats().setAbility(ability);
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
			            makeResource(suits[i]+cards[ii]);
			}
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
		        R.show(msg.source(),null,this,CMMsg.MASK_GENERAL|CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> shuffle(s) <O-NAMESELF>.");
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
		    }
		    return false;
		}
		return super.okMessage(myHost,msg);
	}
}