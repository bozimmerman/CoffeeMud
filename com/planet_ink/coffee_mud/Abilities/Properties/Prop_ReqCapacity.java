package com.planet_ink.coffee_mud.Abilities.Properties;
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


import java.util.*;

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
public class Prop_ReqCapacity extends Property
{
	public String ID() { return "Prop_ReqCapacity"; }
	public String name(){ return "Capacity Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	public int peopleCap=Integer.MAX_VALUE;
    public int playerCap=Integer.MAX_VALUE;
    public int mobCap=Integer.MAX_VALUE;
	public int itemCap=Integer.MAX_VALUE;
	public int maxWeight=Integer.MAX_VALUE;
	public boolean indoorOnly=false;

	public String accountForYourself()
	{
		return "Person limit: "+((peopleCap==Integer.MAX_VALUE)?"None":(""+peopleCap))
          +"\n\rPlayer limit: "+((playerCap==Integer.MAX_VALUE)?"None":(""+playerCap))
          +"\n\rMOB limit   : "+((mobCap==Integer.MAX_VALUE)?"None":(""+mobCap))
		  +"\n\rItem limit  : "+((itemCap==Integer.MAX_VALUE)?"None":(""+itemCap))
		  +"\n\rWeight limit: "+((maxWeight==Integer.MAX_VALUE)?"None":(""+maxWeight));
	}

	public void setMiscText(String txt)
	{
		super.setMiscText(txt);
		peopleCap=Integer.MAX_VALUE;
        playerCap=Integer.MAX_VALUE;
        mobCap=Integer.MAX_VALUE;
		itemCap=Integer.MAX_VALUE;
		maxWeight=Integer.MAX_VALUE;
		indoorOnly=false;
		if(txt.length()==0)
			peopleCap=2;
		else
		if(CMath.isNumber(txt))
			peopleCap=CMath.s_int(txt);
		else
		{
			peopleCap=CMParms.getParmInt(txt,"people",peopleCap);
            playerCap=CMParms.getParmInt(txt,"players",playerCap);
            mobCap=CMParms.getParmInt(txt,"mobs",mobCap);
			itemCap=CMParms.getParmInt(txt,"items",itemCap);
			maxWeight=CMParms.getParmInt(txt,"weight",maxWeight);
			indoorOnly=CMParms.getParmBool(txt,"indoor",indoorOnly);
		}
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected!=null)
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_ENTER:
			if((msg.target() instanceof Room)
			&&(peopleCap<Integer.MAX_VALUE)
			&&((!indoorOnly)||((((Room)msg.target()).domainType()&Room.INDOORS)==Room.INDOORS))
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				if(((Room)msg.target()).numInhabitants()>=peopleCap)
				{
					msg.source().tell("No more people can fit in there.");
					return false;
				}
                if(((Room)msg.target()).numPCInhabitants()>=playerCap)
                {
                    msg.source().tell("No more players can fit in there.");
                    return false;
                }
                if(msg.source().isMonster() 
                && (((Room)msg.target()).numInhabitants()-((Room)msg.target()).numPCInhabitants())>=mobCap)
                {
                    msg.source().tell("No more MOBs can fit in there.");
                    return false;
                }
			}
			break;
		case CMMsg.TYP_DROP:
			if((msg.target() instanceof Item)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null)
	        &&((msg.targetMessage()==null)||(!msg.targetMessage().equalsIgnoreCase("GIVE"))))
			{
                Room R=null;
                if(affected instanceof Room)
                    R=(Room)affected;
                else
                if(myHost instanceof Room)
                    R=(Room)myHost;
                else
                    R=msg.source().location();
				if((!indoorOnly)||((R.domainType()&Room.INDOORS)==Room.INDOORS))
				{
					if(itemCap<Integer.MAX_VALUE)
					{
						int soFar=0;
                        int rawResources=0;
						for(int i=0;i<R.numItems();i++)
						{
                            Item I=R.fetchItem(i); 
                            if(I instanceof RawMaterial) 
                                rawResources++;
                            if((I!=null)&&(I.container()==null)) 
                                soFar++;
                        }
						if(soFar>=itemCap)
						{
							msg.source().tell("There is no more room in here to drop "+msg.target().Name()+".");
                            if((rawResources>0)&&(CMath.div(rawResources,itemCap)>0.5))
                                msg.source().tell("You should consider bundling up some of those resources.");
							return false;
						}
					}
					if(maxWeight<Integer.MAX_VALUE)
					{
						int soFar=0;
						for(int i=0;i<R.numItems();i++)
						{Item I=R.fetchItem(i); if(I!=null) soFar+=I.envStats().weight();}
						if((soFar+msg.target().envStats().weight())>=maxWeight)
						{
							msg.source().tell("There is no room in here to put "+msg.target().Name()+".");
							return false;
						}
					}
				}
			}
			break;
		}
		return super.okMessage(myHost,msg);
	}
}
