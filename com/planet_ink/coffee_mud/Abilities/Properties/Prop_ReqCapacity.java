package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2004-2014 Bo Zimmerman

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
public class Prop_ReqCapacity extends Property implements TriggeredAffect
{
	@Override public String ID() { return "Prop_ReqCapacity"; }
	@Override public String name(){ return "Capacity Limitations";}
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	public int peopleCap=Integer.MAX_VALUE;
	public int playerCap=Integer.MAX_VALUE;
	public int mobCap=Integer.MAX_VALUE;
	public int itemCap=Integer.MAX_VALUE;
	public int maxWeight=Integer.MAX_VALUE;
	public boolean indoorOnly=false;
	public boolean containersOk=false;


	@Override public long flags(){return Ability.FLAG_ZAPPER;}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public String accountForYourself()
	{
		return "Person limit: "+((peopleCap==Integer.MAX_VALUE)?"None":(""+peopleCap))
		  +"\n\rPlayer limit: "+((playerCap==Integer.MAX_VALUE)?"None":(""+playerCap))
		  +"\n\rMOB limit   : "+((mobCap==Integer.MAX_VALUE)?"None":(""+mobCap))
		  +"\n\rItem limit  : "+((itemCap==Integer.MAX_VALUE)?"None":(""+itemCap))
		  +"\n\rWeight limit: "+((maxWeight==Integer.MAX_VALUE)?"None":(""+maxWeight));
	}

	@Override
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
			containersOk=CMParms.getParmBool(txt,"droponly",containersOk)||CMParms.getParmBool(txt,"containersok",containersOk);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
					msg.source().tell(L("No more people can fit in there."));
					return false;
				}
				if(((Room)msg.target()).numPCInhabitants()>=playerCap)
				{
					msg.source().tell(L("No more players can fit in there."));
					return false;
				}
				if(msg.source().isMonster()
				&& (((Room)msg.target()).numInhabitants()-((Room)msg.target()).numPCInhabitants())>=mobCap)
				{
					msg.source().tell(L("No more MOBs can fit in there."));
					return false;
				}
			}
			break;
		case CMMsg.TYP_DROP:
			if((msg.target() instanceof Item)
			&&(msg.source().location()!=null)
			&&((!msg.targetMajor(CMMsg.MASK_INTERMSG))||(!containersOk))) // intermsgs are PUTs on the ground
			{
				final Item targetI=(Item)msg.target();
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
							final Item I=R.getItem(i);
							if(I instanceof RawMaterial)
								rawResources++;
							if((I!=null)&&(I.container()==null))
								soFar++;
						}
						if(soFar>=itemCap)
						{
							msg.source().tell(L("There is no more room in here to drop @x1.",msg.target().Name()));
							if((rawResources>0)&&(CMath.div(rawResources,itemCap)>0.5))
								msg.source().tell(L("You should consider bundling up some of those resources."));
							return false;
						}
					}
					if(maxWeight<Integer.MAX_VALUE)
					{
						int soFar=0;
						for(int i=0;i<R.numItems();i++)
						{final Item I=R.getItem(i); if(I!=null) soFar+=I.phyStats().weight();}
						if((soFar+targetI.phyStats().weight())>=maxWeight)
						{
							msg.source().tell(L("There is no room in here to put @x1.",targetI.Name()));
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
