package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Auction extends Channel
{
	public Auction(){}
	protected Ability auctionA=null;

	private String[] access={getScr("Auction","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		int channelInt=ChannelSet.getChannelIndex(getScr("Auction","cmd"));
		int channelNum=ChannelSet.getChannelCodeNumber(getScr("Auction","cmd"));

		if(Util.isSet(pstats.getChannelMask(),channelInt))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(getScr("Auction","turnon"));
		}

		if((commands.size()>1)
		&&(auctionA!=null)
		&&(auctionA.invoker()==mob))
		{
			if(((String)commands.elementAt(1)).equalsIgnoreCase(getScr("Auction","channel")))
			{
				commands.removeElementAt(1);
				super.execute(mob,commands);
				return false;
			}
			else
			if(((String)commands.elementAt(1)).equalsIgnoreCase(getScr("Auction","close")))
			{
				commands.removeElementAt(1);
				Vector V=new Vector();
				V.addElement(getScr("Auction","cmd"));
				V.addElement(getScr("Auction","closed"));
				CMClass.ThreadEngine().deleteTick(auctionA,MudHost.TICK_QUEST);
				auctionA=null;
				super.execute(mob,V);
				return false;
			}
		}
		if(auctionA==null)
		{
			if(commands.size()==1)
			{
				mob.tell(getScr("Auction","nothing"));
				return false;
			}
			Vector V=new Vector();
			if((commands.size()>2)
			&&((EnglishParser.numPossibleGold(mob,(String)commands.lastElement())>0)||(((String)commands.lastElement()).equals("0"))))
			{
				V.addElement(commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			else
				V.addElement("0");

			String s=Util.combine(commands,1);
			Environmental E=mob.fetchInventory(null,s);
			if((E==null)||(E instanceof MOB))
			{
				mob.tell(getScr("Auction","erritem",s));
				return false;
			}
			if((mob.isMonster())
            ||(!mob.session().confirm(getScr("Auction","confirmed",E.name(),((String)V.firstElement())),getScr("Auction","yes"))))
				return false;
			auctionA=CMClass.getAbility("Prop_Auction");
			auctionA.invoke(mob,V,E,false,0);
            auctionA.setInvoker(mob);
		}
		else
		{
			commands.removeElementAt(0);
			auctionA.invoke(mob,commands,null,false,0);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
