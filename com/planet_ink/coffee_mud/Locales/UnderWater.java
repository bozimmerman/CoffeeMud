package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;

import java.util.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
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
public class UnderWater extends StdRoom implements Drink
{
	public String ID(){return "UnderWater";}
	public UnderWater()
	{
		super();
		name="the water";
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}



	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}

	public static void makeSink(Environmental E, Room room, int avg)
	{
		if((E==null)||(room==null)) return;

		Room R=room.getRoomInDir(Directions.DOWN);
		if(avg>0) R=room.getRoomInDir(Directions.UP);
		if((R==null)
		||((R.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
		   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)))
			return;

		if(((E instanceof MOB)&&(!Sense.isWaterWorthy(E))&&(!Sense.isInFlight(E))&&(E.envStats().weight()>=1))
		||((E instanceof Item)&&(!Sense.isInFlight(((Item)E).ultimateContainer()))&&(!Sense.isWaterWorthy(((Item)E).ultimateContainer()))))
			if(E.fetchEffect("Sinking")==null)
			{
				Ability sinking=CMClass.getAbility("Sinking");
				if(sinking!=null)
				{
					sinking.setProfficiency(avg);
					sinking.setAffectedOne(room);
					sinking.invoke(null,null,E,true,0);
				}
			}
	}

	public static void sinkAffects(Room room, CMMsg msg)
	{
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			MOB mob=msg.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(((Drink)room).thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}

		if(Sense.isSleeping(room))
			return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector needToFall=new Vector();
		Vector mightNeedAdjusting=new Vector();

		if((room.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(room.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob=room.fetchInhabitant(i);
				if((mob!=null)
				&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room)))
				{
					Ability A=mob.fetchEffect("Sinking");
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
					if((!Util.bset(mob.baseEnvStats().disposition(),EnvStats.IS_SWIMMING))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Amphibian"))
					&&(!mob.charStats().getMyRace().racialCategory().equals("Fish")))
						needToFall.addElement(mob);
				}
			}
		for(int i=0;i<room.numItems();i++)
		{
			Item item=room.fetchItem(i);
			if(item!=null)
			{
				Ability A=item.fetchEffect("Sinking");
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
			Ability A=E.fetchEffect("Sinking");
			if(A!=null) A.setProfficiency(avg);
		}
		for(int i=0;i<needToFall.size();i++)
			makeSink((Environmental)needToFall.elementAt(i),room,avg);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(UnderWater.isOkUnderWaterAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}
	public static int isOkUnderWaterAffect(Room room, CMMsg msg)
	{
		if(Sense.isSleeping(room))
			return 0;

		if((msg.targetMinor()==CMMsg.TYP_FIRE)
		||(msg.targetMinor()==CMMsg.TYP_GAS)
		||(msg.sourceMinor()==CMMsg.TYP_FIRE)
		||(msg.sourceMinor()==CMMsg.TYP_GAS))
		{
			msg.source().tell("That won't work underwater.");
			return -1;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon))
		{
			Weapon w=(Weapon)msg.tool();
			if((w.weaponType()==Weapon.TYPE_SLASHING)
			||(w.weaponType()==Weapon.TYPE_BASHING))
			{
				int damage=msg.value();
				damage=damage/3;
				damage=damage*2;
				msg.setValue(msg.value()-damage);
			}
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				msg.source().tell("You don't want to be drinking saltwater.");
				return -1;
			}
			return 1;
		}
		return 0;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}

	public int thirstQuenched(){return 500;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_SEAWEED),
		new Integer(EnvResource.RESOURCE_FISH),
		new Integer(EnvResource.RESOURCE_CATFISH),
		new Integer(EnvResource.RESOURCE_SALMON),
		new Integer(EnvResource.RESOURCE_CARP),
		new Integer(EnvResource.RESOURCE_TROUT),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_CLAY),
		new Integer(EnvResource.RESOURCE_LIMESTONE)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
