package com.planet_ink.coffee_mud.Behaviors;
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
public class WeatherAffects extends StdBehavior
{
	public String ID(){return "WeatherAffects";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	protected int puddlepct=50;
	protected int windsheer=10;
	
	public int pct(){return puddlepct;} // for puddles only
	
	public void setParms(String newParms)
	{
		parms=newParms;
		puddlepct=Util.getParmInt(parms,"puddlepct",50);
		windsheer=Util.getParmInt(parms,"windsheer",10);
	}
	
	public Area area(Environmental host)
	{
		Area A=(host instanceof Area)?(Area)host:CoffeeUtensils.roomLocation(host).getArea();
		return A;
	}
	
	public int weather(Environmental host, Room room)
	{
		if(room==null) return 0;
		Area A=(host instanceof Area)?(Area)host:CoffeeUtensils.roomLocation(host).getArea();
		if(A!=null) return A.getClimateObj().weatherType(room);
		return 0;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg)) return false;
		
		if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(msg.source().rangeToTarget()!=0)
		&&(msg.tool() instanceof Item)
		&&(!(msg.tool() instanceof Electronics)))
			switch(weather(host,msg.source().location()))
			{
			case Climate.WEATHER_WINDY:
			case Climate.WEATHER_THUNDERSTORM:
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_DUSTSTORM:
				if((msg.sourceMinor()==CMMsg.TYP_THROW)
				||((msg.tool() instanceof Weapon)
					&&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
					   ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN))))
				{
					if((Dice.rollPercentage()<windsheer)
					&&(msg.source().location()!=null))
					{
						msg.source().location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_ACTION,"The strong wind blows <S-YOUPOSS> attack against <T-NAMESELF> with <O-NAME> off target.");
						return false;
					}
				}
				break;
			}
		return true;
	}
}
