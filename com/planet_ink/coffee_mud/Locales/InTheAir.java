package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;

public class InTheAir extends StdRoom
{
	public InTheAir()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats.setWeight(1);
		name="the sky";
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new InTheAir();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		return isOkAffect(this,affect);
	}
	public static boolean isOkAffect(Room room, Affect affect)
	{
		if(Sense.isSleeping(room)) return true;
		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.amITarget(room)))
		{
			MOB mob=affect.source();
			if((!Sense.isInFlight(mob))
			&&(!Sense.isFalling(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
		}
		
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector needToFall=new Vector();
		Vector mightNeedAdjusting=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(mob!=null)
			{
				Ability A=mob.fetchAffect("Falling");
				if(A!=null)
				{
					if(A.profficiency()>=100)
					{
						foundReversed=true;
						mightNeedAdjusting.addElement(mob);
					}
					foundNormal=foundNormal||(A.profficiency()<=0);
				}
				else
					needToFall.addElement(mob);
			}
		}
		for(int i=0;i<room.numItems();i++)
		{
			Item item=room.fetchItem(i);
			if(item!=null)
			{
				Ability A=item.fetchAffect("Falling");
				if(A!=null)
				{
					if(A.profficiency()>=100)
					{
						foundReversed=true;
						mightNeedAdjusting.addElement(item);
					}
					foundNormal=foundNormal||(A.profficiency()<=0);
				}
				else
					needToFall.addElement(item);
			}
		}
		int avg=((foundReversed)&&(!foundNormal))?100:0;
		for(int i=0;i<mightNeedAdjusting.size();i++)
		{
			Environmental E=(Environmental)mightNeedAdjusting.elementAt(i);
			Ability A=E.fetchAffect("Falling");
			if(A!=null) A.setProfficiency(avg);
		}
		for(int i=0;i<needToFall.size();i++)
		{
			Environmental E=(Environmental)needToFall.elementAt(i);
			if(((E instanceof MOB)&&(!Sense.isInFlight(E)))
			||((E instanceof Item)&&(!Sense.isFlying(((Item)E).ultimateContainer()))))
			{
				Ability falling=CMClass.getAbility("Falling");
				falling.setProfficiency(avg);
				falling.setAffectedOne(room);
				falling.invoke(null,null,E,true);
			}
		}
		return true;
	}
}
