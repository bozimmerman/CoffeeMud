package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqCapacity extends Property
{
	public String ID() { return "Prop_ReqCapacity"; }
	public String name(){ return "Capacity Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqCapacity newOne=new Prop_ReqCapacity();	newOne.setMiscText(text());	return newOne;}
	
	public int peopleCap=Integer.MAX_VALUE;
	public int itemCap=Integer.MAX_VALUE;
	public int maxWeight=Integer.MAX_VALUE;

	public String accountForYourself()
	{ 
		return "Person limit: "+((peopleCap==Integer.MAX_VALUE)?"None":(""+peopleCap))
		  +"\n\rItem limit  : "+((itemCap==Integer.MAX_VALUE)?"None":(""+itemCap))
		  +"\n\rWeight limit: "+((maxWeight==Integer.MAX_VALUE)?"None":(""+maxWeight));	
	}

	public void setMiscText(String txt)
	{
		super.setMiscText(txt);
		peopleCap=Integer.MAX_VALUE;
		itemCap=Integer.MAX_VALUE;
		maxWeight=Integer.MAX_VALUE;
		if(txt.length()==0)
			peopleCap=2;
		else
		if(Util.isNumber(txt))
			peopleCap=Util.s_int(txt);
		else
		{
			peopleCap=Util.getParmInt(txt,"people",peopleCap);
			itemCap=Util.getParmInt(txt,"items",itemCap);
			maxWeight=Util.getParmInt(txt,"weight",maxWeight);
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
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				if(((Room)msg.target()).numInhabitants()>=peopleCap)
				{
					msg.source().tell("No more people can fit in there.");
					return false;
				}
			}
			break;
		case CMMsg.TYP_DROP:
			if((msg.target() instanceof Item)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null))
			{
				Room R=msg.source().location();
				if(itemCap<Integer.MAX_VALUE)
				{
					int soFar=0;
					for(int i=0;i<R.numItems();i++)
					{Item I=R.fetchItem(i); if((I!=null)&&(I.container()==null)) soFar++;}
					if(soFar>=itemCap)
					{
						msg.source().tell("There is no more room in here to drop "+msg.target().Name()+".");
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
			break;
		}
		return super.okMessage(myHost,msg);
	}
}
