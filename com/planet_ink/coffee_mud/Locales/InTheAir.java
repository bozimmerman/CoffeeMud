package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class InTheAir extends StdRoom
{
	public String ID(){return "InTheAir";}
	public InTheAir()
	{
		super();
		baseEnvStats.setWeight(1);
		name="the sky";
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		return isOkAirAffect(this,msg);
	}

	public static void makeFall(Environmental E, Room room, int avg)
	{
		if((E==null)||(room==null)) return;

		if((avg==0)&&(room.getRoomInDir(Directions.DOWN)==null)) return;
		if((avg>0)&&(room.getRoomInDir(Directions.UP)==null)) return;

		if(((E instanceof MOB)&&(!Sense.isInFlight(E)))
		||((E instanceof Item)&&(!Sense.isFlying(((Item)E).ultimateContainer()))))
		{
			if(!Sense.isFalling(E))
			{
				Ability falling=CMClass.getAbility("Falling");
				if(falling!=null)
				{
					falling.setProfficiency(avg);
					falling.setAffectedOne(room);
					falling.invoke(null,null,E,true);
				}
			}
		}
	}

	public static void airAffects(Room room, CMMsg msg)
	{
		if(Sense.isSleeping(room)) return;
		boolean foundReversed=false;
		boolean foundNormal=false;
		Vector needToFall=new Vector();
		Vector mightNeedAdjusting=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if((mob!=null)
			&&((mob.getStartRoom()==null)||(mob.getStartRoom()!=room)))
			{
				Ability A=mob.fetchEffect("Falling");
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
				Ability A=item.fetchEffect("Falling");
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
			Ability A=E.fetchEffect("Falling");
			if(A!=null) A.setProfficiency(avg);
		}
		for(int i=0;i<needToFall.size();i++)
			makeFall((Environmental)needToFall.elementAt(i),room,avg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		InTheAir.airAffects(this,msg);
	}

	public static boolean isOkAirAffect(Room room, CMMsg msg)
	{
		if(Sense.isSleeping(room))
			return true;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.amITarget(room)))
		{
			MOB mob=msg.source();
			if((!Sense.isInFlight(mob))&&(!Sense.isFalling(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
			if(Dice.rollPercentage()>50)
			switch(room.getArea().getClimateObj().weatherType(room))
			{
			case Climate.WEATHER_BLIZZARD:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The swirling blizzard inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_HAIL:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The hail storm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_RAIN:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The rain storm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_SLEET:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The biting sleet inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_THUNDERSTORM:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The thunderstorm inhibits <S-YOUPOSS> progress.");
				return false;
			case Climate.WEATHER_WINDY:
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"The hard winds inhibit <S-YOUPOSS> progress.");
				return false;
			}
		}
		InTheAir.airAffects(room,msg);
		return true;
	}
}
